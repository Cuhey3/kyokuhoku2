package com.heroku.kyokuhoku2.sources.site;

import org.springframework.stereotype.Component;

@Component
public class AmiamiSiteSource extends SiteSource {

    private AmiamiSiteSource() {
        setSourceKind("amiami");
        setSourceUrl("http://www.amiami.jp/top/page/cal/goods.html");
        setPeriodExpression("30m");
        buildEndpoint();
    }
}
