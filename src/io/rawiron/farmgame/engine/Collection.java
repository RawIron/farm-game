package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Collection {

    private Trace t;
    private Logging l;
    private DataStore ds;
    private Reward reward;

    public Collection(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }


    public boolean test(String in_facebookuser, int in_farmID, String in_unlockableName)
/**
 * ABSTRACT
 * trigger(count(collection)==10): add(Rwl)
 *
 * IN
 * unlockableName:String
 * facebookuser:String
 * farmID:int
 *
 * RETURN
 * complete:Boolean
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 *
 */
    {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =Collection.testComplete=");

        boolean complete = false;

        String db_sql_read_UnlockablePairs_collection =
                " SELECT distinct(u.Collection) as Collection, if(Count(ItemID)=10,1,0) as check_true "
                        + " FROM UnlockablePairs INNER JOIN Unlockables as u ON u.ID=UnlockablePairs.Unlockable "
                        + " INNER JOIN Unlockables as c ON u.Collection=c.Collection "
                        + " WHERE FarmerID=" + "'" + in_facebookuser + "'" + " AND u.Name=" + "'" + in_unlockableName + "'";
        ResultSet countQuery = ds.query(db_sql_read_UnlockablePairs_collection, "write", in_facebookuser);

        try {
            if ((countQuery.next()) && (countQuery.getShort("check_true") == 1)) {
                // collection complete
                reward.give(countQuery.getString("Collection"), in_facebookuser, in_farmID);
                complete = true;
            }
        } catch (SQLException e) {
        }

        return complete;
    }


    public ResultSet retrieve(String in_facebookuser) {
        String sql = " SELECT * FROM Collections INNER JOIN RewardList ON Name=RewardName ";

        return ds.query(sql, "read", in_facebookuser);
    }


}
