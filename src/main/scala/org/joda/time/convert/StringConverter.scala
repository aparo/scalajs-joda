/*
 *  Copyright 2001-2009 Stephen Colebourne
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
package org.joda.time.convert

import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.ReadWritableInterval
import org.joda.time.ReadWritablePeriod
import org.joda.time.ReadablePartial
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter

/**
 * StringConverter converts from a String to an instant, partial,
 * duration, period or interval..
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object StringConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: StringConverter = new StringConverter
}

class StringConverter extends AbstractConverter with InstantConverter with PartialConverter with DurationConverter with PeriodConverter with IntervalConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the millis, which is the ISO parsed string value.
   *
   * @param object  the String to convert, must not be null
   * @param chrono  the chronology to use, non-null result of getChronology
   * @return the millisecond value
   * @throws IllegalArgumentException if the value if invalid
   */
  override def getInstantMillis(`object`: AnyRef, chrono: Chronology): Long = {
    val str: String = `object`.asInstanceOf[String]
    val p: DateTimeFormatter = ISODateTimeFormat.dateTimeParser
    return p.withChronology(chrono).parseMillis(str)
  }

  /**
   * Extracts the values of the partial from an object of this converter's type.
   * This method checks if the parser has a zone, and uses it if present.
   * This is most useful for parsing local times with UTC.
   *
   * @param fieldSource  a partial that provides access to the fields.
   *                     This partial may be incomplete and only getFieldType(int) should be used
   * @param object  the object to convert
   * @param chrono  the chronology to use, which is the non-null result of getChronology()
   * @param parser the parser to use, may be null
   * @return the array of field values that match the fieldSource, must be non-null valid
   * @throws ClassCastException if the object is invalid
   * @throws IllegalArgumentException if the value if invalid
   * @since 1.3
   */
  override def getPartialValues(fieldSource: ReadablePartial, `object`: AnyRef, chrono: Chronology, parser: DateTimeFormatter): Array[Int] = {
    if (parser.getZone != null) {
      chrono = chrono.withZone(parser.getZone)
    }
    val millis: Long = parser.withChronology(chrono).parseMillis(`object`.asInstanceOf[String])
    return chrono.get(fieldSource, millis)
  }

  /**
   * Gets the duration of the string using the standard type.
   * This matches the toString() method of ReadableDuration.
   *
   * @param object  the String to convert, must not be null
   * @throws ClassCastException if the object is invalid
   */
  def getDurationMillis(`object`: AnyRef): Long = {
    val original: String = `object`.asInstanceOf[String]
    var str: String = original
    val len: Int = str.length
    if (len >= 4 && (str.charAt(0) == 'P' || str.charAt(0) == 'p') && (str.charAt(1) == 'T' || str.charAt(1) == 't') && (str.charAt(len - 1) == 'S' || str.charAt(len - 1) == 's')) {
    }
    else {
      throw new IllegalArgumentException("Invalid format: \"" + original + '"')
    }
    str = str.substring(2, len - 1)
    var dot: Int = -1
    var negative: Boolean = false
    {
      var i: Int = 0
      while (i < str.length) {
        {
          if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
          }
          else if (i == 0 && str.charAt(0) == '-') {
            negative = true
          }
          else if (i > (if (negative) 1 else 0) && str.charAt(i) == '.' && dot == -1) {
            dot = i
          }
          else {
            throw new IllegalArgumentException("Invalid format: \"" + original + '"')
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    var millis: Long = 0
    var seconds: Long = 0
    val firstDigit: Int = if (negative) 1 else 0
    if (dot > 0) {
      seconds = Long.parseLong(str.substring(firstDigit, dot))
      str = str.substring(dot + 1)
      if (str.length != 3) {
        str = (str + "000").substring(0, 3)
      }
      millis = str.toInt
    }
    else if (negative) {
      seconds = Long.parseLong(str.substring(firstDigit, str.length))
    }
    else {
      seconds = Long.parseLong(str)
    }
    if (negative) {
      return FieldUtils.safeAdd(FieldUtils.safeMultiply(-seconds, 1000), -millis)
    }
    else {
      return FieldUtils.safeAdd(FieldUtils.safeMultiply(seconds, 1000), millis)
    }
  }

  /**
   * Extracts duration values from an object of this converter's type, and
   * sets them into the given ReadWritableDuration.
   *
   * @param period  period to get modified
   * @param object  the String to convert, must not be null
   * @param chrono  the chronology to use
   * @return the millisecond duration
   * @throws ClassCastException if the object is invalid
   */
  def setInto(period: ReadWritablePeriod, `object`: AnyRef, chrono: Chronology) {
    val str: String = `object`.asInstanceOf[String]
    val parser: PeriodFormatter = ISOPeriodFormat.standard
    period.clear
    val pos: Int = parser.parseInto(period, str, 0)
    if (pos < str.length) {
      if (pos < 0) {
        parser.withParseType(period.getPeriodType).parseMutablePeriod(str)
      }
      throw new IllegalArgumentException("Invalid format: \"" + str + '"')
    }
  }

  /**
   * Sets the value of the mutable interval from the string.
   *
   * @param writableInterval  the interval to set
   * @param object  the String to convert, must not be null
   * @param chrono  the chronology to use, may be null
   */
  def setInto(writableInterval: ReadWritableInterval, `object`: AnyRef, chrono: Chronology) {
    val str: String = `object`.asInstanceOf[String]
    val separator: Int = str.indexOf('/')
    if (separator < 0) {
      throw new IllegalArgumentException("Format requires a '/' separator: " + str)
    }
    val leftStr: String = str.substring(0, separator)
    if (leftStr.length <= 0) {
      throw new IllegalArgumentException("Format invalid: " + str)
    }
    val rightStr: String = str.substring(separator + 1)
    if (rightStr.length <= 0) {
      throw new IllegalArgumentException("Format invalid: " + str)
    }
    var dateTimeParser: DateTimeFormatter = ISODateTimeFormat.dateTimeParser
    dateTimeParser = dateTimeParser.withChronology(chrono)
    val periodParser: PeriodFormatter = ISOPeriodFormat.standard
    var startInstant: Long = 0
    var endInstant: Long = 0
    var period: Period = null
    var parsedChrono: Chronology = null
    var c: Char = leftStr.charAt(0)
    if (c == 'P' || c == 'p') {
      period = periodParser.withParseType(getPeriodType(leftStr)).parsePeriod(leftStr)
    }
    else {
      val start: DateTime = dateTimeParser.parseDateTime(leftStr)
      startInstant = start.getMillis
      parsedChrono = start.getChronology
    }
    c = rightStr.charAt(0)
    if (c == 'P' || c == 'p') {
      if (period != null) {
        throw new IllegalArgumentException("Interval composed of two durations: " + str)
      }
      period = periodParser.withParseType(getPeriodType(rightStr)).parsePeriod(rightStr)
      chrono = (if (chrono != null) chrono else parsedChrono)
      endInstant = chrono.add(period, startInstant, 1)
    }
    else {
      val end: DateTime = dateTimeParser.parseDateTime(rightStr)
      endInstant = end.getMillis
      parsedChrono = (if (parsedChrono != null) parsedChrono else end.getChronology)
      chrono = (if (chrono != null) chrono else parsedChrono)
      if (period != null) {
        startInstant = chrono.add(period, endInstant, -1)
      }
    }
    writableInterval.setInterval(startInstant, endInstant)
    writableInterval.setChronology(chrono)
  }

  /**
   * Returns String.class.
   *
   * @return String.class
   */
  def getSupportedType: Class[_] = {
    return classOf[String]
  }
}