package io.github.kurrycat.mpkmod.loader.forge.event.guava;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;

@AutoService(GuavaEventReceiverProvider.class)
public class CpwInitializationReceiverProvider extends GuavaEventReceiverProvider {
    @Override
    public boolean canProvide() {
        return doesClassExist("cpw.mods.fml.common.event.FMLInitializationEvent");
    }

    @Override
    public String eventType() {
        return "FMLInitializationEvent";
    }

    @Override
    public int eventTypePriority() {
        return 0;
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
