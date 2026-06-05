package io.github.kurrycat.mpkmod;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.AppMetadata;

import java.util.Arrays;
import java.util.List;

@AutoService(AppMetadata.class)
public class AppMetadataImpl implements AppMetadata {
    private List<String> authors = null;

    @Override
    public String id() {
        return ModMetadata.MOD_ID;
    }

    @Override
    public String name() {
        return ModMetadata.MOD_NAME;
    }

    @Override
    public String group() {
        return ModMetadata.MOD_GROUP;
    }

    @Override
    public String version() {
        return ModMetadata.MOD_VERSION;
    }

    @Override
    public String description() {
        return ModMetadata.MOD_DESCRIPTION;
    }

    @Override
    public String license() {
        return ModMetadata.MOD_LICENSE;
    }

    @Override
    public String logoFile() {
        return ModMetadata.MOD_LOGO_FILE;
    }

    @Override
    public List<String> authors() {
        if (authors == null) {
            authors = Arrays.asList(ModMetadata.MOD_AUTHORS.split(", "));
        }
        return authors;
    }

    @Override
    public String homepage() {
        return ModMetadata.MOD_HOMEPAGE;
    }

    @Override
    public String sources() {
        return ModMetadata.MOD_SOURCES;
    }

    @Override
    public String issues() {
        return ModMetadata.MOD_ISSUES;
    }
}
