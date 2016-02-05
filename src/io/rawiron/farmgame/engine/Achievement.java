package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataAchievement;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Achievement {

    private Trace t;
    private Logging l;
    private DataStore ds;
    private Reward reward;
    private Farmer farmer;

    private DataAchievement dataAchievement;
    private DataUnlockable dataUnlockable;


    public Achievement(DataStore dataStore, Logging logger, Trace tracer) {
        ds = dataStore;
        l = logger;
        t = tracer;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setDataAchievement(DataAchievement in_dav) {
        dataAchievement = in_dav;
    }

    public void setDataUnlockable(DataUnlockable in_du) {
        dataUnlockable = in_du;
    }


    public boolean test(String in_facebookuser, int in_farmID, String in_achievementName, char in_TotalOrStored)
    /**
     * guard(
     *    exist(Unlockable) and count(Achievement)>t
     *    exist(Unlockable) and count(Storage)>t
     * )
     * {	add(Reward) }
     *
     * @param farmDBGroup
     * @param user
     * @param farmID
     * @param achievementName
     * @param TotalOrStored:String
     *
     * @return Object.fullResult:Array
     *
     */
    {
        if (t.VERBOSE && (((Character) in_TotalOrStored == null)))
            t.trace("assert failure TotalOrStored=" + in_TotalOrStored + " invalid");
        if (t.VERBOSE && (in_TotalOrStored == 'S'))
            t.trace("assert failure TotalOrStored=" + in_TotalOrStored + " request for an achievement of type Stored");


        boolean success = false;

        String db_sql_read_Achievements =
                " SELECT min(check_true) as condition FROM ( "
                        + " SELECT if(count(*)=0,1,0) as check_true "
                        + " FROM UnlockablePairs as up "
                        + " WHERE up.Unlockable=" + dataUnlockable.cached.get(in_achievementName).id
                        + " AND up.FarmerID=" + "'" + in_facebookuser + "'"
                        + " UNION ALL "
                        + " SELECT count(*) as check_true "
                        + " FROM AchievementCounters as ac "
                        + " WHERE ac.AchievementItem=" + "'" + dataAchievement.cachedByUnlockAward.get(in_achievementName).name + "'"
                        + " AND ac.FarmerID=" + in_farmID
                        + " AND ac.Count >=" + dataAchievement.cachedByUnlockAward.get(in_achievementName).threshold
                        + " ) as requirement ";

        // always returns fullResult[]
        // no rows in Achievements where TotalOrStored='S'
        String db_sql_read_Achievements_s =
                " SELECT min(check_true) as condition FROM ( "
                        + " SELECT if(count(*)=0,1,0) as check_true "
                        + " FROM UnlockablePairs as up "
                        + " WHERE up.Unlockable=" + dataUnlockable.cached.get(in_achievementName).id
                        + " AND up.FarmerID=" + "'" + in_facebookuser + "'"
                        + " UNION ALL "
                        + " SELECT count(*) as check_true "
                        + " FROM Storage as s "
                        + " WHERE s.Contents=" + "'" + dataAchievement.cachedByUnlockAward.get(in_achievementName).name + "'"
                        + " AND s.FarmerID=" + in_farmID
                        + " AND s.Quantity >=" + dataAchievement.cachedByUnlockAward.get(in_achievementName).threshold + " AND s.Quantity>0 "
                        + " ) as requirement ";

        ResultSet result;
        if (in_TotalOrStored == 'T') {
            result = ds.query(db_sql_read_Achievements, "read", in_facebookuser);
        } else {
            result = ds.query(db_sql_read_Achievements_s, "read", in_facebookuser);
        }

        try {
            if (result.next())
            // not yet received the award
            // total is over the threshold
            // => give award
            {
                if (result.getInt("condition") > 0) {
                    reward.give(in_achievementName, in_facebookuser, in_farmID);
                    success = true;
                }
            }
        } catch (SQLException e) {
        }


        return success;
        // POST
        // result.fullResult[0]["UnlockAwardName"]==in_achievementName
        // result.fullResult[]
    }


    public int add(String in_facebookuser, int in_playerID, String in_item, int in_num)
    /**
     * add(Avl, num)
     *
     * @param playerID
     * @param item
     * @param num
     *
     * PRE
     * AchievementCounters has unique key (FarmerID,AchievementItem)
     */
    {
        return ds.execute("INSERT INTO AchievementCounters ( FarmerID, AchievementItem, Count ) "
                        + " VALUES (" + in_playerID + ",'" + in_item + "', FLOOR(" + in_num + ") ) "
                        + " ON DUPLICATE KEY UPDATE Count=Count+ FLOOR(" + in_num + ")"
                , "write", in_facebookuser);
    }


    public ResultSet retrieve(String in_facebookuser) {
        String sql =
                String.format(" SELECT Achievements.*, Unlockables.ID, UnlockablePairs.FarmerID AS Earned  " +
                        "FROM Achievements " +
                        "LEFT JOIN Unlockables ON (UnlockAwardName=Name)  " +
                        "LEFT JOIN UnlockablePairs ON (Unlockable=Unlockables.ID AND UnlockablePairs.FarmerID='%s')",
                        in_facebookuser);
        return ds.query(sql, "read", in_facebookuser);
    }

    public ResultSet retrieveList(String in_facebookuser, int in_farmID) {
        String sql = String.format("SELECT AchievementItem, Count FROM AchievementCounters WHERE FarmerID=%d", in_farmID);
        return ds.query(sql, "read", in_facebookuser);
    }

}
