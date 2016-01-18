package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataBuff {
	private DataStore ds;
	public HashMap<String,DataItemBuff> cached = new HashMap<String,DataItemBuff>();

	public DataBuff(DataStore in_ds)
	{
	    // read from DataStore
	    String db_sql_read_Buff =
	    	" SELECT Title, GrowthMod, DeathMod "
	        + " FROM Buffs ";

	    DataItemBuff buff = null;
	    ResultSet db_res_buff = ds.query( db_sql_read_Buff, "read", null );
	    try {
		    while (db_res_buff.next())
		    {
		        buff = new DataItemBuff();
				buff.growthMod = db_res_buff.getDouble("GrowthMod");
				buff.deathMod = db_res_buff.getDouble("DeathMod");

				cached.put(db_res_buff.getString("Title"), buff);
		    }
	    } catch (SQLException e) {}

	}
}
