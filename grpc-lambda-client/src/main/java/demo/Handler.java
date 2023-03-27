package demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import demo.client.DataSender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import demo.client.ExecutionConfig;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	public static final String EXECUTOR_THREAD_POOL_SIZE = "10";
	public static final String REQUEST_COUNT = "1000";
	public static final String REQUEST_SLEEP_TIME = "5";

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		ExecutionConfig config = createExecutionParameter(event.getQueryStringParameters());
		DataSender.export(config);
		return createApiGatewayProxyResponseEvent();
	}

	private ExecutionConfig createExecutionParameter(Map<String, String> params) {
		String grpcServerAddress = System.getenv().get("GRPC_SERVER_ADDRESS");
		if (grpcServerAddress == null) {
			throw new RuntimeException("GRPC_SERVER_ADDRESS is empty!");
		}

		Integer poolSize = params != null && params.get("poolSize") != null ? Integer.parseInt(params.get("poolSize")) : null;
		if (poolSize == null) {
			poolSize = Integer.parseInt(System.getenv()
					.getOrDefault("EXECUTOR_THREAD_POOL_SIZE", EXECUTOR_THREAD_POOL_SIZE));
		}

		Integer requestCount = params != null && params.get("requestCount") != null ? Integer.parseInt(params.get("requestCount")) : null;
		if (requestCount == null) {
			requestCount = Integer.parseInt(System.getenv()
					.getOrDefault("REQUEST_COUNT", REQUEST_COUNT));
		}

		Integer requestSleepTime = params != null && params.get("requestSleepTime") != null ? Integer.parseInt(params.get("requestSleepTime")) : null;
		if (requestSleepTime == null) {
			requestSleepTime = Integer.parseInt(System.getenv()
					.getOrDefault("REQUEST_SLEEP_TIME", REQUEST_SLEEP_TIME));
		}

		return ExecutionConfig.builder()
				.grpcServerAddress(grpcServerAddress)
				.requestCount(requestCount)
				.threadPoolSize(poolSize)
				.requestSleepTime(requestSleepTime)
				.build();
	}

	private APIGatewayProxyResponseEvent createApiGatewayProxyResponseEvent() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & Serverless");
		headers.put("Content-Type", "application/json");
		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put("Content-Type", "application/json");
		return new APIGatewayProxyResponseEvent()
				.withHeaders(responseHeaders)
				.withStatusCode(200)
				.withBody("Your request has been received at " + new Date());
	}
}
