package com.squareup.shaneal.location;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: smanek
 *
 * A purely in-memory implementation of a LocationStore.
 */
public class MemoryLocationStore implements LocationStore {

  private static final long MAX_SUPPORTED_LOCATION_IDS = 10000;
  private static final long MAX_SUPPORTED_LOCATION_ID_LENGTH = 1000;

  private final Set<String> knownLocations;
  private final ReadWriteLock lock; // used to synchronize access to the HashSet knownLocations

  private static final MemoryLocationStore INSTANCE = new MemoryLocationStore();

  public static MemoryLocationStore getInstance() {
    return INSTANCE;
  }

  private MemoryLocationStore() {
    this.knownLocations = new HashSet<String>();
    lock = new ReentrantReadWriteLock();
  }

  private static void checkLocationId(String locationId) throws StorageException {
    if (locationId == null) {
      throw new StorageException("We don't support null locationIds");
    } else if (locationId.length() > MAX_SUPPORTED_LOCATION_ID_LENGTH) {
      throw new StorageException("The locationIds length is " + locationId.length()
          + " which is greater than the max supported length of " + MAX_SUPPORTED_LOCATION_ID_LENGTH);
    }
  }

  @Override
  public void add(String locationId) throws StorageException {
    checkLocationId(locationId);
    lock.writeLock().lock();
    try {
      if (knownLocations.size() > MAX_SUPPORTED_LOCATION_IDS) {
        throw new StorageException("The MemoryLocationStore is full (it only supports " + MAX_SUPPORTED_LOCATION_IDS
            + " elements).");
      } else {
        knownLocations.add(locationId);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void remove(String locationId) throws StorageException {
    checkLocationId(locationId);
    lock.writeLock().lock();
    try {
      knownLocations.remove(locationId);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public boolean contains(String locationId) throws StorageException {
    lock.readLock().lock();
    try {
      return knownLocations.contains(locationId);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Collection<String> list() throws StorageException {
    lock.readLock().lock();
    try {
      return Collections.unmodifiableCollection(knownLocations);
    } finally {
      lock.readLock().unlock();
    }
  }
}
