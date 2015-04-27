/*
 *  Copyright 2001-2006 Stephen Colebourne
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

import java.io.Serializable
import org.joda.time.base.BaseInterval
import org.joda.time.chrono.ISOChronology
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.ISOPeriodFormat

/**
 * Interval is the standard implementation of an immutable time interval.
 * <p>
 * A time interval represents a period of time between two instants.
 * Intervals are inclusive of the start instant and exclusive of the end.
 * The end instant is always greater than or equal to the start instant.
 * <p>
 * Intervals have a fixed millisecond duration.
 * This is the difference between the start and end instants.
 * The duration is represented separately by {@link ReadableDuration}.
 * As a result, intervals are not comparable.
 * To compare the length of two intervals, you should compare their durations.
 * <p>
 * An interval can also be converted to a {@link ReadablePeriod}.
 * This represents the difference between the start and end points in terms of fields
 * such as years and days.
 * <p>
 * Interval is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Sean Geoghegan
 * @author Stephen Colebourne
 * @author Julen Parra
 * @since 1.0
 */
@SerialVersionUID(4922451897541386752L)
object Interval {
  /**
   * Parses a {@code Interval} from the specified string.
   * <p>
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}
   * and {@link ISOPeriodFormat#standard()}, and may be 'datetime/datetime',
   * 'datetime/period' or 'period/datetime'.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  def parse(str: String): Interval = {
    return new Interval(str)
  }
}

@SerialVersionUID(4922451897541386752L)
final class Interval extends BaseInterval with ReadableInterval with Serializable {
  /**
   * Constructs an interval from a start and end instant with the ISO
   * default chronology in the default time zone.
   *
   * @param startInstant  start of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @param endInstant  end of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @throws IllegalArgumentException if the end is before the start
   */
  def this(startInstant: Long, endInstant: Long) {
    this()
    `super`(startInstant, endInstant, null)
  }

  /**
   * Constructs an interval from a start and end instant with the ISO
   * default chronology in the specified time zone.
   *
   * @param startInstant  start of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @param endInstant  end of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @param zone  the time zone to use, null means default zone
   * @throws IllegalArgumentException if the end is before the start
   * @since 1.5
   */
  def this(startInstant: Long, endInstant: Long, zone: DateTimeZone) {
    this()
    `super`(startInstant, endInstant, ISOChronology.getInstance(zone))
  }

  /**
   * Constructs an interval from a start and end instant with the
   * specified chronology.
   *
   * @param chronology  the chronology to use, null is ISO default
   * @param startInstant  start of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @param endInstant  end of this interval, as milliseconds from 1970-01-01T00:00:00Z.
   * @throws IllegalArgumentException if the end is before the start
   */
  def this(startInstant: Long, endInstant: Long, chronology: Chronology) {
    this()
    `super`(startInstant, endInstant, chronology)
  }

  /**
   * Constructs an interval from a start and end instant.
   * <p>
   * The chronology used is that of the start instant.
   *
   * @param start  start of this interval, null means now
   * @param end  end of this interval, null means now
   * @throws IllegalArgumentException if the end is before the start
   */
  def this(start: ReadableInstant, end: ReadableInstant) {
    this()
    `super`(start, end)
  }

  /**
   * Constructs an interval from a start instant and a duration.
   *
   * @param start  start of this interval, null means now
   * @param duration  the duration of this interval, null means zero length
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the end instant exceeds the capacity of a long
   */
  def this(start: ReadableInstant, duration: ReadableDuration) {
    this()
    `super`(start, duration)
  }

  /**
   * Constructs an interval from a millisecond duration and an end instant.
   *
   * @param duration  the duration of this interval, null means zero length
   * @param end  end of this interval, null means now
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the start instant exceeds the capacity of a long
   */
  def this(duration: ReadableDuration, end: ReadableInstant) {
    this()
    `super`(duration, end)
  }

  /**
   * Constructs an interval from a start instant and a time period.
   * <p>
   * When forming the interval, the chronology from the instant is used
   * if present, otherwise the chronology of the period is used.
   *
   * @param start  start of this interval, null means now
   * @param period  the period of this interval, null means zero length
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the end instant exceeds the capacity of a long
   */
  def this(start: ReadableInstant, period: ReadablePeriod) {
    this()
    `super`(start, period)
  }

  /**
   * Constructs an interval from a time period and an end instant.
   * <p>
   * When forming the interval, the chronology from the instant is used
   * if present, otherwise the chronology of the period is used.
   *
   * @param period  the period of this interval, null means zero length
   * @param end  end of this interval, null means now
   * @throws IllegalArgumentException if the end is before the start
   * @throws ArithmeticException if the start instant exceeds the capacity of a long
   */
  def this(period: ReadablePeriod, end: ReadableInstant) {
    this()
    `super`(period, end)
  }

  /**
   * Constructs a time interval by converting or copying from another object.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadableInterval and String.
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}
   * and {@link ISOPeriodFormat#standard()}, and may be 'datetime/datetime',
   * 'datetime/period' or 'period/datetime'.
   *
   * @param interval  the time interval to copy
   * @throws IllegalArgumentException if the interval is invalid
   */
  def this(interval: AnyRef) {
    this()
    `super`(interval, null)
  }

