package com.heroku.kyokuhoku2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class JsonUtil {

    @Autowired
    ObjectMapper mapper;

    public <T extends Object> T unmarshal(String jsonString, Class<T> type) throws IOException {
        T readValue = mapper.readValue(jsonString, type);
        return readValue;
    }

    public String getJsonString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException t) {
            return "";
        }
    }
}
