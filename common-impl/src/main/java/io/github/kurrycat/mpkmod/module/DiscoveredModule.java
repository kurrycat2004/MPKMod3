package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;

public record DiscoveredModule(
        Path source,
        String sourceHash,
        String displayName,
        ModuleEntry entry,
        ModuleLoadException error
) {
    public static DiscoveredModule fromEntry(Path source, ModuleEntry entry) throws IOException {
        try {
            return new DiscoveredModule(source, FileUtil.computeHash(source),
                    entry.id(), entry, null);
        } catch (IOException e) {
            throw new IOException("Failed to compute hash for module " + entry.id(), e);
        }
    }

    public static DiscoveredModule fromError(Path source, ModuleLoadException e) {
        return new DiscoveredModule(source, null,
                FileUtil.getFileNameWithoutExtension(source), null, e);
    }

    public DiscoveredModule withError(ModuleLoadException error) {
        return new DiscoveredModule(source, sourceHash, displayName, entry, error);
    }

    public boolean isError() {
        return error != null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
