package com.heroku.kyokuhoku2;

import java.util.Map;
import org.apache.camel.Headers;

public class Utility {

    public void setCustomDelay(@Headers Map headers, Long defaultDelay) {
        Long delay = (Long) headers.get("customDelay");
        delay = delay == null ? defaultDelay : delay * 2;
        headers.put("customDelay", delay);
    }
}
