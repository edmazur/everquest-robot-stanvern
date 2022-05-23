package com.edmazur.eqrs;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// TODO: Give callers a way to control order that options get applied (because order matters for the
// "convert" command). The current order is arbitrary.
public class OcrStrategy {

  private boolean disableAlpha;
  private boolean enableNegate;
  private boolean enableContrast;
  private Integer resizePercent;
  private BigDecimal blurRadius;
  private BigDecimal blurSigma;

  public OcrStrategy disableAlpha() {
    this.disableAlpha = true;
    return this;
  }

  public OcrStrategy enableNegate() {
    this.enableNegate = true;
    return this;
  }

  public OcrStrategy enableContrast() {
    this.enableContrast = true;
    return this;
  }

  public OcrStrategy enableResize(int resizePercent) {
    this.resizePercent = resizePercent;
    return this;
  }

  public OcrStrategy enableBlur(BigDecimal blurRadius, BigDecimal blurSigma) {
    this.blurRadius = blurRadius;
    this.blurSigma = blurSigma;
    return this;
  }

  public String[] getCommand(File input, File output) {
    List<String> commandParts = new ArrayList<>();
    commandParts.add("convert");
    commandParts.add(input.getAbsolutePath());
    if (disableAlpha) {
      commandParts.add("-alpha");
      commandParts.add("off");
    }
    if (enableNegate) {
      commandParts.add("-channel");
      commandParts.add("RGB");
      commandParts.add("-negate");
    }
    if (enableContrast) {
      commandParts.add("+contrast");
    }
    if (resizePercent != null) {
      commandParts.add("-resize");
      commandParts.add(resizePercent + "%");
    }
    if (blurRadius != null && blurSigma != null) {
      commandParts.add("-gaussian-blur");
      commandParts.add(blurRadius.toString() + "x" + blurSigma.toString());
    }
    commandParts.add(output.getAbsolutePath());
    return commandParts.toArray(new String[0]);
  }

}
