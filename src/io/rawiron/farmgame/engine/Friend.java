package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Friend {

    private DataStore ds;
    private Trace t;
    private Logging l;

    private Achievement achievement;
    private Farmer farmer;


    public Friend(DataStore in_ds, Logging in_l, Trace in_t) {
        ds = in_ds;
        t = in_t;
        l = in_l;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }


    public ResultSet retrieve(String in_facebookuser, String in_where) {
        String sql =
                " SELECT Friends.* "
                        + ", FarmerIndex.Level, FarmerIndex.PlayerName, FarmerIndex.Gender, FarmerIndex.HairStyle, FarmerIndex.SkinTone, FarmerIndex.Clothing "
                        + ", timestampdiff( hour, LastChoreTime, Now()) as ChoreHoursAgo  "
                        + " FROM Friends INNER JOIN FarmerIndex ON Friend=FacebookUser "
                        + " WHERE Farmer=" + "'" + in_facebookuser + "'";

        if (in_where != null && in_where.equals("active")) {
            sql += " AND Status='active' ";
        } else if (in_where != null && in_where.equals("inactive")) {
            sql += " AND Status='inactive' ";
        }

        return ds.query(sql, "read", in_facebookuser);
    }


    public int doChore(String in_facebookuser, String in_friend) {
        boolean hoursPassed = false;

        String db_sql_read_Chore =
                " SELECT timestampdiff( hour, LastChoreTime, Now() ) As ChoreHoursPassed "
                        + " FROM Friends "
                        + " WHERE Farmer='" + in_facebookuser + "' AND Friend='" + in_friend + "'";

        ResultSet friendQuery = ds.query(db_sql_read_Chore, "write", in_facebookuser);
        try {
            if (friendQuery.next() && (friendQuery.getInt("ChoreHoursPassed") >= 8))
                hoursPassed = true;
        } catch (SQLException e) {
        }


        if (hoursPassed) {
            int playerID = 0;
            playerID = farmer.getPlayerID(in_facebookuser);
            achievement.add(in_facebookuser, playerID, "DoChore", 1);
            ds.execute(" UPDATE Friends SET LastChoreTime=Now() "
                            + " WHERE Farmer='" + in_facebookuser + "' AND Friend='" + in_friend + "'"
                    , "write", in_facebookuser);
        }

        return 1;
    }


    public void remove(String in_facebookuser, String in_friend) {
        ds.execute("UPDATE Friends SET Status='inactive' WHERE Farmer='" + in_facebookuser + "' AND Friend='" + in_friend + "'", "write", in_facebookuser);
    }

    public int count(String in_facebookuser) {
        ResultSet db_res_FriendCount = ds.query("SELECT FriendCount FROM Farmers WHERE FacebookUser=" + "'" + in_facebookuser + "'"
                , "read", in_facebookuser);

        int FriendCount = 0;
        try {
            if (db_res_FriendCount.next()) {
                FriendCount = db_res_FriendCount.getInt("FriendCount");
            }
        } catch (SQLException e) {
        }

        return FriendCount;
    }


    public void updateCount(String in_facebookuser, int in_farmID, int in_fCount) {
        String db_sql_write_Farmers = "UPDATE Farmers SET FriendCount=" + in_fCount + " WHERE PlayerID=" + in_farmID;
        ds.execute(db_sql_write_Farmers, "write", in_facebookuser);
    }

    public void add(String in_facebookuser, String in_friend, String in_friendName, int in_farmID) {
        if (l.log_data_read)
            ds.execute("INSERT INTO `log` VALUES ( Now(), 'addFriend','" + in_facebookuser + "','" + in_farmID + "','" + in_friend + "',0,'L',null,1)", "log", null);

        ResultSet db_res_friends = ds.query(" SELECT Farmer FROM Friends "
                        + " WHERE Farmer='" + in_facebookuser + "' AND Friend='" + in_friend + "'"
                , "read", in_facebookuser);

        boolean hasFriend = false;
        try {
            if (db_res_friends.next())
                hasFriend = true;
        } catch (SQLException e) {
        }

        if (hasFriend) {
            ds.execute("UPDATE Friends SET Status='active' WHERE Farmer='" + in_facebookuser + "' AND Friend='" + in_friend + "'"
                    , "write", in_facebookuser);
        } else {
            ResultSet db_res_index = ds.query(" SELECT FacebookUser FROM FarmerIndex "
                            + " WHERE FacebookUser='" + in_friend + "'"
                    , "read", in_facebookuser);

            boolean hasNewFriend = false;
            try {
                if (db_res_index.next())
                    hasNewFriend = true;
            } catch (SQLException e) {
            }

            if (hasNewFriend) {
                ds.execute("INSERT INTO Friends ( Farmer, Friend, Name, Added, LastChoreTime ) "
                                + " VALUES ('" + in_facebookuser + "','" + in_friend + "','" + in_friendName + "', Now(), '2009/01/01 12:00:00' )"
                        , "write", in_facebookuser);
            }
        }
    }

/*
public void addFriends( params )
{
		t.trace( "friendList"+params.friendList );
		t.trace( "friendList n="+params.friendList.length() );
}
*/

}
