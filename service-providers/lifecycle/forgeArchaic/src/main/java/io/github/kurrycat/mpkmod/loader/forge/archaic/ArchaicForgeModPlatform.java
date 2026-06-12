package io.github.kurrycat.mpkmod.loader.forge.archaic;

import com.google.auto.service.AutoService;
import cpw.mods.fml.common.Loader;
import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.loader.forge.CommonForgeEntrypoint;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ArchaicForgeModPlatform implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModPlatform> {
        public Provider() {
            super(ArchaicForgeModPlatform::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("cpw.mods.fml.common.Loader")) {
                return Optional.of("cpw.mods.fml.common.Loader not found");
            }
            return super.invalidReason();
        }
    }

    @Override
    public String loader() {
        return "forge";
    }

    @Override
    public String mcVersion() {
        return Loader.MC_VERSION;
    }

    @Override
    public Path gamePath() {
        // should be fine
        return Loader.instance().getConfigDir().toPath().getParent();
    }

    @Override
    public Path gameConfigPath() {
        return Loader.instance().getConfigDir().toPath();
    }

    @Override
    public List<Path> rootPaths() {
        return CommonForgeEntrypoint.INSTANCE.rootPaths();
    }
}
