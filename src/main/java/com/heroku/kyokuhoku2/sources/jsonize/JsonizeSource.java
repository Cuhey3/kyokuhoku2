package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.JsonUtil;
import com.heroku.kyokuhoku2.sources.Source;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JsonizeSource extends Source {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    protected Object jsonObject;
    protected String diffString;
    protected List diffList;
    protected boolean isInitDone = false;
    protected Object upcomingJsonString;
    protected String entryEndpointUri = String.format("seda:jsonize.%s.entry", sourceKind);
    protected String updateEndpointUri = String.format("seda:jsonize.%s.update", sourceKind);
    protected String initEndpointUri = String.format("seda:jsonize.%s.init", sourceKind);

    @Override
    public void configure() throws Exception {
        from(entryEndpointUri)
                .choice().when(isInitDone())
                .to(updateEndpointUri)
                .otherwise()
                .process(setDelay())
                .to(entryEndpointUri);

        from(initEndpointUri)
                .to("direct:utility.mapBodyToHeader")
                .process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String str = exchange.getIn().getHeader("json", String.class);
                        if (str.isEmpty()) {
                            str = "{}";
                        }
                    }
                });

    }

    public void entry(Object object) {
        String str = jsonUtil.getJsonString(object);
        if (str.isEmpty()) {
            System.out.println(String.format("無効なエントリーです。kind:%s object:%s", sourceKind, object));
        } else {
            upcomingJsonString = str;
            ProducerTemplate pt = this.getContext().createProducerTemplate();
            DefaultExchange exchange = new DefaultExchange(this.getContext());
            pt.send(entryEndpointUri, exchange);
        }
    }

    private Predicate isInitDone() {
        return new Predicate() {

            @Override
            public boolean matches(Exchange exchange) {
                return isInitDone;
            }
        };
    }

    public Processor setDelay() {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                Long delay = exchange.getIn().getHeader("myDelay", Long.class);
                if (delay == null) {
                    delay = 1000L;
                }
                delay *= 2;
                exchange.getIn().setHeader("myDelay", delay);
            }
        };
    }
}
