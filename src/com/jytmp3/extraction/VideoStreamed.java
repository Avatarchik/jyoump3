package com.jytmp3.extraction;

import com.jytmp3.MemoryStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class VideoStreamed
{
    MemoryStream videoStream;
    public MemoryStream getVideoStream() {
        return videoStream;
    }
    public void setVideoStream(MemoryStream stream) {
        videoStream = stream;
    }
    public VideoStreamed(String path) throws FileNotFoundException {
        FileOutputStream fout = new FileOutputStream(path, false);
        videoStream = new MemoryStream(fout);
    }
}
