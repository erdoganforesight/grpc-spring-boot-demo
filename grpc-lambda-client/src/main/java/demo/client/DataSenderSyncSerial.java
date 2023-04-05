package demo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static demo.client.RequestUtil.createExportTraceServiceRequest;

public class DataSenderSyncSerial {
    private final ExecutorService executor;
    private ManagedChannel channel;
    private long executionDuration;
    private int parallelizationFactor;
    private AtomicLong counter = new AtomicLong();

    public DataSenderSyncSerial(String grpcServerAddress, int parallelizationFactor, long executionDuration) {
        this.executionDuration = executionDuration;
        this.parallelizationFactor = parallelizationFactor;
        this.executor = Executors.newFixedThreadPool(parallelizationFactor);
            channel =ManagedChannelBuilder
                    .forTarget(grpcServerAddress)
                    .keepAliveTime(1, TimeUnit.MINUTES)
                    .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                    .offloadExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                    .build();
    }

    public void export() {
        System.out.println("######################################################");
        long endTime = System.currentTimeMillis() + executionDuration;
        System.out.println("parallelizationFactor:" + parallelizationFactor + " executionDuration:" + executionDuration);
        try {
            var client = TraceServiceGrpc.newBlockingStub(channel);
            System.out.println("endTime:" + endTime);
            for (; System.currentTimeMillis() < endTime;) {
                client.export(createExportTraceServiceRequest());
                counter.incrementAndGet();
            }
            System.out.println("TotalMessageCount:" + counter.get());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
