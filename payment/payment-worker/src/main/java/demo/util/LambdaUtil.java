package demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
public class LambdaUtil {

    private ObjectMapper objectMapper;

    public LambdaUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HashMap objectToMap(Object object) {
        HashMap result = null;

        try {
            result = objectMapper.readValue(objectMapper.writeValueAsString(object), HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
