package de.lwerner.bigdata.graphMetrics;

import org.apache.commons.cli.ParseException;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.util.Collector;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import de.lwerner.bigdata.graphMetrics.models.FoodBrokerEdge;
import de.lwerner.bigdata.graphMetrics.models.FoodBrokerVertex;
import de.lwerner.bigdata.graphMetrics.utils.ArgumentsParser;
import de.lwerner.bigdata.graphMetrics.io.FoodBrokerGraphReader;

import static de.lwerner.bigdata.graphMetrics.utils.GraphMetricsConstants.*;

import java.io.IOException;

/**
 * Calculates the average degree of all vertices
 * 
 * @author Lukas Werner
 * @author Toni Pohl
 */
public class AverageDegree<K extends Number, VV, EV> extends GraphAlgorithm<K, VV, EV> {
	
	private double averageDegree = 0;

	public AverageDegree(Graph<K, VV, EV> graph, ExecutionEnvironment context) throws Exception {
		super(graph, context);
	}
	
	/**
	 * Get result of AverageDegree
	 * 
	 * @return the average degree
	 */
	public double getAverageDegree() {
		return averageDegree;
	}
	
	/**
	 * The main job
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			arguments = ArgumentsParser.parseArguments(AverageDegree.class.getName(), FILENAME_AVERAGE_DEGREE, args);
		} catch (IllegalArgumentException | ParseException e) {
			e.printStackTrace();
			return;
		}
		
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		FoodBrokerGraphReader reader = new FoodBrokerGraphReader(env, arguments.getVerticesPath(), arguments.getEdgesPath());
		new AverageDegree<>(reader.getGraph(), env).runAndWrite();
	}

	/**
	 * Group reduce function to calculate the average of all values
	 * 
	 * @author Lukas Werner
	 */
	public final class AverageCalculator implements GroupReduceFunction<Tuple2<K, Long>, Double> {
		@Override
		public void reduce(Iterable<Tuple2<K, Long>> values, Collector<Double> out) throws Exception {
			long sum = 0;
			long count = 0;
			for (Tuple2<K, Long> value: values) {
				sum += value.f1;
				count++;
			}
			out.collect((double)sum / count);
		}
	}

	@Override
	public void run() throws Exception {		
		// Just need to calculate either out degree or in degree because
		// every outgoing edge goes to any other vertex as an ingoing edge
		DataSet<Tuple2<K, Long>> outDegrees = getGraph().outDegrees();
		DataSet<Double> averageDegreeDataSet = outDegrees.reduceGroup(new AverageCalculator()); 
		averageDegree = averageDegreeDataSet.collect().get(0);
	}

	@Override
	public JsonNode writeOutput(ObjectMapper m) {
		ObjectNode averageDegreeObject = m.createObjectNode();
		averageDegreeObject.put("averageDegree", averageDegree);
		return averageDegreeObject;
	}	
}