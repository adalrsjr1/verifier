package com.adalrsjr.processor_unit.processor

import groovy.util.logging.Slf4j

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import com.adalrsjr.processor_unit.fluentd.pubsub.IPublisher;
import com.adalrsjr.processor_unit.fluentd.pubsub.ISubscriber;
import com.adalrsjr.processor_unit.fluentd.pubsub.TracePublisher
import com.adalrsjr.processor_unit.fluentd.pubsub.TraceSubscriber

interface IProcessor extends Runnable {
	void doSomething(Map object) 
	void stop()
}

interface IProcessorUnit {
	def process(Map object)
	void publish(Map object)
	IPublisher getPublisher()
	void setPublisher(IPublisher publisher)
	void cleanup() 
}

@Slf4j
class Processor implements IProcessor {

	ISubscriber subscriber
	IPublisher publisher
	IProcessorUnit processor
	
	BlockingQueue<Map> queue = new LinkedBlockingQueue<>()
	boolean stopped = false	
	
	Processor(ISubscriber subscriber, IPublisher publisher, IProcessorUnit processorUnit) {
		log.info "setting subscriber at ${this}"
		this.subscriber = subscriber
		log.info "setting publisher at ${this}"
		this.publisher = publisher
		
		log.info "registering ${processorUnit} at ${this}"
		subscriber.registerProcessor(this)
		
	}
	
	@Override
	synchronized void stop() {
		subscriber.unregisterProcessor(this)
		stopped = true		
	}
	
	@Override
	public void run() {
		while(!stopped) {
			Map object = queue.take()
			
			if(processor) {
				processor.process(object)
			}
		}
		processor.cleanup()
	}
	
	@Override
	public void doSomething(Map object) {
		queue.add(object)
	}
	
	private IProcessorUnit registerProcessorUnit(IProcessorUnit procesorUnit) {
		processor = procesorUnit
		processor.setPublisher(publisher)
	}
}
