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
package com.jolbox.bonecp.reportedIssues;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.jolbox.bonecp.BoneCPDataSource;

public class TimeoutFailure {

	  private static Throwable closeException = null;

	  public static void main(String[] args)
	    throws ClassNotFoundException, InterruptedException, SQLException, IOException
	  {
//	    System.setProperty("oracle.jdbc.ReadTimeout", "5000");

	    Class.forName("org.postgresql.Driver");

	    BoneCPDataSource ds = new BoneCPDataSource();
	    ds.setJdbcUrl("jdbc:postgresql://localhost/postgres");
	    ds.setUsername("postgres");
	    ds.setPassword("postgres");
	    ds.setDefaultAutoCommit(false);
	    ds.sanitize();

	    System.out.println("Initial connection test");
	    testConnection(ds);

	    System.out.println("Unplug your network cable ");
	    System.in.read();

	    System.out.println("Doing test, will have pgsql timeout after 5 seconds");
	    testConnection(ds);

	    System.out.println("Plug your network cable");
	    System.in.read();
//	    Thread.sleep(10000);

	    System.out.println("Doing test");
	    testConnection(ds);
	  }

	  private static void testConnection(BoneCPDataSource ds)
	    throws SQLException
	  {
	    closeException = null;
	    Connection connection = null;
	    boolean timedOut = true;

	    try
	    {
	      connection = ds.getConnection();
	      System.out.println("   Got connection");

	      connection.getMetaData();

	      int leased = ds.getTotalLeased();

	      System.out.println("      Leased (should be 1) = " + leased);

	      Statement stmt = connection.createStatement();

	      System.out.println("      Executing statement");
	      stmt.execute("select NOW()");
	      timedOut = false;
	    }
	    catch (Throwable t)
	    {
	      System.err.println("Exception: " + t.getMessage());
	    }
	    finally
	    {
	      if (timedOut)
	      {
	        System.out.println("      Timed out");
	      }
	      silentClose(connection);
	      System.out.println("   Released connection");
	      int leased = ds.getTotalLeased();
	      System.out.println("      Leased (should be 0) = " + leased);
	      if (leased != 0)
	      {
	        System.out.println("Failed, there should be no leased connections.");

	        if (closeException != null)
	        {
	          System.err.println("An exception was thrown on close: " + closeException.getMessage());
	        }
	      }
	    }
	  }

	  private static void silentClose(Connection connection)
	  {
	    try
	    {
	      if (connection != null)
	        connection.close();

	      Thread.sleep(100);
	    }
	    catch (Throwable t)
	    {
	      closeException = t;
	    }
	  }

	}