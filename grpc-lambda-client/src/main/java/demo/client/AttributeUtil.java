package demo.client;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributeUtil {
    public static Map<String, Object> flaskAttributes = new HashMap<>();
    public static Map<String, Object> mongoAttributes = new HashMap<>();

    static {
        flaskAttributes.put("http.method", "GET");
        flaskAttributes.put("http.server_name", "127.0.0.1");
        flaskAttributes.put("http.scheme", "http");
        flaskAttributes.put("net.host.port", 8090);
        flaskAttributes.put("http.host", "127.0.0.1:8090");
        flaskAttributes.put("http.target", "/rolldice");
        flaskAttributes.put("net.peer.ip", "127.0.0.1");
        flaskAttributes.put("http.user_agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        flaskAttributes.put("net.peer.port", 53446);
        flaskAttributes.put("http.flavor", "1.1");
        flaskAttributes.put("http.route", "/rolldice");
        flaskAttributes.put("http.status_code", 200);

        mongoAttributes.put("db.system", "mongodb");
        mongoAttributes.put("db.name", "admin");
        mongoAttributes.put("db.statement", "find");
        mongoAttributes.put("net.peer.name", "cluster0-shard-00-01.m8dpz.mongodb.net");
        mongoAttributes.put("net.peer.port", 27017);
    }
    
    public static List<KeyValue> createMongoStartAttributes() {
        List<KeyValue> attributes = createMongoAttributes();
        attributes.add(createKeyValue("db.statement", "saslStart"));
        return attributes;
    }

    public static List<KeyValue> createMongoAttributes() {
        return mongoAttributes.entrySet().stream()
                .map(e -> createKeyValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static List<KeyValue> createFlaskAttributes() {
        return flaskAttributes.entrySet().stream()
                .map(e -> createKeyValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static KeyValue createKeyValue(String key, Object value) {
        if (value instanceof Integer) {
            return KeyValue.newBuilder()
                    .setKey(key)
                    .setValue(AnyValue.newBuilder().setIntValue((Integer)value).build())
                    .build();
        } else {
            return KeyValue.newBuilder()
                    .setKey(key)
                    .setValue(AnyValue.newBuilder().setStringValue((String)value).build())
                    .build();
        }
    }

}
