package com.jytmp3.extraction;

/**
 * Created by Sp0x on 9/27/2014.
 */
public class ProgressEventArgs {
    double percentage;
    Object sender;

    public ProgressEventArgs(IProgressUpdater sender, double perc) {
        this.sender = sender;
        this.percentage = perc;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Object getSender() {
        return sender;
    }

    public void setSender(Object sender) {
        this.sender = sender;
    }
}
