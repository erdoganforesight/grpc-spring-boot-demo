package demo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static demo.client.RequestUtil.createExportTraceServiceRequest;

public class DataSenderSync {
    private final ExecutorService executor;
    private List<ManagedChannel> channelList = new ArrayList<>();
    private long executionDuration;
    private int parallelizationFactor;
    private AtomicLong counter = new AtomicLong();

    public DataSenderSync(String grpcServerAddress, int parallelizationFactor, long executionDuration) {
        this.executionDuration = executionDuration;
        this.parallelizationFactor = parallelizationFactor;
        this.executor = Executors.newFixedThreadPool(parallelizationFactor);
        for (int i = 0; i < parallelizationFactor; i++) {
            channelList.add(ManagedChannelBuilder
                    .forTarget(grpcServerAddress)
                    .keepAliveTime(1, TimeUnit.MINUTES)
                    .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                    .offloadExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                    .usePlaintext()
                    .build());
        }
    }

    public void export() {
        System.out.println("######################################################");
        long endTime = System.currentTimeMillis() + executionDuration;
        CountDownLatch latch = new CountDownLatch(parallelizationFactor);
        System.out.println("parallelizationFactor:" + parallelizationFactor + " executionDuration:" + executionDuration);
        try {
            for (int i = 0; i < parallelizationFactor; i++) {
                ManagedChannel channel = channelList.get(i % channelList.size());
                this.executor.execute(new ExportTask(channel, endTime, latch, counter));
            }
            latch.await();
            System.out.println("TotalMessageCount:" + counter.get());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static class ExportTask implements Runnable {
        private ManagedChannel channel;
        private long endTime;
        private CountDownLatch latch;
        private AtomicLong counter;

        public ExportTask(ManagedChannel channel, long endTime, CountDownLatch latch, AtomicLong counter) {
            this.channel = channel;
            this.endTime = endTime;
            this.latch = latch;
            this.counter = counter;
        }

        @Override
        public void run() {
            var client = TraceServiceGrpc.newBlockingStub(channel);
            System.out.println("endTime:" + endTime);
            for (; System.currentTimeMillis() < this.endTime;) {
                client.export(createExportTraceServiceRequest());
                counter.incrementAndGet();
            }
            latch.countDown();
        }
    }

}
