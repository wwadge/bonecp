package com.jolbox.bonecp;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/** A bunch of generic routines used internally by the pool.
 * @author wallacew
 *
 */
public class PoolUtil {

	/** Returns sql statement used in this prepared statement together with the parameters.
	 * @param sql base sql statement
	 * @param logParams parameters to print out
	 * @return returns printable statement 
	 */
	public static String fillLogParams(String sql, Map<Object, Object> logParams) {
		StringBuilder result = new StringBuilder();
		Iterator<Object> it = logParams.values().iterator();
		boolean inQuote = false;
		boolean inQuote2 = false;
		char[] sqlChar = sql.toCharArray();

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
					result.append("?");
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
		} else {
			if (obj instanceof Blob){
				sb.append(formatLogParam((Blob)obj));
			} else
				if (obj instanceof Clob){
					sb.append(formatLogParam((Clob)obj));
				} else
					if (obj instanceof Ref){
						sb.append(formatLogParam((Ref)obj));
					} else
						if (obj instanceof Array){
							sb.append(formatLogParam((Array)obj));
						} else {
							sb.append(obj.toString());
						}
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
}
