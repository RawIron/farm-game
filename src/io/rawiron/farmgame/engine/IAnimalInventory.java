package io.rawiron.farmgame.engine;

import java.sql.ResultSet;


public interface IAnimalInventory {
    boolean add(String playerId, AnimalItem animal);

    boolean sub(String playerId, AnimalItem animal);

    ResultSet retrieve(String playerId);

    AnimalItem getAnimal(String playerId, Integer animalId);

    boolean replace(String playerId, AnimalItem animal);

    int goldValue(String playerId);
}
