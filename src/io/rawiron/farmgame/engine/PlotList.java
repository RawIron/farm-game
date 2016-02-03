package io.rawiron.farmgame.engine;


import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.gamesettings.DataBuff;
import io.rawiron.farmgame.gamesettings.DataProducer;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class PlotList {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Buff buff;
    private Storage storage;
    private Achievement achievement;
    private Farmer farmer;

    private DataProducer dataProducer;
    private DataBuff dataBuff;


    public PlotList(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setBuff(Buff in_b) {
        buff = in_b;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }

    public void setDataBuff(DataBuff in_db) {
        dataBuff = in_db;
    }


    public ResultSet retrieve(String in_facebookuser, int in_farmID)
/**
 * ABSTRACT
 *
 * IN
 *	params.userDBGroup
 *	params.farmDBGroup
 *	params.farmID
 *	params.user
 * OUT
 *
 * PERFORMANCE_IMPACT
 *	General:Low
 *	Frequency:few
 *	Cost:high
 */
    {
        if (t.trace_timers) t.timer.push(t.getTimer());
        if (t.verbose) t.trace("enter function retrievePlotList ");

        if (l.log_data_read)
            ds.execute("INSERT INTO `log` VALUES ( Now(), 'retrievePlotList','" + in_facebookuser + "','" + in_farmID + "','',0,'L',null,1)"
                    , "log", in_facebookuser);

        //
        int user_timeMultiplier = 1;
        user_timeMultiplier = farmer.getTimeMultiplier(in_facebookuser);


        //
        String db_sql_read_PlotList = " SELECT PlotList.* "
                + ", if ( (ActiveBuffs.X >= 0), 1, 0 ) AS HasBuff "
                + ", TIMESTAMPDIFF( SECOND, LastSave, Now() ) As ElapsedTime "
                + ", DATE_FORMAT(CreateDate,'%a %b %d %k:%i:%s GMT-0800 %Y') as Planted "
                + ", LastWatering IS NOT NULL as Watered "
                + " FROM PlotList LEFT JOIN ActiveBuffs ON (ActiveBuffs.X=PlotList.X AND ActiveBuffs.Y=PlotList.Y AND ActiveBuffs.FarmID=PlotList.FarmID) "
                + " WHERE PlotList.FarmID=" + in_farmID;

        int growthPercentage = 0;
        ResultSet queryRes = ds.query(db_sql_read_PlotList, "read", in_facebookuser);
        try {
            while (queryRes.next()) {
                String db_content = queryRes.getString("Contents");
                if ((!db_content.equals("Harvest")) && (!db_content.equals("foundation")) && (!db_content.equals("Buildings_CanvasTent_Name"))) {
                    // this is something 'growable'
                    growthPercentage = growRow(in_facebookuser, queryRes, user_timeMultiplier);
                }
            }
        } catch (SQLException e) {
        }


        // Show the time it took to parse the request
        if (t.trace_timers) t.timer.push(t.getTimer());
        if (t.trace_timers) t.trace("Request took: " + (t.timer.pop() - t.timer.pop()) + " ms.");


        return queryRes;
        // response.fullResult.plotList[i] = row;
    }


    public boolean plow(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_taskName) {
        String db_sql_write_PlotList_plow = " REPLACE INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, State ) "
                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", '" + in_taskName + "', 'Plow', Now(), Now(), '' )";

        achievement.add(in_facebookuser, in_farmID, "Plow", 1);
        buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);
        ds.execute(db_sql_write_PlotList_plow, "write", in_facebookuser);

        return true;
    }


    public boolean plant(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_itemName, String in_taskName, int in_mystery) {
        String db_sql_write_PlotList_plant = "REPLACE INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, MysterySeed, Repeatable ) "
                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", '"
                + in_taskName + "', '" + in_itemName + "', Now(), Now(), "
                + in_mystery + "," + dataProducer.cached.get(in_itemName).repeatable + " )";

        if (in_mystery == 1) {
            in_taskName = "PlantMysterySeed";
        }
        ds.execute(db_sql_write_PlotList_plant, "write", in_facebookuser);

        return true;
    }


    public boolean fertilize(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_itemName) {
        achievement.add(in_facebookuser, in_farmID, in_itemName, 1);
        this.fertilizePlot(in_facebookuser, in_farmID, in_X, in_Y, in_itemName);

        return true;
    }


    public boolean clear(String in_facebookuser, int in_farmID, int in_X, int in_Y) {
        buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);
        this.deletePlotContents(in_facebookuser, in_farmID, in_X, in_Y);

        return true;
    }

    public boolean water(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_vitality) {
        String db_sql_write_PlotList_water =
                " UPDATE PlotList "
                        + " SET LastWatering=Now(), LastSave=Now(), Vitality=" + in_vitality
                        + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y;

        ds.execute(db_sql_write_PlotList_water, "write", in_facebookuser);

        return true;
    }


    public boolean plantTree(String in_facebookuser, int in_farmID, int in_tilePointColumn, int in_tilePointRow, int in_columnWidth, int in_rowHeight
            , String in_taskClassName, String in_itemName) {
        String db_sql_PlotList_tree =
                " INSERT INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave ) "
                        + " VALUES ( " + in_farmID + ", " + in_tilePointColumn + ", " + in_tilePointRow + ", " + "'" + in_taskClassName + "'"
                        + ", " + "'" + in_itemName + "'" + ", Now(), Now() " + ")";

        this.deleteAreaContents(in_facebookuser, in_farmID, in_tilePointColumn, in_tilePointRow, in_columnWidth, in_rowHeight);
        ds.execute(db_sql_PlotList_tree, "write", in_facebookuser);

        return true;
    }


    public int xpEarned(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_plantState) {
        int XP_earned = 0;
        if (in_plantState.equals("grown")) {
            String contents = null;
            String db_sql_read =
                    " SELECT Contents "
                            + " FROM PlotList "
                            + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y;

            ResultSet db_res = ds.query(db_sql_read, "read", in_facebookuser);
            try {
                if (db_res.next()) {
                    contents = db_res.getString("Contents");
                }
            } catch (SQLException e) {
            }


            XP_earned = dataProducer.cached.get(contents).xpHarvestValue;
        }

        return XP_earned;
    }

    public int handleHarvest(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_taskClassName, String in_plantState, String in_family)
