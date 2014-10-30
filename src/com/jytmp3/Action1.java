package com.jytmp3;

public interface Action1<T, TA, V>{
    V invoke(T arg, TA argb);
}
