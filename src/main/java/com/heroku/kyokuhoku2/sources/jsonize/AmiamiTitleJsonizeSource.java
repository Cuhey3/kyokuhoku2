package com.heroku.kyokuhoku2.sources.jsonize;

import com.heroku.kyokuhoku2.sources.JsonizeSource;
import com.heroku.kyokuhoku2.sources.site.AmiamiSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class AmiamiTitleJsonizeSource extends JsonizeSource {

    private AmiamiTitleJsonizeSource() {
        setSourceKind("jsonize.amiami.title");
        getSuperiorSourceClasses().add(AmiamiSiteSource.class);
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
        SiteSource source = getFactory().getBean(AmiamiSiteSource.class);
        LinkedHashMap<String, String> amiamiTitle = new LinkedHashMap<>();
        Document doc = source.getDocument();
        doc.select(".listitem:has(.originaltitle:matches(^$))").remove();
        Elements el = doc.select(".listitem");
        for (Element e : el) {
            String title = e.select(".originaltitle").text();
            amiamiTitle.put(title, title);
        }
        return amiamiTitle;
    }
}
