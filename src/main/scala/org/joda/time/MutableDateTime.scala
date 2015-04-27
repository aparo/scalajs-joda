/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Locale
import org.joda.convert.FromString
import org.joda.time.base.BaseDateTime
import org.joda.time.chrono.ISOChronology
import org.joda.time.field.AbstractReadableInstantFieldProperty
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * MutableDateTime is the standard implementation of a modifiable datetime class.
 * It holds the datetime as milliseconds from the Java epoch of 1970-01-01T00:00:00Z.
 * <p>
 * This class uses a Chronology internally. The Chronology determines how the
 * millisecond instant value is converted into the date time fields.
 * The default Chronology is <code>ISOChronology</code> which is the agreed
 * international standard and compatible with the modern Gregorian calendar.
 * <p>
 * Each individual field can be accessed in two ways:
 * <ul>
 * <li><code>getHourOfDay()</code>
 * <li><code>hourOfDay().get()</code>
 * </ul>
 * The second technique also provides access to other useful methods on the
 * field:
 * <ul>
 * <li>get numeric value
 * <li>set numeric value
 * <li>add to numeric value
 * <li>add to numeric value wrapping with the field
 * <li>get text value
 * <li>get short text value
 * <li>set text value
 * <li>field maximum value
 * <li>field minimum value
 * </ul>
 *
 * <p>
 * MutableDateTime is mutable and not thread-safe, unless concurrent threads
 * are not invoking mutator methods.
 *
 * @author Guy Allard
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @author Mike Schrag
 * @since 1.0
 * @see DateTime
 */
@SerialVersionUID(2852608688135209575L)
object MutableDateTime {
  /** Rounding is disabled */
  val ROUND_NONE: Int = 0
  /** Rounding mode as described by {@link DateTimeField#roundFloor} */
  val ROUND_FLOOR: Int = 1
  /** Rounding mode as described by {@link DateTimeField#roundCeiling} */
  val ROUND_CEILING: Int = 2
  /** Rounding mode as described by {@link DateTimeField#roundHalfFloor} */
  val ROUND_HALF_FLOOR: Int = 3
  /** Rounding mode as described by {@link DateTimeField#roundHalfCeiling} */
  val ROUND_HALF_CEILING: Int = 4
  /** Rounding mode as described by {@link DateTimeField#roundHalfEven} */
  val ROUND_HALF_EVEN: Int = 5

  /**
   * Obtains a {@code MutableDateTime} set to the current system millisecond time
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @return the current date-time, not null
   * @since 2.0
   */
  def now: MutableDateTime = {
    return new MutableDateTime
  }

  /**
   * Obtains a {@code MutableDateTime} set to the current system millisecond time
   * using <code>ISOChronology</code> in the specified time zone.
   *
   * @param zone  the time zone, not null
   * @return the current date-time, not null
   * @since 2.0
   */
  def now(zone: DateTimeZone): MutableDateTime = {
    if (zone == null) {
      throw new NullPointerException("Zone must not be null")
    }
    return new MutableDateTime(zone)
  }

  /**
   * Obtains a {@code MutableDateTime} set to the current system millisecond time
   * using the specified chronology.
   *
   * @param chronology  the chronology, not null
   * @return the current date-time, not null
   * @since 2.0
   */
  def now(chronology: Chronology): MutableDateTime = {
    if (chronology == null) {
      throw new NullPointerException("Chronology must not be null")
    }
    return new MutableDateTime(chronology)
  }

  /**
   * Parses a {@code MutableDateTime} from the specified string.
   * <p>
   * This uses {@link ISODateTimeFormat#dateTimeParser()}.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  @FromString def parse(str: String): MutableDateTime = {
    return parse(str, ISODateTimeFormat.dateTimeParser.withOffsetParsed)
  }

  /**
   * Parses a {@code MutableDateTime} from the specified string using a formatter.
   *
   * @param str  the string to parse, not null
   * @param formatter  the formatter to use, not null
   * @since 2.0
   */
  def parse(str: String, formatter: DateTimeFormatter): MutableDateTime = {
    return formatter.parseDateTime(str).toMutableDateTime
  }

