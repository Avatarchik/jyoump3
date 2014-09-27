package com.jytmp3.extraction;

import com.jytmp3.MemoryStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Sp0x on 9/27/2014.
 */
public abstract class AudioExtractor  extends VideoStreamed implements IAudioExtractor
{
    int channelConfig;
    public static final int MAX_CHUNK = 1024 * 1000 * 2;
    public abstract void dispose() throws IOException;
    public abstract void writeChunk(byte[] bytes, long timeStamp) throws IOException, AudioExtractionException;
    public AudioExtractor(String path) throws FileNotFoundException {
        super(path);
    }
}
