package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.HttpUtil;
import org.springframework.stereotype.Component;

@Component
public class KoepotaSiteSource extends SiteSource {

    @Override
    public void configure() throws Exception {
        from("timer:site.koepota.timer?period=1m")
                .to("direct:site.koepota.get");

        from("direct:site.koepota.get")
                .process(HttpUtil.getHtmlProcessor(simple("http://www.koepota.jp/eventschedule/")))
                .choice().when(isSiteChanging()).to("log:site.koepota.log.changing?showBody=false")
                .otherwise().to("log:site.koepota.log.not_changing?showBody=false");
        
    }
}
