package com.heroku.kyokuhoku2;

import java.util.List;
import java.util.Map;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class UtilityRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:utility.unmarshal.jsonObject").unmarshal().json(JsonLibrary.Jackson, Map.class);
        from("direct:utility.unmarshal.jsonArray").unmarshal().json(JsonLibrary.Jackson, List.class);
    }
}
