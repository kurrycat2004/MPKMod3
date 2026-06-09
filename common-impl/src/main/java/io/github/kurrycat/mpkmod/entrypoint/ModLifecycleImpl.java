package io.github.kurrycat.mpkmod.entrypoint;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;

import java.nio.file.Path;

public class ModLifecycleImpl implements ModLifecycle {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModLifecycle> {
        public Provider() {
            super(ModLifecycleImpl::new, ModLifecycle.class);
        }
    }

    private final ILogger LOGGER = ILogger.createLogger(ModLifecycle.class.getSimpleName());

    @Override
    public void init() {
        ModPlatform modPlatform = ModPlatform.HANDLE.get();

        LOGGER.info("Initialized {} ({}) ModPlatform for loader({}), mc_version({})",
                App.name(), App.id(), modPlatform.loader(), modPlatform.mcVersion()
        );
        Path rootPath = modPlatform.rootPaths().getFirst();
        LOGGER.info("Mod source file system: {}", rootPath.getFileSystem());

        ModuleRegistry.HANDLE.get().loadAllModules();
    }
}
