package io.github.kurrycat.mpkmod;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.AppMetadata;

@AutoService(AppMetadata.class)
public class AppMetadataImpl implements AppMetadata {
    @Override
    public String id() {
        return Tags.MOD_ID;
    }

    @Override
    public String version() {
        return Tags.MOD_VERSION;
    }

    @Override
    public String name() {
        return Tags.MOD_NAME;
    }

    @Override
    public String group() {
        return Tags.MOD_GROUP;
    }
}
