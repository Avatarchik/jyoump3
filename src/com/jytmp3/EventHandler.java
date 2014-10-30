package com.jytmp3;

import java.awt.*;

/**
 * Created by Sp0x on 10/17/2014.
 */
public abstract class EventHandler <TEventArgument> extends EventQueue {
    public abstract void handle(Object sender, TEventArgument ev);
}
