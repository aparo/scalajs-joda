/*
 *  Copyright 2001-2010 Stephen Colebourne
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
import org.joda.convert.FromString
import org.joda.time.base.AbstractInstant
import org.joda.time.chrono.ISOChronology
import org.joda.time.convert.ConverterManager
import org.joda.time.convert.InstantConverter
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * Instant is the standard implementation of a fully immutable instant in time.
 * <p>
 * <code>Instant</code> is an implementation of {@link ReadableInstant}.
 * As with all instants, it represents an exact point on the time-line,
 * but limited to the precision of milliseconds. An <code>Instant</code>
 * should be used to represent a point in time irrespective of any other
 * factor, such as chronology or time zone.
 * <p>
 * Internally, the class holds one piece of data, the instant as milliseconds
 * from the Java epoch of 1970-01-01T00:00:00Z.
 * <p>
 * For example, an Instant can be used to compare two <code>DateTime</code>
 * objects irrespective of chronology or time zone.
 * <pre>
 * boolean sameInstant = dt1.toInstant().equals(dt2.toInstant());
 * </pre>
 * Note that the following code will also perform the same check:
 * <pre>
 * boolean sameInstant = dt1.isEqual(dt2);
 * </pre>
 * <p>
 * Instant is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(3299096530934209741L)
object Instant {
  /**
   * Obtains an {@code Instant} set to the current system millisecond time.
   *
   * @return the current instant, not null
   * @since 2.0
   */
  def now: Instant = {
    return new Instant
  }

  /**
   * Parses a {@code Instant} from the specified string.
   * <p>
   * This uses {@link ISODateTimeFormat#dateTimeParser()}.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  @FromString def parse(str: String): Instant = {
    return parse(str, ISODateTimeFormat.dateTimeParser)
  }

  /**
   * Parses a {@code Instant} from the specified string using a formatter.
   *
   * @param str  the string to parse, not null
   * @param formatter  the formatter to use, not null
   * @since 2.0
   */
  def parse(str: String, formatter: DateTimeFormatter): Instant = {
    return formatter.parseDateTime(str).toInstant
  }
}

@SerialVersionUID(3299096530934209741L)
final class Instant extends AbstractInstant with ReadableInstant with Serializable {
  /** The millis from 1970-01-01T00:00:00Z */
  private final val iMillis: Long = 0L

  /**
   * Constructs an instance set to the current system millisecond time.
   *
   * @see #now()
   */
  def this() {
    this()
    `super`
    iMillis = DateTimeUtils.currentTimeMillis
  }

  /**
   * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z
   */
  def this(instant: Long) {
    this()
    `super`
    iMillis = instant
  }

  /**
   * Constructs an instance from an Object that represents a datetime.
   * <p>
   * The recognised object types are defined in {@link ConverterManager} and
   * include String, Calendar and Date.
   *
   * @param instant  the datetime object, null means now
   * @throws IllegalArgumentException if the instant is invalid
   */
  def this(instant: AnyRef) {
    this()
    `super`
    val converter: InstantConverter = ConverterManager.getInstance.getInstantConverter(instant)
    iMillis = converter.getInstantMillis(instant, ISOChronology.getInstanceUTC)
  }

  /**
   * Get this object as an Instant by returning <code>this</code>.
   *
   * @return <code>this</code>
   */
  override def toInstant: Instant = {
    return this
  }

  /**
   * Gets a copy of this instant with different millis.
   * <p>
   * The returned object will be either be a new Instant or <code>this</code>.
   *
   * @param newMillis  the new millis, from 1970-01-01T00:00:00Z
   * @return a copy of this instant with different millis
   */
  def withMillis(newMillis: Long): Instant = {
    return (if (newMillis == iMillis) this else new Instant(newMillis))
  }

  /**
   * Gets a copy of this instant with the specified duration added.
   * <p>
   * If the addition is zero, then <code>this</code> is returned.
   *
   * @param durationToAdd  the duration to add to this one
   * @param scalar  the amount of times to add, such as -1 to subtract once
   * @return a copy of this instant with the duration added
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def withDurationAdded(durationToAdd: Long, scalar: Int): Instant = {
    if (durationToAdd == 0 || scalar == 0) {
      return this
    }
    val instant: Long = getChronology.add(getMillis, durationToAdd, scalar)
    return withMillis(instant)
  }

  /**
   * Gets a copy of this instant with the specified duration added.
   * <p>
   * If the addition is zero, then <code>this</code> is returned.
   *
   * @param durationToAdd  the duration to add to this one, null means zero
   * @param scalar  the amount of times to add, such as -1 to subtract once
   * @return a copy of this instant with the duration added
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def withDurationAdded(durationToAdd: ReadableDuration, scalar: Int): Instant = {
    if (durationToAdd == null || scalar == 0) {
      return this
    }
    return withDurationAdded(durationToAdd.getMillis, scalar)
  }

  /**
   * Gets a copy of this instant with the specified duration added.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   *
   * @param duration  the duration to add to this one
   * @return a copy of this instant with the duration added
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def plus(duration: Long): Instant = {
    return withDurationAdded(duration, 1)
  }

  /**
   * Gets a copy of this instant with the specified duration added.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   *
   * @param duration  the duration to add to this one, null means zero
   * @return a copy of this instant with the duration added
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def plus(duration: ReadableDuration): Instant = {
    return withDurationAdded(duration, 1)
  }

  /**
   * Gets a copy of this instant with the specified duration taken away.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   *
   * @param duration  the duration to reduce this instant by
   * @return a copy of this instant with the duration taken away
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def minus(duration: Long): Instant = {
    return withDurationAdded(duration, -1)
  }

  /**
   * Gets a copy of this instant with the specified duration taken away.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   *
   * @param duration  the duration to reduce this instant by
   * @return a copy of this instant with the duration taken away
   * @throws ArithmeticException if the new instant exceeds the capacity of a long
   */
  def minus(duration: ReadableDuration): Instant = {
    return withDurationAdded(duration, -1)
  }

