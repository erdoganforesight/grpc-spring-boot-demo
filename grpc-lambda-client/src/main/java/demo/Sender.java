package demo;

import demo.client.DataSender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import demo.client.DataSenderSync;


public class Sender implements RequestHandler<SQSEvent, Void> {

	public static final String PARALLELIZATION_COUNT = "1";

	private DataSenderSync sender;

	public Sender() {
		String grpcServerAddress = System.getenv().get("GRPC_SERVER_ADDRESS");
		if (grpcServerAddress == null) {
			throw new RuntimeException("GRPC_SERVER_ADDRESS is empty!");
		}
		int parallelizationFactor = Integer.parseInt(System.getenv().getOrDefault("PARALLELIZATION_COUNT", PARALLELIZATION_COUNT));
		long duration = Integer.parseInt(System.getenv().getOrDefault("EXECUTION_DURATION", "300000"));
		this.sender = new DataSenderSync(grpcServerAddress, parallelizationFactor, duration);
	}

	@Override
	public Void handleRequest(SQSEvent event, Context context) {
		sender.export();
		return null;
	}
}
