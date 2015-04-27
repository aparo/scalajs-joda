/*
 *  Copyright 2001-2005 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time

/**
 * Writable interface for an interval.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
trait ReadWritableInterval extends ReadableInterval {
  /**
   * Sets this interval from two millisecond instants.
   *
   * @param startInstant  the start of the time interval
   * @param endInstant  the start of the time interval
   * @throws IllegalArgumentException if the end is before the start
   */
  def setInterval(startInstant: Long, endInstant: Long)

  /**
   * Sets this interval to be the same as another.
   *
   * @param interval  the interval to copy
   * @throws IllegalArgumentException if the end is before the start
   */
  def setInterval(interval: ReadableInterval)

  /**
   * Sets this interval from two instants.
   *
   * @param startInstant  the start of the time interval
   * @param endInstant  the start of the time interval
   * @throws IllegalArgumentException if the end is before the start
   */
  def setInterval(startInstant: ReadableInstant, endInstant: ReadableInstant)

  /**
   * Sets the chronology of this time interval.
   *
   * @param chrono  the chronology to use, null means ISO default
   */
  def setChronology(chrono: Chronology)

  /**
   * Sets the start of this time interval.
   *
   * @param millisInstant  the start of the time interval,
   *                       millisecond instant from 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if the end is before the start
   */
  def setStartMillis(millisInstant: Long)

  /**
   * Sets the start of this time interval as an Instant.
   *
   * @param instant  the start of the time interval
   * @throws IllegalArgumentException if the end is before the start
   */
  def setStart(instant: ReadableInstant)

  /**
   * Sets the end of this time interval.
   *
   * @param millisInstant  the end of the time interval,
   *                       millisecond instant from 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if the end is before the start
   */
  def setEndMillis(millisInstant: Long)

  /**
   * Sets the end of this time interval as an Instant.
   *
   * @param instant  the end of the time interval
   * @throws IllegalArgumentException if the end is before the start
   */
  def setEnd(instant: ReadableInstant)

  /**
   * Sets the duration of this time interval, preserving the start instant.
   *
   * @param duration  new duration for interval
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the end instant exceeds the capacity of a long
   */
  def setDurationAfterStart(duration: ReadableDuration)

  /**
   * Sets the duration of this time interval, preserving the end instant.
   *
   * @param duration  new duration for interval
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the start instant exceeds the capacity of a long
   */
  def setDurationBeforeEnd(duration: ReadableDuration)

  /**
   * Sets the period of this time interval, preserving the start instant.
   *
   * @param period  new period for interval, null means zero length
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the end instant exceeds the capacity of a long
   */
  def setPeriodAfterStart(period: ReadablePeriod)

  /**
   * Sets the period of this time interval, preserving the end instant.
   *
   * @param period  new period for interval, null means zero length
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the start instant exceeds the capacity of a long
   */
  def setPeriodBeforeEnd(period: ReadablePeriod)
}