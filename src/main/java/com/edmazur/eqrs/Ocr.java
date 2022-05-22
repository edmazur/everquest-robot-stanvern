package com.edmazur.eqrs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Switch from using system-installed utilities to library code versions that are explicitly
// packaged with this codebase.
public class Ocr {

  public List<String> scrape(File image) {
    try {
      // Invert image.
      File invertedImage = File.createTempFile(this.getClass().getName() + "-", ".png");
      Runtime.getRuntime().exec(new String[] {
          "convert",
          image.getAbsolutePath(),
          "-channel",
          "RGB",
          "-negate",
          invertedImage.getAbsolutePath()});

      // Run Tesseract.
      Process process = Runtime.getRuntime().exec(
          new String[] {"tesseract", invertedImage.getAbsolutePath(), "-"});
      return new BufferedReader(new InputStreamReader(
          process.getInputStream())).lines().collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      return List.of();
    }
  }

}
