package io.rawiron.farmgame.webservices;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.engine.*;
import io.rawiron.farmgame.system.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataLevel;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;

import java.util.HashMap;


public class LoaderGame {
    public HashMap<String, JSONArray> cached = new HashMap<String, JSONArray>();
    private Trace t;
    private Logging l;
    private DataStore ds;
    private Unlockable unlockable;
    private Achievement achievement;
    private PlotList plot;
    private Storage storage;
    private Farmer farmer;
    private Farm farm;
    private Gift gift;
    private Friend friend;
    private Job job;
    private IAnimalInventory animalInventory;
    private Animal animal;
    private Decoration decoration;
    private Collection collection;
    private Reward reward;
    private DataGameSettings dataGameSettings;
    private DataLevel dataLevel;
    LoaderLocalizedStrings ls;

    public LoaderGame(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;

        int dbgroup = 0;
        // GAMESETTINGS
        ResultSet gameSettingsQueryRes = dataGameSettings.retrieve(dbgroup);
        JSONArray gameSettings = new JSONArray();
        Util.appendResult(gameSettings, gameSettingsQueryRes, 0);
        cached.put("GameSettings", gameSettings);

        // LEVEL
        ResultSet levelsQueryRes = dataLevel.retrieve(dbgroup);
        JSONArray levelList = new JSONArray();
        Util.appendResult(levelList, levelsQueryRes, 0);
        cached.put("Level", levelList);
    }

    public void setAnimal(Animal in_a) {
        animal = in_a;
    }

