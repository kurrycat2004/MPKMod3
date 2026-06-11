package io.github.kurrycat.mpkmod.log;

import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.util.Flags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class StdoutLogger implements ILogger {
    public static final StdoutLogger FALLBACK = new StdoutLogger(App.id());

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final String name;
    private final Level logLevel;

    public StdoutLogger(String name) {
        if (!name.matches("[a-zA-Z0-9/]+")) {
            throw new IllegalArgumentException("Logger name has to match \"[a-zA-Z0-9/]+\", got: " + name);
        }
        this.name = name;
        this.logLevel = currentLogLevel();
    }

    @Override
    public ILogger createSubLogger(String name) {
        return new StdoutLogger(this.name + "/" + name);
    }

    @Override
    public void log(Level level, String formatString) {
        if (shouldNotLog(level)) return;

        print(level, formatString, null);
    }

    @Override
    public void log(Level level, String formatString, Object var1) {
        if (shouldNotLog(level)) return;

        if (var1 instanceof Throwable t) {
            print(level, formatString, t);
        } else {
            print(level, formatStringWithArgs(formatString, new Object[] { var1 }, 1), null);
        }
    }

    @Override
    public void log(Level level, String formatString, Object var1, Object var2) {
        if (shouldNotLog(level)) return;

        if (var2 instanceof Throwable t) {
            print(level, formatStringWithArgs(formatString, new Object[] { var1 }, 1), t);
        } else {
            print(level, formatStringWithArgs(formatString, new Object[] { var1, var2 }, 2), null);
        }
    }

    @Override
    public void log(Level level, String formatString, Object... vars) {
        if (shouldNotLog(level)) return;

        int argCount = vars == null ? 0 : vars.length;
        Throwable throwable = null;
        int usableArgs = argCount;

        if (argCount > 0 && vars[argCount - 1] instanceof Throwable t) {
            throwable = t;
            usableArgs--;
        }

        print(level, formatStringWithArgs(formatString, vars, usableArgs), throwable);
    }

    private boolean shouldNotLog(Level level) {
        return logLevel.ordinal() > level.ordinal();
    }

    private Level currentLogLevel() {
        int loggerLen = name.length();
        Level logLevel;
        do {
            String loggerName = name.substring(0, loggerLen);
            logLevel = parseLogLevel(loggerName);
            loggerLen = loggerName.lastIndexOf('/');
        } while (logLevel == null && loggerLen > 0);

        return logLevel == null ? Level.INFO : logLevel;
    }

    private Level parseLogLevel(String loggerName) {
        String logLevelFlag = Flags.getDynString(Flags.LOGGER_PREFIX, loggerName);
        if (logLevelFlag == null) return null;

        Level logLevel = null;
        try {
            logLevel = Level.valueOf(logLevelFlag);
        } catch (IllegalArgumentException e) {
            warn("Invalid log level \"{}\" specified for logger \"{}\": ", logLevelFlag, loggerName);
        }
        return logLevel;
    }

    private void print(Level level, String message, Throwable throwable) {
        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        String threadName = Thread.currentThread().getName();
        System.out.print("[" + timestamp + "] [" + threadName + "/" + level.name() + "] [" + name + "]: " + message + "\n");

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            System.out.print(sw);
        }
    }

    private String formatStringWithArgs(String formatString, Object[] args, int limit) {
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int lastIndex = 0;
        int nextPlaceholder;

        while ((nextPlaceholder = formatString.indexOf("{}", lastIndex)) != -1 && argIndex < limit) {
            sb.append(formatString, lastIndex, nextPlaceholder);
            sb.append(args[argIndex++]);
            lastIndex = nextPlaceholder + 2;
        }

        sb.append(formatString.substring(lastIndex));
        return sb.toString();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "StdoutLogger[" +
               "name=" + name + ']';
    }
}
