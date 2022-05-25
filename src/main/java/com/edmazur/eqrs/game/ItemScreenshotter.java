package com.edmazur.eqrs.game;

import java.io.File;
import java.util.UUID;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ItemScreenshotter {

  private static final String CHROME_DRIVER =
      "/home/mazur/git/everquest-robot-stanvern/src/main/resources/chromedriver-101.0.4951.41";

  public File get(Item item) {
    System.setProperty("webdriver.chrome.driver", CHROME_DRIVER);
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
    return screenshot;
  }

}
