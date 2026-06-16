package io.github.kurrycat.mpkmod.render;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.RenderLayer;
import io.github.kurrycat.mpkmod.api.render.RenderState;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.service.util.ClasspathServiceProvider;
import io.github.kurrycat.mpkmod.shadedlibs.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;

public class SeargeRenderState implements RenderState {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends ClasspathServiceProvider<RenderState> {
        public Provider() {
            super(SeargeRenderState::new, RenderState.class);
        }

        private final RequiredMethod GET_MINECRAFT = new RequiredMethod(
                "net.minecraft.client.Minecraft",
                "func_71410_x",
                "net.minecraft.client.Minecraft"
        );
        private final RequiredMethod SCALED_RESOLUTION_INIT = new RequiredMethod(
                "net.minecraft.client.gui.ScaledResolution",
                "<init>",
                "void",
                "net.minecraft.client.Minecraft"
        );
        private final RequiredMethod GET_SCALED_WIDTH_DOUBLE = new RequiredMethod(
                "net.minecraft.client.gui.ScaledResolution",
                "func_78327_c",
                "double"
        );
        private final RequiredMethod GET_SCALED_HEIGHT_DOUBLE = new RequiredMethod(
                "net.minecraft.client.gui.ScaledResolution",
                "func_78324_d",
                "double"
        );

        @Override
        protected List<RequiredMethod> requiredMethods() {
            return List.of(
                    GET_MINECRAFT,
                    SCALED_RESOLUTION_INIT,
                    GET_SCALED_WIDTH_DOUBLE,
                    GET_SCALED_HEIGHT_DOUBLE
            );
        }
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
                ScaledResolution sr = new ScaledResolution(Minecraft.func_71410_x());
                double width = sr.func_78327_c();
                double height = sr.func_78324_d();

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
