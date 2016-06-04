package com.adalrsjr.processor_unit.processor.hoafautomaton

import groovy.transform.Memoized

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import jhoafparser.ast.BooleanExpression
import jhoafparser.consumer.HOAConsumerStore
import jhoafparser.parser.HOAFParser
import jhoafparser.storage.StoredAutomaton

import com.adalrsjr.graphview.HoafAutomatonVisualizer
import com.adalrsjr.processor_unit.fluentd.pubsub.IPublisher
import com.adalrsjr.processor_unit.processor.IProcessorUnit

@Slf4j
class BulkHoafAutomatonProcessorUnit extends HoafAutomatonProcessorUnit {
	private int bulkSize
	private int countedEvents = 0
	
	BulkHoafAutomatonProcessorUnit(String property, int bulkSize) {
		super(property)
		this.bulkSize = bulkSize
	}

	private void setCurrentState(int state, Map event) {
		int prevState = currentState
		long newTime = System.currentTimeMillis()
		currentState = state
		if(countedEvents > 0 && countedEvents % bulkSize == 0) {
			log.info "${this} notifying listeners isAcceptanceState == ${isInAcceptanceState()}"
			if(isInAcceptanceState()) {
				notifyListeners(prevState, state, isInAcceptanceState(), System.currentTimeMillis()-timeInCurrentState, event)
			}
			else {
				log.info "not in Acceptance State"	
			}
		}
		
		timeInCurrentState = newTime
	}

	boolean transition(int currentState, Map event) {
		def transitions = storedAutomaton.getEdgesWithLabel(currentState)

		for(transition in transitions) {
			boolean result = evaluate(transition.labelExpr, event)
			if(result) {
				countedEvents++
				event["countedEvents"] = countedEvents
				setCurrentState(transition.conjSuccessors.first(), event)
				return true
			}
		}
		return false
	}

	public static void main(String[] args) {
		//		AutomatonProcessorUnit automaton = ProcessorUnitFactory.createLtlProcessorUnit("G(\"context:REQUEST\"->F\"statusCode:404\")", true)
		//		HoafAutomaton automaton = new HoafAutomaton("G(\"req_host_src:172.017.000.001\" && \"res_host_dst:172.017.000.006\" -> F \"req_host_dst:172.017.000.001\" && \"res_host_src:172.017.000.006\")")
		BulkHoafAutomatonProcessorUnit automaton = new BulkHoafAutomatonProcessorUnit("G(\"req_host_src:172.017.000.001\" && \"req_method:GET\"->F\"req_host_dst:172.017.000.006\" && \"response:200\")",7)
//G("req_host_src:172.017.000.001"->F"req_host_dst:172.017.000.006")
		int t = 100
		while(true) {
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.005", req_method:"GET", response:"300"])
			Thread.sleep(t)
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.005", req_method:"GET", response:"200"])
			Thread.sleep(t)
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.006", req_method:"GET", response:"200"])
			Thread.sleep(t)
			automaton.next([req_host_src:"172.017.000.002", req_host_dst:"172.017.000.006", req_method:"POST", response:"200"])
			Thread.sleep(t)
			
		}


		//		automaton.handle([context:"REQUEST"]);
	}
}
