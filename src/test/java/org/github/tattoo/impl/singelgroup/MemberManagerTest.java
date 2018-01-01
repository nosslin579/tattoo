package org.github.tattoo.impl.singelgroup;


import org.github.tattoo.TournamentOptions;
import org.github.tattoo.impl.singelgroup.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MemberManagerTest {

    @Test
    public void testMatchResult() {
        MemberManager memberManager = new MemberManager();
        Match match = new Match();
        match.getTeamMembers().put("a", new TeamMember(new Participant("a"), TeamId.BLUE));
        memberManager.movePplToCorrectTeam(match, (name, teamId) -> Assert.fail());
    }

}
