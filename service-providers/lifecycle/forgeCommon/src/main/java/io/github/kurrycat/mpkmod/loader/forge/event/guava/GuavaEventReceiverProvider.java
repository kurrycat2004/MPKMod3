package io.github.kurrycat.mpkmod.loader.forge.event.guava;

public abstract class GuavaEventReceiverProvider {
    public abstract boolean canProvide();

    /**
     * Only one of each event type will be registered
     */
    public abstract String eventType();

    /**
     * When multiple providers for one {@link #eventType()} are found, the one with the highest priority is selected.
     */
    public abstract int eventTypePriority();

    /**
     * Should always return the same instance
     */
    public abstract IGuavaEventReceiver provide();

    protected final boolean doesClassExist(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