  /**
   * Gets the milliseconds of the instant.
   *
   * @return the number of milliseconds since 1970-01-01T00:00:00Z
   */
  def getMillis: Long = {
    return iMillis
  }

  /**
   * Gets the chronology of the instant, which is ISO in the UTC zone.
   * <p>
   * This method returns {@link ISOChronology#getInstanceUTC()} which
   * corresponds to the definition of the Java epoch 1970-01-01T00:00:00Z.
   *
   * @return ISO in the UTC zone
   */
  def getChronology: Chronology = {
    return ISOChronology.getInstanceUTC
  }

  /**
   * Get this object as a DateTime using ISOChronology in the default zone.
   * <p>
   * This method returns a DateTime object in the default zone.
   * This differs from the similarly named method on DateTime, DateMidnight
   * or MutableDateTime which retains the time zone. The difference is
   * because Instant really represents a time <i>without</i> a zone,
   * thus calling this method we really have no zone to 'retain' and
   * hence expect to switch to the default zone.
   * <p>
   * This method definition preserves compatibility with earlier versions
   * of Joda-Time.
   *
   * @return a DateTime using the same millis
   */
  override def toDateTime: DateTime = {
    return new DateTime(getMillis, ISOChronology.getInstance)
  }

  /**
   * Get this object as a DateTime using ISOChronology in the default zone.
   * This method is identical to <code>toDateTime()</code>.
   * <p>
   * This method returns a DateTime object in the default zone.
   * This differs from the similarly named method on DateTime, DateMidnight
   * or MutableDateTime which retains the time zone. The difference is
   * because Instant really represents a time <i>without</i> a zone,
   * thus calling this method we really have no zone to 'retain' and
   * hence expect to switch to the default zone.
   * <p>
   * This method is deprecated because it is a duplicate of {@link #toDateTime()}.
   * However, removing it would cause the superclass implementation to be used,
   * which would create silent bugs in any caller depending on this implementation.
   * As such, the method itself is not currently planned to be removed.
   * <p>
   * This method definition preserves compatibility with earlier versions
   * of Joda-Time.
   *
   * @return a DateTime using the same millis with ISOChronology
   * @deprecated Use toDateTime() as it is identical
   */
  @deprecated override def toDateTimeISO: DateTime = {
    return toDateTime
  }

  /**
   * Get this object as a MutableDateTime using ISOChronology in the default zone.
   * <p>
   * This method returns a MutableDateTime object in the default zone.
   * This differs from the similarly named method on DateTime, DateMidnight
   * or MutableDateTime which retains the time zone. The difference is
   * because Instant really represents a time <i>without</i> a zone,
   * thus calling this method we really have no zone to 'retain' and
   * hence expect to switch to the default zone.
   * <p>
   * This method definition preserves compatibility with earlier versions
   * of Joda-Time.
   *
   * @return a MutableDateTime using the same millis
   */
  override def toMutableDateTime: MutableDateTime = {
    return new MutableDateTime(getMillis, ISOChronology.getInstance)
  }

  /**
   * Get this object as a MutableDateTime using ISOChronology in the default zone.
   * This method is identical to <code>toMutableDateTime()</code>.
   * <p>
   * This method returns a MutableDateTime object in the default zone.
   * This differs from the similarly named method on DateTime, DateMidnight
   * or MutableDateTime which retains the time zone. The difference is
   * because Instant really represents a time <i>without</i> a zone,
   * thus calling this method we really have no zone to 'retain' and
   * hence expect to switch to the default zone.
   * <p>
   * This method is deprecated because it is a duplicate of {@link #toMutableDateTime()}.
   * However, removing it would cause the superclass implementation to be used,
   * which would create silent bugs in any caller depending on this implementation.
   * As such, the method itself is not currently planned to be removed.
   * <p>
   * This method definition preserves compatibility with earlier versions
   * of Joda-Time.
   *
   * @return a MutableDateTime using the same millis with ISOChronology
   * @deprecated Use toMutableDateTime() as it is identical
   */
  @deprecated override def toMutableDateTimeISO: MutableDateTime = {
    return toMutableDateTime
  }
}