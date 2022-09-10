package com.edmazur.eqrs;

import java.time.Duration;
import java.time.LocalDateTime;

public class RateLimiter {

  private final Duration duration;

  private LocalDateTime rateLimitExpiration;

  public RateLimiter(Duration duration) {
    this.duration = duration;
  }

  // Tries to get permission to do whatever action is being rate limited. If
  // this returns false, then the caller is rate-limited and should not proceed.
  public boolean getPermission() {
    if (rateLimitExpiration == null || rateLimitExpiration.isBefore(LocalDateTime.now())) {
      rateLimitExpiration = LocalDateTime.now().plus(duration);
      return true;
    } else {
      return false;
    }
  }

}
