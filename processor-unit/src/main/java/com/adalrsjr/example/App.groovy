package com.adalrsjr.example

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

import com.adalrsjr.processor_unit.Parser;
import com.adalrsjr.processor_unit.ProcessorFactory
import com.adalrsjr.processor_unit.processor.IProcessor
import com.google.common.util.concurrent.ThreadFactoryBuilder


class App
{
	public static void main( String[] args )
	{
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("processor-unit-%d").build()
		Executor tPool = Executors.newCachedThreadPool(threadFactory)
		
//		TraceSubscriber subscriber1 = TraceSubscriber.create("localhost", 5556)
//		Processor processor1 = new Processor(subscriber1) 
//		HoafAutomaton automaton1 = new HoafAutomaton("G(\"req_host_src:172.017.000.001\" && \"req_method:GET\"->F\"req_host_dst:172.017.000.006\" && \"response:200\")")
//		processor1.registerProcessorUnit(automaton1)
		
		
		
		IProcessor processor1 = ProcessorFactory.newHoafAutomatonProcessor("localhost", 5558, "localhost", 5557, Parser.JSON_PARSER, "G(\"req_host_src:172.017.000.001\" && \"req_method:GET\"->F\"req_host_dst:172.017.000.006\" && \"response:200\")")
		IProcessor processor2 = ProcessorFactory.newHoafAutomatonProcessor("localhost", 5558, "localhost", 5557, Parser.JSON_PARSER, "G(\"req_method:GET\"->F\"response:200\")")
		
//		TraceSubscriber subscriber2 = TraceSubscriber.create("localhost", 5556)
//		Processor processor2 = new Processor(subscriber2)
//		HoafAutomaton automaton2 = new HoafAutomaton("G(\"req_method:GET\"->F\"response:200\")")
//		processor2.registerProcessorUnit(automaton2)
		
//		HoafAutomatonVisualizer visualizer = new HoafAutomatonVisualizer(automaton1)
//		automaton1.registerListener(visualizer)
//		visualizer.createGraph()
//		visualizer.show()
		
		tPool.execute(processor1)
		tPool.execute(processor2)
		
		
				
	}
}

