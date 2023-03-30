package com.example.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class TraceService extends TraceServiceGrpc.TraceServiceImplBase {

    AtomicInteger counter = new AtomicInteger(0);

    // Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        int count = counter.getAndIncrement();
        // if (count % 1000 == 0) System.out.println("count::" + count);
        // System.out.println(request);
        /*executor.execute(() -> {
            try {
                System.out.println(count + " th sample: " + request);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });*/

        ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder()
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
