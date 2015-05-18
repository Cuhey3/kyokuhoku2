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

    private String html = "";
    private String[] htmlArray = new String[]{};
    private Integer hash = null;
    private String sourceUrl, continueQuery, continueKey, continueValue;
    private String periodExpression = "1m";
    private boolean singlePage = true;
    private String getEndpoint, validateEndpoint, timerEndpoint, compareEndpoint;

    @Override
    public void buildEndpoint() {
        super.buildEndpoint();
        timerEndpoint = format("timer:%s.poll?period=%s", sourceKind, periodExpression);
        getEndpoint = format("direct:%s.get", sourceKind);
        validateEndpoint = format("direct:%s.validate", sourceKind);
        compareEndpoint = format("direct:%s.compare", sourceKind);
    }

    @Override
    public void configure() throws Exception {
        from(initEndpoint)
                .to(initImplEndpoint)
                .bean(this, "ready()");

        from(initImplEndpoint)
                .to(getEndpoint);

        from(timerEndpoint)
                .filter().method(this, "isReady()")
                .to(getEndpoint);

        from(getEndpoint)
                .choice().when().method(this, "isSinglePage")
                .bean(HttpUtil.class, format("getHtml(%s)", sourceUrl))
                .otherwise()
                .bean(HttpUtil.class, format("getPages(%s,%s,%s,%s)", sourceUrl, continueQuery, continueKey, continueValue))
                .end()
                .to(validateEndpoint);

        from(validateEndpoint)
                .choice().when().method(this, "validate")
                .to(compareEndpoint)
                .otherwise()
                .bean(Utility.class, "setCustomDelay(*,1000L)")
                .delay(simple("${header.customDelay}"))
                .to(getEndpoint);

        from(compareEndpoint)
                .filter().method(this, "compare")
                .bean(this, "updated")
                .end()
                .bean(this, "checkedForUpdate");
    }

    public boolean compare(@Body Object body) {
        Integer newHash;
        Integer oldHash = getHash();
        if (body instanceof String) {
            String newHtml = ((String) body);
            newHash = newHtml.hashCode();
            setHtml(newHtml);
        } else if (body instanceof String[]) {
            String[] newHtmlArray = ((String[]) body);
            newHash = Arrays.hashCode(newHtmlArray);
            setHtmlArray(newHtmlArray);
        } else {
            return false;
        }
        setHash(newHash);
        return !(oldHash == null || newHash.equals(oldHash));
    }

    public boolean validate(@Body Object body) {
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
