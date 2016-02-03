package io.rawiron.farmgame.gamesettings;

import io.rawiron.farmgame.system.DataStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DataProduct {
	public HashMap<String,DataItemProduct> cached = new HashMap<String,DataItemProduct>();

	public DataProduct(DataStore ds)
	{
	    String db_sql_read_Product =
	    	" SELECT Name, CoinsValue, GoldValue "
	        + " FROM Products ";

	    ResultSet db_res_product = ds.query( db_sql_read_Product, "read", null );
	    try {
		    while (db_res_product.next())
		    {
				DataItemProduct product = new DataItemProduct();
				product.name = db_res_product.getString("Name");
				product.coinsValue = db_res_product.getInt("CoinsValue");
				product.goldValue = db_res_product.getInt("GoldValue");

				cached.put(product.name, product);
		    }
	    } catch (SQLException e) {}
	}
}
