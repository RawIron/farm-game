package io.rawiron.farmgame.gamesettings;

import io.rawiron.farmgame.system.DataStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataBuff {
    public HashMap<String, DataItemBuff> cached = new HashMap<String, DataItemBuff>();

    public DataBuff(DataStore engine) {
        String sql =
                " SELECT Title, GrowthMod, DeathMod "
                        + " FROM Buffs ";

        ResultSet db_res_buff = engine.query(sql, "read", null);
        try {
            while (db_res_buff.next()) {
                DataItemBuff buff = new DataItemBuff();
                buff.title = db_res_buff.getString("Title");
                buff.growthMod = db_res_buff.getDouble("GrowthMod");
                buff.deathMod = db_res_buff.getDouble("DeathMod");

                cached.put(buff.title, buff);
            }
        } catch (SQLException e) {
        }
    }
}
