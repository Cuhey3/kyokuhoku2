package com.heroku.kyokuhoku2;

import org.apache.camel.Header;

public class Utility {

    public void setCustomDelay(@Header(value = "customDelay") Long delay) {
        if (delay == null) {
            delay = 1000L;
        }
        delay *= 2;
    }
}
