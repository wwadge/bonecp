package com.jolbox.bonecp;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCPConfig;


/**
 * A utility class for the test classes.
 *
 * @author wwadge
 * @version $Revision$
 */
public class CommonUtils {
	/** Logger handle. */
	private static Logger logger = Logger.getLogger(CommonUtils.class);
	/**
	 * Helper map.
	 */
	public static Map<Class<?>, Object> instanceMap;
	/** A dummy query for HSQLDB. */
	public static final String TEST_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
	/** A Sample test query for use with HSQLDB*/
	static String url="jdbc:hsqldb:mem:test";
	/** hsqldb username. */
	static String username="sa";
	/** hsqldb password. */
	static String password="";
	/** Config file handle. */
	static BoneCPConfig config = new BoneCPConfig();

	static{
		instanceMap = new HashMap<Class<?>, Object>();
		instanceMap.put(int.class, 1);
		instanceMap.put(long.class, 1L);
		instanceMap.put(byte.class, (byte) 1);
		instanceMap.put(char.class, 'a');
		instanceMap.put(double.class, 1.0);
		instanceMap.put(short.class, new Short("1"));
		instanceMap.put(float.class, new Float(1.0));
		instanceMap.put(boolean.class, true);
		instanceMap.put(int[].class, new int[]{0,1});
		instanceMap.put(String[].class, new String[]{"test","bar"});
		instanceMap.put(String.class, "test");


	}
	/**
	 * Pretty printing.
	 */
	static void logPass(){
		logTestInfo("Passing test");
	}

	/** Pretty printing. 
	 * @param text
	 */
	static void logTestInfo(String text){
		logger.debug("-----------------------------------------------------------------------------");
		logger.debug(text);
		logger.debug("-----------------------------------------------------------------------------\n");
	}


	/**
	 * Helper function.
	 *
	 * @param threads
	 * @param connections
	 * @param cpds
	 * @param workDelay
	 * @param doPreparedStatement 
	 * @return time taken
	 * @throws InterruptedException
	 */
	public static long startThreadTest(int threads, long connections,
			DataSource cpds, int workDelay, boolean doPreparedStatement) throws InterruptedException {
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(threads);

		ExecutorService pool = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++){ // create and start threads
			pool.execute(new ThreadTesterUtil(startSignal, doneSignal, cpds, connections, workDelay, doPreparedStatement));
		}

		long start = System.currentTimeMillis();
		startSignal.countDown(); // START TEST!
		doneSignal.await(); 
		long end = (System.currentTimeMillis()-start);

		pool.shutdown();
		return end;
	}


	/** Create mock expectations of the given classes then invoke the given method twice (once normal + once faking an SQL exception).
	 * @param mockClass
	 * @param testClass
	 * @param method
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static void doTestStatementBounceMethod(Object mockClass, Object testClass, Method method, Object... args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{

		if (method.getReturnType() == void.class){
			method.invoke(mockClass, args);
			expectLastCall().once().andThrow(new SQLException()).once();
		} else {
			expect(method.invoke(mockClass, args)).andReturn(null).once().andThrow(new SQLException()).once();
		}

		replay(mockClass);
		method.invoke(testClass, args);
		try{
			method.invoke(testClass, args); // and repeat the test with the fake exception trigger
			fail("Should have thrown an exception");
		} catch(Throwable t){
			// do nothing
		}
		verify(mockClass);
		reset(mockClass);
	}

	/** Create mock expectations of the given classes then invoke the given method twice (once normal + once faking an SQL exception).
	 * @param testClass
	 * @param skipTests
	 * @param mockClass
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void testStatementBounceMethod(Object testClass, Set<String> skipTests, Object mockClass) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{

		Method[] methods = testClass.getClass().getDeclaredMethods();
		for (Method method: methods){
			if (skipTests.contains(method.getName()) || method.getExceptionTypes().length == 0 || !method.getExceptionTypes()[0].equals(SQLException.class)) {
				continue;
			}
			Class<?>[] params = method.getParameterTypes();
			Object[] mockParams = new Object[params.length];
			for (int i=0; i < params.length; i++){
				mockParams[i] = CommonUtils.instanceMap.get(params[i]);
			}

			doTestStatementBounceMethod(mockClass, testClass, method, mockParams);
		}
	}




}
