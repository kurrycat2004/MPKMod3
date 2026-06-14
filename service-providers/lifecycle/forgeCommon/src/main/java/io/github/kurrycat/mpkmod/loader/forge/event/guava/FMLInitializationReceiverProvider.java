package io.github.kurrycat.mpkmod.loader.forge.event.guava;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.Subscribe;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@AutoService(GuavaEventReceiverProvider.class)
public class FMLInitializationReceiverProvider extends GuavaEventReceiverProvider {
    @Override
    public boolean canProvide() {
        return doesClassExist("net.minecraftforge.fml.common.event.FMLInitializationEvent");
    }

    @Override
    public String eventType() {
        return "FMLInitializationEvent";
    }

    @Override
    public int eventTypePriority() {
        return 10;
    }

    @Override
    public IGuavaEventReceiver provide() {
        return EventReceiver.INSTANCE;
    }

    private static class EventReceiver implements IGuavaEventReceiver {
        private static final EventReceiver INSTANCE = new EventReceiver();

        @Subscribe
        public void onInitialize(FMLInitializationEvent ignored) {
            ModLifecycle.HANDLE.get().init();
        }
    }
}
