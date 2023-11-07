package com.edmazur.eqrs.game;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ItemScreenshotter {

  private static final int LOG_SPAM_DELINEATOR_SIDE_SIZE = 40;
  private static final String LOG_SPAM_DELINEATOR_SIDE =
      String.join("", Collections.nCopies(LOG_SPAM_DELINEATOR_SIDE_SIZE, "#"));
  private static final String LOG_SPAM_DELINEATOR_FORMAT =
      LOG_SPAM_DELINEATOR_SIDE + " (ItemScreenshotter log spam - %s) " + LOG_SPAM_DELINEATOR_SIDE;

  private ItemScreenshotter() {
    throw new IllegalStateException("Cannot be instantiated");
  }

  public static Optional<File> get(Item item) {
    try {
      // TODO: Eliminate all of the log spam from here and then remove the delineators.
      System.out.println(String.format(LOG_SPAM_DELINEATOR_FORMAT, "START"));
      WebDriverManager.chromedriver().setup();
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--headless");
      options.addArguments("--window-size=1980,960");
      // The P99 wiki certificate periodically expires and takes a day+ to get fixed. Since we're
      // not doing anything sensitive, accept insecure certificates to prevent item screenshotting
      // from breaking.
      options.setAcceptInsecureCerts(true);
      System.setProperty("webdriver.chrome.silentOutput", "true");
      ChromeDriver driver = new ChromeDriver(options);
      System.out.println(String.format(LOG_SPAM_DELINEATOR_FORMAT, "END"));

      driver.get(item.getUrl());

      // This is super hacky: Put the 3 elements that make up the item plate into a single element
      // to facilitate the screenshot step below.
      String unique = UUID.randomUUID().toString();
      JavascriptExecutor js = driver;
      js.executeScript("var wrapperDiv = document.createElement('div');"
          + "wrapperDiv.setAttribute('id', '" + unique + "');"
          + "wrapperDiv.style.width = 'fit-content';"
          + "var itemTopDiv = document.getElementsByClassName('itemtopbg')[0];"
          + "var itemMiddleDiv = document.getElementsByClassName('itembg')[0];"
          + "var itemBottomDiv = document.getElementsByClassName('itembotbg')[0];"
          + "itemTopDiv.parentNode.insertBefore(wrapperDiv, itemTopDiv);"
          + "wrapperDiv.appendChild(itemTopDiv);"
          + "wrapperDiv.appendChild(itemMiddleDiv);"
          + "wrapperDiv.appendChild(itemBottomDiv);");
      WebElement itemElement = driver.findElement(By.id(unique));
      File screenshot = ((TakesScreenshot) itemElement).getScreenshotAs(OutputType.FILE);
      driver.quit();
      return Optional.of(screenshot);
    } catch (Exception e) {
      // This is a bit of a sledgehammer. https://github.com/SeleniumHQ/selenium/issues/11750 broke
      // your WebDriver integration, specifically with ConnectionFailedException. I could check for
      // just that here, but I couldn't find docs indicating what other exceptions are possible
      // here. I'd rather err on having users of this class always being able to continue without a
      // screenshot (which is often just a bonus), hence catching any exception at all.
      System.err.println("Error getting item screenshot for: " + item.getName());
      e.printStackTrace();
      return Optional.empty();
    }
  }

}
