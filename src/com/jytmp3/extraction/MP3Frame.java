package com.jytmp3.extraction;

/**
 * Created by Sp0x on 9/26/2014.
 */
public class MP3Frame {
//region Variables
    public int  p_ChannelMode;
    public int  p_BitRate;
    public long p_FirstFrameHeader; //uint
    public int  p_MpegVersion;
    public int  p_SampleRate;
    public int  p_LayerCount;
    private int p_Padding;
//endregion

    //region "Static variables"
    public static int[] mpeg1BitRate = new int[]{0, 32, 40, 48, 56, 64,
            80, 96, 112, 128, 160, 192,
            224, 256, 320, 0};
    public static int[] mpeg2XBitRate = new int[]{0, 8, 16, 24, 32, 40,
            48, 56, 64, 80, 96, 112,
            128, 144, 160, 0};
    public static int[] mpeg1SampleRate = new int[]{44100, 48000, 32000, 0};
    public static int[] mpeg20SampleRate = new int[]{22050, 24000, 16000, 0};
    public static int[] mpeg25SampleRate = new int[]{11025, 12000, 8000, 0};
//endregion


    //region Setters and Getters
    public int getChannelMode() {
        return p_ChannelMode;
    }

    public MP3Frame setChannelMode(int p_ChannelMode) {
        this.p_ChannelMode = p_ChannelMode;
        return this;
    }

    public int getBitRate() {
        return p_BitRate;
    }

    public MP3Frame setBitRate(int p_BitRate) {
        this.p_BitRate = p_BitRate;
        return this;
    }

    public long getFirstFrameHeader() {
        return p_FirstFrameHeader;
    }

    public MP3Frame setFirstFrameHeader(long p_FirstFrameHeader) {
        this.p_FirstFrameHeader = p_FirstFrameHeader;
        return this;
    }

    public int getMpegVersion() {
        return p_MpegVersion;
    }

    public MP3Frame setMpegVersion(int p_MpegVersion) {
        this.p_MpegVersion = p_MpegVersion;
        return this;
    }

    public int getSampleRate() {
        return p_SampleRate;
    }

    public MP3Frame setSampleRate(int p_SampleRate) {
        this.p_SampleRate = p_SampleRate;
        return this;
    }

    public int getLayerCount()
    {
        return p_LayerCount;
    }
    public MP3Frame setLayerCount(int layer) {
        this.p_LayerCount=layer;
        return this;
    }

    public int getPadding(){return this.p_Padding;}
    public MP3Frame setPadding(int p_Padding) {
        this.p_Padding = p_Padding; return this;
    }
//endregion


    public MP3Frame()
    {
    }

//region Helpers
 /* ''' <summary>
             ''' Gets ssamplerate, based on the version of MPEG
             ''' </summary>
             ''' <param name="mpgVersion"></param>
             ''' <param name="sampleRate"></param>
             ''' <remarks></remarks>*/
public int parseSampleRate() {
    switch (this.p_MpegVersion) {
        case 2:
            this.p_SampleRate = mpeg20SampleRate[this.p_SampleRate]; break;
        case 3:
            this.p_SampleRate = mpeg1SampleRate[this.p_SampleRate]; break;
        default:
            this.p_SampleRate = mpeg25SampleRate[this.p_SampleRate];
    }
    return this.p_SampleRate;
}
//endregion


    public boolean isInvalidFrame()
    {
       return p_MpegVersion==1 || p_LayerCount!=1 || p_BitRate==0 || p_BitRate==15 || p_SampleRate==3;
    }

    public int calcBitRate() {
        return ((this.p_MpegVersion==3 ? mpeg1BitRate[this.p_BitRate] : mpeg2XBitRate[this.p_BitRate])) * 1000;
    }

    public void parseBitRate() {
        this.p_BitRate=calcBitRate();
    }

    public int getFrameLength() {
        return getFrameLength(p_MpegVersion, p_BitRate, p_SampleRate, p_Padding);
    }
    public static int getFrameLength(int mpegVersion, int bitrate, int sampleRate, int padding) {
        int vFlag = (mpegVersion == 3 ? 144 : 72);
        bitrate = vFlag * bitrate;
        return (int) Math.floor(bitrate / sampleRate) + padding;
    }
}
