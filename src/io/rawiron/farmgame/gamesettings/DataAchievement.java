package io.rawiron.farmgame.gamesettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class DataAchievement {
    public HashMap<String, DataItemAchievement> cached = new HashMap<String, DataItemAchievement>();
    public HashMap<String, DataItemAchievement> cachedByUnlockAward = new HashMap<String, DataItemAchievement>();

    public DataAchievement(DataStore engine) {
        String sql =
                " SELECT AchievementItem, Threshold, UnlockAwardName, TotalOrStored "
                        + " FROM Achievement ";

        ResultSet db_res = engine.query(sql, "read", null);
        try {
            while (db_res.next()) {
                DataItemAchievement achievement = new DataItemAchievement();
                achievement.name = db_res.getString("AchievementItem");
                achievement.unlockAwardName = db_res.getString("UnlockAwardName");
                achievement.threshold = db_res.getInt("Threshold");
                achievement.totalOrStored = db_res.getString("TotalOrStored");

                cached.put(achievement.name, achievement);
                cachedByUnlockAward.put(achievement.unlockAwardName, achievement);
            }
        } catch (SQLException e) {
        }
    }
}
