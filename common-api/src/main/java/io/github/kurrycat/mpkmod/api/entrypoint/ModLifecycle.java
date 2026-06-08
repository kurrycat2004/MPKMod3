package io.github.kurrycat.mpkmod.api.entrypoint;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ModLifecycle {
    ServiceHandle<ModLifecycle> HANDLE = Services.getHandle(ModLifecycle.class);

    /**
     * Initialization entrypoint.<br>
     * Gets called in one of the mod-loader entrypoints on game initialization.
     */
    void init();
}
