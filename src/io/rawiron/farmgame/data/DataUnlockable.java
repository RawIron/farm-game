package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import io.rawiron.farmgame.system.Trace;


public class DataUnlockable {
    private DataStore ds;
    private Trace t;
    public HashMap<String, DataItemUnlockable> cached = new HashMap<String, DataItemUnlockable>();

    public DataUnlockable(DataStore in_ds, Trace in_t) {
        ds = in_ds;
        t = in_t;

        // read from DataStore
        DataItemUnlockable unlock = null;
        ResultSet db_res_unlockables = this.retrieve();
        try {
            while (db_res_unlockables.next()) {
                unlock = new DataItemUnlockable();
                unlock.type = db_res_unlockables.getString("Type");
                unlock.subtype = db_res_unlockables.getString("Subtype");
                unlock.collection = db_res_unlockables.getString("Collection");
                unlock.id = db_res_unlockables.getInt("ID");
                unlock.fuelCost = db_res_unlockables.getInt("FuelCost");
                unlock.coinsCost = db_res_unlockables.getInt("CoinsCost");
                unlock.coinsEarned = db_res_unlockables.getInt("CoinsEarned");
                unlock.goldCost = db_res_unlockables.getInt("GoldCost");
                unlock.xpEarned = db_res_unlockables.getInt("XPEarned");
                unlock.param1 = db_res_unlockables.getInt("Param1");

                cached.put(db_res_unlockables.getString("Name"), unlock);
            }
        } catch (SQLException e) {
            t.trace("SQLException: " + e.getMessage());
            t.trace("SQLState: " + e.getSQLState());
            t.trace("VendorError: " + e.getErrorCode());
        }
    }


    public ResultSet retrieve() {
        String db_sql_read_Unlockables =
                " SELECT ID, Name, Type, Subtype, Collection, FuelCost, CoinsCost, CoinsEarned, GoldCost, XPEarned, Param1 "
                        + " FROM Unlockables ";

        return ds.query(db_sql_read_Unlockables, "read", null);
    }
}
