package com.company;

/**
 * Created by Sp0x on 9/24/2014.
 */
public interface IAudioExtractor {
    extends  IDisposable

    Property VideoStream As IO.FileStream

    '''<exception cref="AudioExtractionException">An error occured while writing the chunk.</exception>
    Sub WriteChunk(chunk As Byte(), timeStamp As UInt32)


}
