package io.rawiron.farmgame.engine;

import java.sql.ResultSet;
import java.util.List;


public class AnimalInventoryLite implements IAnimalInventory {

    public boolean add(String playerId, AnimalItem animal) {
        return save(animal);
    }

    public boolean sub(String playerId, AnimalItem animal) {
        AnimalItemLite a = AnimalItemLite.findFirst("player_id = ? and animal_id = ?", playerId, animal.id);
        a.delete();
        return true;
    }

    public ResultSet retrieve(String playerId) {
        List<AnimalItemLite> al = AnimalItemLite.find("player_id = ?", playerId);
        return null;
    }

    public AnimalItem getAnimal(String playerId, Integer animalId) {
        AnimalItemLite a = AnimalItemLite.findFirst("player_id = ? and animal_id = ?", playerId, animalId);
        return null;
    }

    public boolean replace(String playerId, AnimalItem animal) {
        return save(animal);
    }

    public int  goldValue(String playerId) { return 0; }

    private boolean save(AnimalItem animal) {
        AnimalItemLite a = new AnimalItemLite();
        a.set("player_id", animal.playerId)
                .set("animal_id", animal.id)
                .set("name", animal.name)
                .set("last_harvest", animal.lastHarvest)
                .set("x", animal.X)
                .set("y", animal.Y);
        a.saveIt();
        return true;
    }
}
