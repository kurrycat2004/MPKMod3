package io.github.kurrycat.mpkmod.lwjgl.api;

import java.nio.FloatBuffer;

public interface IGL20 extends IGL15 {
    int glCreateProgram();

    void glUseProgram(int program);

    void glLinkProgram(int program);

    int glGetProgrami(int program, int pname);

    String glGetProgramInfoLog(int program, int maxLength);

    int glCreateShader(int type);

    void glAttachShader(int program, int shader);

    void glDetachShader(int program, int shader);

    void glDeleteShader(int shader);

    void glShaderSource(int shader, CharSequence string);

    void glCompileShader(int shader);

    int glGetShaderi(int shader, int pname);

    String glGetShaderInfoLog(int shader, int maxLength);

    void glUniform1i(int location, int v0);

    void glUniformMatrix4fv(int location, boolean transpose, FloatBuffer value);

    int glGetUniformLocation(int program, CharSequence name);

    void glEnableVertexAttribArray(int index);

    void glDisableVertexAttribArray(int index);

    void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointer);
}
