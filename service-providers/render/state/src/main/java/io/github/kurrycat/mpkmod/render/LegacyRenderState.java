package io.github.kurrycat.mpkmod.render;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.RenderLayer;
import io.github.kurrycat.mpkmod.api.render.RenderState;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.service.util.ApiCreationException;
import io.github.kurrycat.mpkmod.service.util.RTServiceApi;
import io.github.kurrycat.mpkmod.service.util.RTServiceProvider;
import io.github.kurrycat.mpkmod.shadedlibs.joml.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LegacyRenderState implements RenderState {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends RTServiceProvider<API, RenderState> {
        public Provider() {
            super(API::new, LegacyRenderState::new, RenderState.class);
        }
    }

    protected static final class API extends RTServiceApi {
        public API() throws ApiCreationException {}

        private final Class<?> MINECRAFT = clazz("net.minecraft.client.Minecraft");
        private final Class<?> SCALED_RESOLUTION = clazz("net.minecraft.client.gui.ScaledResolution");

        private final MethodHandle getMinecraft = instanceGetter(
                MINECRAFT, "getMinecraft", "func_71410_x"
        );
        private final MethodHandle newScaledResolution = constructor(
                SCALED_RESOLUTION, MethodType.methodType(void.class, MINECRAFT)
        );
        private final MethodHandle getScaledWidthDouble = method(
                SCALED_RESOLUTION, MethodType.methodType(double.class),
                "getScaledWidth_double", "func_78327_c"
        );
        private final MethodHandle getScaledHeightDouble = method(
                SCALED_RESOLUTION, MethodType.methodType(double.class),
                "getScaledHeight_double", "func_78324_d"
        );

        private final MethodHandle createScaledResolution = feed(newScaledResolution, getMinecraft);
        private final MethodHandle currentWidth = feed(getScaledWidthDouble, createScaledResolution);
        private final MethodHandle currentHeight = feed(getScaledHeightDouble, createScaledResolution);

        protected double currentWidth() {
            try {
                return (double) currentWidth.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        protected double currentHeight() {
            try {
                return (double) currentHeight.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final API api;

    protected LegacyRenderState(API api) {
        this.api = api;
    }

    private final RenderLayer layer = RenderLayer.UI;

    @Override
    public RenderLayer layer() {
        return layer;
    }

    @Override
    public void projectionMatrix(Matrix4f out) {
        switch (layer) {
            case UI -> {
                double width = api.currentWidth();
                double height = api.currentHeight();
                out.setOrtho(
                        0.0f, (float) width,
                        (float) height, 0.0f,
                        -1000.0f, 1000.0f
                );
            }
            case WORLD -> throw new UnsupportedOperationException("World projection matrix not implemented yet");
        }
    }
}
