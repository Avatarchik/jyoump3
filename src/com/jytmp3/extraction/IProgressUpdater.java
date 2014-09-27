package com.jytmp3.extraction;

/**
 * Created by Sp0x on 9/27/2014.
 */
public interface IProgressUpdater {
    int addListener(IProgressListener ls);
    void removeListener(IProgressListener ls);
    void raiseUpdate(double progress);
}
