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
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * DateTime is the standard implementation of an unmodifiable datetime class.
 * <p>
 * <code>DateTime</code> is the most widely used implementation of
 * {@link ReadableInstant}. As with all instants, it represents an exact
 * point on the time-line, but limited to the precision of milliseconds.
 * A <code>DateTime</code> calculates its fields with respect to a
 * {@link DateTimeZone time zone}.
 * <p>
 * Internally, the class holds two pieces of data. Firstly, it holds the
 * datetime as milliseconds from the Java epoch of 1970-01-01T00:00:00Z.
 * Secondly, it holds a {@link org.joda.Chronology} which determines how the
 * millisecond instant value is converted into the date time fields.
 * The default Chronology is {@link ISOChronology} which is the agreed
 * international standard and compatible with the modern Gregorian calendar.
 * <p>
 * Each individual field can be queried in two ways:
 * <ul>
 * <li><code>getHourOfDay()</code>
 * <li><code>hourOfDay().get()</code>
 * </ul>
 * The second technique also provides access to other useful methods on the
 * field:
 * <ul>
 * <li>numeric value
 * <li>text value
 * <li>short text value
 * <li>maximum/minimum values
 * <li>add/subtract
 * <li>set
 * <li>rounding
 * </ul>
 * <p>
 * DateTime is thread-safe and immutable, provided that the Chronology is as well.
 * All standard Chronology classes supplied are thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @author Kandarp Shah
 * @author Brian S O'Neill
 * @since 1.0
 * @see MutableDateTime
 */
object DateTime {
  /**
   * Obtains a {@code DateTime} set to the current system millisecond time
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @return the current date-time, not null
   * @since 2.0
   */
  def now: DateTime = {
    return new DateTime
  }

  /**
   * Obtains a {@code DateTime} set to the current system millisecond time
   * using <code>ISOChronology</code> in the specified time zone.
   *
   * @param zone  the time zone, not null
   * @return the current date-time, not null
   * @since 2.0
   */
  def now(zone: DateTimeZone): DateTime = {
    if (zone == null) {
      throw new NullPointerException("Zone must not be null")
    }
    return new DateTime(zone)
  }

  /**
   * Obtains a {@code DateTime} set to the current system millisecond time
   * using the specified chronology.
   *
   * @param chronology  the chronology, not null
   * @return the current date-time, not null
   * @since 2.0
   */
  def now(chronology: Chronology): DateTime = {
    if (chronology == null) {
      throw new NullPointerException("Chronology must not be null")
    }
    return new DateTime(chronology)
  }

  /**
   * Parses a {@code DateTime} from the specified string.
   * <p>
   * This uses {@link ISODateTimeFormat#dateTimeParser().withOffsetParsed()}
   * which is different to passing a {@code String} to the constructor.
   * <p>
   * Sometimes this method and {@code new DateTime(str)} return different results.
   * This can be confusing as the different is not visible in {@link #toString()}.
   * <p>
   * When passed a date-time string without an offset, such as '2010-06-30T01:20',
   * both the constructor and this method use the default time-zone.
   * As such, {@code DateTime.parse("2010-06-30T01:20")} and
   * {@code new DateTime("2010-06-30T01:20"))} are equal.
   * <p>
   * However, when this method is passed a date-time string with an offset,
   * the offset is directly parsed and stored.
   * As such, {@code DateTime.parse("2010-06-30T01:20+02:00")} and
   * {@code new DateTime("2010-06-30T01:20+02:00"))} are NOT equal.
   * The object produced via this method has a zone of {@code DateTimeZone.forOffsetHours(1)}.
   * The object produced via the constructor has a zone of {@code DateTimeZone.getDefault()}.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  @FromString def parse(str: String): DateTime = {
    return parse(str, ISODateTimeFormat.dateTimeParser.withOffsetParsed)
  }

  /**
   * Parses a {@code DateTime} from the specified string using a formatter.
   *
   * @param str  the string to parse, not null
   * @param formatter  the formatter to use, not null
   * @since 2.0
   */
  def parse(str: String, formatter: DateTimeFormatter): DateTime = {
    return formatter.parseDateTime(str)
  }

  /**
   * DateTime.Property binds a DateTime to a DateTimeField allowing powerful
   * datetime functionality to be easily accessed.
   * <p>
   * The simplest use of this class is as an alternative get method, here used to
   * get the year '1972' (as an int) and the month 'December' (as a String).
   * <pre>
   * DateTime dt = new DateTime(1972, 12, 3, 0, 0, 0, 0);
   * int year = dt.year().get();
   * String monthStr = dt.month().getAsText();
   * </pre>
   * <p>
   * Methods are also provided that allow date modification. These return new instances
   * of DateTime - they do not modify the original. The example below yields two
   * independent immutable date objects 20 years apart.
   * <pre>
   * DateTime dt = new DateTime(1972, 12, 3, 0, 0, 0, 0);
   * DateTime dt20 = dt.year().addToCopy(20);
   * </pre>
   * Serious modification of dates (ie. more than just changing one or two fields)
   * should use the {@link org.joda.time.MutableDateTime MutableDateTime} class.
   * <p>
   * DateTime.Propery itself is thread-safe and immutable, as well as the
   * DateTime being operated on.
   *
   * @author Stephen Colebourne
   * @author Brian S O'Neill
   * @since 1.0
   */

  final class Property extends AbstractReadableInstantFieldProperty {
    /** The instant this property is working against */
    private var iInstant: DateTime = null
    /** The field this property is working against */
    private var iField: DateTimeField = null

