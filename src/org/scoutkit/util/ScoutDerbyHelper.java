package org.scoutkit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;

public class ScoutDerbyHelper {
    /* the default framework is embedded */

    private static String framework = "embedded";
    private static String protocol = "jdbc:derby:";
    private static String dbName;
    private static String tableName;

    private static Connection conn = null;
    private static ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
    private static ResultSet rs = null;
    private static Statement s = null;
    
    public static void main(String[] args) {
        ScoutDerbyHelper ss = new ScoutDerbyHelper("scoutDB");
//        System.out.println("---Insert Entry---");
//        ss.insertEntry(1, 2, 3, 4, 5, 6);
        System.out.println("---Printing Table---");
        ss.printEntries();
        System.out.println("Demo finished");
    }

    public ScoutDerbyHelper() {
        this("testDB", "matches");
    }

    public ScoutDerbyHelper(String dbName) {
        this(dbName, "matches");
    }

    public ScoutDerbyHelper(String dbName, String tableName) {
        this.dbName = dbName;
        this.tableName = tableName;
        openDB();
        makeTables();
    }

    private static boolean makeTables() {
        boolean successful = true;
        try {
            // We want to control transactions manually. Autocommit is on by
            // default in JDBC.
            // conn.setAutoCommit(false);

            /* Creating a statement object that we can use for running various
             * SQL statements commands against the database.*/

            // We create a table...
            s.execute("create table " + tableName + "("
                    + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " team INTEGER NOT NULL,"
                    + " matchNo INTEGER NOT NULL,"
                    + " high INTEGER NOT NULL,"
                    + " low INTEGER NOT NULL,"
                    + " catch INTEGER NOT NULL,"
                    + " throw INTEGER NOT NULL,"
                    + " CONSTRAINT primary_key PRIMARY KEY (id)"
                    + ")");
            System.out.println("---Created table " + tableName + "---");

            /*
             We commit the transaction. Any changes will be persisted to
             the database now.
             */
            // conn.commit();
            // System.out.println("Committed the transaction");

        } catch (SQLException sqle) {
            if (((sqle.getErrorCode() == 30000)
                    && ("X0Y32".equals(sqle.getSQLState())))) {
                // we got the expected exception
                System.out.println("---Table " + tableName + " Exists---");
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                printSQLException(sqle);
            }
        }
        return successful;
    }

    public static boolean insertEntry(int team, int matchNo, int critA, int critB, int critC, int critD) {
        boolean successful = true;
        PreparedStatement psInsert;
        try {

            // insert statement: par 1 ID (bigint), par 2 team (int), par 3 match (int), par 4 critA (int), par 5 critB (int), par 6 critC (int), par 7 critD (int)
            psInsert = conn.prepareStatement(
                    "insert into " + tableName
                    + "(team, matchNo, high, low, catch, throw)"
                    + " values (?, ?, ?, ?, ?, ?)");
            statements.add(psInsert);
            int parNo = 1;
            psInsert.setInt(parNo++, team); // par 2 - team
            psInsert.setInt(parNo++, matchNo); // par 3 - match
            psInsert.setInt(parNo++, critA); // par 4 - critA
            psInsert.setInt(parNo++, critB); // par 5 - critB
            psInsert.setInt(parNo++, critC); // par 6 - critC
            psInsert.setInt(parNo++, critD); // par 7 - critD
            psInsert.executeUpdate();
            parNo = 0;
            System.out.println("---Inserted: " + team + ":" + matchNo + ":" + critA + ":" + critB + ":" + critC + ":" + critD + "---");

            /*
             * In embedded mode, an application should shut down the database.
             * If the application fails to shut down the database,
             * Derby will not perform a checkpoint when the JVM shuts down.
             * This means that it will take longer to boot (connect to) the
             * database the next time, because Derby needs to perform a recovery
             * operation.
             *
             * It is also possible to shut down the Derby system/engine, which
             * automatically shuts down all booted databases.
             *
             * Explicitly shutting down the database or the Derby engine with
             * the connection URL is preferred. This style of shutdown will
             * always throw an SQLException.
             *
             * Not shutting down when in a client environment, see method
             * Javadoc.
             */

        } catch (SQLException sqle) {
            printSQLException(sqle);
            successful = false;
        }
        return successful;
    }

