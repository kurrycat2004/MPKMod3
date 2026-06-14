package io.github.kurrycat.mpkmod.log;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.util.Flags;

public final class StdoutLogManager implements LogManager {
    private static final boolean FORCE_STDOUT_LOGGER = Flags.getBoolean(Flags.LOGGER_FORCE_STDOUT);

    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<LogManager> {
        public Provider() {
            super(StdoutLogManager::new, LogManager.class);
        }

        @Override
        public int priority() {
            return FORCE_STDOUT_LOGGER ? 99999 : -100;
        }
    }

    @Override
    public ILogger createLogger(String name) {
        return new StdoutLogger(name);
    }
}
