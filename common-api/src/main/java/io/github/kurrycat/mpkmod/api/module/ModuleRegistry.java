package io.github.kurrycat.mpkmod.api.module;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ModuleRegistry {
    ServiceHandle<ModuleRegistry> HANDLE = Services.getHandle(ModuleRegistry.class);

    boolean isModuleLoaded(String moduleId);

    void loadAllModules();
}
