package net.minecraft.client.resources;

import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public interface IResourceManager {
    IResource getResource(ResourceLocation resourceLocation) throws IOException;
}
