package com.heroku.kyokuhoku2.sources.site;

import org.springframework.stereotype.Component;

@Component
public class KoepotaSiteSource extends SiteSource {

    private KoepotaSiteSource() {
        sourceKind = "koepota";
        sourceUrl = "http://www.koepota.jp/eventschedule/";
        periodExpression = "30m";
    }
}
