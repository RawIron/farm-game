package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Decoration {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Achievement achievement;
    private Valuable valuable;

    private DataUnlockable dataUnlockable;
    private DataGameSettings dataGameSettings;


    public Decoration(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setDataUnlockable(DataUnlockable in_dsu) {
        dataUnlockable = in_dsu;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }


    public int buy(String in_facebookuser, int in_farmID, int in_tilePointColumn, int in_tilePointRow, int in_slotID, String in_itemName) {
        if (dataUnlockable.cached.get(in_itemName).subtype != null && !dataUnlockable.cached.get(in_itemName).subtype.equals("")) {
            achievement.add(in_facebookuser, in_farmID, dataUnlockable.cached.get(in_itemName).subtype, 1);
        } else {
            achievement.add(in_facebookuser, in_farmID, in_itemName, 1);
        }
        this.add(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_slotID, in_itemName, 1);

        return 1;
    }


    public int sell(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_slotID) {
        String decoration = null;
        String db_sql_read_DecorationList_cost =
                " SELECT Decoration  "
                        + " FROM DecorationList "
                        + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y
                        + " AND Slot=" + in_slotID;

        ResultSet db_res_Decoration_cost = ds.query(db_sql_read_DecorationList_cost, "read", in_facebookuser);
        try {
            if (db_res_Decoration_cost.next()) {
                decoration = db_res_Decoration_cost.getString("Decoration");
            }
        } catch (SQLException e) {
        }


        int earn_amount_gold = 0;
        int cost_amount_coins = 0;
        earn_amount_gold = dataUnlockable.cached.get(decoration).goldCost;
        cost_amount_coins = dataUnlockable.cached.get(decoration).coinsCost;
        if (earn_amount_gold == 0) {
            earn_amount_gold = (int) (cost_amount_coins * dataGameSettings.cached_CoinsGoldSaleMultiplier);
        }
        earn_amount_gold = (int) (earn_amount_gold * dataGameSettings.cached_DecorationSaleRatio);


        valuable.add(in_facebookuser, in_farmID, 0, earn_amount_gold, 0, 0, 0);
        this.sub(in_facebookuser, in_farmID, in_X, in_Y, in_slotID, null, 1);

        return 1;
    }

    public int goldValue(String in_facebookuser, int in_farmID) {
        String db_sql_read_DecorationList_goldValue =
                " SELECT SUM( FLOOR( GoldCost * DecorationSaleRatio) ) AS Gold "
                        + " FROM DecorationList INNER JOIN Unlockables ON Name=Decoration INNER JOIN GameSettings WHERE FarmID=" + in_farmID;

        int gold = 0;
        ResultSet goldQuery = ds.query(db_sql_read_DecorationList_goldValue, "write", in_facebookuser);
        try {
            if (goldQuery.next()) gold = goldQuery.getInt("Gold");
        } catch (SQLException e) {
        }

        return gold;
    }


    public int count(String in_facebookuser, int in_farmID, String in_decoration) {
        String db_sql_read_DecorationList_buildings =
                " SELECT count(Decoration) AS NumDecorations "
                        + " FROM DecorationList "
                        + " WHERE FarmID=" + in_farmID + " AND Decoration=" + "'" + in_decoration + "'";

        int quantity = 0;
        ResultSet db_res_decoration = ds.query(db_sql_read_DecorationList_buildings, "read", in_facebookuser);
        try {
            if (db_res_decoration.next()) quantity = db_res_decoration.getInt("NumDecorations");
        } catch (SQLException e) {
        }

        return quantity;
    }


    public int sub(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_slot, String in_item, int in_amount)
    /**
        sub(Dl) := add(-Dl)
     */
    {
        if (t.verbose && (t.verbose_level >= 0) && (in_amount < 0))
            t.trace("assert failure in_amount=" + in_amount + " is <0");

        int success = this.add(in_facebookuser, in_farmID, in_X, in_Y, in_slot, in_item, -in_amount);
        return success;
    }


    public int add(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_slot, String in_item, int in_amount)
/**
 * add(Dl)
 *
 */
    {
        int success = 0;
        if (in_amount > 0) {
            success = ds.execute(" INSERT INTO DecorationList ( FarmID, Decoration, X, Y, Slot ) "
                            + " VALUES ( " + in_farmID + ", '" + in_item + "', " + in_X + ", " + in_Y + ", " + in_slot + " ) "
                            + " ON DUPLICATE KEY UPDATE Decoration=" + "'" + in_item + "'"
                    , "write", in_facebookuser);
        } else if (in_amount < 0) {
            success = ds.execute(" DELETE FROM DecorationList "
                            + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y + " AND Slot=" + in_slot
                    , "write", in_facebookuser);
        }

        return success;
    }


    public ResultSet retrieve(String in_facebookuser, int in_farmID) {
        String db_sql_read_DecorationList = "SELECT * FROM DecorationList WHERE FarmID=" + in_farmID;
        ResultSet queryRes = ds.query(db_sql_read_DecorationList, "read", in_facebookuser);

        return queryRes;
    }


    public int move(String in_facebookuser, int in_farmID, int in_tilePointColumn, int in_tilePointRow, int in_slot, int in_fromColumn, int in_fromRow, int in_fromSlot) {
        ds.execute(" UPDATE DecorationList SET X=" + in_tilePointColumn + ",Y=" + in_tilePointRow + ",Slot=" + in_slot
                        + " WHERE FarmID=" + in_farmID + " AND X=" + in_fromColumn + " AND Y=" + in_fromRow + " AND Slot=" + in_fromSlot
                , "write", in_facebookuser);
        return 1;
    }


}
