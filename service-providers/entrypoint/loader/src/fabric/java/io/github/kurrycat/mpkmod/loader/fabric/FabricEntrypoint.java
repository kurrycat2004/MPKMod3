package io.github.kurrycat.mpkmod.loader.fabric;

import io.github.kurrycat.mpkmod.api.minecraft.ModPlatform;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        ModPlatform.HANDLE.get().init();
    }
}
