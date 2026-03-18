package io.github.kurrycat.mpkmod.lwjgl;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.DrawMode;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.lwjgl.api.IGL11;
import io.github.kurrycat.mpkmod.lwjgl.api.IGL15;
import io.github.kurrycat.mpkmod.lwjgl.api.IGLCapabilities;
import io.github.kurrycat.mpkmod.lwjgl.api.LwjglBackend;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC11;
import io.github.kurrycat.mpkmod.lwjgl.constants.GLC15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GL11RenderBackend implements RenderBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<RenderBackend> {
        public Provider() {
            super(GL11RenderBackend::new, RenderBackend.class);
        }

        @Override
        public int priority() {
            return 11;
        }

        @Override
        public Optional<String> invalidReason() {
            IGLCapabilities caps = LwjglBackend.HANDLE.get().capabilities();
            if (!caps.OpenGL11()) {
                return Optional.of("Missing OpenGL 1.1 support");
            }
            if (!caps.OpenGL15() && !caps.GL_ARB_vertex_buffer_object()) {
                return Optional.of("Missing VBO support (OpenGL 1.5 or ARB_vbo)");
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

    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    private final GlStateSnapshot snapshot;

    public GL11RenderBackend() {
        final IGL15 gl15 = LwjglBackend.HANDLE.get().gl15();

        vboPos = gl15.glGenBuffers();
        vboUV = gl15.glGenBuffers();
        vboCol = gl15.glGenBuffers();
        ebo = gl15.glGenBuffers();
        snapshot = new GlStateSnapshot();

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
    public FloatBuffer vertexPositions() {
        return vertexPositions;
    }

    @Override
    public ByteBuffer vertexColors() {
        return vertexColors;
    }

    @Override
    public FloatBuffer vertexUVs() {
        return vertexUVs;
    }

    @Override
    public IntBuffer indices() {
        return indices;
    }

    @Override
    public void flush(List<IDrawCommand> commands) {
        final IGL15 gl15 = LwjglBackend.HANDLE.get().gl15();

        vertexPositions.flip();
        vertexUVs.flip();
        vertexColors.flip();
        indices.flip();

        snapshot.capture(gl15); // ensure GlStateManager stays in sync

        IResource lastTex = null;
        gl15.glDisable(GLC11.GL_TEXTURE_2D);
        gl15.glEnable(GLC11.GL_BLEND);
        gl15.glDisable(GLC11.GL_ALPHA_TEST);
        gl15.glDisable(GLC11.GL_DEPTH_TEST);
        gl15.glBlendFunc(GLC11.GL_SRC_ALPHA, GLC11.GL_ONE_MINUS_SRC_ALPHA);
        gl15.glShadeModel(GLC11.GL_FLAT);

        //TODO: check if GlStateManager.resetColor() is needed
        gl15.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboPos);
        gl15.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexPositions, GLC15.GL_DYNAMIC_DRAW);

        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboCol);
        gl15.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexColors, GLC15.GL_DYNAMIC_DRAW);

        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboUV);
        gl15.glBufferData(GLC15.GL_ARRAY_BUFFER, vertexUVs, GLC15.GL_DYNAMIC_DRAW);

        gl15.glBindBuffer(GLC15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        gl15.glBufferData(GLC15.GL_ELEMENT_ARRAY_BUFFER, indices, GLC15.GL_DYNAMIC_DRAW);

        gl15.glEnableClientState(GLC11.GL_VERTEX_ARRAY);
        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboPos);
        gl15.glVertexPointer(3, GLC11.GL_FLOAT, 0, 0);

        gl15.glEnableClientState(GLC11.GL_COLOR_ARRAY);
        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboCol);
        gl15.glColorPointer(4, GLC11.GL_UNSIGNED_BYTE, 0, 0);

        gl15.glEnableClientState(GLC11.GL_TEXTURE_COORD_ARRAY);
        gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, vboUV);
        gl15.glTexCoordPointer(2, GLC11.GL_FLOAT, 0, 0);

        gl15.glBindBuffer(GLC15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        TextureManager textureManager = TextureManager.HANDLE.get();
        for (IDrawCommand cmd : commands) {
            IResource tex = cmd.texture();
            if (!Objects.equals(tex, lastTex)) {
                if (tex != null) {
                    gl15.glEnable(GLC11.GL_TEXTURE_2D);
                    textureManager.bindTexture(tex);
                } else {
                    gl15.glDisable(GLC11.GL_TEXTURE_2D);
                }
                lastTex = tex;
            }

            gl15.glDrawElements(
                    RENDER_MODES[cmd.mode().ordinal()],
                    cmd.count(),
                    GLC11.GL_UNSIGNED_INT,
                    (long) cmd.startIdx() * Integer.BYTES
            );
        }

        gl15.glDisableClientState(GLC11.GL_VERTEX_ARRAY);
        gl15.glDisableClientState(GLC11.GL_TEXTURE_COORD_ARRAY);
        gl15.glDisableClientState(GLC11.GL_COLOR_ARRAY);

        snapshot.restore(gl15);

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();

        LwjglBackend.HANDLE.readyForSwitch();
    }

    private static final class GlStateSnapshot {
        private boolean texture2D, blend, alphaTest, depthTest;
        private int blendSrc, blendDst;
        private int shadeModel;
        // no texture binding, TextureManager handles that
        private int arrayBufferBinding;
        private int elementArrayBufferBinding;

        public void capture(IGL11 gl11) {
            texture2D = gl11.glIsEnabled(GLC11.GL_TEXTURE_2D);
            blend = gl11.glIsEnabled(GLC11.GL_BLEND);
            alphaTest = gl11.glIsEnabled(GLC11.GL_ALPHA_TEST);
            depthTest = gl11.glIsEnabled(GLC11.GL_DEPTH_TEST);

            blendSrc = gl11.glGetInteger(GLC11.GL_BLEND_SRC);
            blendDst = gl11.glGetInteger(GLC11.GL_BLEND_DST);

            shadeModel = gl11.glGetInteger(GLC11.GL_SHADE_MODEL);

            arrayBufferBinding = gl11.glGetInteger(GLC15.GL_ARRAY_BUFFER_BINDING);
            elementArrayBufferBinding = gl11.glGetInteger(GLC15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        }

        public void restore(IGL15 gl15) {
            set(gl15, GLC11.GL_TEXTURE_2D, texture2D);
            set(gl15, GLC11.GL_BLEND, blend);
            set(gl15, GLC11.GL_ALPHA_TEST, alphaTest);
            set(gl15, GLC11.GL_DEPTH_TEST, depthTest);

            gl15.glBlendFunc(blendSrc, blendDst);
            gl15.glShadeModel(shadeModel);

            gl15.glBindBuffer(GLC15.GL_ARRAY_BUFFER, arrayBufferBinding);
            gl15.glBindBuffer(GLC15.GL_ELEMENT_ARRAY_BUFFER, elementArrayBufferBinding);
        }

        private static void set(IGL11 gl11, int cap, boolean enable) {
            if (enable) gl11.glEnable(cap);
            else gl11.glDisable(cap);
        }
    }
}
