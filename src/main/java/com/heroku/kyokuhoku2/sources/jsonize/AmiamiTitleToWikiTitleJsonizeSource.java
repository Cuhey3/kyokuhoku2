package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.sources.JsonizeSource;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AmiamiTitleToWikiTitleJsonizeSource extends JsonizeSource {
    
    private AmiamiTitleToWikiTitleJsonizeSource() {
        setSourceKind("jsonize.amiami.title.to.wikititle");
        getSuperiorSourceClasses().add(AmiamiTitleJsonizeSource.class);
        setCheckForUpdateTime(-1L);
        buildEndpoint();
    }
    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        from(computeImplEndpoint)
                .bean(this, "compute()")
                .to(entryJsonEndpoint);
    }
    
    @Override
    public Object compute() {
        JsonizeSource source = getFactory().getBean(AmiamiTitleJsonizeSource.class);
        for (Object map : source.getDiffList()) {
            for (Map<String, String> entry : ((List<Map<String, String>>) ((Map) map).get("diff"))) {
                if (entry.get("op").equals("add")) {
                    System.out.println(entry.get("value"));
                }
            }
        }
        return null;
    }
    
}
