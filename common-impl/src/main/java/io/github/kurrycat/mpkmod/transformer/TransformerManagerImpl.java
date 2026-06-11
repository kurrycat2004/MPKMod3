package io.github.kurrycat.mpkmod.transformer;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.api.transformer.Transformer;
import io.github.kurrycat.mpkmod.api.transformer.TransformerContext;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import io.github.kurrycat.mpkmod.shadedlibs.asm.ClassReader;
import io.github.kurrycat.mpkmod.shadedlibs.asm.ClassWriter;
import io.github.kurrycat.mpkmod.shadedlibs.asm.tree.ClassNode;

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
    public boolean shouldTransform(TransformerContext context) {
        if (context.internalClassName().startsWith(EXCLUDE_PREFIX)) return false;

        for (Transformer transformer : TRANSFORMERS) {
            if (transformer.shouldTransform(context.internalClassName())) return true;
        }
        return false;
    }

    @Override
    public byte[] transform(TransformerContext context, byte[] classBytes) {
        boolean shouldTransform = false;
        for (Transformer transformer : TRANSFORMERS) {
            if (transformer.shouldTransform(context.internalClassName())) {
                shouldTransform = true;
                break;
            }
        }
        if (!shouldTransform) return classBytes;

        ClassNode node = createClassNode(context, classBytes);
        if (node == null) return classBytes;

        boolean didTransform = false;
        for (Transformer transformer : TRANSFORMERS) {
            didTransform |= transformer.transform(node);
        }
        if (!didTransform) return classBytes;

        // FIXME: COMPUTE_FRAMES might load superclasses
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private ClassNode createClassNode(TransformerContext context, byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "Failed to parse class: {} with class file version {}",
                    context.binaryClassName(), readMajorVersion(classBytes)
            );
            return null;
        }
    }

    private int readMajorVersion(byte[] classBytes) {
        if (classBytes.length < 8) return -1;

        int magic = ((classBytes[0] & 0xFF) << 24) |
                    ((classBytes[1] & 0xFF) << 16) |
                    ((classBytes[2] & 0xFF) << 8) |
                    ((classBytes[3] & 0xFF));

        if (magic != 0xCAFEBABE) return -1;

        return ((classBytes[6] & 0xFF) << 8) |
               ((classBytes[7] & 0xFF));
    }

}
