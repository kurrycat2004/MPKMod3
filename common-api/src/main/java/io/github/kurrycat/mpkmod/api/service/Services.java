package io.github.kurrycat.mpkmod.api.service;

import java.util.List;
import java.util.ServiceLoader;

public final class Services {
    private static final ServiceManager INSTANCE = ServiceLoader.load(ServiceManager.class, ServiceManager.class.getClassLoader())
            .findFirst()
            .orElseThrow();

    static {
        INSTANCE.initialize();
    }

    public static <S> ServiceHandle<S> getHandle(Class<S> serviceClass) {
        return INSTANCE.getHandle(serviceClass);
    }

    public static <S> List<ServiceProvider> getProviders(Class<S> serviceClass) {
        return INSTANCE.getProviders(serviceClass);
    }

    public static void switchToService(ServiceProvider provider) {
        INSTANCE.switchToProvider(provider);
    }
}
