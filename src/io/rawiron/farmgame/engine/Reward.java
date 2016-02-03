package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Reward {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Valuable valuable;
    private Storage storage;
    private Unlockable unlockable;
    private Gift gift;

    private DataUnlockable dataUnlockable;


    public Reward(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setUnlockable(Unlockable in_u) {
        unlockable = in_u;
    }

    public void setGift(Gift in_g) {
        gift = in_g;
    }

    public void setDataUnlockable(DataUnlockable in_dsu) {
        dataUnlockable = in_dsu;
    }


    public int give(String rewardName, String in_facebookuser, int in_farmID)
/**
 * ABSTRACT
 * trigger(rewardName as  ):
 *
 * IN
 * rewardName:String
 * facebookuser:String
 * farmID:int
 * dbgroup:int
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 *
 */
    {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =giveReward=   rewardName=" + rewardName + "");

        // BUG .. OR Rl.ID=rewardName  .. JobList-Mastery uses RewardList.ID and NOT RewardList.RewardName
        String db_sql_read_RewardList =
                " SELECT Rl.CoinsReward, Rl.GoldReward, Rl.XPReward "
                        + ", Rl.StorageQuantity, Rl.StorageReward, Rl.UnlockableReward, Rl.GiftReward "
                        + " FROM RewardList as Rl "
                        + " WHERE Rl.RewardName=" + "'" + rewardName + "'"
                        + " OR Rl.ID=" + "'" + rewardName + "'";


        int reward_amount_coins = 0;
        int reward_amount_gold = 0;
        int reward_amount_xp = 0;
        int storageQuantity = 0;
        String storageReward = null;
        String unlockableReward = null;
        String giftReward = null;

        ResultSet db_res_Reward = ds.query(db_sql_read_RewardList, "read", in_facebookuser);
        try {
            if (db_res_Reward.next()) {
                reward_amount_coins = db_res_Reward.getInt("CoinsReward");
                reward_amount_gold = db_res_Reward.getInt("GoldReward");
                reward_amount_xp = db_res_Reward.getInt("XPReward");

                storageQuantity = db_res_Reward.getInt("StorageQuantity");
                storageReward = db_res_Reward.getString("StorageReward");
                unlockableReward = db_res_Reward.getString("UnlockableReward");
                giftReward = db_res_Reward.getString("GiftReward");
            }
        } catch (SQLException e) {
        }
        int unlockableID = 0;
        unlockableID = dataUnlockable.cached.get(unlockableReward).id;


        //
        // add(Vl)
        valuable.add(in_facebookuser, in_farmID, reward_amount_coins, reward_amount_gold, 0, 0, 0);
        valuable.levelUp(reward_amount_xp, in_facebookuser);


        //
        //
        if ((storageQuantity > 0) && (storageReward != null)) {
            // add(Pl)
            storage.add(in_facebookuser, in_farmID, storageReward, storageQuantity);
        }

        if (unlockableReward != null) {
            // add(Ul)
            unlockable.add(in_facebookuser, unlockableID, 1);
        }

        if (giftReward != null) {
            // add(Gl)
            gift.add(giftReward, "starterfarm", in_facebookuser);
        }


        if (l.log) {
            if (reward_amount_coins > 0) {
                ds.execute("INSERT INTO `log` VALUES ( Now(), 'reward','" + in_facebookuser + "','" + in_farmID + "','" + rewardName + "'," + reward_amount_coins + ", 'K',null,1 )", "log", null);
            }
            if (reward_amount_gold > 0) {
                ds.execute("INSERT INTO `log` VALUES ( Now(), 'reward','" + in_facebookuser + "','" + in_farmID + "','" + rewardName + "'," + reward_amount_gold + ", 'G',null,1 )", "log", null);
            }
        }

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =giveReward=");
        return 1;
    }


    public ResultSet retrieve() {
        String sql = "SELECT * FROM RewardList ";
        return ds.query(sql, "read", null);
    }

}
