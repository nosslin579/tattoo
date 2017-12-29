package org.github.tattoo.impl.singelgroup;

import org.github.tattoo.TournamentException;
import org.github.tattoo.impl.singelgroup.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class ParticipantManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final List<Participant> participants = Collections.synchronizedList(new ArrayList<>());

    public void signUp(String name) {
        if (name.equals("Some Ball")) {
            throw new TournamentException("Cant join with that name");
        }
        if (participants.stream().map(Participant::getName).anyMatch(name::equals)) {
            throw new TournamentException("Can't sign up twice.");
        }
        if (participants.size() == 8) {
            throw new TournamentException("Tournament full");
        }
        participants.add(new Participant(name));
        log.info("Added participant: {}, {}", name, this);
    }

    public boolean cancelSignUp(String name) {
        return getParticipantByName(name)
                .map(participants::remove)
                .orElseThrow(() -> {
                    log.warn("Can't cancel signup {}", name);
                    return new TournamentException("Not signed up");
                });
    }

    public Optional<Participant> getParticipantByName(String name) {
        return participants.stream()
                .filter(participant -> Objects.equals(participant.getName(), name))
                .findAny();
    }

    @Override
    public String toString() {
        String participantsString = participants.stream().map(Participant::getName).collect(Collectors.joining(", "));
        return "Participants (" + participants.size() + "): " + participantsString;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

}
