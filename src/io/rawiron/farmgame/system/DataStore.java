package io.rawiron.farmgame.system;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import it.gotoandplay.smartfoxserver.db.*;


public interface DataStore {
    public boolean addPooledConnection(String dataServerKey, DbManager connectPool);

    public boolean closeAll();

    public HashMap<String, Connection> retrieve();

    public int execute(String statement, String Region, String key);

    public ResultSet query(String statement, String Region, String key);
}