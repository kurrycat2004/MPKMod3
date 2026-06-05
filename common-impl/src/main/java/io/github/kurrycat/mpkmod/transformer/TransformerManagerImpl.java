package io.github.kurrycat.mpkmod.transformer;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.transformer.Transformer;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import org.objectweb.asm.tree.ClassNode;

import java.util.ServiceLoader;

public class TransformerManagerImpl implements TransformerManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<TransformerManager> {
        public Provider() {
            super(TransformerManagerImpl::new, TransformerManager.class);
        }
    }

    private static final ILogger LOGGER = ILogger.createLogger(TransformerManager.class.getSimpleName());
    private static final String MOD_GROUP = App.group().replace('.', '/');
    private static final String EXCLUDE_PREFIX = MOD_GROUP + "/";

    private final ServiceLoader<Transformer> TRANSFORMERS = ServiceLoader.load(Transformer.class);

    private volatile boolean isInitialized = false;

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public boolean tryInitialize(Class<?> transformerPipelineType) {
        if (isInitialized) return false;
        isInitialized = true;
        LOGGER.info("Initializing transformer pipeline using: {}", transformerPipelineType.getName());
        return true;
    }

    @Override
    public boolean shouldTransform(String className) {
        className = className.replace('.', '/');
        if (className.startsWith(EXCLUDE_PREFIX)) return false;

        for (Transformer transformer : TRANSFORMERS) {
            if (transformer.shouldTransform(className)) return true;
        }
        return false;
    }

    @Override
    public boolean transform(ClassNode input) {
        boolean didTransform = false;

        for (Transformer transformer : TRANSFORMERS) {
            if (!transformer.shouldTransform(input.name)) continue;
            didTransform |= transformer.transform(input);
        }

        return didTransform;
    }
}
