package io.github.kurrycat.mpkmod.service.util;

@FunctionalInterface
public interface RTServiceApiProvider<API extends RTServiceApi> {
    API provide() throws ApiCreationException;
}
