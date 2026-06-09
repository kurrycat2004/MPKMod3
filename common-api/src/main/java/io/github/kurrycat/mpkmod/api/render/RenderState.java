package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;
import io.github.kurrycat.mpkmod.shadedlibs.joml.Matrix4f;

public interface RenderState {
    ServiceHandle<RenderState> HANDLE = Services.getHandle(RenderState.class);

    RenderLayer layer();

    void projectionMatrix(Matrix4f out);
}
