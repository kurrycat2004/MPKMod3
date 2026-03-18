package io.github.kurrycat.mpkmod.api.resource;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceManager {
    ServiceHandle<ResourceManager> HANDLE = Services.getHandle(ResourceManager.class);

    IResource resource(String domain, String path);

    InputStream inputStream(IResource resource) throws IOException;
}
