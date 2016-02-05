package io.rawiron.farmgame.engine;

import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Trace;


public class Transaction {
    private Trace t;
    private Valuable valuable;
    private DataUnlockable dataUnlockable;


    public boolean enter(String in_facebookUser, String in_costItem, char in_currency) {
        return valuable.test(in_facebookUser, in_costItem, in_currency);
    }

    public boolean leave(String in_facebookUser, String in_costItem, char in_currency, int in_xpEarned) {
        int earned_amount_coins = 0;
        int cost_amount_coins = 0;
        int cost_amount_gold = 0;
        int cost_amount_fuel = 0;
        // read the cost associated with this task
        boolean gotCost = false;
        earned_amount_coins = dataUnlockable.cached.get(in_costItem).coinsEarned;
        cost_amount_coins = dataUnlockable.cached.get(in_costItem).coinsCost;
        cost_amount_gold = dataUnlockable.cached.get(in_costItem).goldCost;
        cost_amount_fuel = dataUnlockable.cached.get(in_costItem).fuelCost;
        //if (earned_amount_coins == 0) {
        gotCost = true;
        //}

        if (gotCost) {
            // pay the bill
            if (((in_currency != 'K') && cost_amount_gold == 0 && cost_amount_coins != 0) || (in_currency == 'K')) {
                valuable.add(in_facebookUser, -cost_amount_coins, 0, 0, 0, -cost_amount_fuel, 0);
            } else if (in_currency == 'G') {
                valuable.add(in_facebookUser, earned_amount_coins, -cost_amount_gold, 0, 0, -cost_amount_fuel, 0);
            }
        } else {
            if (Trace.VERBOSE && (Trace.VERBOSE_LEVEL >= 0))
                t.trace("assert failure precondition met but costItem missing");
        }


        valuable.levelUp(in_xpEarned, in_facebookUser);

        return true;
    }

}
