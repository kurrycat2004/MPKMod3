package io.github.kurrycat.mpkmod.lwjgl.api;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface IGL15 extends IGL11 {
    int glGenBuffers();

    void glBindBuffer(int target, int buffer);

    void glBufferData(int target, ByteBuffer data, int usage);

    void glBufferData(int target, IntBuffer data, int usage);

    void glBufferData(int target, FloatBuffer data, int usage);
}
