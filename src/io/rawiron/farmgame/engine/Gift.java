package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataProducer;
import io.rawiron.farmgame.gamesettings.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Gift {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Decoration decoration;
    private Buff buff;
    private Animal animal;
    private PlotList plotList;
    private Valuable valuable;

    private DataProducer dataProducer;
    private DataGameSettings dataGameSettings;


    public Gift(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
    }

    public void setAnimal(Animal in_a) {
        animal = in_a;
    }

    public void setBuff(Buff in_b) {
        buff = in_b;
    }

    public void setDecoration(Decoration in_d) {
        decoration = in_d;
    }

    public void setPlotList(PlotList in_pl) {
        plotList = in_pl;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }


    public int place(String in_facebookuser, int in_farmID, int in_giftID, int in_X, int in_slot, int in_Y, String in_mystery)
/**
 *
 *
 * PERFORMANCE_IMPACT
 *	General:medium
 *	Frequency:many
 *	Cost:high
 */
    {

        // DEFINE
        //
        int result = 0;

        ResultSet queryResult = ds.query(
                " SELECT GiftPairs.ItemName, Unlockables.Type, Unlockables.ButtonType "
                        + " FROM GiftPairs INNER JOIN Unlockables ON GiftPairs.ItemName=Unlockables.Name "
                        + " WHERE OfferID=" + in_giftID + " AND Receiver=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        boolean getGiftPairs = false;
        String unlockType = null;
        String giftItemName = null;
        String unlockButtonType = null;
        try {
            if (queryResult.next()) {
                unlockType = queryResult.getString("Type");
                giftItemName = queryResult.getString("ItemName");
                unlockButtonType = queryResult.getString("ButtonType");

                getGiftPairs = true;
            }
        } catch (SQLException e) {
        }

        if (getGiftPairs) {
            if (unlockType.equals("Tree")) {
                buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);

                ds.execute(" DELETE FROM PlotList "
                                + " WHERE FarmID=" + in_farmID + " AND X>=" + in_X + " AND Y>=" + in_Y + " AND X<" + (2 + in_X) + " AND Y<" + (2 + in_Y)
                        , "write", in_facebookuser);
                ds.execute(" INSERT INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave ) "
                                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", 'PlantTreeTask', '" + giftItemName + "', Now(), Now() )"
                        , "write", in_facebookuser);
            } else if (unlockButtonType.equals("AnimalBuyButton")) {
                result = animal.add(in_facebookuser, in_farmID, null, in_X, in_Y, giftItemName, 1, 0);
            } else if (unlockButtonType.equals("BuildingBuyButton")) {
                Integer repeat = (Integer) dataProducer.cached.get(giftItemName).repeatable;
                String state = "growing";
                if (repeat == null) {
                    repeat = 0;
                    state = null;
                }

                buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);
                ds.execute(" DELETE FROM PlotList "
                                + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y
                        , "write", in_facebookuser);
                ds.execute(" INSERT INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, Repeatable, State ) "
                                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", 'com.embassy.farm.tiles.BuildingTile', '" + giftItemName + "', Now(), Now(), " + repeat + ", '" + state + "' )"
                        , "write", in_facebookuser);
            } else if (unlockType.equals("Plant")) {

//                      deletePlotContents( in_farmID, in_X, in_Y, params.farmDBGroup);
                buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);
                ds.execute(" DELETE FROM PlotList "
                                + "WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y
                        , "write", in_facebookuser);

                String itemName = giftItemName;
                byte mysterySeed = 0;
                if (itemName.equals("Mystery Seed")) {
                    itemName = in_mystery;
                    mysterySeed = 1;
                }

                ds.execute("INSERT INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, MysterySeed, Repeatable ) "
                                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", 'PlantTask', '" + itemName + "', Now(), Now(), " + mysterySeed + ", " + dataProducer.cached.get(giftItemName).repeatable + " )"
                        , "write", in_facebookuser);
            } else if (unlockButtonType.equals("DecorationBuyButton")) {
                decoration.add(in_facebookuser, in_farmID, in_X, in_Y, in_slot, giftItemName, 1);
            } else if (unlockButtonType.equals("FertilizerBuyButton")) {
                plotList.fertilize(in_facebookuser, in_farmID, in_X, in_Y, giftItemName);
            }

            ds.execute("UPDATE GiftPairs SET OfferStatus='Accepted' WHERE OfferID=" + in_giftID, "read", in_facebookuser);
        }


        return result;
    }


    public boolean sell(String in_facebookuser, Integer in_farmID, int in_giftID)
