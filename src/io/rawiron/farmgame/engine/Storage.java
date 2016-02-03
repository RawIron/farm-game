package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataProduct;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Storage {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Valuable valuable;
    private DataProduct dataProduct;


    public Storage(Valuable in_v, DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
        valuable = in_v;
    }

    public void setDataProduct(DataProduct in_dpr) {
        dataProduct = in_dpr;
    }


    public boolean sell(String in_facebookuser, int in_farmID, String in_itemName, int in_quantity)
/**
 * ABSTRACT
 * swap(Pl,Vl)
 *
 * IN
 * params.userDBGroup
 * params.farmID
 * params.itemName
 * params.quantity
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 */
    {
        int quantity = 0;
        String contents = null;
        String db_sql_read_Storage =
                " SELECT s.Contents, s.Quantity "
                        + " FROM Storage as s "
                        + " WHERE s.Contents=" + "'" + in_itemName + "'" + " AND s.FarmID=" + in_farmID
                        + " AND s.Quantity>0 ";

        ResultSet db_res_stored = ds.query(db_sql_read_Storage, "write", in_facebookuser);
        try {
            if (db_res_stored.next()) {
                contents = db_res_stored.getString("Contents");
                quantity = db_res_stored.getInt("Quantity");
            }
        } catch (SQLException e) {
        }
        // exchange rate for swap

        int coins = 0;
        int gold = 0;
        coins = (in_quantity * dataProduct.cached.get(contents).coinsValue);
        gold = (in_quantity * dataProduct.cached.get(contents).goldValue);


        if ((quantity > 0) && (in_quantity > 0)) {
            if (quantity < in_quantity) {
                in_quantity = quantity;
                // BUG .. coins and gold are not reduced !!
            }

            // swap(Pl,Vl) := add(-Pl), add(Vl)
            this.add(in_facebookuser, in_farmID, in_itemName, -in_quantity);
            valuable.add(in_facebookuser, in_farmID, coins, gold, 0, 0, 0);


            if (l.log) {
                ds.execute("INSERT INTO log VALUES ( Now(), 'sellStorageItem','" + in_facebookuser + "'," + in_farmID + ",'" + in_itemName + "'," + gold + ", 'G',null," + in_quantity + " )"
                        , "log", in_facebookuser);
            }
        }

        return true;
    }

    public int goldValue(String in_facebookuser, int in_farmID) {
        String db_sql_read_Storage_gold =
                " SELECT SUM( Storage.Quantity * Products.GoldValue) AS Gold "
                        + " FROM Storage INNER JOIN Products ON Name=Contents WHERE FarmID=" + in_farmID + " AND Storage.Quantity>0";

        int gold = 0;
        ResultSet goldQuery = ds.query(db_sql_read_Storage_gold, "write", in_facebookuser);
        try {
            if (goldQuery.next()) gold = goldQuery.getInt("Gold");
        } catch (SQLException e) {
        }

        return gold;
    }


    public int add(String in_facebookuser, int in_farmID, String item, int num)
/**
 * ABSTRACT
 * add(Pl)
 * sub(Pl) := add(-Pl)
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:low
 */
    {
        int success = 0;

        success = ds.execute(
                " INSERT INTO Storage ( FarmID, Contents, Quantity, DateStored ) "
                        + " VALUES (" + in_farmID + ", " + "'" + item + "'" + ", FLOOR(" + num + "), Now() ) "
                        + " ON DUPLICATE KEY UPDATE Quantity=Quantity+ FLOOR(" + num + ")" + ",DateStored=Now() "
                , "write", in_facebookuser);

        return success;
    }


    public ResultSet retrieve(String in_facebookuser, int in_farmID)
/**
 * IN
 * params.userDBGroup
 * params.farmID
 */
    {
        ResultSet queryRes;

        String db_sql_read_Storage = " SELECT * FROM Storage WHERE FarmID=" + in_farmID + " AND Quantity>0 ";
        queryRes = ds.query(db_sql_read_Storage, "read", in_facebookuser);

        return queryRes;
    }

}
