package io.github.kurrycat.mpkmod.lwjgl.api;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface LwjglBackend {
    ServiceHandle<LwjglBackend> HANDLE = Services.getHandle(LwjglBackend.class);

    IGLCapabilities capabilities();

    IGL11 gl11();

    IGL15 gl15();

    IGL20 gl20();

    IGL30 gl30();
}