/**
 *
 * PERFORMANCE_IMPACT
 *	General:medium
 *	Frequency:many
 *	Cost:high
 */
    {
        boolean result = false;

        int vFarmID = 0;
        if ((in_farmID == null)) {
            // read farmID from Farmers table
            vFarmID = 0;
        } else {
            vFarmID = in_farmID;
        }

        ResultSet db_res_Gift_cost = ds.query(
                " SELECT Unlockables.Type, Unlockables.ButtonType, Unlockables.GoldCost, Unlockables.CoinsCost "
                        + " FROM GiftPairs INNER JOIN Unlockables ON GiftPairs.ItemName=Unlockables.Name "
                        + " WHERE OfferID=" + in_giftID + " AND Receiver=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        boolean gotGiftCost = false;
        int cost_amount_gold = 0;
        int cost_amount_coins = 0;
        String buttonType = null;

        try {
            if (db_res_Gift_cost.next()) {
                cost_amount_gold = db_res_Gift_cost.getInt("GoldCost");
                cost_amount_coins = db_res_Gift_cost.getInt("CoinsCost");
                buttonType = db_res_Gift_cost.getString("ButtonType");

                gotGiftCost = true;
            }
        } catch (SQLException e) {
        }


        if (cost_amount_gold == 0) {
            cost_amount_gold = (int) (cost_amount_coins * dataGameSettings.cached_CoinsGoldSaleMultiplier);
        }

        //
        // reward the farmer
        if (gotGiftCost) {

            if (buttonType.equals("AnimalBuyButton")) {
                cost_amount_gold = (int) (cost_amount_gold * dataGameSettings.cached_AnimalSaleRatio);
                valuable.add(in_facebookuser, vFarmID, 0, cost_amount_gold, 0, 0, 0);
            } else if (buttonType.equals("BuildingBuyButton")) {
                cost_amount_gold = (int) (cost_amount_gold * dataGameSettings.cached_BuildingSaleRatio);
                valuable.add(in_facebookuser, vFarmID, 0, cost_amount_gold, 0, 0, 0);
            } else {
                cost_amount_gold = (int) (cost_amount_gold * dataGameSettings.cached_DecorationSaleRatio);
                valuable.add(in_facebookuser, vFarmID, 0, cost_amount_gold, 0, 0, 0);
            }

            // update state of the gift
            ds.execute("UPDATE GiftPairs SET OfferStatus='Rejected' WHERE OfferID=" + in_giftID, "write", in_facebookuser);

            result = true;
        }

        return result;
    }


    public ResultSet retrieve(String in_facebookuser, String in_offerStatus) {
        ResultSet db_res_GiftPairs = null;
        ds.execute(" SELECT * FROM GiftPairs WHERE OfferStatus='" + in_offerStatus + "' AND Receiver=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        return db_res_GiftPairs;
    }

    public ResultSet retrieveList(String in_facebookuser) {
        String sql =
                " SELECT GiftPairs.*, Unlockables.* "
                        + ", FarmerIndex.PlayerName, FarmerIndex.Gender, FarmerIndex.HairStyle, FarmerIndex.SkinTone, FarmerIndex.Clothing "
                        + ", ProductionHours AS ProductionSeconds, WitherTime, XPHarvestValue "
                        + ", Producers.Produce, Products.GoldValue, Products.CoinsValue, Products.SWF, DryYield "
                        + ", ual.HairStyleUnique "
                        + " FROM GiftPairs INNER JOIN Unlockables ON Name=ItemName INNER JOIN FarmerIndex ON Giver=FarmerIndex.FacebookUser "
                        + " LEFT JOIN Producers ON Producers.Name=Unlockables.Name LEFT JOIN Products ON Producers.Produce=Products.Name "
                        + " LEFT JOIN UniqueAvatarList ual on ual.PlayerID=FarmerIndex.PlayerID "
                        + " WHERE Receiver=" + "'" + in_facebookuser + "'" + " AND OfferStatus ='Offered'";

        return ds.query(sql, "read", in_facebookuser);
    }

    public void acceptOrReject(String in_facebookuser, int in_offerID, boolean in_accept) {
        String acceptString = "Rejected";

        if (in_accept) {
            acceptString = "Accepted";
        }
        ds.execute("UPDATE GiftPairs SET OfferStatus='" + acceptString + "' WHERE OfferID=" + in_offerID + " AND Receiver=" + "'" + in_facebookuser + "'"
                , "write", in_facebookuser);
    }


    public int add(String in_item, String in_giver, String in_receiver)
/**
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:low
 */
    {
        int success = 0;
        // add(Gl)
        success = ds.execute(" INSERT INTO GiftPairs ( ItemName, Giver, Receiver, OfferDate ) "
                        + " VALUES ( '" + in_item + "'" + ", '" + in_giver + "', '" + in_receiver + "', Now() )"
                , "write", in_giver);

        return success;
    }

}
