package io.github.kurrycat.mpkmod.loader.archaicForge;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import io.github.kurrycat.mpkmod.api.loader.ForgeModContainer;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.loader.commonForge.CommonForgeEntrypoint;

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
            bus.register(EventReceiver.INSTANCE);
            return true;
        }
    }

    public static class EventReceiver {
        private static final EventReceiver INSTANCE = new EventReceiver();

        private EventReceiver() {}

        @Subscribe
        public void onInitialize(FMLInitializationEvent ignored) {
            ModLifecycle.HANDLE.get().init();
        }
    }
}
