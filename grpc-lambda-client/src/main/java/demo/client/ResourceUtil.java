package demo.client;

import io.opentelemetry.proto.resource.v1.Resource;

public class ResourceUtil {

    private static Resource resource;

    static {
        resource = Resource.newBuilder()
                .addAttributes(AttributeUtil.createKeyValue("telemetry.sdk.language", "python"))
                .addAttributes(AttributeUtil.createKeyValue("telemetry.sdk.name", "opentelemetry"))
                .addAttributes(AttributeUtil.createKeyValue("telemetry.sdk.version", "1.16.0"))
                .addAttributes(AttributeUtil.createKeyValue("telemetry.auto.version", "0.37b0"))
                .addAttributes(AttributeUtil.createKeyValue("service.name", "unknown_service"))
                .build();
    }

    public static Resource getResource() {
        return resource;
    }
}
