package io.rawiron.farmgame.gamesettings;

import io.rawiron.farmgame.system.DataStore;

import java.sql.ResultSet;
import java.sql.SQLException;


public class DataGameSettings {
    private DataStore ds;

    // Cache
    public int cached_BasicDailyReward = 0;
    public int cached_PerFriendDailyReward = 0;
    public int cached_PerFriendDailyRewardLimit = 0;
    public String cached_StartingGift = null;

    public int cached_FuelLimit = 0;
    public int cached_FuelRefillSecondsPerUnit = 0;
    public int cached_LoadBalanceTechnique = 0;

    public float cached_CoinsGoldSaleMultiplier = 0;
    public float cached_AnimalSaleRatio = 0;
    public float cached_DecorationSaleRatio = 0;
    public float cached_BuildingSaleRatio = 0;


    public DataGameSettings(DataStore in_ds) {
        ds = in_ds;

        // read from DataStore
        ResultSet db_res_game = ds.query(
                " SELECT BasicDailyReward, FuelLimit, FuelRefillSecondsPerUnit, LoadBalanceTechnique, StartingGift "
                        + ", CoinsGoldSaleMuliplier, AnimalSaleRatio, DecorationSaleRatio, BuildingSaleRatio "
                        + ", PerFriendDailyReward, PerFriendDailyRewardLimit "
                        + " FROM GameSettings "
                , "read", null);

        try {
            if (db_res_game.next()) {
                cached_BasicDailyReward = db_res_game.getInt("BasicDailyReward");
                cached_FuelLimit = db_res_game.getInt("FuelLimit");
                cached_FuelRefillSecondsPerUnit = db_res_game.getInt("FuelRefillSecondsPerUnit");
                cached_LoadBalanceTechnique = db_res_game.getInt("LoadBalanceTechnique");
                cached_StartingGift = db_res_game.getString("StartingGift");

                cached_CoinsGoldSaleMultiplier = db_res_game.getFloat("CoinsGoldSaleMuliplier");
                cached_AnimalSaleRatio = db_res_game.getFloat("AnimalSaleRatio");
                cached_DecorationSaleRatio = db_res_game.getFloat("DecorationSaleRatio");
                cached_BuildingSaleRatio = db_res_game.getFloat("BuildingSaleRatio");

                cached_PerFriendDailyReward = db_res_game.getInt("PerFriendDailyReward");
                cached_PerFriendDailyRewardLimit = db_res_game.getInt("PerFriendDailyRewardLimit");
            }
        } catch (SQLException e) {
        }
    }

    public ResultSet retrieve(int in_dbgroup) {
        return ds.query("SELECT * FROM GameSettings", "read", null);
    }
}
