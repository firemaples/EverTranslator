package tw.firemaples.onscreenocr.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class JsonUtil<T> {

    private static Gson gson = null;
    private static GsonBuilder gsonBuilder = null;

    public static final ThreadLocal<Object> sThreadLocalReadObject = new ThreadLocal<>();

    // this helper method can be used to make JSON parsing a one-line operation

    public T parseJson(String str, Class<T> clazz) {
        try {
            sThreadLocalReadObject.set(clazz);
            return defaultGson().fromJson(str, clazz);
        } catch (Exception e) {
            Tool.logError("Failed to parse JSON entity " + clazz.getSimpleName(), e);
            throw new RuntimeException(e);
        } finally {
            sThreadLocalReadObject.set(null);
        }
    }

    public <R> R parseJson(JsonObject json, Class<R> clazz) {
        try {
            sThreadLocalReadObject.set(clazz);
            return defaultGson().fromJson(json, clazz);
        } catch (Exception e) {
            Tool.logError("Failed to parse JSON entity " + clazz.getSimpleName(), e);
            throw new RuntimeException(e);
        } finally {
            sThreadLocalReadObject.set(null);
        }
    }

    public T parseJson(String str, TypeReference<T> typeRef) {
        if (str == null) {
            return null;
        }
        if (String.class.equals(typeRef.getType())) {
            return (T) str;
        }
        try {
            sThreadLocalReadObject.set(typeRef);
            return defaultGson().fromJson(str, typeRef.getType());
        } catch (Exception e) {
            Tool.logError("Failed to parse JSON entity " + typeRef, e);
            throw new RuntimeException(e);
        } finally {
            sThreadLocalReadObject.set(null);
        }
    }

    // this helper method can be used to make string encoded to JSON
    public String writeJson(Object obj) {
        try {
            return defaultGson().toJson(obj);
        } catch (Exception e) {
            Tool.logError("Failed to encode JSON entity", e);
            throw new RuntimeException(e);
        }
    }

    private Gson defaultGson() {
        if (gson == null) {
            gson = defaultGsonBuilder().create();
        }
        return gson;
    }

    private GsonBuilder defaultGsonBuilder() {
        if (gsonBuilder != null) {
            return gsonBuilder;
        }
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new BooleanTypeAdapterFactory());
        return gsonBuilder;
    }

    public void registerTypeAdapter(Class<T> type, Object deser) {
        defaultGsonBuilder().registerTypeAdapter(type, deser);
    }


    public class BooleanTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            Class<T> rawType = (Class<T>) type.getRawType();
            if (rawType.equals(boolean.class) || rawType.equals(Boolean.class)) {
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        delegate.write(out, value);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        } else if (in.peek() == JsonToken.STRING) {
                            String s = in.nextString();
                            if (s.equalsIgnoreCase("true") || s.equals("1")) {
                                return (T) Boolean.valueOf(true);
                            } else if (s.equalsIgnoreCase("false") || s.equals("0")) {
                                return (T) Boolean.valueOf(false);
                            }
                        } else if (in.peek() == JsonToken.NUMBER) {
                            int i = in.nextInt();
                            if (i == 1) {
                                return (T) Boolean.valueOf(true);
                            } else if (i == 0) {
                                return (T) Boolean.valueOf(false);
                            }
                        }
                        return delegate.read(in);
                    }
                };
            } else {
                return delegate;
            }
        }
    }
}
