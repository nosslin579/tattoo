package org.github.tattoo.impl.singelgroup.model;

import org.github.tattoo.TournamentOptions;
import org.github.tattoo.impl.singelgroup.TournamentState;
import org.github.tattoo.socket.model.Member;

import java.util.Collection;
import java.util.List;

public class TournamentStatus {
    private List<Participant> participants;
    private Collection<Member> members;
    private String poolStatus;
    private TournamentState tournamentState;
    private String group;
    private List<ParticipantResult> participantResult;
    private Collection<Match> matches;
    private TournamentOptions options;
    private String chatListeners;
    private boolean completed;
    private String socketInfo;

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setMembers(Collection<Member> members) {
        this.members = members;
    }

    public Collection<Member> getMembers() {
        return members;
    }

    public void setPoolStatus(String poolStatus) {
        this.poolStatus = poolStatus;
    }

    public String getPoolStatus() {
        return poolStatus;
    }

    public void setTournamentState(TournamentState tournamentState) {
        this.tournamentState = tournamentState;
    }

    public TournamentState getTournamentState() {
        return tournamentState;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setParticipantResult(List<ParticipantResult> participantResult) {
        this.participantResult = participantResult;
    }

    public List<ParticipantResult> getParticipantResult() {
        return participantResult;
    }

    public void setMatches(Collection<Match> matches) {
        this.matches = matches;
    }

    public Collection<Match> getMatches() {
        return matches;
    }

    public void setOptions(TournamentOptions options) {
        this.options = options;
    }

    public TournamentOptions getOptions() {
        return options;
    }

    public void setChatListeners(String chatListeners) {
        this.chatListeners = chatListeners;
    }

    public String getChatListeners() {
        return chatListeners;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setSocketInfo(String socketInfo) {
        this.socketInfo = socketInfo;
    }

    public String getSocketInfo() {
        return socketInfo;
    }
}
