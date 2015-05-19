package com.heroku.kyokuhoku2.sources;

import com.heroku.kyokuhoku2.JsonUtil;
import java.util.List;
import lombok.Setter;
import org.apache.camel.Body;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

public abstract class JsonizeSource extends ComputableSource {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    @Getter
    protected List diffList;
    @Setter
    protected int diffSize = 20;
    public String jsonDiffEndpoint;
    public String entryJsonEndpoint;

    @Override
    public void buildEndpoint() {
        super.buildEndpoint();
        entryJsonEndpoint = format("direct:%s.entryJson", sourceKind);
        jsonDiffEndpoint = format("direct:%s.jsonDiff", sourceKind);
    }

    @Override
    public void configure() throws Exception {
        super.configure();

        from(initImplEndpoint)
                .bean(this, "initImpl()");

        from(entryJsonEndpoint)
                .filter().simple("${body} != null")
                .bean(this, "convertToJson")
                .filter().simple("${body} != null")
                .filter().method(this, "jsonCompare")
                .to(jsonDiffEndpoint)
                .end()
                .bean(this, "checkedForUpdate");

        from(jsonDiffEndpoint)
                .bean(this, "jsonDiff");
    }

    public String convertToJson(@Body Object body) {
        String str = jsonUtil.getJsonString(body);
        if (str.isEmpty()) {
            System.out.println(String.format("無効なエントリーです。kind:%s object:%s", sourceKind, body));
            return null;
        } else {
            return str;
        }
    }

    public boolean jsonCompare(@Body String body) {
        return !body.equals(jsonString);
    }

    public void jsonDiff(@Body String body) throws IOException {
        String diff = jsonUtil.getJsonDiff(body, jsonString);
        if (!diff.equals("[]")) {
            System.out.println(diff);
            pushJsonDiff(diff);
            updated();
        }
    }

    public void pushJsonDiff(String diffString) throws IOException {
        Map map = new LinkedHashMap<>();
        map.put("time", System.currentTimeMillis());
        map.put("diff", jsonUtil.unmarshal(diffString, List.class));
        diffList.add(map);
        while (diffList.size() > diffSize) {
            diffList.remove(0);
        }
    }

    public void initImpl() {
        jsonString = "{}";
        diffList = new ArrayList<>();
    }
}
