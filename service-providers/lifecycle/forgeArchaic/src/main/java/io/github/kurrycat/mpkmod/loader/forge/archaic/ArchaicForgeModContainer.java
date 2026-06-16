package io.github.kurrycat.mpkmod.loader.forge.archaic;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.lifecycle.forge.ForgeModContainer;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.loader.forge.CommonForgeEntrypoint;
import io.github.kurrycat.mpkmod.loader.forge.event.EventManager;
import io.github.kurrycat.mpkmod.service.util.ServiceUtil;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.List;
import java.util.Optional;

public final class ArchaicForgeModContainer implements ForgeModContainer {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ForgeModContainer> {
        public Provider() {
            super(ArchaicForgeModContainer::new, ForgeModContainer.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!ServiceUtil.doesClassExist("cpw.mods.fml.relauncher.CoreModManager")) {
                return Optional.of("cpw.mods.fml CoreModManager not found");
            }
            return super.invalidReason();
        }
    }

    @Override
    public Class<?> modContainerImplementation() {
        return ModContainer.class;
    }

    public static class ModContainer extends DummyModContainer {
        public ModContainer() {
            super(createModMetadata());
        }

        private static ModMetadata createModMetadata() {
            ModMetadata metadata = new ModMetadata();
            metadata.modId = App.id();
            metadata.name = App.name();
            metadata.description = App.description();
            metadata.url = App.homepage();
            metadata.logoFile = App.logoFile();
            metadata.version = App.version();
            metadata.authorList = List.copyOf(App.authors());
            return metadata;
        }

        @Override
        public Object getMod() {
            return CommonForgeEntrypoint.INSTANCE;
        }

        @Override
        public boolean matches(Object mod) {
            return mod == CommonForgeEntrypoint.INSTANCE;
        }

        @Override
        public File getSource() {
            return CommonForgeEntrypoint.INSTANCE.source();
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            EventManager.registerFMLEventReceiver(bus::register);
            EventManager.registerForgeEventReceiver(MinecraftForge.EVENT_BUS::register);
            return true;
        }

        @Override
        public Class<?> getCustomResourcePackClass() {
            try {
                return Class.forName(
                        "cpw.mods.fml.client.FMLFileResourcePack",
                        true, getClass().getClassLoader()
                );
            } catch (ClassNotFoundException ignored) {
                return null;
            }
        }
    }
}
