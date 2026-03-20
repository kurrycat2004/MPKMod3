package io.github.kurrycat.mpkmod.lwjgl.lwjgl3;

import io.github.kurrycat.mpkmod.lwjgl.api.IGL30;
import org.lwjgl.opengl.GL30C;

abstract class GL30Impl extends GL20Impl implements IGL30 {
    @Override
    public int glGenVertexArrays() {
        return GL30C.glGenVertexArrays();
    }

    @Override
    public void glBindVertexArray(int array) {
        GL30C.glBindVertexArray(array);
    }
}
