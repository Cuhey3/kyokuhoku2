package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.JsonUtil;
import com.heroku.kyokuhoku2.Utility;
import com.heroku.kyokuhoku2.sources.Source;
import java.util.List;
import lombok.Getter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JsonizeSource extends Source {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    protected Object jsonObject;
    protected String diffString;
    protected List diffList;
    @Getter
    protected boolean isInitDone = false;
    protected Object upcomingJsonString;
    protected String entryEndpointUri = String.format("seda:jsonize.%s.entry", sourceKind);
    protected String updateEndpointUri = String.format("seda:jsonize.%s.update", sourceKind);
    protected String initEndpointUri = String.format("seda:jsonize.%s.init", sourceKind);

    @Override
    public void configure() throws Exception {
        from(entryEndpointUri)
                .choice().when().method(this, "isInitDone")
                .to(updateEndpointUri)
                .otherwise()
                .bean(Utility.class, "setCustomDelay")
                .delay(simple("${header.customDelay}"))
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
}
