package com.adalrsjr.processor_unit.processor

import groovy.util.logging.Slf4j

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import com.adalrsjr.processor_unit.fluentd.pubsub.TraceSubscriber;

interface IProcessor {
	void doSomething(Map object) 
}

interface IProcessorUnit {
	def process(Map object)
}

@Slf4j
class Processor implements IProcessor, Runnable {

	TraceSubscriber subscriber
	IProcessorUnit processor
	
	BlockingQueue<Map> queue = new LinkedBlockingQueue<>()
	boolean stopped = false	
	
	Processor(TraceSubscriber subscriber) {
		this.subscriber = subscriber
		subscriber.registerProcessor(this)
	}
	
	void stop() {
		stopped = true
	}
	
	public void run() {
		while(!stopped) {
			Map object = queue.take()
			
			if(processor) {
				processor.process(object)
			}
		}
	}
	
	public void doSomething(Map object) {
		queue.add(object)
	}
	
	IProcessorUnit registerProcessorUnit(IProcessorUnit procesorUnit) {
		processor = procesorUnit
	}

	IProcessorUnit releaseProcessorUnit() {
		IProcessorUnit processor = this.processor
		this.processor = null
		return processor
	}
	
}
