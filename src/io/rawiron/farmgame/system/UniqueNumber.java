package io.rawiron.farmgame.system;

import java.util.concurrent.ThreadLocalRandom;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class UniqueNumber {

    public static int create() {
        return ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE);
    }
}
