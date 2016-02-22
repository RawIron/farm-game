package io.rawiron.farmgame.system;

import org.joda.time.LocalDateTime;

public class RewardEvent extends GameEvent {
    public static final String EVENT_NAME = RewardEvent.class.getSimpleName();

    public RewardEvent(LocalDateTime utcTimestamp, String in_facebookuser, int in_farmID,
                       String rewardName, String coins, int coinsReward) {
        super();
    }
}
