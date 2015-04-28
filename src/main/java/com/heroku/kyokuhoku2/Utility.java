package com.heroku.kyokuhoku2;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Utility {

    public static void mapBodyToHeader(Exchange exchange, Map<String, Object> map, boolean rewrite) {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        for (Entry<String, Object> entry : map.entrySet()) {
            if (rewrite || !headers.containsKey(entry.getKey())) {
                exchange.getIn().setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    public static Processor listToMapByUniqueKey(final String key) {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                List<Map<String, Object>> list = exchange.getIn().getBody(List.class);
                Map<String, Map> result = new LinkedHashMap<>();
                for (Map<String, Object> map : list) {
                    String k = (String) map.get(key);
                    result.put(k, map);
                }
                exchange.getIn().setBody(result);
            }
        };
    }

    public static Processor mapListToListByOneField(final String key) {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                List<Map> mapList = exchange.getIn().getBody(List.class);
                ArrayList result = new ArrayList();
                for (Map map : mapList) {
                    result.add(map.get(key));
                }
                exchange.getIn().setBody(result);
            }
        };
    }

    /*public static Processor StringListToMap() {
     return new Processor() {

     @Override
     public void process(Exchange exchange) throws Exception {
     List<String> list = exchange.getIn().getBody(List.class);
     LinkedHashMap<String, Object> result = new LinkedHashMap<>();
     for (String key : list) {
     result.put(key, key);
     }
     exchange.getIn().setBody(result);
     }
     };
     }*/
}
