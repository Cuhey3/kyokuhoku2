package com.heroku.kyokuhoku2.sources;

import static java.lang.String.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public abstract class ComputableSource extends Source {

    @Getter
    public String computeEndpoint;
    @Setter
    public String jsonEntryEndpoint;
    @Getter
    private final Set<Class> superiorSourceClasses = new HashSet<>();
    @Getter
    private final Set<Source> superiorSources = new HashSet<>();

    @Override
    public void buildEndpoint() {
        super.buildEndpoint();
        computeEndpoint = format("direct:%s.compute", sourceKind);
    }

    @Override
    public void configure() throws Exception {
        from(initEndpoint)
                .bean(this, "injectSuperiorSources()")
                .bean(this, "ready()");

        from(computeEndpoint)
                .bean(this, "compute");
    }

    public void injectSuperiorSources() {
        for (Class clazz : superiorSourceClasses) {
            superiorSources.add((Source) factory.getBean(clazz));
        }
        System.out.println(this.getClass().getName() +" injected.");
    }

    @Override
    public boolean isUpToDate() {
        long parentUpdateTime = 0L;
        for (Source source : superiorSources) {
            parentUpdateTime = Math.max(parentUpdateTime, source.getUpdateTime());
        }
        return checkForUpdateTime >= parentUpdateTime;
    }

    public abstract Object compute();
}
