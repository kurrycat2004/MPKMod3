package io.github.kurrycat.mpkmod.loader.forge.event.forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.entrypoint.ModLoop;
import io.github.kurrycat.mpkmod.loader.forge.event.IEventReceiver;
import io.github.kurrycat.mpkmod.service.util.ServiceUtil;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@AutoService(ForgeEventReceiverProvider.class)
public class RenderGameOverlayReceiverProvider extends ForgeEventReceiverProvider {
    @Override
    public boolean canProvide() {
        return ServiceUtil.doesClassExist("net.minecraftforge.client.event.RenderGameOverlayEvent$Text");
    }

    @Override
    public String eventType() {
        return "RenderGameOverlayEvent";
    }

    @Override
    public int eventTypePriority() {
        return 10;
    }

    @Override
    public IEventReceiver provide() {
        return EventReceiver.INSTANCE;
    }

    public static class EventReceiver implements IEventReceiver {
        private static final EventReceiver INSTANCE = new EventReceiver();

        @cpw.mods.fml.common.eventhandler.SubscribeEvent
        @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
        public void onRenderGameOverlay(RenderGameOverlayEvent.Text ignored) {
            ModLoop.HANDLE.get().frame();
        }
    }
}
