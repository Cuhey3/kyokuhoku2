package com.heroku.kyokuhoku2.sources;

import java.util.HashSet;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Source extends RouteBuilder {

    @Autowired
    BeanFactory factory;
    protected boolean upToDate = false;
    protected Set<Class> onChangeToUpdateSourceClasses = new HashSet<>();
    protected Set<Class> onChangeActionClasses = new HashSet<>();
    protected String sourceKind;

    public boolean isUpToDate() {
        return upToDate;
    }

    public Predicate isNotUpToDateSourcePredicate() {
        return new Predicate() {

            @Override
            public boolean matches(Exchange exchange) {
                return !upToDate;
            }
        };
    }

    protected void upToDate() {
        this.upToDate = true;
    }

    protected void notUpToDate() {
        this.upToDate = false;
    }

    protected Processor turnOtherSourceToNotUpToDate() {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                for (Class clazz : onChangeToUpdateSourceClasses) {
                    Source source = (Source) factory.getBean(clazz);
                    source.notUpToDate();
                }
            }
        };
    }

    public void onChangeToUpdateSource(Class clazz) {
        onChangeToUpdateSourceClasses.add(clazz);
    }

    public void onChangeAction(Class clazz) {
        onChangeActionClasses.add(clazz);
    }

    public Set<Class> onChangeActionClasses() {
        return onChangeActionClasses;
    }
}
