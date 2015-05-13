/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heroku.kyokuhoku2;

import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HttpUtil {

    public static Processor urlEncode(final Expression fromExp, final String header) {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(header, URLEncoder.encode(fromExp.evaluate(exchange, String.class), "UTF-8"));
            }
        };
    }

    public String getHtml(final String url) {
        Document document = HttpUtil.get(url);
        if (document == null) {
            return null;
        } else {
            return document.outerHtml();
        }
    }

    public static String getJson(String url) {
        int retry = 0;
        Connection.Response response = null;
        while (response == null && retry < 10) {
            try {
                response = Jsoup.connect(url).ignoreContentType(true).method(Connection.Method.GET).execute();
            } catch (Throwable t) {
                retry++;
            }
        }
        if (response != null) {
            return response.body();
        } else {
            return "{}";
        }
    }

    public static String[] getPages(final String url, final String query, final String param, final String attr) {
        Document doc = HttpUtil.get(url);
        ArrayList<String> pages = new ArrayList<>();
        while (true && doc != null) {
            pages.add(doc.outerHtml());
            Elements select = doc.select(query);
            if (select.isEmpty()) {
                break;
            } else {
                String cmcontinue = select.get(0).attr(attr);
                doc = HttpUtil.get(url + param + cmcontinue);
            }
        }
        return pages.toArray(new String[pages.size()]);
    }

    public static Document get(String url) {
        Document doc = null;
        int retry = 0;
        while (doc == null && retry < 10) {
            try {
                System.out.println(url);
                doc = Jsoup.connect(url).maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE).get();
            } catch (Throwable t) {
                retry++;
            }
        }
        return doc;
    }

    public static byte[] getBytes(String url) {
        Connection.Response response = null;
        int retry = 0;
        while (response == null & retry < 10) {
            try {
                response = Jsoup.connect(url).maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE).ignoreContentType(true).execute();
            } catch (Throwable t) {
                retry++;
            }
        }
        if (response.bodyAsBytes().length != 0) {
            return response.bodyAsBytes();
        } else {
            return new byte[]{};
        }
    }
}
