package app.simple.inure.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

import app.simple.inure.wrapper.ApiResponse;

public class JsonParserUtil {
    
    private static final Gson gson = new Gson();
    
    public static <T> T parseSingleAttributes(JSONObject jsonObject, Class <T> clazz) {
        String jsonString = jsonObject.toString();
        
        Type type = TypeToken.getParameterized(ApiResponse.class, clazz).getType();
        ApiResponse <T> response = gson.fromJson(jsonString, type);
        
        if (response != null && response.getData() != null) {
            return response.getData().getAttributes();
        } else {
            throw new NullPointerException("Response or Data is null");
        }
    }
}
