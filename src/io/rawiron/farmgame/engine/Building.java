package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.data.DataGameSettings;
import io.rawiron.farmgame.data.DataProducer;
import io.rawiron.farmgame.data.DataStore;
import io.rawiron.farmgame.data.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Building {

    private DataStore ds;
    private Trace t;
    private Logging l;

    private PlotList plotList;
    private Achievement achievement;
    private Valuable valuable;
    private Buff buff;

    private DataUnlockable dataUnlockable;
    private DataGameSettings dataGameSettings;
    private DataProducer dataProducer;


    public Building(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        l = in_l;
        t = in_t;
    }

    public void setPlotList(PlotList in_pl) {
        plotList = in_pl;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setValuable(Valuable in_v) {
        valuable = in_v;
    }

    public void setBuff(Buff in_b) {
        buff = in_b;
    }

    public void setDataUnlockable(DataUnlockable in_dsu) {
        dataUnlockable = in_dsu;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }


    // ??? BuyBuilding ???
    //
    // Buy something
    // something in {Tent}
    // IN
    // 	params.taskClassName
    // 	params.farmID
    // 	params.tilePointColumn, params.tilePointRow
    //	params.itemName
    //	params.farmDBGroup
    //
    public int buy(String in_facebookuser, int in_farmID, int in_tilePointColumn, int in_tilePointRow, int in_columnWidth, int in_rowHeight, String in_costItem, String in_itemName, String in_taskClassName) {
        int param1 = 0;
        param1 = dataUnlockable.cached.get(in_costItem).param1;

        plotList.deleteAreaContents(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_columnWidth, in_rowHeight);

        if (dataUnlockable.cached.get(in_itemName).subtype != null && !dataUnlockable.cached.get(in_itemName).subtype.equals("")) {
            achievement.add(in_facebookuser, in_farmID, dataUnlockable.cached.get(in_itemName).subtype, 1);
        } else {
            achievement.add(in_facebookuser, in_farmID, in_itemName, 1);
        }


        Integer repeat = dataProducer.cached.get(in_itemName).repeatable;
        String stateName = "growing";
        if (repeat == null) {
            repeat = 0;
            stateName = "";
        }

        // overwrite upper-left corner
        ds.execute("REPLACE INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, State, MysterySeed, Repeatable ) "
                        + " VALUES ( " + in_farmID + ", " + in_tilePointColumn + ", " + in_tilePointRow
                        + ", '" + in_taskClassName + "', '" + in_itemName + "', Now(), Now(), '" + stateName + "', 0, " + repeat + " )"
                , "write", in_facebookuser);
        // remove Buff
        buff.sub(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, null, 1);

        // overwrite all plots except the upper-left corner
        plotList.areaOver(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, param1);

        return 1;
    }


    public int sell(String in_facebookuser, int in_farmID, int in_X, int in_Y) {
        String contents = null;
        String db_sql_read_Building_cost =
                " SELECT Contents "
                        + " FROM PlotList "
                        + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y;

        ResultSet db_res_building_cost = ds.query(db_sql_read_Building_cost, "write", in_facebookuser);
        try {
            if (db_res_building_cost.next()) {
                contents = db_res_building_cost.getString("Contents");
            }
        } catch (SQLException e) {
        }


        int earn_amount_gold = 0;
        int cost_amount_coins = 0;
        int param1 = 0;
        earn_amount_gold = dataUnlockable.cached.get(contents).goldCost;
        cost_amount_coins = dataUnlockable.cached.get(contents).coinsCost;
        param1 = dataUnlockable.cached.get(contents).param1;

        if (earn_amount_gold == 0) {
            earn_amount_gold = (int) (cost_amount_coins * dataGameSettings.cached_CoinsGoldSaleMultiplier);
        }
        earn_amount_gold = (int) (earn_amount_gold * dataGameSettings.cached_BuildingSaleRatio);

        valuable.add(in_facebookuser, in_farmID, 0, earn_amount_gold, 0, 0, 0);

        // overwrite upper-left corner
        this.sub(in_facebookuser, in_farmID, in_X, in_Y, 1);
        // overwrite all plots except the upper-left corner
        plotList.areaClear(in_facebookuser, in_farmID, in_X, in_Y, param1);

        return 1;
    }

    public int goldValue(String in_facebookuser, int in_farmID) {
        String db_sql_read_PlotList_goldValue =
                " SELECT SUM( FLOOR( GoldCost * BuildingSaleRatio) ) AS Gold "
                        + " FROM PlotList INNER JOIN Unlockables ON Name=Contents INNER JOIN GameSettings "
                        + " WHERE FarmID=" + in_farmID
                        + " AND Type = 'Building' ";

        int gold = 0;
        ResultSet goldQuery = ds.query(db_sql_read_PlotList_goldValue, "write", in_facebookuser);
        try {
            if (goldQuery.next()) gold = goldQuery.getInt("Gold");
        } catch (SQLException e) {
        }

        return gold;
    }


    public int count(String in_facebookuser, int in_farmID, String in_building) {
        String db_sql_read_PlotList_buildings =
                " SELECT count(Contents) AS NumBuildings "
                        + " FROM PlotList "
                        + " WHERE FarmID=" + in_farmID + " AND Contents= " + "'" + in_building + "'";

        int quantity = 0;
        ResultSet db_res_building = ds.query(db_sql_read_PlotList_buildings, "read", in_facebookuser);
        try {
            if (db_res_building.next()) {
                quantity = db_res_building.getInt("NumBuildings");
            }
        } catch (SQLException e) {
        }

        return quantity;
    }


    public int sub(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_amount)
    /*
    * ABSTRACT
    * sub(Dl) := add(-Dl)
    *
    */
    {
        if (t.verbose && (t.verbose_level >= 0) && (in_amount < 0)) {
            t.trace("assert failure in_amount=" + in_amount + " is <0");
        }

        return this.add(in_facebookuser, in_farmID, in_X, in_Y, -in_amount);
    }


    public int add(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_amount)
/*
* ABSTRACT
* add(Dl)
*
* PERFORMANCE_IMPACT
*	General:high
*	Frequency:stress
*	Cost:low
*/ {
        int success = 0;

        if (in_amount > 0) {
            success = ds.execute(
                    " "
                    , "write", in_facebookuser);
        } else if (in_amount < 0) {
            success = ds.execute(" DELETE FROM PlotList WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y
                    , "write", in_facebookuser);
        }

        return success;
    }


    public int move(String in_facebookuser, int in_farmID, int in_fromColumn, int in_fromRow, int in_tilePointColumn, int in_tilePointRow) {
        boolean gotDimension = false;

        String contents = null;
        ResultSet unlockLine = ds.query(
                " SELECT Contents "
                        + " FROM PlotList "
                        + " WHERE FarmID=" + in_farmID + " AND X=" + in_fromColumn + " AND Y=" + in_fromRow
                , "write", in_facebookuser);

        try {
            if (unlockLine.next()) {
                contents = unlockLine.getString("Contents");
            }
            gotDimension = true;
        } catch (SQLException e) {
        }

        int param1 = 0;
        param1 = dataUnlockable.cached.get(contents).param1;


        if (gotDimension) {
            // destroy source and target
            String db_sql_write_ActiveBuffs_move = " UPDATE ActiveBuffs "
                    + " SET X= -100 ,Y=" + in_tilePointRow
                    + " WHERE FarmID=" + in_farmID
                    + " AND X=" + in_fromColumn + " AND Y=" + in_fromRow;
            ds.execute(db_sql_write_ActiveBuffs_move, "write", in_facebookuser);


            String db_sql_write_PlotList_move = " UPDATE PlotList "
                    + " SET X= -100 ,Y=" + in_tilePointRow
                    + " WHERE FarmID=" + in_farmID
                    + " AND X=" + in_fromColumn + " AND Y=" + in_fromRow;
            ds.execute(db_sql_write_PlotList_move, "write", in_facebookuser);


            String db_sql_write_PlotList_delete = " DELETE FROM PlotList "
                    + " WHERE FarmID=" + in_farmID
                    + " AND X=" + in_fromColumn + " AND Y=" + in_fromRow;
            ds.execute(db_sql_write_PlotList_delete, "write", in_facebookuser);

            db_sql_write_PlotList_delete = " DELETE FROM PlotList "
                    + " WHERE FarmID=" + in_farmID
                    + " AND X=" + in_tilePointColumn + " AND Y=" + in_tilePointRow;
            ds.execute(db_sql_write_PlotList_delete, "write", in_facebookuser);


            plotList.areaClear(in_facebookuser, in_farmID, in_fromColumn, in_fromRow, param1);
            plotList.areaClear(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, param1);


            // create target                     
            String db_sql_write_ActiveBuffs_moveX = " UPDATE ActiveBuffs "
                    + " SET X=" + in_tilePointColumn
                    + " WHERE FarmID=" + in_farmID
                    + " AND X= -100 AND Y=" + in_tilePointRow;
            ds.execute(db_sql_write_ActiveBuffs_moveX, "write", in_facebookuser);

            String db_sql_write_PlotList_moveX = " UPDATE PlotList "
                    + " SET X=" + in_tilePointColumn
                    + " WHERE FarmID=" + in_farmID
                    + " AND X= -100 AND Y=" + in_tilePointRow;
            ds.execute(db_sql_write_PlotList_moveX, "write", in_facebookuser);


            plotList.areaOver(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, param1);
        }

        return 1;
    }

}
