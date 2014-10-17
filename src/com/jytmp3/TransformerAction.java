package com.jytmp3;

/**
 * Created by Sp0x on 10/17/2014.
 */
public abstract class TransformerAction <T> {
    public abstract T aggregate(T arg, T next);
}

