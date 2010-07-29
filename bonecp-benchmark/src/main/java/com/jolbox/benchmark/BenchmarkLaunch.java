/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * 
 */
package com.jolbox.benchmark;

import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.jolbox.bonecp.MockJDBCDriver;


/**
 * @author Wallace
 *
 */
public class BenchmarkLaunch {

	

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws PropertyVetoException 
	 * @throws SQLException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InterruptedException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws NamingException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, PropertyVetoException, IllegalArgumentException, SecurityException, InterruptedException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NamingException, ParseException, IOException {

		Options options = new Options();
		options.addOption("t", "threads",true, "Max number of threads");
		options.addOption("s", "stepping",true, "Stepping of threads");
		options.addOption("p", "poolsize",true, "Pool size");
		options.addOption("h", "help",false, "Help");
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		if (cmd.hasOption("h")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "benchmark.jar", options );
			System.exit(1);
		}
			
		
		BenchmarkTests.threads=400;
		BenchmarkTests.stepping=5;
		BenchmarkTests.pool_size=200;
		if (cmd.hasOption("t")){
			BenchmarkTests.threads=Integer.parseInt(cmd.getOptionValue("t", "400"));
		} 
		if (cmd.hasOption("s")){
			BenchmarkTests.stepping=Integer.parseInt(cmd.getOptionValue("s", "20"));
		} 
		if (cmd.hasOption("p")){
			BenchmarkTests.pool_size=Integer.parseInt(cmd.getOptionValue("p", "200"));
		} 
		
		System.out.println("Starting benchmark tests with "
				+ BenchmarkTests.threads + " threads (stepping "
				+ BenchmarkTests.stepping+ ") using pool size of "+BenchmarkTests.pool_size+" connections");
		
		new MockJDBCDriver();
		BenchmarkTests tests = new BenchmarkTests();


		
		plotLineGraph(tests.testMultiThreadedConstantDelay(0), 0, false);
		plotLineGraph(tests.testMultiThreadedConstantDelay(10), 10, false);
		plotLineGraph(tests.testMultiThreadedConstantDelay(25), 25, false);
		plotLineGraph(tests.testMultiThreadedConstantDelay(50), 50, false);
		plotLineGraph(tests.testMultiThreadedConstantDelay(75), 75, false);
		
		plotBarGraph("Single Thread", "bonecp-singlethread-poolsize-"+BenchmarkTests.pool_size+"-threads-"+BenchmarkTests.threads+".png", tests.testSingleThread());
		plotBarGraph("Prepared Statement\nSingle Threaded", "bonecp-preparedstatement-single-poolsize-"+BenchmarkTests.pool_size+"-threads-"+BenchmarkTests.threads+".png", tests.testPreparedStatementSingleThread());
		plotLineGraph(tests.testMultiThreadedConstantDelayWithPreparedStatements(0), 0, true);
		plotLineGraph(tests.testMultiThreadedConstantDelayWithPreparedStatements(10), 10, true);
		plotLineGraph(tests.testMultiThreadedConstantDelayWithPreparedStatements(25), 25, true);
		plotLineGraph(tests.testMultiThreadedConstantDelayWithPreparedStatements(50), 50, true);
		plotLineGraph(tests.testMultiThreadedConstantDelayWithPreparedStatements(75), 75, true);

	}


	/**
	 * @param results
	 * @param delay
	 * @param statementBenchmark
	 * @throws IOException 
	 */
	private static void plotLineGraph(long[][] results, int delay, boolean statementBenchmark) throws IOException {
//		doPlotLineGraph(results, delay, statementBenchmark, true);
		doPlotLineGraph(results, delay, statementBenchmark, false);
	}
	/**
	 * @param results
	 * @param delay 
	 * @param statementBenchmark 
	 * @param noC3P0 
	 * @throws IOException 
	 */
	private static void doPlotLineGraph(long[][] results, int delay, boolean statementBenchmark, boolean noC3P0) throws IOException {
		String title = "Multi-Thread test ("+delay+"ms delay)";
		if (statementBenchmark){
			title += "\n(with PreparedStatements tests)";
		}
		String fname = System.getProperty("java.io.tmpdir")+File.separator+"bonecp-multithread-"+delay+"ms-delay";
    	if (statementBenchmark){
    		fname += "-with-preparedstatements";
    	}
    	fname += "-poolsize-"+BenchmarkTests.pool_size+"-threads-"+BenchmarkTests.threads;
    	if (noC3P0){
    		fname+="-noC3P0";
    	}
    	PrintWriter out = new PrintWriter(new FileWriter(fname+".txt"));
    	 fname += ".png";
     
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i=0; i <  ConnectionPoolType.values().length; i++){ //
			if (!ConnectionPoolType.values()[i].isEnabled() || (noC3P0 && ConnectionPoolType.values()[i].equals(ConnectionPoolType.C3P0))){
				continue;
			}
			XYSeries series = new XYSeries(ConnectionPoolType.values()[i].toString());
			out.println(ConnectionPoolType.values()[i].toString());
			for (int j=1+BenchmarkTests.stepping; j < results[i].length; j+=BenchmarkTests.stepping){
		        series.add(j, results[i][j]);
		        out.println(j+","+results[i][j]);
			}
			dataset.addSeries(series);
		}
		out.close();

		//         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(title, // Title
                "threads", // x-axis Label
                "time (ns)", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
        

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.YELLOW);
        renderer.setSeriesPaint(2, Color.BLACK);
        renderer.setSeriesPaint(3, Color.DARK_GRAY);
        renderer.setSeriesPaint(4, Color.MAGENTA);
        renderer.setSeriesPaint(5, Color.RED);
        renderer.setSeriesPaint(6, Color.LIGHT_GRAY);
//          renderer.setSeriesShapesVisible(1, true);   
//          renderer.setSeriesShapesVisible(2, true);   

        try {
        	   ChartUtilities.saveChartAsPNG(new File(fname), chart, 1024, 768);
            System.out.println("******* Saved chart to: " + fname);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
        
	}


	/**
	 * @param title 
	 * @param filename 
	 * @param results 
	 * @throws IOException 
	 */
	private static void plotBarGraph(String title, String filename, long[] results) throws IOException {
		String fname = System.getProperty("java.io.tmpdir")+File.separator+filename;
		PrintWriter out = new PrintWriter(new FileWriter(fname+".txt"));
    	
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (ConnectionPoolType poolType: ConnectionPoolType.values()){
				dataset.setValue(results[poolType.ordinal()], "ms", poolType);
				out.println(results[poolType.ordinal()]+ ","+ poolType);
		}
		out.close();
		JFreeChart chart = ChartFactory.createBarChart(title,
				"Connection Pool", "Time (ms)", dataset, PlotOrientation.VERTICAL, false,
				true, false);
		try {
			ChartUtilities.saveChartAsPNG(new File(fname), chart, 1024,
					768);
			System.out.println("******* Saved chart to: " + fname);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Problem occurred creating chart.");
		}
	}

}
