package buildlogic;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.nio.file.Path;
import java.util.List;

public record ModMetadata(
        String id,
        String name,
        String group,
        String version,
        String description,
        String license,
        String logoFile,
        List<String> authors,
        String homepage,
        String sources,
        String issues
) {
    public static ModMetadata read(Path path) {
        JToml toml = JToml.jToml();
        TomlTable table = toml.read(path);
        return toml.fromToml(ModMetadata.class, table);
    }
}
