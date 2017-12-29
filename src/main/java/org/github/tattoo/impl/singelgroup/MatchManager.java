package org.github.tattoo.impl.singelgroup;

import org.github.tattoo.TournamentOptions;
import org.github.tattoo.impl.singelgroup.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class MatchManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Deque<Match> matches = new ConcurrentLinkedDeque<>();

    public Match create(List<Participant> participants) {
        Match ret = new Match();

        List<Participant> participantsOrdered = participants.stream()
                .sorted((o1, o2) -> Integer.compare(getParticipantResult(o2).getPoints(), getParticipantResult(o1).getPoints()))
                .limit(8)
                .collect(Collectors.toList());

        int teamId = TeamId.RED;
        for (Participant p : participantsOrdered) {
            ret.getTeamMembers().put(p.getName(), new TeamMember(p, teamId));
            teamId = toggleTeam(teamId);
        }

        List<Participant> reserve = new ArrayList<>(participants);
        reserve.removeAll(participantsOrdered);
        ret.setReservePlayers(reserve);

        matches.add(ret);
        return ret;
    }

    private int toggleTeam(int teamId) {
        return teamId == TeamId.RED ? TeamId.BLUE : TeamId.RED;
    }

    private Match getCurrentMatch() {
        return matches.getLast();
    }

    public void completeMatch(MatchScore matchScore) {
        Match match = getCurrentMatch();
        match.setScore(matchScore);
        match.setFinished(true);
    }

    public boolean hasMoreMatches(TournamentOptions options) {
        return matches.size() < options.getNumberOfMatches();
    }

    public Collection<Match> getMatches() {
        return Collections.unmodifiableCollection(matches);
    }

    public List<ParticipantResult> getParticipantResults() {
        return matches.stream()
                .filter(Match::isFinished)
                .flatMap(match -> match.getTeamMembers().values().stream())
                .map(TeamMember::getParticipant)
                .distinct()
                .map(this::getParticipantResult)
                .sorted((pr1, pr2) -> Integer.compare(pr1.getPoints(), pr2.getPoints()))
                .collect(Collectors.toList());
    }

    public ParticipantResult getParticipantResult(Participant participant) {
        return matches.stream()
                .filter(Match::isFinished)
                .collect(() -> new ParticipantResult(participant),
                        (participantResult, match) -> {
                            TeamMember teamMember = match.getTeamMembers().get(participant.getName());
                            if (teamMember != null) {//null teamMember was not playing this match
                                int pointsForMatch = getPointsForMatch(match, teamMember.getTeamId());
                                participantResult.increasePoints(pointsForMatch);
                                participantResult.increaseMatchesPlayed(1);
                            }
                        },
                        (participantResult1, participantResult2) -> {
                            throw new UnsupportedOperationException("Not needed");
                        });
    }

    private int getPointsForMatch(Match match, int teamId) {
        if (match.getScore().getBlueScore() == match.getScore().getRedScore()) {
            return 1;
        }
        if (match.getScore().getRedScore() > match.getScore().getBlueScore()) {
            return teamId == TeamId.RED ? 3 : 0;
        } else {
            return teamId == TeamId.BLUE ? 0 : 3;
        }
    }

    public void chatResult(Consumer<String> chat, boolean endResult) {
        try {
            List<ParticipantResult> participantResults = getParticipantResults();
            if (participantResults.isEmpty()) {
                chat.accept("No results :(");
            } else if (endResult) {
                chat.accept("Balls and ballettes, we have a winner:");
                chat.accept("###########################");
                chat.accept("_ _ _ " + participantResults.iterator().next().getParticipant().getName() + " _ _ _");
                chat.accept("###########################");
            }
            final int[] i = {1};
            participantResults.stream()
                    .limit(4)
                    .forEach(pr -> chat.accept("#" + i[0]++ + " place: " + pr.getParticipant().getName()));
        } catch (Exception e) {
            log.error("Failed to chat result", e);
        }
    }
}
