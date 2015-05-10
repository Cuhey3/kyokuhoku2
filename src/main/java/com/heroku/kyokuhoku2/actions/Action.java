package com.heroku.kyokuhoku2.actions;

import com.heroku.kyokuhoku2.sources.Source;
import java.util.ArrayList;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.Body;
import org.apache.camel.Headers;
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
    protected String defaultActionEndpoint;

    @Override
    public void configure() throws Exception {
        from(defaultActionEndpoint).bean(this, "defaultAction");
    }

    public abstract void defaultAction(@Body Object body, @Headers Map headers);

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
