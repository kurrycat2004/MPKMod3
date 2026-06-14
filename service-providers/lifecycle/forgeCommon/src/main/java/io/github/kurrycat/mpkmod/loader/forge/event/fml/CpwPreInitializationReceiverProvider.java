package io.github.kurrycat.mpkmod.loader.forge.event.fml;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import io.github.kurrycat.mpkmod.loader.forge.event.EventManager;
import io.github.kurrycat.mpkmod.loader.forge.event.IEventReceiver;

@AutoService(FMLEventReceiverProvider.class)
public class CpwPreInitializationReceiverProvider extends FMLEventReceiverProvider {
    @Override
    public boolean canProvide() {
        return doesClassExist("cpw.mods.fml.common.event.FMLPreInitializationEvent");
    }

    @Override
    public String eventType() {
        return "FMLPreInitializationEvent";
    }

    @Override
    public int eventTypePriority() {
        return 0;
    }

    @Override
    public IEventReceiver provide() {
        return EventReceiver.INSTANCE;
    }

    private static class EventReceiver implements IEventReceiver {
        private static final EventReceiver INSTANCE = new EventReceiver();

        @Subscribe
        public void onInitialize(FMLPreInitializationEvent ignored) {
            EventManager.runRegisterForgeEventReceiver();
        }
    }
}
