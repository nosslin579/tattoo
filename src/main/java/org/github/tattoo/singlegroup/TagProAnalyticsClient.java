package org.github.tattoo.singlegroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagProAnalyticsClient {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Pattern matchIdRegEx = Pattern.compile("#\\d{7,8}");
  private final Gson gson = new Gson();


  public Optional<Match> getMatchByTeamName(String teamName) {
    return searchMatch(teamName)
        .flatMap(this::getMatchById);
  }

  private Optional<String> searchMatch(final String teamName) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      HttpGet request = new HttpGet(new URI("https", "tagpro.eu", null, "search=team&name=" + teamName, null));
      CloseableHttpResponse response = httpClient.execute(request);
      log.info("Response from search {}", response);
      if (response.getStatusLine().getStatusCode() != 200) {
        log.error("Non 200 response, teamName:{}", teamName);
        return Optional.empty();
      }
      HttpEntity entity = response.getEntity();
      //https://stackoverflow.com/questions/237061/using-regular-expressions-to-extract-a-value-in-java
      return new BufferedReader(new InputStreamReader(entity.getContent()))
          .lines()
          .map(s -> {
            Matcher matcher = matchIdRegEx.matcher(s);
            if (matcher.find()) {
              return matcher.group();
            }
            return null;
          })
          .filter(Objects::nonNull)
          .map(s -> s.substring(1))
          .findAny();
    } catch (Exception e) {
      log.error("Failed to search by teamName:" + teamName, e);
      return Optional.empty();
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        log.error("Failed to close http client", e);
      }

    }
  }

  private Optional<Match> getMatchById(String matchId) {
    log.info("Get match {}", matchId);
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      URI uri = new URI("https", "tagpro.eu", "/data", "bulk=matches&first=" + matchId + "&last=" + matchId, null);
      HttpGet request = new HttpGet(uri);
      CloseableHttpResponse response = httpClient.execute(request);
      log.info("Response from /data {}", response);
      HttpEntity entity = response.getEntity();
      if (response.getStatusLine().getStatusCode() != 200) {
        log.error("Non 200 response, matchId:{}", matchId);
        return Optional.empty();
      }
      InputStreamReader reader = new InputStreamReader(entity.getContent(), "UTF-8");
      //https://stackoverflow.com/questions/2779251/how-can-i-convert-json-to-a-hashmap-using-gson
      Type type = new TypeToken<Map<String, Match>>() {
      }.getType();
      Map<String, Match> map = gson.fromJson(reader, type);
      return Optional.ofNullable(map.get(matchId));
    } catch (Exception e) {
      log.error("Failed to get match by id:" + matchId, e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        log.error("Failed to close http client", e);
      }

    }
    return Optional.empty();
  }

  public static class Match {
    private String server;
    private String port;// 9000,
    private String official;// true,
    private String group;// "",
    private long date;// 1564130182,
    private String timeLimit;// 6,
    private int duration;// 21602,
    private boolean finished;// true,
    private int mapId;// 12703,
    private Player[] players;
    private Team[] teams;

    public String getServer() {
      return server;
    }

    public void setServer(String server) {
      this.server = server;
    }

    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public String getOfficial() {
      return official;
    }

    public void setOfficial(String official) {
      this.official = official;
    }

    public String getGroup() {
      return group;
    }

    public void setGroup(String group) {
      this.group = group;
    }

    public long getDate() {
      return date;
    }

    public void setDate(long date) {
      this.date = date;
    }

    public String getTimeLimit() {
      return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
      this.timeLimit = timeLimit;
    }

    public int getDuration() {
      return duration;
    }

    public void setDuration(int duration) {
      this.duration = duration;
    }

    public boolean isFinished() {
      return finished;
    }

    public void setFinished(boolean finished) {
      this.finished = finished;
    }

    public int getMapId() {
      return mapId;
    }

    public void setMapId(int mapId) {
      this.mapId = mapId;
    }

    public Player[] getPlayers() {
      return players;
    }

    public void setPlayers(Player[] players) {
      this.players = players;
    }

    public Team[] getTeams() {
      return teams;
    }

    public void setTeams(Team[] teams) {
      this.teams = teams;
    }
  }

  private static class Player {
    private boolean auth;
    private String name;
    private int flair;
    private int degree;
    private int score;
    private int points;
    private int team;

    public boolean isAuth() {
      return auth;
    }

    public void setAuth(boolean auth) {
      this.auth = auth;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getFlair() {
      return flair;
    }

    public void setFlair(int flair) {
      this.flair = flair;
    }

    public int getDegree() {
      return degree;
    }

    public void setDegree(int degree) {
      this.degree = degree;
    }

    public int getScore() {
      return score;
    }

    public void setScore(int score) {
      this.score = score;
    }

    public int getPoints() {
      return points;
    }

    public void setPoints(int points) {
      this.points = points;
    }

    public int getTeam() {
      return team;
    }

    public void setTeam(int team) {
      this.team = team;
    }
  }

  public static class Team {
    private String name;
    private int score;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getScore() {
      return score;
    }

    public void setScore(int score) {
      this.score = score;
    }
  }

}
