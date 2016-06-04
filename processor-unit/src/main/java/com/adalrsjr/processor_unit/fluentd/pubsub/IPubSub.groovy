package com.adalrsjr.processor_unit.fluentd.pubsub

import java.util.Map;

import com.adalrsjr.processor_unit.processor.IProcessor;

interface IPublisher {
	void publish(Map message)
}

interface ISubscriber {
	void registerProcessor(IProcessor processor)
	void unregisterProcessor(IProcessor processor)
	void notifyProcessors(Map object)
	
}
