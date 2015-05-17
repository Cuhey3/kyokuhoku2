package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.sources.site.SeiyuCategoryMemberSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashSet;
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
    public Object compute() {
        SiteSource source = getFactory().getBean(SeiyuCategoryMemberSiteSource.class);
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Document doc : source.getDocumentArray()) {
            Elements els = doc.select("categorymembers cm[title]");
            for (Element e : els) {
                names.add(e.attr("title"));
            }
        }
        for (String s : names) {
            System.out.println(s);
        }
        setUpdateTime(System.currentTimeMillis());
        setCheckForUpdateTime(System.currentTimeMillis());
        return null;
    }
}
