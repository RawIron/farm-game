package io.rawiron.farmgame.sfsextension;

import io.rawiron.farmgame.session.Session;
import io.rawiron.farmgame.webservices.LoaderGame;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.engine.BalanceSheet;
import io.rawiron.farmgame.engine.Game;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Login {

	private Trace t;
	private Logging l;
	private DataStore ds;

	private Game game;
	private Session session;
	private LoaderGame loaderGame;


	public void setGame(Game in_gm)
	{
		game = in_gm;
	}
	public void setLoaderGame(LoaderGame in_sfsg)
	{
		loaderGame = in_sfsg;
	}


	public JSONObject login(String in_facebookuser, String in_userName, String in_skey)
	{
		JSONObject result = new JSONObject();
		JSONArray fullResult = new JSONArray();


		BalanceSheet balanceGold = new BalanceSheet();
		BalanceSheet balanceCoins = new BalanceSheet();
		int rc = session.login(in_facebookuser, in_userName, in_skey, balanceGold, balanceCoins);

		if (rc == 1)
		// EXISTING User
		{
			// create message content
			//
			// read the updated data
			fullResult = loaderGame.getSession(in_facebookuser, 0);

			// better use a Java object and game.login() returns the dbgroup in the object
			// for now this has to do the trick
			try {
				JSONObject jdb = new JSONObject();
				jdb = fullResult.getJSONObject(0);
				//inout_dbgroup = jdb.getInt("DataBaseGroup");
			} catch (JSONException e) {e.printStackTrace();}

			if (t.VERBOSE && (t.VERBOSE_LEVEL >=3) ) {
				try {
					JSONObject jdb = new JSONObject();
					jdb = fullResult.getJSONObject(0);
					t.trace("variable dump  LastPlayDate="+ jdb.getString("LastPlayDate"));
				} catch (JSONException e) {e.printStackTrace();}
			}


			// tempResult = [];
			// addQueryResponse( tempResult, farmerQuery, 0 );
			// result.fullResult[1] = tempResult[0];
			fullResult = loaderGame.getFarmer(in_facebookuser, 1);


			try {
				// result.fullResult[2] = {};
				JSONObject jso = new JSONObject();
				jso.put("dailyGoldEarned", balanceGold.earned);
				jso.put("basicGoldEarned", balanceGold.daily);
				jso.put("animalGoldEarned", balanceGold.animal);
				jso.put("collectionGoldEarned", balanceGold.collection);
				jso.put("buildingGoldEarned", balanceGold.building);
				jso.put("decorationGoldEarned", balanceGold.decoration);
				jso.put("clothingGoldEarned", balanceGold.clothing);
				jso.put("landGoldEarned", balanceGold.land);
				jso.put("contraptionGoldEarned", balanceGold.contraption);
				jso.put("protectionGoldEarned", balanceGold.protection);
				jso.put("dailyGoldFriends", balanceGold.friend);

				jso.put("dailyCoinsEarned", balanceCoins.earned);
				jso.put("basicCoinsEarned", balanceCoins.daily);
				jso.put("animalCoinsEarned", balanceCoins.animal);
				jso.put("collectionCoinsEarned", balanceCoins.collection);
				jso.put("buildingCoinsEarned", balanceCoins.building);
				jso.put("decorationCoinsEarned", balanceCoins.decoration);
				jso.put("clothingCoinsEarned", balanceCoins.clothing);
				jso.put("landCoinsEarned", balanceCoins.land);
				jso.put("contraptionCoinsEarned", balanceCoins.contraption);
				jso.put("protectionCoinsEarned", balanceCoins.protection);

				fullResult.put(2, jso);

				result.put("fullResult", fullResult);

			} catch (JSONException e) {	e.printStackTrace(); }
		}


		else if (rc == 0)
		// NEW User
		{
			// read the inserted rows
			fullResult = loaderGame.getSession(in_facebookuser, 0);

			try {
				JSONObject jdb = new JSONObject();
				jdb = fullResult.getJSONObject(0);
				//inout_dbgroup = jdb.getInt("DataBaseGroup");
			} catch (JSONException e) {e.printStackTrace();}
			//if (t.VERBOSE && (t.VERBOSE_LEVEL>=3) ) t.trace("variable dump  dbgroup="+ inout_dbgroup);


			// addQueryResponse( tempResult, farmerQuery, 0 );
			// result.fullResult[1] = tempResult[0];
			fullResult = loaderGame.getFarmer(in_facebookuser, 1);

			try {
				// result.fullResult[2] = {};
				// result.fullResult[2].dailyGoldEarned = 0;
				JSONObject jso = new JSONObject();
				jso.put("dailyGoldEarned", 0);
				fullResult.put(2, jso);

				result.put("fullResult", fullResult);
			} catch (JSONException e) { e.printStackTrace(); }
		}


		else if (rc == 2)
		// ERROR skey
		{
			fullResult = loaderGame.getFarmerSkey(in_facebookuser, 0, in_skey);

			try {
				result.put("fullResult", fullResult);
			} catch (JSONException e) { e.printStackTrace(); }
		}

		return result;
	}

}
