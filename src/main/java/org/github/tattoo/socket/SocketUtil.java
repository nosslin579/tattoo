package org.github.tattoo.socket;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.github.tattoo.TagproServer;
import org.github.tattoo.TournamentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SocketUtil {
    private static final Logger log = LoggerFactory.getLogger(SocketUtil.class);

    public static Socket joinGroupSocket(TagproServer server, String group, String tagProId) {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        final Socket socket = IO.socket(server.getUri(group), opts);
        socket.io().on(Manager.EVENT_TRANSPORT, new SetCookieEmitterListener(tagProId));
        socket.on("connect", objects -> log.info("Connected to {}", group));
        socket.on("disconnect", objects -> log.info("Disconnect from {}", group));
        socket.on("setting", objects -> log.debug("Setting:{}", objects[0]));
        socket.on("full", objects -> log.info("Full:{}", Arrays.toString(objects)));
        socket.connect();

        return socket;
    }

    public static void joinJoinerSocket(TagproServer server, String tagProId) {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        final Socket socket = IO.socket(server.getUri("/games/find"), opts);
        socket.io().on(Manager.EVENT_TRANSPORT, new SetCookieEmitterListener(tagProId));
        socket.on("connect", objects -> log.info("Connected to joiner"));
        socket.on("disconnect", objects -> log.info("Disconnect from joiner"));
        socket.on("FoundWorld", objects -> log.info("FoundWorld:{}", Arrays.toString(objects)));
        socket.on("WaitingForMembers", objects -> log.info("WaitingForMembers:{}", Arrays.toString(objects)));
        socket.on("CreatingWorld", objects -> log.info("CreatingWorld:{}", Arrays.toString(objects)));
        socket.on("GroupLeaderNotInTheJoiner", objects -> log.error("GroupLeaderNotInTheJoiner:{}", Arrays.toString(objects)));
        socket.on("port", objects -> log.info("port:{}", Arrays.toString(objects)));
        socket.on("TrollControl", objects -> log.warn("TrollControl:{}", Arrays.toString(objects)));
        socket.on("WaitForEligibility", objects -> log.info("WaitForEligibility:{}", Arrays.toString(objects)));
        socket.connect();
    }

    public static String createGroup(TagproServer server, Consumer<String> tagProIdConsumer, String name) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost request = new HttpPost(server.getUrl() + "/groups/create");
            List<BasicNameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("name", name));
//            parameters.add(new BasicNameValuePair("public", "on"));
            request.setEntity(new UrlEncodedFormEntity(parameters));
            HttpResponse response = httpclient.execute(request);
            if (response.getStatusLine().getStatusCode() < 300) {
                log.error("Response: {}", response);
            }
            Header[] locations = response.getHeaders("Location");
            log.info("Group created {}", locations[0].getValue());

            final Header setCookieHeaders = response.getFirstHeader("Set-Cookie");
            final List<HeaderElement> headerElements = Arrays.asList(setCookieHeaders.getElements());
            for (HeaderElement headerElement : headerElements) {
                if (headerElement.getName().equals("tagpro")) {
                    tagProIdConsumer.accept(headerElement.getValue());
                }
            }
            return locations[0].getValue();
        } catch (IOException e) {
            log.error("IO error", e);
            throw new TournamentException("Create group failed", e);
        }
    }

    private String getTagProId(TagproServer server) {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet request = new HttpGet(server.getUrl());
            HttpResponse response = httpclient.execute(request);
            final Header setCookieHeaders = response.getFirstHeader("Set-Cookie");

            final List<HeaderElement> headerElements = Arrays.asList(setCookieHeaders.getElements());
            for (HeaderElement headerElement : headerElements) {
                if (headerElement.getName().equals("tagpro")) {
                }
                return headerElement.getValue();
            }
        } catch (IOException e) {
            log.error("IO error", e);
        }
        throw new TournamentException("No session could be found");
    }

    private void fingerPrint(String serverUri) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost request = new HttpPost(serverUri + "/fingerprint");
        List<BasicNameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("id", "on"));
        request.setEntity(new UrlEncodedFormEntity(parameters));
        HttpResponse response = httpclient.execute(request);
        log.info("Finger print {}", response);
    }

    public static void leaveGroup(TagproServer server, String tagProId) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(new URI(server.getUrl() + "/groups/leave"));
            HttpResponse response = httpClient.execute(request);
            log.info("Leaving {}", response);
        } catch (Exception e) {
            throw new TournamentException(e);
        }
    }

}
