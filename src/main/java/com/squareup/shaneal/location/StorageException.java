package com.squareup.shaneal.location;

/**
 * User: smanek
 *
 * An exception a LocationStore implementation can throw if it has underlying problems.
 */
public class StorageException extends Exception {

  public StorageException(String s) {
    super(s);
  }

  public StorageException(String s, Throwable throwable) {
    super(s, throwable);
  }
}
