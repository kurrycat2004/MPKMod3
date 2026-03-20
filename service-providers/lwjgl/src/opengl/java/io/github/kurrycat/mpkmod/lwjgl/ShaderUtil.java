package io.github.kurrycat.mpkmod.lwjgl;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.lwjgl.api.IGL20;
import io.github.kurrycat.mpkmod.lwjgl.api.LwjglBackend;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC11;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC20;

import java.io.IOException;

public final class ShaderUtil {
    private ShaderUtil() {}

    public static int createProgram(IResource vertexShader, IResource fragmentShader) throws IOException {
        final IGL20 gl20 = LwjglBackend.HANDLE.get().gl20();

        String vertexSource = vertexShader.readUtf8();
        String fragmentSource = fragmentShader.readUtf8();

        int vs = compile(gl20, GLC20.GL_VERTEX_SHADER, vertexSource);
        int fs = compile(gl20, GLC20.GL_FRAGMENT_SHADER, fragmentSource);

        int program = gl20.glCreateProgram();
        gl20.glAttachShader(program, vs);
        gl20.glAttachShader(program, fs);
        gl20.glLinkProgram(program);

        if (gl20.glGetProgrami(program, GLC20.GL_LINK_STATUS) == GLC11.GL_FALSE) {
            int logLength = gl20.glGetProgrami(program, GLC20.GL_INFO_LOG_LENGTH);
            String log = gl20.glGetProgramInfoLog(program, logLength);
            throw new RuntimeException("Program link error:\n" + log);
        }

        gl20.glDetachShader(program, vs);
        gl20.glDetachShader(program, fs);
        gl20.glDeleteShader(vs);
        gl20.glDeleteShader(fs);

        return program;
    }

    private static int compile(IGL20 gl20, int type, String source) {
        int shader = gl20.glCreateShader(type);
        gl20.glShaderSource(shader, source);
        gl20.glCompileShader(shader);
        if (gl20.glGetShaderi(shader, GLC20.GL_COMPILE_STATUS) == GLC11.GL_FALSE) {
            int logLength = gl20.glGetShaderi(shader, GLC20.GL_INFO_LOG_LENGTH);
            String log = gl20.glGetShaderInfoLog(shader, logLength);
            throw new RuntimeException("Shader compile error (" + shaderTypeName(type) + "):\n" + log);
        }
        return shader;
    }

    private static String shaderTypeName(int type) {
        return switch (type) {
            case GLC20.GL_VERTEX_SHADER -> "vertex";
            case GLC20.GL_FRAGMENT_SHADER -> "fragment";
            default -> "unknown(" + type + ")";
        };
    }
}
