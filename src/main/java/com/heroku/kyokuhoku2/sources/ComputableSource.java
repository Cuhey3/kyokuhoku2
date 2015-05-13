package com.heroku.kyokuhoku2.sources;

import org.apache.camel.ProducerTemplate;
import static java.lang.String.*;
import org.apache.camel.impl.DefaultExchange;

public abstract class ComputableSource extends Source {

    public String computeEndpoint;
    public ProducerTemplate computeProducerTemplate;

    @Override
    public void buildEndpoint() {
        computeEndpoint = format("direct:%s.compute", sourceKind);
    }

    public void checkProducerTemplate() {
        if (computeProducerTemplate == null) {
            computeProducerTemplate = this.getContext().createProducerTemplate();
            computeProducerTemplate.setDefaultEndpointUri(computeEndpoint);
        }
    }

    public void compute() {
        checkProducerTemplate();
        computeProducerTemplate.send(new DefaultExchange(this.getContext()));
    }
}
