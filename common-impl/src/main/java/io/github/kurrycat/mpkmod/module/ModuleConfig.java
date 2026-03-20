package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.IVersion;
import io.github.kurrycat.mpkmod.api.module.IVersionConstraint;
import io.github.kurrycat.mpkmod.api.module.InvalidVersionConstraintException;
import io.github.kurrycat.mpkmod.util.FileUtil;
import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.TomlIOException;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * {@snippet lang = TOML:
 * format_version = 1
 * id = "examplemod"
 * version = "1.0.0"
 * entrypoint = "com.example.example_mod.ExampleModule"
 * name = "ExampleModule"
 * description = "Example Module"
 * authors = ["ExampleAuthor"]
 * # source = "https://github.com/example/example-mod"
 * license = "MIT"
 * # icon = "assets/modules/examplemod/icon.png"
 *
 * [dependencies]
 * # core = ">=1.0.0"
 *}
 */
public final class ModuleConfig {
    private static final String MODULES_DIR = "META-INF/mpkmodules";
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    private static final TomlKey FORMAT_VERSION = TomlKey.parse("format_version");
    private static final TomlKey ID = TomlKey.parse("id");
    private static final TomlKey VERSION = TomlKey.parse("version");
    private static final TomlKey ENTRYPOINT = TomlKey.parse("entrypoint");
    private static final TomlKey NAME = TomlKey.parse("name");
    private static final TomlKey DESCRIPTION = TomlKey.parse("description");
    private static final TomlKey AUTHORS = TomlKey.parse("authors");
    private static final TomlKey SOURCE = TomlKey.parse("source");
    private static final TomlKey LICENSE = TomlKey.parse("license");
    private static final TomlKey ICON = TomlKey.parse("icon");
    private static final TomlKey DEPENDENCIES = TomlKey.parse("dependencies");

    private ModuleConfig() {
    }

