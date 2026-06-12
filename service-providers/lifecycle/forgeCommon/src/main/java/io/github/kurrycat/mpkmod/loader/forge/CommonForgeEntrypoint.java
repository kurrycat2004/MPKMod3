package io.github.kurrycat.mpkmod.loader.forge;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonForgeEntrypoint {
    public static final CommonForgeEntrypoint INSTANCE = new CommonForgeEntrypoint();

    private final File source = findSource();
    private final List<Path> rootPaths = findRootPaths(source.toPath());

    private CommonForgeEntrypoint() {}

    public File source() {
        return source;
    }

    public List<Path> rootPaths() {
        return rootPaths;
    }

    private static File findSource() {
        String classFile = CommonForgeEntrypoint.class.getName().replace('.', '/') + ".class";
        URL url = CommonForgeEntrypoint.class.getClassLoader().getResource(classFile);
        if (url == null) {
            throw new IllegalStateException(
                    "Error finding code source location of " + CommonForgeEntrypoint.class.getName()
            );
        }

        try {
            switch (url.getProtocol()) {
                case "jar" -> {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    URL jarFileUrl = connection.getJarFileURL();
                    return new File(jarFileUrl.toURI()).getAbsoluteFile();
                }
                case "union" -> {
                    URI uri = url.toURI();
                    String part = uri.getRawSchemeSpecificPart();
                    // UnionFileSystem uses <basePath + # + index> as key (first part),
                    // which gets URL escaped to %23
                    int split = part.indexOf("%23");
                    return new File(part.substring(0, split)).getAbsoluteFile();
                }
                default -> throw new IllegalStateException(
                        "Unknown protocol for url: " + url.getPath() +
                        " while trying to get code source of " + CommonForgeEntrypoint.class.getName()
                );
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(
                    "Error finding code source location of " + CommonForgeEntrypoint.class.getName(), e
            );
        }
    }

    private static List<Path> findRootPaths(Path path) {
        if (Files.isDirectory(path)) {
            return List.of(path);
        }

        URI jarURI = URI.create("jar:" + path.toUri());
        FileSystem fileSystem;
        try {
            fileSystem = FileSystems.getFileSystem(jarURI);
        } catch (FileSystemNotFoundException ignored) {
            try {
                //noinspection resource
                fileSystem = FileSystems.newFileSystem(jarURI, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException ignored2) {
                fileSystem = FileSystems.getFileSystem(jarURI);
            } catch (IOException e) {
                throw new IllegalStateException("Error accessing " + jarURI + ": " + e, e);
            }
        }
        List<Path> rootPaths = new ArrayList<>();
        fileSystem.getRootDirectories().forEach(rootPaths::add);
        return List.copyOf(rootPaths);
    }
}
