package io.rawiron.farmgame.system;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;

import it.gotoandplay.smartfoxserver.db.*;


public class jdbcDB implements DataStore {

    private HashMap<String, Connection> connMap = new HashMap<String, Connection>();
    private Trace t;

    public jdbcDB(Trace in_t) {
        t = in_t;
    }

    public boolean closeAll() {
        return true;
    }

    public boolean addPooledConnection(String key, DbManager connectPool) {
        boolean success = false;
        Connection con = null;

        try {
            con = this.connectPool(connectPool);
        } catch (SQLException e) {
            t.trace("SQLException: " + e.getMessage());
            t.trace("SQLState: " + e.getSQLState());
            t.trace("VendorError: " + e.getErrorCode());
        }

        if (con != null) {
            connMap.put(key, con);
            success = true;
        } else {
            if (t.VERBOSE && (t.VERBOSE_LEVEL >= 1)) t.trace("error connection NULL for=" + key);
        }
        return success;
    }

    public HashMap<String, Connection> retrieve() {
        return connMap;
    }


    private Connection connectPool(DbManager conPool) throws SQLException
    /**
     create a connection
     @return newly created Connection
     */
    {
        Connection con = null;
        // con = DriverManager.getConnection( "jdbc:mysql://localhost:3306/test"+"user=reader&password=hello");
        con = conPool.getConnection();

        return con;
    }


    public int execute(String sql, String region, String key)
    /**
     * Sends a query to the database and returns a boolean
     * result of success or failure.
     *
     * @param con:Connection
     * @param sql:String
     *
     * @return success:Boolean
     */
    {
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 4)) t.trace(key + ":" + sql);

        Statement stmt = null;
        int success = 0;
        Connection con;

        con = (Connection) connMap.get(key);
        if (con != null) {
            try {
                if (t.TRACE_TIMERS) t.timer.push(t.getTimer());
                stmt = con.createStatement();
                success = stmt.executeUpdate(sql);
                if (t.TRACE_TIMERS) t.timer.push(t.getTimer());

                // Show the time it took to parse the request
                if (t.TRACE_TIMERS) t.trace("DB Request took: " + (t.timer.pop() - t.timer.pop()) + " ms.");
            } catch (SQLException e) {
                t.trace("SQLException: " + e.getMessage());
                t.trace("SQLState: " + e.getSQLState());
                t.trace("VendorError: " + e.getErrorCode());
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                } // ignore

                stmt = null;
            }
        } else {
            t.trace("Can't get a connection to the database");
        }

        return success;
    }


    public ResultSet query(String sql, String region, String key)
    /**
     * ABSTRACT
      * Sends a query to the database and returns a ResultSet object.
     * Use size() and get(<<int>>) to extract a row from the result and
      * getItem(<<column name>>) to retrieve a value from a row.
     *
     * IN
     * key:int, sql:String
     * OUT
     * rs:ResultSet
     *
     * RETURN
     * fail: == null
     * success: != null
     *
     * PERFORMANCE_IMPACT
     *	General:High
     *	Frequency:Stress
     *	Cost:low
      */
    {
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace(key + ":" + sql);

        Statement stmt = null;
        ResultSet rs = null;
        Connection con;

        con = (Connection) connMap.get(key);
        if (con != null) {
            try {
                if (t.TRACE_TIMERS) t.timer.push(t.getTimer());
                stmt = con.createStatement();
                rs = stmt.executeQuery(sql);
                if (t.TRACE_TIMERS) t.timer.push(t.getTimer());

                // Show the time it took to parse the request
                if (t.TRACE_TIMERS) t.trace("DB Request took: " + (t.timer.pop() - t.timer.pop()) + " ms.");
            } catch (SQLException e) {
                t.trace("SQLException: " + e.getMessage());
                t.trace("SQLState: " + e.getSQLState());
                t.trace("VendorError: " + e.getErrorCode());
            }
/*
        if (stmt != null) {
		try {
			stmt.close();
		} catch (SQLException e) { } // ignore

		stmt = null;
		}
*/
        } else {
            t.trace("Can't get a connection to the database");
        }


        return rs;
        // rs.close()
    }

}
