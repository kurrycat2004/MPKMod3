package io.github.kurrycat.mpkmod.loader.forge.event.fml;

import com.google.auto.service.AutoService;
import com.google.common.eventbus.Subscribe;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLifecycle;
import io.github.kurrycat.mpkmod.loader.forge.event.IEventReceiver;
import io.github.kurrycat.mpkmod.service.util.ServiceUtil;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@AutoService(FMLEventReceiverProvider.class)
public class FMLInitializationReceiverProvider extends FMLEventReceiverProvider {
    @Override
    public boolean canProvide() {
        return ServiceUtil.doesClassExist("net.minecraftforge.fml.common.event.FMLInitializationEvent");
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
    public IEventReceiver provide() {
        return EventReceiver.INSTANCE;
    }

    private static class EventReceiver implements IEventReceiver {
        private static final EventReceiver INSTANCE = new EventReceiver();

        @Subscribe
        public void onInitialize(FMLInitializationEvent ignored) {
            ModLifecycle.HANDLE.get().init();
        }
    }
}
