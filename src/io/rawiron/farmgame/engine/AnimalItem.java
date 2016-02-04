package io.rawiron.farmgame.engine;

import io.rawiron.farmgame.system.UniqueNumber;


public class AnimalItem {
    public AnimalItem() {}

    public AnimalItem(String playerId, String name) {
        this.playerId = playerId;
        this.id = UniqueNumber.create();
        this.name = name;
    }

    public String playerId;
    public Integer id;
    public String name;
    public int lastHarvest = 0; // should be utc_now()
    public int progress = 0;
    public int elapsedTime = 0;
    public int X = -1;
    public int Y = -1;
}
