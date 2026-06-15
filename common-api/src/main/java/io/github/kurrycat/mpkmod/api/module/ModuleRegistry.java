package io.github.kurrycat.mpkmod.api.module;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface ModuleRegistry {
    ServiceHandle<ModuleRegistry> HANDLE = Services.getHandle(ModuleRegistry.class);

    void loadAllModules();

    boolean isModuleLoaded(String moduleId);

    @UnmodifiableView
    Collection<ModuleEntrypoint> loadedEntrypoints();
}
