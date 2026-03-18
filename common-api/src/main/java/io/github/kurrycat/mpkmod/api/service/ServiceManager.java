package io.github.kurrycat.mpkmod.api.service;

import java.util.List;

public interface ServiceManager {
    void initialize();

    <S> ServiceHandle<S> getHandle(Class<S> serviceClass);

    <S> List<ServiceProvider> getProviders(Class<S> serviceClass);

    void switchToProvider(ServiceProvider provider);
}
