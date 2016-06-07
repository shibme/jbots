package me.shib.java.lib.jbots;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonLib {

    private Gson gson;
    private Gson prettyGson;
    private Gson upperCamelCaseGson;

    public JsonLib() {
        gson = new Gson();
    }

    private Gson getPrettyGson() {
        if (null == prettyGson) {
            prettyGson = new GsonBuilder().setPrettyPrinting().create();
        }
        return prettyGson;
    }

    private Gson getUpperCamelCaseGson() {
        if (null == upperCamelCaseGson) {
            upperCamelCaseGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        }
        return upperCamelCaseGson;
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public <T> T fromUpperCamelCaseJson(String json, Class<T> classOfT) {
        return getUpperCamelCaseGson().fromJson(json, classOfT);
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public String toPrettyJson(Object object) {
        return getPrettyGson().toJson(object);
    }

}