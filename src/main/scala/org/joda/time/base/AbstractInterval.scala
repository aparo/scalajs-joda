/*
 *  Copyright 2001-2011 Stephen Colebourne
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
package org.joda.time.base

import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.MutableInterval
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.ReadableInstant
import org.joda.time.ReadableInterval
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * AbstractInterval provides the common behaviour for time intervals.
 * <p>
 * This class should generally not be used directly by API users. The
 * {@link ReadableInterval} interface should be used when different
 * kinds of intervals are to be referenced.
 * <p>
 * AbstractInterval subclasses may be mutable and not thread-safe.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
abstract class AbstractInterval extends ReadableInterval {
  /**
   * Constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Validates an interval.
   *
   * @param start  the start instant in milliseconds
   * @param end  the end instant in milliseconds
   * @throws IllegalArgumentException if the interval is invalid
   */
  protected def checkInterval(start: Long, end: Long) {
    if (end < start) {
      throw new IllegalArgumentException("The end instant must be greater or equal to the start")
    }
  }

  /**
   * Gets the start of this time interval, which is inclusive, as a DateTime.
   *
   * @return the start of the time interval
   */
  def getStart: DateTime = {
    return new DateTime(getStartMillis, getChronology)
  }

  /**
   * Gets the end of this time interval, which is exclusive, as a DateTime.
   *
   * @return the end of the time interval
   */
  def getEnd: DateTime = {
    return new DateTime(getEndMillis, getChronology)
  }

  /**
   * Does this time interval contain the specified millisecond instant.
   * <p>
   * Non-zero duration intervals are inclusive of the start instant and
   * exclusive of the end. A zero duration interval cannot contain anything.
   *
   * @param millisInstant  the instant to compare to,
   *                       millisecond instant from 1970-01-01T00:00:00Z
   * @return true if this time interval contains the millisecond
   */
  def contains(millisInstant: Long): Boolean = {
    val thisStart: Long = getStartMillis
    val thisEnd: Long = getEndMillis
    return (millisInstant >= thisStart && millisInstant < thisEnd)
  }

  /**
   * Does this time interval contain the current instant.
   * <p>
   * Non-zero duration intervals are inclusive of the start instant and
   * exclusive of the end. A zero duration interval cannot contain anything.
   *
   * @return true if this time interval contains the current instant
   */
  def containsNow: Boolean = {
    return contains(DateTimeUtils.currentTimeMillis)
  }

  /**
   * Does this time interval contain the specified instant.
   * <p>
   * Non-zero duration intervals are inclusive of the start instant and
   * exclusive of the end. A zero duration interval cannot contain anything.
   * <p>
   * For example:
   * <pre>
   * [09:00 to 10:00) contains 08:59  = false (before start)
   * [09:00 to 10:00) contains 09:00  = true
   * [09:00 to 10:00) contains 09:59  = true
   * [09:00 to 10:00) contains 10:00  = false (equals end)
   * [09:00 to 10:00) contains 10:01  = false (after end)
   *
   * [14:00 to 14:00) contains 14:00  = false (zero duration contains nothing)
   * </pre>
   * Passing in a <code>null</code> parameter will have the same effect as
   * calling {@link #containsNow()}.
   *
   * @param instant  the instant, null means now
   * @return true if this time interval contains the instant
   */
  def contains(instant: ReadableInstant): Boolean = {
    if (instant == null) {
      return containsNow
    }
    return contains(instant.getMillis)
  }

  /**
   * Does this time interval contain the specified time interval.
   * <p>
   * Non-zero duration intervals are inclusive of the start instant and
   * exclusive of the end. The other interval is contained if this interval
   * wholly contains, starts, finishes or equals it.
   * A zero duration interval cannot contain anything.
   * <p>
   * When two intervals are compared the result is one of three states:
   * (a) they abut, (b) there is a gap between them, (c) they overlap.
   * The <code>contains</code> method is not related to these states.
   * In particular, a zero duration interval is contained at the start of
   * a larger interval, but does not overlap (it abuts instead).
   * <p>
   * For example:
   * <pre>
   * [09:00 to 10:00) contains [09:00 to 10:00)  = true
   * [09:00 to 10:00) contains [09:00 to 09:30)  = true
   * [09:00 to 10:00) contains [09:30 to 10:00)  = true
   * [09:00 to 10:00) contains [09:15 to 09:45)  = true
   * [09:00 to 10:00) contains [09:00 to 09:00)  = true
   *
   * [09:00 to 10:00) contains [08:59 to 10:00)  = false (otherStart before thisStart)
   * [09:00 to 10:00) contains [09:00 to 10:01)  = false (otherEnd after thisEnd)
   * [09:00 to 10:00) contains [10:00 to 10:00)  = false (otherStart equals thisEnd)
   *
   * [14:00 to 14:00) contains [14:00 to 14:00)  = false (zero duration contains nothing)
   * </pre>
   * Passing in a <code>null</code> parameter will have the same effect as
   * calling {@link #containsNow()}.
   *
   * @param interval  the time interval to compare to, null means a zero duration interval now
   * @return true if this time interval contains the time interval
   */
  def contains(interval: ReadableInterval): Boolean = {
    if (interval == null) {
      return containsNow
    }
    val otherStart: Long = interval.getStartMillis
    val otherEnd: Long = interval.getEndMillis
    val thisStart: Long = getStartMillis
    val thisEnd: Long = getEndMillis
    return (thisStart <= otherStart && otherStart < thisEnd && otherEnd <= thisEnd)
  }

  /**
   * Does this time interval overlap the specified time interval.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   * An interval overlaps another if it shares some common part of the
   * datetime continuum.
   * <p>
   * When two intervals are compared the result is one of three states:
   * (a) they abut, (b) there is a gap between them, (c) they overlap.
   * The abuts state takes precedence over the other two, thus a zero duration
   * interval at the start of a larger interval abuts and does not overlap.
   * <p>
   * For example:
   * <pre>
   * [09:00 to 10:00) overlaps [08:00 to 08:30)  = false (completely before)
   * [09:00 to 10:00) overlaps [08:00 to 09:00)  = false (abuts before)
   * [09:00 to 10:00) overlaps [08:00 to 09:30)  = true
   * [09:00 to 10:00) overlaps [08:00 to 10:00)  = true
   * [09:00 to 10:00) overlaps [08:00 to 11:00)  = true
   *
   * [09:00 to 10:00) overlaps [09:00 to 09:00)  = false (abuts before)
   * [09:00 to 10:00) overlaps [09:00 to 09:30)  = true
   * [09:00 to 10:00) overlaps [09:00 to 10:00)  = true
   * [09:00 to 10:00) overlaps [09:00 to 11:00)  = true
   *
   * [09:00 to 10:00) overlaps [09:30 to 09:30)  = true
   * [09:00 to 10:00) overlaps [09:30 to 10:00)  = true
   * [09:00 to 10:00) overlaps [09:30 to 11:00)  = true
   *
   * [09:00 to 10:00) overlaps [10:00 to 10:00)  = false (abuts after)
   * [09:00 to 10:00) overlaps [10:00 to 11:00)  = false (abuts after)
   *
   * [09:00 to 10:00) overlaps [10:30 to 11:00)  = false (completely after)
   *
   * [14:00 to 14:00) overlaps [14:00 to 14:00)  = false (abuts before and after)
   * [14:00 to 14:00) overlaps [13:00 to 15:00)  = true
   * </pre>
   *
   * @param interval  the time interval to compare to, null means a zero length interval now
   * @return true if the time intervals overlap
   */
  def overlaps(interval: ReadableInterval): Boolean = {
    val thisStart: Long = getStartMillis
    val thisEnd: Long = getEndMillis
    if (interval == null) {
      val now: Long = DateTimeUtils.currentTimeMillis
      return (thisStart < now && now < thisEnd)
    }
    else {
      val otherStart: Long = interval.getStartMillis
      val otherEnd: Long = interval.getEndMillis
      return (thisStart < otherEnd && otherStart < thisEnd)
    }
  }

  /**
   * Is this interval equal to the specified interval ignoring the chronology.
   * <p>
   * This compares the underlying instants, ignoring the chronology.
   *
   * @param other  a readable interval to check against
   * @return true if the intervals are equal comparing the start and end millis
   * @since 2.3
   */
  def isEqual(other: ReadableInterval): Boolean = {
    return getStartMillis == other.getStartMillis && getEndMillis == other.getEndMillis
  }

  /**
   * Is this time interval before the specified millisecond instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @param millisInstant  the instant to compare to,
   *                       millisecond instant from 1970-01-01T00:00:00Z
   * @return true if this time interval is before the instant
   */
  def isBefore(millisInstant: Long): Boolean = {
    return (getEndMillis <= millisInstant)
  }

  /**
   * Is this time interval before the current instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @return true if this time interval is before the current instant
   */
  def isBeforeNow: Boolean = {
    return isBefore(DateTimeUtils.currentTimeMillis)
  }

  /**
   * Is this time interval before the specified instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @param instant  the instant to compare to, null means now
   * @return true if this time interval is before the instant
   */
  def isBefore(instant: ReadableInstant): Boolean = {
    if (instant == null) {
      return isBeforeNow
    }
    return isBefore(instant.getMillis)
  }

  /**
   * Is this time interval entirely before the specified instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @param interval  the interval to compare to, null means now
   * @return true if this time interval is before the interval specified
   */
  def isBefore(interval: ReadableInterval): Boolean = {
    if (interval == null) {
      return isBeforeNow
    }
    return isBefore(interval.getStartMillis)
  }

  /**
   * Is this time interval after the specified millisecond instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @param millisInstant  the instant to compare to,
   *                       millisecond instant from 1970-01-01T00:00:00Z
   * @return true if this time interval is after the instant
   */
  def isAfter(millisInstant: Long): Boolean = {
    return (getStartMillis > millisInstant)
  }

  /**
   * Is this time interval after the current instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @return true if this time interval is after the current instant
   */
  def isAfterNow: Boolean = {
    return isAfter(DateTimeUtils.currentTimeMillis)
  }

  /**
   * Is this time interval after the specified instant.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   *
   * @param instant  the instant to compare to, null means now
   * @return true if this time interval is after the instant
   */
  def isAfter(instant: ReadableInstant): Boolean = {
    if (instant == null) {
      return isAfterNow
    }
    return isAfter(instant.getMillis)
  }

  /**
   * Is this time interval entirely after the specified interval.
   * <p>
   * Intervals are inclusive of the start instant and exclusive of the end.
   * Only the end time of the specified interval is used in the comparison.
   *
   * @param interval  the interval to compare to, null means now
   * @return true if this time interval is after the interval specified
   */
  def isAfter(interval: ReadableInterval): Boolean = {
    var endMillis: Long = 0L
    if (interval == null) {
      endMillis = DateTimeUtils.currentTimeMillis
    }
    else {
      endMillis = interval.getEndMillis
    }
    return (getStartMillis >= endMillis)
  }

  /**
   * Get this interval as an immutable <code>Interval</code> object.
   *
   * @return the interval as an Interval object
   */
  def toInterval: Interval = {
    return new Interval(getStartMillis, getEndMillis, getChronology)
  }

  /**
   * Get this time interval as a <code>MutableInterval</code>.
   * <p>
   * This will always return a new <code>MutableInterval</code> with the same interval.
   *
   * @return the time interval as a MutableInterval object
   */
  def toMutableInterval: MutableInterval = {
    return new MutableInterval(getStartMillis, getEndMillis, getChronology)
  }

  /**
   * Gets the duration of this time interval in milliseconds.
   * <p>
   * The duration is equal to the end millis minus the start millis.
   *
   * @return the duration of the time interval in milliseconds
   * @throws ArithmeticException if the duration exceeds the capacity of a long
   */
  def toDurationMillis: Long = {
    return FieldUtils.safeAdd(getEndMillis, -getStartMillis)
  }

  /**
   * Gets the duration of this time interval.
   * <p>
   * The duration is equal to the end millis minus the start millis.
   *
   * @return the duration of the time interval
   * @throws ArithmeticException if the duration exceeds the capacity of a long
   */
  def toDuration: Duration = {
    val durMillis: Long = toDurationMillis
    if (durMillis == 0) {
      return Duration.ZERO
    }
    else {
      return new Duration(durMillis)
    }
  }

  /**
   * Converts the duration of the interval to a <code>Period</code> using the
   * All period type.
   * <p>
   * This method should be used to exract the field values describing the
   * difference between the start and end instants.
   *
   * @return a time period derived from the interval
   */
  def toPeriod: Period = {
    return new Period(getStartMillis, getEndMillis, getChronology)
  }

  /**
   * Converts the duration of the interval to a <code>Period</code> using the
   * specified period type.
   * <p>
   * This method should be used to exract the field values describing the
   * difference between the start and end instants.
   *
   * @param type  the requested type of the duration, null means AllType
   * @return a time period derived from the interval
   */
  def toPeriod(`type`: PeriodType): Period = {
    return new Period(getStartMillis, getEndMillis, `type`, getChronology)
  }

  /**
   * Compares this object with the specified object for equality based
   * on start and end millis plus the chronology.
   * All ReadableInterval instances are accepted.
   * <p>
   * To compare the duration of two time intervals, use {@link #toDuration()}
   * to get the durations and compare those.
   *
   * @param readableInterval  a readable interval to check against
   * @return true if the intervals are equal comparing the start millis,
   *         end millis and chronology
   */
  override def equals(readableInterval: AnyRef): Boolean = {
    if (this eq readableInterval) {
      return true
    }
    if (readableInterval.isInstanceOf[ReadableInterval] == false) {
      return false
    }
    val other: ReadableInterval = readableInterval.asInstanceOf[ReadableInterval]
    return getStartMillis == other.getStartMillis && getEndMillis == other.getEndMillis && FieldUtils.equals(getChronology, other.getChronology)
  }

  /**
   * Hashcode compatible with equals method.
   *
   * @return suitable hashcode
   */
  override def hashCode: Int = {
    val start: Long = getStartMillis
    val end: Long = getEndMillis
    var result: Int = 97
    result = 31 * result + ((start ^ (start >>> 32)).toInt)
    result = 31 * result + ((end ^ (end >>> 32)).toInt)
    result = 31 * result + getChronology.hashCode
    return result
  }

  /**
   * Output a string in ISO8601 interval format.
   * <p>
   * From version 2.1, the string includes the time zone offset.
   *
   * @return re-parsable string (in the default zone)
   */
  override def toString: String = {
    var printer: DateTimeFormatter = ISODateTimeFormat.dateTime
    printer = printer.withChronology(getChronology)
    val buf: StringBuffer = new StringBuffer(48)
    printer.printTo(buf, getStartMillis)
    buf.append('/')
    printer.printTo(buf, getEndMillis)
    return buf.toString
  }
}