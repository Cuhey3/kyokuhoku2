package com.heroku.kyokuhoku2.sources.site;

import com.heroku.kyokuhoku2.sources.Source;
import java.util.Arrays;
import java.util.Objects;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class SiteSource extends Source {

    protected String html = "";
    protected String[] htmlArray = new String[]{};
    protected Integer hash = null;

    protected Predicate isSiteChanging() {
        return new Predicate() {

            @Override
            public boolean matches(Exchange exchange) {
                boolean siteChanging = false;
                Object body = exchange.getIn().getBody();
                if (body == null) {
                    notUpToDate();
                } else if (body instanceof String) {
                    String newHtml = (String) body;
                    if (newHtml.isEmpty()) {
                        notUpToDate();
                    } else {
                        Integer newHash = newHtml.hashCode();
                        if (!Objects.equals(newHash, hash())) {
                            siteChanging = hash() != null;
                            html(newHtml);
                            hash(newHash);
                            upToDate();
                        }
                    }
                } else if (body instanceof String[]) {
                    String[] newHtmlArray = (String[]) body;
                    for (String newHtml : newHtmlArray) {
                        if (newHtml == null) {
                            notUpToDate();
                            return false;
                        }
                    }
                    Integer newHash = Arrays.hashCode(newHtmlArray);
                    if (newHash != 1 && !Objects.equals(newHash, hash())) {
                        siteChanging = hash() != null;
                        htmlArray(newHtmlArray);
                        hash(newHash);
                        upToDate();
                    }
                }
                return siteChanging;
            }
        };
    }

    protected void html(String html) {
        this.html = html;
    }

    protected String html() {
        return html;
    }

    protected void htmlArray(String[] htmlArray) {
        this.htmlArray = htmlArray;
    }

    protected String[] htmlArray() {
        return htmlArray;
    }

    protected void hash(int hash) {
        this.hash = hash;
    }

    protected Integer hash() {
        return hash;
    }

    protected Document document() {
        return Jsoup.parse(this.html);
    }

    protected Document[] documentArray() {
        Document[] documentArray = new Document[htmlArray.length];
        for (int i = 0; i < htmlArray.length; i++) {
            documentArray[i] = Jsoup.parse(htmlArray[i]);
        }
        return documentArray;
    }

    @Override
    public <T extends Object> T get(Class<T> type) {
        return (T) document();
    }

    @Override
    public Object get() {
        return document();
    }
}
