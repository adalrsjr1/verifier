package com.adalrsjr.processor_unit.fluentd.pubsub

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory

import org.zeromq.ZMQ
import org.zeromq.ZMQ.Context
import org.zeromq.ZMQ.Socket

import com.adalrsjr.processor_unit.fluentd.parsers.IParser
import com.adalrsjr.processor_unit.fluentd.parsers.JsonParser
import com.google.common.util.concurrent.ThreadFactoryBuilder

import groovy.util.logging.Slf4j;

@Slf4j
class TracePublisher implements Runnable, IPublisher  {
	private Context context = ZMQ.context(10)
	private Socket publisher = context.socket(ZMQ.PUB)

	private stopped = false
	private static final ThreadFactory subThreadFactory = new ThreadFactoryBuilder().setNameFormat("log-publisher").build()
	private static final ExecutorService tPublisher = Executors.newSingleThreadExecutor(subThreadFactory)

	private static final BlockingQueue<Map> queue = new LinkedBlockingQueue<>()

	private final IParser parser
	private String topic

	private static TracePublisher INSTANCE

	private TracePublisher(String host, int port, IParser parser, String topic) {
		this.parser = parser
		this.topic = topic
		publisher.bind("tcp://${host}:${port}")
	}

	/**
	 * 
	 * @param host
	 * @param port
	 * @param parser
	 * @param topic must be the same of the 'match' property in fluentd.config E.g.: <match trace.**> , then topic must be trace.anything
	 * @return
	 */
	static TracePublisher create(String host, int port, IParser parser, String topic = null) {
		if(INSTANCE == null) {
			INSTANCE = new TracePublisher(host, port, parser, topic)
			tPublisher.execute(INSTANCE)
		}
		return INSTANCE
	}
	
	TracePublisher getInstance() {
		INSTANCE
	}

	void stop() {
		stopped = true
	}

	@Override
	synchronized void publish(Map message) {
		List toFluentd = new ArrayList<String>()
		toFluentd.add(topic)
		toFluentd.add(System.currentTimeMillis())
		toFluentd.add(message)
		def serialized = parser.serialize(toFluentd)
		queue.add(serialized)
	}

	void run() {
		try {
			while(!stopped && !Thread.currentThread().isInterrupted()) {
				def message = queue.take()
				publisher.sendMore(topic)
				publisher.send(message)
				log.debug new String(message)
			}
		}
		finally {
			publisher.close()
			context.term()
			queue.clear()
		}
	}

	public static void main (String[] args) throws Exception {
		JsonParser parser = new JsonParser()
		TracePublisher publisher = TracePublisher.create("localhost", 5557, parser, "trace.verifier")

		while(true) {
			Map msg = [msg:"123", time:System.currentTimeMillis(), topic:"trace.verifier.zmq"]
			publisher.publish(msg)
			Thread.sleep(300)
		}

	}

}
