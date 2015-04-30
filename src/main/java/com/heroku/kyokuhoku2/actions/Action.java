package com.heroku.kyokuhoku2.actions;

import com.heroku.kyokuhoku2.sources.Source;
import java.util.ArrayList;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Action extends RouteBuilder {

    @Autowired
    BeanFactory factory;
    private final ArrayList<Class> sourceClasses = new ArrayList<>();
    private String defaultActionEndpoint;

    public boolean isReadyToAction() {
        for (Class clazz : sourceClasses) {
            Source source = (Source) factory.getBean(clazz);
            if (!source.isUpToDate()) {
                return false;
            }
        }
        return true;
    }

    public String defaultActionEndpoint() {
        return defaultActionEndpoint;
    }

    public void defaultActionEndpoint(String endpoint) {
        defaultActionEndpoint = endpoint;
    }

    protected void addSourceClass(Class clazz) {
        sourceClasses.add(clazz);
    }

    public BeanFactory factory() {
        return factory;
    }
}
