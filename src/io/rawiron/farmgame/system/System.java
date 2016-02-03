package io.rawiron.farmgame.system;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataStore;


public class System {
    private Trace t;
    private DataStore ds;
    private DataGameSettings dataGameSettings;

    public System(DataStore in_ds, Trace in_t) {
        t = in_t;
        ds = in_ds;
    }

    private static final String db_sql_read_ExtraDatabases = " SELECT DBGroup FROM ExtraDatabases WHERE Purpose='ReadWrite' ORDER BY RAND() LIMIT 1 ";


    public int assignShard() {
        int loadBalance = dataGameSettings.cached_LoadBalanceTechnique;
        int dbShard = -1;
        if (loadBalance >= 0) {
            t.trace("databaseGroup=" + loadBalance);
            dbShard = loadBalance;
        } else if (loadBalance == -1) // Random.
        {
            ResultSet db_res_load = ds.query(db_sql_read_ExtraDatabases, "read", null);
            try {
                if (db_res_load.next()) {
                    dbShard = db_res_load.getInt("DBGroup");
                }
            } catch (SQLException e) {
            }
        }
        if (t.verbose) t.trace("Assigned to=" + dbShard);

        return dbShard;
    }

    public int getShard(String in_facebookuser) {
        String db_sql_read_FarmerIndex_exist =
                " SELECT DataBaseGroup "
                        + " FROM FarmerIndex WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        int dbShard = -1;
        ResultSet db_res_dbgroup = ds.query(db_sql_read_FarmerIndex_exist, "read", in_facebookuser);
        try {
            if (db_res_dbgroup.next()) {
                // shard where user data are stored
                dbShard = db_res_dbgroup.getInt("DataBaseGroup");
            }
        } catch (SQLException e) {
        }

        return dbShard;
    }

}
