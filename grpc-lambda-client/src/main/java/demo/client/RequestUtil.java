package demo.client;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;

public class RequestUtil {

    public static ExportTraceServiceRequest createExportTraceServiceRequest() {
        ResourceSpans resourceSpans = ResourceSpans.newBuilder()
                .setResource(ResourceUtil.getResource())
                .addAllInstrumentationLibrarySpans(SpanUtil.createInstrumentationLibrarySpans(System.currentTimeMillis()))
                .build();

        return ExportTraceServiceRequest.newBuilder()
                .addResourceSpans(resourceSpans)
                .build();
    }
}
