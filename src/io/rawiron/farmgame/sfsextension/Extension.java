package io.rawiron.farmgame.sfsextension;


import java.util.LinkedList;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.rawiron.farmgame.engine.*;
import it.gotoandplay.smartfoxserver.extensions.*;
import it.gotoandplay.smartfoxserver.db.*;
import it.gotoandplay.smartfoxserver.data.*;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;

import org.json.*;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataLevel;
import io.rawiron.farmgame.gamesettings.DataProducer;
import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.jdbcDB;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Extension extends AbstractExtension {

    private ExtensionHelper helper;
    private Zone zone;

    private Logging l;
    private Trace t;
    private DataStore ds;

    private Router router;


    public void init()
    /**
     * ABSTRACT
     * Initializion of threads reading from Extension Message Queue:
     * this function is called as soon as the extension
     * is loaded in the SFS.
     *
     * create connection pools
     *
     * CONCURRENT
     * Extension is multi-threaded.
     * Each thread loads the script ???
     *
     */
    {
        helper = ExtensionHelper.instance();
        zone = helper.getZone(this.getOwnerZone());

        // create Java Objects
        Trace t = new Trace();
        Logging l = new Logging();
        DataStore ds = new jdbcDB(t);

        this.t = t;
        this.l = l;
        this.ds = ds;


        if ((t != null) && (t.VERBOSE) && (t.VERBOSE_LEVEL >= 4)) t.trace("enter function: init()");

        // Java Objects read from the DataStore in the Constructor method
        //
        //Sharding ..
        //create the connection pools within the SFS
        //PRE
        //		there must be one db server configured in the SFS config file
        //		this db server has the ExtraDatabases table

        //get a reference to the database manager object
        //connect to the databases configured for this zone
        DbManager dbase = zone.dbManager;
        String db_sql_read_ExtraDatabases = " SELECT DBServerString, DBGroup, User, Password, Purpose FROM ExtraDatabases ";
        ds.addPooledConnection("read" + "dbase", dbase);
        ds.addPooledConnection("write" + "dbase", dbase);
        ds.addPooledConnection("log" + "dbase", dbase);


        DbManager dbmanager = null;
        //foreach database server configured
        ResultSet extraDBsQueryRes = ds.query(db_sql_read_ExtraDatabases, "read", null);
        try {
            while (extraDBsQueryRes.next()) {
                String dbpurpose = extraDBsQueryRes.getString("Purpose");
                int dbgroup = extraDBsQueryRes.getInt("DBGroup");
                String dbserver = extraDBsQueryRes.getString("DBServerString");
                String dbuser = extraDBsQueryRes.getString("User");
                String dbpassword = extraDBsQueryRes.getString("Password");
                if (t.VERBOSE) t.trace("Group:" + dbgroup + "(" + dbpurpose + ")->" + dbserver);

                dbmanager = this.createDbManager(dbpurpose, dbserver, dbgroup, dbuser, dbpassword);
                if (dbpurpose.equals("ReadOnly")) {
                    // create a connection pool within SFS for this database server
                    // readonlyDBase[group] = connectDB( extraDBsQueryRes );
                    ds.addPooledConnection("read" + dbgroup, dbmanager);
                } else if (dbpurpose.equals("ReadWrite")) {
                    // create a connection pool within SFS for this database server
                    // readWriteDBase[group] = connectDB( extraDBsQueryRes );
                    ds.addPooledConnection("write" + dbgroup, dbmanager);

                } else if (dbpurpose.equals("Log")) {
                    // create a connection pool within SFS for this database server
                    // logDBase[group] = connectDB( extraDBsQueryRes );
                    ds.addPooledConnection("log" + dbgroup, dbmanager);
                }
            }
        } catch (SQLException e) {
            t.trace("SQLException: " + e.getMessage());
            t.trace("SQLState: " + e.getSQLState());
            t.trace("VendorError: " + e.getErrorCode());
        }


        // create Java Objects
        //
        Buff b = new Buff(ds, l, t);
        Building bd = new Building(ds, l, t);
        Farm fm = new Farm(ds, l, t);
        Friend fr = new Friend(ds, l, t);
        Decoration d = new Decoration(ds, l, t);
        Valuable v = new Valuable(ds, l, t);

        Farmer f = new Farmer(v, ds, l, t);
        Storage s = new Storage(v, ds, l, t);

        PlotList pl = new PlotList(ds, l, t);
        Unlockable u = new Unlockable(ds);
        Reward rw = new Reward(ds, l, t);
        Achievement av = new Achievement(ds, l, t);
        IAnimalInventory al = new AnimalInventoryRaw(ds, t);
        Animal a = new Animal(al, l, t);
        Job j = new Job(ds, l, t);
        Collection c = new Collection(ds, l, t);
        Gift g = new Gift(ds, l, t);
        Game gm = new Game(ds, l, t);
        Router sm = new Router(ds, l, t);
        Treasure tr = new Treasure(ds, l, t);

        DataUnlockable dsu = new DataUnlockable(ds, t);
        DataGameSettings dsgs = new DataGameSettings(ds);
        DataProducer dsp = new DataProducer(ds);
        DataLevel dsl = new DataLevel(ds, t);


        // Farmer f = new Farmer(dsgs, v, ds, l, t);
        f.setDataGameSettings(dsgs);

        // PlotList pl = new PlotList(av, b, s, ds, l, t);
        pl.setAchievement(av);
        pl.setBuff(b);
        pl.setStorage(s);

        // Unlockable u = new Unlockable(av, c, f, v, ds, l, t);
        u.setAchievement(av);
        u.setCollection(c);
        u.setFarmer(f);
        u.setValuable(v);

        // Reward rw = new Reward(g, s, u, v, ds, l, t);
        rw.setGift(g);
        rw.setStorage(s);
        rw.setUnlockable(u);
        rw.setValuable(v);

        // Achievement av = new Achievement(rw, ds, l, t);
        av.setReward(rw);

        // Animal a = new Animal(av, s, ds, l, t);
        a.setAchievement(av);
        a.setStorage(s);

        // Job j = new Job(rw, s, ds, l, t);
        j.setReward(rw);
        j.setStorage(s);

        // Collection c = new Collection(rw, ds, l, t);
        c.setReward(rw);

        // Decoration d = new Decoration(av, dsu, v, ds, l, t);
        d.setDataUnlockable(dsu);
        d.setAchievement(av);
        d.setValuable(v);

        // Gift g = new Gift(a, b, d, pl, v, ds, l, t);
        g.setAnimal(a);
        g.setBuff(b);
        g.setDecoration(d);
        g.setPlotList(pl);
        g.setValuable(v);

        // Game gm = new Game(f, fm, dsgs, dsu, pl, v, ds, l, t);
        gm.setPlotList(pl);
        gm.setValuable(v);
        gm.setFarmer(f);
        gm.setFarm(fm);
        gm.setDataUnlockable(dsu);
        gm.setDataGameSettings(dsgs);

        // Router sm = new Router(a, av, f, fm, fr, g, gm, j, s, tr, u, v, ds, l, t);
        sm.setAnimal(a);
        sm.setAchievement(av);
        sm.setBuilding(bd);
        sm.setFarmer(f);
        sm.setFriend(fr);
        sm.setGift(g);
        sm.setJob(j);
        sm.setStorage(s);
        sm.setTreasure(tr);
        sm.setUnlockable(u);
        sm.setValuable(v);

        // Treasure tr = new Treasure(f, rw, v, ds, l, t);
        tr.setFarmer(f);
        tr.setReward(rw);
        tr.setValuable(v);

        // Reward rw = new Reward(g, s, u, v, ds, l, t);
        rw.setGift(g);
        rw.setStorage(s);
        rw.setUnlockable(u);
        rw.setValuable(v);


        this.router = sm;

        t.trace("exit function: init()");
    }


    public void handleRequest(String cmd, JSONObject jso, User user, int fromRoom) {
        // Your code here, handles JSON-based requests
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("enter function: handleRequest(JSON Protocol");
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("Got command:" + cmd);

        JSONObject result = null;
        String logCommand = cmd;

        int in_dbgroup = -1;
        String in_facebookuser = null;
        int in_farmID = 0;
        String in_giftID = null;
        int in_useCoins = 0;

        try {
            in_dbgroup = jso.getInt("userDBGroup");
            in_facebookuser = jso.getString("user");
            in_farmID = jso.getInt("farmID");
            in_giftID = jso.getString("giftID");
            in_useCoins = (short) jso.getInt("useCoins");
        } catch (JSONException e) {
        }

        if (l.log && !cmd.equals("checkForDesync")) {
            String str = null;
            char currency = 'G';
            int value = 0;

            if (cmd.equals("placeGift")) {
                str = in_giftID;
            }

            if (in_useCoins == 1) {
                currency = 'K';
            }

            if (str != null) {
                ds.execute("INSERT INTO `log` VALUES ( Now(), '" + logCommand + "','" + in_facebookuser + "','" + in_farmID + "','" + str + "'," + value + ", '" + currency + "',null,1 )"
                        , "log", null);
            }
        }


        result = router.requestRouter(cmd, jso);
        if (result == null) {
            if (t.VERBOSE && (t.VERBOSE_LEVEL >= 0))
                t.trace("assert failure No Command=" + cmd + " found in messageHandler");
            result = new JSONObject();
        }

        try {
            // String msg = gCache[0].gameSettings.getItem('SystemMessage');
            String msg = null;
            if ((msg != null) && (msg.length() > 0)) {
                // result.systemMessage = msg;
                result.put("systemMessage", msg);
            }
            // result.messageIndex = params.messageIndex;
            result.put("systemMessage", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            // result._cmd = cmd;
            result.put("_cmd", cmd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Prepare a list of recipients and put the user that requested the command
        LinkedList<SocketChannel> recipientList = new LinkedList<SocketChannel>();
        recipientList.add(user.getChannel());

//        this.sendResponse(result, -1, null, recipientList);
    }


    public void handleRequest(String cmd, ActionscriptObject asObj, User user, int fromRoom) {
        // ..
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("enter function: handleRequest(ActionscriptObject Protocol");
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("Got command:" + cmd);
    }


    public void handleRequest(String cmd, String[] stringArray, User user, int fromRoom) {
        // ..
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("enter function: handleRequest(String Protocol");
        if (t.VERBOSE && (t.VERBOSE_LEVEL >= 3)) t.trace("Got command:" + cmd);
    }


    public void destroy()
    /**
     * This method is called by the server when an extension
     * is being removed / destroyed.
     *
     * Always make sure to release resources like setInterval(s)
     * open files etc in this method.
     *
     * In this case we delete the reference to the databaseManager
     */
    {
        //Release the reference to the dbase manager
        ds.closeAll();
        trace("Extension destroyed");
    }


    public void handleInternalEvent(InternalEventObject ieo) {
        // ..
    }


    private DbManager createDbManager(String in_purpose, String in_server, int in_dbgroup, String in_user, String in_password)
    /**
     * create a connection pool in the SFS DbManager
     *
     * @param DBGroup:int
     * @param DBServerString:String
     * @param User:String
     * @param Password:String
     *
     * @return DatabaseManager
     */
    {
        DbManager result = null;

        if (in_server != null) {
            String connName = in_purpose + Integer.toString(in_dbgroup);
            String connString = "jdbc:mysql://" + in_server;
            String usrName = in_user;
            String pword = in_password;
            String driverName = "com.mysql.jdbc.Driver";
            int maxActive = 20;
            int maxIdle = -1;
            String exhaustedAction = "fail";
            int blockTime = 500;

            result = new DbManager(driverName,
                    connString,
                    usrName,
                    pword,
                    connName,
                    maxActive,
                    maxIdle,
                    exhaustedAction,
                    blockTime);


            if ((t != null) && t.VERBOSE) t.trace(connName + " created");
        }

        return result;
    }
}
