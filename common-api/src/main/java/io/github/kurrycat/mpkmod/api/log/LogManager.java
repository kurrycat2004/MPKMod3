package io.github.kurrycat.mpkmod.api.log;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface LogManager {
    ServiceHandle<LogManager> HANDLE = Services.getHandle(LogManager.class);

    ILogger createLogger(String name);
}
