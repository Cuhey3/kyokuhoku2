package com.heroku.kyokuhoku2.sources;

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
    protected long modifiedTime = 0L;

    public abstract void buildEndpoint();

    public abstract boolean isUpToDate();
}
