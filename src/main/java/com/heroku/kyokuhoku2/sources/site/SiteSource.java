package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.HttpUtil;
import com.heroku.kyokuhoku2.actions.ActionBroker;
import com.heroku.kyokuhoku2.sources.Source;
import java.util.Arrays;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static java.lang.String.format;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class SiteSource extends Source {

    private String html = "";
    private String[] htmlArray = new String[]{};
    private Integer hash = null;
    private String sourceKind, sourceUrl, continueQuery, continueKey, continueValue;
    private String periodExpression = "1m";
    private boolean singlePage = true;
    private String getEndpoint, retryEndpoint, changingEndpoint, timerEndpoint;

    @Override
    public void configure() throws Exception {
        from(timerEndpoint)
                .to(getEndpoint);

        from(getEndpoint)
                .choice().when().method(this, "isSinglePage")
                .bean(HttpUtil.class, format("getHtml(%s)", sourceUrl))
                .otherwise()
                .bean(HttpUtil.class, format("getPages(%s,%s,%s,%s)", sourceUrl, continueQuery, continueKey, continueValue))
                .end()
                .choice().when().method(this, "validateUpcomingSiteSource(${body},false)")
                .to(retryEndpoint)
                .otherwise()
                .filter().method(this, "isSiteChanging")
                .to(changingEndpoint);

        from(retryEndpoint)
                .bean(this, "setDelay")
                .delay(simple("${header.myDelay}"))
                .filter().method(this, "isNotUpToDate")
                .to(getEndpoint);

        from(changingEndpoint)
                .toF("log:site.%s.log.changing?showBody=false", sourceKind)
                .bean(this, "turnOtherSourceToNotUpToDate")
                .bean(this, "getOnChangeActionClasses")
                .to(ActionBroker.ENTRY_ENDPOINT);
    }

    public boolean isSiteChanging(@Body Object body) {
        boolean siteChanging = false;
        if (body instanceof String) {
            String newHtml = (String) body;
            Integer newHash = newHtml.hashCode();
            if (!Objects.equals(newHash, getHash())) {
                siteChanging = getHash() != null;
                setHtml(newHtml);
                setHash(newHash);
            }
        } else if (body instanceof String[]) {
            String[] newHtmlArray = (String[]) body;
            Integer newHash = Arrays.hashCode(newHtmlArray);
            if (newHash != 1 && !Objects.equals(newHash, getHash())) {
                siteChanging = getHash() != null;
                setHtmlArray(newHtmlArray);
                setHash(newHash);
            }
        }
        return siteChanging;
    }

    public Document getDocument() {
        return Jsoup.parse(this.html);
    }

    public Document[] getDocumentArray() {
        int len = htmlArray.length;
        Document[] documentArray = new Document[len];
        for (int i = 0; i < len; i++) {
            documentArray[i] = Jsoup.parse(htmlArray[i]);
        }
        return documentArray;
    }

    public void setDelay(@Header(value = "myDelay") Long delay) {
        if (delay == null) {
            delay = 1000L;
        }
        delay *= 2;
    }

    public boolean validateUpcomingSiteSource(@Body Object body, final boolean flag) {
        boolean validate = true;
        if (body == null) {
            validate = false;
        } else if (body instanceof String) {
            String newHtml = (String) body;
            if (newHtml.isEmpty()) {
                validate = false;
            }
        } else if (body instanceof String[]) {
            String[] newHtmlArray = (String[]) body;
            for (String newHtml : newHtmlArray) {
                if (newHtml == null) {
                    validate = false;
                    break;
                }
            }
        }
        if (validate) {
            setUpToDate(true);
        } else {
            setUpToDate(false);
        }
        return validate == flag;
    }

    @Override
    public void buildEndpoint() {
        getEndpoint = format("seda:site.%s.get", sourceKind);
        retryEndpoint = format("seda:site.%s.retry", sourceKind);
        changingEndpoint = format("seda:site.%s.changing", sourceKind);
        timerEndpoint = format("timer:site.%s.timer?period=%s", sourceKind, periodExpression);
    }
}
