package io.github.kurrycat.mpkmod.loader.fabric;

import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import net.fabricmc.api.ModInitializer;

public class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        ModLifecycle.HANDLE.get().init();
    }
}
