package io.github.kurrycat.mpkmod.resource;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.resource.ResourceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.service.util.ApiCreationException;
import io.github.kurrycat.mpkmod.service.util.RTServiceApi;
import io.github.kurrycat.mpkmod.service.util.RTServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LegacyResourceLocationManager implements ResourceManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends RTServiceProvider<API, ResourceManager> {
        public Provider() {
            super(API::new, LegacyResourceLocationManager::new, ResourceManager.class);
        }
    }

    protected static final class API extends RTServiceApi {
        public API() throws ApiCreationException {}

        private final Class<?> MINECRAFT = clazz("net.minecraft.client.Minecraft");
        private final Class<?> IRESOURCE_MANAGER = clazz("net.minecraft.client.resources.IResourceManager");
        private final Class<?> IRESOURCE = clazz("net.minecraft.client.resources.IResource");
        private final Class<?> RESOURCE_LOCATION = clazz("net.minecraft.util.ResourceLocation");

        private final MethodHandle newResourceLocation = constructor(
                RESOURCE_LOCATION, MethodType.methodType(void.class, String.class, String.class)
        );
        private final MethodHandle getMinecraft = instanceGetter(
                MINECRAFT, "getMinecraft", "func_71410_x"
        );
        private final MethodHandle getResourceManager = method(
                MINECRAFT, MethodType.methodType(IRESOURCE_MANAGER),
                "getResourceManager", "func_110442_L"
        );
        private final MethodHandle getResource = method(
                IRESOURCE_MANAGER, MethodType.methodType(IRESOURCE, RESOURCE_LOCATION),
                "getResource", "func_110536_a"
        );
        private final MethodHandle getInputStream = method(
                IRESOURCE, MethodType.methodType(InputStream.class),
                "getInputStream", "func_110527_b"
        );

        private final MethodHandle currentResourceManager = feed(getResourceManager, getMinecraft);
        private final MethodHandle currentResource = feed(getResource, currentResourceManager);
        private final MethodHandle currentResourceInputStream = feed(getInputStream, currentResource);

        protected Object resourceLocation(String domain, String path) {
            try {
                return newResourceLocation.invoke(domain, path);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        protected InputStream inputStream(Object backendResource) throws IOException {
            try {
                return (InputStream) currentResourceInputStream.invoke(backendResource);
            } catch (IOException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final API api;

    protected LegacyResourceLocationManager(API api) {
        this.api = api;
    }

    @Override
    public IResource resource(String domain, String path) {
        Object loc = api.resourceLocation(domain, path);
        return new Resource(domain, path, loc);
    }

    @Override
    public InputStream inputStream(IResource resource) throws IOException {
        return api.inputStream(resource.backendResource());
    }

    private record Resource(
            String domain,
            String path,
            Object backendResource
    ) implements IResource {}
}
