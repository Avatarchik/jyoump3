package com.jytmp3.extraction;

/**
 * Created by Sp0x on 9/26/2014.
 */
public class MP3Frame {

//region Setters and Getters
    public int getP_ChannelMode() {
        return p_ChannelMode;
    }

    public void setP_ChannelMode(int p_ChannelMode) {
        this.p_ChannelMode = p_ChannelMode;
    }

    public int getP_BitRate() {
        return p_BitRate;
    }

    public void setP_BitRate(int p_BitRate) {
        this.p_BitRate = p_BitRate;
    }

    public long getP_FirstFrameHeader() {
        return p_FirstFrameHeader;
    }

    public void setP_FirstFrameHeader(long p_FirstFrameHeader) {
        this.p_FirstFrameHeader = p_FirstFrameHeader;
    }

    public int getP_MpegVersion() {
        return p_MpegVersion;
    }

    public void setP_MpegVersion(int p_MpegVersion) {
        this.p_MpegVersion = p_MpegVersion;
    }

    public int getP_SampleRate() {
        return p_SampleRate;
    }

    public void setP_SampleRate(int p_SampleRate) {
        this.p_SampleRate = p_SampleRate;
    }
//endregion

    public int  p_ChannelMode;
    public int  p_BitRate;
    public long p_FirstFrameHeader; //uint
    public int  p_MpegVersion;
    public int  p_SampleRate;
    public MP3Frame()
    {
    }
}
