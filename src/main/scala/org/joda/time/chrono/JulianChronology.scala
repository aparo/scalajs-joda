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
package org.joda.time.chrono

import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.IllegalFieldValueException
import org.joda.time.field.SkipDateTimeField

/**
 * Implements a pure proleptic Julian calendar system, which defines every
 * fourth year as leap. This implementation follows the leap year rule
 * strictly, even for dates before 8 CE, where leap years were actually
 * irregular. In the Julian calendar, year zero does not exist: 1 BCE is
 * followed by 1 CE.
 * <p>
 * Although the Julian calendar did not exist before 45 BCE, this chronology
 * assumes it did, thus it is proleptic. This implementation also fixes the
 * start of the year at January 1.
 * <p>
 * JulianChronology is thread-safe and immutable.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Julian_calendar">Wikipedia</a>
 * @see GregorianChronology
 * @see GJChronology
 *
 * @author Guy Allard
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-8731039522547897247L)
object JulianChronology {
  private val MILLIS_PER_YEAR: Long = (365.25 * DateTimeConstants.MILLIS_PER_DAY).toLong
  private val MILLIS_PER_MONTH: Long = (365.25 * DateTimeConstants.MILLIS_PER_DAY / 12).toLong
  /** The lowest year that can be fully supported. */
  private val MIN_YEAR: Int = -292269054
  /** The highest year that can be fully supported. */
  private val MAX_YEAR: Int = 292272992
  /** Singleton instance of a UTC JulianChronology */
  private val INSTANCE_UTC: JulianChronology = null
  /** Cache of zone to chronology arrays */
  private val cCache: ConcurrentHashMap[DateTimeZone, Array[JulianChronology]] = new ConcurrentHashMap[DateTimeZone, Array[JulianChronology]]

  private[chrono] def adjustYearForSet(year: Int): Int = {
    if (year <= 0) {
      if (year == 0) {
        throw new IllegalFieldValueException(DateTimeFieldType.year, Integer.valueOf(year), null, null)
      }
      year += 1
    }
    return year
  }

  /**
   * Gets an instance of the JulianChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: JulianChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the JulianChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: JulianChronology = {
    return getInstance(DateTimeZone.getDefault, 4)
  }

  /**
   * Gets an instance of the JulianChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): JulianChronology = {
    return getInstance(zone, 4)
  }

  /**
   * Gets an instance of the JulianChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone, minDaysInFirstWeek: Int): JulianChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: JulianChronology = null
    var chronos: Array[JulianChronology] = cCache.get(zone)
    if (chronos == null) {
      chronos = new Array[JulianChronology](7)
      val oldChronos: Array[JulianChronology] = cCache.putIfAbsent(zone, chronos)
      if (oldChronos != null) {
        chronos = oldChronos
      }
    }
    try {
      chrono = chronos(minDaysInFirstWeek - 1)
    }
    catch {
      case e: ArrayIndexOutOfBoundsException => {
        throw new IllegalArgumentException("Invalid min days in first week: " + minDaysInFirstWeek)
      }
    }
    if (chrono == null) {
      chronos synchronized {
        chrono = chronos(minDaysInFirstWeek - 1)
        if (chrono == null) {
          if (zone eq DateTimeZone.UTC) {
            chrono = new JulianChronology(null, null, minDaysInFirstWeek)
          }
          else {
            chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek)
            chrono = new JulianChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek)
          }
          chronos(minDaysInFirstWeek - 1) = chrono
        }
      }
    }
    return chrono
  }

  try {
    INSTANCE_UTC = getInstance(DateTimeZone.UTC)
  }
}

@SerialVersionUID(-8731039522547897247L)
final class JulianChronology extends BasicGJChronology {
  /**
   * Restricted constructor
   */
  private[chrono] def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
    this()
    `super`(base, param, minDaysInFirstWeek)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    val base: Chronology = getBase
    var minDays: Int = getMinimumDaysInFirstWeek
    minDays = (if (minDays == 0) 4 else minDays)
    return if (base == null) JulianChronology.getInstance(DateTimeZone.UTC, minDays) else JulianChronology.getInstance(base.getZone, minDays)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return JulianChronology.INSTANCE_UTC
  }

  /**
   * Gets the Chronology in a specific time zone.
   *
   * @param zone  the zone to get the chronology in, null is default
   * @return the chronology
   */
  def withZone(zone: DateTimeZone): Chronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    if (zone eq getZone) {
      return this
    }
    return JulianChronology.getInstance(zone)
  }

  @throws(classOf[IllegalArgumentException])
  private[chrono] def getDateMidnightMillis(year: Int, monthOfYear: Int, dayOfMonth: Int): Long = {
    return super.getDateMidnightMillis(JulianChronology.adjustYearForSet(year), monthOfYear, dayOfMonth)
  }

  private[chrono] def isLeapYear(year: Int): Boolean = {
    return (year & 3) == 0
  }

  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long = {
    val relativeYear: Int = year - 1968
    var leapYears: Int = 0
    if (relativeYear <= 0) {
      leapYears = (relativeYear + 3) >> 2
    }
    else {
      leapYears = relativeYear >> 2
      if (!isLeapYear(year)) {
        leapYears += 1
      }
    }
    val millis: Long = (relativeYear * 365L + leapYears) * DateTimeConstants.MILLIS_PER_DAY.toLong
    return millis - (366L + 352) * DateTimeConstants.MILLIS_PER_DAY
  }

  private[chrono] def getMinYear: Int = {
    return JulianChronology.MIN_YEAR
  }

  private[chrono] def getMaxYear: Int = {
    return JulianChronology.MAX_YEAR
  }

  private[chrono] def getAverageMillisPerYear: Long = {
    return JulianChronology.MILLIS_PER_YEAR
  }

  private[chrono] def getAverageMillisPerYearDividedByTwo: Long = {
    return JulianChronology.MILLIS_PER_YEAR / 2
  }

  private[chrono] def getAverageMillisPerMonth: Long = {
    return JulianChronology.MILLIS_PER_MONTH
  }

  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long = {
    return (1969L * JulianChronology.MILLIS_PER_YEAR + 352L * DateTimeConstants.MILLIS_PER_DAY) / 2
  }

  protected override def assemble(fields: AssembledChronology.Fields) {
    if (getBase == null) {
      super.assemble(fields)
      fields.year = new SkipDateTimeField(this, fields.year)
      fields.weekyear = new SkipDateTimeField(this, fields.weekyear)
    }
  }
}