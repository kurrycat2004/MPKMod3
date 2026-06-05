package io.github.kurrycat.mpkmod.api.loader;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ForgeModContainer {
    ServiceHandle<ForgeModContainer> HANDLE = Services.getHandle(ForgeModContainer.class);

    Class<?> modContainerImplementation();
}
