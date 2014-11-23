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

  private static String framework = "embedded";
  private static String protocol = "jdbc:derby:";
  private static String dbName;
  private static String tableName;
  private static String lasterror = "";

  private static Connection conn = null;
  private static ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
  private static ResultSet rs = null;
  private static Statement s = null;

  public ScoutDerbyHelper() { // Empty constructor with default settings
    this("testDB", "matches");
  }

  public ScoutDerbyHelper(String dbName) { // Change the DB Name
    this(dbName, "matches");
  }

  public ScoutDerbyHelper(String dbName, String tableName) { // Change the DB Name and Table Name
    this.dbName = dbName;
    this.tableName = tableName;
    openDB();
    makeTables();
    closeDB();
  }

  private static boolean makeTables() { // Initialize the table
    boolean successful = true;
    try {
      System.out.println("Connected to database " + dbName);

      // We create a table...
      s.execute("create table " + tableName + "("
              + " team INTEGER NOT NULL,"
              + " matchNo INTEGER NOT NULL,"
              + " A FLOAT NOT NULL,"
              + " B FLOAT NOT NULL,"
              + " C FLOAT NOT NULL,"
              + " D FLOAT NOT NULL,"
              + " comment VARCHAR(512),"
              + " CONSTRAINT id PRIMARY KEY (team, matchNo)"
              + ")");
      System.out.println("---Created table " + tableName + "---");

    } catch (SQLException sqle) {
      if (((sqle.getErrorCode() == 30000)
              && ("X0Y32".equals(sqle.getSQLState())))) {
        // The table already exists. Let the user know
        System.out.println("---Table " + tableName + " Exists---");
      } else {
        // if the error code or SQLState is different, we have
        // an unexpected exception (something failed)
        printSQLException(sqle);
      }
    }
    return successful;
  }

  /** Inserts an entry into the matches table.
   *  or updates it if an entry with a matchiing team and matchNo exists.
   * @returns successful - if false, then something went wrong. Check getLastError for details.
   */
  public static boolean insertEntry(int team, int matchNo, int critA, int critB, int critC, int critD, String comment) {
    boolean successful = true;
    PreparedStatement psInsert;
    try {
      System.out.println("Connected to database " + dbName);
      psInsert = conn.prepareStatement(
              "INSERT INTO " + tableName
              + "(team, matchNo, A, B, C, D, comment)"
              + " VALUES (?, ?, ?, ?, ?, ?, ?)");
      statements.add(psInsert);
      int parNo = 1;
      psInsert.setInt(parNo++, team); // par 1 - team
      psInsert.setInt(parNo++, matchNo); // par 2 - match
      psInsert.setInt(parNo++, critA); // par 3 - critA
      psInsert.setInt(parNo++, critB); // par 4 - critB
      psInsert.setInt(parNo++, critC); // par 5 - critC
      psInsert.setInt(parNo++, critD); // par 6 - critD
      psInsert.setString(parNo++, comment); // par 8 - Comment
      psInsert.executeUpdate();
      parNo = 0;
      System.out.println("---Inserted: " + team + ":" + matchNo + ":" + critA + ":" + critB + ":" + critC + ":" + critD + ":" + comment + "---");

    } catch (SQLException sqle) {
      if (((sqle.getErrorCode() == 30000)
              && ("23505".equals(sqle.getSQLState())))) {
        // There is a key conflict. Update that entry instead
        updateEntry(team, matchNo, critA, critB, critC, critD, comment);
      } else {
        // if the error code or SQLState is different, we have
        // an unexpected exception (something failed)
        printSQLException(sqle);
        successful = false;
      }
    }
    return successful;
  }

  /** Updates an entry with existing team and matchNo
  * @returns successful - if false, then something went wrong. Check getLastError for details.
   */
  public static boolean updateEntry(int team, int matchNo, int critA, int critB, int critC, int critD, String comment) {
    boolean successful = true;
    PreparedStatement psInsert;
    try {
      psInsert = conn.prepareStatement(
              "UPDATE " + tableName + " SET \n"
              + "A = ?, B = ?, C = ?, D = ?, comment = ? \n"
              + " WHERE team = ? AND matchNo = ?");
      statements.add(psInsert);
      int parNo = 1;
      psInsert.setInt(parNo++, critA); // par 1 - critA
      psInsert.setInt(parNo++, critB); // par 2 - critB
      psInsert.setInt(parNo++, critC); // par 3 - critC
      psInsert.setInt(parNo++, critD); // par 4 - critD
      psInsert.setString(parNo++, comment); // par 5 - comment
      psInsert.setInt(parNo++, team); // par 6 - team
      psInsert.setInt(parNo++, matchNo); // par 7 - matchNo
      psInsert.executeUpdate();
      parNo = 0;
      System.out.println("---Updated: " + team + ":" + matchNo + ":" + critA + ":" + critB + ":" + critC + ":" + critD + ":" + comment + "---");

    } catch (SQLException sqle) {
      printSQLException(sqle);
      successful = false;
    }
    return successful;
  }

  /** Reads entries from matches table
   *  @returns rs - contains ResultSet if sucessful. Null if not.
   */
  public static ResultSet printEntries() {
    boolean successful = true;
    try {
      System.out.println("Connected to database " + dbName);

      s = conn.createStatement(); // create statement
      statements.add(s);

      System.out.println("---Reading Entries---");

      rs = s.executeQuery("SELECT * FROM " + tableName + " ORDER BY team");

    } catch (SQLException sqle) {
      printSQLException(sqle);
      return null;
    }

    return rs;
  }


  /** Executes an arbitrary SQL query and returns the result.
   *  @returns rs - contains ResultSet if sucessful. Null if not.
   */
  public static ResultSet printStats(String statstring) {
    boolean successful = true;
    try {
      System.out.println("Connected to database " + dbName);

      s = conn.createStatement(); // create statement
      statements.add(s);

      System.out.println("---Reading Statistics---");

      rs = s.executeQuery(statstring);

    } catch (SQLException sqle) {
      printSQLException(sqle);
      successful = false;
      return null;
    }

    return rs;
  }

  /** Opens the database for modifying. Must be called before anything else!
  */
  public static void openDB() {
    try {
      Properties props = new Properties();
      props.put("user", "FRC");
      props.put("password", "FRC");
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
      conn = DriverManager.getConnection(protocol + dbName
              + ";create=true", props);

      s = conn.createStatement(); // create statement
      statements.add(s);
    } catch (SQLException sqle) {
      printSQLException(sqle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void closeDB() {
    if (framework.equals("embedded")) {
      try {
        // the shutdown=true attribute shuts down Derby
        DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
      } catch (SQLException se) {
        if (((se.getErrorCode() == 45000)
        && ("08006".equals(se.getSQLState())))) {
          // we got the expected exception
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
      //System.err.println("Cleaned ResultSet");
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
      lasterror = e.getMessage();
      e = e.getNextException();
    }
  }

  /** Contains the latest error message. Call if something goes wrong.
   * @returns String containing the latest error message.
   */
  public static String getLastError() {
    return lasterror;
  }
}
