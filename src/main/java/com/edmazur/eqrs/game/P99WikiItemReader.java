// $ ./gradlew runP99WikiItemReader --args='src/main/resources/items.txt'

package com.edmazur.eqrs.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class P99WikiItemReader {

  private static final Duration DELAY_BETWEEN_REQUESTS = Duration.ofSeconds(2);
  private static final int ITEMS_PER_REQUEST = 500;

  private static final String API_URL = "https://wiki.project1999.com/api.php?"
      + "format=json&"
      + "action=query&"
      + "generator=categorymembers&"
      + "gcmtitle=Category:Items&"
      + "gcmlimit=" + ITEMS_PER_REQUEST + "&"
      + "prop=info&"
      + "inprop=url";

  public static void main(String[] args) {
    File outputFile = new File(args[0]);
    try {
      PrintWriter printWriter = new PrintWriter(outputFile);
      for (Map.Entry<String, String> mapEntry : getItems().entrySet()) {
        String name = mapEntry.getKey();
        String url = mapEntry.getValue();
        printWriter.println(name + "\t" + url);
      }
      printWriter.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static SortedMap<String, String> getItems() {
    SortedMap<String, String> items = new TreeMap<>();
    String continueKey = null;
    int itemsRead = 0;

    while (true) {
      String url = API_URL + (continueKey == null ? "" : "&gcmcontinue=" + continueKey);
      JSONObject response = getJsonInsecure(url);
      JSONObject pages = response.getJSONObject("query").getJSONObject("pages");
      items.putAll(getItems(pages));
      if (response.has("query-continue")) {
        continueKey = response
            .getJSONObject("query-continue")
            .getJSONObject("categorymembers")
            .getString("gcmcontinue");
      } else {
        break;
      }

      itemsRead += ITEMS_PER_REQUEST;
      System.out.println("Read " + itemsRead + " items...");
      try {
        Thread.sleep(DELAY_BETWEEN_REQUESTS.toMillis());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return items;
  }

  private static Map<String, String> getItems(JSONObject pages) {
    SortedMap<String, String> items = new TreeMap<>();
    for (Iterator<String> i = pages.keySet().iterator(); i.hasNext();) {
      JSONObject page = (JSONObject) pages.get(i.next());
      String name = page.getString("title");
      String url = page.getString("fullurl");
      items.put(name, url);
    }
    return items;
  }

  // Use this custom function instead of Json because the P99 wiki certificate is self-signed and it
  // seems very complicated to work around that in Java.
  private static JSONObject getJsonInsecure(String url) {
    Process process = null;
    try {
      process = Runtime.getRuntime().exec(new String[] {"curl", "--insecure", url});
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new JSONObject(new BufferedReader(new InputStreamReader(
        process.getInputStream())).lines().collect(Collectors.toList()).get(0));
  }

}
