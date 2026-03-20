package io.github.kurrycat.mpkmod.api.log;

import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ILogger {
    enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    record FixedLevel(ILogger parentLogger, Level level) {
        public void log(String formatString) {
            parentLogger.log(level, formatString);
        }

        public void log(String formatString, Object var1) {
            parentLogger.log(level, formatString, var1);
        }

        public void log(String formatString, Object var1, Object var2) {
            parentLogger.log(level, formatString, var1, var2);
        }

        public void log(String formatString, Object... vars) {
            parentLogger.log(level, formatString, vars);
        }
    }

    class LogManagerHolder {
        private static final ServiceHandle<LogManager> HANDLE = Services.getHandle(LogManager.class);
    }

    static ILogger createLogger(String name) {
        return LogManagerHolder.HANDLE.get().createLogger(App.id() + "/" + name);
    }

    default ILogger createSubLogger(String name) {
        return LogManagerHolder.HANDLE.get().createLogger(this.name() + "/" + name);
    }

    default FixedLevel createFixed(Level level) {
        return new FixedLevel(this, level);
    }

    String name();

    void log(Level level, String formatString);

    void log(Level level, String formatString, Object var1);

    void log(Level level, String formatString, Object var1, Object var2);

    void log(Level level, String formatString, Object... vars);

    default void trace(String formatString) {
        log(Level.TRACE, formatString);
    }

    default void trace(String formatString, Object var1) {
        log(Level.TRACE, formatString, var1);
    }

    default void trace(String formatString, Object var1, Object var2) {
        log(Level.TRACE, formatString, var1, var2);
    }

    default void trace(String formatString, Object... vars) {
        log(Level.TRACE, formatString, vars);
    }

    default void trace(String formatString, Throwable throwable) {
        log(Level.TRACE, formatString, throwable);
    }

    default void debug(String formatString) {
        log(Level.DEBUG, formatString);
    }

    default void debug(String formatString, Object var1) {
        log(Level.DEBUG, formatString, var1);
    }

    default void debug(String formatString, Object var1, Object var2) {
        log(Level.DEBUG, formatString, var1, var2);
    }

    default void debug(String formatString, Object... vars) {
        log(Level.DEBUG, formatString, vars);
    }

    default void debug(String formatString, Throwable throwable) {
        log(Level.DEBUG, formatString, throwable);
    }

    default void info(String formatString) {
        log(Level.INFO, formatString);
    }

    default void info(String formatString, Object var1) {
        log(Level.INFO, formatString, var1);
    }

    default void info(String formatString, Object var1, Object var2) {
        log(Level.INFO, formatString, var1, var2);
    }

    default void info(String formatString, Object... vars) {
        log(Level.INFO, formatString, vars);
    }

    default void info(String formatString, Throwable throwable) {
        log(Level.INFO, formatString, throwable);
    }

    default void warn(String formatString) {
        log(Level.WARN, formatString);
    }

    default void warn(String formatString, Object var1) {
        log(Level.WARN, formatString, var1);
    }

    default void warn(String formatString, Object var1, Object var2) {
        log(Level.WARN, formatString, var1, var2);
    }

    default void warn(String formatString, Object... vars) {
        log(Level.WARN, formatString, vars);
    }

    default void warn(String formatString, Throwable throwable) {
        log(Level.WARN, formatString, throwable);
    }

    default void error(String formatString) {
        log(Level.ERROR, formatString);
    }

    default void error(String formatString, Object var1) {
        log(Level.ERROR, formatString, var1);
    }

    default void error(String formatString, Object var1, Object var2) {
        log(Level.ERROR, formatString, var1, var2);
    }

    default void error(String formatString, Object... vars) {
        log(Level.ERROR, formatString, vars);
    }

    default void error(String formatString, Throwable throwable) {
        log(Level.ERROR, formatString, throwable);
    }
}
