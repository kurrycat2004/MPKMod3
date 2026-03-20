package io.github.kurrycat.mpkmod.lwjgl.api;

public interface IGL11 {
    void glEnable(int target);

    void glDisable(int target);

    boolean glIsEnabled(int cap);

    int glGetInteger(int pname);

    void glBlendFunc(int sfactor, int dfactor);

    void glShadeModel(int mode);

    void glColor4f(float red, float green, float blue, float alpha);

    void glVertexPointer(int size, int type, int stride, long pointer);

    void glColorPointer(int size, int type, int stride, long pointer);

    void glTexCoordPointer(int size, int type, int stride, long pointer);

    void glDrawElements(int mode, int count, int type, long indices);

    void glEnableClientState(int cap);

    void glDisableClientState(int cap);
}
