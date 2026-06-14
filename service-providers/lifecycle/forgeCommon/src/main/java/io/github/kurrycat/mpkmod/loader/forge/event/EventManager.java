package io.github.kurrycat.mpkmod.loader.forge.event;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.loader.forge.event.fml.FMLEventReceiverProvider;
import io.github.kurrycat.mpkmod.loader.forge.event.forge.ForgeEventReceiverProvider;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EventManager {
    private EventManager() {}

    private static final ILogger LOGGER = ILogger.createLogger(EventManager.class.getSimpleName());

    private static Consumer<Object> forgeEventBusRegisterMethod = null;

    public static void registerFMLEventReceiver(Consumer<Object> registerMethod) {
        getProviderStream(FMLEventReceiverProvider.class)
                .forEach(p -> registerEventReceiver(registerMethod, p));
    }

    public static void registerForgeEventReceiver(Consumer<Object> registerMethod) {
        forgeEventBusRegisterMethod = registerMethod;
    }

    public static void runRegisterForgeEventReceiver() {
        if (forgeEventBusRegisterMethod == null) {
            throw new IllegalStateException("No forge event receiver set");
        }
        getProviderStream(ForgeEventReceiverProvider.class)
                .forEach(p -> registerEventReceiver(
                        forgeEventBusRegisterMethod, p
                ));
        forgeEventBusRegisterMethod = null;
    }

    private static <T extends EventReceiverProvider> Stream<T> getProviderStream(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(EventManager::filterProvider)
                .collect(Collectors.groupingBy(EventReceiverProvider::eventType))
                .entrySet().stream()
                .map(EventManager::selectProvider);
    }

    private static <T extends EventReceiverProvider> T selectProvider(
            Map.Entry<String, List<T>> providersForEventType
    ) {
        return providersForEventType.getValue().stream()
                .collect(Collectors.groupingBy(
                        EventReceiverProvider::eventTypePriority,
                        TreeMap::new,
                        Collectors.toList()
                ))
                .lastEntry().getValue().stream()
                .reduce(EventManager::warnDuplicateAndSelectFirst)
                .orElseThrow();
    }

    private static <T extends EventReceiverProvider> boolean filterProvider(T provider) {
        boolean canProvide = provider.canProvide();
        if (!canProvide) {
            LOGGER.debug(
                    "Skipping event provider {}: can not provide",
                    provider.getClass().getName()
            );
        }
        return canProvide;
    }

    private static <T extends EventReceiverProvider> T warnDuplicateAndSelectFirst(T first, T duplicate) {
        LOGGER.warn(
                "Found duplicate event receiver for event type {} with same priority {}. Using {}, ignoring {}",
                first.eventType(),
                first.eventTypePriority(),
                first.getClass().getName(),
                duplicate.getClass().getName()
        );
        return first;
    }

    private static void registerEventReceiver(
            Consumer<Object> registerMethod,
            EventReceiverProvider provider
    ) {
        IEventReceiver receiver = provider.provide();

        LOGGER.debug(
                "Registering event receiver {} for event type {}",
                receiver.getClass().getName(),
                provider.eventType()
        );

        registerMethod.accept(receiver);
    }
}
