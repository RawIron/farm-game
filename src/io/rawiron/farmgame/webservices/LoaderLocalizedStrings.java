package io.rawiron.farmgame.webservices;


import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;
import io.rawiron.farmgame.system.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;

public class LoaderLocalizedStrings {

    private DataStore ds;

    public JSONObject getStringTable(String in_language) {
        Trace.trace("enter function =getStringTable= ");

        String db_sql_read_Localization = "SELECT `Key`,`" + in_language + "`,'" + in_language + "' AS Language FROM Localization";
        ResultSet db_res_localize = ds.query(db_sql_read_Localization, "read", null);

        // response = {};
        JSONObject response = new JSONObject();
        // response.fullResult = []
        JSONArray fullResult = new JSONArray();

        // response.fullResult[i] = queryRes.get(i);
        Util.appendResult(fullResult, db_res_localize, 0);
        try {
            response.put("fullResult", fullResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Trace.trace("exit function =getStringTable= ");
        return response;
    }
}
