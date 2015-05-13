package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.HttpUtil;
import com.heroku.kyokuhoku2.Utility;
import com.heroku.kyokuhoku2.sources.Source;
import java.util.Arrays;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.camel.Body;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static java.lang.String.format;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class SiteSource extends Source {

    private boolean ready = false;
    private String html = "";
    private String[] htmlArray = new String[]{};
    private Integer hash = null;
    private String sourceUrl, continueQuery, continueKey, continueValue;
    private String periodExpression = "1m";
    private boolean singlePage = true;
    private String getEndpoint, retryEndpoint, timerEndpoint;

    @Override
    public void buildEndpoint() {
        getEndpoint = format("seda:%s.get", sourceKind);
        retryEndpoint = format("seda:%s.retry", sourceKind);
        timerEndpoint = format("timer:%s.timer?period=%s", sourceKind, periodExpression);
    }

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
                .choice().when().method(this, "validateUpcomingSiteSource")
                .bean(this, "update")
                .otherwise()
                .to(retryEndpoint);

        from(retryEndpoint)
                .bean(Utility.class, "setCustomDelay(*,1000L)")
                .delay(simple("${header.customDelay}"))
                .to(getEndpoint);
    }

    public void update(@Body String newHtml) {
        Integer newHash = newHtml.hashCode();
        if (getHash() == null || !newHash.equals(getHash())) {
            if (getHash() == null) {
                setReady(true);
                System.out.println(sourceKind + " is up-to-date.");
            } else {
                setModifiedTime(System.currentTimeMillis());
                System.out.println(sourceKind + " is updated.");
            }
            setHtml(newHtml);
            setHash(newHash);

        }
    }

    public void update(@Body String[] newHtmlArray) {
        Integer newHash = Arrays.hashCode(newHtmlArray);
        if (getHash() == null || !newHash.equals(getHash())) {
            if (getHash() == null) {
                setReady(true);
            } else {
                setModifiedTime(System.currentTimeMillis());
            }
            setHtmlArray(newHtmlArray);
            setHash(newHash);
        }
    }

    public boolean validateUpcomingSiteSource(@Body Object body) {
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
                if (newHtml == null || newHtml.isEmpty()) {
                    validate = false;
                    break;
                }
            }
        }
        return validate;
    }

    public Document getDocument(String html) {
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

    @Override
    public boolean isUpToDate() {
        return ready;
    }
}
