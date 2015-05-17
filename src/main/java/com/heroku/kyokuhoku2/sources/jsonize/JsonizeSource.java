package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.JsonUtil;
import com.heroku.kyokuhoku2.sources.ComputableSource;
import java.util.List;
import org.apache.camel.Body;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class JsonizeSource extends ComputableSource {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    protected Object jsonObject;
    protected String diffString;
    protected List diffList;
    protected String upcomingJsonString;

    @Override
    public void configure() throws Exception {
        from(jsonEntryEndpoint)
                .filter().method(this, "jsonValidate");
    }

    public void jsonValidate(@Body Object object) {
        String str = jsonUtil.getJsonString(object);
        if (str.isEmpty()) {
            System.out.println(String.format("無効なエントリーです。kind:%s object:%s", sourceKind, object));
        } else {
            upcomingJsonString = str;
        }
    }

    @Override
    public void buildEndpoint() {
        super.buildEndpoint();
        jsonEntryEndpoint = String.format("direct:%s.entry", sourceKind);
    }
}
