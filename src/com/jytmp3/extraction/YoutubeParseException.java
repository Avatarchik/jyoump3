package com.jytmp3.extraction;

/**
 * Created by Sp0x on 10/15/2014.
 */
public class YoutubeParseException extends Throwable {
    public YoutubeParseException(String s, Exception innerException) {
        super(s,innerException);
    }
}
