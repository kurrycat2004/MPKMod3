package io.github.kurrycat.mpkmod.api.transformer;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import org.objectweb.asm.tree.ClassNode;

public interface Transformer {
    ILogger LOGGER = ILogger.createLogger(Transformer.class.getSimpleName());

    boolean shouldTransform(String className);

    boolean transform(ClassNode input);
}