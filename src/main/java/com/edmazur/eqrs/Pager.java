package com.edmazur.eqrs;

import com.github.dikhan.pagerduty.client.events.PagerDutyEventsClient;
import com.github.dikhan.pagerduty.client.events.domain.Payload;
import com.github.dikhan.pagerduty.client.events.domain.Severity;
import com.github.dikhan.pagerduty.client.events.domain.TriggerIncident;
import com.github.dikhan.pagerduty.client.events.exceptions.NotifyEventException;

public class Pager {

  private static final Logger LOGGER = new Logger();

  private Config config;

  public Pager(Config config) {
    this.config = config;
  }

  public void page(String message) {
    PagerDutyEventsClient pagerDutyEventsClient =
        PagerDutyEventsClient.create();
    Payload payload = Payload.Builder.newBuilder()
        .setSummary(message)
        .setSource("RobotStanvern")
        .setSeverity(Severity.CRITICAL)
        .build();
    TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
        .newBuilder(
            config.getString(Config.Property.PAGERDUTY_INTEGRATION_KEY),
            payload)
        .build();
    try {
      pagerDutyEventsClient.trigger(incident);
    } catch (NotifyEventException e) {
      LOGGER.log("Error sending page");
      e.printStackTrace();
    }
  }

}