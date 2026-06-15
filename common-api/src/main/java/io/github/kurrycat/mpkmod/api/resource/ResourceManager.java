package io.github.kurrycat.mpkmod.api.resource;

import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public interface ResourceManager {
    ServiceHandle<ResourceManager> HANDLE = Services.getHandle(ResourceManager.class);

    IResource resource(String domain, String path);

    default IResource resource(String path) {
        return resource(App.id(), path);
    }

    InputStream inputStream(IResource resource) throws IOException;

    default String readUTF8(IResource resource) throws IOException {
        try (InputStream stream = inputStream(resource)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
