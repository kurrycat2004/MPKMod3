package io.github.kurrycat.mpkmod.loader.forge;

import io.github.kurrycat.mpkmod.ModMetadata;
import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import net.minecraftforge.fml.common.Mod;

@Mod(ModMetadata.MOD_ID)
public class ForgeEntrypoint {
    public ForgeEntrypoint() {
        ModPlatform.HANDLE.get().init();
    }
}
