package io.github.kurrycat.mpkmod.entrypoint;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLoop;
import io.github.kurrycat.mpkmod.api.module.ModuleEntrypoint;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;

public class ModLoopImpl implements ModLoop {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModLoop> {
        public Provider() {
            super(ModLoopImpl::new, ModLoop.class);
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void frame() {
        final ModuleRegistry registry = ModuleRegistry.HANDLE.get();
        for (ModuleEntrypoint entrypoint : registry.loadedEntrypoints()) {
            entrypoint.onFrame();
        }
    }
}
