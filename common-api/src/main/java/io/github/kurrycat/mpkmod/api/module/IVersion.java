package io.github.kurrycat.mpkmod.api.module;

public interface IVersion extends Comparable<IVersion> {
    default boolean satisfies(Constraint range) {
        return range.isSatisfiedBy(this);
    }

    interface Constraint {
        boolean isSatisfiedBy(IVersion version);
    }
}
