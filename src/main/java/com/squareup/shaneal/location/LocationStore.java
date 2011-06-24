package com.squareup.shaneal.location;

import java.util.Collection;

/**
 * User: smanek
 * <p/>
 * This is an interface for the LocationStore.
 */
public interface LocationStore {

  /**
   * Called to add a location to the store. Subsequent calls to contains for this locationId should return True.
   *
   * @param locationId The location ID.
   * @throws StorageException if there is a problem with the underlying storage system.
   */
  public void add(String locationId) throws StorageException;

  /**
   * Called to remove a location from the store. Subsequent calls to contains for this locationId should return False.
   *
   * @param locationId the location ID.
   * @throws StorageException if there is a problem with the underlying storage system.
   */
  public void remove(String locationId) throws StorageException;

  /**
   * Determines if the given locationId is currently in the store.
   *
   * @param locationId the locationId you want to know is in the store.
   * @return True iff the given locationId was added (without being subsequently removed) from the store.
   * @throws StorageException if there is a problem with the underlying storage system.
   */
  public boolean contains(String locationId) throws StorageException;

  /**
   * Lists all locationIds currently in the store (i.e., those for which contains() would return true).
   *
   * @return A Collection of all locationIds in the store
   * @throws StorageException if there is a problem with the underlying storage system.
   */
  public Collection<String> list() throws StorageException;
}
