package io.github.kurrycat.mpkmod.api.service;

public interface ServiceHandle<S> {
    S get();

    void readyForSwitch();
}
