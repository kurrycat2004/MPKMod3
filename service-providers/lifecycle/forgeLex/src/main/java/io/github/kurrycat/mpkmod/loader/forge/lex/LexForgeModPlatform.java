package io.github.kurrycat.mpkmod.loader.forge.lex;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.loader.forge.CommonForgeEntrypoint;
import io.github.kurrycat.mpkmod.service.util.ServiceUtil;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class LexForgeModPlatform implements ModPlatform {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModPlatform> {
        public Provider() {
            super(LexForgeModPlatform::new, ModPlatform.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!ServiceUtil.doesClassExist("net.minecraftforge.versions.forge.ForgeVersion")) {
                return Optional.of("net.minecraftforge.versions.forge.ForgeVersion not found");
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
        return ForgeVersion.getVersion();
    }

    @Override
    public Path gamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path gameConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public List<Path> rootPaths() {
        return CommonForgeEntrypoint.INSTANCE.rootPaths();
    }
}
