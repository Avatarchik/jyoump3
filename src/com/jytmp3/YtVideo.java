package com.jytmp3;

import com.jytmp3.extraction.InvalidOperationException;
import com.jytmp3.extraction.VideoCodecInfo;
import static lombok.*;


import java.util.List;
import java.util.regex.Pattern;

import static com.jytmp3.Utils.matchGroup;

/**
 * Created by Sp0x on 10/17/2014.
 */
public class YtVideo {
    public String Id;
    public List<VideoCodecInfo> Codecs;
    public Boolean Available;
    public static Pattern RxVideoId = Pattern.compile("(watch\\?v=)(.*?)(&|$)");




    public YtVideo(String videoId, Boolean bAvailable ) {
        this(videoId);
        this.Available = bAvailable;
    }
    public YtVideo(String videoId){
        this.Id= videoId;
    }

    public List<VideoCodecInfo> getCodecs(){
        YtVideo tmpVideo= Downloader.Factory<AudioDownloader>.FetchVideo(ToString());
        if(tmpVideo==null) return null;
        this.Codecs = tmpVideo.Codecs;
        return this.Codecs;
    }

    public Downloader GetDownloader(DownloadOptions options , Boolean isPlaylist , Boolean lazy ){
        Downloader result ;
        if(lazy) {
            result = Downloader.CreateEmpty(toString(), options, isPlaylist);
        }else {
            result = Downloader.Factory.Create(this, options, isPlaylist, lazy);
        }
        result.inputUrl = toString();
        return result;
    }


    @Override
    public String toString(){
        if(Id==null) {
            try {
                throw new InvalidOperationException("Video ID is null");
            } catch (InvalidOperationException e) {
                e.printStackTrace();
            }
        }
        return String.format("https://www.youtube.com/watch?v=%s", Id);
    }

    public static String GetVideoId(String videoId ){
        return matchGroup(RxVideoId, videoId, 2);
    }

    public static String GetVideoCoverUrl(String videoUrl) throws InvalidOperationException {
        String id  = GetVideoId(videoUrl);
        if(id==null) throw new InvalidOperationException("Invalid Youtube URL!");
        return String.format("http://img.youtube.com/vi/%s/2.jpg", id);
    }

}
