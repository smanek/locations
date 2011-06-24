package com.squareup.shaneal.stats;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Keep a moving average.
 *
 * My primary desiderata for this class was to not force the 'observe' method to be synchronized
 * (i.e., to allow multiple observe() calls to be executed simultaneously).
 *
 * Towards that end, I created this approach with a ScheduledExecutorService pruning data outside
 * of the window.
 *
 */
public class RollingAverage {

  private static final ScheduledExecutorService TICK_THREAD = Executors.newScheduledThreadPool(2);

  private static final TimeUnit GRANULARITY = TimeUnit.SECONDS;
  // how often old samples are pruned (in GRANULARITY units)
  // the mean and variance may, at worst, include data from INTERVAL time outside the rolling window
  private static final long INTERVAL = 1;

  /**
   * Creates a new RollingAverage
   *
   * @param windowSize the size of the window statistics should be kept for
   * @param windowUnit the unit the window size is given in
   * @return a new RollingAverage
   */
  public static RollingAverage newRollingAverage(long windowSize, TimeUnit windowUnit) {
    final RollingAverage meter = new RollingAverage(windowSize, windowUnit);

    final Runnable job = new Runnable() {
      public void run() {
        meter.tick();
      }
    };

    TICK_THREAD.scheduleAtFixedRate(job, INTERVAL, INTERVAL, GRANULARITY);

    return meter;
  }

  /**
   * Observe the occurrence of a particular value of an event
   *
   * @param val the value of the event
   */
  public void observe(long val) {
    // unfortunately, I need a readLock here to ensure this does not run concurrently with mean/variance
    // (in which case, total could be updated without count being updated, thus resulting in incorrect stats).
    lock.readLock().lock();
    try {
      this.samples.offer(new Sample(val));
      this.total.addAndGet(val);
      this.count.incrementAndGet();
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Returns the number of observations that have been recorded within the window.
   *
   * @return the number of observations that have been recorded within the window.
   */
  public long count() {
    return count.get();
  }


  /**
   * Returns the mean observed value observed within the window.
   *
   * @return the mean observed value observed within the window
   */
  public double mean() {
    // we need a writeLock here to prevent it from running in parallel with observe()
    // there is a race if observe updates the total but not the count while we're computing
    // the mean.
    lock.writeLock().lock();
    try {
      if (count() == 0) {
        return 0.0;
      } else {
        return (double) this.total.get() / count();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Returns the variance of values within the window.
   *
   * @return the variance of values within the window.
   */
  public double variance() {
    // we need a writeLock here to ensure we get a consistent snapshot of the samples between when we sum the
    // squared deviations from the mean, and when we compute the mean.
    lock.writeLock().lock();
    try {
      long count = count();
      
      if (count == 0) {
        return 0.0;
      } else {
        double mean = mean();
        double squaredDeviationSum = 0;
        for (Sample sample : samples) {
          squaredDeviationSum += Math.pow(sample.getVal() - mean, 2.0);
        }
        return squaredDeviationSum / (double) count;
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private RollingAverage(final long windowSize, final TimeUnit windowUnit) {
    this.windowSize = windowUnit.convert(windowSize, GRANULARITY);
    this.total = new AtomicLong(0);
    this.count = new AtomicLong(0);
    
    // we need a thread safe (and ideally non-blocking and lock-free) data structure so we can allow multiple observe()
    // calls concurrently
    this.samples = new ConcurrentLinkedQueue<Sample>();
  }

  // a list of samples within the rolling window
  private final Queue<Sample> samples;

  // the sum of all the samples values the window. This is a minor optimization that is only useful if mean() is
  // called without variance() being called - since variance()
  private final AtomicLong total;

  // the total number of samples (the size() method on a ConcurrentLinkedQueue is O(n)).
  private final AtomicLong count;

  // the size of the rolling window, in GRANULARITY units
  private final long windowSize;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();


  private void tick() {
    lock.writeLock().lock();

    try {
      final Iterator<Sample> iterator = samples.iterator();
      final long now = System.currentTimeMillis();
      
      while (iterator.hasNext()) {
        Sample s = iterator.next();
        if (TimeUnit.MILLISECONDS.convert(now - s.getTimestamp(), GRANULARITY) > (now - windowSize)) {
          total.addAndGet(0 - s.getVal());
          count.decrementAndGet();
          iterator.remove();
        } else {
          break;
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Inner class representing one sample's data.
   */
  private static class Sample {
    private final long val;
    private final long timestamp;

    public Sample(long value) {
      this.timestamp = System.currentTimeMillis();
      this.val = value;
    }

    public long getVal() {
      return val;
    }

    public long getTimestamp() {
      return timestamp;
    }

    @Override
    public String toString() {
      return "Sample{" +
          "val=" + val +
          ", timestamp=" + timestamp +
          '}';
    }
  }
}
