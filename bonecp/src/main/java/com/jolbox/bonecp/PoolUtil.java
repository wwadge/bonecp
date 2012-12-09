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

package com.jolbox.bonecp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** A bunch of generic routines used internally by the pool.
 * @author wallacew
 *
 */
@SuppressWarnings("unchecked")
public class PoolUtil {

	protected static Class<Throwable> sqlExceptionClass;
	private static final String exceptionClass = "java.sql.SQLException";

	/** Returns sql statement used in this prepared statement together with the parameters.
	 * @param sql base sql statement
	 * @param logParams parameters to print out
	 * @return returns printable statement 
	 */
	public static String fillLogParams(String sql, Map<Object, Object> logParams) {
		StringBuilder result = new StringBuilder();
		Map<Object, Object> tmpLogParam = (logParams == null ? new HashMap<Object, Object>() : logParams);

		Iterator<Object> it = tmpLogParam.values().iterator();
		boolean inQuote = false;
		boolean inQuote2 = false;
		char[] sqlChar = sql != null ? sql.toCharArray() : new char[]{};

		for (int i=0; i < sqlChar.length; i++){
			if (sqlChar[i] == '\''){
				inQuote = !inQuote;
			}
			if (sqlChar[i] == '"'){
				inQuote2 = !inQuote2;
			}

			if (sqlChar[i] == '?' && !(inQuote || inQuote2)){
				if (it.hasNext()){
					result.append(prettyPrint(it.next()));
				} else {
					result.append('?');
				}
			} else {
				result.append(sqlChar[i]);
			}
		}


		return result.toString();
	}

	/** Helper method
	 * @param o items to print
	 * @return String for safe printing.
	 */
	protected static String safePrint(Object... o){
		StringBuilder sb = new StringBuilder();
		for (Object obj: o){
			sb.append(obj != null ? obj : "null");
		}
		return sb.toString();
	}

	/** Helper method
	 * @param obj item to print
	 * @return String for pretty printing.
	 */
	protected static String prettyPrint(Object obj){
		StringBuilder sb = new StringBuilder();
		if (obj == null){
			sb.append("NULL");
		} else 	if (obj instanceof Blob){
			sb.append(formatLogParam((Blob)obj));
		} else if (obj instanceof Clob){
			sb.append(formatLogParam((Clob)obj));
		} else if (obj instanceof Ref){
			sb.append(formatLogParam((Ref)obj));
		} else if (obj instanceof Array){
			sb.append(formatLogParam((Array)obj));
		} else if (obj instanceof String){
			sb.append("'" + obj.toString()+"'");
		} else {
			sb.append(obj.toString());
		}
		return sb.toString();
	}

	/** Formatter for debugging purposes only. 
	 * @param obj to print
	 * @return String
	 */
	private static String formatLogParam(Blob obj) {
		String result="";
		try {
			result= "(blob of length "+obj.length()+")";
		} catch (SQLException e) {
			result = "(blob of unknown length)";
		}
		return result;
	}

	/** Formatter for debugging purposes only. 
	 * @param obj to print
	 * @return String
	 */
	private static String formatLogParam(Clob obj) {
		String result="";
		try {
			result= "(cblob of length "+obj.length()+")";
		} catch (SQLException e) {
			result = "(cblob of unknown length)";
		}
		return result;
	}

	/** Formatter for debugging purposes only. 
	 * @param obj to print
	 * @return String
	 */
	private static String formatLogParam(Array obj) {
		String result="";
		try {
			result= "(array of type"+obj.getBaseTypeName().length()+")";
		} catch (SQLException e) {
			result = "(array of unknown type)";
		}
		return result;
	}

	/** Formatter for debugging purposes only. 
	 * @param obj to print
	 * @return String
	 */
	private static String formatLogParam(Ref obj) {
		String result="";
		try {
			result= "(ref of type"+obj.getBaseTypeName().length()+")";
		} catch (SQLException e) {
			result = "(ref of unknown type)";
		}
		return result;
	}

	/** For backwards compatibility with JDK1.5 (SQLException doesn't accept the original exception).
	 * @param t exception
	 * @return printStackTrace converted to a string
	 */
	public static String stringifyException(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String result = "";

		result = "------\r\n" + sw.toString() + "------\r\n";
		// closing a stringwriter has no effect.
		return result;
	}	

	public static SQLException generateSQLException(String reason, Throwable t) {
		// slow, but this is for very exceptional cases only
		try {
			// try JDK6/7 style constructor
			if (sqlExceptionClass == null){ // yes there's a chance this can race but nothing bad happens except for a performance hit...
				sqlExceptionClass = (Class<Throwable>) Class.forName(exceptionClass);
			}

				return (SQLException)sqlExceptionClass.getConstructor(String.class, Throwable.class).newInstance(reason, t);
		} catch (Exception e) {
			// fallback to JDK5
			return new SQLException(PoolUtil.stringifyException(t));
		}

	}

}