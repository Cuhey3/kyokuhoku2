package com.heroku.kyokuhoku2.sources;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Source extends RouteBuilder {

    @Autowired
    @Getter
    BeanFactory factory;
    @Getter
    @Setter
    private boolean upToDate = false;
    @Getter
    private final Set<Class> onChangeToUpdateSourceClasses = new HashSet<>();
    @Getter
    private final Set<Class> onChangeActionClasses = new HashSet<>();
    @Setter
    protected String sourceKind;

    public boolean isNotUpToDate() {
        return !isUpToDate();
    }

    public void turnOtherSourceToNotUpToDate() {
        for (Class clazz : onChangeToUpdateSourceClasses) {
            Source source = (Source) getFactory().getBean(clazz);
            source.setUpToDate(false);
        }
    }

    public void onChangeToUpdateSource(Class clazz) {
        onChangeToUpdateSourceClasses.add(clazz);
    }

    public void onChangeAction(Class clazz) {
        onChangeActionClasses.add(clazz);
    }

    public void buildEndpoint() {
    }
}
