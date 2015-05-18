package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.sources.JsonizeSource;
import com.heroku.kyokuhoku2.sources.site.SeiyuCategoryMemberSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SeiyuNameJsonizeSource extends JsonizeSource {

    private SeiyuNameJsonizeSource() {
        setSourceKind("jsonize.seiyu.name");
        getSuperiorSourceClasses().add(SeiyuCategoryMemberSiteSource.class);
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
        SiteSource source = getFactory().getBean(SeiyuCategoryMemberSiteSource.class);
        LinkedHashMap<String, String> names = new LinkedHashMap<>();
        for (Document doc : source.getDocumentArray()) {
            Elements els = doc.select("categorymembers cm[title]");
            for (Element e : els) {
                names.put(e.attr("title"), e.attr("title"));
            }
        }
        return names;
    }
}
