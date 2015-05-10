package com.heroku.kyokuhoku2;

import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class UtilityRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:utility.unmarshal.jsonObject").unmarshal().json(JsonLibrary.Jackson, Map.class);
        from("direct:utility.unmarshal.jsonArray").unmarshal().json(JsonLibrary.Jackson, List.class);
        from("direct:utility.mapBodyToHeader").process(new UtilityMapBodyToHeaderProcessor());
    }

    class UtilityMapBodyToHeaderProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            Utility.mapBodyToHeader(exchange, exchange.getIn().getBody(Map.class), false);
        }
    }
}
