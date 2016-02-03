package io.rawiron.farmgame.engine;

import java.sql.ResultSet;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Farm {

    private Trace t;
    private DataStore ds;
    private Logging l;

    public Farm(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
    }


    public ResultSet retrieve(String in_facebookuser) {
        return ds.query("SELECT * FROM Farms WHERE PlayerID=" + in_facebookuser, "read", in_facebookuser);
    }


    public ResultSet loadFarm(String in_facebookuser, String in_friendID) {
        if (l.log_data_read) ds.execute("INSERT INTO `log` "
                        + " VALUES ( Now(), 'loadFarm','" + in_facebookuser + "','0','" + in_friendID + "',0,'L',null,1)"
                , "log", null);

        return ds.query(" SELECT PlayerID, FacebookUser, DataBaseGroup "
                        + " FROM FarmerIndex "
                        + " WHERE FacebookUser=" + "'" + in_friendID + "'"
                , "read", in_facebookuser);
    }

    public boolean create(String in_facebookuser, int in_farmID) {
        String db_sql_write_Farms_new = "INSERT INTO Farms ( PlayerID ) VALUES ( " + in_farmID + " )";
        ds.execute(db_sql_write_Farms_new, "write", in_facebookuser);

        return true;
    }

}
