package io.github.kurrycat.mpkmod.transformer.modlauncher;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.transformer.TransformerContext;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

@AutoService(ILaunchPluginService.class)
public final class CoreLaunchPlugin implements ILaunchPluginService {
    private static final EnumSet<Phase> AFTER_TRANSFORM = EnumSet.of(Phase.AFTER);
    private static final EnumSet<Phase> NEVER = EnumSet.noneOf(Phase.class);
    private static final boolean hasInitialized = TransformerManager.HANDLE.get()
            .tryInitialize(ILaunchPluginService.class);

    @Override
    public @NotNull String name() {
        return App.id() + "_core";
    }

    private record LaunchPluginTransformerContext(
            String binaryClassName,
            String internalClassName
    ) implements TransformerContext {}

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (!hasInitialized) return NEVER;
        TransformerContext transformerContext = new LaunchPluginTransformerContext(
                classType.getClassName(),
                classType.getInternalName()
        );
        return TransformerManager.HANDLE.get().shouldTransform(transformerContext)
               ? AFTER_TRANSFORM : NEVER;
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        if (!hasInitialized) return false;
        TransformerContext transformerContext = new LaunchPluginTransformerContext(
                classType.getClassName(),
                classType.getInternalName()
        );

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] classBytes = writer.toByteArray();

        byte[] newClassBytes = TransformerManager.HANDLE.get().transform(transformerContext, classBytes);

        if (newClassBytes == null || newClassBytes == classBytes) {
            return false;
        }

        ClassReader reader = new ClassReader(classBytes);

        ClassNode replacement = new ClassNode();
        reader.accept(replacement, 0);

        classNode.version = replacement.version;
        classNode.access = replacement.access;
        classNode.name = replacement.name;
        classNode.signature = replacement.signature;
        classNode.superName = replacement.superName;
        classNode.interfaces = replacement.interfaces;
        classNode.sourceFile = replacement.sourceFile;
        classNode.sourceDebug = replacement.sourceDebug;
        classNode.module = replacement.module;
        classNode.outerClass = replacement.outerClass;
        classNode.outerMethod = replacement.outerMethod;
        classNode.outerMethodDesc = replacement.outerMethodDesc;
        classNode.visibleAnnotations = replacement.visibleAnnotations;
        classNode.invisibleAnnotations = replacement.invisibleAnnotations;
        classNode.visibleTypeAnnotations = replacement.visibleTypeAnnotations;
        classNode.invisibleTypeAnnotations = replacement.invisibleTypeAnnotations;
        classNode.attrs = replacement.attrs;
        classNode.innerClasses = replacement.innerClasses;
        classNode.fields = replacement.fields;
        classNode.methods = replacement.methods;

        return true;
    }
}
