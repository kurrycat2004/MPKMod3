package io.github.kurrycat.mpkmod.api.render.texture;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface TextureManager {
    ServiceHandle<TextureManager> HANDLE = Services.getHandle(TextureManager.class);

    void bindTexture(IResource texture);
}