  /**
   * Constructs a time interval by converting or copying from another object,
   * overriding the chronology.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadableInterval and String.
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}
   * and {@link ISOPeriodFormat#standard()}, and may be 'datetime/datetime',
   * 'datetime/period' or 'period/datetime'.
   *
   * @param interval  the time interval to copy
   * @param chronology  the chronology to use, null means ISO default
   * @throws IllegalArgumentException if the interval is invalid
   */
  def this(interval: AnyRef, chronology: Chronology) {
    this()
    `super`(interval, chronology)
  }

  /**
   * Get this interval as an immutable <code>Interval</code> object
   * by returning <code>this</code>.
   *
   * @return <code>this</code>
   */
  override def toInterval: Interval = {
    return this
  }

  /**
   * Gets the overlap between this interval and another interval.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   * An interval overlaps another if it shares some common part of the
   * datetime continuum. This method returns the amount of the overlap,
   * only if the intervals actually do overlap.
   * If the intervals do not overlap, then null is returned.
   * <p>
   * When two intervals are compared the result is one of three states:
   * (a) they abut, (b) there is a gap between them, (c) they overlap.
   * The abuts state takes precedence over the other two, thus a zero duration
   * interval at the start of a larger interval abuts and does not overlap.
   * <p>
   * The chronology of the returned interval is the same as that of
   * this interval (the chronology of the interval parameter is not used).
   * Note that the use of the chronology was only correctly implemented
   * in version 1.3.
   *
   * @param interval  the interval to examine, null means now
   * @return the overlap interval, null if no overlap
   * @since 1.1
   */
  def overlap(interval: ReadableInterval): Interval = {
    interval = DateTimeUtils.getReadableInterval(interval)
    if (overlaps(interval) == false) {
      return null
    }
    val start: Long = Math.max(getStartMillis, interval.getStartMillis)
    val end: Long = Math.min(getEndMillis, interval.getEndMillis)
    return new Interval(start, end, getChronology)
  }

  /**
   * Gets the gap between this interval and another interval.
   * The other interval can be either before or after this interval.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   * An interval has a gap to another interval if there is a non-zero
   * duration between them. This method returns the amount of the gap only
   * if the intervals do actually have a gap between them.
   * If the intervals overlap or abut, then null is returned.
   * <p>
   * When two intervals are compared the result is one of three states:
   * (a) they abut, (b) there is a gap between them, (c) they overlap.
   * The abuts state takes precedence over the other two, thus a zero duration
   * interval at the start of a larger interval abuts and does not overlap.
   * <p>
   * The chronology of the returned interval is the same as that of
   * this interval (the chronology of the interval parameter is not used).
   * Note that the use of the chronology was only correctly implemented
   * in version 1.3.
   *
   * @param interval  the interval to examine, null means now
   * @return the gap interval, null if no gap
   * @since 1.1
   */
  def gap(interval: ReadableInterval): Interval = {
    interval = DateTimeUtils.getReadableInterval(interval)
    val otherStart: Long = interval.getStartMillis
    val otherEnd: Long = interval.getEndMillis
    val thisStart: Long = getStartMillis
    val thisEnd: Long = getEndMillis
    if (thisStart > otherEnd) {
      return new Interval(otherEnd, thisStart, getChronology)
    }
    else if (otherStart > thisEnd) {
      return new Interval(thisEnd, otherStart, getChronology)
    }
    else {
      return null
    }
  }

  /**
   * Does this interval abut with the interval specified.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   * An interval abuts if it starts immediately after, or ends immediately
   * before this interval without overlap.
   * A zero duration interval abuts with itself.
   * <p>
   * When two intervals are compared the result is one of three states:
   * (a) they abut, (b) there is a gap between them, (c) they overlap.
   * The abuts state takes precedence over the other two, thus a zero duration
   * interval at the start of a larger interval abuts and does not overlap.
   * <p>
   * For example:
   * <pre>
   * [09:00 to 10:00) abuts [08:00 to 08:30)  = false (completely before)
   * [09:00 to 10:00) abuts [08:00 to 09:00)  = true
   * [09:00 to 10:00) abuts [08:00 to 09:01)  = false (overlaps)
   *
   * [09:00 to 10:00) abuts [09:00 to 09:00)  = true
   * [09:00 to 10:00) abuts [09:00 to 09:01)  = false (overlaps)
   *
   * [09:00 to 10:00) abuts [10:00 to 10:00)  = true
   * [09:00 to 10:00) abuts [10:00 to 10:30)  = true
   *
   * [09:00 to 10:00) abuts [10:30 to 11:00)  = false (completely after)
   *
   * [14:00 to 14:00) abuts [14:00 to 14:00)  = true
   * [14:00 to 14:00) abuts [14:00 to 15:00)  = true
   * [14:00 to 14:00) abuts [13:00 to 14:00)  = true
   * </pre>
   *
   * @param interval  the interval to examine, null means now
   * @return true if the interval abuts
   * @since 1.1
   */
  def abuts(interval: ReadableInterval): Boolean = {
    if (interval == null) {
      val now: Long = DateTimeUtils.currentTimeMillis
      return (getStartMillis == now || getEndMillis == now)
    }
    else {
      return (interval.getEndMillis == getStartMillis || getEndMillis == interval.getStartMillis)
    }
  }