  /**
   * MutableDateTime.Property binds a MutableDateTime to a
   * DateTimeField allowing powerful datetime functionality to be easily
   * accessed.
   * <p>
   * The example below shows how to use the property to change the value of a
   * MutableDateTime object.
   * <pre>
   * MutableDateTime dt = new MutableDateTime(1972, 12, 3, 13, 32, 19, 123);
   * dt.year().add(20);
   * dt.second().roundFloor().minute().set(10);
   * </pre>
   * <p>
   * MutableDateTime.Propery itself is thread-safe and immutable, but the
   * MutableDateTime being operated on is not.
   *
   * @author Stephen Colebourne
   * @author Brian S O'Neill
   * @since 1.0
   */
  @SerialVersionUID(-4481126543819298617L)
  final class Property extends AbstractReadableInstantFieldProperty {
    /** The instant this property is working against */
    private var iInstant: MutableDateTime = null
    /** The field this property is working against */
    private var iField: DateTimeField = null

    /**
     * Constructor.
     *
     * @param instant  the instant to set
     * @param field  the field to use
     */
    private[time] def this(instant: MutableDateTime, field: DateTimeField) {
      this()
      `super`
      iInstant = instant
      iField = field
    }

    /**
     * Writes the property in a safe serialization format.
     */
    @throws(classOf[IOException])
    private def writeObject(oos: ObjectOutputStream) {
      oos.writeObject(iInstant)
      oos.writeObject(iField.getType)
    }

    /**
     * Reads the property from a safe serialization format.
     */
    @throws(classOf[IOException])
    @throws(classOf[ClassNotFoundException])
    private def readObject(oos: ObjectInputStream) {
      iInstant = oos.readObject.asInstanceOf[MutableDateTime]
      val `type`: DateTimeFieldType = oos.readObject.asInstanceOf[DateTimeFieldType]
      iField = `type`.getField(iInstant.getChronology)
    }

    /**
     * Gets the field being used.
     *
     * @return the field
     */
    def getField: DateTimeField = {
      return iField
    }

    /**
     * Gets the milliseconds of the datetime that this property is linked to.
     *
     * @return the milliseconds
     */
    protected def getMillis: Long = {
      return iInstant.getMillis
    }

    /**
     * Gets the chronology of the datetime that this property is linked to.
     *
     * @return the chronology
     * @since 1.4
     */
    protected override def getChronology: Chronology = {
      return iInstant.getChronology
    }

    /**
     * Gets the mutable datetime being used.
     *
     * @return the mutable datetime
     */
    def getMutableDateTime: MutableDateTime = {
      return iInstant
    }

    /**
     * Adds a value to the millis value.
     *
     * @param value  the value to add
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#add(long,int)
     */
    def add(value: Int): MutableDateTime = {
      iInstant.setMillis(getField.add(iInstant.getMillis, value))
      return iInstant
    }

    /**
     * Adds a value to the millis value.
     *
     * @param value  the value to add
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#add(long,long)
     */
    def add(value: Long): MutableDateTime = {
      iInstant.setMillis(getField.add(iInstant.getMillis, value))
      return iInstant
    }

    /**
     * Adds a value, possibly wrapped, to the millis value.
     *
     * @param value  the value to add
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#addWrapField
     */
    def addWrapField(value: Int): MutableDateTime = {
      iInstant.setMillis(getField.addWrapField(iInstant.getMillis, value))
      return iInstant
    }

    /**
     * Sets a value.
     *
     * @param value  the value to set.
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#set(long,int)
     */
    def set(value: Int): MutableDateTime = {
      iInstant.setMillis(getField.set(iInstant.getMillis, value))
      return iInstant
    }

    /**
     * Sets a text value.
     *
     * @param text  the text value to set
     * @param locale  optional locale to use for selecting a text symbol
     * @return the mutable datetime being used, so calls can be chained
     * @throws IllegalArgumentException if the text value isn't valid
     * @see DateTimeField#set(long,java.lang.String,java.util.Locale)
     */
    def set(text: String, locale: Locale): MutableDateTime = {
      iInstant.setMillis(getField.set(iInstant.getMillis, text, locale))
      return iInstant
    }

    /**
     * Sets a text value.
     *
     * @param text  the text value to set
     * @return the mutable datetime being used, so calls can be chained
     * @throws IllegalArgumentException if the text value isn't valid
     * @see DateTimeField#set(long,java.lang.String)
     */
    def set(text: String): MutableDateTime = {
      set(text, null)
      return iInstant
    }

