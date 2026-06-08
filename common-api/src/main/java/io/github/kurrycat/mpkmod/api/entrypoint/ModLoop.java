package io.github.kurrycat.mpkmod.api.entrypoint;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ModLoop {
    ServiceHandle<ModLoop> HANDLE = Services.getHandle(ModLoop.class);

    void tick();

    void frame();
}
