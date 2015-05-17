package com.heroku.kyokuhoku2.sources;

import static java.lang.String.format;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Source extends RouteBuilder {

    @Autowired
    @Getter
    BeanFactory factory;
    @Setter
    protected String sourceKind;
    @Getter
    @Setter
    protected long updateTime = 0L, checkForUpdateTime = 0L;
    @Getter
    protected boolean ready = false;
    protected String initEndpoint;

    public void buildEndpoint() {
        initEndpoint = format("timer:%s.init?repeatCount=1", sourceKind);
    }

    public abstract boolean isUpToDate();

    public void updated() {
        updateTime = System.currentTimeMillis();
    }

    public void checkedForUpdate() {
        checkForUpdateTime = System.currentTimeMillis();
    }

    public void ready() {
        System.out.println(this.getClass().getName() + " is ready.");
        ready = true;
    }
}
