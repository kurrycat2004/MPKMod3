package io.github.kurrycat.mpkmod.lwjgl.lwjgl3;

import io.github.kurrycat.mpkmod.lwjgl.api.IGL11;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

abstract class GL11Impl implements IGL11 {
    @Override
    public void glEnable(int target) {
        GL11C.glEnable(target);
    }

    @Override
    public void glDisable(int target) {
        GL11C.glDisable(target);
    }

    @Override
    public boolean glIsEnabled(int cap) {
        return GL11C.glIsEnabled(cap);
    }

    @Override
    public int glGetInteger(int pname) {
        return GL11C.glGetInteger(pname);
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor) {
        GL11C.glBlendFunc(sfactor, dfactor);
    }

    @Override
    public void glShadeModel(int mode) {
        GL11.glShadeModel(mode);
    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, long pointer) {
        GL11.glVertexPointer(size, type, stride, pointer);
    }

    @Override
    public void glColorPointer(int size, int type, int stride, long pointer) {
        GL11.glColorPointer(size, type, stride, pointer);
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, long pointer) {
        GL11.glTexCoordPointer(size, type, stride, pointer);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, long indices) {
        GL11C.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glEnableClientState(int cap) {
        GL11.glEnableClientState(cap);
    }

    @Override
    public void glDisableClientState(int cap) {
        GL11.glDisableClientState(cap);
    }
}
