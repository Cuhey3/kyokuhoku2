package com.heroku.kyokuhoku2.sources;

import org.apache.camel.builder.RouteBuilder;

public abstract class Source extends RouteBuilder {

    protected boolean upToDate = false;

    public boolean isUpToDate() {
        return upToDate;
    }

    protected void upToDate() {
        this.upToDate = true;
    }

    protected void notUpToDate() {
        this.upToDate = false;
    }

    public abstract <T extends Object> T get(Class<T> type);

    public abstract Object get();
}
