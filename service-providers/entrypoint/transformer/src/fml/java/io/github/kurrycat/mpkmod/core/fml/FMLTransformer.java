package io.github.kurrycat.mpkmod.core.fml;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.transformer.Transformer;
import io.github.kurrycat.mpkmod.api.transformer.TransformerContext;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import net.minecraft.launchwrapper.IClassTransformer;

/// Used in {@link FMLLoadingPlugin#getASMTransformerClass()}
public final class FMLTransformer implements IClassTransformer {
    private static final ILogger LOGGER = Transformer.LOGGER.createSubLogger(FMLTransformer.class.getSimpleName());
    private static final boolean hasInitialized = TransformerManager.HANDLE.get()
            .tryInitialize(FMLTransformer.class);

    public static String[] tryGetTransformerClasses() {
        if (hasInitialized) {
            return new String[] { FMLTransformer.class.getName() };
        } else {
            LOGGER.warn(
                    "Transformer pipeline was already initialized. " +
                    "This initialization attempt will be ignored."
            );
            return new String[0];
        }
    }

    public FMLTransformer() {
        @SuppressWarnings("unused")
        FMLTransformerContext forceInit = new FMLTransformerContext("");
    }

    private record FMLTransformerContext(
            String binaryClassName,
            String internalClassName
    ) implements TransformerContext {
        public FMLTransformerContext(String binaryClassName) {
            this(binaryClassName, binaryClassName.replace('.', '/'));
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBytes) {
        if (classBytes == null) return null;
        final TransformerManager transformerManager = TransformerManager.HANDLE.get();

        FMLTransformerContext transformerContext = new FMLTransformerContext(transformedName);
        return transformerManager.transform(transformerContext, classBytes);
    }
}