package org.github.tattoo.impl.singelgroup;


import org.github.tattoo.impl.singelgroup.MatchManager;
import org.github.tattoo.impl.singelgroup.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatchManagerTest {

    @Test
    public void testMatchResult() {
        MatchManager matchManager = new MatchManager();

        List<Participant> nineParticipants = IntStream.range(1, 10)
                .boxed()
                .map(playerNumber -> new Participant("p" + playerNumber))
                .collect(Collectors.toList());

        matchManager.create(nineParticipants);
        matchManager.completeMatch(new MatchScore(2, 1));

        matchManager.create(nineParticipants);
        matchManager.completeMatch(new MatchScore(2, 1));

        Assert.assertEquals(6, matchManager.getParticipantResult(nineParticipants.get(0)).getPoints());
        Assert.assertEquals(3, matchManager.getParticipantResult(nineParticipants.get(1)).getPoints());
        Assert.assertEquals(2, matchManager.getParticipantResult(nineParticipants.get(1)).getMatchesPlayed());
        Assert.assertEquals(0, matchManager.getParticipantResult(nineParticipants.get(8)).getMatchesPlayed());
    }

}