    /**
     * Round to the lowest whole unit of this field.
     *
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#roundFloor
     */
    def roundFloor: MutableDateTime = {
      iInstant.setMillis(getField.roundFloor(iInstant.getMillis))
      return iInstant
    }

    /**
     * Round to the highest whole unit of this field.
     *
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#roundCeiling
     */
    def roundCeiling: MutableDateTime = {
      iInstant.setMillis(getField.roundCeiling(iInstant.getMillis))
      return iInstant
    }

    /**
     * Round to the nearest whole unit of this field, favoring the floor if
     * halfway.
     *
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#roundHalfFloor
     */
    def roundHalfFloor: MutableDateTime = {
      iInstant.setMillis(getField.roundHalfFloor(iInstant.getMillis))
      return iInstant
    }

    /**
     * Round to the nearest whole unit of this field, favoring the ceiling if
     * halfway.
     *
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#roundHalfCeiling
     */
    def roundHalfCeiling: MutableDateTime = {
      iInstant.setMillis(getField.roundHalfCeiling(iInstant.getMillis))
      return iInstant
    }

    /**
     * Round to the nearest whole unit of this field. If halfway, the ceiling
     * is favored over the floor only if it makes this field's value even.
     *
     * @return the mutable datetime being used, so calls can be chained
     * @see DateTimeField#roundHalfEven
     */
    def roundHalfEven: MutableDateTime = {
      iInstant.setMillis(getField.roundHalfEven(iInstant.getMillis))
      return iInstant
    }
  }

}

@SerialVersionUID(2852608688135209575L)
class MutableDateTime extends BaseDateTime with ReadWritableDateTime with Cloneable with Serializable {
  /** The field to round on */
  private var iRoundingField: DateTimeField = null
  /** The mode of rounding */
  private var iRoundingMode: Int = 0

  /**
   * Constructs an instance set to the current system millisecond time
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @see #now()
   */
  def this() {
    this()
    `super`
  }

  /**
   * Constructs an instance set to the current system millisecond time
   * using <code>ISOChronology</code> in the specified time zone.
   * <p>
   * If the specified time zone is null, the default zone is used.
   *
   * @param zone  the time zone, null means default zone
   * @see #now(DateTimeZone)
   */
  def this(zone: DateTimeZone) {
    this()
    `super`(zone)
  }

  /**
   * Constructs an instance set to the current system millisecond time
   * using the specified chronology.
   * <p>
   * If the chronology is null, <code>ISOChronology</code>
   * in the default time zone is used.
   *
   * @param chronology  the chronology, null means ISOChronology in default zone
   * @see #now(org.joda.Chronology)
   */
  def this(chronology: Chronology) {
    this()
    `super`(chronology)
  }

  /**
   * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z
   */
  def this(instant: Long) {
    this()
    `super`(instant)
  }

  /**
   * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z
   * using <code>ISOChronology</code> in the specified time zone.
   * <p>
   * If the specified time zone is null, the default zone is used.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z
   * @param zone  the time zone, null means default zone
   */
  def this(instant: Long, zone: DateTimeZone) {
    this()
    `super`(instant, zone)
  }

  /**
   * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z
   * using the specified chronology.
   * <p>
   * If the chronology is null, <code>ISOChronology</code>
   * in the default time zone is used.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z
   * @param chronology  the chronology, null means ISOChronology in default zone
   */
  def this(instant: Long, chronology: Chronology) {
    this()
    `super`(instant, chronology)
  }

  /**
   * Constructs an instance from an Object that represents a datetime.
   * <p>
   * If the object implies a chronology (such as GregorianCalendar does),
   * then that chronology will be used. Otherwise, ISO default is used.
   * Thus if a GregorianCalendar is passed in, the chronology used will
   * be GJ, but if a Date is passed in the chronology will be ISO.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadableInstant, String, Calendar and Date.
   *
   * @param instant  the datetime object, null means now
   * @throws IllegalArgumentException if the instant is invalid
   */
  def this(instant: AnyRef) {
    this()
    `super`(instant, null.asInstanceOf[Chronology])
  }

