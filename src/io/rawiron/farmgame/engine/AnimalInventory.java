package io.rawiron.farmgame.engine;


import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;

import java.sql.ResultSet;
import java.sql.SQLException;


public class AnimalInventory {

    private Trace t;
    private DataStore ds;

    public AnimalInventory(DataStore engine, Trace tracer) {
        t = tracer;
        ds = engine;
    }

    public boolean add(String playerId, AnimalItem animal) {
        if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 3))
            t.trace("variable dump - task - add animal to inventory: animalID=" + animal.id);

        int success = 0;
        int new_animalID = 0;

        if (animal.id != null) {
            success = ds.execute(" UPDATE AnimalList SET " +
                    "LastHarvest=Now(), Progress=" + animal.progress +
                    " WHERE FarmID=" + animal.playerId + " AND ID=" + animal.id, "write", playerId);

        } else if (animal.id == null) {
            success = ds.execute(" INSERT INTO AnimalList ( FarmID, Animal, X, Y, LastHarvest, Progress) "
                    + " VALUES ( " + animal.playerId + ", '" + animal.name + "', " + animal.X + ", " + animal.Y + ", Now(), -1 ) ", "write", playerId);

            // doh ..
            ResultSet db_res_animalID = ds.query("SELECT ID FROM AnimalList " +
                    "WHERE FarmID=" + animal.playerId + " AND Progress=-1", "write", playerId);
            try {
                if (db_res_animalID.next()) new_animalID = db_res_animalID.getInt("ID");
            } catch (SQLException e) {
            }

            ds.execute("UPDATE AnimalList SET Progress=" + animal.progress +
                    " WHERE FarmID=" + animal.playerId + " AND Progress=-1", "write", playerId);
        }

        return true;
    }

    public boolean sub(String playerId, AnimalItem animal, int in_amount) {
        if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 0) && (in_amount < 0))
            t.trace("assert failure in_amount=" + in_amount + " is <0");

        ds.execute(" DELETE FROM AnimalList WHERE FarmID=" + animal.playerId + " AND ID=" + animal.id, "write", playerId);
        return true;
    }

    public ResultSet retrieve(String in_facebookuser) {
        // BUGS Progress is not calculated using this.harvest()
        String db_sql_read_AnimalList =
                String.format(" SELECT AnimalList.ID, Animal, X, Y, FarmID , " +
                        "TIMESTAMPDIFF( SECOND, LastHarvest, Now() ) / ProductionHours * 100 AS Progress" +
                        "FROM AnimalList LEFT JOIN Producers ON Animal=Name  " +
                        "WHERE FarmID='%s'", in_facebookuser);

        return ds.query(db_sql_read_AnimalList, "read", in_facebookuser);
    }

    public AnimalItem getAnimal(String playerId, Integer animalId) {
        String sql =
                String.format(" SELECT FarmID, ID, Animal, LastHarvest, " +
                        "TIMESTAMPDIFF( SECOND, LastHarvest, Now() ) / ProductionHours * 100 AS Progress, " +
                        "TIMESTAMPDIFF( SECOND, LastHarvest, Now() ) AS ElapsedTime,  " +
                        "X, Y " +
                        "FROM AnimalList INNER JOIN Producers ON Animal=Name WHERE FarmID='%s' AND ID=%d", playerId, animalId);
        ResultSet result = ds.query(sql, "read", playerId);

        AnimalItem animal = null;
        try {
            if (result.next()) {
                animal = new AnimalItem();
                animal.playerId = result.getString("FarmID");
                animal.id = result.getInt("ID");
                animal.name = result.getString("Animal");
                animal.lastHarvest = result.getInt("LastHarvest");
                animal.progress = result.getInt("Progress");
                animal.elapsedTime = result.getInt("ElapsedTime");
                animal.X = result.getInt("X");
                animal.Y = result.getInt("Y");
            }
        } catch (SQLException e) {
        }

        return animal;
    }

    public boolean replace(String playerId, AnimalItem animal) {
        ds.execute(" UPDATE AnimalList SET " +
                "LastHarvest = " + animal.lastHarvest +
                "X = " + animal.X +
                "Y = " + animal.Y +
                " WHERE FarmID=" + animal.playerId + " AND ID=" + animal.id, "write", playerId);
        return true;
    }

    public int goldValue(String in_facebookuser) {
        String sql =
                " SELECT SUM( FLOOR( GoldCost * AnimalSaleRatio) ) AS GoldTotal "
                        + " FROM AnimalList INNER JOIN Unlockables ON Name=Animal INNER JOIN GameSettings WHERE FarmID=" + in_facebookuser;

        int gold = 0;
        ResultSet result = ds.query(sql, "write", in_facebookuser);
        try {
            if (result.next()) gold = result.getInt("GoldTotal");
        } catch (SQLException e) {
        }

        return gold;
    }
}
