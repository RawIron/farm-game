package io.rawiron.farmgame.engine;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.system.DataStore;


public class JerryCan {
    private DataStore ds;
    private DataGameSettings dataGameSettings;

    public JerryCan() {
    }

    public void use(String in_facebookuser) {
        this.add(in_facebookuser, -1, 0);
    }

    public int add(String in_facebookuser, int in_jerry, int in_fuel)
    /**
     * ABSTRACT
     * add(Jel)
     *
     * PERFORMANCE_IMPACT
     *	General:low
     *	Frequency:low
     *	Cost:low
     */
    {
        int success = 0;

        if (in_jerry < 0) {
            success = ds.execute("UPDATE Farmers "
                            + " SET LastFuelSave=Now(), JerryCans=JerryCans+" + in_jerry + ", Fuel=" + dataGameSettings.cached_FuelLimit
                            + " WHERE JerryCans > 1 AND FacebookUser='" + in_facebookuser + "'"
                    , "write", in_facebookuser);
        } else if (in_jerry > 0) {
            success = ds.execute("UPDATE Farmers "
                            + " SET JerryCans=JerryCans+" + in_jerry
                            + " WHERE FacebookUser='" + in_facebookuser + "'"
                    , "write", in_facebookuser);
        }

        return success;
    }
}
