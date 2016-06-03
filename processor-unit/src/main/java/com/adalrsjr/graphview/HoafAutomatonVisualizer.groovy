package com.adalrsjr.graphview

import java.awt.Color

import jhoafparser.storage.StoredAutomaton
import jhoafparser.storage.StoredEdgeWithLabel
import jhoafparser.storage.StoredState

import com.adalrsjr.graphview.InternalGraph.EdgeWrapper
import com.adalrsjr.processor_unit.processor.hoafautomaton.HoafAutomaton;
import com.adalrsjr.processor_unit.processor.hoafautomaton.IHoafAutomatonListener;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.graph.util.EdgeType

class HoafAutomatonVisualizer extends InternalGraph<Integer, String, HoafAutomaton> 
							  implements IHoafAutomatonListener {
	private HoafAutomaton context

	HoafAutomatonVisualizer(HoafAutomaton context) {
		this.context = context
	}
	
	@Override
	public Graph createGraph() {
		StoredAutomaton automaton = context.storedAutomaton
		
		Iterable<StoredState> states = automaton.storedStates
		graph = new DirectedSparseMultigraph()

		states.each { StoredState state ->
			automaton.getEdgesWithLabel(state.stateId).each { StoredEdgeWithLabel edge ->
				
//				int cont = 0
//				String nEdge = edge.labelExpr.toString()
//				automaton.storedHeader.APs.each {
//					nEdge = nEdge.replaceAll("${cont}", automaton.storedHeader.APs[cont])
//					cont++
//				}
				
				edge.conjSuccessors.each { def to ->
					EdgeWrapper e = createEdgeWrapper(edge.labelExpr.toString())

					graph.addEdge(e, state.stateId, automaton.getStoredState(to).stateId, EdgeType.DIRECTED)
				}
			}

		}

		return graph
	}

	@Override
	public Color vertexTransformation(Integer vertex) {
		Color color
		boolean isAcc = context.storedAutomaton.getStoredState(vertex).accSignature != null
		
		if(vertex == context.currentState) {
			return isAcc ? Color.GREEN : Color.BLUE;
		}
		return isAcc ? Color.RED : Color.YELLOW;
	}

	public void reachNewState(int prevState, int nextState, boolean isAcceptanceState, long timeAtPrevState,
			Map event) {
		jFrame.repaint()
	}

}
