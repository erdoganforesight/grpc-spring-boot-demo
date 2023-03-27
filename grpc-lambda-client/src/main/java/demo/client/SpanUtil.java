package demo.client;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SpanUtil {
    public static final Random RANDOM = new Random();
    public static final int SPAN_DURATION = 10;
    public static final int WAIT_DURATION = 5;
    public static final int CHILD_SPAN_COUNT = 5;

    public static List<InstrumentationLibrarySpans> createInstrumentationLibrarySpans(long start) {
        Long traceId = createRandom();
        Long parentSpanId = createRandom();
        long finish = start + (CHILD_SPAN_COUNT * (SPAN_DURATION + WAIT_DURATION)) + SPAN_DURATION;
        InstrumentationLibrarySpans rootSpan = createFlaskSpan(traceId, parentSpanId, start, finish);
        InstrumentationLibrarySpans childSpan = createMongoSpan(traceId, parentSpanId, start + WAIT_DURATION);
        return Arrays.asList(rootSpan, childSpan);
    }

    private static InstrumentationLibrarySpans createFlaskSpan(Long traceId, Long spanId, long start, long finish) {
        Span span = createSpan(traceId, null, spanId, start, finish, "/rolldice",
                Span.SpanKind.SPAN_KIND_SERVER, AttributeUtil.createFlaskAttributes());

        return InstrumentationLibrarySpans.newBuilder()
                .setInstrumentationLibrary(InstrumentationLibrary.newBuilder()
                        .setName("opentelemetry.instrumentation.flask").setVersion("0.37b0")
                        .build())
                .addSpans(span)
                .build();
    }

    private static InstrumentationLibrarySpans createMongoSpan(Long traceId, Long parentSpanId, long start) {
        List<Span> spans = IntStream.range(0, CHILD_SPAN_COUNT).mapToObj(i -> {
            return createSpan(traceId, parentSpanId, createRandom(),
                    start + (SPAN_DURATION * i),
                    start + (SPAN_DURATION * i) + WAIT_DURATION,
                    "test.find", Span.SpanKind.SPAN_KIND_CLIENT, AttributeUtil.createMongoAttributes());
        }).collect(Collectors.toList());

        return InstrumentationLibrarySpans.newBuilder()
                .setInstrumentationLibrary(InstrumentationLibrary.newBuilder()
                        .setName("opentelemetry.instrumentation.pymongo").setVersion("0.37b0")
                        .build())
                .addAllSpans(spans)
                .build();
    }

    private static Span createSpan(Long traceId, Long parentSpanId, Long spanId, long start, long finish, String name,
                                   Span.SpanKind spanKind, List<KeyValue> attributes) {
        Span.Builder spanBuilder = Span.newBuilder()
                .setTraceId(ByteString.copyFromUtf8(traceId.toString()))
                .setSpanId(ByteString.copyFromUtf8(spanId.toString()))
                .setName(name)
                .setKind(spanKind)
                .setStartTimeUnixNano(start)
                .setEndTimeUnixNano(finish)
                .setStatus(Status.newBuilder().buildPartial())
                .addAllAttributes(attributes);
        Optional.ofNullable(parentSpanId)
                .ifPresent(id -> spanBuilder.setParentSpanId(ByteString.copyFromUtf8(id.toString())));
        return spanBuilder.build();
    }

    private static Long createRandom() {
        return Math.abs(RANDOM.nextLong());
    }
}
