package com.jytmp3;

public interface Action<T , V>{
    V invoke(T arg);
}
