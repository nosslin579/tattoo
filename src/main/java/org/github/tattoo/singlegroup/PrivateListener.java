package org.github.tattoo.singlegroup;

import com.google.gson.Gson;
import io.socket.emitter.Emitter;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.model.Private;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateListener implements Emitter.Listener {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Gson gson = new Gson();
    private final GroupCommand groupCommand;

    public PrivateListener(GroupCommand groupCommand) {
        this.groupCommand = groupCommand;
    }

    @Override
    public void call(Object... objects) {
        final JSONObject privateObject = (JSONObject) objects[0];
        log.info("Private:{}", objects[0]);
        Private privateSetting = gson.fromJson(privateObject.toString(), Private.class);
        if (!privateSetting.isPrivate()) {
            groupCommand.switchToPug();
            groupCommand.setDiscoverable(true);
        }
        if (privateSetting.isSelfAssignment()) {
            groupCommand.setSettingSelfAssignment(false);
        }

    }
}
