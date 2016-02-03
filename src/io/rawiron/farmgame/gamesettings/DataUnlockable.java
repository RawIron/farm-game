package io.rawiron.farmgame.gamesettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;


public class DataUnlockable {
    private DataStore ds;
    private Trace t;
    public HashMap<String, DataItemUnlockable> cached = new HashMap<String, DataItemUnlockable>();

    public DataUnlockable(DataStore in_ds, Trace in_t) {
        ds = in_ds;
        t = in_t;

        ResultSet db_res = this.retrieve();
        try {
            while (db_res.next()) {
                DataItemUnlockable unlock = new DataItemUnlockable();
                unlock.name = db_res.getString("Name");
                unlock.type = db_res.getString("Type");
                unlock.subtype = db_res.getString("Subtype");
                unlock.collection = db_res.getString("Collection");
                unlock.id = db_res.getInt("ID");
                unlock.fuelCost = db_res.getInt("FuelCost");
                unlock.coinsCost = db_res.getInt("CoinsCost");
                unlock.coinsEarned = db_res.getInt("CoinsEarned");
                unlock.goldCost = db_res.getInt("GoldCost");
                unlock.xpEarned = db_res.getInt("XPEarned");
                unlock.param1 = db_res.getInt("Param1");

                cached.put(unlock.name, unlock);
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
