package io.github.kurrycat.mpkmod.lwjgl.lwjgl3;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.lwjgl.api.IGL30;
import io.github.kurrycat.mpkmod.lwjgl.api.IGLCapabilities;
import io.github.kurrycat.mpkmod.lwjgl.api.LwjglBackend;
import io.github.kurrycat.mpkmod.service.util.ServiceUtil;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Optional;

public final class Lwjgl3Backend extends GL30Impl implements LwjglBackend, IGLCapabilities {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<LwjglBackend> {
        public Provider() {
            super(Lwjgl3Backend::new, LwjglBackend.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!ServiceUtil.doesClassExist("org.lwjgl.opengl.GLCapabilities")) {
                return Optional.of("No LWJGL3 found");
            }
            return Optional.empty();
        }
    }

    private final GLCapabilities caps;

    public Lwjgl3Backend() {
        this.caps = GL.getCapabilities();
    }

    @Override
    public IGLCapabilities capabilities() {return this;}

    @Override
    public IGL30 gl11() {return this;}

    @Override
    public IGL30 gl15() {return this;}

    @Override
    public IGL30 gl20() {return this;}

    @Override
    public IGL30 gl30() {return this;}

    @Override
    public boolean OpenGL11() {return caps.OpenGL11;}

    @Override
    public boolean OpenGL15() {return caps.OpenGL15;}

    @Override
    public boolean OpenGL33() {return caps.OpenGL33;}

    @Override
    public boolean GL_ARB_vertex_buffer_object() {return caps.GL_ARB_vertex_buffer_object;}
}
