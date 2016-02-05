package io.rawiron.farmgame.system;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;


public class Util {

    public static int appendResult(JSONArray jsoArray, ResultSet rs, int in_startindex) {
        return appendResultCount(jsoArray, rs, in_startindex, 0);
    }

    public static int appendResultCount(JSONArray jsoArray, ResultSet rs, int in_startindex, int in_count) {
        if (in_startindex < 0) {
            return -1;
        }

        boolean countDown = false;
        int rowNumber = 0;
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            if (in_startindex > 0) {
                rowNumber = in_startindex;
            }
            if (in_count > 0) {
                countDown = true;
            }

            String columnName = null;
            int columnType = 0;
            int i = 0;

            String stringValue = null;
            short shortValue = 0;
            int intValue = 0;
            long longValue = 0;
            double doubleValue = 0.0;
            boolean booleanValue;
            // careful: order in the below expression is critical
            //	only move the cursor to the next position when first expression is true
            while (((!countDown) || (countDown && in_count > 0)) && (rs.next())) {
                try {
                    JSONObject jso = new JSONObject();
                    for (i = 1; i <= colCount; i++) {
                        columnName = rsmd.getColumnName(i);
                        columnType = rsmd.getColumnType(i);
                        switch (columnType) {
                            case Types.VARCHAR:
                                stringValue = rs.getString(i);
                                jso.put(columnName, stringValue);
                                break;
                            case Types.CHAR:
                                stringValue = rs.getString(i);
                                jso.put(columnName, stringValue);
                                break;
                            case Types.TINYINT:
                                shortValue = rs.getShort(i);
                                jso.put(columnName, shortValue);
                                break;
                            case Types.SMALLINT:
                                shortValue = rs.getShort(i);
                                jso.put(columnName, shortValue);
                                break;
                            case Types.INTEGER:
                                intValue = rs.getInt(i);
                                jso.put(columnName, intValue);
                                break;
                            case Types.BIGINT:
                                longValue = rs.getLong(i);
                                jso.put(columnName, longValue);
                                break;
                            case Types.BOOLEAN:
                                booleanValue = rs.getBoolean(i);
                                jso.put(columnName, (int) (booleanValue ? 1 : 0));
                                break;
                            case Types.FLOAT:
                                doubleValue = rs.getDouble(i);
                                jso.put(columnName, doubleValue);
                                break;
                            case Types.DOUBLE:
                                doubleValue = rs.getDouble(i);
                                jso.put(columnName, doubleValue);
                                break;
                            default:
                                stringValue = rs.getString(i);
                                jso.put(columnName, stringValue);
                                break;
                        }
                    }
                    jsoArray.put(rowNumber, jso);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                rowNumber++;
                in_count--;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 3)) Trace.trace("exit function  =appendResult=" + rowNumber);
        return (rowNumber - in_startindex);
    }
}
