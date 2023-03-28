package demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import demo.client.DataSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Starter implements RequestHandler<Map, Void> {

	public static final String MESSAGE_COUNT = "100";

	private final AmazonSQS sqsClient;

	public Starter() {
		this.sqsClient = AmazonSQSClientBuilder
				.standard()
				.build();
	}

	@Override
	public Void handleRequest(Map map, Context context) {
		int messageCount = Integer.parseInt(System.getenv().getOrDefault("MESSAGE_COUNT", MESSAGE_COUNT));

		List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
		for (int i = 0; i < messageCount; i++) {
			SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry()
					.withId(Integer.toString(i))
					.withMessageBody("Message " + i);
			entries.add(entry);
		}

		List<List<SendMessageBatchRequestEntry>> chunks = entries.stream()
				.collect(Collectors.groupingBy(it -> (Integer.parseInt(it.getId()))/10))
				.values()
				.stream()
				.map(items -> items.stream().collect(Collectors.toList()))
				.collect(Collectors.toList());

		for (List<SendMessageBatchRequestEntry> chunk : chunks) {
			SendMessageBatchRequest batchRequest = new SendMessageBatchRequest()
					.withQueueUrl(System.getenv().get("SQS_URL"))
					.withEntries(chunk);
			sqsClient.sendMessageBatch(batchRequest);
		}
		return null;
	}

}
