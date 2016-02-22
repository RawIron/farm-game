package io.rawiron.farmgame.system;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public class UtcDateTime {

    public static LocalDateTime now() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        return LocalDateTime.now();
    }
}
