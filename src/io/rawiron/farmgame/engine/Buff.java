package io.rawiron.farmgame.engine;

import java.sql.ResultSet;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Buff {

	private Trace t;
	private Logging l;
	private DataStore ds;

	public Buff(DataStore in_ds, Logging in_l, Trace in_t) {
		ds = in_ds;
		l = in_l;
		t = in_t;
	}


public int sub( String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_type, int in_amount )
/**
* ABSTRACT
* sub(Bfl) := add(-Bfl)
*
*/
{
	if (t.verbose && (t.verbose_level>=0) && ((1* in_amount)<0) ) t.trace("assert failure in_amount=" + in_amount + " is <0");

	int success = this.add( in_facebookuser, in_farmID, in_X, in_Y, in_type, -in_amount );
	return success;
}



public int add( String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_type, int in_amount )
/**
* ABSTRACT
* add(Bfl)
*
* PERFORMANCE_IMPACT
*	General:high
*	Frequency:stress
*	Cost:low
*/
{
	int success = 0;

	if (in_amount>0) {
		success = ds.execute("INSERT INTO ActiveBuffs ( FarmID, X, Y, Buff, Start ) "
					+ " VALUES ("+ in_farmID +", "+ in_X +", "+ in_Y +", '"+in_type+"', Now() ) "
					+ " ON DUPLICATE KEY UPDATE Buff="+ "'"+in_type+"'" +", Start=Now() "
					, "write", in_facebookuser );
	}
	else if (in_amount<0) {
		success = ds.execute(" DELETE FROM ActiveBuffs WHERE FarmID="+ in_farmID +" AND X="+ in_X +" AND Y="+ in_Y
				, "write", in_facebookuser );
	}

	return success;
}


public ResultSet retrieve(String in_facebookuser, int in_farmID)
{
	// ACTIVEBUFFS
	String db_sql_read_ActiveBuffs =
				" SELECT Buff, Start, FarmID, X, Y, ItemClass, IconClass "
				+ " FROM ActiveBuffs INNER JOIN Buffs ON Title=Buff INNER JOIN Unlockables ON Title=Name "
				+ " WHERE FarmID="+ in_farmID;

	ResultSet buffQueryRes = ds.query( db_sql_read_ActiveBuffs, "read", in_facebookuser );

	return buffQueryRes;
}


}
