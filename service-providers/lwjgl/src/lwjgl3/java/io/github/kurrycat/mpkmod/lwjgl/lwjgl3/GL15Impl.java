package io.github.kurrycat.mpkmod.lwjgl.lwjgl3;

import io.github.kurrycat.mpkmod.lwjgl.api.IGL15;
import org.lwjgl.opengl.GL15C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

abstract class GL15Impl extends GL11Impl implements IGL15 {
    @Override
    public int glGenBuffers() {
        return GL15C.glGenBuffers();
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        GL15C.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, ByteBuffer data, int usage) {
        GL15C.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, IntBuffer data, int usage) {
        GL15C.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, FloatBuffer data, int usage) {
        GL15C.glBufferData(target, data, usage);
    }
}
