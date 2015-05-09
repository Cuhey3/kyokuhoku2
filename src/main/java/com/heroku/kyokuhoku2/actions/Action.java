package com.heroku.kyokuhoku2.actions;

import com.heroku.kyokuhoku2.sources.Source;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Action extends RouteBuilder {

    @Autowired
    @Getter
    BeanFactory factory;
    private final ArrayList<Class> sourceClasses = new ArrayList<>();
    @Getter
    @Setter
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

    protected void addSourceClass(Class clazz) {
        sourceClasses.add(clazz);
    }
}
