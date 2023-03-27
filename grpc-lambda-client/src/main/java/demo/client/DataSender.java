package demo.client;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;

import java.util.concurrent.*;

import static demo.client.RequestUtil.createExportTraceServiceRequest;


public class DataSender  {
    private static ConcurrentLinkedQueue<Future> FUTURE_QUEUE = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Exception> EXCEPTION_QUEUE = new ConcurrentLinkedQueue<>();
    private static int FUTURE_CHECK_SIZE = 1000;

    public static void main(String[] args) {
        // if you are going to test from local you should remove the comment on line 90
        export(ExecutionConfig.builder()
                .grpcServerAddress("dns:///127.0.0.1:9099")
                .requestCount(1000)
                .threadPoolSize(10)
                .requestSleepTime(0)
                .build());
    }

    public static void export(ExecutionConfig config) {
        long start = System.currentTimeMillis();
        try {
            TraceServiceGrpc.TraceServiceFutureStub client = createCollectorAsyncClient(config.getGrpcServerAddress());
            CountDownLatch latch = new CountDownLatch(config.getRequestCount());
            ExecutorService executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
            for (int i = 0; i < config.getRequestCount(); i++) {
                executorService.submit(() -> {
                    sendDataToServer(client, latch, config.getRequestSleepTime());
                });
            }
            latch.await();
            checkFutures();
            executorService.shutdown();
            printResult(config, (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printResult(ExecutionConfig config, long duration) {
        System.out.println("######################################################");
        System.out.println("Total duration: " +  duration
                + " total count: " + config.getRequestCount() + " failed count::" + EXCEPTION_QUEUE.size());
        for (int i = 0; i < EXCEPTION_QUEUE.size() && i < config.getThreadPoolSize(); i++) {
            System.out.println("Exception detail:");
            EXCEPTION_QUEUE.poll().printStackTrace();
        }
    }

    private static void sendDataToServer(TraceServiceGrpc.TraceServiceFutureStub client, CountDownLatch latch, long requestSleepTime) {
        try {
            if (requestSleepTime > 0) {
                Thread.sleep(requestSleepTime);
            }
            FUTURE_QUEUE.add(client.export(createExportTraceServiceRequest()));
        } catch (Exception e) {
            EXCEPTION_QUEUE.add(e);
        } finally {
            latch.countDown();
        }
        if (FUTURE_QUEUE.size() > FUTURE_CHECK_SIZE) {
            checkFutures();
        }
    }

    public static void checkFutures() {
        for (int i = 0; i < FUTURE_CHECK_SIZE && !FUTURE_QUEUE.isEmpty(); i++) {
            try {
                Future future = FUTURE_QUEUE.poll();
                if (future != null) {
                    future.get();
                }
            } catch (Exception e) {
                EXCEPTION_QUEUE.add(e);
            }
        }
    }

    private static TraceServiceGrpc.TraceServiceFutureStub createCollectorAsyncClient(String serverAddress) {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(serverAddress)
                //.usePlaintext() if your communication over http, uncomment this
                .build();
        return TraceServiceGrpc.newFutureStub(channel);
    }
}
