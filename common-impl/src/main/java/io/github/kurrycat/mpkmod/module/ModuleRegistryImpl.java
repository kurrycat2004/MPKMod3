package io.github.kurrycat.mpkmod.module;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.IVersion;
import io.github.kurrycat.mpkmod.api.module.ModuleEntrypoint;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.util.FileUtil;
import io.github.kurrycat.mpkmod.util.MultiParentClassLoader;
import io.github.kurrycat.mpkmod.util.StringUtil;
import io.github.kurrycat.mpkmod.util.TarjanSCC;
import xyz.wagyourtail.jvmdg.ClassDowngrader;
import xyz.wagyourtail.jvmdg.classloader.DowngradingClassLoader;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModuleRegistryImpl implements ModuleRegistry {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ModuleRegistry> {
        public Provider() {
            super(ModuleRegistryImpl::new, ModuleRegistry.class);
        }
    }

    final static ILogger LOGGER = ILogger.createLogger(ModuleRegistry.class.getSimpleName());

    private final static ILogger MODULE_LOGGER = ILogger.createLogger("module");

    private final Set<String> disabledModuleIds = new HashSet<>();
    private final Map<Path, DiscoveredModule> erroredModules = new HashMap<>();
    private final Map<String, DiscoveredModule> disabledModules = new HashMap<>();
    private final Map<String, LoadedModule> loadedModules = new HashMap<>();
    private final Collection<ModuleEntrypoint> loadedEntrypoints = Collections.unmodifiableCollection(
            loadedModules.values()
    );

    private final Map<String, Set<String>> moduleToDependants = new HashMap<>();
    private final Map<String, Set<String>> moduleToDependencies = new HashMap<>();

    private static <T> T removeOrDefault(Map<String, T> map, String key, T defaultValue) {
        T value = map.remove(key);
        return value == null ? defaultValue : value;
    }

    private void unloadRecursive(String moduleId) {
        Set<String> dependants = removeOrDefault(moduleToDependants, moduleId, Set.of());
        for (String dependantId : dependants) {
            unloadRecursive(dependantId);
        }

        Set<String> dependencies = removeOrDefault(moduleToDependencies, moduleId, Set.of());
        for (String dep : dependencies) {
            moduleToDependants.get(dep).remove(moduleId);
        }

        LoadedModule module = loadedModules.remove(moduleId);
        module.moduleInstance().onUnload();
        if (module.classLoader() instanceof Closeable cl) {
            try {
                cl.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close classloader for module {}", moduleId, e);
            }
        }
    }

    public void loadModules() {
        erroredModules.clear();

        List<DiscoveredModule> discoveredModules = new ArrayList<>();
        ModuleCache.extractInternalModules();
        ModuleDiscoverer.discoverModulesFromDir(
                ModuleCache.getInternalModuleDir(),
                discoveredModules
        );
        ModuleDiscoverer.discoverModulesFromDir(
                ModuleCache.getModulesDir(),
                discoveredModules
        );

        // filter errored + disabled modules
        Map<String, DiscoveredModule> toLoad = new HashMap<>();
        for (DiscoveredModule module : discoveredModules) {
            if (module.isError()) {
                erroredModules.put(module.source(), module);
            } else if (!disabledModuleIds.contains(module.entry().id())) {
                toLoad.put(module.entry().id(), module);
            } else {
                disabledModules.put(module.entry().id(), module);
            }
        }

        // load trivially loadable modules
        boolean modifiedToLoad = true;
        while (modifiedToLoad) {
            modifiedToLoad = false;
            Iterator<Map.Entry<String, DiscoveredModule>> iterator = toLoad.entrySet().iterator();
            currentPass:
            while (iterator.hasNext()) {
                Map.Entry<String, DiscoveredModule> entry = iterator.next();
                DiscoveredModule module = entry.getValue();

                for (Map.Entry<String, IVersion.Constraint> depEntry : module.entry().dependencies().entrySet()) {
                    String depId = depEntry.getKey();
                    IVersion.Constraint depVersion = depEntry.getValue();
                    LoadedModule loaded = loadedModules.get(depId);

                    if (loaded == null) {
                        continue currentPass;
                    }

                    if (!loaded.entry().version().satisfies(depVersion)) {
                        modifiedToLoad = true;
                        iterator.remove();
                        erroredModules.put(module.source(), module.withError(
                                new ModuleLoadException.Builder("Unsatisfied version constraint")
                                        .addError("Requires the " + depId + " version to match " + depVersion +
                                                  ", but found version " + loaded.entry().version())
                                        .build()
                        ));
                        continue currentPass;
                    }
                }

                modifiedToLoad = true;
                iterator.remove();
                try {
                    LoadedModule loadedModule = loadModule(loadedModules, module);
                    loadedModules.put(loadedModule.entry().id(), loadedModule);
                } catch (ModuleLoadException e) {
                    erroredModules.put(module.source(), module.withError(e));
                } catch (Exception e) {
                    ModuleLoadException exception = new ModuleLoadException.Builder("Unexpected error while trying to load module: " + module.entry().id())
                            .addError(e)
                            .build();
                    erroredModules.put(module.source(), module.withError(exception));
                }
            }
        }

        // handle missing dependencies
        Iterator<DiscoveredModule> it = toLoad.values().iterator();
        while (it.hasNext()) {
            DiscoveredModule mod = it.next();
            List<String> missing = mod.entry().dependencies().keySet().stream()
                    .filter(d -> !loadedModules.containsKey(d) && !toLoad.containsKey(d))
                    .toList();
            if (!missing.isEmpty()) {
                it.remove();
                erroredModules.put(mod.source(), mod.withError(
                        new ModuleLoadException.Builder("Missing dependency")
                                .addError("Module " + mod.entry().id() +
                                          " requires missing module(s): " + String.join(", ", missing))
                                .build()
                ));
            }
        }

        // build graph of remaining modules
        Set<String> nodeIds = toLoad.keySet();
        Map<String, List<String>> graph = new HashMap<>();
        for (var e : toLoad.entrySet()) {
            graph.put(e.getKey(),
                    e.getValue().entry().dependencies().keySet().stream()
                            .filter(nodeIds::contains)
                            .toList());
        }

        // handle cycles
        TarjanSCC<String> scc = new TarjanSCC<>(graph);
        for (Set<String> comp : scc.getSCCs()) {
            String start = comp.iterator().next();
            boolean isCycle = comp.size() > 1 || (comp.size() == 1 && graph.get(start).contains(start));
            if (!isCycle) continue;

            for (String modId : comp) {
                DiscoveredModule mod = toLoad.remove(modId);
                erroredModules.put(mod.source(), mod.withError(
                        new ModuleLoadException.Builder("Circular dependency")
                                .addError("Cycle: " + StringUtil.joinCycle(comp, start, " -> "))
                                .build()
                ));
            }
        }
    }

    private static LoadedModule loadModule(Map<String, LoadedModule> loadedModules, DiscoveredModule module) throws ModuleLoadException, IOException {
        CachedModule cachedModule = ModuleCache.getOrCreateCachedModule(module);
        FileUtil.tryCloseJar(module.source());

        List<ClassLoader> dependencyClassLoaders = new ArrayList<>();
        for (String depId : cachedModule.entry().dependencies().keySet()) {
            LoadedModule depModule = loadedModules.get(depId);
            dependencyClassLoaders.add(depModule.classLoader());
        }

        ClassLoader parent = buildClassLoaderHierarchy(dependencyClassLoaders);
        DowngradingClassLoader loader = new DowngradingClassLoader(ClassDowngrader.getCurrentVersionDowngrader(), parent);
        loader.addDelegate(new URL[] { cachedModule.source().toUri().toURL() });

        Class<?> entrypointClass;
        try {
            entrypointClass = loader.loadClass(cachedModule.entry().entrypoint());
        } catch (ClassNotFoundException e) {
            throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                    .addError(e)
                    .build();
        }

        if (!ModuleEntrypoint.class.isAssignableFrom(entrypointClass)) {
            throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                    .addError("Entrypoint class: " + entrypointClass.getName() + " does not implement " + ModuleEntrypoint.class.getName())
                    .build();
        }

        @SuppressWarnings("unchecked")
        Class<? extends ModuleEntrypoint> moduleClass = (Class<? extends ModuleEntrypoint>) entrypointClass;

        ModuleEntrypoint moduleInstance;
        try {
            moduleInstance = moduleClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                    .addError("Entrypoint class: " + entrypointClass.getName() + " does not have a no-arg constructor")
                    .build();
        } catch (Exception e) {
            throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                    .addError("Entrypoint class: " + entrypointClass.getName() + " threw an exception during initialization", e)
                    .build();
        }

        ILogger logger = MODULE_LOGGER.createSubLogger(cachedModule.entry().id());
        try {
            moduleInstance.onLoad(cachedModule.entry(), logger);
        } catch (Exception e) {
            throw new ModuleLoadException.Builder("Invalid entrypoint class for module " + cachedModule.entry().id())
                    .addError("onLoad() threw an error", e)
                    .build();
        }

        return new LoadedModule(
                cachedModule.source(),
                cachedModule.sourceHash(),
                cachedModule.entry(),
                loader,
                moduleInstance
        );
    }

    private static ClassLoader buildClassLoaderHierarchy(List<ClassLoader> parents) {
        if (parents.isEmpty()) return ModuleRegistryImpl.class.getClassLoader();
        else if (parents.size() == 1) return parents.getFirst();
        else return new MultiParentClassLoader(parents);
    }

    @Override
    public void loadAllModules() {
        loadModules();
        LOGGER.info("Loaded modules: {}", loadedModules.keySet());
        LOGGER.info("Disabled modules: {}", disabledModules.keySet());
        LOGGER.info("Errored modules:");
        for (var e : erroredModules.values()) {
            LOGGER.info("", e.error());
        }
    }

    @Override
    public boolean isModuleLoaded(String moduleId) {
        return loadedModules.containsKey(moduleId);
    }

    @Override
    public Collection<ModuleEntrypoint> loadedEntrypoints() {
        return loadedEntrypoints;
    }
}
