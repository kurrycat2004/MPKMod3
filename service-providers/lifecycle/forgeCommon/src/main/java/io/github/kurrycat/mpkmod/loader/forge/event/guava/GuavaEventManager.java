package io.github.kurrycat.mpkmod.loader.forge.event.guava;

import com.google.common.eventbus.EventBus;
import io.github.kurrycat.mpkmod.api.log.ILogger;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class GuavaEventManager {
    private GuavaEventManager() {}

    private static final ILogger LOGGER = ILogger.createLogger(GuavaEventManager.class.getSimpleName());

    public static void registerEventReceivers(EventBus bus) {
        ServiceLoader.load(GuavaEventReceiverProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(GuavaEventManager::filterProvider)
                .collect(Collectors.groupingBy(GuavaEventReceiverProvider::eventType))
                .entrySet().stream()
                .map(GuavaEventManager::selectProvider)
                .forEach(provider -> registerEventReceiver(bus, provider));
    }

    private static GuavaEventReceiverProvider selectProvider(
            Map.Entry<String, List<GuavaEventReceiverProvider>> providersForEventType
    ) {
        return providersForEventType.getValue().stream()
                .collect(Collectors.groupingBy(
                        GuavaEventReceiverProvider::eventTypePriority,
                        TreeMap::new,
                        Collectors.toList()
                ))
                .lastEntry().getValue().stream()
                .reduce(GuavaEventManager::warnDuplicateAndSelectFirst)
                .orElseThrow();
    }

    private static boolean filterProvider(GuavaEventReceiverProvider provider) {
        boolean canProvide = provider.canProvide();
        if (!canProvide) {
            LOGGER.debug("Skipping event provider {}: can not provide", provider.getClass().getName());
        }
        return canProvide;
    }

    private static GuavaEventReceiverProvider warnDuplicateAndSelectFirst(
            GuavaEventReceiverProvider first,
            GuavaEventReceiverProvider duplicate
    ) {
        LOGGER.warn(
                "Found duplicate event receiver for event type {} with same priority {}. Using {}, ignoring {}",
                first.eventType(),
                first.eventTypePriority(),
                first.getClass().getName(),
                duplicate.getClass().getName()
        );
        return first;
    }

    private static void registerEventReceiver(EventBus bus, GuavaEventReceiverProvider provider) {
        IGuavaEventReceiver receiver = provider.provide();

        LOGGER.debug(
                "Registering event receiver {} for event type {}",
                receiver.getClass().getName(),
                provider.eventType()
        );

        bus.register(receiver);
    }
}