/**
 * ABSTRACT
 * swap((Ptl,Bfl),(Pl,Avl))
 *
 * IN
 * params.farmID
 * params.X
 * params.Y
 * params.plantState
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 */
    {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =handleHarvest=");
        int yield;

        // what is it
        String db_sql_read_PlotList_harvest =
                " SELECT Contents, LastWatering IS NOT NULL AS Watered, PlotList.Repeatable "
                        + " FROM PlotList "
                        + " WHERE PlotList.FarmID=" + in_farmID + " AND PlotList.X=" + in_X + " AND PlotList.Y=" + in_Y;

        String crop = null;
        String plant = null;
        byte watered = 0;
        int repeatable = 0;
        ResultSet queryRes = ds.query(db_sql_read_PlotList_harvest, "read", in_facebookuser);
        try {
            if (queryRes.next()) {
                plant = queryRes.getString("Contents");
                watered = queryRes.getByte("Watered");
                repeatable = queryRes.getInt("Repeatable");
            }
        } catch (SQLException e) {
        }

        crop = dataProducer.cached.get(plant).produce;
        if ((in_plantState.equals("dead"))) {
            if ((watered == 1)) {
                yield = dataProducer.cached.get(plant).wetWitheredYield;
            } else {
                yield = dataProducer.cached.get(plant).dryWitheredYield;
            }
        } else {
            if ((watered == 1)) {
                yield = dataProducer.cached.get(plant).wetYield;
            } else {
                yield = dataProducer.cached.get(plant).dryYield;
            }
        }

        // store it
        // add(Pl)
        storage.add(in_facebookuser, in_farmID, crop, yield);
        // add(Avl)
        achievement.add(in_facebookuser, in_farmID, crop, yield);


        // remove it
        // add(-Ptl)
        String db_sql_write_PlotList_harvest_repeatable = " UPDATE PlotList "
                + " SET LastWatering=NULL, LastSave=Now(), Vitality=0, Repeatable = if(Repeatable > 0, Repeatable-1, -1), MysterySeed=0 "
                + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y;
        String db_sql_write_PlotList_harvest = " REPLACE INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, State ) "
                + " VALUES ( " + in_farmID + ", " + in_X + ", " + in_Y + ", '" + in_taskClassName + "', 'Harvest', Now(), Now(), '' )";


        if (repeatable != 0)
        // leave the crop on the plot and reset all growth parameters
        // keep the State
        {
            ds.execute(db_sql_write_PlotList_harvest_repeatable, "write", in_facebookuser);
        } else
        // remove the crop from the plot
        // overwrite plot: State='', Task=HarvestTask, Contents=Harvest
        {
            ds.execute(db_sql_write_PlotList_harvest, "write", in_facebookuser);
        }
        // add(-Bfl)
        buff.sub(in_facebookuser, in_farmID, in_X, in_Y, null, 1);


        // LOG
        //
        ds.execute("INSERT INTO `log` VALUES ( Now(), 'Harvest','" + in_facebookuser + "','" + in_farmID + "','" + plant + "'," + yield + ", 'N',null,1 )"
                , "log", in_facebookuser);

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =handleHarvest=");

        return 1;
    }


    private void fertilizePlot(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_type)
