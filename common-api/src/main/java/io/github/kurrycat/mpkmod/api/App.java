package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

import java.util.List;
import java.util.ServiceLoader;

public final class App {
    private static final AppMetadata METADATA = ServiceLoader.load(AppMetadata.class, ServiceManager.class.getClassLoader())
            .findFirst()
            .orElseThrow();

    private App() {}

    public static String id() {
        return METADATA.id();
    }

    public static String name() {
        return METADATA.name();
    }

    public static String group() {
        return METADATA.group();
    }

    public static String version() {
        return METADATA.version();
    }

    public static String description() {
        return METADATA.description();
    }

    public static String license() {
        return METADATA.license();
    }

    public static String logoFile() {
        return METADATA.logoFile();
    }

    public static List<String> authors() {
        return METADATA.authors();
    }

    public static String homepage() {
        return METADATA.homepage();
    }

    public static String sources() {
        return METADATA.sources();
    }

    public static String issues() {
        return METADATA.issues();
    }
}
