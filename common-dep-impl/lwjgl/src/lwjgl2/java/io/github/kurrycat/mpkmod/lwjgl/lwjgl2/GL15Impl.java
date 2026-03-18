package io.github.kurrycat.mpkmod.lwjgl.lwjgl2;

import io.github.kurrycat.mpkmod.lwjgl.api.IGL15;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

abstract class GL15Impl extends GL11Impl implements IGL15 {
    @Override
    public int glGenBuffers() {
        return GL15.glGenBuffers();
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        GL15.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, ByteBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, IntBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, FloatBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }
}
