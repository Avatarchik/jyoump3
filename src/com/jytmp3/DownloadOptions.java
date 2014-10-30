package com.jytmp3;
import com.jytmp3.extraction.ArgumentNullException;
import com.jytmp3.extraction.VideoCodecInfo;

import static propel.core.utils.Linq.*;

import com.jytmp3.extraction.VideoNotAvailableException;
import propel.core.collections.*;
import propel.core.functional.Functions.*;
import propel.core.functional.Predicates.*;

import java.util.Optional;

/**
 * Created by Sp0x on 10/17/2014.
 */
public class DownloadOptions {
    //region "Properties"
    private Boolean _onlyVideo;
    public Boolean getOnlyVideo() {
        return _onlyVideo;
    }

    public String Format;
    public int Quality;
    public long SizeLimit;
    private String _strOutput;

    public String getOutput() {
        return _strOutput;
    }
    public void setOutput(String _strOutput) {
        this._strOutput = _strOutput;
    }

    public Boolean IsPlaylist;

    private Predicate1<VideoCodecInfo> dFilter;
    public Predicate1<VideoCodecInfo> getFilter() {
        return dFilter;
    }
    public void setFilter(Predicate1<VideoCodecInfo> dFilter) {
        this.dFilter = dFilter;
    }





    //region "Codec resolvers"

    public VideoCodecInfo GetCodec(String videoId ) throws ArgumentNullException, VideoNotAvailableException {
        if(videoId.contains("http:") || videoId.contains("https:"))
                videoId = YtVideo.GetVideoId(videoId);
        if(videoId==null) throw new ArgumentNullException("videoId");
        return GetCodec(new YtVideo(videoId));
    }

    public VideoCodecInfo GetCodec(YtVideo video) throws VideoNotAvailableException {
        if(video.Codecs==null)
            video = Downloader.Factory<VideoDownloader>.FetchVideo(video.toString());
        VideoCodecInfo codec;
        if(this.Quality > -1 && this.Quality < Integer.MAX_VALUE) {
            codec = firstOrDefault(video.Codecs, dFilter);//video.Codecs.firstOrDefault(this.dFilter);
        }else if(this.Quality == -1 ) {
            codec = firstOrDefault(orderBy(where(video.Codecs,this.dFilter), new Function1<VideoCodecInfo, Comparable>(){
                @Override
                public Comparable apply(VideoCodecInfo videoCodecInfo) { return videoCodecInfo;}
            }));
        }else if(this.Quality == Integer.MAX_VALUE) {
            codec = lastOrDefault(orderBy(where(video.Codecs,this.dFilter), new Function1<VideoCodecInfo, Comparable>(){
                @Override
                public Comparable apply(VideoCodecInfo videoCodecInfo) { return videoCodecInfo;}
            }));
        }

        if(codec==null) {
            throw new VideoNotAvailableException("Can't find a codec matching the parameters!");
        }
        if(!this._onlyVideo)// ' we're interesed when can we download an audio
            this._onlyVideo = !codec.canExtractAudio();

        return codec;
    }
    //endregion

    //region "Construction"
    protected DownloadOptions(Holder<Boolean> onlyvideo , String format, int quality){
        this._onlyVideo = onlyvideo.val;
        this.Format = format;
        this.Quality = quality;
    }
    protected DownloadOptions(String outputPath, Holder<Boolean> onlyvideo, String format, int quality){
        this._strOutput = outputPath;
        this._onlyVideo = onlyvideo.val;
        this.Format = format;
        this.Quality = quality;
    }
    public void setOnlyVideo( Boolean value){
        _onlyVideo = value;
    }
    //endregion

    public DownloadOptions clone(){
        Holder<Boolean> onlyVideo = new Holder<Boolean>(this._onlyVideo);
        DownloadOptions res = new DownloadOptions(onlyVideo, this.Format, this.Quality);
        res.SizeLimit = SizeLimit;
        res._strOutput = _strOutput;
        res.IsPlaylist = IsPlaylist;
        res.dFilter = dFilter;
        return res;
    }

    public void cloneTo( DownloadOptions options){
        Holder<Boolean> onlyVideo = new Holder<Boolean>(this._onlyVideo);
        options = new DownloadOptions(onlyVideo, Format, Quality);
        options.SizeLimit = SizeLimit;
        options._strOutput = this._strOutput;
        options.IsPlaylist = IsPlaylist;
        options.dFilter = dFilter;
    }
}
