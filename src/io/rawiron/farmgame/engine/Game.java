package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataProducer;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.System;
import io.rawiron.farmgame.system.Trace;


public class Game {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Valuable valuable;
    private PlotList plotList;
    private Farmer farmer;
    private Farm farm;
    private Friend friend;
    private Gift gift;

    private Transaction transaction;
    private System system;

    private DataUnlockable dataUnlockable;
    private DataProducer dataProducer;
    private DataGameSettings dataGameSettings;


    public Game(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setPlotList(PlotList in_pl) {
        plotList = in_pl;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setFarm(Farm in_fm) {
        farm = in_fm;
    }

    public void setFriend(Friend in_fr) {
        friend = in_fr;
    }

    public void setGift(Gift in_g) {
        gift = in_g;
    }

    public void setTransaction(Transaction in_tx) {
        transaction = in_tx;
    }

    public void setSystem(System in_sys) {
        system = in_sys;
    }

    public void setDataUnlockable(DataUnlockable in_dsu) {
        dataUnlockable = in_dsu;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }


    public int login(String in_facebookuser, String in_userName, String in_skey, BalanceSheet inout_gold, BalanceSheet inout_coins)
/**
 * ABSTRACT
 *
 *
 * IN
 * params.userDBGroup:int
 * params.user:String
 * params.userName:String
 * params.skey:String
 *
 * OUT
 *
 *
 * PERFORMANCE_IMPACT
 *	General:Low
 *	Frequency:few
 *	Cost:high
 *
 */
    {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =game.login=" + in_facebookuser);

        String db_sql_read_FarmerIndex =
                " SELECT PlayerID, skey, (DAY(LastDailyReward) = DAY(Now())) AS SameDay "
                        + " FROM FarmerIndex WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        boolean gotSkey = false;
        String db_skey = null;

        boolean userExists = false;
        int ID = -1;
        byte sameDay = 0;

        ResultSet db_res_index = ds.query(db_sql_read_FarmerIndex, "read", in_facebookuser);
        try {
            if (db_res_index.next()) {
                db_skey = db_res_index.getString("skey");
                gotSkey = true;

                ID = db_res_index.getInt("PlayerID");
                sameDay = db_res_index.getByte("SameDay");
                userExists = true;
            }
        } catch (SQLException e) {
        }

        if (gotSkey && (db_skey != null && !db_skey.equals("") && !db_skey.equals(in_skey))) {
            return 2;
        }


        int rc = -1;

        String db_sql_read_UnlockablePairs_Gold = " SELECT SUM(numCollected) AS Total "
                + " FROM ( SELECT Collection, if (Count(*) =10, Effect, 0) AS NumCollected "
                + " FROM UnlockablePairs INNER JOIN Unlockables ON Unlockable=ID INNER JOIN Collections ON Collections.Name=Collection "
                + " WHERE FarmerID=" + "'" + in_facebookuser + "'" + " AND Collection IS NOT NULL GROUP BY Collection ) AS a";

        String db_sql_read = "SELECT SUM(DailyGoldReward) AS TotalGoldReward, SUM(DailyCoinsReward) AS TotalCoinsReward, Type FROM ("
                + " SELECT if(DailyGoldReward>0, DailyGoldReward, 0) AS DailyGoldReward, if(DailyCoinsReward>0, DailyCoinsReward, 0) AS DailyCoinsReward, Type "
                + " FROM PlotList INNER JOIN Unlockables ON Name = Contents "
                + " WHERE FarmID=" + ID
                + " UNION ALL SELECT if(DailyGoldReward>0, DailyGoldReward, 0) AS DailyGoldReward, if(DailyCoinsReward>0, DailyCoinsReward, 0) AS DailyCoinsReward, Type "
                + " FROM AnimalList INNER JOIN Unlockables ON Name = Animal "
                + " WHERE FarmID = " + ID
                + " UNION ALL SELECT if(DailyGoldReward>0, DailyGoldReward, 0) AS DailyGoldReward, if(DailyCoinsReward>0, DailyCoinsReward, 0) AS DailyCoinsReward, Type "
                + " FROM UnlockablePairs INNER JOIN Unlockables ON Unlockable = ID "
                + " WHERE FarmerID =" + "'" + in_facebookuser + "'"
                + " UNION ALL SELECT if(DailyGoldReward>0, DailyGoldReward, 0) AS DailyGoldReward, if(DailyCoinsReward>0, DailyCoinsReward, 0) AS DailyCoinsReward, Type "
                + " FROM DecorationList INNER JOIN Unlockables ON Name = Decoration "
                + " WHERE FarmID = " + ID
                + " ) AS A GROUP BY Type ";


        if (userExists) {
            // EXISTING USER

            if (sameDay != 1) {
                //
                // GOLD

                // calculate daily gold reward
                inout_gold.daily = dataGameSettings.cached_BasicDailyReward;

                // count collection gold
                ResultSet queryResult = ds.query(db_sql_read_UnlockablePairs_Gold, "read", in_facebookuser);
                try {
                    if (queryResult.next()) {
                        inout_gold.collection = queryResult.getInt("Total");
                    }
                } catch (SQLException e) {
                }


                //calculate daily gold awarded to players for having N number of friends
                int FriendCount = 0;
                FriendCount = friend.count(in_facebookuser);

                if (FriendCount > dataGameSettings.cached_PerFriendDailyRewardLimit)
                    FriendCount = dataGameSettings.cached_PerFriendDailyRewardLimit;
                inout_gold.friend = dataGameSettings.cached_PerFriendDailyReward * FriendCount;


                // count other gold
                String type = null;
                queryResult = ds.query(db_sql_read, "read", in_facebookuser);
                try {
                    while (queryResult.next()) {
                        type = queryResult.getString("Type");
                        if (type.equals("Building")) {
                            inout_gold.building = queryResult.getInt("TotalGoldReward");
                            inout_coins.building = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Contraption")) {
                            inout_gold.contraption = queryResult.getInt("TotalGoldReward");
                            inout_coins.contraption = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Protection")) {
                            inout_gold.protection = queryResult.getInt("TotalGoldReward");
                            inout_coins.protection = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Animal")) {
                            inout_gold.animal = queryResult.getInt("TotalGoldReward");
                            inout_coins.animal = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Decoration")) {
                            inout_gold.decoration = queryResult.getInt("TotalGoldReward");
                            inout_coins.decoration = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Avatar")) {
                            inout_gold.clothing = queryResult.getInt("TotalGoldReward");
                            inout_coins.clothing = queryResult.getInt("TotalCoinsReward");
                        } else if (type.equals("Land")) {
                            inout_gold.land = queryResult.getInt("TotalGoldReward");
                            inout_coins.land = queryResult.getInt("TotalCoinsReward");
                        }
                        if (t.verbose) t.trace(type);
                    }
                } catch (SQLException e) {
                }
                inout_gold.earned = inout_gold.daily + inout_gold.collection + inout_gold.friend + inout_gold.building
                        + inout_gold.contraption + inout_gold.protection
                        + inout_gold.animal + inout_gold.decoration + inout_gold.clothing + inout_gold.land;

                inout_coins.earned = inout_coins.daily + inout_coins.collection + inout_coins.building
                        + inout_coins.contraption + inout_coins.protection
                        + inout_coins.animal + inout_coins.decoration + inout_coins.clothing + inout_coins.land;


            }
            if (t.verbose && (t.verbose_level >= 3))
                t.trace("day:" + inout_gold.daily + " coll:" + inout_gold.collection + " friend:" + inout_gold.friend
                        + " build:" + inout_gold.building
                        + " contraption:" + inout_gold.contraption + " protection:" + inout_gold.protection
                        + " animal:" + inout_gold.animal + " decor:" + inout_gold.decoration + " land:" + inout_gold.land);
            if (t.verbose && (t.verbose_level >= 3))
                t.trace("day:" + inout_coins.daily + " coll:" + inout_coins.collection
                        + " build:" + inout_coins.building
                        + " contraption:" + inout_coins.contraption + " protection:" + inout_coins.protection
                        + " animal:" + inout_coins.animal + " decor:" + inout_coins.decoration + " land:" + inout_coins.land);
            // POST
            // goldEarned, coinsEarned


            // update Player status
            //
            String db_sql_write_FarmerIndex_login = "UPDATE FarmerIndex SET LastPlayDate=Now()," + "LastDailyReward = Now()," + "PlayerName=" + "'" + in_userName + "'"
                    + " WHERE FacebookUser=" + "'" + in_facebookuser + "'";

            String db_sql_write_Farmers = "UPDATE Farmers "
                    + " SET Fuel = if ("
                    + " Fuel < " + dataGameSettings.cached_FuelLimit
                    + " && (@f:= Fuel + floor(timestampdiff( second, LastFuelSave, Now() ) / " + dataGameSettings.cached_FuelRefillSecondsPerUnit + ") ) "
                    + " < " + dataGameSettings.cached_FuelLimit
                    + ", @f, " + dataGameSettings.cached_FuelLimit
                    + ")"
                    + ", PlayerName=" + "'" + in_userName + "'"
                    + ", Gold=Gold+" + inout_gold.earned + ", Coins=Coins+" + inout_coins.earned + " "
                    + " , WeeklyCoinsPoints=WeeklyCoinsPoints+" + inout_coins.earned + ", AllTimeCoinsPoints=AllTimeCoinsPoints+" + inout_coins.earned + ", LastFuelSave=Now() "
                    + " WHERE FacebookUser=" + "'" + in_facebookuser + "'";

            ds.execute(db_sql_write_FarmerIndex_login, "write", in_facebookuser);
            ds.execute(db_sql_write_Farmers, "write", in_facebookuser);

            rc = 1;
        } else {
            // NEW USER
            // create entries .. built starter farm

            if (in_skey == null || in_skey.equals("null")) {
                in_skey = "";
            }

            farmer.createIndex(in_facebookuser, in_userName, in_skey);
            int farmID = 0;
            farmID = farmer.getPlayerID(in_facebookuser);

            farmer.create(in_facebookuser, in_userName, farmID);

            farm.create(in_facebookuser, farmID);

            if (!dataGameSettings.cached_StartingGift.equals("")) {
                gift.add(dataGameSettings.cached_StartingGift, "starterfarm", in_facebookuser);
            }

            plotList.create(in_facebookuser, farmID);

            rc = 0;
        }

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =game.login=" + rc);
        return rc;
    }


    public int test(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_costItem, int in_useCoins, String in_plantState)
/**
 * ABSTRACT
 *
 *
 * IN
 * params.userDBGroup:int
 * params.user:String
 * params.userName:String
 * params.taskClassName:String
 * params.useCoins:Boolean
 * params.farmID:int
 * params.X:int
 * params.Y:int
 * params.itemName:String
 * params.animalID:int
 * params.plantState:String
 * params.vitality:int
 * params.mystery:Boolean
 * params.friend:String
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 *
 */
    {
        int result = -1;

        char currency = 'G';
        Integer useYourCoins = in_useCoins;
        if (useYourCoins == null) {
            useYourCoins = 0;
        }
        if (useYourCoins == 1) {
            currency = 'K';
        }

        boolean precondition_pay = false;
        precondition_pay = transaction.enter(in_facebookuser, in_costItem, currency);

        if (precondition_pay) {
            int earned_amount_xp = dataUnlockable.cached.get(in_costItem).xpEarned;
            int XP_earned = 0;

            XP_earned = plotList.xpEarned(in_facebookuser, in_farmID, in_X, in_Y, in_plantState);
            if (XP_earned == 0) {
                XP_earned = earned_amount_xp;
            }

            transaction.leave(in_facebookuser, in_costItem, currency, XP_earned);

            result = 1;
        }
        else {
            if (Trace.verbose && (Trace.verbose_level >= 0)) t.trace("assert failure - Not enough of something");
            result = 2;
        }

        return result;
    }


    private void handleTaskLog(String in_facebookuser, int in_farmID, String in_task, String in_itemName, int in_cost, char in_currency, int in_useYourCoins) {
        ds.execute("INSERT INTO log VALUES ( Now(), '" + in_task + "','" + in_facebookuser + "','" + in_farmID + "','" + in_itemName + "'," + in_cost + ", '" + in_currency + "',null,1 )"
                , "log", null);
    }

}