    /**
     * Constructor.
     *
     * @param instant  the instant to set
     * @param field  the field to use
     */
    private[time] def this(instant: DateTime, field: DateTimeField) {
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
      iInstant = oos.readObject.asInstanceOf[DateTime]
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
     * Gets the datetime being used.
     *
     * @return the datetime
     */
    def getDateTime: DateTime = {
      return iInstant
    }

    /**
     * Adds to this field in a copy of this DateTime.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param value  the value to add to the field in the copy
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the value isn't valid
     */
    def addToCopy(value: Int): DateTime = {
      return iInstant.withMillis(iField.add(iInstant.getMillis, value))
    }

    /**
     * Adds to this field in a copy of this DateTime.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param value  the value to add to the field in the copy
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the value isn't valid
     */
    def addToCopy(value: Long): DateTime = {
      return iInstant.withMillis(iField.add(iInstant.getMillis, value))
    }

    /**
     * Adds to this field, possibly wrapped, in a copy of this DateTime.
     * A wrapped operation only changes this field.
     * Thus 31st January addWrapField one day goes to the 1st January.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param value  the value to add to the field in the copy
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the value isn't valid
     */
    def addWrapFieldToCopy(value: Int): DateTime = {
      return iInstant.withMillis(iField.addWrapField(iInstant.getMillis, value))
    }

    /**
     * Sets this field in a copy of the DateTime.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param value  the value to set the field in the copy to
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the value isn't valid
     */
    def setCopy(value: Int): DateTime = {
      return iInstant.withMillis(iField.set(iInstant.getMillis, value))
    }

    /**
     * Sets this field in a copy of the DateTime to a parsed text value.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param text  the text value to set
     * @param locale  optional locale to use for selecting a text symbol
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the text value isn't valid
     */
    def setCopy(text: String, locale: Locale): DateTime = {
      return iInstant.withMillis(iField.set(iInstant.getMillis, text, locale))
    }

    /**
     * Sets this field in a copy of the DateTime to a parsed text value.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     * This operation is faster than converting a DateTime to a MutableDateTime
     * and back again when setting one field. When setting multiple fields,
     * it is generally quicker to make the conversion to MutableDateTime.
     *
     * @param text  the text value to set
     * @return a copy of the DateTime with the field value changed
     * @throws IllegalArgumentException if the text value isn't valid
     */
    def setCopy(text: String): DateTime = {
      return setCopy(text, null)
    }

    /**
     * Returns a new DateTime with this field set to the maximum value
     * for this field.
     * <p>
     * This operation is useful for obtaining a DateTime on the last day
     * of the month, as month lengths vary.
     * <pre>
     * DateTime lastDayOfMonth = dt.dayOfMonth().withMaximumValue();
     * </pre>
     * <p>
     * Where possible, the offset from UTC will be retained, thus applications
     * may need to call {@link DateTime#withLaterOffsetAtOverlap()} on the result
     * to force the later time during a DST overlap if desired.
     * <p>
     * From v2.2, this method handles a daylight svaings time gap, setting the
     * time to the last instant before the gap.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     *
     * @return a copy of the DateTime with this field set to its maximum
     * @since 1.2
     */
    def withMaximumValue: DateTime = {
      try {
        return setCopy(getMaximumValue)
      }
      catch {
        case ex: RuntimeException => {
          if (IllegalInstantException.isIllegalInstant(ex)) {
            val beforeGap: Long = getChronology.getZone.previousTransition(getMillis + DateTimeConstants.MILLIS_PER_DAY)
            return new DateTime(beforeGap, getChronology)
          }
          throw ex
        }
      }
    }

    /**
     * Returns a new DateTime with this field set to the minimum value
     * for this field.
     * <p>
     * Where possible, the offset from UTC will be retained, thus applications
     * may need to call {@link DateTime#withEarlierOffsetAtOverlap()} on the result
     * to force the earlier time during a DST overlap if desired.
     * <p>
     * From v2.2, this method handles a daylight svaings time gap, setting the
     * time to the first instant after the gap.
     * <p>
     * The DateTime attached to this property is unchanged by this call.
     *
     * @return a copy of the DateTime with this field set to its minimum
     * @since 1.2
     */
    def withMinimumValue: DateTime = {
      try {
        return setCopy(getMinimumValue)
      }
      catch {
        case ex: RuntimeException => {
          if (IllegalInstantException.isIllegalInstant(ex)) {
            val afterGap: Long = getChronology.getZone.nextTransition(getMillis - DateTimeConstants.MILLIS_PER_DAY)
            return new DateTime(afterGap, getChronology)
          }
          throw ex
        }
      }
    }

    /**
     * Rounds to the lowest whole unit of this field on a copy of this DateTime.
     *
     * @return a copy of the DateTime with the field value changed
     */
    def roundFloorCopy: DateTime = {
      return iInstant.withMillis(iField.roundFloor(iInstant.getMillis))
    }

    /**
     * Rounds to the highest whole unit of this field on a copy of this DateTime.
     *
     * @return a copy of the DateTime with the field value changed
     */
    def roundCeilingCopy: DateTime = {
      return iInstant.withMillis(iField.roundCeiling(iInstant.getMillis))
    }

    /**
     * Rounds to the nearest whole unit of this field on a copy of this DateTime,
     * favoring the floor if halfway.
     *
     * @return a copy of the DateTime with the field value changed
     */
    def roundHalfFloorCopy: DateTime = {
      return iInstant.withMillis(iField.roundHalfFloor(iInstant.getMillis))
    }

    /**
     * Rounds to the nearest whole unit of this field on a copy of this DateTime,
     * favoring the ceiling if halfway.
     *
     * @return a copy of the DateTime with the field value changed
     */
    def roundHalfCeilingCopy: DateTime = {
      return iInstant.withMillis(iField.roundHalfCeiling(iInstant.getMillis))
    }

    /**
     * Rounds to the nearest whole unit of this field on a copy of this
     * DateTime.  If halfway, the ceiling is favored over the floor only if
     * it makes this field's value even.
     *
     * @return a copy of the DateTime with the field value changed
     */
    def roundHalfEvenCopy: DateTime = {
      return iInstant.withMillis(iField.roundHalfEven(iInstant.getMillis))
    }
  }

}


final class DateTime extends BaseDateTime with ReadableDateTime with Serializable {
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
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}.
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
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}.
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
   * The String formats are described by {@link ISODateTimeFormat#dateTimeParser()}.
   *
   * @param instant  the datetime object, null means now
   * @param chronology  the chronology, null means ISO in default zone
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
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0)
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the specified time zone.
   * <p>
   * If the specified time zone is null, the default zone is used.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @param zone  the time zone, null means default time zone
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, zone: DateTimeZone) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0, zone)
  }

  /**
   * Constructs an instance from datetime field values
   * using the specified chronology.
   * <p>
   * If the chronology is null, <code>ISOChronology</code>
   * in the default time zone is used.
   *
   * @param year  the year, valid values defined by the chronology
   * @param monthOfYear  the month of the year, valid values defined by the chronology
   * @param dayOfMonth  the day of the month, valid values defined by the chronology
   * @param hourOfDay  the hour of the day, valid values defined by the chronology
   * @param minuteOfHour  the minute of the hour, valid values defined by the chronology
   * @param chronology  the chronology, null means ISOChronology in default zone
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, chronology: Chronology) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0, chronology)
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @param secondOfMinute  the second of the minute, from 0 to 59
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, 0)
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the specified time zone.
   * <p>
   * If the specified time zone is null, the default zone is used.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @param secondOfMinute  the second of the minute, from 0 to 59
   * @param zone  the time zone, null means default time zone
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, zone: DateTimeZone) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, 0, zone)
  }

  /**
   * Constructs an instance from datetime field values
   * using the specified chronology.
   * <p>
   * If the chronology is null, <code>ISOChronology</code>
   * in the default time zone is used.
   *
   * @param year  the year, valid values defined by the chronology
   * @param monthOfYear  the month of the year, valid values defined by the chronology
   * @param dayOfMonth  the day of the month, valid values defined by the chronology
   * @param hourOfDay  the hour of the day, valid values defined by the chronology
   * @param minuteOfHour  the minute of the hour, valid values defined by the chronology
   * @param secondOfMinute  the second of the minute, valid values defined by the chronology
   * @param chronology  the chronology, null means ISOChronology in default zone
   * @since 2.0
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, chronology: Chronology) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, 0, chronology)
  }

  /**
   * Constructs an instance from datetime field values
   * using <code>ISOChronology</code> in the default time zone.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @param secondOfMinute  the second of the minute, from 0 to 59
   * @param millisOfSecond  the millisecond of the second, from 0 to 999
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
   * @param monthOfYear  the month of the year, from 1 to 12
   * @param dayOfMonth  the day of the month, from 1 to 31
   * @param hourOfDay  the hour of the day, from 0 to 23
   * @param minuteOfHour  the minute of the hour, from 0 to 59
   * @param secondOfMinute  the second of the minute, from 0 to 59
   * @param millisOfSecond  the millisecond of the second, from 0 to 999
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
   * @param year  the year, valid values defined by the chronology
   * @param monthOfYear  the month of the year, valid values defined by the chronology
   * @param dayOfMonth  the day of the month, valid values defined by the chronology
   * @param hourOfDay  the hour of the day, valid values defined by the chronology
   * @param minuteOfHour  the minute of the hour, valid values defined by the chronology
   * @param secondOfMinute  the second of the minute, valid values defined by the chronology
   * @param millisOfSecond  the millisecond of the second, valid values defined by the chronology
   * @param chronology  the chronology, null means ISOChronology in default zone
   */
  def this(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int, chronology: Chronology) {
    this()
    `super`(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, chronology)
  }

  /**
   * Get this object as a DateTime by returning <code>this</code>.
   *
   * @return <code>this</code>
   */
  override def toDateTime: DateTime = {
    return this
  }

  /**
   * Get this object as a DateTime using ISOChronology in the default zone,
   * returning <code>this</code> if possible.
   *
   * @return a DateTime using the same millis
   */
  override def toDateTimeISO: DateTime = {
    if (getChronology eq ISOChronology.getInstance) {
      return this
    }
    return super.toDateTimeISO
  }

  /**
   * Get this object as a DateTime, returning <code>this</code> if possible.
   *
   * @param zone time zone to apply, or default if null
   * @return a DateTime using the same millis
   */
  override def toDateTime(zone: DateTimeZone): DateTime = {
    zone = DateTimeUtils.getZone(zone)
    if (getZone eq zone) {
      return this
    }
    return super.toDateTime(zone)
  }

  /**
   * Get this object as a DateTime, returning <code>this</code> if possible.
   *
   * @param chronology chronology to apply, or ISOChronology if null
   * @return a DateTime using the same millis
   */
  override def toDateTime(chronology: Chronology): DateTime = {
    chronology = DateTimeUtils.getChronology(chronology)
    if (getChronology eq chronology) {
      return this
    }
    return super.toDateTime(chronology)
  }

  /**
   * Returns a copy of this datetime with different millis.
   * <p>
   * The returned object will be either be a new instance or <code>this</code>.
   * Only the millis will change, the chronology and time zone are kept.
   *
   * @param newMillis  the new millis, from 1970-01-01T00:00:00Z
   * @return a copy of this datetime with different millis
   */
  def withMillis(newMillis: Long): DateTime = {
    return (if (newMillis == getMillis) this else new DateTime(newMillis, getChronology))
  }

  /**
   * Returns a copy of this datetime with a different chronology.
   * <p>
   * The returned object will be either be a new instance or <code>this</code>.
   * Only the chronology will change, the millis are kept.
   *
   * @param newChronology  the new chronology, null means ISO default
   * @return a copy of this datetime with a different chronology
   */
  def withChronology(newChronology: Chronology): DateTime = {
    newChronology = DateTimeUtils.getChronology(newChronology)
    return (if (newChronology eq getChronology) this else new DateTime(getMillis, newChronology))
  }

  /**
   * Returns a copy of this datetime with a different time zone, preserving the
   * millisecond instant.
   * <p>
   * This method is useful for finding the local time in another timezone.
   * For example, if this instant holds 12:30 in Europe/London, the result
   * from this method with Europe/Paris would be 13:30.
   * <p>
   * The returned object will be a new instance of the same implementation type.
   * This method changes the time zone, and does not change the
   * millisecond instant, with the effect that the field values usually change.
   * The returned object will be either be a new instance or <code>this</code>.
   *
   * @param newZone  the new time zone
   * @return a copy of this datetime with a different time zone
   * @see #withZoneRetainFields
   */
  def withZone(newZone: DateTimeZone): DateTime = {
    return withChronology(getChronology.withZone(newZone))
  }

  /**
   * Returns a copy of this datetime with a different time zone, preserving the
   * field values.
   * <p>
   * This method is useful for finding the millisecond time in another timezone.
   * For example, if this instant holds 12:30 in Europe/London (ie. 12:30Z),
   * the result from this method with Europe/Paris would be 12:30 (ie. 11:30Z).
   * <p>
   * The returned object will be a new instance of the same implementation type.
   * This method changes the time zone and the millisecond instant to keep
   * the field values the same.
   * The returned object will be either be a new instance or <code>this</code>.
   *
   * @param newZone  the new time zone, null means default
   * @return a copy of this datetime with a different time zone
   * @see #withZone
   */
  def withZoneRetainFields(newZone: DateTimeZone): DateTime = {
    newZone = DateTimeUtils.getZone(newZone)
    val originalZone: DateTimeZone = DateTimeUtils.getZone(getZone)
    if (newZone eq originalZone) {
      return this
    }
    val millis: Long = originalZone.getMillisKeepLocal(newZone, getMillis)
    return new DateTime(millis, getChronology.withZone(newZone))
  }

  /**
   * Returns a copy of this ZonedDateTime changing the zone offset to the earlier
   * of the two valid offsets at a local time-line overlap.
   * <p>
   * This method only has any effect when the local time-line overlaps, such as at
   * an autumn daylight savings cutover. In this scenario, there are two valid offsets
   * for the local date-time. Calling this method will return a date-time with the
   * earlier of the two selected.
   * <p>
   * If this method is called when it is not an overlap, this is returned.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @return a copy of this datetime with the earliest valid offset for the local datetime
   */
  def withEarlierOffsetAtOverlap: DateTime = {
    val newMillis: Long = getZone.adjustOffset(getMillis, false)
    return withMillis(newMillis)
  }

  /**
   * Returns a copy of this ZonedDateTime changing the zone offset to the later
   * of the two valid offsets at a local time-line overlap.
   * <p>
   * This method only has any effect when the local time-line overlaps, such as at
   * an autumn daylight savings cutover. In this scenario, there are two valid offsets
   * for the local date-time. Calling this method will return a date-time with the
   * later of the two selected.
   * <p>
   * If this method is called when it is not an overlap, this is returned.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @return a copy of this datetime with the latest valid offset for the local datetime
   */
  def withLaterOffsetAtOverlap: DateTime = {
    val newMillis: Long = getZone.adjustOffset(getMillis, true)
    return withMillis(newMillis)
  }

  /**
   * Returns a copy of this datetime with the specified date, retaining the time fields.
   * <p>
   * If the date is already the date passed in, then <code>this</code> is returned.
   * <p>
   * To set a single field use the properties, for example:
   * <pre>
   * DateTime set = monthOfYear().setCopy(6);
   * </pre>
   * <p>
   * If the time is invalid on the new date due to the time-zone, the time will be adjusted.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param year  the new year value
   * @param monthOfYear  the new monthOfYear value
   * @param dayOfMonth  the new dayOfMonth value
   * @return a copy of this datetime with a different date
   * @throws IllegalArgumentException if any value if invalid
   */
  def withDate(year: Int, monthOfYear: Int, dayOfMonth: Int): DateTime = {
    val chrono: Chronology = getChronology
    val localInstant: Long = chrono.withUTC.getDateTimeMillis(year, monthOfYear, dayOfMonth, getMillisOfDay)
    return withMillis(chrono.getZone.convertLocalToUTC(localInstant, false, getMillis))
  }

  /**
   * Returns a copy of this datetime with the specified date, retaining the time fields.
   * <p>
   * If the time is invalid on the new date due to the time-zone, the time will be adjusted.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param date  the local date
   * @return a copy of this datetime with a different date
   * @throws IllegalArgumentException if the time-of-day is invalid for the date
   * @throws NullPointerException if the date is null
   */
  def withDate(date: LocalDate): DateTime = {
    return withDate(date.getYear, date.getMonthOfYear, date.getDayOfMonth)
  }

  /**
   * Returns a copy of this datetime with the specified time, retaining the date fields.
   * <p>
   * If the time is already the time passed in, then <code>this</code> is returned.
   * <p>
   * To set a single field use the properties, for example:
   * <pre>
   * DateTime set = dt.hourOfDay().setCopy(6);
   * </pre>
   * <p>
   * If the new time is invalid due to the time-zone, the time will be adjusted.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @return a copy of this datetime with a different time
   * @throws IllegalArgumentException if any value if invalid
   */
  def withTime(hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): DateTime = {
    val chrono: Chronology = getChronology
    val localInstant: Long = chrono.withUTC.getDateTimeMillis(getYear, getMonthOfYear, getDayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    return withMillis(chrono.getZone.convertLocalToUTC(localInstant, false, getMillis))
  }

  /**
   * Returns a copy of this datetime with the specified time, retaining the date fields.
   * <p>
   * If the new time is invalid due to the time-zone, the time will be adjusted.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param time  the local time
   * @return a copy of this datetime with a different time
   * @throws IllegalArgumentException if the time-of-day is invalid for the date
   * @throws NullPointerException if the time is null
   */
  def withTime(time: LocalTime): DateTime = {
    return withTime(time.getHourOfDay, time.getMinuteOfHour, time.getSecondOfMinute, time.getMillisOfSecond)
  }

  /**
   * Returns a copy of this datetime with the time set to the start of the day.
   * <p>
   * The time will normally be midnight, as that is the earliest time on
   * any given day. However, in some time zones when Daylight Savings Time
   * starts, there is no midnight because time jumps from 11:59 to 01:00.
   * This method handles that situation by returning 01:00 on that date.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @return a copy of this datetime with the time set to the start of the day, not null
   */
  def withTimeAtStartOfDay: DateTime = {
    return toLocalDate.toDateTimeAtStartOfDay(getZone)
  }

  /**
   * Returns a copy of this datetime with the partial set of fields replacing those
   * from this instance.
   * <p>
   * For example, if the partial is a <code>TimeOfDay</code> then the time fields
   * would be changed in the returned instance.
   * If the partial is null, then <code>this</code> is returned.
   *
   * @param partial  the partial set of fields to apply to this datetime, null ignored
   * @return a copy of this datetime with a different set of fields
   * @throws IllegalArgumentException if any value is invalid
   */
  def withFields(partial: ReadablePartial): DateTime = {
    if (partial == null) {
      return this
    }
    return withMillis(getChronology.set(partial, getMillis))
  }

  /**
   * Returns a copy of this datetime with the specified field set to a new value.
   * <p>
   * For example, if the field type is <code>hourOfDay</code> then the hour of day
   * field would be changed in the returned instance.
   * If the field type is null, then <code>this</code> is returned.
   * <p>
   * These three lines are equivalent:
   * <pre>
   * DateTime updated = dt.withField(DateTimeFieldType.dayOfMonth(), 6);
   * DateTime updated = dt.dayOfMonth().setCopy(6);
   * DateTime updated = dt.property(DateTimeFieldType.dayOfMonth()).setCopy(6);
   * </pre>
   *
   * @param fieldType  the field type to set, not null
   * @param value  the value to set
   * @return a copy of this datetime with the field set
   * @throws IllegalArgumentException if the value is null or invalid
   */
  def withField(fieldType: DateTimeFieldType, value: Int): DateTime = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    val instant: Long = fieldType.getField(getChronology).set(getMillis, value)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime with the value of the specified field increased.
   * <p>
   * If the addition is zero or the field is null, then <code>this</code> is returned.
   * <p>
   * These three lines are equivalent:
   * <pre>
   * DateTime added = dt.withFieldAdded(DurationFieldType.years(), 6);
   * DateTime added = dt.plusYears(6);
   * DateTime added = dt.plus(Period.years(6));
   * </pre>
   *
   * @param fieldType  the field type to add to, not null
   * @param amount  the amount to add
   * @return a copy of this datetime with the field updated
   * @throws IllegalArgumentException if the value is null or invalid
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def withFieldAdded(fieldType: DurationFieldType, amount: Int): DateTime = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    if (amount == 0) {
      return this
    }
    val instant: Long = fieldType.getField(getChronology).add(getMillis, amount)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime with the specified duration added.
   * <p>
   * If the addition is zero, then <code>this</code> is returned.
   *
   * @param durationToAdd  the duration to add to this one
   * @param scalar  the amount of times to add, such as -1 to subtract once
   * @return a copy of this datetime with the duration added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def withDurationAdded(durationToAdd: Long, scalar: Int): DateTime = {
    if (durationToAdd == 0 || scalar == 0) {
      return this
    }
    val instant: Long = getChronology.add(getMillis, durationToAdd, scalar)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime with the specified duration added.
   * <p>
   * If the addition is zero, then <code>this</code> is returned.
   *
   * @param durationToAdd  the duration to add to this one, null means zero
   * @param scalar  the amount of times to add, such as -1 to subtract once
   * @return a copy of this datetime with the duration added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def withDurationAdded(durationToAdd: ReadableDuration, scalar: Int): DateTime = {
    if (durationToAdd == null || scalar == 0) {
      return this
    }
    return withDurationAdded(durationToAdd.getMillis, scalar)
  }

  /**
   * Returns a copy of this datetime with the specified period added.
   * <p>
   * If the addition is zero, then <code>this</code> is returned.
   * <p>
   * This method is typically used to add multiple copies of complex
   * period instances. Adding one field is best achieved using methods
   * like {@link #withFieldAdded(DurationFieldType, int)}
   * or {@link #plusYears(int)}.
   *
   * @param period  the period to add to this one, null means zero
   * @param scalar  the amount of times to add, such as -1 to subtract once
   * @return a copy of this datetime with the period added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def withPeriodAdded(period: ReadablePeriod, scalar: Int): DateTime = {
    if (period == null || scalar == 0) {
      return this
    }
    val instant: Long = getChronology.add(period, getMillis, scalar)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime with the specified duration added.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param duration  the duration, in millis, to add to this one
   * @return a copy of this datetime with the duration added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def plus(duration: Long): DateTime = {
    return withDurationAdded(duration, 1)
  }

  /**
   * Returns a copy of this datetime with the specified duration added.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param duration  the duration to add to this one, null means zero
   * @return a copy of this datetime with the duration added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def plus(duration: ReadableDuration): DateTime = {
    return withDurationAdded(duration, 1)
  }

  /**
   * Returns a copy of this datetime with the specified period added.
   * <p>
   * This method will add each element of the period one by one, from largest
   * to smallest, adjusting the datetime to be accurate between each.
   * <p>
   * Thus, adding a period of one month and one day to 2007-03-31 will
   * work as follows:
   * First add one month and adjust, resulting in 2007-04-30
   * Then add one day and adjust, resulting in 2007-05-01.
   * <p>
   * This method is typically used to add complex period instances.
   * Adding one field is best achieved using methods
   * like {@link #plusYears(int)}.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param period  the duration to add to this one, null means zero
   * @return a copy of this datetime with the period added
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def plus(period: ReadablePeriod): DateTime = {
    return withPeriodAdded(period, 1)
  }

  /**
   * Returns a copy of this datetime plus the specified number of years.
   * <p>
   * The calculation will do its best to only change the year field
   * retaining the same month of year.
   * However, in certain circumstances, it may be necessary to alter
   * smaller fields. For example, 2008-02-29 plus one year cannot result
   * in 2009-02-29, so the day of month is adjusted to 2009-02-28.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusYears(6);
   * DateTime added = dt.plus(Period.years(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.years(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param years  the amount of years to add, may be negative
   * @return the new datetime plus the increased years
   * @since 1.1
   */
  def plusYears(years: Int): DateTime = {
    if (years == 0) {
      return this
    }
    val instant: Long = getChronology.years.add(getMillis, years)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of months.
   * <p>
   * The calculation will do its best to only change the month field
   * retaining the same day of month.
   * However, in certain circumstances, it may be necessary to alter
   * smaller fields. For example, 2007-03-31 plus one month cannot result
   * in 2007-04-31, so the day of month is adjusted to 2007-04-30.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusMonths(6);
   * DateTime added = dt.plus(Period.months(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.months(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param months  the amount of months to add, may be negative
   * @return the new datetime plus the increased months
   * @since 1.1
   */
  def plusMonths(months: Int): DateTime = {
    if (months == 0) {
      return this
    }
    val instant: Long = getChronology.months.add(getMillis, months)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of weeks.
   * <p>
   * The calculation operates as if it were adding the equivalent in days.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusWeeks(6);
   * DateTime added = dt.plus(Period.weeks(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.weeks(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param weeks  the amount of weeks to add, may be negative
   * @return the new datetime plus the increased weeks
   * @since 1.1
   */
  def plusWeeks(weeks: Int): DateTime = {
    if (weeks == 0) {
      return this
    }
    val instant: Long = getChronology.weeks.add(getMillis, weeks)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of days.
   * <p>
   * The calculation will do its best to only change the day field
   * retaining the same time of day.
   * However, in certain circumstances, typically daylight savings cutover,
   * it may be necessary to alter the time fields.
   * <p>
   * In spring an hour is typically removed. If adding one day results in
   * the time being within the cutover then the time is adjusted to be
   * within summer time. For example, if the cutover is from 01:59 to 03:00
   * and the result of this method would have been 02:30, then the result
   * will be adjusted to 03:30.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusDays(6);
   * DateTime added = dt.plus(Period.days(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.days(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param days  the amount of days to add, may be negative
   * @return the new datetime plus the increased days
   * @since 1.1
   */
  def plusDays(days: Int): DateTime = {
    if (days == 0) {
      return this
    }
    val instant: Long = getChronology.days.add(getMillis, days)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of hours.
   * <p>
   * The calculation will add a duration equivalent to the number of hours
   * expressed in milliseconds.
   * <p>
   * For example, if a spring daylight savings cutover is from 01:59 to 03:00
   * then adding one hour to 01:30 will result in 03:30. This is a duration
   * of one hour later, even though the hour field value changed from 1 to 3.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusHours(6);
   * DateTime added = dt.plus(Period.hours(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.hours(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to add, may be negative
   * @return the new datetime plus the increased hours
   * @since 1.1
   */
  def plusHours(hours: Int): DateTime = {
    if (hours == 0) {
      return this
    }
    val instant: Long = getChronology.hours.add(getMillis, hours)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of minutes.
   * <p>
   * The calculation will add a duration equivalent to the number of minutes
   * expressed in milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusMinutes(6);
   * DateTime added = dt.plus(Period.minutes(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.minutes(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to add, may be negative
   * @return the new datetime plus the increased minutes
   * @since 1.1
   */
  def plusMinutes(minutes: Int): DateTime = {
    if (minutes == 0) {
      return this
    }
    val instant: Long = getChronology.minutes.add(getMillis, minutes)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of seconds.
   * <p>
   * The calculation will add a duration equivalent to the number of seconds
   * expressed in milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusSeconds(6);
   * DateTime added = dt.plus(Period.seconds(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.seconds(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param seconds  the amount of seconds to add, may be negative
   * @return the new datetime plus the increased seconds
   * @since 1.1
   */
  def plusSeconds(seconds: Int): DateTime = {
    if (seconds == 0) {
      return this
    }
    val instant: Long = getChronology.seconds.add(getMillis, seconds)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime plus the specified number of millis.
   * <p>
   * The calculation will add a duration equivalent to the number of milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime added = dt.plusMillis(6);
   * DateTime added = dt.plus(Period.millis(6));
   * DateTime added = dt.withFieldAdded(DurationFieldType.millis(), 6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param millis  the amount of millis to add, may be negative
   * @return the new datetime plus the increased millis
   * @since 1.1
   */
  def plusMillis(millis: Int): DateTime = {
    if (millis == 0) {
      return this
    }
    val instant: Long = getChronology.millis.add(getMillis, millis)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime with the specified duration taken away.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param duration  the duration, in millis, to reduce this instant by
   * @return a copy of this datetime with the duration taken away
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def minus(duration: Long): DateTime = {
    return withDurationAdded(duration, -1)
  }

  /**
   * Returns a copy of this datetime with the specified duration taken away.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param duration  the duration to reduce this instant by
   * @return a copy of this datetime with the duration taken away
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def minus(duration: ReadableDuration): DateTime = {
    return withDurationAdded(duration, -1)
  }

  /**
   * Returns a copy of this datetime with the specified period taken away.
   * <p>
   * This method will subtract each element of the period one by one, from
   * largest to smallest, adjusting the datetime to be accurate between each.
   * <p>
   * Thus, subtracting a period of one month and one day from 2007-05-31 will
   * work as follows:
   * First subtract one month and adjust, resulting in 2007-04-30
   * Then subtract one day and adjust, resulting in 2007-04-29.
   * Note that the day has been adjusted by two.
   * <p>
   * This method is typically used to subtract complex period instances.
   * Subtracting one field is best achieved using methods
   * like {@link #minusYears(int)}.
   * <p>
   * If the amount is zero or null, then <code>this</code> is returned.
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param period  the period to reduce this instant by
   * @return a copy of this datetime with the period taken away
   * @throws ArithmeticException if the new datetime exceeds the capacity of a long
   */
  def minus(period: ReadablePeriod): DateTime = {
    return withPeriodAdded(period, -1)
  }

  /**
   * Returns a copy of this datetime minus the specified number of years.
   * <p>
   * The calculation will do its best to only change the year field
   * retaining the same month of year.
   * However, in certain circumstances, it may be necessary to alter
   * smaller fields. For example, 2008-02-29 minus one year cannot result
   * in 2007-02-29, so the day of month is adjusted to 2007-02-28.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusYears(6);
   * DateTime subtracted = dt.minus(Period.years(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.years(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param years  the amount of years to subtract, may be negative
   * @return the new datetime minus the increased years
   * @since 1.1
   */
  def minusYears(years: Int): DateTime = {
    if (years == 0) {
      return this
    }
    val instant: Long = getChronology.years.subtract(getMillis, years)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of months.
   * <p>
   * The calculation will do its best to only change the month field
   * retaining the same day of month.
   * However, in certain circumstances, it may be necessary to alter
   * smaller fields. For example, 2007-05-31 minus one month cannot result
   * in 2007-04-31, so the day of month is adjusted to 2007-04-30.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusMonths(6);
   * DateTime subtracted = dt.minus(Period.months(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.months(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param months  the amount of months to subtract, may be negative
   * @return the new datetime minus the increased months
   * @since 1.1
   */
  def minusMonths(months: Int): DateTime = {
    if (months == 0) {
      return this
    }
    val instant: Long = getChronology.months.subtract(getMillis, months)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of weeks.
   * <p>
   * The calculation operates as if it were subtracting the equivalent in days.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusWeeks(6);
   * DateTime subtracted = dt.minus(Period.weeks(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.weeks(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param weeks  the amount of weeks to subtract, may be negative
   * @return the new datetime minus the increased weeks
   * @since 1.1
   */
  def minusWeeks(weeks: Int): DateTime = {
    if (weeks == 0) {
      return this
    }
    val instant: Long = getChronology.weeks.subtract(getMillis, weeks)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of days.
   * <p>
   * The calculation will do its best to only change the day field
   * retaining the same time of day.
   * However, in certain circumstances, typically daylight savings cutover,
   * it may be necessary to alter the time fields.
   * <p>
   * In spring an hour is typically removed. If subtracting one day results
   * in the time being within the cutover then the time is adjusted to be
   * within summer time. For example, if the cutover is from 01:59 to 03:00
   * and the result of this method would have been 02:30, then the result
   * will be adjusted to 03:30.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusDays(6);
   * DateTime subtracted = dt.minus(Period.days(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.days(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param days  the amount of days to subtract, may be negative
   * @return the new datetime minus the increased days
   * @since 1.1
   */
  def minusDays(days: Int): DateTime = {
    if (days == 0) {
      return this
    }
    val instant: Long = getChronology.days.subtract(getMillis, days)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of hours.
   * <p>
   * The calculation will subtract a duration equivalent to the number of
   * hours expressed in milliseconds.
   * <p>
   * For example, if a spring daylight savings cutover is from 01:59 to 03:00
   * then subtracting one hour from 03:30 will result in 01:30. This is a
   * duration of one hour earlier, even though the hour field value changed
   * from 3 to 1.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusHours(6);
   * DateTime subtracted = dt.minus(Period.hours(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.hours(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to subtract, may be negative
   * @return the new datetime minus the increased hours
   * @since 1.1
   */
  def minusHours(hours: Int): DateTime = {
    if (hours == 0) {
      return this
    }
    val instant: Long = getChronology.hours.subtract(getMillis, hours)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of minutes.
   * <p>
   * The calculation will subtract a duration equivalent to the number of
   * minutes expressed in milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusMinutes(6);
   * DateTime subtracted = dt.minus(Period.minutes(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.minutes(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to subtract, may be negative
   * @return the new datetime minus the increased minutes
   * @since 1.1
   */
  def minusMinutes(minutes: Int): DateTime = {
    if (minutes == 0) {
      return this
    }
    val instant: Long = getChronology.minutes.subtract(getMillis, minutes)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of seconds.
   * <p>
   * The calculation will subtract a duration equivalent to the number of
   * seconds expressed in milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusSeconds(6);
   * DateTime subtracted = dt.minus(Period.seconds(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.seconds(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param seconds  the amount of seconds to subtract, may be negative
   * @return the new datetime minus the increased seconds
   * @since 1.1
   */
  def minusSeconds(seconds: Int): DateTime = {
    if (seconds == 0) {
      return this
    }
    val instant: Long = getChronology.seconds.subtract(getMillis, seconds)
    return withMillis(instant)
  }

  /**
   * Returns a copy of this datetime minus the specified number of millis.
   * <p>
   * The calculation will subtract a duration equivalent to the number of
   * milliseconds.
   * <p>
   * The following three lines are identical in effect:
   * <pre>
   * DateTime subtracted = dt.minusMillis(6);
   * DateTime subtracted = dt.minus(Period.millis(6));
   * DateTime subtracted = dt.withFieldAdded(DurationFieldType.millis(), -6);
   * </pre>
   * <p>
   * This datetime instance is immutable and unaffected by this method call.
   *
   * @param millis  the amount of millis to subtract, may be negative
   * @return the new datetime minus the increased millis
   * @since 1.1
   */
  def minusMillis(millis: Int): DateTime = {
    if (millis == 0) {
      return this
    }
    val instant: Long = getChronology.millis.subtract(getMillis, millis)
    return withMillis(instant)
  }

  /**
   * Gets the property object for the specified type, which contains many useful methods.
   *
   * @param type  the field type to get the chronology for
   * @return the property object
   * @throws IllegalArgumentException if the field is null or unsupported
   */
  def property(`type`: DateTimeFieldType): DateTime.Property = {
    if (`type` == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null")
    }
    val field: DateTimeField = `type`.getField(getChronology)
    if (field.isSupported == false) {
      throw new IllegalArgumentException("Field '" + `type` + "' is not supported")
    }
    return new DateTime.Property(this, field)
  }

  /**
   * Converts this object to a <code>DateMidnight</code> using the
   * same millis and chronology.
   *
   * @return a DateMidnight using the same millis and chronology
   * @deprecated DateMidnight is deprecated
   */
  @deprecated def toDateMidnight: DateMidnight = {
    return new DateMidnight(getMillis, getChronology)
  }

  /**
   * Converts this object to a <code>YearMonthDay</code> using the
   * same millis and chronology.
   *
   * @return a YearMonthDay using the same millis and chronology
   * @deprecated Use LocalDate instead of YearMonthDay
   */
  @deprecated def toYearMonthDay: YearMonthDay = {
    return new YearMonthDay(getMillis, getChronology)
  }

  /**
   * Converts this object to a <code>TimeOfDay</code> using the
   * same millis and chronology.
   *
   * @return a TimeOfDay using the same millis and chronology
   * @deprecated Use LocalTime instead of TimeOfDay
   */
  @deprecated def toTimeOfDay: TimeOfDay = {
    return new TimeOfDay(getMillis, getChronology)
  }

  /**
   * Converts this object to a <code>LocalDateTime</code> with
   * the same datetime and chronology.
   *
   * @return a LocalDateTime with the same datetime and chronology
   * @since 1.3
   */
  def toLocalDateTime: LocalDateTime = {
    return new LocalDateTime(getMillis, getChronology)
  }

  /**
   * Converts this object to a <code>LocalDate</code> with the
   * same date and chronology.
   *
   * @return a LocalDate with the same date and chronology
   * @since 1.3
   */
  def toLocalDate: LocalDate = {
    return new LocalDate(getMillis, getChronology)
  }

  /**
   * Converts this object to a <code>LocalTime</code> with the
   * same time and chronology.
   *
   * @return a LocalTime with the same time and chronology
   * @since 1.3
   */
  def toLocalTime: LocalTime = {
    return new LocalTime(getMillis, getChronology)
  }

  /**
   * Returns a copy of this datetime with the era field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * era changed.
   *
   * @param era  the era to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withEra(era: Int): DateTime = {
    return withMillis(getChronology.era.set(getMillis, era))
  }

  /**
   * Returns a copy of this datetime with the century of era field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * century of era changed.
   *
   * @param centuryOfEra  the centurey of era to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withCenturyOfEra(centuryOfEra: Int): DateTime = {
    return withMillis(getChronology.centuryOfEra.set(getMillis, centuryOfEra))
  }

  /**
   * Returns a copy of this datetime with the year of era field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * year of era changed.
   *
   * @param yearOfEra  the year of era to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withYearOfEra(yearOfEra: Int): DateTime = {
    return withMillis(getChronology.yearOfEra.set(getMillis, yearOfEra))
  }

  /**
   * Returns a copy of this datetime with the year of century field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * year of century changed.
   *
   * @param yearOfCentury  the year of century to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withYearOfCentury(yearOfCentury: Int): DateTime = {
    return withMillis(getChronology.yearOfCentury.set(getMillis, yearOfCentury))
  }

  /**
   * Returns a copy of this datetime with the year field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * year changed.
   *
   * @param year  the year to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withYear(year: Int): DateTime = {
    return withMillis(getChronology.year.set(getMillis, year))
  }

  /**
   * Returns a copy of this datetime with the weekyear field updated.
   * <p>
   * The weekyear is the year that matches with the weekOfWeekyear field.
   * In the standard ISO8601 week algorithm, the first week of the year
   * is that in which at least 4 days are in the year. As a result of this
   * definition, day 1 of the first week may be in the previous year.
   * The weekyear allows you to query the effective year for that day.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * weekyear changed.
   *
   * @param weekyear  the weekyear to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withWeekyear(weekyear: Int): DateTime = {
    return withMillis(getChronology.weekyear.set(getMillis, weekyear))
  }

  /**
   * Returns a copy of this datetime with the month of year field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * month of year changed.
   *
   * @param monthOfYear  the month of year to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withMonthOfYear(monthOfYear: Int): DateTime = {
    return withMillis(getChronology.monthOfYear.set(getMillis, monthOfYear))
  }

  /**
   * Returns a copy of this datetime with the week of weekyear field updated.
   * <p>
   * This field is associated with the "weekyear" via {@link #withWeekyear(int)}.
   * In the standard ISO8601 week algorithm, the first week of the year
   * is that in which at least 4 days are in the year. As a result of this
   * definition, day 1 of the first week may be in the previous year.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * week of weekyear changed.
   *
   * @param weekOfWeekyear  the week of weekyear to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withWeekOfWeekyear(weekOfWeekyear: Int): DateTime = {
    return withMillis(getChronology.weekOfWeekyear.set(getMillis, weekOfWeekyear))
  }

  /**
   * Returns a copy of this datetime with the day of year field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * day of year changed.
   *
   * @param dayOfYear  the day of year to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withDayOfYear(dayOfYear: Int): DateTime = {
    return withMillis(getChronology.dayOfYear.set(getMillis, dayOfYear))
  }

  /**
   * Returns a copy of this datetime with the day of month field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * day of month changed.
   *
   * @param dayOfMonth  the day of month to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withDayOfMonth(dayOfMonth: Int): DateTime = {
    return withMillis(getChronology.dayOfMonth.set(getMillis, dayOfMonth))
  }

  /**
   * Returns a copy of this datetime with the day of week field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * day of week changed.
   *
   * @param dayOfWeek  the day of week to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withDayOfWeek(dayOfWeek: Int): DateTime = {
    return withMillis(getChronology.dayOfWeek.set(getMillis, dayOfWeek))
  }

  /**
   * Returns a copy of this datetime with the hour of day field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * hour of day changed.
   *
   * @param hour  the hour of day to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withHourOfDay(hour: Int): DateTime = {
    return withMillis(getChronology.hourOfDay.set(getMillis, hour))
  }

  /**
   * Returns a copy of this datetime with the minute of hour updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * minute of hour changed.
   *
   * @param minute  the minute of hour to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withMinuteOfHour(minute: Int): DateTime = {
    return withMillis(getChronology.minuteOfHour.set(getMillis, minute))
  }

  /**
   * Returns a copy of this datetime with the second of minute field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * second of minute changed.
   *
   * @param second  the second of minute to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withSecondOfMinute(second: Int): DateTime = {
    return withMillis(getChronology.secondOfMinute.set(getMillis, second))
  }

  /**
   * Returns a copy of this datetime with the millis of second field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * millis of second changed.
   *
   * @param millis  the millis of second to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withMillisOfSecond(millis: Int): DateTime = {
    return withMillis(getChronology.millisOfSecond.set(getMillis, millis))
  }

  /**
   * Returns a copy of this datetime with the millis of day field updated.
   * <p>
   * DateTime is immutable, so there are no set methods.
   * Instead, this method returns a new instance with the value of
   * millis of day changed.
   *
   * @param millis  the millis of day to set
   * @return a copy of this object with the field set
   * @throws IllegalArgumentException if the value is invalid
   * @since 1.3
   */
  def withMillisOfDay(millis: Int): DateTime = {
    return withMillis(getChronology.millisOfDay.set(getMillis, millis))
  }

  /**
   * Get the era property which provides access to advanced functionality.
   *
   * @return the era property
   */
  def era: DateTime.Property = {
    return new DateTime.Property(this, getChronology.era)
  }

  /**
   * Get the century of era property which provides access to advanced functionality.
   *
   * @return the year of era property
   */
  def centuryOfEra: DateTime.Property = {
    return new DateTime.Property(this, getChronology.centuryOfEra)
  }

  /**
   * Get the year of century property which provides access to advanced functionality.
   *
   * @return the year of era property
   */
  def yearOfCentury: DateTime.Property = {
    return new DateTime.Property(this, getChronology.yearOfCentury)
  }

  /**
   * Get the year of era property which provides access to advanced functionality.
   *
   * @return the year of era property
   */
  def yearOfEra: DateTime.Property = {
    return new DateTime.Property(this, getChronology.yearOfEra)
  }

  /**
   * Get the year property which provides access to advanced functionality.
   *
   * @return the year property
   */
  def year: DateTime.Property = {
    return new DateTime.Property(this, getChronology.year)
  }

  /**
   * Get the year of a week based year property which provides access to advanced functionality.
   *
   * @return the year of a week based year property
   */
  def weekyear: DateTime.Property = {
    return new DateTime.Property(this, getChronology.weekyear)
  }

  /**
   * Get the month of year property which provides access to advanced functionality.
   *
   * @return the month of year property
   */
  def monthOfYear: DateTime.Property = {
    return new DateTime.Property(this, getChronology.monthOfYear)
  }

  /**
   * Get the week of a week based year property which provides access to advanced functionality.
   *
   * @return the week of a week based year property
   */
  def weekOfWeekyear: DateTime.Property = {
    return new DateTime.Property(this, getChronology.weekOfWeekyear)
  }

  /**
   * Get the day of year property which provides access to advanced functionality.
   *
   * @return the day of year property
   */
  def dayOfYear: DateTime.Property = {
    return new DateTime.Property(this, getChronology.dayOfYear)
  }

  /**
   * Get the day of month property which provides access to advanced functionality.
   *
   * @return the day of month property
   */
  def dayOfMonth: DateTime.Property = {
    return new DateTime.Property(this, getChronology.dayOfMonth)
  }

  /**
   * Get the day of week property which provides access to advanced functionality.
   *
   * @return the day of week property
   */
  def dayOfWeek: DateTime.Property = {
    return new DateTime.Property(this, getChronology.dayOfWeek)
  }

  /**
   * Get the hour of day field property which provides access to advanced functionality.
   *
   * @return the hour of day property
   */
  def hourOfDay: DateTime.Property = {
    return new DateTime.Property(this, getChronology.hourOfDay)
  }

  /**
   * Get the minute of day property which provides access to advanced functionality.
   *
   * @return the minute of day property
   */
  def minuteOfDay: DateTime.Property = {
    return new DateTime.Property(this, getChronology.minuteOfDay)
  }

  /**
   * Get the minute of hour field property which provides access to advanced functionality.
   *
   * @return the minute of hour property
   */
  def minuteOfHour: DateTime.Property = {
    return new DateTime.Property(this, getChronology.minuteOfHour)
  }

  /**
   * Get the second of day property which provides access to advanced functionality.
   *
   * @return the second of day property
   */
  def secondOfDay: DateTime.Property = {
    return new DateTime.Property(this, getChronology.secondOfDay)
  }

  /**
   * Get the second of minute field property which provides access to advanced functionality.
   *
   * @return the second of minute property
   */
  def secondOfMinute: DateTime.Property = {
    return new DateTime.Property(this, getChronology.secondOfMinute)
  }

  /**
   * Get the millis of day property which provides access to advanced functionality.
   *
   * @return the millis of day property
   */
  def millisOfDay: DateTime.Property = {
    return new DateTime.Property(this, getChronology.millisOfDay)
  }

  /**
   * Get the millis of second property which provides access to advanced functionality.
   *
   * @return the millis of second property
   */
  def millisOfSecond: DateTime.Property = {
    return new DateTime.Property(this, getChronology.millisOfSecond)
  }
}