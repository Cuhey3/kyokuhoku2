package com.heroku.kyokuhoku2.actions;

import com.heroku.kyokuhoku2.sources.site.SeiyuCategoryMemberSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashSet;
import java.util.Map;
import org.apache.camel.Headers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class CacheSeiyuCategoryMemberAction extends Action {

    private CacheSeiyuCategoryMemberAction() {
        setDefaultActionEndpoint("direct:cache.seiyu.categorymember");
        addSourceClass(SeiyuCategoryMemberSiteSource.class);
    }

    @Override
    public void configure() throws Exception {
        from(getDefaultActionEndpoint())
                .bean(this, "action");
    }

    public void action(@Headers Map headers) {
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
        headers.put("doneAction", true);
    }
}
