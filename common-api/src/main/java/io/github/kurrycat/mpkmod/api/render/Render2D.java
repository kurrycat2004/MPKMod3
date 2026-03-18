package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface Render2D {
    ServiceHandle<Render2D> HANDLE = Services.getHandle(Render2D.class);

    void pushRect(float x, float y, float w, float h, int argb);
}
