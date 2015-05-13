package com.heroku.kyokuhoku2.sources.site;

import org.springframework.stereotype.Component;

@Component
public class KoepotaSiteSource extends SiteSource {

    private KoepotaSiteSource() {
        setSourceKind("site.koepota");
        setSourceUrl("http://www.koepota.jp/eventschedule/");
        setPeriodExpression("30m");
        buildEndpoint();
    }
}
