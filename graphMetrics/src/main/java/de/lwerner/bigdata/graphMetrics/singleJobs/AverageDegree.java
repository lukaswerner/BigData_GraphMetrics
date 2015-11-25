package de.lwerner.bigdata.graphMetrics.singleJobs;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.util.Collector;

import de.lwerner.bigdata.graphMetrics.models.FoodBrokerEdge;
import de.lwerner.bigdata.graphMetrics.models.FoodBrokerVertex;
import de.lwerner.bigdata.graphMetrics.utils.ArgumentsParser;
import de.lwerner.bigdata.graphMetrics.utils.CommandLineArguments;
import de.lwerner.bigdata.graphMetrics.utils.FoodBrokerReader;

public class AverageDegree {

	private static CommandLineArguments arguments;
	
	public static void main(String[] args) throws Exception {
		arguments = ArgumentsParser.parseArguments(AverageDegree.class.getName(), args);
		
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		
		DataSet<Vertex<Long, FoodBrokerVertex>> vertices = FoodBrokerReader.getVertices(env, arguments.getNodesPath());
		DataSet<Edge<Long, FoodBrokerEdge>> edges = FoodBrokerReader.getEdges(env, arguments.getEdgesPath());
		
		Graph<Long, FoodBrokerVertex, FoodBrokerEdge> graph = Graph.fromDataSet(vertices, edges, env);
		
		DataSet<Tuple2<Long, Long>> outDegrees = graph.outDegrees();
		DataSet<Tuple2<Long, Long>> inDegrees = graph.inDegrees();
		DataSet<Double> outDegreeAverage = outDegrees.reduceGroup(new AverageCalculator());
		DataSet<Double> inDegreeAverage = inDegrees.reduceGroup(new AverageCalculator());
		
		outDegreeAverage.print();
		inDegreeAverage.print();
	}

	private static final class AverageCalculator implements GroupReduceFunction<Tuple2<Long, Long>, Double> {

		private static final long serialVersionUID = 1L;

		@Override
		public void reduce(Iterable<Tuple2<Long, Long>> values, Collector<Double> out) throws Exception {
			long sum = 0;
			long count = 0;
			
			for (Tuple2<Long, Long> value: values) {
				sum += value.f1;
				count++;
			}
			
			out.collect((double)sum / count);
		}
		
	}
	
}