    public static ResultSet printEntries() {
        boolean successful = true;
        try {
            System.out.println("Connected to database " + dbName);

            s = conn.createStatement(); // create statement
            statements.add(s);

            rs = s.executeQuery("SELECT * FROM " + tableName + " ORDER BY id");
            //printResults(rs);

        } catch (SQLException sqle) {
            printSQLException(sqle);
            successful = false;
        }

        return rs;
    }
    
       public static ResultSet printStats(String statstring) {
        boolean successful = true;
        try {
            System.out.println("Connected to database " + dbName);

            s = conn.createStatement(); // create statement
            statements.add(s);

            rs = s.executeQuery(statstring);
            //printResults(rs);

        } catch (SQLException sqle) {
            printSQLException(sqle);
            successful = false;
        }

        return rs;
    }
   
    public static void openDB() {
        try {
            Properties props = new Properties(); // connection properties
            // providing a user name and password is optional in the embedded
            // and derbyclient frameworks
            props.put("user", "FRC");
            props.put("password", "FRC");

            /* By default, the schema APP will be used when no username is
             * provided.
             * Otherwise, the schema name is the same as the user name (in this
             * case "user1" or USER1.)
             *
             * Note that user authentication is off by default, meaning that any
             * user can connect to your database using any password. To enable
             * authentication, see the Derby Developer's Guide.
             */

            /*
             * This connection specifies create=true in the connection URL to
             * cause the database to be created when connecting for the first
             * time. To remove the database, remove the directory derbyDB (the
             * same as the database name) and its contents.
             *
             * The directory derbyDB will be created under the directory that
             * the system property derby.system.home points to, or the current
             * directory (user.dir) if derby.system.home is not set.
             */
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(protocol + dbName
                    + ";create=true", props);

            System.out.println("Connected to and created database " + dbName);
            
            s = conn.createStatement(); // create statement
            statements.add(s);
        } catch (SQLException sqle) {
            printSQLException(sqle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void closeDB() {
        /*
         * In embedded mode, an application should shut down the database.
         * If the application fails to shut down the database,
         * Derby will not perform a checkpoint when the JVM shuts down.
         * This means that it will take longer to boot (connect to) the
         * database the next time, because Derby needs to perform a recovery
         * operation.
         *
         * It is also possible to shut down the Derby system/engine, which
         * automatically shuts down all booted databases.
         *
         * Explicitly shutting down the database or the Derby engine with
         * the connection URL is preferred. This style of shutdown will
         * always throw an SQLException.
         *
         * Not shutting down when in a client environment, see method
         * Javadoc.
         */
        if (framework.equals("embedded")) {
            try {
                // the shutdown=true attribute shuts down Derby
                DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");

                // To shut down a specific database only, but keep the
                // engine running (for example for connecting to other
                // databases), specify a database in the connection URL:
                //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
            } catch (SQLException se) {
                if (((se.getErrorCode() == 45000)
                        && ("08006".equals(se.getSQLState())))) {
                    // we got the expected exception
                    System.out.println("Derby shut down normally");
                    // Note that for single database shutdown, the expected
                    // SQL state is "08006", and the error code is 45000.
                } else {
                    // if the error code or SQLState is different, we have
                    // an unexpected exception (shutdown failed)
                    System.err.println("Derby did not shut down normally");
                    printSQLException(se);
                }
            }
        }
        // release all open resources to avoid unnecessary memory usage

        // ResultSet
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            System.err.println("Cleaned ResultSet");
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }

        // Statements and PreparedStatements
        int i = 0;
        while (!statements.isEmpty()) {
            // PreparedStatement extend Statement
            Statement st = (Statement) statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
                System.err.println("Did not clean Statements");
            }
        }

        //Connection
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Connection closed");
                conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Reports a data verification failure to System.err with the given message.
     *
     * @param message A message describing what failed.
     */
    private void reportFailure(String message) {
        System.err.println("\nData verification failed:");
        System.err.println('\t' + message);
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e) {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null) {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }

    public static void printResults(ResultSet rs) throws SQLException {
        while (rs.next()) {
            int team = rs.getInt(rs.findColumn("team"));
            int matchNo = rs.getInt(rs.findColumn("matchNo"));
            int critA = rs.getInt(rs.findColumn("high"));
            int critB = rs.getInt(rs.findColumn("low"));
            int critC = rs.getInt(rs.findColumn("catch"));
            int critD = rs.getInt(rs.findColumn("throw"));

            System.out.println(
                    team + ":" + matchNo + ":" + critA + ":" + critB + ":" + critC + ":" + critD);
        }
    }
}
