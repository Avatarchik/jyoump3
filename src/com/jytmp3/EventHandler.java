package com.jytmp3;

/**
 * Created by Sp0x on 10/17/2014.
 */
public abstract class EventHandler <TEventArgument> extends DelegateChain{

    public abstract void handle(Object sender, TEventArgument ev);
}
