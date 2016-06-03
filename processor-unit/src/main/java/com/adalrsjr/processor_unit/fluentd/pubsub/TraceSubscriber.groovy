package com.adalrsjr.processor_unit.fluentd.pubsub

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j;

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

import org.zeromq.ZMQ
import org.zeromq.ZMQ.Context
import org.zeromq.ZMQ.Socket

import com.adalrsjr.processor_unit.fluentd.parsers.IParser
import com.adalrsjr.processor_unit.fluentd.parsers.JsonParser;
import com.adalrsjr.processor_unit.processor.IProcessor
import com.google.common.util.concurrent.ThreadFactoryBuilder

@Slf4j
class TraceSubscriber implements Runnable, ISubscriber {
	private Context context = ZMQ.context(1)
	private Socket subscriber = context.socket(ZMQ.SUB)

	private stopped = false
	private static final ThreadFactory subThreadFactory = new ThreadFactoryBuilder().setNameFormat("log-subscriber-%d").build()
	private static final ExecutorService tSubscriber = Executors.newCachedThreadPool(subThreadFactory)
	static final int tCount = 0

	private final IParser parser
	
	IProcessor processor

	private TraceSubscriber(String host, int port, IParser parser, String topic) {
		this.parser = parser
		subscriber.connect("tcp://${host}:${port}")
		subscriber.subscribe(topic ? topic.bytes : ZMQ.SUBSCRIPTION_ALL)
		
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param parser
	 * @param topic must be the same of the 'match' property in fluentd.config E.g.: <match trace.**> , then topic must be trace.anything
	 * @return
	 */
	static TraceSubscriber create(String host, int port, IParser parser, String topic = null) {
		TraceSubscriber subscriber = new TraceSubscriber(host, port, parser, topic)
		tSubscriber.execute(subscriber)
		tCount++
		return subscriber
	}

	void stop() {
		stopped = true
		tCount--
	}

	void shutdown() {
		tSubscriber.shutdown()
	}

	void run() {
		try {
			while(!stopped && !Thread.currentThread().isInterrupted()) {
				byte[] raw = subscriber.recv()
				
				Map object = parser.deserialize(raw)
				log.debug object.toString()
				if(processor)
					notifyProcessor(object)
			}
		}
		finally {
			subscriber.close()
			context.term()
		}
	}

	void registerProcessor(IProcessor processor) {
		this.processor = processor
	}

	void unregisterProcessor() {
		processor = null
	}

	private void notifyProcessor(Map object) {
		processor.doSomething(object)
	}
	
	
	public static void main(String[] args) {
		
		Thread.start {
			JsonParser parser = new JsonParser()
			TraceSubscriber sub = TraceSubscriber.create("localhost", 5558, parser)
		}
		
	}
}
