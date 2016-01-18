package io.rawiron.farmgame.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataProduct {
	private DataStore ds;
	public HashMap<String,DataItemProduct> cached = new HashMap<String,DataItemProduct>();

	public DataProduct(DataStore in_ds)
	{
		ds=in_ds;

	    // read from DataStore
	    String db_sql_read_Product =
	    	" SELECT Name, CoinsValue, GoldValue "
	        + " FROM Products ";

	    DataItemProduct product = null;
	    ResultSet db_res_product = ds.query( db_sql_read_Product, "read", null );
	    try {
		    while (db_res_product.next())
		    {
		        product = new DataItemProduct();
				product.coinsValue = db_res_product.getInt("CoinsValue");
				product.goldValue = db_res_product.getInt("GoldValue");

				cached.put(db_res_product.getString("Name"), product);
		    }
	    } catch (SQLException e) {}
	}
}
