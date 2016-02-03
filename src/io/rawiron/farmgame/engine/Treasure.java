package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Treasure {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Valuable valuable;
    private Farmer farmer;
    private Reward reward;


    public Treasure(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }


    public void check(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_tool) {

        String toolName = in_tool;
        if (in_tool.equals("TreasureHunt")) {
            toolName = "Coins";
        }

        ResultSet queryRes = ds.query(" SELECT Unlockables.*, ( FarmerID = FacebookUser) AS Owned "
                        + ", Collections.Name, Collections.GoldBoobyPrize, Collections.XPBoobyPrize, Collections.CoinsBoobyPrize "
                        + " FROM Farmers JOIN Unlockables ON '" + in_tool + "'=Unlockables.Name LEFT JOIN UnlockablePairs ON FarmerID=FacebookUser AND Unlockable=Unlockables.ID "
                        + " INNER JOIN Collections ON Collections.SearchTool = '" + in_tool + "' "
                        + " WHERE FacebookUser='" + in_facebookuser + "' AND TreasureX=" + in_X + " AND TreasureY=" + in_Y + ""
                , "read", in_facebookuser);
//		String queryRes = ds.query("SELECT Unlockables.*, ( FarmerID = FacebookUser) AS Owned, Collections.GoldBoobyPrize, Collections.XPBoobyPrize, Collections.CoinsBoobyPrize FROM Farmers JOIN Unlockables ON Treasure=Unlockables.Name LEFT JOIN UnlockablePairs ON FarmerID=FacebookUser AND Unlockable=Unlockables.ID JOIN Collections ON Collections.Name = Unlockables.Collection WHERE FacebookUser='" + in_facebookuser + "' AND TreasureX="+ in_x+" AND TreasureY="+ in_y+"", readonlyDBase[ in_facebookuserDBGroup ] );

        boolean treasureFound = false;
        int owned = 0;
        int amount_coins = 0;
        int amount_gold = 0;
        int amount_xp = 0;

        try {
            if (queryRes.next()) {
                owned = queryRes.getInt("Owned");
                amount_coins = queryRes.getInt("CoinsBoobyPrize");
                amount_gold = queryRes.getInt("GoldBoobyPrize");
                amount_xp = queryRes.getInt("XPBoobyPrize");

                treasureFound = true;
            }
        } catch (SQLException e) {
        }


        if ((treasureFound) && (in_tool.equals("TreasureHunt") || owned == 1)) // passing this means that a treasure was found.
        {
            ResultSet unlockRes = ds.query(" SELECT Unlockables.*, ( FarmerID = '" + in_facebookuser + "') AS Owned "
                            + " FROM Unlockables LEFT JOIN UnlockablePairs ON FarmerID='" + in_facebookuser + "' AND Unlockable=Unlockables.ID "
                            + " WHERE Collection=" + "'" + toolName + "'"
                            + " ORDER BY RAND() LIMIT 1 "
                    , "read", in_facebookuser);

            int unlock_owned = 0;
            int unlock_id = 0;
            String unlock_collection = null;
            String unlock_name = null;
            try {
                if (unlockRes.next()) {
                    unlock_owned = unlockRes.getInt("Owned");
                    unlock_id = unlockRes.getInt("ID");
                    unlock_collection = unlockRes.getString("Collection");
                    unlock_name = unlockRes.getString("Name");
                }
            } catch (SQLException e) {
            }

            if (unlock_owned != 1) {
                ds.execute(" INSERT INTO UnlockablePairs ( Unlockable, FarmerID ) VALUES ( " + unlock_id + ", '" + in_facebookuser + "' )"
                        , "write", in_facebookuser);

                /// Give new treasure
                ds.execute(" UPDATE Farmers SET TreasureX=FLOOR(RAND()*20), TreasureY=FLOOR(RAND()*21) "
                                + " WHERE FacebookUser='" + in_facebookuser + "'"
                        , "write", in_facebookuser);

                ResultSet countQuery = ds.query(" SELECT Count(ItemID) AS howMany "
                                + " FROM UnlockablePairs JOIN Unlockables ON Unlockables.ID=UnlockablePairs.Unlockable "
                                + " WHERE FarmerID ='" + in_facebookuser + "' AND Collection='" + unlock_collection + "'"
                        , "write", in_facebookuser);
                try {
                    if (countQuery.next() && countQuery.getInt("howMany") == 10) {
                        reward.give(unlock_collection, in_facebookuser, in_farmID);
                    }
                } catch (SQLException e) {
                }

                if (l.log) {
                    ds.execute(" INSERT INTO `log` VALUES ( Now(), 'FoundTreasure','" + in_facebookuser + "','0','" + unlock_name + "',0, 'G',null,1 )"
                            , "log", null);
                }

            } else {
                String treasure_X = " FLOOR(RAND()*20) ";
                String treasure_Y = " FLOOR(RAND()*21) ";
                String treasure_name = null;

                ResultSet db_res_treasure = ds.query(
                        " SELECT Name FROM Unlockables WHERE Collection IS NOT NULL ORDER BY RAND() LIMIT 1 ) "
                        , "write", in_facebookuser);
                try {
                    if (db_res_treasure.next())
                        treasure_name = db_res_treasure.getString("Name");
                } catch (SQLException e) {
                }


                this.add(in_facebookuser, in_farmID, treasure_name, treasure_X, treasure_Y, amount_coins, amount_gold);

                valuable.levelUp(amount_xp, in_facebookuser);


                if (l.log) {
                    ds.execute("INSERT INTO `log` VALUES ( Now(), 'FoundTreasure','" + in_facebookuser + "','0','Repeat'," + amount_gold + ", 'G',null,1 )"
                            , "log", null);
                }
            }

        }

/*
        response={};
		response.fullResult = {};
		response.fullResult.itemName = unlock.getItem("Name");
		response.fullResult.itemCollection = unlock.getItem("Collection");

		response={};
		response.fullResult = {};
		response.fullResult.itemName = unlock.getItem("Name");
		response.fullResult.itemCollection = unlock.getItem("Collection");
		response.fullResult.TreasureX = queryRes.getInt("TreasureX");
		response.fullResult.TreasureY = queryRes.getInt("TreasureY");
*/
    }


    public int add(String in_facebookuser, int in_farmerID, String in_treasure, String in_X, String in_Y, int in_coins, int in_gold)
/**
 * ABSTRACT
 * add(Tl)
 *
 * PERFORMANCE_IMPACT
 *	General:low
 *	Frequency:low
 *	Cost:low
 */
    {
        int success = 0;

        success = ds.execute(" UPDATE Farmers "
                        + " SET Coins=Coins+ FLOOR(" + in_coins + "), WeeklyCoinsPoints=WeeklyCoinsPoints+ABS(" + in_coins + "), AllTimeCoinsPoints=AllTimeCoinsPoints+ABS(" + in_coins + ")"
                        + ", Gold=Gold+ FLOOR(" + in_gold + ") "
                        + ", Treasure=" + "'" + in_treasure + "'"
                        + ", TreasureX=" + in_X
                        + ", TreasureY=" + in_Y
                        + " WHERE PlayerID=" + in_farmerID + " "
                , "write", in_facebookuser);

        return success;
    }

}
