package io.github.kurrycat.mpkmod.api.transformer;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;
import org.objectweb.asm.tree.ClassNode;

public interface TransformerManager {
    ServiceHandle<TransformerManager> HANDLE = Services.getHandle(TransformerManager.class);

    boolean isInitialized();

    /**
     * Because there might be multiple transformer pipelines present at runtime,
     * only the first is run.
     * A {@link TransformerManager} should only run its pipeline when this method returns {@code true}
     *
     * @param transformerPipelineType the transformer pipeline that is used
     * @return whether to run the transformer pipeline
     */
    boolean tryInitialize(Class<?> transformerPipelineType);

    /**
     * @param className the name of the class to be checked
     * @return whether there is a {@link Transformer} which wants to handle the given class
     */
    boolean shouldTransform(String className);

    /**
     * @param input the {@link ClassNode} to be transformed in-place
     * @return whether the input node changed
     */
    boolean transform(ClassNode input);

    default boolean tryTransform(String className, ClassNode input) {
        if (!shouldTransform(className)) return false;
        return transform(input);
    }
}
