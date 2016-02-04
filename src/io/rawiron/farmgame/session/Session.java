package io.rawiron.farmgame.session;

import io.rawiron.farmgame.engine.BalanceSheet;
import io.rawiron.farmgame.engine.Game;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Session {

    private Trace t;
    private DataStore ds;
    private Game game;

    public int login(String in_facebookuser, String in_userName, String in_skey, BalanceSheet inout_gold, BalanceSheet inout_coins) {
        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("enter function =game.login=" + in_facebookuser);

        String db_sql_read_FarmerIndex =
                " SELECT PlayerID, skey, (DAY(LastDailyReward) = DAY(Now())) AS SameDay "
                        + " FROM FarmerIndex WHERE FacebookUser=" + "'" + in_facebookuser + "'";

        boolean gotSkey = false;
        String db_skey = null;

        boolean userExists = false;
        int ID = -1;
        byte sameDay = 0;

        ResultSet db_res_index = ds.query(db_sql_read_FarmerIndex, "read", in_facebookuser);
        try {
            if (db_res_index.next()) {
                db_skey = db_res_index.getString("skey");
                gotSkey = true;

                ID = db_res_index.getInt("PlayerID");
                sameDay = db_res_index.getByte("SameDay");
                userExists = true;
            }
        } catch (SQLException e) {
        }

        if (gotSkey && (db_skey != null && !db_skey.equals("") && !db_skey.equals(in_skey))) {
            return 2;
        }


        int rc = -1;

        if (userExists) {
            // EXISTING USER
            if (sameDay != 1) {
                game.dailyReward(in_facebookuser, in_userName, inout_gold, inout_coins);
                rc = 1;
            }

        } else {
            // NEW USER
            if (in_skey == null || in_skey.equals("null")) {
                in_skey = "";
            }

            game.init(in_facebookuser, in_userName, in_skey);
            rc = 0;
        }

        if (Trace.verbose && (Trace.verbose_level >= 4)) t.trace("exit function =game.login=" + rc);
        return rc;
    }
}
