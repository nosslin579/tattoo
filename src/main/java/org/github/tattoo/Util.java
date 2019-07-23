package org.github.tattoo;

import java.util.Objects;
import java.util.Random;

public class Util {

  public static void sleepSeconds(long seconds) {
    //ugly hack to speed up testing
    int multiplier = Objects.equals(System.getenv("sleepdisabled"), "true") ? 100 : 1000;
    try {
      Thread.sleep(multiplier * seconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TournamentException("Interrupted", e);
    }
  }

  public static String getRandomString() {
    int leftLimit = 65; // letter 'A'
    int rightLimit = 89; // letter 'Z'
    int targetStringLength = 12;
    Random random = new Random();
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < targetStringLength; i++) {
      int randomLimitedInt = leftLimit + (int)
          (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    return buffer.toString();
  }
}
