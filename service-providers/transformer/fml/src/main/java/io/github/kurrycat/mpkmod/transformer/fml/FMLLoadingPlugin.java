package io.github.kurrycat.mpkmod.transformer.fml;

import io.github.kurrycat.mpkmod.api.loader.ForgeModContainer;

import java.util.Map;

// We can only specify a single coremod in the MANIFEST.MF, so it has to implement
// both versions of IFMLLoadingPlugin, which means we have to ship the stubs.
// Without the stubs it tries to load the version of IFMLLoadingPlugin that is not present too,
// and therefore just crashes.
// This should be fine as the actual IFMLLoadingPlugin class should always be already loaded
// when forge tries to actually load this implementation.
public final class FMLLoadingPlugin implements
        net.minecraftforge.fml.relauncher.IFMLLoadingPlugin,
        cpw.mods.fml.relauncher.IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return FMLTransformer.tryGetTransformerClasses();
    }

    @Override
    public String getModContainerClass() {
        return ForgeModContainer.HANDLE.get().modContainerImplementation().getName();
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
