package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;



public class DataProducer {
	private DataStore ds;
	public HashMap<String,DataItemProducer> cached = new HashMap<String,DataItemProducer>();

	public DataProducer(DataStore in_ds)
	{
		ds=in_ds;

	    // read from DataStore
	    String db_sql_read_Producer =
	    	" SELECT Producers.Name, Repeatable, XPHarvestValue "
	    	+ ", ProductionHours, WitherTime, Repeatable, NeedTilled, Type, DryYield, DryWitheredYield, WetYield, WetWitheredYield "
	        + " FROM Producers INNER JOIN Unlockables ON Producers.Name=Unlockables.Name";

	    DataItemProducer producer = null;
	    ResultSet db_res_producer = ds.query( db_sql_read_Producer, "read", null );
	    try {
		    while (db_res_producer.next())
		    {
		        producer = new DataItemProducer();
				producer.produce = db_res_producer.getString("Produce");
				producer.repeatable = db_res_producer.getInt("Repeatable");
				producer.xpHarvestValue = db_res_producer.getInt("XPHarvestValue");
				producer.productionHours = db_res_producer.getInt("ProductionHours");
				producer.productionWitherRates = db_res_producer.getInt("WitherTime");
				producer.wetWitheredYield = db_res_producer.getInt("WetWitheredYield");
				producer.dryWitheredYield = db_res_producer.getInt("DryWitheredYield");
				producer.wetYield = db_res_producer.getInt("WetYield");
				producer.dryYield = db_res_producer.getInt("DryYield");

				cached.put(db_res_producer.getString("Name"), producer);
		    }
	    } catch (SQLException e) {}
	}
}
