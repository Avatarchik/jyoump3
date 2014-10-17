package com.jytmp3.extraction;

/**
 * Created by Sp0x on 10/16/2014.
 */
public class ArgumentNullException extends Exception {
    public ArgumentNullException(String videoUrl) {
        super(videoUrl);
    }

}
