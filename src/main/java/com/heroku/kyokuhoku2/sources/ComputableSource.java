package com.heroku.kyokuhoku2.sources;

import static java.lang.String.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

public abstract class ComputableSource extends Source {

    @Getter
    public String computeEndpoint;
    @Getter
    private final Set<Class> superiorSourceClasses = new HashSet<>();
    @Getter
    private final Set<Source> superiorSources = new HashSet<>();

    @Override
    public void buildEndpoint() {
        computeEndpoint = format("direct:%s.compute", sourceKind);
    }

    public void injectSuperiorSources() {
        for (Class clazz : superiorSourceClasses) {
            superiorSources.add((Source) factory.getBean(clazz));
        }
    }

    @Override
    public boolean isUpToDate() {
        long parentModifiedTime = 0L;
        for (Source source : superiorSources) {
            parentModifiedTime = Math.max(parentModifiedTime, source.getModifiedTime());
        }
        return modifiedTime >= parentModifiedTime;
    }
}
