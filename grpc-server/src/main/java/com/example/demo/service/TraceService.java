package com.example.demo.service;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class TraceService extends TraceServiceGrpc.TraceServiceImplBase {

    AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        int count = counter.getAndIncrement();
        if (count % 100 == 0) {
            System.out.println(count + " th sample: " + request);
        }
        ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder()
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
