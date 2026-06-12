package io.github.kurrycat.mpkmod.transformer.mixin;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.transformer.Transformer;
import io.github.kurrycat.mpkmod.api.transformer.TransformerContext;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public final class CoreMixinPlugin implements IMixinConfigPlugin {
    private static final ILogger LOGGER = Transformer.LOGGER.createSubLogger(IMixinConfigPlugin.class.getSimpleName());

    @Override
    public void onLoad(String mixinPackage) {
        final TransformerManager transformerManager = TransformerManager.HANDLE.get();
        if (transformerManager.isInitialized()) return;

        @SuppressWarnings("unused")
        MixinTransformerContext forceInit = new MixinTransformerContext("");

        try {
            ClassLoader classLoader = MixinEnvironment
                    .getCurrentEnvironment()
                    .getActiveTransformer()
                    .getClass().getClassLoader();

            List<?> coprocessors = getCoProcessorList(classLoader);

            Method tryTransformMethod = getClass().getMethod("tryTransform", String.class, ClassNode.class);
            MethodHandle tryTransformHandle = MethodHandles.publicLookup().unreflect(tryTransformMethod);

            Method shouldTransformMethod = getClass().getMethod("shouldTransform", String.class);
            MethodHandle shouldTransformHandle = MethodHandles.publicLookup().unreflect(shouldTransformMethod);

            Class<?> coreCoProcClass = generateCoProcClass(classLoader);

            Object coreCoProcessor = coreCoProcClass
                    .getConstructor(MethodHandle.class, MethodHandle.class)
                    .newInstance(tryTransformHandle, shouldTransformHandle);
            injectIntoCoprocessorList(coprocessors, coreCoProcClass, coreCoProcessor);

            transformerManager.tryInitialize(IMixinConfigPlugin.class);
        } catch (ReflectiveOperationException | ClassCastException e) {
            LOGGER.error("Failed to initialize core mixin plugin", e);
        }
    }

    private record MixinTransformerContext(
            String binaryClassName,
            String internalClassName
    ) implements TransformerContext {
        public MixinTransformerContext(String binaryClassName) {
            this(binaryClassName, binaryClassName.replace('.', '/'));
        }
    }

    public static boolean tryTransform(String className, ClassNode input) {
        final TransformerManager transformerManager = TransformerManager.HANDLE.get();
        TransformerContext transformerContext = new MixinTransformerContext(className);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        input.accept(writer);
        byte[] classBytes = writer.toByteArray();

        byte[] newClassBytes = transformerManager.transform(transformerContext, classBytes);

        if (newClassBytes == null || newClassBytes == classBytes) {
            return false;
        }

        ClassReader reader = new ClassReader(classBytes);

        ClassNode replacement = new ClassNode();
        reader.accept(replacement, 0);

        input.version = replacement.version;
        input.access = replacement.access;
        input.name = replacement.name;
        input.signature = replacement.signature;
        input.superName = replacement.superName;
        input.interfaces = replacement.interfaces;
        input.sourceFile = replacement.sourceFile;
        input.sourceDebug = replacement.sourceDebug;
        input.outerClass = replacement.outerClass;
        input.outerMethod = replacement.outerMethod;
        input.outerMethodDesc = replacement.outerMethodDesc;
        input.visibleAnnotations = replacement.visibleAnnotations;
        input.invisibleAnnotations = replacement.invisibleAnnotations;
        input.visibleTypeAnnotations = replacement.visibleTypeAnnotations;
        input.invisibleTypeAnnotations = replacement.invisibleTypeAnnotations;
        input.attrs = replacement.attrs;
        input.innerClasses = replacement.innerClasses;
        input.fields = replacement.fields;
        input.methods = replacement.methods;

        return true;
    }

    public static boolean shouldTransform(String className) {
        TransformerContext transformerContext = new MixinTransformerContext(className);
        return TransformerManager.HANDLE.get().shouldTransform(transformerContext);
    }

    @SuppressWarnings("unchecked")
    private <T> void injectIntoCoprocessorList(List<T> coprocessors, Class<?> coreCoProcClass, Object coreCoProcessor) throws ClassNotFoundException {
        for (Object coprocessor : coprocessors) {
            if (coreCoProcClass.isInstance(coprocessor)) {
                return;
            }
        }
        coprocessors.add((T) coreCoProcessor);
    }

    private static final String MIXIN_TRANSFORMER = "org.spongepowered.asm.mixin.transformer.MixinTransformer";
    private static final String MIXIN_PROCESSOR = "org.spongepowered.asm.mixin.transformer.MixinProcessor";
    private static final String MIXIN_COPROCESSOR = "org.spongepowered.asm.mixin.transformer.MixinCoprocessor";

    private static List<?> getCoProcessorList(ClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Class<?> mixinTransformerClass = Class.forName(MIXIN_TRANSFORMER, true, classLoader);

        Field procField = mixinTransformerClass.getDeclaredField("processor");
        procField.setAccessible(true);

        Class<?> mixinProcessorClass = Class.forName(MIXIN_PROCESSOR, true, classLoader);

        Field coprocsField = mixinProcessorClass.getDeclaredField("coprocessors");
        coprocsField.setAccessible(true);

        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (!mixinTransformerClass.isInstance(transformer)) {
            throw new ClassCastException(
                    "Active transformer is not an instance of MixinTransformer: " + transformer.getClass()
            );
        }

        Object processor = procField.get(transformer);
        return (List<?>) coprocsField.get(processor);
    }

    private static Class<?> generateCoProcClass(ClassLoader classLoader) throws ReflectiveOperationException {
        byte[] coreCoProcBytecode = CoreMixinCoprocessorGenerator.generate();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                Class.forName(MIXIN_COPROCESSOR, true, classLoader),
                MethodHandles.lookup()
        );
        return lookup.defineClass(coreCoProcBytecode);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}