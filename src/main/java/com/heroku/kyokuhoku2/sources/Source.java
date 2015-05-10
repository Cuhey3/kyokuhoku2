package com.heroku.kyokuhoku2.sources;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Source extends RouteBuilder {

    @Autowired
    @Getter
    BeanFactory factory;
    @Getter
    private final Set<Class> superiorSourceClasses = new HashSet<>();
    @Getter
    private final Set<Source> superiorSources = new HashSet<>();
    @Setter
    protected String sourceKind;
    @Getter
    @Setter
    protected long modifiedTime = 0L;

    public void buildEndpoint() {
    }

    public void injectSuperiorSources() {
        for (Class clazz : superiorSourceClasses) {
            superiorSources.add((Source) factory.getBean(clazz));
        }
    }

    public boolean isUpToDate() {
        long parentModifiedTime = 0L;
        for (Source source : superiorSources) {
            parentModifiedTime = Math.max(parentModifiedTime, source.getModifiedTime());
        }
        return modifiedTime >= parentModifiedTime;
    }
}
