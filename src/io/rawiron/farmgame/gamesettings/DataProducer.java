package io.rawiron.farmgame.gamesettings;

import io.rawiron.farmgame.system.DataStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;



public class DataProducer {
	public HashMap<String,DataItemProducer> cached = new HashMap<String,DataItemProducer>();

	public DataProducer(DataStore ds)
	{
	    String db_sql_read_Producer =
	    	" SELECT Producers.Name, Repeatable, XPHarvestValue "
	    	+ ", ProductionHours, WitherTime, Repeatable, NeedTilled, Type, DryYield, DryWitheredYield, WetYield, WetWitheredYield "
	        + " FROM Producers INNER JOIN Unlockables ON Producers.Name=Unlockables.Name";

	    ResultSet db_res = ds.query( db_sql_read_Producer, "read", null );
	    try {
		    while (db_res.next())
		    {
				DataItemProducer producer = new DataItemProducer();
				producer.produce = db_res.getString("Produce");
				producer.repeatable = db_res.getInt("Repeatable");
				producer.xpHarvestValue = db_res.getInt("XPHarvestValue");
				producer.productionHours = db_res.getInt("ProductionHours");
				producer.productionWitherRates = db_res.getInt("WitherTime");
				producer.wetWitheredYield = db_res.getInt("WetWitheredYield");
				producer.dryWitheredYield = db_res.getInt("DryWitheredYield");
				producer.wetYield = db_res.getInt("WetYield");
				producer.dryYield = db_res.getInt("DryYield");

				cached.put(db_res.getString("Name"), producer);
		    }
	    } catch (SQLException e) {}
	}
}
