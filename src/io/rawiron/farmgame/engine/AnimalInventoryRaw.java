package io.rawiron.farmgame.engine;


import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;

import java.sql.ResultSet;
import java.sql.SQLException;


public class AnimalInventoryRaw implements IAnimalInventory {

    private Trace t;
    private DataStore ds;

    public AnimalInventoryRaw(DataStore engine, Trace trace) {
        t = trace;
        ds = engine;
    }

    public boolean add(String playerId, AnimalItem animal) {
        if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 3))
            t.trace("variable dump - task - add animal to inventory: animal_id=" + animal.id);

        int success = 0;
        int new_animalanimal_id = 0;

        if (animal.id != null) {
            success = ds.execute(" UPDATE animal_list SET " +
                    "LastHarvest=Now(), Progress=" + animal.progress +
                    " WHERE player_id=" + animal.playerId + " AND animal_id=" + animal.id, "write", playerId);

        } else if (animal.id == null) {
            success = ds.execute(" INSERT INTO animal_list ( player_id, animal_id, animal_name, x, y, last_harvest) "
                    + " VALUES ( " + animal.playerId + ", " + animal.id + ", '" + animal.name + "', " + animal.X + ", " + animal.Y + ", Now()) ", "write", playerId);

            // doh ..
            ResultSet db_res_animalanimal_id = ds.query("SELECT animal_id FROM animal_list " +
                    "WHERE player_id=" + animal.playerId + " AND Progress=-1", "write", playerId);
            try {
                if (db_res_animalanimal_id.next()) new_animalanimal_id = db_res_animalanimal_id.getInt("animal_id");
            } catch (SQLException e) {
            }

                ds.execute("UPDATE animal_list SET Progress=" + animal.progress +
                    " WHERE player_id=" + animal.playerId + " AND Progress=-1", "write", playerId);
        }

        return true;
    }

    public boolean sub(String playerId, AnimalItem animal) {
        ds.execute(" DELETE FROM animal_list WHERE player_id=" + animal.playerId +
                " AND animal_id=" + animal.id, "write", playerId);
        return true;
    }

    public ResultSet retrieve(String playerId) {
        // BUGS Progress is not calculated using this.harvest()
        String db_sql_read_animal_list =
                String.format(" SELECT animal_id, animal_name, x, y, player_id , " +
                        "TIMESTAMPDIFF( SECOND, last_harvest, Now() ) / ProductionHours * 100 AS progress" +
                        "FROM animal_list LEFT JOIN Producers ON animal_name=Name  " +
                        "WHERE player_id='%s'", playerId);

        return ds.query(db_sql_read_animal_list, "read", playerId);
    }

    public AnimalItem getAnimal(String playerId, Integer animalId) {
        String sql =
                String.format(" SELECT player_id, animal_id, animal_name, last_harvest, " +
                        "TIMESTAMPDIFF( SECOND, last_harvest, Now() ) / ProductionHours * 100 AS progress, " +
                        "TIMESTAMPDIFF( SECOND, last_harvest, Now() ) AS ElapsedTime,  " +
                        "X, Y " +
                        "FROM animal_list INNER JOIN Producers ON animal_name=Name WHERE player_id='%s' AND animal_id=%d", playerId, animalId);
        ResultSet result = ds.query(sql, "read", playerId);

        AnimalItem animal = null;
        try {
            if (result.next()) {
                animal = new AnimalItem();
                animal.playerId = result.getString("player_id");
                animal.id = result.getInt("animal_id");
                animal.name = result.getString("animal_name");
                animal.lastHarvest = result.getInt("last_harvest");
                animal.progress = result.getInt("progress");
                animal.elapsedTime = result.getInt("ElapsedTime");
                animal.X = result.getInt("x");
                animal.Y = result.getInt("y");
            }
        } catch (SQLException e) {
        }

        return animal;
    }

    public boolean replace(String playerId, AnimalItem animal) {
        ds.execute(" UPDATE animal_list SET " +
                "last_harvest = " + animal.lastHarvest +
                "x = " + animal.X +
                "y = " + animal.Y +
                " WHERE player_id=" + animal.playerId + " AND animal_id=" + animal.id, "write", playerId);
        return true;
    }

    public int goldValue(String playerId) {
        String sql =
                " SELECT SUM( FLOOR( GoldCost * AnimalSaleRatio) ) AS GoldTotal " +
                        " FROM animal_list INNER JOIN Unlockables ON animal_name=Animal " +
                        " INNER JOIN GameSettings WHERE player_id=" + playerId;

        int gold = 0;
        ResultSet result = ds.query(sql, "write", playerId);
        try {
            if (result.next()) gold = result.getInt("GoldTotal");
        } catch (SQLException e) {
        }

        return gold;
    }
}
