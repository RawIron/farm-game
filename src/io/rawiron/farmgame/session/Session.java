package io.rawiron.farmgame.session;

import com.sun.xml.internal.bind.v2.TODO;
import io.rawiron.farmgame.engine.BalanceSheet;
import io.rawiron.farmgame.engine.Game;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Trace;
import io.rawiron.farmgame.system.TraceFactory;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Session {

    private static final Trace t = TraceFactory.create();
    private DataStore ds;
    private Game game;

    public static final int NEW_PLAYER = 0;
    public static final int NEW_SESSION = 1;
    public static final int VALID_SESSION = 2;

    public Session(DataStore ds, Game game) {
        this.ds = ds;
        this.game = game;
    }

    public boolean isValid(String skey) {
        return true;
    }

    public int login(String in_facebookuser, String in_userName, String in_skey, BalanceSheet inout_gold, BalanceSheet inout_coins) {
        t.trace("enter function =game.login=" + in_facebookuser);

        ResultSet db_res_index = read(in_facebookuser);

        boolean gotSkey = false;
        String db_skey = null;
        boolean userExists = false;
        int ID = -1;
        byte sameDay = 0;

        try {
            if (db_res_index.next()) {
                // TODO strange logic: gotSkey, userExists should be checked after the data was retrieved
                db_skey = db_res_index.getString("skey");
                gotSkey = true;

                ID = db_res_index.getInt("PlayerID");
                sameDay = db_res_index.getByte("SameDay");
                userExists = true;
            }
        } catch (SQLException e) {
        }

        int rc = -1;

        if (gotSkey && (db_skey != null && !db_skey.equals("") && !db_skey.equals(in_skey))) {
            return VALID_SESSION;

        } else if (userExists) {
            // EXISTING USER
            if (sameDay != 1) {
                game.dailyReward(in_facebookuser, in_userName, inout_gold, inout_coins);
                rc = NEW_SESSION;
            }

        } else {
            // NEW USER
            if (in_skey == null || in_skey.equals("null")) {
                in_skey = "";
            }

            // TODO move out of this class
            game.init(in_facebookuser, in_userName, in_skey);

            rc = NEW_PLAYER;
        }

        t.trace("exit function =game.login=" + rc);
        return rc;
    }

    private ResultSet read(String playerId) {
        String sql = String.format(" SELECT PlayerID, skey, (DAY(LastDailyReward) = DAY(Now())) AS SameDay" +
                " FROM Session WHERE FacebookUser='%s'", playerId);

        return ds.query(sql, "read", playerId);
    }
}
