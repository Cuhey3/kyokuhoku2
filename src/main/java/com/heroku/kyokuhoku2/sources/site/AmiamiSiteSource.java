package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.HttpUtil;
import org.springframework.stereotype.Component;

@Component
public class AmiamiSiteSource extends SiteSource {

    @Override
    public void configure() throws Exception {
        from("timer:site.amiami.timer?period=1m")
                .process(HttpUtil.getHtmlProcessor(simple("http://www.amiami.jp/top/page/cal/goods.html")))
                .choice().when(isSiteChanging()).to("log:site.amiami.log.changing?showBody=false")
                .otherwise().to("log:site.amiami.log.not_changing?showBody=false");
    }
}
