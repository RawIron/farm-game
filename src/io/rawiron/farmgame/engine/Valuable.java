package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataLevel;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Valuable {

    private DataStore ds;
    private Trace t;
    private Logging l;
    private DataLevel dataLevel;

    // Cache
    private class coinsGold {
        public int coins;
        public int gold;
    }

    private coinsGold[] coins2Gold = new coinsGold[3];


    public Valuable(DataStore in_ds, Logging in_l, Trace in_t) {
        l = in_l;
        t = in_t;
        ds = in_ds;

        // read data into Cache
        ResultSet db_res_game = ds.query(
                " SELECT CoinsConvert1, CoinsConvert2, CoinsConvert3, CoinsGoldExchange1, CoinsGoldExchange2, CoinsGoldExchange3 "
                        + " FROM GameSettings ", "read", null);
        try {
            if (db_res_game.next()) {
                coins2Gold[0] = new coinsGold();
                coins2Gold[0].coins = db_res_game.getInt("CoinsConvert1");
                coins2Gold[0].gold = db_res_game.getInt("CoinsGoldExchange1");
                coins2Gold[1] = new coinsGold();
                coins2Gold[1].coins = db_res_game.getInt("CoinsConvert2");
                coins2Gold[1].gold = db_res_game.getInt("CoinsGoldExchange2");
                coins2Gold[2] = new coinsGold();
                coins2Gold[2].coins = db_res_game.getInt("CoinsConvert3");
                coins2Gold[2].gold = db_res_game.getInt("CoinsGoldExchange3");
            }
        } catch (SQLException e) {
        }
    }

    public void setDataLevel(DataLevel in_dsl) {
        dataLevel = in_dsl;
    }


    public int add(String in_facebookuser, int in_farmerID, int in_coins, int in_gold, int in_xp, int in_level, int in_fuel)
    /**
     * ABSTRACT
     * add(Vl)
     *
     * PERFORMANCE_IMPACT
     *	General:high
     *	Frequency:stress
     *	Cost:low
     */
    {
        int success = 0;

        if ((in_coins != 0) || (in_gold != 0) || (in_xp != 0) || (in_level != 0) || (in_fuel != 0)) {
            success = ds.execute(" UPDATE Farmers "
                            + " SET Coins=Coins+ FLOOR(" + in_coins + "), WeeklyCoinsPoints=WeeklyCoinsPoints+ABS(" + in_coins + "), AllTimeCoinsPoints=AllTimeCoinsPoints+ABS(" + in_coins + ")"
                            + ", Gold=Gold+ FLOOR(" + in_gold + ") "
                            + ", Experience=Experience+ FLOOR(" + in_xp + ") "
                            + ", Level=Level+ FLOOR(" + in_level + ") "
                            + ", Fuel=Fuel+ FLOOR(" + in_fuel + ") "
                            + " WHERE PlayerID=" + in_farmerID + " "
                    , "write", in_facebookuser);
        }

        return success;
    }


    public boolean test(String in_facebookuser, String in_costItem, char in_currency) {
        boolean precondition_task = false;

        if (t.verbose && (t.verbose_level >= 3)) t.trace("variable dump currency=" + in_currency);

        // one of the conditions must be met
        String db_sql_read_Cost_condition = " SELECT max(requirement) as check_true FROM ( "
                + " SELECT  count(*) as requirement "
                + " FROM Unlockables as u CROSS JOIN Farmers as f ON f.facebookuser=" + "'" + in_facebookuser + "'"
                + " WHERE u.Name=" + "'" + in_costItem + "'"
                + " AND 'K'=" + in_currency
                + " AND f.Coins>=u.CoinsCost AND f.Fuel>=u.FuelCost AND u.CoinsCost > 0 "
                + " UNION ALL "
                + " SELECT  count(*) as requirement "
                + " FROM Unlockables as u CROSS JOIN Farmers as f ON f.facebookuser=" + "'" + in_facebookuser + "'"
                + " WHERE u.Name=" + "'" + in_costItem + "'"
                + " AND 'K' !=" + in_currency
                + " AND IF (u.GoldCost=0 AND u.CoinsCost>0, f.Coins>=u.CoinsCost AND f.Fuel>=u.FuelCost, f.Gold>=u.GoldCost AND f.Coins>= -u.CoinsEarned AND f.Fuel>=u.FuelCost )"
                + ") as condition_check ";


        ResultSet db_res_cost_condition = ds.query(db_sql_read_Cost_condition, "write", in_facebookuser);
        try {
            if ((db_res_cost_condition.next()) && (db_res_cost_condition.getByte("check_true") > 0)) {
                precondition_task = true;
            }
        } catch (SQLException e) {
        }
        // POST
        // precondition_task

        return precondition_task;
    }


    public int levelUp(int XP_earned, String in_facebookuser)
    /**
     * ABSTRACT
     * swap(Vl(xp)+xp_earned, (Vl(l),Vl(k)) )
     *
     * IN
     *	in_dbgroup:int
     *	in_facbookuser:String
     * 	XP_earned:int
     * OUT
     *	result:Boolean
     *
     * PERFORMANCE_IMPACT
     *	General:high
     *	Frequency:stress
     *	Cost:high
     */
    {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =levelUpHandler=" + XP_earned);
        // ASSERT
        //
        // XP_earned > 0
        if (t.verbose && (t.verbose_level >= 0) && (XP_earned <= 0))
            t.trace("assert failure XP_earned=" + XP_earned + " <=0");


        // DEFINE
        //
        int result = 0;
        int levelUpCounter;
        boolean levelUp = false;
        int experience_sum = 0;
        int level = 0;
        int XPNeeded = 0;
        int reward_amount_coins = 0;

        String db_sql_read_Farmers_XP =
                " SELECT f.Experience, f.Level "
                        + " FROM Farmers as f "
                        + " WHERE f.FacebookUser=" + "'" + in_facebookuser + "'";


        if ((XP_earned) > 0) {
            ResultSet farmer = ds.query(db_sql_read_Farmers_XP, "write", in_facebookuser);
            try {
                level = farmer.getInt("Level");
                experience_sum = (farmer.getInt("Experience")) + (XP_earned);
            } catch (SQLException e) {
            }
            ;
            XP_earned = 0;

            level++;
            reward_amount_coins = dataLevel.cached.get(level).reward;
            XPNeeded = dataLevel.cached.get(level).xpNeeded;
            levelUp = (experience_sum >= XPNeeded);
            if (t.verbose && (XP_earned == 0) && (levelUp))
                t.trace("assert failure XP_earned=" + XP_earned + " and levelUp is" + levelUp);
            if (t.verbose && (t.verbose_level >= 3))
                t.trace("variable dump = " + experience_sum + " " + XPNeeded + " " + levelUp);


            levelUpCounter = 0;
            while (levelUp) {
                if (t.verbose && (t.verbose_level >= 3))
                    t.trace("variable dump = " + experience_sum + " " + XPNeeded + " " + (experience_sum - XPNeeded) + " " + levelUp);

                experience_sum -= XPNeeded;
                result = ds.execute("UPDATE Farmers "
                        + " SET Experience=" + experience_sum
                        + ", Level=Level+1 "
                        + ", Coins=Coins+" + reward_amount_coins + ", WeeklyCoinsPoints=WeeklyCoinsPoints+" + reward_amount_coins + ", AllTimeCoinsPoints=AllTimeCoinsPoints+" + reward_amount_coins
                        + " WHERE 1=1 "
                        + " AND FacebookUser=" + "'" + in_facebookuser + "'", "write", in_facebookuser);
                result = ds.execute("UPDATE Session SET Level=Level+1 WHERE FacebookUser=" + "'" + in_facebookuser + "'", "write", in_facebookuser);

                level++;
                reward_amount_coins = dataLevel.cached.get(level).reward;
                XPNeeded = dataLevel.cached.get(level).xpNeeded;
                levelUp = (experience_sum >= XPNeeded);

                levelUpCounter++;
            }

            if (levelUpCounter == 0) {
                result = ds.execute("UPDATE Farmers "
                        + " SET Experience=" + experience_sum
                        + " WHERE 1=1 "
                        + " AND FacebookUser=" + "'" + in_facebookuser + "'", "write", in_facebookuser);
            }
        }

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =levelUpHandler");
        return result;
    }


    public void convertCoinsToGold(String in_facebookuser, int in_farmID, int in_howMuch)
    /**
     * IN
     *	params.userDBGroup:String
     *	params.user:String
     *	params.farmID:int
     *	params.howMuch:int
     * OUT
     *
     * PERFORMANCE_IMPACT
     *	General:medium
     *	Frequency:many
     *	Cost:low
     */
    {
        int cost_amount_coins = coins2Gold[in_howMuch - 1].coins;
        int earn_amount_gold = coins2Gold[in_howMuch - 1].gold;

        ResultSet db_res_Coins_condition = ds.query(" SELECT Coins FROM Farmers WHERE PlayerID=" + in_farmID, "write", in_facebookuser);
        try {
            if ((db_res_Coins_condition.next()) && (db_res_Coins_condition.getInt("Coins") >= cost_amount_coins)) {
                this.add(in_facebookuser, in_farmID, -cost_amount_coins, earn_amount_gold, 0, 0, 0);
            }
        } catch (SQLException e) {
        }

        // LOG
        //
        if (l.log) {
            ds.execute("INSERT INTO `log` VALUES ( Now(), 'convertCoinsToGold','" + in_facebookuser + "','" + in_farmID + "','" + in_howMuch + "'," + cost_amount_coins + ", 'K',null,1 )", "log", null);
        }
    }
}
