package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.sources.ComputableSource;
import com.heroku.kyokuhoku2.sources.site.SeiyuCategoryMemberSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashSet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SeiyuNameSource extends ComputableSource {

    private SeiyuNameSource() {
        setSourceKind("jsonize.seiyu.name");
        getSuperiorSourceClasses().add(SeiyuCategoryMemberSiteSource.class);
        buildEndpoint();
    }

    @Override
    public void configure() throws Exception {
        from(computeEndpoint).bean(this, "wao");
        setModifiedTime(-1L);
    }

    public void wao() {
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
        setModifiedTime(System.currentTimeMillis());
    }
}
