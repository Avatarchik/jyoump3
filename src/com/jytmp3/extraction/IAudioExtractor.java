package com.jytmp3.extraction;

import com.jytmp3.MemoryStream;

import java.io.IOException;

/**
 * Created by Sp0x on 9/24/2014.
 */
public interface IAudioExtractor {
    MemoryStream getVideoStream();
    void setVideoStream(MemoryStream stream);
    void writeChunk(byte bytes[], long timeStamp) throws IOException;//timestamp = uint32

   /* extends  IDisposable

     VideoStream As IO.FileStream

    '''<exception cref="AudioExtractionException">An error occured while writing the chunk.</exception>
    Sub WriteChunk(chunk As Byte(), timeStamp As UInt32)*/


}
