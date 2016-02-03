package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Unlockable {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Achievement achievement;
    private Collection collection;
    private Valuable valuable;
    private Farmer farmer;
    private JerryCan jerryCan;

    private DataUnlockable dataUnlockable;
    private DataGameSettings dataGameSettings;


    public Unlockable(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setCollection(Collection in_c) {
        collection = in_c;
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


    public ResultSet getPlantStats() {
        String db_sql_read_Unlockables_plantStats = " SELECT Unlockables.Name AS Name, UnlockLevel, UnlockFriends, ProductionHours/3600 AS GrowthTime "
                + ", WitherTime/3600 AS WitherTime, Unlockables.GoldCost, GoldValue, 0 AS Specialization "
                + ", CONCAT( 'com.embassy.', ItemClass) AS PlantClassType, CONCAT( 'com.embassy.', IconClass) AS IconClass "
                + ", Producers.XPHarvestValue "
                + " FROM Unlockables INNER JOIN Producers ON Unlockables.Name=Producers.Name INNER JOIN Products ON Producers.Produce=Products.Name "
                + " WHERE Unlockables.Type='Plant' OR Unlockables.Type='Tree' ";

        return ds.query(db_sql_read_Unlockables_plantStats, "read", null);
    }


    public boolean buy(String in_facebookuser, String in_unlockName, byte in_useCoins)
/**
 * IN
 *	params.farmDBGroup
 *	params.userDBGroup
 *	params.user
 *	params.name
 * OUT
 */
    {
        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("enter function =BuyUnlockable=");
        boolean success = false;

        int playerID = 0;
        playerID = farmer.getPlayerID(in_facebookuser);

        boolean unlock = false;
        int unlockID = 0;
        String unlockType = null;
        String unlockSubtype = null;

        unlockID = dataUnlockable.cached.get(in_unlockName).id;
        unlockType = dataUnlockable.cached.get(in_unlockName).type;
        unlockSubtype = dataUnlockable.cached.get(in_unlockName).subtype;
        //if (unlockID == 0) {
        unlock = true;
        //}


        if (unlock) {
            success = payForUnlock(in_facebookuser, playerID, in_unlockName, in_useCoins);
            if (success) {
                this.add(in_facebookuser, unlockID, 1);

                if (!unlockSubtype.equals("")) {
                    achievement.add(in_facebookuser, playerID, unlockType, 1);
                } else if (!unlockType.equals("") && !unlockType.equals(unlockSubtype)) {
                    achievement.add(in_facebookuser, playerID, unlockSubtype, 1);
                }

                collection.test(in_facebookuser, playerID, in_unlockName);
            }
        }

        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("exit function =BuyUnlockable=");
        return success;
    }


    public boolean buyInventory(String in_facebookuser, String in_unlockName, byte in_useCoins) {
        boolean success = true;

        int playerID = 0;
        playerID = farmer.getPlayerID(in_facebookuser);

        String db_sql_read_Unlockables_name = " SELECT ID, Collection, Type, Subtype "
                + ", Fuel, FuelCost, CoinsCost, CoinsEarned, GoldCost, Param1, XPEarned "
                + " FROM Unlockables WHERE Name=" + "'" + in_unlockName + "'";


        boolean unlock = false;
        int unlockParam1 = 0;
        String unlockSubtype = null;

        ResultSet db_res_unlockable = ds.query(db_sql_read_Unlockables_name, "read", in_facebookuser);
        try {
            if (db_res_unlockable.next()) {
                unlockParam1 = db_res_unlockable.getInt("Param1");
                unlockSubtype = db_res_unlockable.getString("Subtype");

                unlock = true;
            }
        } catch (SQLException e) {
        }

        if (unlock) {
            success = payForUnlock(in_facebookuser, playerID, in_unlockName, in_useCoins);
            if (success) {
                if (unlockSubtype.equals("Fuel")) {
                    jerryCan.add(in_facebookuser, unlockParam1, 0);
                    achievement.add(in_facebookuser, playerID, in_unlockName, unlockParam1);
                }
            }
        }

        return success;
    }


    public boolean payForUnlock(String in_facebookuser, int in_farmID, String in_unlockName, byte in_useCoins)
/**
 * ABSTRACT
 *
 * IN
 * params.user
 * params.farmID
 * params.name
 * params.useCoins
 * unlockable.FuelCost
 * unlockable.CoinsCost
 * unlockable.CoinsEarned
 * unlockable.GoldCost
 * unlockable.XPEarned
 */
    {
        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("enter function =payForUnlock=");
        boolean result = false;


        int cost_amount_fuel = 0;
        int earned_amount_coins = 0;
        int cost_amount_gold = 0;
        int cost_amount_coins = 0;
        int earned_amount_xp = 0;

        cost_amount_fuel = dataUnlockable.cached.get(in_unlockName).fuelCost;
        cost_amount_coins = dataUnlockable.cached.get(in_unlockName).coinsCost;
        cost_amount_gold = dataUnlockable.cached.get(in_unlockName).goldCost;
        earned_amount_coins = dataUnlockable.cached.get(in_unlockName).coinsEarned;
        earned_amount_xp = dataUnlockable.cached.get(in_unlockName).xpEarned;


        if (in_useCoins == 1 && cost_amount_coins > 0) {

            String db_sql_read_Unlockable_payCondition =
                    " SELECT count(*) as check_true "
                            + " FROM Farmers "
                            + " WHERE 1=1 "
                            + " AND Coins>=" + cost_amount_coins
                            + " AND ( " + cost_amount_fuel + " <= Fuel OR " + cost_amount_fuel + " <= Fuel + floor(timestampdiff( second, LastFuelSave, Now() ) "
                            + " / " + dataGameSettings.cached_FuelRefillSecondsPerUnit
                            + ") ) "
                            + " AND FacebookUser=" + "'" + in_facebookuser + "'";

            ResultSet db_res_condition_pay = ds.query(db_sql_read_Unlockable_payCondition, "read", in_facebookuser);
            try {
                if ((db_res_condition_pay.next()) && (db_res_condition_pay.getInt("check_true") > 0)) {
                    valuable.add(in_facebookuser, in_farmID, -cost_amount_coins, 0, 0, 0, -cost_amount_fuel);
                    result = true;
                    valuable.levelUp(earned_amount_xp, in_facebookuser);
                }
            } catch (SQLException e) {
            }

            // LOG
            //
            if (l.log && result) {
                ds.execute("INSERT INTO `log` VALUES ( Now(), 'buyUnlockable','" + in_facebookuser + "','" + in_farmID + "','" + in_unlockName + "'," + cost_amount_coins + ", 'K',null,1 )"
                        , "log", null);
            }
        }

        //
        //
        else if (cost_amount_gold > 0 || cost_amount_coins == 0) {

            String db_sql_read_Unlockable_payCondition = " SELECT count(*) as check_true "
                    + " FROM Farmers "
                    + " WHERE 1=1 "
                    + " AND Gold >= " + cost_amount_gold
                    + " AND Coins >= -" + earned_amount_coins
                    + " AND ( " + cost_amount_fuel + " <= Fuel OR " + cost_amount_fuel + " <= Fuel + floor(timestampdiff( second, LastFuelSave, Now() ) "
                    + " / " + dataGameSettings.cached_FuelRefillSecondsPerUnit
                    + ") ) "
                    + " AND FacebookUser=" + "'" + in_facebookuser + "'";

            ResultSet db_res_condition_pay = ds.query(db_sql_read_Unlockable_payCondition, "read", in_facebookuser);
            try {
                if ((db_res_condition_pay.next()) && (db_res_condition_pay.getInt("check_true") > 0)) {
                    valuable.add(in_facebookuser, in_farmID, -earned_amount_coins, -cost_amount_gold, 0, 0, -cost_amount_fuel);
                    result = true;
                    valuable.levelUp(earned_amount_xp, in_facebookuser);
                }
            } catch (SQLException e) {
            }


            // LOG
            //
            if (l.log && result) {
                ds.execute("INSERT INTO `log` VALUES ( Now(), 'buyUnlockable','" + in_facebookuser + "','" + in_farmID + "','" + in_unlockName + "'," + cost_amount_gold + ", 'G',null,1 )"
                        , "log", null);
            }
        }

        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("exit function =payForUnlock= with result=" + result);

        return result;
    }


    public ResultSet retrieve(String in_facebookuser, String in_itemType, boolean in_locked, boolean in_unlocked
            , boolean in_coinsCost, boolean in_goldCost, String in_collectionName
    )
/**
 * ABSTRACT
 *
 * IN
 * params.user
 * params.coinsCost, params.goldCost
 * params.itemType
 * params.collection
 * params.unlocked
 * params.locked
 *
 */
    {
        ResultSet getRes = this.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked
                , in_coinsCost, in_goldCost, in_collectionName
        );
/*
        int i;
		var response = {};
		while ( getRes.next() )
		{
				response.fullResult = [];
				for ( i = 0; i < getRes.size(); i++ )
				{
						response.fullResult[i] = getRes.get(i);
				}
		}
		response.itemType = in_itemType;
		return response;
*/

        return getRes;
    }


    public ResultSet get(String in_facebookuser, String in_itemType, boolean in_locked, boolean in_unlocked
            , boolean in_coinsCost, boolean in_goldCost, String in_collectionName
    )
/**
 * IN
 * params.user
 * params.coinsCost, params.goldCost
 * params.itemType
 * params.collection
 * params.unlocked
 * params.locked
 *
 * OUT
 */
    {
        ResultSet result = null;

        //
        // built the query
        String coinsString = "";
        String goldString = "";
        String collectionString = "";
        String itemString = "";
        String selectString = "";
        String joinString = " INNER JOIN ( SELECT Level, FriendCount FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'" + " ) AS F";


        if (in_coinsCost) {
            coinsString = " AND CoinsCost>0";
        }
        if (in_goldCost) {
            goldString = " AND GoldCost>0";
        }
        if (in_collectionName != null) {
            collectionString = " AND Collection= " + "'" + in_collectionName + "'";
        }


        if (in_itemType != null) {
            if (in_itemType.equals("Recipe")) {
                selectString = ", RecipeList.* ";
                joinString = " INNER JOIN RecipeList ON RecipeList.Title=Unlockables.Name" + joinString;
            } else if (in_itemType.equals("Product")) {
                selectString = ", Products.GoldValue, Products.CoinsValue, Products.SWF";
                joinString = " INNER JOIN Products ON Products.Name=Unlockables.Name" + joinString;
                in_itemType = "Plant' OR Type='Product";
            } else if (in_itemType.equals("Tending")) {
                selectString = ", Buffs.GrowthMod, Buffs.DeathMod, Buffs.UsageRule";
                joinString = " LEFT JOIN Buffs ON Title=Unlockables.Name" + joinString;
            } else if (in_itemType.equals("Plant") || in_itemType.equals("Tree") || in_itemType.equals("Seeds")) {
                selectString = ", ProductionHours AS ProductionSeconds, WitherTime, XPHarvestValue, Producers.Produce, MysteryWeight, Producers.Repeatable "
                        + ", NeedTilled, Products.GoldValue, Products.CoinsValue, Products.SWF "
                        + ", DryYield, DryWitheredYield, WetYield, WetWitheredYield ";
                joinString = " INNER JOIN Producers ON Producers.Name=Unlockables.Name INNER JOIN Products ON Producers.Produce=Products.Name " + joinString;

                if (in_itemType.equals("Seeds")) {
                    in_itemType = "Plant' OR Type='Tree";
                }
            } else if (in_itemType.equals("Animal")) {
                selectString = ", ProductionHours AS ProductionSeconds, WitherTime, XPHarvestValue, Producers.Produce, Products.GoldValue, Products.CoinsValue, Products.SWF, DryYield ";
                joinString = " LEFT JOIN Producers ON Producers.Name=Unlockables.Name LEFT JOIN Products ON Producers.Produce=Products.Name " + joinString;
            } else if (in_itemType.equals("Building")) {
                selectString = ", ProductionHours AS ProductionSeconds, WitherTime, XPHarvestValue, Producers.Produce, Products.GoldValue, Products.CoinsValue, Products.SWF, DryYield ";
                joinString = " LEFT JOIN Producers ON Producers.Name=Unlockables.Name LEFT JOIN Products ON Producers.Produce=Products.Name " + joinString;
            }

            itemString = " AND (Type=" + "'" + in_itemType + "'" + ")";
        }


        //
        // fire the query
        if (!in_unlocked) // locked only
        {
            result = ds.query("SELECT Unlockables.* " + selectString
                    + " FROM Unlockables" + joinString
                    + " WHERE 1=1 " + coinsString + goldString + itemString + collectionString
                    + " AND ( Level < UnlockLevel OR FriendCount < UnlockFriends ) "
                    + " AND Unlockables.ID NOT IN ( SELECT Unlockable FROM UnlockablePairs WHERE FarmerID=" + "'" + in_facebookuser + "'" + ")", "read", in_facebookuser);
        } else if (!in_locked) //Unlocked only
        {
            result = ds.query("SELECT Unlockables.* " + selectString
                    + " FROM Unlockables LEFT JOIN UnlockablePairs ON ID=Unlockable" + joinString
                    + " WHERE 1=1 "
                    + coinsString + goldString + itemString + collectionString
                    + " AND ( ( UnlockType='level' AND Level >= UnlockLevel AND FriendCount >= UnlockFriends AND FarmerID IS NULL) "
                    + " OR FarmerID='" + in_facebookuser + "')", "read", in_facebookuser);
        } else // All
        {
            result = ds.query("SELECT Unlockables.*, IF ( FarmerID='" + in_facebookuser + "', 1, 0) AS Owned, ( Level>=UnlockLevel AND FriendCount >= UnlockFriends ) AS Unlocked"
                    + selectString
                    + " FROM Unlockables LEFT JOIN ( SELECT * FROM UnlockablePairs " + " WHERE FarmerID='" + in_facebookuser + "' ) AS U ON Unlockable=ID"
                    + joinString
                    + " WHERE 1=1 " + coinsString + goldString + itemString + collectionString, "read", in_facebookuser);
        }

        return result;
    }


    public int add(String in_facebookuser, int in_unlockable, int in_amount)
    /**
     * add(Ul)
     *
     * @return success:Boolean
     */
    {
        return ds.execute(
                " INSERT INTO UnlockablePairs (Unlockable, FarmerID) VALUES (" + in_unlockable + "," + "'" + in_facebookuser + "'" + " )"
                        + " ON DUPLICATE KEY UPDATE Unlockable=" + in_unlockable
                , "write", in_facebookuser);
    }

}
