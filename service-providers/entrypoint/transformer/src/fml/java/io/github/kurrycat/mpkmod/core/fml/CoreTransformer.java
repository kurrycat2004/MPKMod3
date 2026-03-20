package io.github.kurrycat.mpkmod.core.fml;

import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/// Used in {@link CoreLoadingPlugin#getASMTransformerClass()}
public final class CoreTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        final TransformerManager transformerManager = TransformerManager.HANDLE.get();

        if (!transformerManager.shouldTransform(transformedName)) return basicClass;

        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        boolean changed = transformerManager.transform(classNode);
        if (!changed) return basicClass;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}