package io.github.kurrycat.mpkmod.lwjgl.api;

public interface IGLCapabilities {
    boolean OpenGL11();

    boolean OpenGL15();

    boolean OpenGL33();

    boolean GL_ARB_vertex_buffer_object();
}
