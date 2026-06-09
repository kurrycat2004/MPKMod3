package io.github.kurrycat.mpkmod.api.transformer;

import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

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
     * @param context context of the class being loaded
     * @return whether there is a {@link Transformer} which wants to handle the given class
     */
    boolean shouldTransform(TransformerContext context);

    /**
     * @param classBytes the {@link Class} bytes to be transformed
     * @return the transformed class bytes, or {@code input} if no transformation happened
     */
    byte[] transform(TransformerContext context, byte[] classBytes);
}