    public static List<ModuleEntry> load(Path root) throws ModuleLoadException {
        Path modulesDir = FileUtil.resolve(root, MODULES_DIR);
        if (!Files.isDirectory(modulesDir)) return List.of();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modulesDir)) {
            List<ModuleEntry> modules = new ArrayList<>();
            for (Path path : stream) {
                if (!Files.isRegularFile(path) || !path.toString().endsWith(".toml")) {
                    continue;
                }
                modules.add(loadFromFile(root, path));
            }
            return modules;
        } catch (IOException e) {
            throw new ModuleLoadException("Failed to read module directory", e);
        }
    }

    private static ModuleEntry loadFromFile(Path root, Path configFile) throws ModuleLoadException {
        JToml toml = JToml.jToml();
        TomlDocument doc;
        try {
            doc = toml.read(configFile);
        } catch (TomlIOException e) {
            throw new ModuleLoadException("Failed to open module .toml", e.getCause());
        } catch (TomlException e) {
            throw new ModuleLoadException("Failed to read module .toml", e);
        }

        ModuleLoadException.Builder errors = new ModuleLoadException.Builder(
                "Invalid module definition: " + configFile
        );

        Long formatVersion = getLong(doc, FORMAT_VERSION, true, errors);
        if (formatVersion == null || formatVersion != 1) {
            throw errors.build();
        }

        return parseModuleEntry(root, doc, errors);
    }

    // TODO: separate fatal errors from non-fatal errors
    private static ModuleEntry parseModuleEntry(Path root, TomlTable table, ModuleLoadException.Builder errors) throws ModuleLoadException {
        String id = getString(table, ID, true, errors);
        if (id != null && !VALID_ID_PATTERN.matcher(id).matches()) {
            errors.addError("Invalid module id: '" + id + "'. Must match [a-z0-9_]+.");
        }

        String versionString = getString(table, VERSION, true, errors);
        IVersion version = null;
        try {
            version = SemVer.parse(versionString);
        } catch (SemVer.InvalidVersionFormatException e) {
            errors.addError(e);
        }

        String entrypoint = getString(table, ENTRYPOINT, true, errors);
        String name = getString(table, NAME, true, errors);
        String description = getString(table, DESCRIPTION, false, errors);
        List<String> authors = getStringArray(table, AUTHORS, false, errors);
        String source = getString(table, SOURCE, false, errors);
        String license = getString(table, LICENSE, false, errors);

        String iconString = getString(table, ICON, false, errors);

        TomlTable dependencyTable = getTable(table, DEPENDENCIES, false, errors);
        Set<TomlKey> depKeys = dependencyTable == null ? Collections.emptySet() : dependencyTable.keys();
        Map<String, IVersionConstraint> dependencies = new HashMap<>();
        for (TomlKey depKey : depKeys) {
            String depId = depKey.toString();
            if (!VALID_ID_PATTERN.matcher(depId).matches()) {
                errors.addError("Invalid dependency module id: '" + depId + "'. Must match [a-z0-9_]+.");
                continue;
            }
            String depVersion = getString(dependencyTable, depKey, false, errors);
            if (depVersion == null) continue;

            try {
                dependencies.put(depId, SemVer.ConstraintSet.parse(depVersion));
            } catch (InvalidVersionConstraintException e) {
                errors.addError(e);
            }
        }

        if (errors.hasErrors()) {
            throw errors.build();
        }

        Path icon = FileUtil.resolve(root, Objects.requireNonNull(iconString));

        return new ModuleEntry(
                id, version, entrypoint, name, description, authors, source, license, icon, dependencies
        );
    }

    private static <T> T required(TomlKey key, ModuleLoadException.Builder errors) {
        errors.addError("Missing required field: '" + key + "'");
        return null;
    }

    private static <T> T expected(TomlKey key, String type, ModuleLoadException.Builder errors) {
        errors.addError("Expected " + type + " value for key: '" + key + "'");
        return null;
    }

    private static <T> T expectedArr(TomlKey key, int index, String type, ModuleLoadException.Builder errors) {
        errors.addError("Expected " + type + " value at index " + index + " for key: '" + key + "'");
        return null;
    }

    private static Long getLong(TomlTable table, TomlKey key, boolean required, ModuleLoadException.Builder errors) {
        TomlValue value = table.get(key);
        if (value == null) return required ? required(key, errors) : null;
        if (!value.isPrimitive() || !value.asPrimitive().isInteger()) return expected(key, "integer", errors);
        return value.asPrimitive().asLong();
    }

    private static String getString(TomlTable table, TomlKey key, boolean required, ModuleLoadException.Builder errors) {
        TomlValue value = table.get(key);
        if (value == null) return required ? required(key, errors) : null;
        if (!value.isPrimitive() || !value.asPrimitive().isString()) return expected(key, "string", errors);
        return value.asPrimitive().asString();
    }

    private static List<String> getStringArray(TomlTable table, TomlKey key, boolean required, ModuleLoadException.Builder errors) {
        TomlValue value = table.get(key);
        if (value == null) return required ? required(key, errors) : null;
        if (!value.isArray()) return expected(key, "string array", errors);

        TomlArray tomlArray = value.asArray();
        List<String> list = new ArrayList<>(tomlArray.size());
        boolean error = false;
        for (int i = 0; i < tomlArray.size(); i++) {
            TomlValue tomlValue = tomlArray.get(i);
            if (!tomlValue.isPrimitive() || !value.asPrimitive().isString()) {
                expectedArr(key, i, "string", errors);
                error = true;
            } else list.add(tomlValue.asPrimitive().asString());
        }

        return error ? null : list;
    }

    private static TomlTable getTable(TomlTable table, TomlKey key, boolean required, ModuleLoadException.Builder errors) {
        TomlValue value = table.get(key);
        if (value == null) return required ? required(key, errors) : null;
        if (!value.isTable()) return expected(key, "table", errors);
        return value.asTable();
    }
}