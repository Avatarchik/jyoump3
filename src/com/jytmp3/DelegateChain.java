package com.jytmp3;

import org.apache.commons.lang3.ArrayUtils;
import sun.awt.EventQueueDelegate;

public class DelegateChain <TDlgType extends EventQueueDelegate.Delegate>
{
    private TDlgType delegates[];
    public DelegateChain()
    {
    }
    public void add(TDlgType delegate)
    {
        ArrayUtils.add(delegates, delegate);
    }
    public void execute()
}
