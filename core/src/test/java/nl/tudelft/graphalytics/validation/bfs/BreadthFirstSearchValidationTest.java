package nl.tudelft.graphalytics.validation.bfs;

import nl.tudelft.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import nl.tudelft.graphalytics.validation.io.GraphParser;
import nl.tudelft.graphalytics.validation.GraphStructure;
import nl.tudelft.graphalytics.validation.GraphValues;
import nl.tudelft.graphalytics.validation.io.LongParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Tim Hegeman
 */
public abstract class BreadthFirstSearchValidationTest {

	public abstract BreadthFirstSearchOutput executeDirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception;

	public abstract BreadthFirstSearchOutput executeUndirectedBreadthFirstSearch(
			GraphStructure graph, BreadthFirstSearchParameters parameters) throws Exception;

	@Test
	public final void testBreadthFirstSearchOnValidationGraph() throws Exception {
		final String inputPath = "/validation-graphs/bfs-input";
		final String outputPath = "/validation-graphs/bfs-output";
		final long sourceVertex = 1L;

		GraphStructure inputGraph = GraphParser.parseGraphStructureFromVertexBasedDataset(
				getClass().getResourceAsStream(inputPath), true);

		validateBreadthFirstSearch(inputGraph, sourceVertex, outputPath);
	}

	private void validateBreadthFirstSearch(GraphStructure inputGraph, long sourceVertex, String outputPath)
			throws Exception {
		BreadthFirstSearchParameters parameters = new BreadthFirstSearchParameters(sourceVertex);
		BreadthFirstSearchOutput executionResult = executeDirectedBreadthFirstSearch(inputGraph, parameters);

		GraphValues<Long> outputGraph = GraphParser.parseGraphValuesFromDataset(
				getClass().getResourceAsStream(outputPath), new LongParser());

		assertThat("result graph has the correct number of vertices",
				executionResult.getVertices(), hasSize(outputGraph.getVertices().size()));
		assertThat("result graph has the expected vertex ids",
				executionResult.getVertices(), containsInAnyOrder(outputGraph.getVertices().toArray()));
		for (long vertexId : outputGraph.getVertices()) {
			assertThat("vertex " + vertexId + " has correct value",
					executionResult.getPathLengthForVertex(vertexId),
					is(equalTo(outputGraph.getVertexValue(vertexId))));
		}
	}

}