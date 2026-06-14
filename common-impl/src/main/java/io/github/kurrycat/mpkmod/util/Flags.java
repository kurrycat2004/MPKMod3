package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.api.App;
import org.intellij.lang.annotations.MagicConstant;

import java.util.Locale;

public final class Flags {
    private static final String PREFIX = App.id() + ".";
    private static final boolean IGNORE_ENV_FLAGS = Boolean.getBoolean(PREFIX + "ignore_env_flags");

    public static final String ENABLE_MODULE_LOAD_STACKTRACE = "enable_module_load_stacktrace";
    public static final String LOGGER_FORCE_MODID_PREFIX = "logger_force_modid_prefix";
    public static final String LOGGER_FORCE_STDOUT = "logger_force_stdout";

    public static final String LOGGER_PREFIX = "logger";

    public static boolean getBoolean(@MagicConstant(valuesFromClass = Flags.class) String flag) {
        return Boolean.parseBoolean(getString(flag));
    }

    public static String getString(@MagicConstant(valuesFromClass = Flags.class) String flag) {
        return getValueOfKey(App.id() + "." + flag);
    }

    public static String getDynString(@MagicConstant(valuesFromClass = Flags.class) String prefix, String dynamicPart) {
        return getValueOfKey(App.id() + "." + prefix + "." + convertToPropKey(dynamicPart));
    }

    public static int getDynInt(
            @MagicConstant(valuesFromClass = Flags.class) String prefix,
            String dynamicPart,
            int defaultValue
    ) {
        String flag = getDynString(prefix, dynamicPart);
        return parseIntOrDefault(flag, defaultValue);
    }

    private static String getValueOfKey(String key) {
        String property = System.getProperty(key);
        if (property != null) return property;

        if (!IGNORE_ENV_FLAGS) {
            return System.getenv(convertToEnvKey(key));
        }
        return null;
    }

    private static String convertToPropKey(String key) {
        return key.replace('/', '.');
    }

    private static String convertToEnvKey(String key) {
        return key.replace('.', '_').toUpperCase(Locale.ROOT);
    }

    public static int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
