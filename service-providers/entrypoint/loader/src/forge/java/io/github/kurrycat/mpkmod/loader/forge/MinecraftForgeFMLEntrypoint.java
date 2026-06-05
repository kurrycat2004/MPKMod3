package io.github.kurrycat.mpkmod.loader.forge;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.EventBus;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.loader.ForgeModContainer;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.List;
import java.util.Optional;

public final class MinecraftForgeFMLEntrypoint implements ForgeModContainer {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<ForgeModContainer> {
        public Provider() {
            super(MinecraftForgeFMLEntrypoint::new, ForgeModContainer.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("net.minecraftforge.fml.common.DummyModContainer")) {
                return Optional.of("net.minecraftforge.fml ModContainer not found");
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
