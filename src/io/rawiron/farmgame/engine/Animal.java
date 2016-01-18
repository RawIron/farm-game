package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.data.DataGameSettings;
import io.rawiron.farmgame.data.DataProducer;
import io.rawiron.farmgame.data.DataStore;
import io.rawiron.farmgame.data.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Animal {

    private Trace t;
    private Logging l;
    private DataStore ds;

    private Achievement achievement;
    private Storage storage;
    private Valuable valuable;
    private Farmer farmer;

    private DataGameSettings dataGameSettings;
    private DataProducer dataProducer;
    private DataUnlockable dataUnlockable;


    public Animal(DataStore in_ds, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        ds = in_ds;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }

    public void setDataUnlockable(DataUnlockable in_du) {
        dataUnlockable = in_du;
    }


    public Boolean harvest(String in_facebookuser, int in_farmID, int in_animalID, String in_animal) {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =animal_handleHarvest=");

        int user_timeMultiplier = 1;
        user_timeMultiplier = farmer.getTimeMultiplier(in_facebookuser);

        String db_sql_read_AnimalList_harvestReady =
                " SELECT Animal, TIMESTAMPDIFF( SECOND, LastHarvest, Now() ) AS ElapsedTime "
                        + " FROM AnimalList "
                        + " WHERE FarmID=" + in_farmID + " AND AnimalList.ID=" + in_animalID;
        ResultSet harvestReady = ds.query(db_sql_read_AnimalList_harvestReady, "read", in_facebookuser);


        float growthPercentage = 0;
        int elapsedTime = 0;
        int db_productionHours_units_sec = 1;
        String animal = null;
        String crop = null;
        int yield = 0;
        try {
            if (harvestReady.next()) {
                elapsedTime = harvestReady.getInt("ElapsedTime");
                animal = harvestReady.getString("Animal");
            }
        } catch (SQLException e) {
        }

        crop = dataProducer.cached.get(animal).produce;
        db_productionHours_units_sec = dataProducer.cached.get(animal).productionHours;
        yield = dataProducer.cached.get(animal).dryYield;
        growthPercentage = ((elapsedTime * user_timeMultiplier * 100) / db_productionHours_units_sec);

        if ((growthPercentage > 99)) {
            storage.add(in_facebookuser, in_farmID, crop, yield);
            achievement.add(in_facebookuser, in_farmID, crop, yield);
            this.add(in_facebookuser, in_farmID, in_animalID, null, null, in_animal, 1, 0);

            ds.execute(" INSERT INTO `log` "
                            + " VALUES ( Now(), 'Harvest'," + "'" + in_facebookuser + "'" + ",'" + in_farmID + "','" + animal + "'," + yield + ", 'N',null,1 )"
                    , "log", null);
        }


        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =animal_handleHarvest=");
        return true;
    }


    public int buy(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_itemName) {
        achievement.add(in_facebookuser, in_farmID, in_itemName, 1);
        return this.add(in_facebookuser, in_farmID, null, in_X, in_Y, in_itemName, 1, 0);
    }

    public int sell(String in_facebookuser, int in_farmID, int in_animalID) {

        String animal = null;
        String db_sql_read_AnimalList_cost =
                " SELECT Animal "
                        + " FROM AnimalList "
                        + " WHERE FarmID=" + in_farmID + " AND AnimalList.ID=" + in_animalID;

        ResultSet db_res_Animal_cost = ds.query(db_sql_read_AnimalList_cost, "read", in_facebookuser);
        try {
            if (db_res_Animal_cost.next()) {
                animal = db_res_Animal_cost.getString("Animal");
            }
        } catch (SQLException e) {
        }


        int earn_amount_gold = 0;
        int cost_amount_coins = 0;
        earn_amount_gold = dataUnlockable.cached.get(animal).goldCost;
        cost_amount_coins = dataUnlockable.cached.get(animal).coinsCost;

        if (earn_amount_gold == 0) {
            earn_amount_gold = (int) (cost_amount_coins * dataGameSettings.cached_CoinsGoldSaleMultiplier);
        }
        earn_amount_gold = (int) (earn_amount_gold * dataGameSettings.cached_AnimalSaleRatio);


        valuable.add(in_facebookuser, in_farmID, 0, earn_amount_gold, 0, 0, 0);
        this.sub(in_facebookuser, in_farmID, in_animalID, 1);

        return 1;
    }

    public int goldValue(String in_facebookuser, int farmID) {
        String db_sql_read_AnimalList_goldValue =
                " SELECT SUM( FLOOR( GoldCost * AnimalSaleRatio) ) AS Gold "
                        + " FROM AnimalList INNER JOIN Unlockables ON Name=Animal INNER JOIN GameSettings WHERE FarmID=" + farmID;

        int gold = 0;
        ResultSet goldQuery = ds.query(db_sql_read_AnimalList_goldValue, "write", in_facebookuser);
        try {
            if (goldQuery.next()) gold = goldQuery.getInt("Gold");
        } catch (SQLException e) {
        }

        return gold;
    }


    public int sub(String in_facebookuser, int in_farmID, int in_animalID, int in_amount)
    /**
     * ABSTRACT
     * sub(Al) := add(-Al)
     *
     */
    {
        if (t.verbose && (t.verbose_level >= 0) && (in_amount < 0))
            t.trace("assert failure in_amount=" + in_amount + " is <0");

        add(in_facebookuser, in_farmID, in_animalID, null, null, null, -in_amount, 0);
        return 1;
    }


    public int add(String in_facebookuser, int in_farmID, Integer in_animalID, Integer in_X, Integer in_Y, String in_animal, int in_amount, int in_progress)
    /**
     * ABSTRACT
     * add(Al)
     *
     * PERFORMANCE_IMPACT
     *	General:high
     *	Frequency:stress
     *	Cost:low
     */
    {
        if (t.verbose && (t.verbose_level >= 3))
            t.trace("variable dump  task animalID=" + in_animalID + " in_amount=" + in_amount);


        int success = 0;
        int new_animalID = 0;

        if (in_amount > 0 && (in_animalID != null)) {
            success = ds.execute(" UPDATE AnimalList SET LastHarvest=Now(), Progress=" + in_progress + " WHERE FarmID=" + in_farmID + " AND ID=" + in_animalID, "write", in_facebookuser);
        } else if (in_amount > 0 && (in_animalID == null)) {
            success = ds.execute(" INSERT INTO AnimalList ( FarmID, Animal, X, Y, LastHarvest, Progress) "
                    + " VALUES ( " + in_farmID + ", '" + in_animal + "', " + in_X + ", " + in_Y + ", Now(), -1 ) ", "write", in_facebookuser);

            // doh ..
            ResultSet db_res_animalID = ds.query("SELECT ID FROM AnimalList WHERE FarmID=" + in_farmID + " AND Progress=-1", "write", in_facebookuser);
            try {
                if (db_res_animalID.next()) new_animalID = db_res_animalID.getInt("ID");
            } catch (SQLException e) {
            }
            ;
            ds.execute("UPDATE AnimalList SET Progress=" + in_progress + " WHERE FarmID=" + in_farmID + " AND Progress=-1", "write", in_facebookuser);

        } else if (in_amount < 0) {
            success = ds.execute(" DELETE FROM AnimalList WHERE FarmID=" + in_farmID + " AND ID=" + in_animalID, "write", in_facebookuser);
        }

        return new_animalID;
    }


    public ResultSet retrieve(String in_facebookuser, int in_farmID) {
        // BUGS Progress is not calculated using this.harvest()
        String db_sql_read_AnimalList =
                " SELECT AnimalList.ID, Animal, X, Y, FarmID "
                        + ", TIMESTAMPDIFF( SECOND, LastHarvest, Now() ) / ProductionHours * 100 AS Progress, Wander "
                        + " FROM AnimalList LEFT JOIN Producers ON Animal=Name "
                        + " WHERE FarmID=" + in_farmID;
        ResultSet queryRes = ds.query(db_sql_read_AnimalList, "read", in_facebookuser);

        return queryRes;
    }


    public int move(String in_facebookuser, int in_farmID, int in_animalID, int in_X, int in_Y, int in_wander) {
        ds.execute(" UPDATE AnimalList SET X=" + in_X + ",Y=" + in_Y + ", Wander=" + in_wander
                        + " WHERE FarmID=" + in_farmID + " AND ID=" + in_animalID
                , "write", in_facebookuser);
        return 1;
    }


}
