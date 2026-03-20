package io.github.kurrycat.mpkmod.lwjgl.lwjgl2;

import io.github.kurrycat.mpkmod.lwjgl.api.IGL30;
import org.lwjgl.opengl.GL30;

abstract class GL30Impl extends GL20Impl implements IGL30 {
    @Override
    public int glGenVertexArrays() {
        return GL30.glGenVertexArrays();
    }

    @Override
    public void glBindVertexArray(int array) {
        GL30.glBindVertexArray(array);
    }
}
