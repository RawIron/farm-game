package io.rawiron.farmgame.engine;

import io.rawiron.farmgame.system.DataStore;
import io.rawiron.farmgame.system.jdbcDB;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


class FarmMain {

	FarmMain() {
	}

	public static void main(String[] args) {
		Trace t = new Trace();
		Logging l = new Logging();
		DataStore ds = new jdbcDB(t);

		Buff b = new Buff(ds, l, t);
		Decoration d = new Decoration(ds, l, t);
		Valuable v = new Valuable(ds, l, t);
		Storage s = new Storage(v, ds, l, t);
		Reward rw = new Reward(ds, l, t);
		Job j = new Job(ds, l, t);
		Collection c = new Collection(ds, l, t);
		Achievement av = new Achievement(ds, l, t);
		AnimalInventory al = new AnimalInventory(ds, t);
		Animal a = new Animal(al, l, t);
	}

}
