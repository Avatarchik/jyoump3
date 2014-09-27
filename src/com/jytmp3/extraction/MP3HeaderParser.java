package com.jytmp3.extraction;

/**
 * Created by Sp0x on 9/27/2014.
 */
public abstract class MP3HeaderParser {
    public abstract void update();
    public abstract void update(int mpgVer, int layer, int bitrate, int samplerate, int padding, int channelMode) ;
}
