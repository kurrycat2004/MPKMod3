package io.github.kurrycat.mpkmod.transformer.mixin;

import io.github.kurrycat.mpkmod.api.App;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Results in:
 * <pre>{@code
 * public class CoreMixinCoprocessor extends MixinCoprocessor {
 *     private final MethodHandle transformHandle;
 *     private final MethodHandle shouldTransformHandle;
 *
 *     public CoreMixinCoprocessor(MethodHandle transformHandle, MethodHandle shouldTransformHandle) {
 *         this.transformHandle = transformHandle;
 *         this.shouldTransformHandle = shouldTransformHandle;
 *     }
 *
 *     @Override
 *     String getName() {
 *         return Tags.MOD_ID + "-core-transformer";
 *     }
 *
 *     @Override
 *     ProcessResult process(String className, ClassNode node) {
 *         try {
 *             return (boolean) transformHandle.invokeExact(className, node)
 *                    ? ProcessResult.TRANSFORMED
 *                    : ProcessResult.NONE;
 *         } catch (Throwable t) {
 *             throw new RuntimeException(t);
 *         }
 *     }
 *
 *     @Override
 *     boolean couldTransform(String className) {
 *         try {
 *             return (boolean) shouldTransformHandle.invokeExact(className);
 *         } catch (Throwable t) {
 *             throw new RuntimeException(t);
 *         }
 *     }
 * }
 * }</pre>
 */
public class CoreMixinCoprocessorGenerator implements Opcodes {
    @SuppressWarnings("CommentedOutCode")
    public static byte[] generate() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String className = "org/spongepowered/asm/mixin/transformer/CoreMixinCoprocessor";
        String superName = "org/spongepowered/asm/mixin/transformer/MixinCoprocessor";

        cw.visit(V1_8, ACC_PUBLIC, className, null, superName, null);

        // Inner class ProcessResult
        cw.visitInnerClass("org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                "org/spongepowered/asm/mixin/transformer/MixinCoprocessor",
                "ProcessResult", ACC_FINAL | ACC_STATIC | ACC_ENUM);

        {
        /*
        private final MethodHandle transformHandle;
        private final MethodHandle shouldTransformHandle;
        */
            cw.visitField(ACC_PRIVATE | ACC_FINAL, "transformHandle", "Ljava/lang/invoke/MethodHandle;", null, null).visitEnd();
            cw.visitField(ACC_PRIVATE | ACC_FINAL, "shouldTransformHandle", "Ljava/lang/invoke/MethodHandle;", null, null).visitEnd();
        }
        {
         /*
         public CoreMixinCoprocessor(MethodHandle transformHandle, MethodHandle shouldTransformHandle) {
             super();
             this.transformHandle = transformHandle;
             this.shouldTransformHandle = shouldTransformHandle;
         }
         */
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodHandle;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, className, "transformHandle", "Ljava/lang/invoke/MethodHandle;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(PUTFIELD, className, "shouldTransformHandle", "Ljava/lang/invoke/MethodHandle;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
        /*
        @Override
        protected String getName() {
            return "core-transformer";
        }
        */
            MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "getName", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitLdcInsn(App.id() + "-core-transformer");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
        /*
        @Override
        public MixinCoprocessor.ProcessResult process(String className, ClassNode classNode) {
            try {
                return handle.invokeExact(className, classNode)
                        ? MixinCoprocessor.ProcessResult.TRANSFORMED
                        : MixinCoprocessor.ProcessResult.NONE;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
         */
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "process", "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;", null, null);
            mv.visitCode();

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchBlock = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchBlock, "java/lang/Throwable");

            mv.visitLabel(tryStart);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "handle", "Ljava/lang/invoke/MethodHandle;");
            mv.visitVarInsn(ALOAD, 1); // className
            mv.visitVarInsn(ALOAD, 2); // classNode
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z", false);

            Label elseLabel = new Label();
            mv.visitJumpInsn(IFEQ, elseLabel);

            mv.visitFieldInsn(GETSTATIC,
                    "org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                    "TRANSFORMED",
                    "Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;");
            Label returnLabel = new Label();
            mv.visitJumpInsn(GOTO, returnLabel);

            mv.visitLabel(elseLabel);
            mv.visitFieldInsn(GETSTATIC,
                    "org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                    "NONE",
                    "Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;");

            mv.visitLabel(returnLabel);
            mv.visitInsn(ARETURN);
            mv.visitLabel(tryEnd);

            // Catch block
            mv.visitLabel(catchBlock);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
        /*
        @Override
        boolean couldTransform(String className) {
            try {
                return (boolean) shouldTransformHandle.invokeExact(className);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        */
            MethodVisitor mv = cw.visitMethod(0, "couldTransform", "(Ljava/lang/String;)Z", null, null);
            mv.visitCode();
            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchBlock = new Label();

            mv.visitTryCatchBlock(tryStart, tryEnd, catchBlock, "java/lang/Throwable");

            mv.visitLabel(tryStart);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "shouldTransformHandle", "Ljava/lang/invoke/MethodHandle;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", "(Ljava/lang/String;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitLabel(tryEnd);

            mv.visitLabel(catchBlock);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            // public synthetic bridge onInit(MixinInfo)
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "onInit",
                    "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, superName, "onInit",
                    "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            // public synthetic bridge onPrepare(MixinInfo)
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "onPrepare", "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, superName, "onPrepare", "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }
}