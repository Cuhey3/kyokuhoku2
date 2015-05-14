package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.JsonUtil;
import com.heroku.kyokuhoku2.Utility;
import com.heroku.kyokuhoku2.sources.ComputableSource;
import java.util.List;
import lombok.Getter;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JsonizeSource extends ComputableSource {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    protected Object jsonObject;
    protected String diffString;
    protected List diffList;
    @Getter
    protected boolean isInitDone = true;
    protected Object upcomingJsonString;
    protected String updateEndpoint;

    @Override
    public void configure() throws Exception {
        from(entryEndpoint)
                .choice().when().method(this, "isInitDone")
                .to(updateEndpoint)
                .otherwise()
                .bean(Utility.class, "setCustomDelay(*,1000L)")
                .delay(simple("${header.customDelay}"))
                .to(entryEndpoint);
    }

    public void entry(Object object) {
        String str = jsonUtil.getJsonString(object);
        if (str.isEmpty()) {
            System.out.println(String.format("無効なエントリーです。kind:%s object:%s", sourceKind, object));
        } else {
            upcomingJsonString = str;
            ProducerTemplate pt = this.getContext().createProducerTemplate();
            DefaultExchange exchange = new DefaultExchange(this.getContext());
            pt.send(entryEndpoint, exchange);
        }
    }

    @Override
    public void buildEndpoint() {
        super.buildEndpoint();
        entryEndpoint = String.format("direct:%s.entry", sourceKind);
        updateEndpoint = String.format("direct:%s.update", sourceKind);
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
