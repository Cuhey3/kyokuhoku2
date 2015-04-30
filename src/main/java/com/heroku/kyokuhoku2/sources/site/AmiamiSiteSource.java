package com.heroku.kyokuhoku2.sources.site;

import org.springframework.stereotype.Component;

@Component
public class AmiamiSiteSource extends SiteSource {

    private AmiamiSiteSource() {
        sourceKind = "amiami";
        sourceUrl = "http://www.amiami.jp/top/page/cal/goods.html";
        periodExpression = "30m";
    }
}
