package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public interface RenderBackend {
    ServiceHandle<RenderBackend> HANDLE = Services.getHandle(RenderBackend.class);

    void reallocVertexBuffers(int posSize, int colorSize, int uvSize);

    void reallocIndexBuffer(int size);

    FloatBuffer vertexPositions();

    ByteBuffer vertexColors();

    FloatBuffer vertexUVs();

    IntBuffer indices();

    void flush(List<IDrawCommand> commands);
}
