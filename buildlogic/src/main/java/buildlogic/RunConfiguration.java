package buildlogic;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import java.nio.file.Path;
import java.util.List;

public record RunConfiguration(
        List<Fabric> fabric
) {
    public record Fabric(
            String version,
            String mappings
    ) {}

    public static RunConfiguration read(Path path) {
        JToml toml = JToml.jToml();
        TomlTable table = toml.read(path);
        return toml.fromToml(RunConfiguration.class, table);
    }
}
