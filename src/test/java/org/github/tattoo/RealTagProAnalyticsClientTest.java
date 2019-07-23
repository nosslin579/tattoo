package org.github.tattoo;

import org.github.tattoo.singlegroup.TagProAnalyticsClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RealTagProAnalyticsClientTest {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

  @Test
  @Ignore("Only for manual testing")
  public void createGroupAndStartGame() throws InterruptedException {
    TagProAnalyticsClient client = new TagProAnalyticsClient();
    Optional<TagProAnalyticsClient.Match> match = client.getMatchByTeamName("MPELCNFIVMJJ");
    Assert.assertTrue(match.isPresent());
    String name = match.get().getTeams()[0].getName();
    Assert.assertEquals("MPELCNFIVMJJ", name);
  }
}
