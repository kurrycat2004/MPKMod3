package cpw.mods.fml.common;

import java.io.File;

public class Loader {
    private Loader() {}

    public static final String MC_VERSION = noInline("STUB");

    @SuppressWarnings("SameParameterValue")
    private static String noInline(String value) {
        return value;
    }

    public static Loader instance() {
        throw new IllegalStateException("STUB");
    }

    public File getConfigDir() {
        throw new IllegalStateException("STUB");
    }
}
