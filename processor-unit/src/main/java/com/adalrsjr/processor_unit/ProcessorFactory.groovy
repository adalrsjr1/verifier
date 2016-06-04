package com.adalrsjr.processor_unit

import com.adalrsjr.processor_unit.fluentd.parsers.IParser
import com.adalrsjr.processor_unit.fluentd.parsers.JsonParser
import com.adalrsjr.processor_unit.fluentd.pubsub.TracePublisher
import com.adalrsjr.processor_unit.fluentd.pubsub.TraceSubscriber
import com.adalrsjr.processor_unit.processor.IProcessor
import com.adalrsjr.processor_unit.processor.Processor
import com.adalrsjr.processor_unit.processor.hoafautomaton.HoafAutomatonProcessorUnit

enum Parser {
	JSON_PARSER
}

class ProcessorFactory {
	private static IParser createParser(Parser p) {
		if(p == Parser.JSON_PARSER) {
			return new JsonParser()
		}
		else {
			return null
		}
	}
	
	static IProcessor newHoafAutomatonProcessor(String subHost, int subPort, String pubHost, int pubPort, Parser parser, String property) {
		TraceSubscriber subscriber = TraceSubscriber.create(subHost, subPort, createParser(parser))
		TracePublisher publisher = TracePublisher.create(pubHost, pubPort, createParser(parser))
		HoafAutomatonProcessorUnit automaton = new HoafAutomatonProcessorUnit(property)
		IProcessor processor = new Processor(subscriber, publisher, automaton)
		return processor
	}
}
