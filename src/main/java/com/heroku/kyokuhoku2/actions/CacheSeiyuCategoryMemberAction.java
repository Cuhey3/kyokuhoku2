package com.heroku.kyokuhoku2.actions;

import com.heroku.kyokuhoku2.sources.site.SeiyuCategoryMemberSiteSource;
import com.heroku.kyokuhoku2.sources.site.SiteSource;
import java.util.LinkedHashSet;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class CacheSeiyuCategoryMemberAction extends Action {

    private CacheSeiyuCategoryMemberAction() {
        defaultActionEndpoint("direct:cache.seiyu.categorymember");
        addSourceClass(SeiyuCategoryMemberSiteSource.class);
    }

    @Override
    public void configure() throws Exception {
        from(defaultActionEndpoint())
                .process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        SiteSource source = factory().getBean(SeiyuCategoryMemberSiteSource.class);
                        LinkedHashSet<String> names = new LinkedHashSet<>();
                        for (Document doc : source.documentArray()) {
                            Elements els = doc.select("categorymembers cm[title]");
                            for (Element e : els) {
                                names.add(e.attr("title"));
                            }
                        }
                        for (String s : names) {
                            System.out.println(s);
                        }
                    }
                })
                .setHeader("doneAction").constant(true);
    }
}
