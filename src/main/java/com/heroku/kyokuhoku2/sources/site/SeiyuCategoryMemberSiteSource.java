package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.HttpUtil;
import org.springframework.stereotype.Component;

@Component
public class SeiyuCategoryMemberSiteSource extends SiteSource {

    @Override
    public void configure() throws Exception {
        from("timer:site.seiyu.categorymembers.timer?period=1m")
                .process(HttpUtil.getPagesProcessor("http://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&format=xml&cmnamespace=0&rawcontinue", "categorymembers[cmcontinue]", "&cmcontinue=", "cmcontinue"))
                .choice().when(isSiteChanging()).to("log:site.seiyu.categorymembers.log.changing?showBody=false")
                .otherwise().to("log:site.seiyu.categorymembers.log.not_changing?showBody=false");
    }
}
