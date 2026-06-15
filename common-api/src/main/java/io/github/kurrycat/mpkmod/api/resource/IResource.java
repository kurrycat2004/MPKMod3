package io.github.kurrycat.mpkmod.api.resource;

public interface IResource {
    String domain();

    String path();

    Object backendResource();
}
