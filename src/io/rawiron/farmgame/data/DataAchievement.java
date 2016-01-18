package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class DataAchievement {
    private DataStore ds;
    public HashMap<String, DataItemAchievement> cached = new HashMap<String, DataItemAchievement>();
    public HashMap<String, DataItemAchievement> cachedByUnlockAward = new HashMap<String, DataItemAchievement>();

    public DataAchievement(DataStore in_ds) {
        // read from DataStore
        String db_sql_read =
                " SELECT AchievementItem, Threshold, UnlockAwardName, TotalOrStored "
                        + " FROM Achievement ";

        DataItemAchievement achievement = null;
        ResultSet db_res = ds.query(db_sql_read, "read", null);
        try {
            while (db_res.next()) {
                achievement = new DataItemAchievement();
                achievement.name = db_res.getString("AchievementItem");
                achievement.unlockAwardName = db_res.getString("UnlockAwardName");
                achievement.threshold = db_res.getInt("Threshold");
                achievement.totalOrStored = db_res.getString("TotalOrStored");

                cached.put(db_res.getString("AchievementItem"), achievement);
                cachedByUnlockAward.put(db_res.getString("UnlockAwardName"), achievement);
            }
        } catch (SQLException e) {
        }
    }
}
