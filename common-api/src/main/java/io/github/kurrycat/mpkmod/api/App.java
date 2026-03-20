package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

import java.util.ServiceLoader;

public final class App {
    private static final AppMetadata METADATA = ServiceLoader.load(AppMetadata.class, ServiceManager.class.getClassLoader())
            .findFirst()
            .orElseThrow();

    private App() {}

    public static String id() {
        return METADATA.id();
    }

    public static String version() {
        return METADATA.version();
    }

    public static String name() {
        return METADATA.name();
    }

    public static String group() {
        return METADATA.group();
    }
}
