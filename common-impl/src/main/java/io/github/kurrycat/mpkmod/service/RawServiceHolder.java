package io.github.kurrycat.mpkmod.service;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;

public final class RawServiceHolder<S> implements ServiceHandle<S> {
    private final Class<S> serviceType;
    private volatile ServiceProvider pending;
    private volatile S impl;

    public RawServiceHolder(Class<S> type, Object initial) {
        serviceType = type;
        impl = type.cast(initial);
    }

    @Override
    public S get() {
        return impl;
    }

    @Override
    public void readyForSwitch() {
        if (pending == null) return;
        switchToPending();
    }

    void switchTo(ServiceProvider provider) {
        pending = provider;
        if (!provider.deferSwitch()) {
            switchToPending();
        }
    }

    private synchronized void switchToPending() {
        ServiceProvider p = pending;
        if (p == null) return;
        Object service = p.provide();
        impl = serviceType.cast(service);
        pending = null;
    }
}
