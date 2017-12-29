package org.github.tattoo.impl.singelgroup;

import org.github.tattoo.socket.GroupCommand;
import org.github.tattoo.socket.ChatListener;
import org.github.tattoo.socket.model.ChatMessage;
import org.github.tattoo.socket.model.Member;
import org.github.tattoo.impl.singelgroup.model.TeamId;

import java.util.Optional;
import java.util.function.Function;

class SpectatorChatListener implements ChatListener {
    private final Function<String, Optional<Member>> getMemberByName;
    private final GroupCommand command;

    public SpectatorChatListener(Function<String,Optional<Member>> getMemberByName, GroupCommand command) {
        this.getMemberByName = getMemberByName;
        this.command = command;
    }

    @Override
    public void onMessage(ChatMessage message) {
        if (message.getMessage().equals("spec")) {
            getMemberByName.apply(message.getFrom()).ifPresent(member -> command.moveMemberToTeam(member.getId(), TeamId.SPECTATOR));
        }
    }
}
