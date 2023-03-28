package demo;

import demo.client.DataSender;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;


public class Sender implements RequestHandler<SQSEvent, Void> {

	public static final String REQUEST_COUNT = "1000";

	private DataSender sender;

	public Sender() {
		String grpcServerAddress = System.getenv().get("GRPC_SERVER_ADDRESS");
		if (grpcServerAddress == null) {
			throw new RuntimeException("GRPC_SERVER_ADDRESS is empty!");
		}
		int requestCount = Integer.parseInt(System.getenv().getOrDefault("REQUEST_COUNT", REQUEST_COUNT));
		this.sender = new DataSender(grpcServerAddress, requestCount);
	}

	@Override
	public Void handleRequest(SQSEvent event, Context context) {
		sender.export();
		sender.close();
		return null;
	}
}
