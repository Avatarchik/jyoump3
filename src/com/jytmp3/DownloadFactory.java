package com.jytmp3;

import com.jytmp3.extraction.*;

import static propel.core.utils.Linq.*;

import propel.core.functional.Functions.*;
import propel.core.functional.Predicates.*;

import java.util.Optional;

/**
 * Created by Sp0x on 10/17/2014.
 */
public class DownloadFactory {

    public static Downloader create(VideoCodecInfo codec,DownloadOptions ops , Boolean isPlaylist){
        Downloader downloader;
        ops.IsPlaylist = isPlaylist;

        if(ops.getOnlyVideo()) {
            downloader = Factory<VideoDownloader>.Create(codec, ops.getOutput());
        }else {
            downloader = Factory<AudioDownloader).Create(codec, ops.getOutput);
        }

        downloader.IsPlaylistMember = isPlaylist;
        downloader.setInitialized(true);
        return downloader;
    }



    public static Downloader create(String url,DownloadOptions options, Boolean isPlaylist , Boolean lazy ) throws VideoNotAvailableException, ArgumentNullException {
        Downloader result ;
        if(lazy){
            result = Downloader.CreateEmpty(url, options, isPlaylist);
        }else {
            VideoCodecInfo codec = options.GetCodec(url);
            result = create(codec, options, isPlaylist);
        }
        result.inputUrl = url;
        return result;
    }

    public static Downloader create(YtVideo video,DownloadOptions options, Boolean isPlaylist, Boolean lazy ) throws VideoNotAvailableException {
        Downloader result;
        if(lazy) {
            result = Downloader.CreateEmpty(video.toString(), options, isPlaylist);
        }else {
            VideoCodecInfo codec = options.GetCodec(video);
            result = create(codec, options, isPlaylist);
        }
        result.InputUrl = video.toString();
        return result;
    }


    Public Shared Async Function CreateListAsync(link As String, outputpath As String, onlyVideo As Boolean, _
            format As String, quality As Int32, _
                                                         Optional isLazy As Boolean = True) As Task(Of List(Of Downloader))

    Return Await Task.Factory.StartNew(Function()
    Return CreateList(link, outputpath, onlyVideo, format, quality, isLazy)
    End Function)
    End Function
    Public Shared Async Sub ExecuteListAsync(link As String, outputpath As String, onlyVideo As Boolean, _
            format As String, quality As Int32, action As Action(Of Downloader))

    Await Task.Factory.StartNew(Sub()
    ExecuteList(link, outputpath, onlyVideo, format, quality, action)
    End Sub)
    End Sub
    Public Shared Sub ExecuteList(link As String, outputPath As String, onlyVideo As Boolean, _
            format As String, quality As Int32, action As Action(Of Downloader))
    Dim dldOps As DownloadOptions = DownloadOptionsBuilder.Build(outputPath, _
            onlyVideo, format, quality)
    ParseLink(link, dldOps, action)
    End Sub
    Public Shared Function CreateList(link As String, outputPath As String, onlyVideo As Boolean, _
            format As String, quality As Int32, Optional lazy As Boolean = True) As List(Of Downloader)
    Dim dldOps As DownloadOptions = DownloadOptionsBuilder.Build(outputPath, _
            onlyVideo, format, quality)
    Return ParseLink(link, dldOps, Nothing, lazy)
    End Function

    Private Shared Function ParseLink(link As String, dldOps As DownloadOptions, actionOnParse As Action(Of Downloader), _
    Optional lazy As Boolean = True) As IEnumerable(Of Downloader)
    Dim output As New List(Of Downloader)
    Dim dldr As Downloader = Nothing
    If YtPlaylist.IsPlaylist(link) Then
    Dim ytl As New YtPlaylist(link, dldOps)
    For Each video As YtVideo In ytl
            dldr = video.GetDownloader(ytl.DownloadOptions, True, lazy:=lazy) ' Lazy should be here, no preprocessing

    If actionOnParse IsNot Nothing Then
                                   actionOnParse(dldr)
    Else
    output.Add(dldr)
    End If
    Next
            Else
    dldr = (Downloader.Factory.Create(link, dldOps, lazy:=lazy))
    If actionOnParse IsNot Nothing Then
                                   actionOnParse(dldr)
    Else
    output.Add(dldr)
    End If
    End If
    Return output
    End Function
}

    public class DownloadFactoryEx <TDldType extends Downloader> {
    public static <TDldType extends Downloader> TDldType create(VideoCodecInfo video, String outputFile, int bytesToDownload,
                                                       Boolean isPlaylsit , Boolean lazy){
        TDldType dldr = new TDldType();
        dldr.setDownloadOptions((new DownloadOptions(false,"mp3",0)));
        DownloadOptions ops = dldr.getDownloadOptions();
        ops.SizeLimit=bytesToDownload;
        dldr.setDownloadOptions(ops);
        dldr.OutputPath = outputFile;
        dldr.setVideoCodec(video);
        dldr.setInitialized(true);
        return dldr;
    }

    public static void SetExtendor(Downloader dldr){
        Downloader tmpCopy = dldr.clone();
        dldr = new TDldType();
        tmpCopy.cloneMembersTo(dldr);
    }


    public static YtVideo FetchVideo(String url) throws ArgumentException, NullReferenceException, YoutubeParseException {
        YtUrlDecoder ytUrl = new YtUrlDecoder();
        if(YtVideo.GetVideoId(url)==null)
            throw new ArgumentException("You must specify a valid url containing the youtube video id.", "url");

        try {
            return ytUrl.GetVideo(url,true);
        }catch(Exception ex){
            System.out.print(ex.getStackTrace());
            return new YtVideo("", false);
        }
    }

    public static synchronized void createAsync(String url,DownloadOptions ops, Boolean isPlaylist ){//As Task(Of TDldType)
        (new Thread(new Runnable(){
            @Override
            public void run() {
                create(url, ops, isPlaylist);
            }
        })).run();
    }





    public static TDldType create(String url,DownloadOptions ops, Boolean isPlaylsit) throws ArgumentNullException, VideoNotAvailableException {
        if(url==null) throw new ArgumentNullException("url");
        YtVideo ytVid = FetchVideo(url);
        VideoCodecInfo vCodec;
        if(ops.getFilter()==null) {
            if(ops.getOnlyVideo()) {
              vCodec = firstOrDefault(VideoCodecInfo.orderByResolution(ytVid.getCodecs(), false));
            }
            else{
                vCodec = firstOrDefault(VideoCodecInfo.orderByAudioBitrate(
                        where(ytVid.getCodecs(), new Predicate1<VideoCodecInfo>(){
                            @Override
                            public boolean evaluate(VideoCodecInfo videoCodecInfo) {
                                return videoCodecInfo.canExtractAudio();
                            }
                        }), false));
            }
        }else{
            vCodec = ops.GetCodec(ytVid);
            if(vCodec==null){
                throw new VideoNotAvailableException("Can't find a valid video codec.");
            }
        }
        Downloader result = create(vCodec, ops.getOutput(), ops.SizeLimit);
        result.InputUrl = url;
        return result;
    }



}
