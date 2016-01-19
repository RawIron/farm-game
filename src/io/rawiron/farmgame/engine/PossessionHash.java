package io.rawiron.farmgame.engine;

import java.sql.ResultSet;


public interface PossessionHash {
	public ResultSet retrieve(String key);
	public int add(String key, Possession [] item);
	public int sub(String key, Possession [] item);
}
