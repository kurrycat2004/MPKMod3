package io.github.kurrycat.mpkmod.service;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.Services;
import io.github.kurrycat.mpkmod.log.StdoutLogger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

@AutoService(ServiceManager.class)
public final class ServiceManagerImpl implements ServiceManager {
    private final Map<Class<?>, List<ServiceProvider>> cache;

    private ILogger LOGGER;
    private boolean initialized = false;

    private final ClassValue<RawServiceHolder<?>> HOLDERS = new ClassValue<>() {
        @Override
        protected RawServiceHolder<?> computeValue(@NotNull Class<?> service) {
            return new RawServiceHolder<>(service, rawLoadOrThrow(service));
        }
    };

    public ServiceManagerImpl() {
        LOGGER = StdoutLogger.FALLBACK
                .createSubLogger(ServiceManager.class.getSimpleName());
        LOGGER.debug("Initialized service manager using class loader: {}",
                ServiceManagerImpl.class.getClassLoader());

        Map<Class<?>, List<ServiceProvider>> cache = new HashMap<>();
        for (ServiceProvider provider : ServiceLoader.load(ServiceProvider.class, ServiceManager.class.getClassLoader())) {
            cache.computeIfAbsent(provider.type(), _ -> new ArrayList<>())
                    .add(provider);
        }
        for (List<ServiceProvider> providers : cache.values()) {
            providers.sort(Comparator.comparingInt(ServiceProvider::priority).reversed());
        }
        this.cache = new HashMap<>();
        for (Map.Entry<Class<?>, List<ServiceProvider>> entry : cache.entrySet()) {
            List<ServiceProvider> providers = entry.getValue();

            List<ServiceProviderWrapper> wrappedProviders = new ArrayList<>(providers.size());
            for (int i = 0; i < providers.size(); i++) {
                wrappedProviders.add(new ServiceProviderWrapper(providers.get(i), i));
            }
            this.cache.put(entry.getKey(), Collections.unmodifiableList(wrappedProviders));
        }
    }

    @Override
    public void initialize() {
        if (initialized) return;
        LOGGER.debug("Initializing logger service...");
        // don't use ILogger.createLogger() here to prevent early initializing the Holder subclass, which would cause a cycle
        LOGGER = Services.getHandle(LogManager.class).get()
                .createLogger(App.id() + "/" + ServiceManager.class.getSimpleName());
        if (LOGGER instanceof StdoutLogger) {
            LOGGER.info("Failed to initialize logger service, continuing with fallback logger");
        } else {
            LOGGER.info("Initialized logger service");
        }
        initialized = true;
    }

    @Override
    public <S> ServiceHandle<S> getHandle(Class<S> serviceClass) {
        @SuppressWarnings("unchecked")
        RawServiceHolder<S> holder = (RawServiceHolder<S>) HOLDERS.get(serviceClass);
        return holder;
    }

    @Override
    public <S> List<ServiceProvider> getProviders(Class<S> serviceClass) {
        return cache.getOrDefault(serviceClass, Collections.emptyList());
    }

    @Override
    public void switchToProvider(ServiceProvider provider) {
        List<ServiceProvider> providers;
        if (
                !(provider instanceof ServiceProviderWrapper wrapper) ||
                (providers = cache.get(provider.type())) == null ||
                wrapper.id() < 0 || wrapper.id() >= providers.size() ||
                providers.get(wrapper.id()) != provider
        ) {
            throw new IllegalArgumentException("Tried to switch to unregistered service provider: " + provider.name());
        }

        Optional<String> reason = provider.invalidReason();
        if (reason.isPresent()) {
            throw new IllegalArgumentException("Failed to switch to service provider " + provider.name() + ": " + reason.get());
        }

        LOGGER.info("Switching service {} from {} to provider {}",
                provider.type().getName(),
                HOLDERS.get(provider.type()).get().getClass().getName(),
                provider.name()
        );
        HOLDERS.get(provider.type()).switchTo(provider);
    }

    private Object rawLoadOrThrow(Class<?> serviceClass) {
        Map<ServiceProvider, String> reasons = new IdentityHashMap<>();
        List<ServiceProvider> providers = cache.get(serviceClass);

        LOGGER.debug("Loading service provider for {}", serviceClass.getName());
        if (providers == null || providers.isEmpty()) {
            LOGGER.debug("No service provider found for ", serviceClass.getName());
            throw new IllegalArgumentException("No service provider found for " + serviceClass.getName());
        }
        LOGGER.debug("Found {} potential provider(s):", providers.size());
        for (ServiceProvider provider : providers) {
            LOGGER.debug("\t{} with priority {}", provider.name(), provider.priority());
        }

        for (ServiceProvider provider : providers) {
            Optional<String> reason = provider.invalidReason();
            if (reason.isPresent()) {
                LOGGER.debug("Service provider {} with priority {} is invalid: {}",
                        provider.name(), provider.priority(), reason.get());
                reasons.put(provider, reason.get());
                continue;
            }
            Object service = provider.provide();
            LOGGER.debug("Selecting service provider {} with priority {}",
                    provider.name(), provider.priority());
            return service;
        }
        StringBuilder sb = new StringBuilder("No valid provider found for ")
                .append(serviceClass.getName())
                .append(": ")
                .append("All providers found are invalid: ");
        for (ServiceProvider provider : providers) {
            sb.append("\n\t").append(provider.name());
            String reason = reasons.get(provider);
            if (reason == null) {
                sb.append(" (no reason given)");
            } else {
                sb.append(" (").append(reason).append(")");
            }
        }

        throw new IllegalArgumentException(sb.toString());
    }
}
