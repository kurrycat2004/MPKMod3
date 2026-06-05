package io.github.kurrycat.mpkmod.api;

import java.util.List;

public interface AppMetadata {
    String id();

    String name();

    String group();

    String version();

    String description();

    String license();

    String logoFile();

    List<String> authors();

    String homepage();

    String sources();

    String issues();
}
