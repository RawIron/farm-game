package io.rawiron.farmgame.engine;

import io.rawiron.farmgame.gamesettings.DataGameSettings;
import io.rawiron.farmgame.gamesettings.DataProducer;
import io.rawiron.farmgame.gamesettings.DataUnlockable;
import io.rawiron.farmgame.system.Logging;
import io.rawiron.farmgame.system.Trace;


public class Animal {
    private Trace t;
    private Logging l;
    private AnimalInventory i;

    private Achievement achievement;
    private Storage storage;
    private Valuable valuable;
    private Farmer farmer;

    private DataGameSettings dataGameSettings;
    private DataProducer dataProducer;
    private DataUnlockable dataUnlockable;


    public Animal(AnimalInventory inventory, Logging in_l, Trace in_t) {
        t = in_t;
        l = in_l;
        i = inventory;
    }

    public void setAchievement(Achievement in_av) {
        achievement = in_av;
    }

    public void setStorage(Storage in_s) {
        storage = in_s;
    }

    public void setFarmer(Farmer in_f) {
        farmer = in_f;
    }

    public void setDataGameSettings(DataGameSettings in_dsgs) {
        dataGameSettings = in_dsgs;
    }

    public void setDataProducer(DataProducer in_dp) {
        dataProducer = in_dp;
    }

    public void setDataUnlockable(DataUnlockable in_du) {
        dataUnlockable = in_du;
    }


    public Boolean harvest(String in_facebookuser, int in_farmID, int in_animalID, String in_animal) {
        if (t.verbose && (t.verbose_level >= 4)) t.trace("enter function =animal_handleHarvest=");

        int user_timeMultiplier = 1;
        user_timeMultiplier = farmer.getTimeMultiplier(in_facebookuser);

        AnimalItem animal = i.getAnimal(in_facebookuser, in_animalID);

        float growthPercentage = 0;
        int elapsedTime = 0;
        int db_productionHours_units_sec = 1;
        String crop = null;
        int yield = 0;

        crop = dataProducer.cached.get(animal.name).produce;
        db_productionHours_units_sec = dataProducer.cached.get(animal.name).productionHours;
        yield = dataProducer.cached.get(animal.name).dryYield;
        growthPercentage = ((elapsedTime * user_timeMultiplier * 100) / db_productionHours_units_sec);

        if ((growthPercentage > 99)) {
            storage.add(in_facebookuser, in_farmID, crop, yield);
            achievement.add(in_facebookuser, in_farmID, crop, yield);
            animal = new AnimalItem(in_facebookuser, in_animal);
            i.add(in_facebookuser, animal);

            l.log("Harvest, " + in_facebookuser + ", " + animal + ", " + yield);
        }

        if (t.verbose && (t.verbose_level >= 4)) t.trace("exit function =animal_handleHarvest=");
        return true;
    }


    public boolean buy(String in_facebookuser, int in_farmID, int in_X, int in_Y, String in_itemName) {
        achievement.add(in_facebookuser, in_farmID, in_itemName, 1);
        AnimalItem animal = new AnimalItem(in_facebookuser, in_itemName);
        return i.add(in_facebookuser, animal);
    }

    public int sell(String in_facebookuser, int in_farmID, int in_animalID) {

        AnimalItem animal = i.getAnimal(in_facebookuser, in_animalID);

        int earn_amount_gold = 0;
        int cost_amount_coins = 0;
        earn_amount_gold = dataUnlockable.cached.get(animal.name).goldCost;
        cost_amount_coins = dataUnlockable.cached.get(animal.name).coinsCost;

        if (earn_amount_gold == 0) {
            earn_amount_gold = (int) (cost_amount_coins * dataGameSettings.cached_CoinsGoldSaleMultiplier);
        }
        earn_amount_gold = (int) (earn_amount_gold * dataGameSettings.cached_AnimalSaleRatio);

        valuable.add(in_facebookuser, in_farmID, 0, earn_amount_gold, 0, 0, 0);
        i.sub(in_facebookuser, animal, 1);

        return 1;
    }

    public boolean move(String in_facebookuser, int in_animalID, int in_X, int in_Y) {
        AnimalItem animal = i.getAnimal(in_facebookuser, in_animalID);
        animal.X = in_X;
        animal.Y = in_Y;
        i.replace(in_facebookuser, animal);
        return true;
    }

}
