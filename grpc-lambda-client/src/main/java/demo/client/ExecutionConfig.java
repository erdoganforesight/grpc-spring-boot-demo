package demo.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionConfig {
    private String grpcServerAddress;
    private Integer threadPoolSize;
    private Integer requestCount;
    private Integer requestSleepTime;
}