  /**
   * Creates a new interval with the same start and end, but a different chronology.
   *
   * @param chronology  the chronology to use, null means ISO default
   * @return an interval with a different chronology
   */
  def withChronology(chronology: Chronology): Interval = {
    if (getChronology eq chronology) {
      return this
    }
    return new Interval(getStartMillis, getEndMillis, chronology)
  }

  /**
   * Creates a new interval with the specified start millisecond instant.
   *
   * @param startInstant  the start instant for the new interval
   * @return an interval with the end from this interval and the specified start
   * @throws IllegalArgumentException if the resulting interval has end before start
   */
  def withStartMillis(startInstant: Long): Interval = {
    if (startInstant == getStartMillis) {
      return this
    }
    return new Interval(startInstant, getEndMillis, getChronology)
  }

  /**
   * Creates a new interval with the specified start instant.
   *
   * @param start  the start instant for the new interval, null means now
   * @return an interval with the end from this interval and the specified start
   * @throws IllegalArgumentException if the resulting interval has end before start
   */
  def withStart(start: ReadableInstant): Interval = {
    val startMillis: Long = DateTimeUtils.getInstantMillis(start)
    return withStartMillis(startMillis)
  }

  /**
   * Creates a new interval with the specified start millisecond instant.
   *
   * @param endInstant  the end instant for the new interval
   * @return an interval with the start from this interval and the specified end
   * @throws IllegalArgumentException if the resulting interval has end before start
   */
  def withEndMillis(endInstant: Long): Interval = {
    if (endInstant == getEndMillis) {
      return this
    }
    return new Interval(getStartMillis, endInstant, getChronology)
  }

  /**
   * Creates a new interval with the specified end instant.
   *
   * @param end  the end instant for the new interval, null means now
   * @return an interval with the start from this interval and the specified end
   * @throws IllegalArgumentException if the resulting interval has end before start
   */
  def withEnd(end: ReadableInstant): Interval = {
    val endMillis: Long = DateTimeUtils.getInstantMillis(end)
    return withEndMillis(endMillis)
  }

  /**
   * Creates a new interval with the specified duration after the start instant.
   *
   * @param duration  the duration to add to the start to get the new end instant, null means zero
   * @return an interval with the start from this interval and a calculated end
   * @throws IllegalArgumentException if the duration is negative
   */
  def withDurationAfterStart(duration: ReadableDuration): Interval = {
    val durationMillis: Long = DateTimeUtils.getDurationMillis(duration)
    if (durationMillis == toDurationMillis) {
      return this
    }
    val chrono: Chronology = getChronology
    val startMillis: Long = getStartMillis
    val endMillis: Long = chrono.add(startMillis, durationMillis, 1)
    return new Interval(startMillis, endMillis, chrono)
  }

  /**
   * Creates a new interval with the specified duration before the end instant.
   *
   * @param duration  the duration to subtract from the end to get the new start instant, null means zero
   * @return an interval with the end from this interval and a calculated start
   * @throws IllegalArgumentException if the duration is negative
   */
  def withDurationBeforeEnd(duration: ReadableDuration): Interval = {
    val durationMillis: Long = DateTimeUtils.getDurationMillis(duration)
    if (durationMillis == toDurationMillis) {
      return this
    }
    val chrono: Chronology = getChronology
    val endMillis: Long = getEndMillis
    val startMillis: Long = chrono.add(endMillis, durationMillis, -1)
    return new Interval(startMillis, endMillis, chrono)
  }

  /**
   * Creates a new interval with the specified period after the start instant.
   *
   * @param period  the period to add to the start to get the new end instant, null means zero
   * @return an interval with the start from this interval and a calculated end
   * @throws IllegalArgumentException if the period is negative
   */
  def withPeriodAfterStart(period: ReadablePeriod): Interval = {
    if (period == null) {
      return withDurationAfterStart(null)
    }
    val chrono: Chronology = getChronology
    val startMillis: Long = getStartMillis
    val endMillis: Long = chrono.add(period, startMillis, 1)
    return new Interval(startMillis, endMillis, chrono)
  }

  /**
   * Creates a new interval with the specified period before the end instant.
   *
   * @param period the period to subtract from the end to get the new start instant, null means zero
   * @return an interval with the end from this interval and a calculated start
   * @throws IllegalArgumentException if the period is negative
   */
  def withPeriodBeforeEnd(period: ReadablePeriod): Interval = {
    if (period == null) {
      return withDurationBeforeEnd(null)
    }
    val chrono: Chronology = getChronology
    val endMillis: Long = getEndMillis
    val startMillis: Long = chrono.add(period, endMillis, -1)
    return new Interval(startMillis, endMillis, chrono)
  }
}