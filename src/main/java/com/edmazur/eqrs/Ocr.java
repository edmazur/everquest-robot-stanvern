package com.edmazur.eqrs;

import com.edmazur.eqrs.game.CharInfoOcrScrapeComparator;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Switch from using system-installed utilities to library code versions that are explicitly
// packaged with this codebase.
public class Ocr {

  // Set this to true to facilitate one-off debugging.
  private static final boolean DEBUG = false;

  private static final List<OcrStrategy> OCR_STRATEGIES = List.of(
      new OcrStrategy()
          .disableAlpha()
          .enableNegate(),
      new OcrStrategy()
          .disableAlpha()
          .enableNegate()
          .enableResize(200)
          .enableBlur(new BigDecimal("1"), new BigDecimal("0.5"))
      );

  public List<String> scrape(File image, Comparator<List<String>> scrapeComparitor) {
    List<List<String>> scrapes = new ArrayList<>();
    for (OcrStrategy ocrStrategy : OCR_STRATEGIES) {
      scrapes.add(scrape(image, ocrStrategy));
    }
    Collections.sort(scrapes, scrapeComparitor);
    List<String> scrape = scrapes.isEmpty() ? List.of() : scrapes.get(scrapes.size() - 1);
    if (DEBUG) {
      System.out.println("Winner:");
      System.out.println(scrape);
    }
    return scrape;
  }

  private List<String> scrape(File image, OcrStrategy ocrStrategy) {
    try {
      // Preprocess image.
      File preprocessedImage = File.createTempFile(this.getClass().getName() + "-", ".png");
      Runtime.getRuntime().exec(ocrStrategy.getCommand(image, preprocessedImage));

      // Run Tesseract.
      Process process = Runtime.getRuntime().exec(
          new String[] {
              "tesseract", "-l", "eng", "--psm", "6", preprocessedImage.getAbsolutePath(), "-"});
      List<String> lines = new BufferedReader(new InputStreamReader(
          process.getInputStream())).lines().collect(Collectors.toList());
      if (DEBUG) {
        System.out.println(String.join(" ", ocrStrategy.getCommand(image, preprocessedImage)));
        System.out.println(CharInfoOcrScrapeComparator.getDebugInfo(lines));
        for (int i = 0; i < lines.size(); i++) {
          System.out.println((i + 1) + ": " + lines.get(i) + "$");
        }
        System.out.println();
      }
      return lines;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
