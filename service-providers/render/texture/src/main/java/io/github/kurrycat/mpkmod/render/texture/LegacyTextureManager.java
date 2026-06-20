package io.github.kurrycat.mpkmod.render.texture;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.service.util.ApiCreationException;
import io.github.kurrycat.mpkmod.service.util.RTServiceApi;
import io.github.kurrycat.mpkmod.service.util.RTServiceProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LegacyTextureManager implements TextureManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends RTServiceProvider<API, TextureManager> {
        public Provider() {
            super(API::new, LegacyTextureManager::new, TextureManager.class);
        }
    }

    protected static final class API extends RTServiceApi {
        public API() throws ApiCreationException {}

        private final Class<?> MINECRAFT = clazz("net.minecraft.client.Minecraft");
        private final Class<?> TEXTURE_MANAGER = clazz("net.minecraft.client.renderer.texture.TextureManager");
        private final Class<?> RESOURCE_LOCATION = clazz("net.minecraft.util.ResourceLocation");

        private final MethodHandle getMinecraft = instanceGetter(
                MINECRAFT, "getMinecraft", "func_71410_x"
        );
        private final MethodHandle getTextureManager = method(
                MINECRAFT, MethodType.methodType(TEXTURE_MANAGER),
                "getTextureManager", "func_110434_K"
        );

        private final MethodHandle bindTexture = method(
                TEXTURE_MANAGER, MethodType.methodType(void.class, RESOURCE_LOCATION),
                "bindTexture", "func_110577_a"
        );

        private final MethodHandle currentTextureManager = feed(getTextureManager, getMinecraft);
        private final MethodHandle bindCurrentTexture = feed(bindTexture, currentTextureManager);

        protected void bindTexture(Object backendResource) {
            try {
                bindCurrentTexture.invoke(backendResource);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final API api;

    protected LegacyTextureManager(API api) {
        this.api = api;
    }

    @Override
    public void bindTexture(IResource texture) {
        api.bindTexture(texture.backendResource());
    }
}
