package io.github.kurrycat.mpkmod.core.fml;

import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.transformer.TransformerManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public final class CoreLoadingPlugin implements IFMLLoadingPlugin {
    private static final boolean hasInitialized = TransformerManager.HANDLE.get()
            .tryInitialize(IFMLLoadingPlugin.class.getSimpleName());

    @Override
    public String[] getASMTransformerClass() {
        if (!hasInitialized) return new String[0];
        return new String[] { App.group() + ".core.fml.CoreTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
