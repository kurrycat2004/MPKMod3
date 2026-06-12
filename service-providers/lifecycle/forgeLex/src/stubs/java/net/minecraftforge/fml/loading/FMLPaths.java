package net.minecraftforge.fml.loading;

import java.nio.file.Path;

public enum FMLPaths {
    GAMEDIR,
    MODSDIR,
    CONFIGDIR,
    ;

    public Path get() {
        throw new IllegalStateException("STUB");
    }
}
