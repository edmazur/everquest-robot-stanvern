package com.edmazur.eqrs;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import org.json.JSONObject;

public class Json {

  public Optional<JSONObject> read(String url) {
    Scanner scanner = null;
    try {
      scanner = new Scanner(new URL(url).openStream(), "UTF-8");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return Optional.empty();
    }
    JSONObject jsonObject = new JSONObject(scanner.useDelimiter("\\A").next());
    scanner.close();
    return Optional.of(jsonObject);

  }

}
