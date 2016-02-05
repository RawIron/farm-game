package io.rawiron.farmgame.webservices;

import io.rawiron.farmgame.engine.*;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;
import io.rawiron.farmgame.system.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class LoaderGamesettings {
    public HashMap<String, JSONArray> cached = new HashMap<String, JSONArray>();
    private Trace t;
    private Logging l;
    private DataStore ds;
    private Unlockable unlockable;
    private Achievement achievement;
    private Gift gift;
    private Friend friend;
    private Job job;
    private Collection collection;
    private Reward reward;

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setUnlockable(Unlockable in_u) {
        unlockable = in_u;
    }

    public void setGift(Gift in_g) {
        gift = in_g;
    }

    public void setFriend(Friend in_fr) {
        friend = in_fr;
    }

    public void setJob(Job in_j) {
        job = in_j;
    }

    public void setCollection(Collection in_c) {
        collection = in_c;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }


    public JSONObject getGameSettings(String in_facebookuser, int in_farmID
            , String in_itemType
            , boolean in_locked, boolean in_unlocked
            , boolean in_coinsCost, boolean in_goldCost, String in_collectionName) {
        // response = {}
        JSONObject response = new JSONObject();
        // response.fullResult = {};
        JSONObject fullResult = new JSONObject();


        // response.fullResult.plotList = [];	// PlotList does not get filled here. Add an empty PlotList.
        JSONArray plotList = new JSONArray();
        try {
            fullResult.put("plotList", plotList);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }


        // GAMESETTINGS
        try {
            fullResult.put("gameSetting", this.cached.get("GameSettings").get(0));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        // LEVEL
        try {
            fullResult.put("levelList", this.cached.get("Level"));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }


        // FRIEND
        ResultSet friendQueryRes = this.friend.retrieve(in_facebookuser, "active");
        // response.fullResult.FriendList = [];
        this.addToResult(fullResult, "FriendList", friendQueryRes);

        ResultSet notFriendQueryRes = this.friend.retrieve(in_facebookuser, "inactive");
        // response.fullResult.NotFriendList = [];
        this.addToResult(fullResult, "NotFriendList", notFriendQueryRes);


        // UNLOCKABLE
        // response.fullResult.ShopItems = {};
        JSONObject ShopItems = new JSONObject();

        in_itemType = "Land";
        ResultSet unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Land = [];
        this.addToResult(ShopItems, "Land", unlockQueryRes);

        in_itemType = "Seeds";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Seeds = [];
        this.addToResult(ShopItems, "Seeds", unlockQueryRes);

        in_itemType = "Tending";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Tending = [];
        this.addToResult(ShopItems, "Tending", unlockQueryRes);

        in_itemType = "Building";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Buildings = [];
        this.addToResult(ShopItems, "Buildings", unlockQueryRes);


        in_itemType = "Contraption";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Contraptions = [];
        this.addToResult(ShopItems, "Contraptions", unlockQueryRes);


        in_itemType = "Protection";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Protection = [];
        this.addToResult(ShopItems, "Protection", unlockQueryRes);


        in_itemType = "Decoration";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Decorations = [];
        this.addToResult(ShopItems, "Decorations", unlockQueryRes);


        in_itemType = "Animal";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Animals = [];
        this.addToResult(ShopItems, "Animals", unlockQueryRes);


        if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 3)) {
            try {
                JSONObject jtrc = new JSONObject();
                JSONArray jtrcList = new JSONArray();
                jtrcList = ShopItems.getJSONArray("Animals");
                jtrc = jtrcList.getJSONObject(0);
                Trace.trace("variable dump =" + jtrc.getString("ForSale"));
                Trace.trace("variable dump =" + jtrc.getString("Description"));
                Trace.trace("variable dump =" + jtrc.getString("FuelCost"));
                Trace.trace("variable dump =" + jtrc.getString("Unlocked"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        in_itemType = "Avatar";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.Avatar = [];
        this.addToResult(ShopItems, "Avatar", unlockQueryRes);


        try {
            fullResult.put("ShopItems", ShopItems);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }


        in_itemType = "Product";
        unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );
        // response.fullResult.ShopItems.ProductList = [];
        this.addToResult(fullResult, "ProductList", unlockQueryRes);


        // GIFTLIST
        ResultSet queryResGift = this.gift.retrieveList(in_facebookuser);
        // response.fullResult.GiftList = [];
        this.addToResult(fullResult, "GiftList", queryResGift);


        // REWARD, COLLECTION
        in_itemType = null;
        ResultSet collectionQuery = this.collection.retrieve(in_facebookuser);
        // response.fullResult.CollectionInfo = [];
        this.addToResult(fullResult, "CollectionInfo", collectionQuery);

        // response.fullResult.Collections = {};
        JSONObject Collections = new JSONObject();
        try {
            collectionQuery.beforeFirst();
            String db_collection = null;
            while (collectionQuery.next()) {
                db_collection = collectionQuery.getString("Name");
                unlockQueryRes = this.unlockable.get(in_facebookuser, in_itemType
                        , in_locked, in_unlocked, in_coinsCost, in_goldCost, db_collection
                );
                // response.fullResult.Collections[params.collection] = [];
                JSONArray collectionName = new JSONArray();
                Util.appendResult(collectionName, unlockQueryRes, 0);
                try {
                    Collections.put(db_collection, collectionName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
        }
        try {
            fullResult.put("Collections", Collections);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // JOB
        ResultSet jobQuery = this.job.retrieve(in_facebookuser);
        // response.fullResult.JobList = [];
        this.addToResult(fullResult, "JobList", jobQuery);

        // REWARD
        ResultSet rewardQuery = this.reward.retrieve();
        // response.fullResult.RewardList = [];
        this.addToResult(fullResult, "RewardList", rewardQuery);

        // ACHIEVEMENT
        ResultSet achievementQuery = this.achievement.retrieve(in_facebookuser);
        // response.fullResult.AchievementList = [];
        this.addToResult(fullResult, "AchievementList", achievementQuery);

        // ACHIEVEMENT COUNTERS
        ResultSet achievementCountersQuery = this.achievement.retrieveList(in_facebookuser, in_farmID);
        // response.fullResult.AchievementCounters = [];
        this.addToResult(fullResult, "AchievementCounters", achievementCountersQuery);


        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void addToResult(JSONObject response, String key, ResultSet resultSet) {
        JSONArray itemList = new JSONArray();
        Util.appendResult(itemList, resultSet, 0);
        try {
            response.put(key, itemList);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
}
