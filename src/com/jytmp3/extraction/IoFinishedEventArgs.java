package com.jytmp3.extraction;

import com.jytmp3.MemoryStream;

/**
 * Created by Sp0x on 10/17/2014.
 */
public class IoFinishedEventArgs{
    public String Path;
    public MemoryStream Stream ;
    public IOMode Property = IOMode.File;
}
