package com.jytmp3;

import com.jytmp3.extraction.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.jytmp3.Utils.dldUrlData;

/**
 * Created by Sp0x on 10/17/2014.
 */
public abstract class Downloader {


//region "Events"
    public EventHandler<IoFinishedEventArgs> DownloadFinished;

    public EventHandler<Object> DownloadStarted;
    public EventHandler<ProgressEventArgs> DownloadProgressChanged;
    public EventHandler<ProgressEventArgs> ExtractionProgressChanged;


    protected void raiseDownloadFinished(Object sender , IoFinishedEventArgs e){
        if(DownloadFinished!=null) DownloadFinished.handle(sender, e);
    }
    protected void raiseDownloadStarted(Object sender,Object e){
        if(DownloadStarted!=null) DownloadStarted.handle(sender, e);
    }
    protected void raiseDownloadProgressChanged(Object sender,ProgressEventArgs e){
        e=isUpdateReady(e);
        if(e.IsReady){
             if(DownloadProgressChanged!=null) DownloadProgressChanged.handle(sender, e);
        }
    }
    protected void raiseExtractionProgressChanged(Object sender, ProgressEventArgs e){
        e=isUpdateReady(e);
        if(e.IsReady){
            if(ExtractionProgressChanged!=null) ExtractionProgressChanged.handle(sender, e);
        }
    }
//endregion

//region "Props"
    public String InputUrl;
    public String OutputPath;
    private DownloadOptions _doOptions = new DownloadOptions();
    private VideoCodecInfo _vCodec;
    public Boolean IsPlaylistMember ;
    private Boolean _bInitialized;

    public DownloadOptions getDownloadOptions() {
        if(_doOptions==null) _doOptions = new DownloadOptions();
        return _doOptions;
    }
    public void setDownloadOptions(DownloadOptions ops)
    {
        _doOptions = ops;
    }

    public VideoCodecInfo getVideoCodec() throws VideoNotAvailableException, ArgumentNullException {
        if(_vCodec==null) _vCodec = _doOptions.GetCodec(InputUrl);
        return _vCodec;
    }

    public void setVideoCodec(VideoCodecInfo _vCodec) {
        this._vCodec = _vCodec;
    }

    public Boolean getInitialized() {
        return _bInitialized;
    }

    private void setInitialized(Boolean _bInitialized) {
        this._bInitialized = _bInitialized;
    }


    //endregion

    //region "Construction"

    public Downloader(String url, DownloadOptions ops , Boolean isPlaylist){
        InputUrl = url;
        IsPlaylistMember = isPlaylist;
        ops.cloneTo(_doOptions);
        _doOptions.setOnlyVideo(ops.getOnlyVideo());
    }


    //region "Init"

    public static Downloader Initialize(Downloader dldr) throws VideoNotAvailableException, ArgumentNullException, IOException {
        if(dldr._vCodec==null) dldr._vCodec = dldr._doOptions.GetCodec(dldr.InputUrl);
         //dldr = Factory.Create(dldr.VideoCodec, dldr.Options, dldr.IsPlaylistMember)
        if(dldr._doOptions.getOnlyVideo()) {
             Factory<VideoDownloader>.SetExtendor(dldr);
        } else {
            Factory<VideoDownloader>.SetExtendor(dldr);
        }
        CorrectDownloaderPath(dldr);
        dldr.setInitialized(True);
        return dldr;
    }

    private static void  CorrectDownloaderPath(Downloader dldr) throws IOException {
       File tmpOut = new File(dldr.OutputPath);
        if(dldr.OutputPath!=null) {
            if(tmpOut.exists() &&  !dldr.OutputPath.endsWith("\\"))
                dldr.OutputPath += "\\";

        }

        if(dldr.IsPlaylistMember) {
            if(dldr.OutputPath!=null) {
                if(!tmpOut.exists())
                    Files.createDirectory(tmpOut.toPath());
            }
        }

        if(dldr.OutputPath!=null && dldr.IsPlaylistMember) {
            if (!dldr.OutputPath.endsWith("\\")) dldr.OutputPath += "\\";
        }
        dldr.OutputPath = String.format("%s%s", dldr.OutputPath, RemoveIllegalPathCharacters(dldr._vCodec.title));
        if(dldr instanceof AudioDownloader) {
            dldr.OutputPath = Path.ChangeExtension(dldr.OutputPath, dldr._vCodec.getAudioExtension());
        } else if(dldr instanceof  VideoDownloader){
            dldr.OutputPath = Path.ChangeExtension(dldr.OutputPath, dldr._vCodec.getVideoExtension());
        }
    }

    public static Downloader CreateEmpty(String url, DownloadOptions ops,Boolean isPlaylist ) {
        Downloader dlm = new VideoDownloader();
        dlm.InputUrl = url;
        dlm.IsPlaylistMember = isPlaylist;
        dlm.OutputPath = ops.Output;
        ops.CloneTo(dlm.Options);
        dlm._doOptions.SetOnlyVideo(ops.OnlyVideo);
        return dlm;
    }
    //endregion


    //region "Update filtering"
    private double _updateDelta = 0D;
    public double UpdateInterval = 2D;

    protected ProgressEventArgs isUpdateReady(ProgressEventArgs pge) {
        Boolean shouldPrint =false;
        double delta = Math.abs(pge.getPercentage() - _updateDelta);
        if(_updateDelta ==0D || _updateDelta == 100D) {
            shouldPrint = true;
            _updateDelta = pge.getPercentage();
        }else if (delta > UpdateInterval){
            //' get delta
            shouldPrint = true;
            _updateDelta = pge.getPercentage();
        }
        pge.IsReady = shouldPrint;
        return pge;
    }
    //endregion

    //region "Starting"
    protected abstract void StartDownloading();

    public void Start() throws InvalidOperationException {
        if(!getInitialized()) {
            throw new InvalidOperationException("Downloader not initialized.");
        }
        StartDownloading();
    }
    public synchronized void StartAsync() {
        StartThreaded();
    }

    public void StartThreaded() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Start();
                } catch (InvalidOperationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //endregion

    //region "Overrides"
    @Override
    public String toString() {
        return super.toString();
    }
    //endregion

    public byte[] getVideoImageBytes() throws InvalidOperationException, IOException {
        String url = YtVideo.GetVideoCoverUrl(InputUrl);
        return dldUrlData(url);
    }


    //region "Cloning"

    public Downloader clone() {
        Downloader res= cloneMembersTo();
        return res;
    }

    public Downloader cloneMembersTo() {
        Downloader dldr;
        dldr.InputUrl = InputUrl;
        dldr.OutputPath = OutputPath;
        dldr.setDownloadOptions(_doOptions);// = Options
        dldr.setInitialized(getInitialized());
        dldr.IsPlaylistMember = IsPlaylistMember;
        dldr.UpdateInterval = UpdateInterval;
        dldr._updateDelta = _updateDelta;
    }

    //endregion

}
