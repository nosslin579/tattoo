package org.github.tattoo.impl.singelgroup;

import com.google.gson.Gson;
import org.github.tattoo.impl.singelgroup.model.Match;
import org.github.tattoo.socket.model.Member;
import org.github.tattoo.impl.singelgroup.model.Participant;
import org.github.tattoo.impl.singelgroup.model.TeamMember;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class MemberManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public static final int WAITING = 1;
    public static final int RED_TEAM = 1;
    private static final int BLUE_TEAM = 2;
    private static final int SPECTATOR = 3;
    private final Gson gson = new Gson();

    //key == id
    private final Map<String, Member> members = new ConcurrentHashMap<>();

    public void onMemberUpdate(Object[] objects) {
        log.debug("Member update:{}", objects[0]);
        final JSONObject chatObject = (JSONObject) objects[0];
        Member member = gson.fromJson(chatObject.toString(), Member.class);
        members.put(member.getId(), member);
    }

    public void onMemberLeave(Object[] objects) {
        final JSONObject chatObject = (JSONObject) objects[0];
        Member member = gson.fromJson(chatObject.toString(), Member.class);
        log.info("Member leaving:{}", member.getName());
        members.remove(member.getId());
    }

    private void removeInactiveMembers() {
        long threshold = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60);
        for (Member member : members.values()) {
            if (member.getLastSeen() < threshold) {
                log.info("Removing inactive member:{}", member.getName());
//                onMemberLeave(member);
            }
        }
    }

    public Optional<Member> getMemberByName(String name) {
        Member[] membersNamed = this.members.values()
                .stream()
                .filter(member -> member.getLocation().equals(Member.IN_HERE))
                .filter(member -> Objects.equals(member.getName(), name))
                .toArray(Member[]::new);
        if (membersNamed.length != 1) {
            log.warn("Could not get member named {} since there are {} with that name", name, membersNamed.length);
            return Optional.empty();
        }
        return Optional.of(membersNamed[0]);
    }

    @Override
    public String toString() {
        String participantsString = members.values()
                .stream()
                .map(Member::getName)
                .collect(Collectors.joining(", "));
        return "Members (" + members.size() + "): " + participantsString;
    }

    public boolean isEverybodyInGroup() {
        return members.values().stream()
                .map(Member::getLocation)
                .allMatch(Member.IN_HERE::equals);
    }

    public Member getLeader() {
        return members.values().stream()
                .filter(Member::isLeader)
                .findAny()
                .get();
    }

    public Collection<Member> getMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public void movePplToCorrectTeam(Match match, BiConsumer<String, Integer> moveMemberToTeam) {
        try {
            Deque<Participant> reserve = new ArrayDeque<>(match.getReserve());
            for (TeamMember teamMember : match.getTeamMembers().values()) {
                String name = teamMember.getParticipant().getName();
                getMemberByName(name) //get from team
                        .map(Optional::of)
                        .orElseGet(() -> {//or get from reserve
                            match.getTeamMembers().remove(name);
                            if (reserve.isEmpty()) {
                                return Optional.empty();
                            }
                            Participant reserveParticipant = reserve.pop();
                            return getMemberByName(reserveParticipant.getName())
                                    .map(member -> {
                                        match.getTeamMembers().put(member.getName(), new TeamMember(reserveParticipant, teamMember.getTeamId()));
                                        return member;
                                    });
                        })
                        .ifPresent(member -> moveMemberToTeam.accept(member.getId(), teamMember.getTeamId())); //move if present
            }
        } catch (Exception e) {
            log.error("Could not move ppl", e);
        }
    }

    public Optional<Member> moveMember(String name, int teamId, BiConsumer<String, Integer> moveMemberToTeam) {
        return getMemberByName(name)
                .map(member -> {
                    moveMemberToTeam.accept(member.getId(), teamId);
                    return member;
                });
    }
}
