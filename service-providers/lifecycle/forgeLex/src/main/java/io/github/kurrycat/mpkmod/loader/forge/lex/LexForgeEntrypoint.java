package io.github.kurrycat.mpkmod.loader.forge.lex;

import io.github.kurrycat.mpkmod.ModMetadata;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import net.minecraftforge.fml.common.Mod;

@Mod(ModMetadata.MOD_ID)
public class LexForgeEntrypoint {
    public LexForgeEntrypoint() {
        ModLifecycle.HANDLE.get().init();
    }
}
