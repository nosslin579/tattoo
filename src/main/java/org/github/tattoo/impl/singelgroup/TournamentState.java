package org.github.tattoo.impl.singelgroup;

public enum TournamentState {
    CREATED,
    SIGN_UP_OPEN,
    SIGN_UP_CLOSED,
    CREATING_MATCH,
    LAUNCHING,
    WAITING_FOR_MATCH_TO_FINISH,
    ASKING_FOR_SCORE,
    WAITING_FOR_SCORE,
    GOT_SCORE,
    CANCELED,
    TIME_OUT,
    DISCONNECTED,
    NOT_ENOUGH_MEMBERS,
    NO_PARTICIPANTS,
    ENDED
}