    public void setAnimalInventory(IAnimalInventory al) { animalInventory = al; }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setPlotList(PlotList in_pl) {
        plot = in_pl;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setUnlockable(Unlockable in_u) {
        unlockable = in_u;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setFarm(Farm in_fm) {
        farm = in_fm;
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

    public void setDecoration(Decoration in_d) {
        decoration = in_d;
    }

    public void setCollection(Collection in_c) {
        collection = in_c;
    }

    public void setReward(Reward in_rw) {
        reward = in_rw;
    }

    public void dataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }

    public void dataLevel(DataLevel in_lv) {
        dataLevel = in_lv;
    }


    public JSONObject getPlotList(String in_facebookuser, int in_farmID)
    {
        if (Trace.TRACE_TIMERS) t.timer.push(Trace.getTimer());
        if (Trace.VERBOSE) Trace.trace("enter function retrievePlotList ");

        if (l.log_data_read)
            ds.execute("INSERT INTO `log` VALUES ( Now(), 'retrievePlotList','" + in_facebookuser + "','" + in_farmID + "','',0,'L',null,1)"
                    , "log", null);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        ResultSet db_res_userSettings = ds.query("SELECT TimeMultiplier" +
                "FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        int user_timeMultiplier = 1;
        try {
            if (db_res_userSettings.next()) {
                user_timeMultiplier = db_res_userSettings.getInt("TimeMultiplier");
            }
        } catch (SQLException e) {
        }

        String db_sql_read_PlotList = " SELECT PlotList.* "
                + ", if ( (ActiveBuffs.X >= 0), 1, 0 ) AS HasBuff "
                + ", TIMESTAMPDIFF( SECOND, LastSave, Now() ) As ElapsedTime "
                + ", DATE_FORMAT(CreateDate,'%a %b %d %k:%i:%s GMT-0800 %Y') as Planted "
                + ", LastWatering IS NOT NULL as Watered "
                + " FROM PlotList LEFT JOIN ActiveBuffs ON (ActiveBuffs.FarmID=PlotList.FarmID AND ActiveBuffs.X=PlotList.X AND ActiveBuffs.Y=PlotList.Y) "
                + " WHERE PlotList.FarmID=" + in_farmID
                + " ORDER BY PlotList.FarmID, PlotList.X, PlotList.Y ASC ";

        // response.fullResult.plotList = [];
        JSONArray plotList = new JSONArray();
        int i = 0;
        int growthPercentage = 0;
        ResultSet queryRes = ds.query(db_sql_read_PlotList, "read", in_facebookuser);
        try {
            for (i = 0; queryRes.next(); i++) {
                String db_content = queryRes.getString("Contents");
                if ((db_content != null) && (!db_content.equals("Harvest")) && (!db_content.equals("foundation")) && (!db_content.equals("Buildings_CanvasTent_Name"))) {
                    // this is something 'growable'
                    growthPercentage = plot.growRow(in_facebookuser, queryRes, user_timeMultiplier);
                }
                JSONObject ao = new JSONObject();
                // response.fullResult.plotList[i] = row;
                queryRes.previous();
                Util.appendResultCount(plotList, queryRes, i, 1);
                try {
                    ao.put("Vitality", growthPercentage);
                    plotList.put(i, ao);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
        }

        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Show the time it took to parse the request
        if (Trace.TRACE_TIMERS) t.timer.push(Trace.getTimer());
        if (Trace.TRACE_TIMERS) Trace.trace("Request took: " + (t.timer.pop() - t.timer.pop()) + " ms.");

        return response;
    }


    public JSONArray getBuff(String in_facebookuser, int in_farmID) {
        String db_sql_read_ActiveBuffs =
                " SELECT ab.Buff as Buff, ab.Start as Start, ab.FarmID as FarmID, ab.X as X, ab.Y as Y, u.ItemClass as ItemClass, u.IconClass as IconClass "
                        + ", if ( (ab.X >= 0), 1, 0 ) AS HasBuff  "
                        + " FROM PlotList LEFT JOIN ( ActiveBuffs as ab INNER JOIN Buffs ON Title=ab.Buff INNER JOIN Unlockables as u ON Title=u.Name ) "
                        + " ON (ab.FarmID=PlotList.FarmID AND ab.X=PlotList.X AND ab.Y=PlotList.Y) "
                        + " WHERE PlotList.FarmID=" + in_farmID
                        + " ORDER BY PlotList.FarmID, PlotList.X, PlotList.Y ASC ";
        ResultSet buffQueryRes = ds.query(db_sql_read_ActiveBuffs, "read", in_facebookuser);

        // response.fullResult.bufList = [];
        JSONArray bufList = new JSONArray();

        int i = 0;
        byte db_hasBuff = 0;
        try {
            for (i = 0; buffQueryRes.next(); i++) {
                db_hasBuff = (byte) buffQueryRes.getInt("HasBuff");
                if ((db_hasBuff == 1)) {
                    // response.fullResult.bufList[i] = buffQueryRes.get(i);
                    buffQueryRes.previous();
                    Util.appendResultCount(bufList, buffQueryRes, i, 1);
                } else {
                    try {
                        bufList.put(i, "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
        }

        return bufList;
    }


    public JSONArray getDecoration(String in_facebookuser, int in_farmID) {
        // DECORATION
        ResultSet decorationQueryRes = decoration.retrieve(in_facebookuser, in_farmID);

        // response.fullResult.decorationList = [];
        JSONArray decorationList = new JSONArray();
        // response.fullResult.bufList[i] = buffQueryRes.get(i);
        Util.appendResult(decorationList, decorationQueryRes, 0);

        return decorationList;
    }


    public JSONArray getAnimal(String in_facebookuser, int in_farmID) {
        // ANIMAL
        ResultSet animalQueryRes = animalInventory.retrieve(in_facebookuser);

        // response.fullResult.animalList = [];
        JSONArray animalList = new JSONArray();
        // response.fullResult.animalList[i] = queryRes.get(i);
        Util.appendResult(animalList, animalQueryRes, 0);

        return animalList;
    }


    public JSONObject getStorage(String in_facebookuser, int in_farmID) {
        // STORAGE
        ResultSet db_res_storage = storage.retrieve(in_facebookuser, in_farmID);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_storage, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject getFarmer(String in_facebookuser) {
        // FARMER
        ResultSet db_res_farmer = farmer.retrieve(in_facebookuser);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farmer, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        response.put("ok", true);
        return response;
    }

    public JSONArray getFarmer(String in_facebookuser, int in_index) {
        // FARMER
        ResultSet db_res_farmer = farmer.retrieve(in_facebookuser);

        // response.fullResult = []
        JSONArray fullResult = new JSONArray();
        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farmer, in_index);

        return fullResult;
    }

    public JSONArray getSession(String in_facebookuser, int in_index) {
        // FARMER
        ResultSet db_res_farmer = farmer.retrieveIndex(in_facebookuser);

        // response.fullResult = []
        JSONArray fullResult = new JSONArray();
        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farmer, in_index);

        return fullResult;
    }

    public JSONArray getFarmerSkey(String in_facebookuser, int in_index, String in_skey) {
        // FARMER
        ResultSet db_res_farmer = farmer.retrieveSkey(in_facebookuser, in_skey);

        // response.fullResult = []
        JSONArray fullResult = new JSONArray();
        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farmer, in_index);

        return fullResult;
    }


    public JSONObject getSafeFarmerData(String in_facebookuser) {
        // FARMER
        ResultSet db_res_farmer = farmer.getSafeFarmerData(in_facebookuser);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farmer, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject loadFarm(String in_facebookuser, String in_friend) {
        // FARM
        ResultSet db_res_farm = farm.loadFarm(in_facebookuser, in_friend);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farm, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject getFarm(String in_facebookuser, int in_farmID) {
        // FARM
        ResultSet db_res_farm = farm.retrieve(in_facebookuser);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_farm, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject getGift(String in_facebookuser, String in_offerStatus) {
        // GIFT
        ResultSet db_res_gift = gift.retrieve(in_facebookuser, in_offerStatus);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_gift, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject getFriend(String in_facebookuser) {
        // FRIEND
        ResultSet db_res_friend = friend.retrieve(in_facebookuser, null);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_friend, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }


    public JSONObject getUnlockables(
            String in_facebookuser, String in_itemType
            , boolean in_locked, boolean in_unlocked, boolean in_coinsCost, boolean in_goldCost, String in_collectionName
    ) {
        ResultSet unlockQueryRes = unlockable.get(in_facebookuser, in_itemType
                , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collectionName
        );

        // response = {};
        JSONObject response = new JSONObject();
        // fullResult = [];
        JSONArray fullResult = new JSONArray();
        Util.appendResult(fullResult, unlockQueryRes, 0);

        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

}
