package dev.ploats.slopstrocity.api.misc;

@FunctionalInterface
public interface QuadConsumer<F, S, T, FO> {

    void accept(F f, S s, T t, FO fo);
}