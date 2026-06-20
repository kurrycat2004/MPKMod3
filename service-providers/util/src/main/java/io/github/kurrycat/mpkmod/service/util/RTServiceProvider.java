package io.github.kurrycat.mpkmod.service.util;

import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

import java.util.Optional;
import java.util.function.Function;

public abstract class RTServiceProvider<API extends RTServiceApi, T> implements TypedServiceProvider<T> {
    private final RTServiceApiProvider<API> apiProvider;
    private final Function<API, T> provider;
    private final Class<T> type;

    private API api;
    private ApiCreationException apiCreationException;

    protected RTServiceProvider(
            RTServiceApiProvider<API> apiProvider,
            Function<API, T> provider,
            Class<T> type
    ) {
        this.apiProvider = apiProvider;
        this.provider = provider;
        this.type = type;
    }

    private API getApi() {
        if (api != null || apiCreationException != null) return api;
        try {
            api = apiProvider.provide();
        } catch (ApiCreationException e) {
            apiCreationException = e;
        }
        return api;
    }

    @Override
    public T provide() {
        return provider.apply(getApi());
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public final Optional<String> invalidReason() {
        if (getApi() != null) return Optional.empty();
        return Optional.of(apiCreationException.getMessage());
    }
}
