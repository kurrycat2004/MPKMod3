package io.github.kurrycat.mpkmod.loader.fabric;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FabricModPlatform implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModPlatform> {
        public Provider() {
            super(FabricModPlatform::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("net.fabricmc.loader.api.FabricLoader")) {
                return Optional.of("FabricLoader not found");
            }
            return super.invalidReason();
        }
    }

    private ModContainer modContainer = null;

    @Override
    public String loader() {
        return "fabric";
    }

    @Override
    public String mcVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
                .orElseThrow(() -> new IllegalStateException("Minecraft not found (?)"))
                .getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public Path gamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path gameConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public List<Path> rootPaths() {
        if (modContainer == null) {
            modContainer = FabricLoader.getInstance().getModContainer(App.id())
                    .orElseThrow(() -> new IllegalStateException("Mod not found: " + App.id()));
        }
        return modContainer.getRootPaths();
    }
}