/**
 * ABSTRACT
 * what is the responsibility of this function?
 * inserts new ActiveBuffs * (number existing ActiveBuffs)
 * updates PlotList * (number existing ActiveBuffs)
 * grows plot * (number existing ActiveBuffs)
 *
 * IN
 *	params.userDBGroup
 *	params.farmDBGroup
 *	params.farmID
 *	X,Y
 *	type
 *	params.user
 * OUT
 * RETURN
 *
 * PRE
 * Plot(x,y) allows multiple ActiveBuffs
 *
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:medium
 */
    {

        //
        int user_timeMultiplier = 1;
        user_timeMultiplier = farmer.getTimeMultiplier(in_facebookuser, in_farmID);


        ResultSet db_res_plotList = ds.query(
                " SELECT PlotList.FarmID, PlotList.X, PlotList.Y, PlotList.Contents, PlotList.Vitality "
                        + ", if ( (ActiveBuffs.X >= 0), 1, 0 ) AS HasBuff "
                        + ", TIMESTAMPDIFF( SECOND, LastSave, Now() ) As ElapsedTime "
                        + ", DATE_FORMAT(CreateDate,'%a %b %d %k:%i:%s GMT-0800 %Y') as Planted "
                        + ", LastWatering IS NOT NULL as Watered "
                        + " FROM PlotList LEFT JOIN ActiveBuffs ON ActiveBuffs.FarmID=PlotList.FarmID AND ActiveBuffs.X=PlotList.X AND ActiveBuffs.Y=PlotList.Y "
                        + " WHERE PlotList.FarmID=" + in_farmID + " AND PlotList.X=" + in_X + " AND PlotList.Y=" + in_Y
                , "read", in_facebookuser);

        int i = 0;
        try {
            while (db_res_plotList.next()) {
                growRow(in_facebookuser, db_res_plotList, user_timeMultiplier);

                ds.execute(" UPDATE PlotList SET Vitality=" + db_res_plotList.getInt("Vitality") + ", LastSave=Now() "
                        + " WHERE X=" + in_X + " AND Y=" + in_Y
                        + " AND FarmID=" + in_farmID, "write", in_facebookuser);
                buff.add(in_facebookuser, in_farmID, in_X, in_Y, in_type, 1);

                i++;
            }
            if (t.verbose && (i == 0)) t.trace("assert failure Plot X,Y=" + in_X + " " + in_Y + " does not exist");
            if (t.verbose && (i > 1)) t.trace("assert failure Plot fertilized and grown=" + i + " times");
        } catch (SQLException e) {
        }

    }


    public int growRow(String in_facebookuser, ResultSet rowToGrow, int user_timeMultiplier)
