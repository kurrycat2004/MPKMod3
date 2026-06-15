package io.github.kurrycat.mpkmod.lwjgl;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.DrawMode;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.RenderState;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.resource.ResourceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.lwjgl.api.IGL30;
import io.github.kurrycat.mpkmod.lwjgl.api.IGLCapabilities;
import io.github.kurrycat.mpkmod.lwjgl.api.LwjglBackend;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC11;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC15;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC20;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC30;
import io.github.kurrycat.mpkmod.shadedlibs.joml.Matrix4f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GL33RenderBackend implements RenderBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<RenderBackend> {
        public Provider() {
            super(GL33RenderBackend::new, RenderBackend.class);
        }

        @Override
        public int priority() {
            return 33;
        }

        @Override
        public Optional<String> invalidReason() {
            IGLCapabilities caps = LwjglBackend.HANDLE.get().capabilities();
            if (!caps.OpenGL33()) {
                return Optional.of("OpenGL 3.3 not supported");
            }
            return Optional.empty();
        }

        @Override
        public boolean deferSwitch() {
            return true;
        }
    }

    private static final int[] RENDER_MODES = {
            GLC11.GL_TRIANGLES,
            GLC11.GL_LINES,
    };

    static {
        assert RENDER_MODES.length == DrawMode.VALUES.length;
    }

    private final int vao;
    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    private final int shaderProgram;
    private final int uProjectionLoc;
    private final int uTexturedLoc;

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer projectionBuffer = BufferUtil.allocDirectFloat(16);

    private final GlStateSnapshot snapshot;

    public GL33RenderBackend() {
        final IGL30 gl30 = LwjglBackend.HANDLE.get().gl30();
        final ResourceManager resourceManager = ResourceManager.HANDLE.get();

        vao = gl30.glGenVertexArrays();
        vboPos = gl30.glGenBuffers();
        vboUV = gl30.glGenBuffers();
        vboCol = gl30.glGenBuffers();
        ebo = gl30.glGenBuffers();
        snapshot = new GlStateSnapshot();

        gl30.glBindVertexArray(vao);
        bindAttrib(gl30, vboPos, 0, 3, GLC11.GL_FLOAT, false);
        bindAttrib(gl30, vboCol, 1, 4, GLC11.GL_UNSIGNED_BYTE, true);
        bindAttrib(gl30, vboUV, 2, 2, GLC11.GL_FLOAT, false);
        gl30.glBindVertexArray(0);

        try {
            shaderProgram = ShaderUtil.createProgram(
                    resourceManager.resource("shaders/gl33.vert"),
                    resourceManager.resource("shaders/gl33.frag")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shaders", e);
        }

        uProjectionLoc = gl30.glGetUniformLocation(shaderProgram, "uProjection");
        uTexturedLoc = gl30.glGetUniformLocation(shaderProgram, "uTextured");

        reallocVertexBuffers(
                CommandReceiver.INITIAL_VERTEX_BUFFER_SIZE * 3,
                CommandReceiver.INITIAL_VERTEX_BUFFER_SIZE * 4,
                CommandReceiver.INITIAL_VERTEX_BUFFER_SIZE * 2
        );
        reallocIndexBuffer(CommandReceiver.INITIAL_INDEX_BUFFER_SIZE);
    }

    @Override
    public void reallocVertexBuffers(int posSizeFloats, int colorSizeBytes, int uvSizeFloats) {
        vertexPositions = BufferUtil.reallocFloat(vertexPositions, posSizeFloats);
        vertexUVs = BufferUtil.reallocFloat(vertexUVs, uvSizeFloats);
        vertexColors = BufferUtil.reallocByte(vertexColors, colorSizeBytes);
    }

    @Override
    public void reallocIndexBuffer(int sizeInts) {
        indices = BufferUtil.reallocInt(indices, sizeInts);
    }

    @Override
    public FloatBuffer vertexPositions() {return vertexPositions;}

    @Override
    public ByteBuffer vertexColors() {return vertexColors;}

    @Override
    public FloatBuffer vertexUVs() {return vertexUVs;}

    @Override
    public IntBuffer indices() {return indices;}

    @Override
    public void flush(List<IDrawCommand> commands) {
        final IGL30 gl30 = LwjglBackend.HANDLE.get().gl30();

        vertexPositions.flip();
        vertexUVs.flip();
        vertexColors.flip();
        indices.flip();

        snapshot.capture(gl30);

        gl30.glUseProgram(shaderProgram);

        RenderState.HANDLE.get().projectionMatrix(projectionMatrix);
        projectionMatrix.get(projectionBuffer);
        gl30.glUniformMatrix4fv(uProjectionLoc, false, projectionBuffer);

        gl30.glBindVertexArray(vao);

        gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboPos);
        gl30.glEnableVertexAttribArray(0);
        gl30.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexPositions, GLC15.GL_DYNAMIC_DRAW);

        gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboCol);
        gl30.glEnableVertexAttribArray(1);
        gl30.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexColors, GLC15.GL_DYNAMIC_DRAW);

        gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboUV);
        gl30.glEnableVertexAttribArray(2);
        gl30.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexUVs, GLC15.GL_DYNAMIC_DRAW);

        gl30.glBindBuffer(GLC15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        gl30.glBufferData(GLC15.GL_ELEMENT_ARRAY_BUFFER, indices, GLC15.GL_DYNAMIC_DRAW);

        TextureManager textureManager = TextureManager.HANDLE.get();
        IResource lastTex = null;

        for (IDrawCommand cmd : commands) {
            IResource tex = cmd.texture();
            if (!Objects.equals(tex, lastTex)) {
                if (tex != null) {
                    gl30.glUniform1i(uTexturedLoc, 1);
                    textureManager.bindTexture(tex);
                } else {
                    gl30.glUniform1i(uTexturedLoc, 0);
                }
                lastTex = tex;
            }

            gl30.glDrawElements(
                    RENDER_MODES[cmd.mode().ordinal()],
                    cmd.count(),
                    GLC11.GL_UNSIGNED_INT,
                    (long) cmd.startIdx() * Integer.BYTES
            );
        }

        gl30.glDisableVertexAttribArray(0);
        gl30.glDisableVertexAttribArray(1);
        gl30.glDisableVertexAttribArray(2);

        snapshot.restore(gl30);

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();

        LwjglBackend.HANDLE.readyForSwitch();
    }

    private static void bindAttrib(IGL30 gl30, int buffer, int index, int size, int type, boolean normalized) {
        gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, buffer);
        gl30.glEnableVertexAttribArray(index);
        gl30.glVertexAttribPointer(index, size, type, normalized, 0, 0L);
        gl30.glDisableVertexAttribArray(index);
        gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, 0);
    }

    private static final class GlStateSnapshot {
        private int shaderProgram;
        private int vao;
        private int arrayBuffer;
        private int elementBuffer;

        void capture(IGL30 gl30) {
            shaderProgram = gl30.glGetInteger(GLC20.GL_CURRENT_PROGRAM);
            vao = gl30.glGetInteger(GLC30.GL_VERTEX_ARRAY_BINDING);
            arrayBuffer = gl30.glGetInteger(GLC15.GL_ARRAY_BUFFER_BINDING);
            elementBuffer = gl30.glGetInteger(GLC15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        }

        void restore(IGL30 gl30) {
            gl30.glUseProgram(shaderProgram);
            gl30.glBindVertexArray(vao);
            gl30.glBindBuffer(GLC15.GL_ARRAY_BUFFER, arrayBuffer);
            gl30.glBindBuffer(GLC15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        }
    }
}
