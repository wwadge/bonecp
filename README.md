BoneCP
======

BoneCP is a Java JDBC connection pool implementation that is tuned for high performance by minimizing lock contention to give greater throughput for your applications. It beats older connection pools such as C3P0 and DBCP but <b>should now be considered deprecated</b> in favour of HikariCP.

---------------------------------------------------------------------
## Change
I find that when bonecp is used on a very idle system, the com.mysql.jdbc.NonRegistingDriver object will grow more and more util JVM start full GC.

I add a variable connectionUsedCounts to ConnectionHandler class to record how many times a connection is called in a test period, if the number is lower than 10 times, tester thread will reset the connectionLastUsedInMs and connectionUsedCounts variable, so that the full GC will not occur any more.
