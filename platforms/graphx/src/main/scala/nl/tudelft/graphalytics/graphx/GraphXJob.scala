package nl.tudelft.graphalytics.graphx

import nl.tudelft.graphalytics.domain.GraphFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.reflect.ClassTag
import org.apache.spark.graphx.{Graph, VertexId}

/**
 * Base class for all GraphX jobs in the Graphalytics benchmark. Handles the Spark
 * setup, graph loading, and writing back results.
 *
 * @tparam VD vertex data type
 * @tparam ED edge data type
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 */
abstract class GraphXJob[VD : ClassTag, ED : ClassTag](graphPath : String, graphFormat : GraphFormat,
		outputPath : String) extends Serializable {

	// Set up the Spark context for use in the GraphX job.
	@transient val sparkConfiguration = new SparkConf()
	sparkConfiguration.setAppName(s"Graphalytics: ${getAppName}")
	sparkConfiguration.setMaster("yarn-client")
	@transient val sparkContext = new SparkContext(sparkConfiguration)

	/**
	 * Executes the full GraphX job by reading and parsing the input graph,
	 * running the job-specific graph computation, and writing back the result. 
	 */
	def runJob() = {
		// Load the raw graph data
		val graphData : RDD[String] = sparkContext.textFile(graphPath)
		// Parse the vertex and edge data
		val graph = GraphLoader.loadGraph(graphData, graphFormat, false)

		// Run the graph computation
		val output = compute(graph).cache()
		graph.unpersistVertices(blocking = false)
		graph.edges.unpersist(blocking = false)

		// Output graph in job-specific format
		val stringOutput = makeOutput(output).cache()
		stringOutput.saveAsTextFile(outputPath)
		output.unpersistVertices(blocking = false)
		output.edges.unpersist(blocking = false)
		stringOutput.unpersist(blocking = false)

		sparkContext.stop()
	}
	
	/**
	 * Perform the graph computation using job-specific logic.
	 * 
	 * @param graph the parsed graph with default vertex and edge values
	 * @return the resulting graph after the computation
	 */
	def compute(graph : Graph[Boolean, Int]) : Graph[VD, ED]
	
	/**
	 * Convert a graph to the output format of this job.
	 * 
	 * @return a RDD of strings (lines of output)
	 */
	def makeOutput(graph : Graph[VD, ED]) : RDD[String]
	
	/**
	 * @return true iff the input is valid
	 */
	def hasValidInput : Boolean
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName : String
	
}