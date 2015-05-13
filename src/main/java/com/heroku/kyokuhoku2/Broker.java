package com.heroku.kyokuhoku2;

import com.heroku.kyokuhoku2.sources.ComputableSource;
import com.heroku.kyokuhoku2.sources.Source;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.camel.Body;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class Broker extends RouteBuilder {

    @Autowired
    DefaultListableBeanFactory factory;
    private final String timerEndpoint = "timer:broker.poll?period=5s&delay=5s";
    private final String initEndpoint = "timer:broker.init?repeatCount=1";
    private final Set<ComputableSource> computableSources = new HashSet<>();

    @Override
    public void configure() throws Exception {
        from(initEndpoint)
                .bean(this, "setComputableSources");

        from(timerEndpoint)
                .bean(this, "getOneShoudUpdateSource")
                .filter(body().isNotNull())
                .bean(this, "compute");
    }

    public void setComputableSources() {
        String[] beanNames = factory.getBeanNamesForType(ComputableSource.class);
        for (String beanName : beanNames) {
            ComputableSource bean = (ComputableSource) factory.getBean(beanName);
            bean.injectSuperiorSources();
            computableSources.add(bean);
        }
    }

    public Source getOneShoudUpdateSource() {
        Iterator<ComputableSource> itr = computableSources.iterator();
        while (itr.hasNext()) {
            Source one = itr.next();
            if (!one.isUpToDate()) {
                for (Source source : one.getSuperiorSources()) {
                    if (!source.isUpToDate()) {
                        one = null;
                        break;
                    }
                }
                if (one != null) {
                    return one;
                }
            }
        }
        return null;
    }

    public void compute(@Body ComputableSource source) {
        source.compute();
    }
}
