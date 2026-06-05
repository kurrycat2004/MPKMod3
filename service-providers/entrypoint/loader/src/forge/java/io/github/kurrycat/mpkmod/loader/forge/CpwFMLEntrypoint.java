package io.github.kurrycat.mpkmod.loader.forge;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.loader.ForgeModContainer;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;

import java.util.List;
import java.util.Optional;

public final class CpwFMLEntrypoint implements ForgeModContainer {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ForgeModContainer> {
        public Provider() {
            super(CpwFMLEntrypoint::new, ForgeModContainer.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("cpw.mods.fml.common.DummyModContainer")) {
                return Optional.of("cpw.mods.fml ModContainer not found");
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
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
        }
    }
}
