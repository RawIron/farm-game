package io.rawiron.farmgame.sfsextension;


import io.rawiron.farmgame.webservices.LoaderGame;
import io.rawiron.farmgame.webservices.LoaderGamesettings;
import io.rawiron.farmgame.webservices.LoaderLocalizedStrings;
import org.json.*;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.engine.Achievement;
import io.rawiron.farmgame.engine.Animal;
import io.rawiron.farmgame.engine.Building;
import io.rawiron.farmgame.engine.Decoration;
import io.rawiron.farmgame.engine.Farmer;
import io.rawiron.farmgame.engine.Friend;
import io.rawiron.farmgame.engine.Gift;
import io.rawiron.farmgame.engine.JerryCan;
import io.rawiron.farmgame.engine.Job;
import io.rawiron.farmgame.engine.PlotList;
import io.rawiron.farmgame.engine.Storage;
import io.rawiron.farmgame.engine.Treasure;
import io.rawiron.farmgame.engine.Unlockable;
import io.rawiron.farmgame.engine.Valuable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Router {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Unlockable unlockable;
    private Achievement achievement;
    private PlotList plot;
    private Storage storage;
    private Farmer farmer;
    private Gift gift;
    private Friend friend;
    private Treasure treasure;
    private Job job;
    private Valuable valuable;
    private Animal animal;
    private Decoration decoration;
    private Building building;
    private JerryCan jerryCan;

    private LoaderGame loaderGame;
    private LoaderGamesettings loaderGamesettings;
    private LoaderLocalizedStrings localizedStrings;
    private Login login;


    public Router(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setLoaderGame(LoaderGame in_sfsg) {
        loaderGame = in_sfsg;
    }

    public void setLoaderGamesettings(LoaderGamesettings loaderGamesettings) {
        this.loaderGamesettings = loaderGamesettings;
    }

    public void setLocalizedStrings(LoaderLocalizedStrings localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    public void setLogin(Login in_sfsl) {
        login = in_sfsl;
    }

    public void setAnimal(Animal in_a) {
        animal = in_a;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setPlotList(PlotList in_pl) {
        plot = in_pl;
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

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
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

    public void setTreasure(Treasure in_tr) {
        treasure = in_tr;
    }


    public void setDecoration(Decoration in_d) {
        decoration = in_d;
    }

    public void setBuilding(Building in_bd) {
        building = in_bd;
    }

    //
    // REQUEST ROUTER
    //
    public JSONObject requestRouter(String cmd, JSONObject in_params) {
        if (t.TRACE_TIMERS) t.timer.push(t.getTimer());
        if (t.TRACE_TIMERS) t.trace("enter function requestRouter Task=" + cmd);

        //
        JSONObject result = null;

        // the usual suspects
        //
        Integer in_farmID = -1;
        String in_facebookuser = null;
        Integer in_dbgroup = -1;

        try {
            in_facebookuser = in_params.getString("user");
            in_farmID = in_params.getInt("farmID");
            in_farmID = in_params.getInt("playerID");
            in_dbgroup = in_params.getInt("userDBGroup");
            in_dbgroup = in_params.getInt("farmDBGroup");
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

		/*
        * assign handler function for each event
		*/
        if (false) {
        } else if (cmd.equals("userTask")) {
            // map in_taskClassName to Unlockables.Name
            // !! use a hash to do this !!
            boolean localLog = l.log;
            String costItem = null;
            String task_category = null;


            // handleUserTask;
            //
            String in_taskClassName = null;
            try {
                in_taskClassName = in_params.getString("taskClassName");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            if (in_taskClassName.length() > 0 && in_taskClassName.startsWith("BuyAnimal")) {
                in_taskClassName = "BuyAnimal";
            } else if (in_taskClassName.length() > 0 && in_taskClassName.startsWith("SellBuilding")) {
                in_taskClassName = "SellBuilding";
            } else if (in_taskClassName.length() > 0 && in_taskClassName.startsWith("com.embassy.farm")) {
                in_taskClassName = "com.embassy.farm";
            }
            if (t.VERBOSE) t.trace("enter function requestRouter Task=" + in_taskClassName);

            byte in_mystery = -1;
            int in_X = -1;
            int in_Y = -1;
            int in_slotID = -1;
            int in_tilePointColumn = -1;
            int in_tilePointRow = -1;
            int in_columnWidth = -1;
            int in_rowHeight = -1;
            int in_fromColumn = -1;
            int in_fromRow = -1;
            int in_fromSlot = -1;
            int in_slot = -1;
            String in_itemName = null;
            int in_animalID = 0;
            int in_wander = -1;
            String in_family = null;
            String in_plantState = null;
            int in_vitality = 0;
            String in_friend = null;


            // FARMING
            //
            if (in_taskClassName.equals("PlowTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "Plow";

                plot.plow(in_facebookuser, in_farmID, in_X, in_Y, in_taskClassName);
                localLog = false;
            } else if (in_taskClassName.equals("PlantTask") && (in_mystery == 0)) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = in_itemName;

                plot.plant(in_facebookuser, in_farmID, in_X, in_Y, in_itemName, in_taskClassName, in_mystery);
            } else if (in_taskClassName.equals("PlantTask") && (in_mystery == 1)) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                if (t.VERBOSE) t.trace("Mystery:" + in_mystery);
                task_category = "Farming";
                costItem = "Mystery Seed";

                plot.plant(in_facebookuser, in_farmID, in_X, in_Y, in_itemName, in_taskClassName, in_mystery);
            } else if (in_taskClassName.equals("PlantTreeTask")) {
                try {
                    in_tilePointColumn = in_params.getInt("tilePointColumn");
                    in_tilePointRow = in_params.getInt("tilePointRow");
                    in_columnWidth = in_params.getInt("columnWidth");
                    in_rowHeight = in_params.getInt("rowHeight");
                    in_itemName = in_params.getString("itemName");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = in_itemName;

                plot.plantTree(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_columnWidth, in_rowHeight, in_taskClassName, in_itemName);
            } else if (in_taskClassName.equals("HarvestTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_plantState = in_params.getString("plantState");
                    in_family = in_params.getString("family");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "Harvest";

                plot.handleHarvest(null, in_farmID, in_X, in_Y, in_taskClassName, in_plantState, in_family);
                localLog = false;
            } else if (in_taskClassName.equals("HarvestAnimal")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_animalID = in_params.getInt("animalID");
                    in_itemName = in_params.getString("itemName");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "Harvest";

                animal.harvest(in_facebookuser, in_farmID, in_animalID, in_itemName);
            } else if (in_taskClassName.equals("ClearTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "Clear";

                plot.clear(in_facebookuser, in_farmID, in_X, in_Y);
            } else if (in_taskClassName.equals("BuffTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = in_itemName;

                plot.fertilize(in_facebookuser, in_farmID, in_X, in_Y, in_itemName);
            } else if (in_taskClassName.equals("WaterTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "Water";
                // do nothing

                localLog = false;
            } else if (in_taskClassName.equals("BuyWaterTask")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                    in_vitality = in_params.getInt("vitality");
                    in_mystery = (byte) in_params.getInt("mystery");
                } catch (JSONException e) {
                }

                task_category = "Farming";
                costItem = "BuyWater";

                plot.water(in_facebookuser, in_farmID, in_X, in_Y, in_vitality);
                localLog = false;
            }

            // BUY
            //
            else if (in_taskClassName.equals("BuyDecoration")) {
                try {
                    in_tilePointColumn = in_params.getInt("tilePointColumn");
                    in_tilePointRow = in_params.getInt("tilePointRow");
                    in_slotID = in_params.getInt("slotID");
                    in_itemName = in_params.getString("itemName");
                } catch (JSONException e) {
                }

                task_category = "Buy";
                costItem = in_itemName;
                decoration.buy(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_slotID, in_itemName);
            } else if (in_taskClassName.equals("BuyAnimal")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_itemName = in_params.getString("itemName");
                } catch (JSONException e) {
                }

                task_category = "Buy";
                costItem = in_itemName;
                animal.buy(in_facebookuser, in_farmID, in_X, in_Y, in_itemName);
            } else if (in_taskClassName.equals("io.rawiron.engine.farm")) // Buy something
            {
                try {
                    in_tilePointColumn = in_params.getInt("tilePointColumn");
                    in_tilePointRow = in_params.getInt("tilePointRow");
                    in_columnWidth = in_params.getInt("columnWidth");
                    in_rowHeight = in_params.getInt("rowHeight");
                    in_itemName = in_params.getString("itemName");
                } catch (JSONException e) {
                }

                task_category = "Buy";
                costItem = in_itemName;
                building.buy(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_columnWidth, in_rowHeight, costItem, in_itemName, in_taskClassName);
            }

            // SELL
            //
            else if (in_taskClassName.equals("SellAnimal")) {
                try {
                    in_animalID = in_params.getInt("animalID");
                } catch (JSONException e) {
                }

                task_category = "Sell";
                costItem = "Clear";
                animal.sell(in_facebookuser, in_farmID, in_animalID);
            } else if (in_taskClassName.equals("SellDecoration")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                    in_slotID = in_params.getInt("slotID");
                } catch (JSONException e) {
                }

                task_category = "Sell";
                costItem = "Clear";
                decoration.sell(in_facebookuser, in_farmID, in_X, in_Y, in_slotID);
            } else if (in_taskClassName.equals("SellBuilding")) {
                try {
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                } catch (JSONException e) {
                }

                task_category = "Sell";
                costItem = "Clear";
                building.sell(in_facebookuser, in_farmID, in_X, in_Y);
            }

            // SOCIAL
            //
            else if (in_taskClassName.equals("DoChore")) {
                try {
                    in_friend = in_params.getString("friend");
                } catch (JSONException e) {
                }

                task_category = "Social";
                costItem = in_itemName;
                friend.doChore(in_facebookuser, in_friend);
            }

            // MOVE
            //
            else if (in_taskClassName.equals("MoveItem")) {
                try {
                    in_tilePointColumn = in_params.getInt("tilePointColumn");
                    in_tilePointRow = in_params.getInt("tilePointRow");
                    in_fromColumn = in_params.getInt("fromColumn");
                    in_fromRow = in_params.getInt("fromRow");
                } catch (JSONException e) {
                }

                task_category = "Move";
                costItem = "Move";
                building.move(in_facebookuser, in_farmID, in_fromColumn, in_fromRow, in_tilePointColumn, in_tilePointRow);
            } else if (in_taskClassName.equals("MoveAnimal")) {
                try {
                    in_animalID = in_params.getInt("animalID");
                    in_X = in_params.getInt("X");
                    in_Y = in_params.getInt("Y");
                } catch (JSONException e) {
                }

                task_category = "Move";
                costItem = "Move";
                animal.move(in_facebookuser, in_animalID, in_X, in_Y);
            } else if (in_taskClassName.equals("MoveDecoration")) {
                try {
                    in_slotID = in_params.getInt("slotID");
                    in_tilePointColumn = in_params.getInt("tilePointColumn");
                    in_tilePointRow = in_params.getInt("tilePointRow");
                    in_fromColumn = in_params.getInt("fromColumn");
                    in_fromRow = in_params.getInt("fromRow");
                    in_fromSlot = in_params.getInt("fromSlot");
                } catch (JSONException e) {
                }

                task_category = "Move";
                costItem = "Move";
                decoration.move(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_slotID, in_fromColumn, in_fromRow, in_fromSlot);
            } else {
                if (t.VERBOSE && (t.VERBOSE_LEVEL >= 0))
                    t.trace("assert failure no defined costItem for task:" + in_taskClassName);
            }
            // POST
            //
            // task_category in {Farming, Buy, Sell, Social, Move, null}
            // costItem in {null, Move, Clear, Plow, Mystery Seed, Harvest, Water, BuyWater, in_itemName}
            if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3))
                t.trace("variable dump  task category=" + task_category + " costItem=" + costItem);


        } else if (cmd.equals("farmerLogin")) {
            // farmerLogin;
            String in_skey = null;
            String in_userName = null;
            try {
                in_skey = in_params.getString("skey");
                in_userName = in_params.getString("userName");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = login.login(in_facebookuser, in_userName, in_skey);
        } else if (cmd.equals("getGameSettings")) {
            // getGameSettings;
            String in_itemType = null;
            boolean in_locked = true;
            boolean in_unlocked = true;
            boolean in_coinsCost = false;
            boolean in_goldCost = false;
            String in_collection = null;
            try {
                in_unlocked = (((byte) in_params.getInt("unlocked") == 1) ? true : false);
                in_locked = (((byte) in_params.getInt("locked") == 1) ? true : false);
                in_coinsCost = (((byte) in_params.getInt("coinsCost") == 1) ? true : false);
                in_goldCost = (((byte) in_params.getInt("goldCost") == 1) ? true : false);
                in_itemType = in_params.getString("itemType");
                in_collection = in_params.getString("collection");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = loaderGamesettings.getGameSettings(in_facebookuser, in_farmID, in_itemType
                    , in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collection
            );
        } else if (cmd.equals("buyUnlockable")) {
            // buyUnlockable;
            String in_name = null;
            byte in_useCoins = -1;
            try {
                in_name = in_params.getString("name");
                in_useCoins = (byte) in_params.getInt("useCoins");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            unlockable.buy(in_facebookuser, in_name, in_useCoins);
        } else if (cmd.equals("getUnlockables")) {
            // getUnlockables;
            String in_itemType = null;
            boolean in_locked = false;
            boolean in_unlocked = false;
            boolean in_coinsCost = false;
            boolean in_goldCost = false;
            String in_collection = null;
            try {
                in_unlocked = (((byte) in_params.getInt("unlocked") == 1) ? true : false);
                in_locked = (((byte) in_params.getInt("locked") == 1) ? true : false);
                in_coinsCost = (((byte) in_params.getInt("coinsCost") == 1) ? true : false);
                in_goldCost = (((byte) in_params.getInt("goldCost") == 1) ? true : false);
                in_itemType = in_params.getString("itemType");
                in_collection = in_params.getString("collection");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = loaderGame.getUnlockables(in_facebookuser, in_itemType, in_locked, in_unlocked, in_coinsCost, in_goldCost, in_collection);

        } else if (cmd.equals("getStringTable")) {
            // getStringTable;
            String in_language = null;
            try {
                in_language = in_params.getString("language");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = localizedStrings.getStringTable(in_language);
        } else if (cmd.equals("sellStorageItem")) {
            // sellStorageItem;
            String in_itemName = null;
            int in_quantity = 0;
            try {
                in_itemName = in_params.getString("itemName");
                in_quantity = in_params.getInt("quantity");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            storage.sell(in_facebookuser, in_farmID, in_itemName, in_quantity);
        } else if (cmd.equals("getStorageList")) {
            // retrieveStorageList;
            result = loaderGame.getStorage(in_facebookuser, in_farmID);
        } else if (cmd.equals("testCurrentJob")) {
            // testJobCompletion;
            int in_jobID = -1;
            int in_recipeID = -1;
            try {
                in_jobID = in_params.getInt("jobID");
                in_recipeID = in_params.getInt("recipeID");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            job.testJobCompletion(in_facebookuser, in_farmID, in_jobID, in_recipeID);
        } else if (cmd.equals("testRecipeCompletion")) {
            // testRecipeCompletion;
            int in_jobID = -1;
            int in_recipeID = -1;
            try {
                in_jobID = in_params.getInt("jobID");
                in_recipeID = in_params.getInt("recipeID");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            job.testRecipeCompletion(in_facebookuser, in_farmID, in_jobID, in_recipeID);
        } else if (cmd.equals("getFarmerData")) {
            // getFarmerData;
            result = loaderGame.getFarmer(in_facebookuser);
        } else if (cmd.equals("getSafeFarmerData")) {
            // getSafeFarmerData;
            result = loaderGame.getSafeFarmerData(in_facebookuser);
        } else if (cmd.equals("caluculateNetWorth")) {
            int in_subtract = 0;
            try {
                in_subtract = in_params.getInt("subtract");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            farmer.goldValue(in_facebookuser, in_subtract);
        } else if (cmd.equals("convertCoinsToGold")) {
            int in_howMuch = 0;
            try {
                in_howMuch = in_params.getInt("howMuch");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            valuable.convertCoinsToGold(in_facebookuser, in_farmID, in_howMuch);
        } else if (cmd.equals("setPlayerSetting")) {
            String in_setting = null;
            int in_value = 0;
            try {
                in_setting = in_params.getString("setting");
                in_value = in_params.getInt("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            farmer.updatePlayerSetting(in_facebookuser, in_setting, in_value);
        } else if (cmd.equals("visitFarmer")) {
            // getFarmerData;
            result = loaderGame.getFarmer(in_facebookuser);
        } else if (cmd.equals("sendPlayerTimeMultiplier")) {
            int in_value = 0;
            try {
                in_value = in_params.getInt("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            farmer.updateTimeMultiplier(in_facebookuser, in_value);
        } else if (cmd.equals("updateFarmerAppearance")) {
            String in_SkinTone = null;
            String in_Gender = null;
            String in_HairStyle = null;
            String in_Clothing = null;
            try {
                in_SkinTone = in_params.getString("SkinTone");
                in_Clothing = in_params.getString("Clothing");
                in_HairStyle = in_params.getString("HairStyle");
                in_Gender = in_params.getString("Gender");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            farmer.updateAppearance(in_facebookuser, in_SkinTone, in_Clothing, in_HairStyle, in_Gender);
        } else if (cmd.equals("useJerryCan")) {
            jerryCan.use(in_facebookuser);
        } else if (cmd.equals("loadFarm")) {
            String in_friendID = null;
            try {
                in_friendID = in_params.getString("friendID");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = loaderGame.loadFarm(in_facebookuser, in_friendID);
        } else if (cmd.equals("getFarmData")) {
            result = loaderGame.getFarm(in_facebookuser, in_farmID);
        } else if (cmd.equals("getGifts")) {
            String in_offerStatus = null;
            try {
                in_offerStatus = in_params.getString("offerStatus");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = loaderGame.getGift(in_facebookuser, in_offerStatus);
        } else if (cmd.equals("placeGift")) {
            int in_giftID = 0;
            int in_X = -1;
            int in_Y = -1;
            //int in_x = -1;
            //int in_y = -1;
            int in_slot = -1;
            String in_mystery = null;
            try {
                in_giftID = in_params.getInt("giftID");
                in_X = in_params.getInt("X");
                in_Y = in_params.getInt("Y");
                //in_x = in_params.getInt("x");
                //in_y = in_params.getInt("y");
                in_slot = in_params.getInt("slot");
                in_mystery = in_params.getString("mystery");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            gift.place(in_facebookuser, in_farmID, in_giftID, in_X, in_slot, in_Y, in_mystery);
        } else if (cmd.equals("sellGift")) {
            // sellGift;
            int in_giftID = 0;
            try {
                in_giftID = in_params.getInt("giftID");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            gift.sell(in_facebookuser, in_farmID, in_giftID);
        } else if (cmd.equals("acceptOrRejectGift")) {
            // acceptOrRejectGift;
            int in_offerID = 0;
            boolean in_accept = false;
            try {
                in_offerID = in_params.getInt("offerID");
                in_accept = (((byte) in_params.getInt("accept") == 1) ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            gift.acceptOrReject(in_facebookuser, in_offerID, in_accept);
        } else if (cmd.equals("getPlotList")) {
            result = loaderGame.getPlotList(in_facebookuser, in_farmID);
        } else if (cmd.equals("getPlantStats")) {
            unlockable.getPlantStats();
        } else if (cmd.equals("buyInventory")) {
            String in_name = null;
            byte in_useCoins = 0;
            try {
                in_name = in_params.getString("name");
                in_useCoins = (byte) in_params.getInt("useCoins");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            unlockable.buyInventory(in_facebookuser, in_name, in_useCoins);
        } else if (cmd.equals("addFriends")) {
            // addFriends;
        } else if (cmd.equals("addFriend")) {
            String in_friend = null;
            String in_friendName = null;
            try {
                in_friend = in_params.getString("friendUser");
                in_friendName = in_params.getString("friend");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            friend.add(in_facebookuser, in_friend, in_friendName, in_farmID);
        } else if (cmd.equals("removeFriend")) {
            String in_friend = null;
            try {
                in_friend = in_params.getString("friend");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            friend.remove(in_facebookuser, in_friend);
        } else if (cmd.equals("getFriendList")) {
            result = loaderGame.getFriend(in_facebookuser);
        } else if (cmd.equals("checkForTreasure")) {
            // checkForTreasure;
            int in_X = 0;
            int in_Y = 0;
            //int in_x = 0;
            //int in_y = 0;
            String in_tool = null;
            try {
                in_X = in_params.getInt("X");
                in_Y = in_params.getInt("Y");
                //in_x = in_params.getInt("x");
                //in_y = in_params.getInt("y");
                in_tool = in_params.getString("tool");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            treasure.check(in_facebookuser, in_farmID, in_X, in_Y, in_tool);
        } else if (cmd.equals("testAchievement")) {
            String in_achievementName = null;
            char in_TotalOrStored = 'T';
            try {
                in_achievementName = in_params.getString("achievementName");
                in_TotalOrStored = (char) in_params.getString("TotalOrStored").charAt(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            achievement.test(in_facebookuser, in_farmID, in_achievementName, in_TotalOrStored);
        } else if (cmd.equals("gang")) {
            // gang;
        }


        // Show the time it took to parse the request
        if (t.TRACE_TIMERS) t.timer.push(t.getTimer());
        if (t.TRACE_TIMERS) t.trace("Request=" + cmd + " took: " + (t.timer.pop() - t.timer.pop()) + " ms.");
        return result;
    }
}
