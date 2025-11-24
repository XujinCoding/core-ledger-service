package com.coreledger.function;

@FunctionalInterface
public interface ThreeFunction<P1, P2, P3, R> {

    R apply(P1 p1, P2 p2, P3 p3);
}
