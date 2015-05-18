package com.heroku.kyokuhoku2.sources;

import com.heroku.kyokuhoku2.JsonUtil;
import java.util.List;
import lombok.Setter;
import org.apache.camel.Body;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;

public abstract class JsonizeSource extends ComputableSource {

    @Autowired
    JsonUtil jsonUtil;
    protected String jsonString;
    protected List jsonDiffList;
    @Setter
    protected int jsonDiffSize = 20;
    public String jsonDiffEndpoint;
    public String entryJsonEndpoint;

    @Override
    public void configure() throws Exception {
        super.configure();

        from(initImplEndpoint)
                .bean(this, "initImpl()");

        from(entryJsonEndpoint)
                .bean(this, "jsonConvert")
                .filter().simple("${body} != null")
                .filter().method(this, "jsonCompare")
                .to(jsonDiffEndpoint)
                .bean(this, "checkedForUpdate");

        from(jsonDiffEndpoint)
                .bean(this, "jsonDiff");
    }

    public String jsonConvert(@Body Object body) {
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
            pushDiff(diff);
            updated();
        }
    }

    @Override

    public void buildEndpoint() {
        super.buildEndpoint();
        entryJsonEndpoint = format("direct:%s.entryJson", sourceKind);
        jsonDiffEndpoint = format("direct:%s.jsonDiff", sourceKind);
    }

    public void pushDiff(String diffString) throws IOException {
        jsonDiffList.add(jsonUtil.unmarshal(diffString, List.class));
        while (jsonDiffList.size() > jsonDiffSize) {
            jsonDiffList.remove(0);
        }
    }

    public void initImpl() {
        jsonString = "{}";
        jsonDiffList = new ArrayList<>();
    }
}
