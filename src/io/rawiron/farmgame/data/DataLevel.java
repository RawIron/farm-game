package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import io.rawiron.farmgame.system.Trace;


public class DataLevel {
    private DataStore ds;
    private Trace t;
    public HashMap<Integer, DataItemLevel> cached = new HashMap<Integer, DataItemLevel>();

    public DataLevel(DataStore in_ds, Trace in_t) {
        ds = in_ds;
        t = in_t;

        ResultSet db_res_level = this.retrieve(0);
        try {
            while (db_res_level.next()) {
                DataItemLevel level = new DataItemLevel();
                level.level = db_res_level.getInt("Level");
                level.xpNeeded = db_res_level.getInt("XPNeeded");
                level.reward = db_res_level.getInt("Reward");

                cached.put(level.level, level);
            }
        } catch (SQLException e) {
            t.trace("SQLException: " + e.getMessage());
            t.trace("SQLState: " + e.getSQLState());
            t.trace("VendorError: " + e.getErrorCode());
        }
    }

    public ResultSet retrieve(int in_dbgroup) {
        return ds.query("SELECT * FROM Levels ORDER BY Level", "read", null);
    }
}
