package com.jolbox.bonecp;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/** Tests util routines.
 * @author wallacew
 *
 */
public class TestPoolUtil {

	/**
	 * Tests formatting stuff.
	 */
	@Test
	public void testPoolUtil(){
			Map<Object, Object> logParams = new LinkedHashMap<Object, Object>();
		
			logParams.put("1", "123");
			logParams.put("2", "456");

			
			// test proper replacement/escaping
			assertEquals("ID=123 AND FOO='?' and LALA=\"BOO\" 456", PoolUtil.fillLogParams("ID=? AND FOO='?' and LALA=\"BOO\" ?", logParams));
		}
}