  /**
   * Constructs an instance from an Object that represents a datetime,
   * forcing the time zone to that specified.
   * <p>
   * If the object implies a chronology (such as GregorianCalendar does),
   * then that chronology will be used, but with the time zone adjusted.
   * Otherwise, ISO is used in the specified time zone.
   * If the specified time zone is null, the default zone is used.
   * Thus if a GregorianCalendar is passed in, the chronology used will
   * be GJ, but if a Date is passed in the chronology will be ISO.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadableInstant, String, Calendar and Date.
   *
   * @param instant  the datetime object, null means now
   * @param zone  the time zone, null means default time zone
   * @throws IllegalArgumentException if the instant is invalid
   */
  def this(instant: AnyRef, zone: DateTimeZone) {
    this()
    `super`(instant, zone)
  }

  /**
   * Constructs an instance from an Object that represents a datetime,
   * using the specified chronology.
   * <p>
   * If the chronology is null, ISO in the default time zone is used.
   * Any chronology implied by the object (such as GregorianCalendar does)
   * is ignored.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadableInstant, String, Calendar and Date.
   *
   * @param instant  the datetime object, null means now
   * @param chronology  the chronology, null means ISOChronology in default zone
   * @throws IllegalArgumentException if the instant is invalid
   */
  def this(instant: AnyRef, chronology: Chronology) {
    this()
    `super`(instant, DateTimeUtils.getChronology(chronology))
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the specified time zone.
   * <p>
   * If the specified time zone is null, the default zone is used.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @param zone  the time zone, null means default time zone
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int, zone: DateTimeZone) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, zone)
  }

  /**
   * Constructs an instance from datetime field values
   * using the specified chronology.
   * <p>
   * If the chronology is null, <code>ISOChronology</code>
   * in the default time zone is used.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @param chronology  the chronology, null means ISOChronology in default zone
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int, chronology: Chronology) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, chronology)
  }

  /**
   * Gets the field used for rounding this instant, returning null if rounding
   * is not enabled.
   *
   * @return the rounding field
   */
  def getRoundingField: DateTimeField = {
    return iRoundingField
  }

  /**
   * Gets the rounding mode for this instant, returning ROUND_NONE if rounding
   * is not enabled.
   *
   * @return the rounding mode constant
   */
  def getRoundingMode: Int = {
    return iRoundingMode
  }

  /**
   * Sets the status of rounding to use the specified field and ROUND_FLOOR mode.
   * A null field will disable rounding.
   * Once set, the instant is then rounded using the new field and mode.
   * <p>
   * Enabling rounding will cause all subsequent calls to {@link #setMillis(long)}
   * to be rounded. This can be used to control the precision of the instant,
   * for example by setting a rounding field of minuteOfDay, the seconds and
   * milliseconds will always be zero.
   *
   * @param field rounding field or null to disable
   */
  def setRounding(field: DateTimeField) {
    setRounding(field, MutableDateTime.ROUND_FLOOR)
  }

  /**
   * Sets the status of rounding to use the specified field and mode.
   * A null field or mode of ROUND_NONE will disable rounding.
   * Once set, the instant is then rounded using the new field and mode.
   * <p>
   * Enabling rounding will cause all subsequent calls to {@link #setMillis(long)}
   * to be rounded. This can be used to control the precision of the instant,
   * for example by setting a rounding field of minuteOfDay, the seconds and
   * milliseconds will always be zero.
   *
   * @param field  rounding field or null to disable
   * @param mode  rounding mode or ROUND_NONE to disable
   * @throws IllegalArgumentException if mode is unknown, no exception if field is null
   */
  def setRounding(field: DateTimeField, mode: Int) {
    if (field != null && (mode < MutableDateTime.ROUND_NONE || mode > MutableDateTime.ROUND_HALF_EVEN)) {
      throw new IllegalArgumentException("Illegal rounding mode: " + mode)
    }
    iRoundingField = (if (mode == MutableDateTime.ROUND_NONE) null else field)
    iRoundingMode = (if (field == null) MutableDateTime.ROUND_NONE else mode)
    setMillis(getMillis)
  }

  /**
   * Set the milliseconds of the datetime.
   * <p>
   * All changes to the millisecond field occurs via this method.
   *
   * @param instant  the milliseconds since 1970-01-01T00:00:00Z to set the
   *                 datetime to
   */
  override def setMillis(instant: Long) {
    iRoundingMode match {
      case MutableDateTime.ROUND_NONE =>
        break //todo: break is not supported
      case MutableDateTime.ROUND_FLOOR =>
        instant = iRoundingField.roundFloor(instant)
        break //todo: break is not supported
      case MutableDateTime.ROUND_CEILING =>
        instant = iRoundingField.roundCeiling(instant)
        break //todo: break is not supported
      case MutableDateTime.ROUND_HALF_FLOOR =>
        instant = iRoundingField.roundHalfFloor(instant)
        break //todo: break is not supported
      case MutableDateTime.ROUND_HALF_CEILING =>
        instant = iRoundingField.roundHalfCeiling(instant)
        break //todo: break is not supported
      case MutableDateTime.ROUND_HALF_EVEN =>
        instant = iRoundingField.roundHalfEven(instant)
        break //todo: break is not supported
    }
    super.setMillis(instant)
  }

  /**
   * Sets the millisecond instant of this instant from another.
   * <p>
   * This method does not change the chronology of this instant, just the
   * millisecond instant.
   *
   * @param instant  the instant to use, null means now
   */
  def setMillis(instant: ReadableInstant) {
    val instantMillis: Long = DateTimeUtils.getInstantMillis(instant)
    setMillis(instantMillis)
  }

  /**
   * Add an amount of time to the datetime.
   *
   * @param duration  the millis to add
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(duration: Long) {
    setMillis(FieldUtils.safeAdd(getMillis, duration))
  }

  /**
   * Adds a duration to this instant.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param duration  the duration to add, null means add zero
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(duration: ReadableDuration) {
    add(duration, 1)
  }

  /**
   * Adds a duration to this instant specifying how many times to add.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param duration  the duration to add, null means add zero
   * @param scalar  direction and amount to add, which may be negative
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(duration: ReadableDuration, scalar: Int) {
    if (duration != null) {
      add(FieldUtils.safeMultiply(duration.getMillis, scalar))
    }
  }

  /**
   * Adds a period to this instant.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param period  the period to add, null means add zero
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(period: ReadablePeriod) {
    add(period, 1)
  }

  /**
   * Adds a period to this instant specifying how many times to add.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param period  the period to add, null means add zero
   * @param scalar  direction and amount to add, which may be negative
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(period: ReadablePeriod, scalar: Int) {
    if (period != null) {
      setMillis(getChronology.add(period, getMillis, scalar))
    }
  }

  /**
   * Set the chronology of the datetime.
   * <p>
   * All changes to the chronology occur via this method.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   */
  override def setChronology(chronology: Chronology) {
    super.setChronology(chronology)
  }

  /**
   * Sets the time zone of the datetime, changing the chronology and field values.
   * <p>
   * Changing the zone using this method retains the millisecond instant.
   * The millisecond instant is adjusted in the new zone to compensate.
   *
   * chronology. Setting the time zone does not affect the millisecond value
   * of this instant.
   * <p>
   * If the chronology already has this time zone, no change occurs.
   *
   * @param newZone  the time zone to use, null means default zone
   * @see #setZoneRetainFields
   */
  def setZone(newZone: DateTimeZone) {
    newZone = DateTimeUtils.getZone(newZone)
    val chrono: Chronology = getChronology
    if (chrono.getZone ne newZone) {
      setChronology(chrono.withZone(newZone))
    }
  }

  /**
   * Sets the time zone of the datetime, changing the chronology and millisecond.
   * <p>
   * Changing the zone using this method retains the field values.
   * The millisecond instant is adjusted in the new zone to compensate.
   * <p>
   * If the chronology already has this time zone, no change occurs.
   *
   * @param newZone  the time zone to use, null means default zone
   * @see #setZone
   */
  def setZoneRetainFields(newZone: DateTimeZone) {
    newZone = DateTimeUtils.getZone(newZone)
    val originalZone: DateTimeZone = DateTimeUtils.getZone(getZone)
    if (newZone eq originalZone) {
      return
    }
    val millis: Long = originalZone.getMillisKeepLocal(newZone, getMillis)
    setChronology(getChronology.withZone(newZone))
    setMillis(millis)
  }

  /**
   * Sets the value of one of the fields of the instant, such as hourOfDay.
   *
   * @param type  a field type, usually obtained from DateTimeFieldType, not null
   * @param value  the value to set the field to
   * @throws IllegalArgumentException if the value is null or invalid
   */
  def set(`type`: DateTimeFieldType, value: Int) {
    if (`type` == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    setMillis(`type`.getField(getChronology).set(getMillis, value))
  }

  /**
   * Adds to the instant specifying the duration and multiple to add.
   *
   * @param type  a field type, usually obtained from DateTimeFieldType, not null
   * @param amount  the amount to add of this duration
   * @throws IllegalArgumentException if the value is null or invalid
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(`type`: DurationFieldType, amount: Int) {
    if (`type` == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    if (amount != 0) {
      setMillis(`type`.getField(getChronology).add(getMillis, amount))
    }
  }

  /**
   * Set the year to the specified value.
   *
   * @param year  the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setYear(year: Int) {
    setMillis(getChronology.year.set(getMillis, year))
  }

  /**
   * Add a number of years to the date.
   *
   * @param years  the years to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addYears(years: Int) {
    if (years != 0) {
      setMillis(getChronology.years.add(getMillis, years))
    }
  }

  /**
   * Set the weekyear to the specified value.
   *
   * @param weekyear  the weekyear
   * @throws IllegalArgumentException if the value is invalid
   */
  def setWeekyear(weekyear: Int) {
    setMillis(getChronology.weekyear.set(getMillis, weekyear))
  }

  /**
   * Add a number of weekyears to the date.
   *
   * @param weekyears  the weekyears to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addWeekyears(weekyears: Int) {
    if (weekyears != 0) {
      setMillis(getChronology.weekyears.add(getMillis, weekyears))
    }
  }

  /**
   * Set the month of the year to the specified value.
   *
   * @param monthOfYear  the month of the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMonthOfYear(monthOfYear: Int) {
    setMillis(getChronology.monthOfYear.set(getMillis, monthOfYear))
  }

  /**
   * Add a number of months to the date.
   *
   * @param months  the months to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMonths(months: Int) {
    if (months != 0) {
      setMillis(getChronology.months.add(getMillis, months))
    }
  }

  /**
   * Set the week of weekyear to the specified value.
   *
   * @param weekOfWeekyear the week of the weekyear
   * @throws IllegalArgumentException if the value is invalid
   */
  def setWeekOfWeekyear(weekOfWeekyear: Int) {
    setMillis(getChronology.weekOfWeekyear.set(getMillis, weekOfWeekyear))
  }

  /**
   * Add a number of weeks to the date.
   *
   * @param weeks  the weeks to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addWeeks(weeks: Int) {
    if (weeks != 0) {
      setMillis(getChronology.weeks.add(getMillis, weeks))
    }
  }

  /**
   * Set the day of year to the specified value.
   *
   * @param dayOfYear the day of the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfYear(dayOfYear: Int) {
    setMillis(getChronology.dayOfYear.set(getMillis, dayOfYear))
  }

  /**
   * Set the day of the month to the specified value.
   *
   * @param dayOfMonth  the day of the month
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfMonth(dayOfMonth: Int) {
    setMillis(getChronology.dayOfMonth.set(getMillis, dayOfMonth))
  }

  /**
   * Set the day of week to the specified value.
   *
   * @param dayOfWeek  the day of the week
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfWeek(dayOfWeek: Int) {
    setMillis(getChronology.dayOfWeek.set(getMillis, dayOfWeek))
  }

  /**
   * Add a number of days to the date.
   *
   * @param days  the days to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addDays(days: Int) {
    if (days != 0) {
      setMillis(getChronology.days.add(getMillis, days))
    }
  }

  /**
   * Set the hour of the day to the specified value.
   *
   * @param hourOfDay  the hour of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setHourOfDay(hourOfDay: Int) {
    setMillis(getChronology.hourOfDay.set(getMillis, hourOfDay))
  }

  /**
   * Add a number of hours to the date.
   *
   * @param hours  the hours to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addHours(hours: Int) {
    if (hours != 0) {
      setMillis(getChronology.hours.add(getMillis, hours))
    }
  }

  /**
   * Set the minute of the day to the specified value.
   *
   * @param minuteOfDay  the minute of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMinuteOfDay(minuteOfDay: Int) {
    setMillis(getChronology.minuteOfDay.set(getMillis, minuteOfDay))
  }

  /**
   * Set the minute of the hour to the specified value.
   *
   * @param minuteOfHour  the minute of hour
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMinuteOfHour(minuteOfHour: Int) {
    setMillis(getChronology.minuteOfHour.set(getMillis, minuteOfHour))
  }

  /**
   * Add a number of minutes to the date.
   *
   * @param minutes  the minutes to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMinutes(minutes: Int) {
    if (minutes != 0) {
      setMillis(getChronology.minutes.add(getMillis, minutes))
    }
  }

  /**
   * Set the second of the day to the specified value.
   *
   * @param secondOfDay  the second of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setSecondOfDay(secondOfDay: Int) {
    setMillis(getChronology.secondOfDay.set(getMillis, secondOfDay))
  }

  /**
   * Set the second of the minute to the specified value.
   *
   * @param secondOfMinute  the second of minute
   * @throws IllegalArgumentException if the value is invalid
   */
  def setSecondOfMinute(secondOfMinute: Int) {
    setMillis(getChronology.secondOfMinute.set(getMillis, secondOfMinute))
  }

  /**
   * Add a number of seconds to the date.
   *
   * @param seconds  the seconds to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addSeconds(seconds: Int) {
    if (seconds != 0) {
      setMillis(getChronology.seconds.add(getMillis, seconds))
    }
  }

  /**
   * Set the millis of the day to the specified value.
   *
   * @param millisOfDay  the millis of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMillisOfDay(millisOfDay: Int) {
    setMillis(getChronology.millisOfDay.set(getMillis, millisOfDay))
  }

  /**
   * Set the millis of the second to the specified value.
   *
   * @param millisOfSecond  the millis of second
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMillisOfSecond(millisOfSecond: Int) {
    setMillis(getChronology.millisOfSecond.set(getMillis, millisOfSecond))
  }

  /**
   * Add a number of milliseconds to the date. The implementation of this
   * method differs from the {@link #add(long)} method in that a
   * DateTimeField performs the addition.
   *
   * @param millis  the milliseconds to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMillis(millis: Int) {
    if (millis != 0) {
      setMillis(getChronology.millis.add(getMillis, millis))
    }
  }

  /**
   * Set the date from milliseconds.
   * The time part of this object will be unaffected.
   *
   * @param instant  an instant to copy the date from, time part ignored
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDate(instant: Long) {
    setMillis(getChronology.millisOfDay.set(instant, getMillisOfDay))
  }

  /**
   * Set the date from another instant.
   * The time part of this object will be unaffected.
   * <p>
   * If the input is a {@code ReadableDateTime} then it is converted to the
   * same time-zone as this object before using the instant millis.
   *
   * @param instant  an instant to copy the date from, time part ignored
   * @throws IllegalArgumentException if the object is invalid
   */
  def setDate(instant: ReadableInstant) {
    var instantMillis: Long = DateTimeUtils.getInstantMillis(instant)
    if (instant.isInstanceOf[ReadableDateTime]) {
      val rdt: ReadableDateTime = instant.asInstanceOf[ReadableDateTime]
      val instantChrono: Chronology = DateTimeUtils.getChronology(rdt.getChronology)
      val zone: DateTimeZone = instantChrono.getZone
      if (zone != null) {
        instantMillis = zone.getMillisKeepLocal(getZone, instantMillis)
      }
    }
    setDate(instantMillis)
  }

  /**
   * Set the date from fields.
   * The time part of this object will be unaffected.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
    val c: Chronology = getChronology
    val instantMidnight: Long = c.getDateTimeMillis(year, monthOfYear, dayOfMonth, 0)
    setDate(instantMidnight)
  }

  /**
   * Set the time from milliseconds.
   * The date part of this object will be unaffected.
   *
   * @param millis  an instant to copy the time from, date part ignored
   * @throws IllegalArgumentException if the value is invalid
   */
  def setTime(millis: Long) {
    val millisOfDay: Int = ISOChronology.getInstanceUTC.millisOfDay.get(millis)
    setMillis(getChronology.millisOfDay.set(getMillis, millisOfDay))
  }

  /**
   * Set the time from another instant.
   * The date part of this object will be unaffected.
   *
   * @param instant  an instant to copy the time from, date part ignored
   * @throws IllegalArgumentException if the object is invalid
   */
  def setTime(instant: ReadableInstant) {
    var instantMillis: Long = DateTimeUtils.getInstantMillis(instant)
    val instantChrono: Chronology = DateTimeUtils.getInstantChronology(instant)
    val zone: DateTimeZone = instantChrono.getZone
    if (zone != null) {
      instantMillis = zone.getMillisKeepLocal(DateTimeZone.UTC, instantMillis)
    }
    setTime(instantMillis)
  }

  /**
   * Set the time from fields.
   * The date part of this object will be unaffected.
   *
   * @param hour  the hour
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @throws IllegalArgumentException if the value is invalid
   */
  def setTime(hour: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int) {
    val instant: Long = getChronology.getDateTimeMillis(getMillis, hour, minuteOfHour, secondOfMinute, millisOfSecond)
    setMillis(instant)
  }

  /**
   * Set the date and time from fields.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDateTime(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int) {
    val instant: Long = getChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    setMillis(instant)
  }

  /**
   * Gets the property object for the specified type, which contains many useful methods.
   *
   * @param type  the field type to get the chronology for
   * @return the property object
   * @throws IllegalArgumentException if the field is null or unsupported
   * @since 1.2
   */
  def property(`type`: DateTimeFieldType): MutableDateTime.Property = {
    if (`type` == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null")
    }
    val field: DateTimeField = `type`.getField(getChronology)
    if (field.isSupported == false) {
      throw new IllegalArgumentException("Field '" + `type` + "' is not supported")
    }
    return new MutableDateTime.Property(this, field)
  }

  /**
   * Get the era property.
   *
   * @return the era property
   */
  def era: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.era)
  }

  /**
   * Get the century of era property.
   *
   * @return the year of era property
   */
  def centuryOfEra: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.centuryOfEra)
  }

  /**
   * Get the year of century property.
   *
   * @return the year of era property
   */
  def yearOfCentury: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.yearOfCentury)
  }

  /**
   * Get the year of era property.
   *
   * @return the year of era property
   */
  def yearOfEra: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.yearOfEra)
  }

  /**
   * Get the year property.
   *
   * @return the year property
   */
  def year: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.year)
  }

  /**
   * Get the year of a week based year property.
   *
   * @return the year of a week based year property
   */
  def weekyear: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.weekyear)
  }

  /**
   * Get the month of year property.
   *
   * @return the month of year property
   */
  def monthOfYear: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.monthOfYear)
  }

  /**
   * Get the week of a week based year property.
   *
   * @return the week of a week based year property
   */
  def weekOfWeekyear: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.weekOfWeekyear)
  }

  /**
   * Get the day of year property.
   *
   * @return the day of year property
   */
  def dayOfYear: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.dayOfYear)
  }

  /**
   * Get the day of month property.
   * <p>
   * The values for day of month are defined in {@link DateTimeConstants}.
   *
   * @return the day of month property
   */
  def dayOfMonth: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.dayOfMonth)
  }

  /**
   * Get the day of week property.
   * <p>
   * The values for day of week are defined in {@link DateTimeConstants}.
   *
   * @return the day of week property
   */
  def dayOfWeek: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.dayOfWeek)
  }

  /**
   * Get the hour of day field property
   *
   * @return the hour of day property
   */
  def hourOfDay: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.hourOfDay)
  }

  /**
   * Get the minute of day property
   *
   * @return the minute of day property
   */
  def minuteOfDay: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.minuteOfDay)
  }

  /**
   * Get the minute of hour field property
   *
   * @return the minute of hour property
   */
  def minuteOfHour: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.minuteOfHour)
  }

  /**
   * Get the second of day property
   *
   * @return the second of day property
   */
  def secondOfDay: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.secondOfDay)
  }

  /**
   * Get the second of minute field property
   *
   * @return the second of minute property
   */
  def secondOfMinute: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.secondOfMinute)
  }

  /**
   * Get the millis of day property
   *
   * @return the millis of day property
   */
  def millisOfDay: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.millisOfDay)
  }

  /**
   * Get the millis of second property
   *
   * @return the millis of second property
   */
  def millisOfSecond: MutableDateTime.Property = {
    return new MutableDateTime.Property(this, getChronology.millisOfSecond)
  }

  /**
   * Clone this object without having to cast the returned object.
   *
   * @return a clone of the this object.
   */
  def copy: MutableDateTime = {
    return clone.asInstanceOf[MutableDateTime]
  }

  /**
   * Clone this object.
   *
   * @return a clone of this object.
   */
  override def clone: AnyRef = {
    try {
      return super.clone
    }
    catch {
      case ex: CloneNotSupportedException => {
        throw new InternalError("Clone error")
      }
    }
  }
}