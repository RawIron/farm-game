package io.rawiron.farmgame.engine;


public class AnimalItem {
    public AnimalItem() {}

    public AnimalItem(String playerId, Integer id, String name, int lastHarvest, int x, int y) {
        this.playerId = playerId;
        this.id = id;
        this.name = name;
        this.lastHarvest = lastHarvest;
        X = x;
        Y = y;
    }

    public String playerId;
    public Integer id;
    public String name;
    public int lastHarvest = 0;
    public int progress = 0;
    public int elapsedTime = 0;
    public int X = -1;
    public int Y = -1;
}
