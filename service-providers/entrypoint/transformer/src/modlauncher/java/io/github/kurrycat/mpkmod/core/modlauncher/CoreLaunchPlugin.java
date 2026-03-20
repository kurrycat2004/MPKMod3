package io.github.kurrycat.mpkmod.core.modlauncher;

import com.google.auto.service.AutoService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

@AutoService(ILaunchPluginService.class)
public final class CoreLaunchPlugin implements ILaunchPluginService {
    private static final EnumSet<Phase> AFTER_TRANSFORM = EnumSet.of(Phase.AFTER);
    private static final EnumSet<Phase> NEVER = EnumSet.noneOf(Phase.class);
    private static final boolean hasInitialized = TransformerManager.HANDLE.get()
            .tryInitialize(ILaunchPluginService.class.getSimpleName());

    @Override
    public @NotNull String name() {
        return App.id() + "_core";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (!hasInitialized) return NEVER;
        return TransformerManager.HANDLE.get().shouldTransform(classType.getClassName())
               ? AFTER_TRANSFORM : NEVER;
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        if (!hasInitialized) return false;
        return TransformerManager.HANDLE.get().transform(classNode);
    }
}
