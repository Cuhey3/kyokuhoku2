package com.heroku.kyokuhoku2.sources.site;

import org.springframework.stereotype.Component;

@Component
public class SeiyuCategoryMemberSiteSource extends SiteSource {

    private SeiyuCategoryMemberSiteSource() {
        setSourceKind("site.wiki.seiyu.categorymember");
        setSourceUrl("http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0&rawcontinue");
        setPeriodExpression("30m");
        setSinglePage(false);
        setContinueQuery("categorymembers[cmcontinue]");
        setContinueKey("&cmcontinue=");
        setContinueValue("cmcontinue");
        buildEndpoint();
    }
}
