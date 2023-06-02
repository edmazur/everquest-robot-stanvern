package com.edmazur.eqrs;

public class ValueOrError<T> {

  private final T value;
  private final String error;

  public static <T> ValueOrError<T> value(T value) {
    return new ValueOrError<>(value, null);
  }

  public static <T> ValueOrError<T> error(String error) {
    return new ValueOrError<>(null, error);
  }

  private ValueOrError(T value, String error) {
    this.value = value;
    this.error = error;
  }

  public T getValue() {
    return value;
  }

  public boolean hasError() {
    return error != null;
  }

  public String getError() {
    return error;
  }

}
