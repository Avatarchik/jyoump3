package com.jytmp3.extraction;

import com.jytmp3.AudioType;

/**
 * Created by Sp0x on 9/28/2014.
 */
public class VideoCodecInfo {

    public static VideoCodecInfo defaults[] = {
            new VideoCodecInfo(5, VideoType.Flash, 240, false, AudioType.Mp3, 64, AdaptiveType.None),
            new VideoCodecInfo(6, VideoType.Flash, 270, false, AudioType.Mp3, 64, AdaptiveType.None),
            new VideoCodecInfo(13, VideoType.Mobile, 0, false, AudioType.Aac, 0, AdaptiveType.None),
            new VideoCodecInfo(17, VideoType.Mobile, 144, false, AudioType.Aac, 24, AdaptiveType.None),
            new VideoCodecInfo(18, VideoType.Mp4, 360, false, AudioType.Aac, 96, AdaptiveType.None),
            new VideoCodecInfo(22, VideoType.Mp4, 720, false, AudioType.Aac, 192, AdaptiveType.None),
            new VideoCodecInfo(34, VideoType.Flash, 360, false, AudioType.Aac, 128, AdaptiveType.None),
            new VideoCodecInfo(35, VideoType.Flash, 480, false, AudioType.Aac, 128, AdaptiveType.None),
            new VideoCodecInfo(36, VideoType.Mobile, 240, false, AudioType.Aac, 38, AdaptiveType.None),
            new VideoCodecInfo(37, VideoType.Mp4, 1080, false, AudioType.Aac, 192, AdaptiveType.None),
            new VideoCodecInfo(38, VideoType.Mp4, 3072, false, AudioType.Aac, 192, AdaptiveType.None),
            new VideoCodecInfo(43, VideoType.WebM, 360, false, AudioType.Vorbis, 128, AdaptiveType.None),
            new VideoCodecInfo(44, VideoType.WebM, 480, false, AudioType.Vorbis, 128, AdaptiveType.None),
            new VideoCodecInfo(45, VideoType.WebM, 720, false, AudioType.Vorbis, 192, AdaptiveType.None),
            new VideoCodecInfo(46, VideoType.WebM, 1080, false, AudioType.Vorbis, 192, AdaptiveType.None),
            new VideoCodecInfo(82, VideoType.Mp4, 360, true, AudioType.Aac, 96, AdaptiveType.None),
            new VideoCodecInfo(83, VideoType.Mp4, 240, true, AudioType.Aac, 96, AdaptiveType.None),
            new VideoCodecInfo(84, VideoType.Mp4, 720, true, AudioType.Aac, 152, AdaptiveType.None),
            new VideoCodecInfo(85, VideoType.Mp4, 520, true, AudioType.Aac, 152, AdaptiveType.None),
            new VideoCodecInfo(100, VideoType.WebM, 360, true, AudioType.Vorbis, 128, AdaptiveType.None),
            new VideoCodecInfo(101, VideoType.WebM, 360, true, AudioType.Vorbis, 192, AdaptiveType.None),
            new VideoCodecInfo(102, VideoType.WebM, 720, true, AudioType.Vorbis, 192, AdaptiveType.None),
            new VideoCodecInfo(133, VideoType.Mp4, 240, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(134, VideoType.Mp4, 360, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(135, VideoType.Mp4, 480, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(136, VideoType.Mp4, 720, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(137, VideoType.Mp4, 1080, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(160, VideoType.Mp4, 144, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(242, VideoType.WebM, 240, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(243, VideoType.WebM, 360, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(244, VideoType.WebM, 480, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(247, VideoType.WebM, 720, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(248, VideoType.WebM, 1080, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(264, VideoType.Mp4, 1440, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(278, VideoType.WebM, 144, false, AudioType.Unknown, 0, AdaptiveType.Video),
            new VideoCodecInfo(139, VideoType.Mp4, 0, false, AudioType.Aac, 48, AdaptiveType.Audio),
            new VideoCodecInfo(140, VideoType.Mp4, 0, false, AudioType.Aac, 128, AdaptiveType.Audio),
            new VideoCodecInfo(141, VideoType.Mp4, 0, false, AudioType.Aac, 256, AdaptiveType.Audio),
            new VideoCodecInfo(171, VideoType.WebM, 0, false, AudioType.Vorbis, 128, AdaptiveType.Audio),
            new VideoCodecInfo(172, VideoType.WebM, 0, false, AudioType.Vorbis, 192, AdaptiveType.Audio)};

    public VideoCodecInfo(int formatCode) {
        this(formatCode, VideoType.Unknown, 0, false, AudioType.Unknown, 0,
                AdaptiveType.None);
    }

    protected VideoCodecInfo(VideoCodecInfo info){
        this(info.formatCode,info.videoType,info.resolution,info.is3D,info.audioType,info.audioBitrate, info.adaptiveType);
    }
    private VideoCodecInfo(int formatCode, VideoType videoType, int resolution, boolean is3D , AudioType audioType, int audioBitrate,
                           AdaptiveType adaptiveType) {
        this.formatCode=formatCode;
        this.videoType=videoType;
        this.resolution=resolution;
        this.is3D=is3D;
        this.audioType=audioType;
        this.audioBitrate=audioBitrate;
        this.adaptiveType=adaptiveType;

    }



    public String getAudioExtension() {
        switch (this.audioType) {
            case Aac:
                return ".aac";
            case Mp3:
                return ".mp3";
            case Vorbis:
                return ".ogg";
        }
        return "";
    }

    public AudioType audioType;
    public boolean canExtractAudio() {
        return videoType==VideoType.Flash;
    }
    public String downloadUrl;
    public int formatCode;
    public boolean is3D;
    public boolean requiresDecryption;
    public int resolution;
    public String title;
    public AdaptiveType adaptiveType;
    public int audioBitrate;

   /* '''<summary>"
            '''Gets the video extension."
            '''</summary>"
            '''<value>The video extension, or <c>null</c> if the video extension is unknown.</value>"*/
    public String getVideoExtension() {
        switch (this.videoType) {
            case Mp4:
                return ".mp4";
            case Mobile:
                return ".3gp";
            case WebM:
                return ".webm";
            case Flash:
                return ".flv";
        }
        return "";
    }
    public VideoType videoType;
    protected String htmlPlayerVersion;

    @Override
    public String toString() {
        return String.format("Full Title: {0}, Type: {1}, Resolution: {2}p", this.title + this.getVideoExtension(), this.videoType, this.resolution);
    }


}