/**
 * ABSTRACT
 * need to grow plot in intervals .. just in case farmer bought an ActiveBuff when after Vitality>0
 * ActiveBuff afffects growth of plant only in the future and not backwards in time
 * 	planted .. normal grow .. buy ActiveBuff .. faster grow
 *
 * IN
 *	rowToGrow.Contents
 *	rowToGrow.HasBuff
 *	rowToGrow.FarmID
 *	rowToGrow.X
 *	rowToGrow.Y
 *	rowToGrow.ElapsedTime
 *	user_TimeMultiplier:int
 *	in_dbgroup:int
 * INOUT
 *	rowToGrow.Vitality
 * OUT
 * RETURN
 *
 * PRE
 * Plot(x,y) allows multiple ActiveBuffs
 *
 * PERFORMANCE_IMPACT
 *	General:high
 *	Frequency:stress
 *	Cost:high
 *
 */
    {
        String in_contents = null;
        int in_elapsedTime = 0;
        byte in_hasBuff = 0;
        int in_farmID = -1;
        int in_X = -1;
        int in_Y = -1;
        int in_vitality = 0;

        try {
            in_contents = rowToGrow.getString("Contents");
            in_elapsedTime = rowToGrow.getInt("ElapsedTime");
            in_hasBuff = rowToGrow.getByte("HasBuff");
            in_farmID = rowToGrow.getInt("FarmID");
            in_X = rowToGrow.getInt("X");
            in_Y = rowToGrow.getInt("Y");
            in_vitality = rowToGrow.getInt("Vitality");
        } catch (SQLException e) {
        }
        if (t.verbose && (t.verbose_level >= 4))
            t.trace("enter function =growRow= " + in_contents + " " + in_elapsedTime + " " + in_vitality);


        // ASSERT
        //
        if ((user_timeMultiplier < 0)) {
            if (t.verbose) t.trace("assert failure timeMultiplier=" + user_timeMultiplier + " undefined");
            user_timeMultiplier = 1;
        }


        Integer productionHours_units_sec;
        int growthPercentage;

        productionHours_units_sec = dataProducer.cached.get(in_contents).productionHours;
        if (productionHours_units_sec == null) {
            if (t.verbose) t.trace("assert failure productionHours=" + productionHours_units_sec + " undefined");
            return 2;
        }
        growthPercentage = 100 * user_timeMultiplier / productionHours_units_sec;

        int deathRate = dataProducer.cached.get(in_contents).productionWitherRates;
        if (deathRate != 0) {
            deathRate = 100 * user_timeMultiplier / deathRate;
        }
        // POST
        // growthPercentage, deathRate, user_timeMultiplier


        // apply the ActiveBuffs
        // BUGS multiple Buffs on a Plot are ignored
        int i = 0;
        if (in_hasBuff == 1) {
            ResultSet db_res_buff = ds.query(
                    " SELECT Title "
                            + " FROM ActiveBuffs "
                            + " WHERE FarmID=" + in_farmID
                            + " AND X=" + in_X + " AND Y=" + in_Y
                    , "read", in_facebookuser);

            String title = null;
            try {
                for (i = 0; db_res_buff.next(); i++) {
                    title = db_res_buff.getString("Title");
                }
                if (t.verbose && (i == 0))
                    t.trace("assert failure ActiveBuff X,Y=" + in_X + " " + in_Y + " does not exist");
                if (t.verbose && (i > 0)) t.trace("growthPercentage modified=" + i + " times");
            } catch (SQLException e) {
            }

            growthPercentage = (int) (growthPercentage * dataBuff.cached.get(title).growthMod);
            deathRate = (int) (deathRate * dataBuff.cached.get(title).deathMod);
        }


        //
        // percentage of ProductionHours reached
        int plantGrowth = in_vitality;
        // time elapsed since last calculation of plantGrowth
        int elapsedTime = in_elapsedTime;

        if (elapsedTime > 0) {
            plantGrowth = (plantGrowth) + (elapsedTime * growthPercentage);

            if ((plantGrowth > 100) && (dataProducer.cached.get(in_contents).repeatable != -1)) {
                // plants .. not animals, trees or the pond
                // We calculate here when exactly the plant peaked...
                int timeSpentDying = (plantGrowth - 100) / growthPercentage;
                plantGrowth = 100 + timeSpentDying * deathRate;

                if (plantGrowth > 200) plantGrowth = 200;
            } else if (plantGrowth > 100) {
                plantGrowth = 100;
            }
        }


        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =growRow= plantGrowth=" + plantGrowth);
        // rowToGrow.addItem( "Vitality", plantGrowth );
        return plantGrowth;

    }


    public boolean create(String in_facebookuser, int in_farmID) {
        // FarmID==1 is Template for Clone
        // add to Cached Data
        int localX = -1;
        int localY = -1;
        String contents = null;
        String task = null;
        String state = "dying";
        int vitality = 0;
        String watering = null;
        String create = "Now()";

        ResultSet db_res_PlotList = ds.query(" SELECT * FROM PlotList WHERE FarmID=1 ", "read", in_facebookuser);
        try {
            while (db_res_PlotList.next()) {
                localX = db_res_PlotList.getInt("X");
                localY = db_res_PlotList.getInt("Y");
                contents = db_res_PlotList.getString("Contents");
                task = db_res_PlotList.getString("Task");

                if (localY == 2 && localX == 4) {
                    vitality = 99;
                    state = "growing";
                    watering = null;
                    create = "SUBTIME( Now(), '4:02:00' )";
                } else if (localY == 3) {
                    vitality = 0;
                    state = "growing";
                    watering = null;
                    create = "Now()";
                } else if (localY == 2 && localX == 5) {
                    vitality = 99;
                    state = "growing";
                    watering = "Now()";
                    create = "SUBTIME( Now(), '4:02:00' )";
                } else if (localY == 2 && localX == 6) {
                    vitality = 0;
                    state = "growing";
                    watering = null;
                    create = "SUBTIME( Now(), '24:00:00' )";
                }

                String db_sql_write_PlotList_new =
                        " INSERT INTO PlotList "
                                + "( FarmID, X, Y, Contents, CreateDate, Task, State, Vitality, LastSave, LastWatering ) "
                                + " VALUES ( "
                                + in_farmID + "," + localX + "," + localY + ", " + "'" + contents + "'"
                                + ", " + create + ",'" + task + "','" + state + "'," + vitality + ", Now() "
                                + ", " + watering + ")";

                ds.execute(db_sql_write_PlotList_new, "write", in_facebookuser);
            }
        } catch (SQLException e) {
        }

        return true;
    }


    public void areaOver(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_bitMatrix) {
        areaWork(in_facebookuser, in_farmID, in_X, in_Y, in_bitMatrix, 1);
    }

    public void areaClear(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_bitMatrix) {
        areaWork(in_facebookuser, in_farmID, in_X, in_Y, in_bitMatrix, -1);
    }


    private void areaWork(String in_facebookuser, int in_farmID, int in_X, int in_Y, int in_bitMatrix, int in_amount)
