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
import org.joda.time.DateTimeZone

/**
 * Implements a pure proleptic Gregorian calendar system, which defines every
 * fourth year as leap, unless the year is divisible by 100 and not by 400.
 * This improves upon the Julian calendar leap year rule.
 * <p>
 * Although the Gregorian calendar did not exist before 1582 CE, this
 * chronology assumes it did, thus it is proleptic. This implementation also
 * fixes the start of the year at January 1, and defines the year zero.
 * <p>
 * GregorianChronology is thread-safe and immutable.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Gregorian_calendar">Wikipedia</a>
 * @see JulianChronology
 * @see GJChronology
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-861407383323710522L)
object GregorianChronology {
  private val MILLIS_PER_YEAR: Long = (365.2425 * DateTimeConstants.MILLIS_PER_DAY).toLong
  private val MILLIS_PER_MONTH: Long = (365.2425 * DateTimeConstants.MILLIS_PER_DAY / 12).toLong
  private val DAYS_0000_TO_1970: Int = 719527
  /** The lowest year that can be fully supported. */
  private val MIN_YEAR: Int = -292275054
  /** The highest year that can be fully supported. */
  private val MAX_YEAR: Int = 292278993
  /** Singleton instance of a UTC GregorianChronology */
  private val INSTANCE_UTC: GregorianChronology = null
  /** Cache of zone to chronology arrays */
  private val cCache: ConcurrentHashMap[DateTimeZone, Array[GregorianChronology]] = new ConcurrentHashMap[DateTimeZone, Array[GregorianChronology]]

  /**
   * Gets an instance of the GregorianChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: GregorianChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the GregorianChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: GregorianChronology = {
    return getInstance(DateTimeZone.getDefault, 4)
  }

  /**
   * Gets an instance of the GregorianChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): GregorianChronology = {
    return getInstance(zone, 4)
  }

  /**
   * Gets an instance of the GregorianChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone, minDaysInFirstWeek: Int): GregorianChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: GregorianChronology = null
    var chronos: Array[GregorianChronology] = cCache.get(zone)
    if (chronos == null) {
      chronos = new Array[GregorianChronology](7)
      val oldChronos: Array[GregorianChronology] = cCache.putIfAbsent(zone, chronos)
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
            chrono = new GregorianChronology(null, null, minDaysInFirstWeek)
          }
          else {
            chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek)
            chrono = new GregorianChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek)
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

@SerialVersionUID(-861407383323710522L)
final class GregorianChronology extends BasicGJChronology {
  /**
   * Restricted constructor
   */
  private def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
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
    return if (base == null) GregorianChronology.getInstance(DateTimeZone.UTC, minDays) else GregorianChronology.getInstance(base.getZone, minDays)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return GregorianChronology.INSTANCE_UTC
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
    return GregorianChronology.getInstance(zone)
  }

  protected override def assemble(fields: AssembledChronology.Fields) {
    if (getBase == null) {
      super.assemble(fields)
    }
  }

  private[chrono] def isLeapYear(year: Int): Boolean = {
    return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0)
  }

  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long = {
    var leapYears: Int = year / 100
    if (year < 0) {
      leapYears = ((year + 3) >> 2) - leapYears + ((leapYears + 3) >> 2) - 1
    }
    else {
      leapYears = (year >> 2) - leapYears + (leapYears >> 2)
      if (isLeapYear(year)) {
        leapYears -= 1
      }
    }
    return (year * 365L + (leapYears - GregorianChronology.DAYS_0000_TO_1970)) * DateTimeConstants.MILLIS_PER_DAY
  }

  private[chrono] def getMinYear: Int = {
    return GregorianChronology.MIN_YEAR
  }

  private[chrono] def getMaxYear: Int = {
    return GregorianChronology.MAX_YEAR
  }

  private[chrono] def getAverageMillisPerYear: Long = {
    return GregorianChronology.MILLIS_PER_YEAR
  }

  private[chrono] def getAverageMillisPerYearDividedByTwo: Long = {
    return GregorianChronology.MILLIS_PER_YEAR / 2
  }

  private[chrono] def getAverageMillisPerMonth: Long = {
    return GregorianChronology.MILLIS_PER_MONTH
  }

  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long = {
    return (1970L * GregorianChronology.MILLIS_PER_YEAR) / 2
  }
}