package io.github.kurrycat.mpkmod.api.transformer;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.shadedlibs.asm.tree.ClassNode;

public interface Transformer {
    ILogger LOGGER = ILogger.createLogger(Transformer.class.getSimpleName());

    boolean shouldTransform(String binaryClassName);

    boolean transform(ClassNode input);
}