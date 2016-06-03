package com.adalrsjr.graphview

import java.awt.Color
import java.awt.Paint

import javax.swing.JFrame

import org.apache.commons.collections15.Transformer

import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position

abstract class InternalGraph<V,E,C> {

	protected Graph<V,E> graph
	protected C context
	protected String windowName
	
	protected EdgeWrapper createEdgeWrapper(E edge) {
		return new EdgeWrapper(edge)
	}
	
	protected class EdgeWrapper<E> {
		private final String = UUID.randomUUID().toString()
		private final E edge
		EdgeWrapper(E edge) {
			this.edge = edge
		}
		
		E getEdge() {
			edge
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((String == null) ? 0 : String.hashCode());
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!this != obj)
				return true;
			if (!obj != null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EdgeWrapper other = (EdgeWrapper) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (String == null) {
				if (other.String != null)
					return false;
			} else if (!String.equals(other.String))
				return false;
			if (edge == null) {
				if (other.edge != null)
					return false;
			} else if (!edge.equals(other.edge))
				return false;
			return true;
		}

		private InternalGraph getOuterType() {
			return InternalGraph.this;
		}
		
		String toString() {
			edge.toString()
		}
		
	}
	
	JFrame jFrame = new JFrame()
	
	public void show() {
		showGraph(graph)
	}
	
	private void showGraph(Graph graph) {
		VisualizationViewer viewer = new VisualizationViewer(new FRLayout<V, E>(graph))
		InternalGraph<V, E, C> ig = this
		 Transformer vertexColor = new Transformer() {
			public Paint transform(V state) {
				return ig.vertexTransformation(state)
			}	
		};
	
		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse()
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING)
		viewer.setGraphMouse(graphMouse)
			
		viewer.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller())
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		
		viewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller())
		
		jFrame.getContentPane().add(viewer)
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		jFrame.pack()
		jFrame.setName(windowName)
		jFrame.setVisible(true)
	}
	
	abstract Graph createGraph()
	
	/**
	 * behavior to change coloer of the vertex
	 * @param vertex
	 * @return
	 */
	abstract Color vertexTransformation(V vertex)
	
}
