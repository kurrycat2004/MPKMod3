package io.github.kurrycat.mpkmod.loader.forge.event;

public abstract class EventReceiverProvider {
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
    public abstract IEventReceiver provide();
}
