package com.adalrsjr.processor_unit.processor.hoafautomaton

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

interface IHoafAutomatonListener {
	void reachNewState(int prevState, int nextState, boolean isAcceptanceState, long timeAtPrevState, Map event)
	void stop()
}

@Slf4j
@CompileStatic
class HoafAutomatonProcessorUnit implements IProcessorUnit {
	private static final String LTL_GENERATOR = "ltl2tgba"
	private static final String LTL_GENERATOR_ARGS = "--ba"

	StoredAutomaton storedAutomaton

	protected int currentState = 0
	protected long timeInCurrentState = 0

	private Set<IHoafAutomatonListener> listeners = [] as Set
	
	private IPublisher publisher
	
	HoafAutomatonProcessorUnit(String property) {
		ProcessBuilder builder = new ProcessBuilder(LTL_GENERATOR, LTL_GENERATOR_ARGS, property)
		Process process = builder.start()

		PipedInputStream input = new PipedInputStream()
		PipedOutputStream out = new PipedOutputStream(input)

		process.waitForProcessOutput(out, System.err)

		HOAConsumerStore consumerStore = new HOAConsumerStore()
//				HOAFParser.parseHOA(input, new HOAConsumerPrint(System.out))
		HOAFParser.parseHOA(input, consumerStore)
		storedAutomaton = consumerStore.getStoredAutomaton()
	}

	@Override
	void setPublisher(IPublisher publisher) {
		this.publisher = publisher
	}
	
	@Override
	IPublisher getPublisher() {
		publisher
	}
	
	@Override
	void publish(Map message) {
		if(publisher) {
			publisher.publish(message)
		} else {
			log.info "need to set a publisher at ${this}"
		}
	}
	
	void registerListener(IHoafAutomatonListener listener) {
		listeners << listener
	}

	void unregisterListener(IHoafAutomatonListener listener) {
		listeners.remove(listener)
	}

	protected void notifyListeners(int prevState, int nextState, boolean isAcceptanceState, long timeAtPrevState, Map event) {
		log.debug "prevState:$prevState nextState:$nextState accState:$isAcceptanceState time:$timeAtPrevState ev:$event"
		for(listener in listeners) {
			listener.reachNewState(currentState, currentState, inAcceptanceState, timeInCurrentState, event)
		}
	}

	int getCurrentState() {
		currentState
	}

	private void setCurrentState(int state, Map event) {
		int prevState = currentState
		long newTime = System.currentTimeMillis()
		currentState = state
		notifyListeners(prevState, state, isInAcceptanceState(), System.currentTimeMillis()-timeInCurrentState, event)
		timeInCurrentState = newTime
	}

	boolean isInAcceptanceState() {
		boolean result = storedAutomaton.getStoredState(currentState).accSignature != null
		return result
	}

	@Memoized
	private Map stringToMap(String token) {
		def splited = token.split(":")
		def map = [:]
		map[splited[0]] = splited[1]
		return map
	}

	@Memoized
	boolean evaluate(BooleanExpression expression, Map toEvaluate) {
		BooleanExpression root = expression
		boolean leftResult = null, rightResult = null, result = true
		if(expression.left != null)
			leftResult = evaluate(expression.left, toEvaluate)

		if(expression.right != null)
			rightResult = evaluate(expression.right, toEvaluate)

		if(expression.left == null && expression.right == null && root != null) {
			if(root.type == BooleanExpression.Type.EXP_ATOM) {
				int nRoot = root.toString().toInteger()

				String token = storedAutomaton.storedHeader.APs[nRoot]
				Map mapToken = stringToMap(token)

				// if intersection is empty then there aren'r correspondence
				return toEvaluate.intersect(mapToken).size() != 0

			}
			if(BooleanExpression.Type.EXP_TRUE == expression.type) {
				return true
			}
			if(BooleanExpression.Type.EXP_FALSE == expression.type) {
				return false
			}
		}

		if(BooleanExpression.Type.EXP_NOT == expression.type) {
			result = leftResult == null ? !rightResult : !leftResult

		}
		else if(BooleanExpression.Type.EXP_OR == expression.type) {
			result = leftResult || rightResult
		}
		else if(BooleanExpression.Type.EXP_AND == expression.type) {
			result = leftResult && rightResult
		}
		return result
	}

	boolean transition(int currentState, Map event) {
		def transitions = storedAutomaton.getEdgesWithLabel(currentState)

		for(transition in transitions) {
			boolean result = evaluate(transition.labelExpr, event)
			if(result) {
				setCurrentState(transition.conjSuccessors.first(), event)
				return true
			}
		}
		return false
	}

	boolean next(Map event) {
		def result = transition(currentState, event)
		return result
	}
 
	@Override
	public Object process(Map object) {
		def init = System.nanoTime()
		next(object)
		log.debug "evaluated in ${(System.nanoTime() - init)/1000000} ms"
	}

	@Override
	void cleanup() {
		listeners.each {
			it.stop()
		}
		listeners.clear()
	}
	
	public static void main(String[] args) {
		//		AutomatonProcessorUnit automaton = ProcessorUnitFactory.createLtlProcessorUnit("G(\"context:REQUEST\"->F\"statusCode:404\")", true)
		//		HoafAutomaton automaton = new HoafAutomaton("G(\"req_host_src:172.017.000.001\" && \"res_host_dst:172.017.000.006\" -> F \"req_host_dst:172.017.000.001\" && \"res_host_src:172.017.000.006\")")
		HoafAutomatonProcessorUnit automaton = new HoafAutomatonProcessorUnit("G(\"req_host_src:172.017.000.001\" && \"req_method:GET\"->F\"req_host_dst:172.017.000.006\" && \"response:200\")")
//G("req_host_src:172.017.000.001"->F"req_host_dst:172.017.000.006")
		HoafAutomatonVisualizer visualizer = new HoafAutomatonVisualizer(automaton)
		automaton.registerListener(visualizer)
		visualizer.createGraph()
		visualizer.show()
		
		while(true) {
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.005", req_method:"GET", response:"300"])
			Thread.sleep(500)
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.005", req_method:"GET", response:"200"])
			Thread.sleep(500)
			automaton.next([req_host_src:"172.017.000.001", req_host_dst:"172.017.000.006", req_method:"GET", response:"200"])
			Thread.sleep(500)
			automaton.next([req_host_src:"172.017.000.002", req_host_dst:"172.017.000.006", req_method:"POST", response:"200"])
			Thread.sleep(500)
			
		}


		//		automaton.handle([context:"REQUEST"]);
	}

	
}
