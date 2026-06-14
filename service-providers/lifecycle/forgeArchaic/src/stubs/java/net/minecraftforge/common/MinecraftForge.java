package net.minecraftforge.common;

import cpw.mods.fml.common.eventhandler.EventBus;

public class MinecraftForge {
    public static final EventBus EVENT_BUS = stubEventBus();

    private static EventBus stubEventBus() {
        throw new IllegalStateException("STUB");
    }
}
