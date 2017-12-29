package org.github.tattoo.impl.singelgroup;

import org.github.tattoo.socket.GroupCommand;
import org.github.tattoo.impl.singelgroup.ParticipantManager;
import org.github.tattoo.socket.ChatListener;
import org.github.tattoo.socket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUpChatListener implements ChatListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ParticipantManager participantManager;
    private final GroupCommand groupCommand;

    public SignUpChatListener(ParticipantManager participantManager, GroupCommand groupCommand) {
        this.participantManager = participantManager;
        this.groupCommand = groupCommand;
    }

    @Override
    public void onMessage(ChatMessage message) {
        try {
            if ("join".equals(message.getMessage())) {
                participantManager.signUp(message.getFrom());
                groupCommand.chat("@" + message.getFrom() + ": You are now signed up!");
            } else if ("leave".equals(message.getMessage())) {
                participantManager.cancelSignUp(message.getFrom());
                groupCommand.chat("@" + message.getFrom() + ": Your sign up has been canceled!");
            } else if ("oi".equals(message.getMessage())) {
                String signedUp = participantManager.getParticipantByName(message.getFrom()).map(participant -> "").orElse("not ");
                groupCommand.chat("Hi " + message.getFrom() + ", you are " + signedUp + "signed up. For more info visit http://smigo.se:8080");
            }
        } catch (Exception e) {
            log.error("Sign up failed, {}", message.getFrom(), e);
            groupCommand.chat(e.getMessage());
        }
    }
}
