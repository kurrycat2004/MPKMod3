package net.minecraftforge.fml.common;

import com.google.common.eventbus.EventBus;

import java.io.File;

public class DummyModContainer {
    public DummyModContainer(ModMetadata md) {
        throw new IllegalStateException("STUB");
    }

    public Object getMod() {
        throw new IllegalStateException("STUB");
    }

    public boolean matches(Object mod) {
        throw new IllegalStateException("STUB");
    }

    public File getSource() {
        throw new IllegalStateException("STUB");
    }

    public boolean registerBus(EventBus bus, LoadController controller) {
        throw new IllegalStateException("STUB");
    }

    public Class<?> getCustomResourcePackClass() {
        throw new IllegalStateException("STUB");
    }
}
