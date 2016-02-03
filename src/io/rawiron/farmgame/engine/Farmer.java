package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Farmer {

    private DataStore ds;
    private Logging l;
    private Trace t;

    private Valuable valuables;
    private Animal animal;
    private Decoration decoration;
    private Building building;
    private Storage storage;

    private DataGameSettings dataGameSettings;


    private static int techType = 0;
    private static final int techTypeMax = 3;


    public Farmer(Valuable in_v, DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
        valuables = in_v;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setAnimal(Animal in_a) {
        animal = in_a;
    }

    public void setDecoration(Decoration in_d) {
        decoration = in_d;
    }

    public void setBuilding(Building in_bd) {
        building = in_bd;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }


    public ResultSet retrieve(String in_facebookuser)
/**
 * read farmer's data
 *
 * PERFORMANCE_IMPACT
 *	General:low
 *	Frequency:few
 *	Cost:low
 */
    {
        String sql = " SELECT * FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'";
        ResultSet result = ds.query(sql, "write", in_facebookuser);

        return result;
    }

    public ResultSet retrieveIndex(String in_facebookuser) {
        String db_sql_read_FarmerIndex =
                " SELECT PlayerName, fi.PlayerID, Level, Gender, HairStyle, SkinTone, Clothing, LastPlayDate, DataBaseGroup "
                        + ", LastDailyReward, FacebookUser, CreateDate, 'match' AS KeyTest "
                        + ", IF ( FacebookUser IN (SELECT User FROM Administrators), 1, 0 ) AS Admin "
                        + ", ua.HairStyleUnique "
                        + " FROM FarmerIndex as fi LEFT JOIN UniqueAvatarList as ua on fi.PlayerID=ua.PlayerID "
                        + " WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        return ds.query(db_sql_read_FarmerIndex, "write", in_facebookuser);
    }

    public ResultSet retrieveSkey(String in_facebookuser, String in_skey) {
        String sql =
                " SELECT FacebookUser, PlayerID, " + "'" + in_skey + "****" + "'" + "+ skey AS KeyTest "
                        + " FROM FarmerIndex WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        ResultSet result = ds.query(sql, "write", in_facebookuser);
        return result;
    }

    public ResultSet getSafeFarmerData(String in_facebookuser) {
        return ds.query(" SELECT FarmerIndex.FacebookUser, FarmerIndex.Gender, FarmerIndex.HairStyle, FarmerIndex.SkinTone, FarmerIndex.Clothing "
                        + " FROM FarmerIndex WHERE FacebookUser=" + "'" + in_facebookuser + "'"
                , "write", in_facebookuser);
    }


    public int getPlayerID(String in_facebookuser) {
        ResultSet db_res_player = ds.query("SELECT PlayerID FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'"
                , "write", in_facebookuser);

        int playerID = 0;
        try {
            if (db_res_player.next()) {
                playerID = db_res_player.getInt("PlayerID");
            }
        } catch (SQLException e) {
        }

        return playerID;
    }

    public int getTimeMultiplier(String in_facebookuser) {
        ResultSet db_res_userSettings = ds.query("SELECT TimeMultiplier FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        int user_timeMultiplier = 1;
        try {
            if (db_res_userSettings.next()) {
                user_timeMultiplier = db_res_userSettings.getInt("TimeMultiplier");
            }
        } catch (SQLException e) {
        }

        return user_timeMultiplier;
    }

    public int getTimeMultiplier(String in_facebookuser, int in_playerID) {
        ResultSet db_res_userSettings = ds.query("SELECT TimeMultiplier FROM Farmers WHERE PlayerID=" + in_playerID
                , "read", in_facebookuser);

        int user_timeMultiplier = 1;
        try {
            if (db_res_userSettings.next()) {
                user_timeMultiplier = db_res_userSettings.getInt("TimeMultiplier");
            }
        } catch (SQLException e) {
        }

        return user_timeMultiplier;
    }

    public void updateAppearance(String in_facebookuser, String in_SkinTone, String in_Clothing, String in_HairStyle, String in_Gender) {
        ds.execute(" UPDATE FarmerIndex "
                        + " SET SkinTone=" + in_SkinTone + ", Clothing=" + in_Clothing + ", HairStyle=" + in_HairStyle + ", Gender='" + in_Gender + "'"
                        + " WHERE FacebookUser='" + in_facebookuser + "'"
                , "write", in_facebookuser);
    }


    public void updateTimeMultiplier(String in_facebookuser, int in_multiplier) {
        ds.execute(" UPDATE Farmers SET TimeMultiplier=" + in_multiplier
                + " WHERE FacebookUser=" + "'" + in_facebookuser + "'", "write", in_facebookuser);
    }


    public void updatePlayerSetting(String in_facebookuser, String in_setting, int in_value) {
        if (in_setting.equals("EffectsVolume")) {
            ds.execute("UPDATE Farmers SET EffectsVolume=" + in_value + " WHERE FacebookUser='" + in_facebookuser + "'", "write", in_facebookuser);
        } else if (in_setting.equals("MusicVolume")) {
            ds.execute("UPDATE Farmers SET MusicVolume=" + in_value + " WHERE FacebookUser='" + in_facebookuser + "'", "write", in_facebookuser);
        } else if (in_setting.equals("TutorialFlags")) {
            ds.execute("UPDATE Farmers SET TutorialFlags=" + in_value + " WHERE FacebookUser='" + in_facebookuser + "'", "write", in_facebookuser);
        } else if (in_setting.equals("GraphicsQuality")) {
            ds.execute("UPDATE Farmers SET GraphicsQuality=" + in_value + " WHERE FacebookUser='" + in_facebookuser + "'", "write", in_facebookuser);
        }
    }


    public int goldValue(String in_facebookuser, int in_subtract)
/**
 * IN
 *	params.userDBGroup
 *	params.user
 *	params.subtract
 * OUT
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 */
    {
        int gold = 0;
        int farmID = 0;
        ResultSet goldQuery;

        //
        //
        String db_sql_read_Farmers_gold = " SELECT Gold, PlayerID FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        goldQuery = ds.query(db_sql_read_Farmers_gold, "write", in_facebookuser);
        try {
            if (goldQuery.next()) {
                gold = (goldQuery.getInt("Gold")) - (in_subtract);
                farmID = goldQuery.getInt("PlayerID");
            }
        } catch (SQLException e) {
        }

        //
        //
        String db_sql_read_PlotList_gold = " SELECT SUM( Products.GoldValue ) * 10 AS Gold "
                + " FROM PlotList "
                + " INNER JOIN Unlockables ON Unlockables.Name=Contents "
                + " INNER JOIN Producers ON Unlockables.Name=Producers.Name "
                + " INNER JOIN Products on Produce=Products.Name "
                + " WHERE FarmID=" + farmID + " AND (Type='Plant' OR Type='Tree')";

        gold += storage.goldValue(in_facebookuser, farmID);

        goldQuery = ds.query(db_sql_read_PlotList_gold, "write", in_facebookuser);
        try {
            if (goldQuery.next()) gold = gold + goldQuery.getInt("Gold");
        } catch (SQLException e) {
        }


        //
        //
        String db_sql_read_Farmers_goldValue = " SELECT Coins * CoinsGoldExchange1 AS Gold "
                + " FROM Farmers INNER JOIN GameSettings WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        if (gold < 200) {
            goldQuery = ds.query(db_sql_read_Farmers_goldValue, "write", in_facebookuser);
            try {
                if (goldQuery.next()) gold = goldQuery.getInt("Gold") - (in_subtract);
            } catch (SQLException e) {
            }

            gold += building.goldValue(in_facebookuser, farmID);
            gold += animal.goldValue(in_facebookuser, farmID);
            gold += decoration.goldValue(in_facebookuser, farmID);
        }


        //
        if (gold < 10) {
            valuables.add(in_facebookuser, farmID, 0, 200, 0, 0, 0);
        }

        return gold;
    }


    public boolean create(String in_facebookuser, String in_userName, int in_farmID) {
        if (techType == 0) {
            techType = techTypeMax;
        }

        String db_sql_write_Farmers_new = " INSERT INTO Farmers "
                + " ( FacebookUser, PlayerName, PlayerID, TechType, TreasureX, TreasureY, Treasure ) "
                + " VALUES ( " + "'" + in_facebookuser + "'" + "," + "'" + in_userName + "'" + ", " + in_farmID
                + ", " + techType
                + ", FLOOR(RAND()*20), FLOOR(RAND()*21) "
                + ", ( SELECT Name FROM Unlockables WHERE Collection IS NOT NULL ORDER BY RAND() LIMIT 1 ) "
                + ")";
        ds.execute(db_sql_write_Farmers_new, "write", in_facebookuser);

        techType--;
        return true;
    }

    public boolean createIndex(String in_facebookuser, String in_userName, String in_skey) {
        String db_sql_write_FarmerIndex_new = " INSERT INTO FarmerIndex "
                + " ( FacebookUser, PlayerName, CreateDate, LastPlayDate, LastDailyReward, skey ) "
                + " VALUES ( " + "'" + in_facebookuser + "'" + "," + "'" + in_userName + "'" + ", Now(), Now(), Now(), " + ", '" + in_skey + "' )";
        ds.execute(db_sql_write_FarmerIndex_new, "write", in_facebookuser);

        return true;
    }

}
