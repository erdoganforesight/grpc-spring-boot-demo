package demo.client;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static demo.client.RequestUtil.createExportTraceServiceRequest;


public class DataSender  {
    private final ExecutorService executor;
    private final ExecutorService offloadExecutor;
    private ConcurrentLinkedQueue<Future> FUTURE_QUEUE = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Exception> EXCEPTION_QUEUE = new ConcurrentLinkedQueue<>();
    private TraceServiceGrpc.TraceServiceFutureStub client;
    private ManagedChannel channel;
    private String grpcServerAddress;
    private int requestCount;

    public DataSender(String grpcServerAddress, int requestCount) {
        this.grpcServerAddress = grpcServerAddress;
        this.requestCount = requestCount;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        offloadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);;
        this.channel = ManagedChannelBuilder
                .forTarget(grpcServerAddress)
                .keepAliveTime(1, TimeUnit.MINUTES)
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .offloadExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .usePlaintext()
                .build();
        this.client = TraceServiceGrpc.newFutureStub(this.channel);
    }

    public void export() {
        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < this.requestCount; i++) {
                FUTURE_QUEUE.add(client.export(createExportTraceServiceRequest()));
            }
            checkFutures();
            if (EXCEPTION_QUEUE.size() > 0){
                System.out.println("TotalDurationError:" + (System.currentTimeMillis() - start) + " error count:"+ EXCEPTION_QUEUE.size());
            } else {
                System.out.println("TotalDuration:" + (System.currentTimeMillis() - start));
            }
            EXCEPTION_QUEUE.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkFutures() {
        while (true) {
            try {
                Future future = FUTURE_QUEUE.poll();
                if (future != null) {
                    future.get();
                } else {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                EXCEPTION_QUEUE.add(e);
            }
        }
    }

    public void close() {
        this.executor.shutdownNow();
        this.offloadExecutor.shutdownNow();
        this.channel.shutdownNow();
    }
}