// free the plots under an area
// buildings are added to the PlotList with a single row
// the plot occupied is the upper left corner of the building
// param1 is a serialized bit-matrix where 1's mark the plots covered by the building
// for example a 2x3-plot building:
//	3171 = 000110001100011
    {
        if (t.verbose && (t.verbose_level >= 4))
            t.trace("enter function =plotList_areaWork=" + in_farmID + " " + in_X + " " + in_Y);

        int footprint = in_bitMatrix;
        int i;
        int x = 1;
        int y = 0;

        for (i = 2; i <= footprint; i = i << 1) {
            if ((i & footprint) != 0 && ((in_amount) > 0)) {
                ds.execute(" REPLACE INTO PlotList ( FarmID, X, Y, Task, Contents, CreateDate, LastSave, State, MysterySeed, Repeatable ) "
                                + " VALUES ( " + in_farmID + ", " + (x + in_X) + ", " + (y + in_Y)
                                + ", 'foundation', 'foundation', Now(), Now(), '', 0, 0 )"
                        , "write", in_facebookuser);

                buff.sub(in_facebookuser, in_farmID, (x + in_X), (y + in_Y), null, 1);
            } else if ((i & footprint) != 0 && ((in_amount) < 0)) {
                deletePlotContents(in_facebookuser, in_farmID, (x + in_X), (y + in_Y));

                buff.sub(in_facebookuser, in_farmID, (x + in_X), (y + in_Y), null, 1);
            }

            ++x;
            if (x == 5) {
                x = 0;
                ++y;
            }
        }

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =plotList_areaWork=");
    }


    public void deletePlotContents(String in_facebookuser, int in_farmID, int in_X, int in_Y) {
        ds.execute("DELETE FROM PlotList " + " WHERE FarmID=" + in_farmID + " AND X=" + in_X + " AND Y=" + in_Y
                , "write", in_facebookuser);
    }


    public void deleteAreaContents(String in_facebookuser, int in_farmID, int in_tilePointColumn, int in_tilePointRow, int in_columnWidth, int in_rowHeight) {
        ds.execute(" DELETE FROM PlotList "
                        + " WHERE FarmID=" + in_farmID
                        + " AND X>=" + in_tilePointColumn + " AND X<" + (in_tilePointColumn + in_columnWidth)
                        + " AND Y>=" + in_tilePointRow + " AND Y<" + (in_tilePointRow + in_rowHeight)
                , "write", in_facebookuser);

        ds.execute(" DELETE FROM ActiveBuffs "
                        + " WHERE FarmID=" + in_farmID
                        + " AND X>=" + in_tilePointColumn + " AND X<" + (in_tilePointColumn + in_columnWidth)
                        + " AND Y>=" + in_tilePointRow + " AND Y<" + (in_tilePointRow + in_rowHeight)
                , "write", in_facebookuser);
    }


}
