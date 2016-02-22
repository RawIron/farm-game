package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.rawiron.farmgame.system.*;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import org.joda.time.LocalDateTime;


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


    /**
     * trigger(rewardName as  ):
     *
     * @param rewardName:String
     * @param facebookuser:String
     * @param farmID:int
     * @param dbgroup:int
     *
     */
    public int give(String rewardName, String in_facebookuser, int in_farmID)
    {
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 4)) t.trace("enter function =giveReward=   rewardName=" + rewardName + "");

        // BUG .. OR ID=rewardName  .. JobList-Mastery uses RewardList.ID and NOT RewardList.RewardName
        List<RewardItem> items = getRewardItems(
                " SELECT CoinsReward, GoldReward, XPReward "
                        + ", StorageQuantity, StorageReward, UnlockableReward, GiftReward "
                        + " FROM RewardList as Rl "
                        + " WHERE RewardName=" + "'" + rewardName + "'"
                        + " OR ID=" + "'" + rewardName + "'"
                );

        if (items.isEmpty()) {
            return 0;
        }

        RewardItem item = items.get(0);

        int unlockableID = 0;
        unlockableID = dataUnlockable.cached.get(item.unlockableReward).id;


        valuable.add(in_facebookuser, in_farmID, item.coinsReward, item.goldReward, 0, 0, 0);
        valuable.levelUp(item.xpReward, in_facebookuser);


        if ((item.storageQuantity > 0) && (item.storageReward != null)) {
            storage.add(in_facebookuser, in_farmID, item.storageReward, item.storageQuantity);
        }

        if (item.unlockableReward != null) {
            unlockable.add(in_facebookuser, unlockableID, 1);
        }

        if (item.giftReward != null) {
            gift.add(item.giftReward, "starterfarm", in_facebookuser);
        }


        if (item.coinsReward > 0) {
            new RewardEvent(
                    UtcDateTime.now(),
                    in_facebookuser,
                    in_farmID,
                    rewardName,
                    "coins",
                    item.coinsReward
            ).track();
        }
        if (item.goldReward > 0) {
            new RewardEvent(
                    UtcDateTime.now(),
                    in_facebookuser,
                    in_farmID,
                    rewardName,
                    "gold",
                    item.goldReward
            ).track();
        }

        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 4)) t.trace("exit function =giveReward=");

        return 1;
    }


    public List<RewardItem> retrieve() {
        return getRewardItems(
                "SELECT CoinsReward, GoldReward, XPReward, " +
                "StorageQuantity, StorageReward, UnlockableReward, GiftReward " +
                "FROM RewardList ");
    }

    private List<RewardItem> getRewardItems(String sql) {
        List<RewardItem> result = new ArrayList<RewardItem>();

        try {
            ResultSet rs = ds.query(sql, "read", null);
            while (rs.next()) {
                RewardItem item = new RewardItem();

                item.coinsReward = rs.getInt("CoinsReward");
                item.goldReward = rs.getInt("GoldReward");
                item.xpReward = rs.getInt("XPReward");
                item.storageQuantity = rs.getInt("StorageQuantity");
                item.storageReward = rs.getString("StorageReward");
                item.unlockableReward = rs.getString("UnlockableReward");
                item.giftReward = rs.getString("GiftReward");

                result.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
