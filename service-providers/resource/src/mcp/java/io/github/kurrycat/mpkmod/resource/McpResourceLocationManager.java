package io.github.kurrycat.mpkmod.resource;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.resource.ResourceManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.service.util.ClasspathServiceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class McpResourceLocationManager implements ResourceManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends ClasspathServiceProvider<ResourceManager> {
        public Provider() {
            super(McpResourceLocationManager::new, ResourceManager.class);
        }

        private final RequiredMethod GET_MINECRAFT = new RequiredMethod(
                "net.minecraft.client.Minecraft",
                "getMinecraft",
                "net.minecraft.client.Minecraft"
        );
        private final RequiredMethod GET_RESOURCEMANAGER = new RequiredMethod(
                "net.minecraft.client.Minecraft",
                "getResourceManager",
                "net.minecraft.client.resources.IResourceManager"
        );
        private final RequiredMethod GET_RESOURCE = new RequiredMethod(
                "net.minecraft.client.resources.IResourceManager",
                "getResource",
                "net.minecraft.client.resources.IResource",
                "net.minecraft.util.ResourceLocation"
        );
        private final RequiredMethod GET_INPUT_STREAM = new RequiredMethod(
                "net.minecraft.client.resources.IResource",
                "getInputStream",
                InputStream.class.getName()
        );

        @Override
        protected List<RequiredMethod> requiredMethods() {
            return List.of(
                    GET_MINECRAFT,
                    GET_RESOURCEMANAGER,
                    GET_RESOURCE,
                    GET_INPUT_STREAM
            );
        }
    }

    @Override
    public IResource resource(String domain, String path) {
        ResourceLocation loc = new ResourceLocation(domain, path);
        return new Resource(domain, path, loc);
    }

    @Override
    public InputStream inputStream(IResource resource) throws IOException {
        return Minecraft.getMinecraft()
                .getResourceManager()
                .getResource((ResourceLocation) resource.backendResource())
                .getInputStream();
    }

    private record Resource(
            String domain,
            String path,
            ResourceLocation backendResource
    ) implements IResource {}
}
