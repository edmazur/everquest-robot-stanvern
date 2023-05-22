package com.edmazur.eqrs.game;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
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

  public Optional<File> get(Item item) {
    try {
      WebDriverManager.chromedriver().setup();
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--headless");
      options.addArguments("--window-size=1980,960");
      ChromeDriver driver = new ChromeDriver(options);

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
