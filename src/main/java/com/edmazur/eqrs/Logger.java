package com.edmazur.eqrs;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Replace this with a real logging library.
public class Logger {

  private static final Format DATE_FORMAT =
      new SimpleDateFormat("MMdd HH:mm:ss.SSS");

  public void log(String message) {
    logInternal(message);
  }

  public void log(String format, Object... messages) {
    logInternal(String.format(format, messages));
  }

  // Re-use this private method across public ones for a consistent stack depth.
  private void logInternal(String message) {
    System.out.println(String.format("%s [%s] [%s:%d] %s",
        DATE_FORMAT.format(new Date()),
        Thread.currentThread().getName(),
        Thread.currentThread().getStackTrace()[3].getClassName(),
        Thread.currentThread().getStackTrace()[3].getLineNumber(),
        message));
  }
}