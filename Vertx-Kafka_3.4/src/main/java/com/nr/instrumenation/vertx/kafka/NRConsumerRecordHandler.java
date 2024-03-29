package com.nr.instrumenation.vertx.kafka;

import java.util.HashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.DestinationType;
import com.newrelic.api.agent.MessageConsumeParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransactionNamePriority;

import io.vertx.core.Handler;

public class NRConsumerRecordHandler<K,V> implements Handler<ConsumerRecord<K, V>> {
	
	private Handler<ConsumerRecord<K, V>> delegate = null;
	
	private static boolean isTransformed = false;
	
	public NRConsumerRecordHandler(Handler<ConsumerRecord<K, V>> d) {
		delegate = d;
		if(!isTransformed) {
			isTransformed = true;
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
		}
	}
	

	@Override
	@Trace(dispatcher=true)
	public void handle(ConsumerRecord<K, V> event) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		KafkaUtils.addAttribute(attributes, "Consumer-Topic", event.topic());
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
		traced.addCustomAttributes(attributes);
		MessageConsumeParameters params = MessageConsumeParameters.library("Kafka").destinationType(DestinationType.NAMED_TOPIC).destinationName(event.topic()).inboundHeaders(null).build();
		traced.reportAsExternal(params);
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_LOW, true, "KafkaConsumer", "Kafka","Consumer","handle",event.topic());
		if(delegate != null) {
			delegate.handle(event);
		}
	}

}
