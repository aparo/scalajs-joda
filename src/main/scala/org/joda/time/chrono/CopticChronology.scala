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
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.field.SkipDateTimeField

/**
 * Implements the Coptic calendar system, which defines every fourth year as
 * leap, much like the Julian calendar. The year is broken down into 12 months,
 * each 30 days in length. An extra period at the end of the year is either 5
 * or 6 days in length. In this implementation, it is considered a 13th month.
 * <p>
 * Year 1 in the Coptic calendar began on August 29, 284 CE (Julian), thus
 * Coptic years do not begin at the same time as Julian years. This chronology
 * is not proleptic, as it does not allow dates before the first Coptic year.
 * <p>
 * This implementation defines a day as midnight to midnight exactly as per
 * the ISO chronology. Some references indicate that a coptic day starts at
 * sunset on the previous ISO day, but this has not been confirmed and is not
 * implemented.
 * <p>
 * CopticChronology is thread-safe and immutable.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Coptic_calendar">Wikipedia</a>
 * @see JulianChronology
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-5972804258688333942L)
object CopticChronology {
  /**
   * Constant value for 'Anno Martyrum' or 'Era of the Martyrs', equivalent
   * to the value returned for AD/CE.
   */
  val AM: Int = DateTimeConstants.CE
  /** A singleton era field. */
  private val ERA_FIELD: DateTimeField = new BasicSingleEraDateTimeField("AM")
  /** The lowest year that can be fully supported. */
  private val MIN_YEAR: Int = -292269337
  /** The highest year that can be fully supported. */
  private val MAX_YEAR: Int = 292272708
  /** Cache of zone to chronology arrays */
  private val cCache: ConcurrentHashMap[DateTimeZone, Array[CopticChronology]] = new ConcurrentHashMap[DateTimeZone, Array[CopticChronology]]
  /** Singleton instance of a UTC CopticChronology */
  private val INSTANCE_UTC: CopticChronology = null

  /**
   * Gets an instance of the CopticChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: CopticChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the CopticChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: CopticChronology = {
    return getInstance(DateTimeZone.getDefault, 4)
  }

  /**
   * Gets an instance of the CopticChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): CopticChronology = {
    return getInstance(zone, 4)
  }

  /**
   * Gets an instance of the CopticChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone, minDaysInFirstWeek: Int): CopticChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: CopticChronology = null
    var chronos: Array[CopticChronology] = cCache.get(zone)
    if (chronos == null) {
      chronos = new Array[CopticChronology](7)
      val oldChronos: Array[CopticChronology] = cCache.putIfAbsent(zone, chronos)
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
            chrono = new CopticChronology(null, null, minDaysInFirstWeek)
            val lowerLimit: DateTime = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono)
            chrono = new CopticChronology(LimitChronology.getInstance(chrono, lowerLimit, null), null, minDaysInFirstWeek)
          }
          else {
            chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek)
            chrono = new CopticChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek)
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

@SerialVersionUID(-5972804258688333942L)
final class CopticChronology extends BasicFixedMonthChronology {
  /**
   * Restricted constructor.
   */
  private[chrono] def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
    this()
    `super`(base, param, minDaysInFirstWeek)
  }

  /**
   * Serialization singleton.
   */
  private def readResolve: AnyRef = {
    val base: Chronology = getBase
    var minDays: Int = getMinimumDaysInFirstWeek
    minDays = (if (minDays == 0) 4 else minDays)
    return if (base == null) CopticChronology.getInstance(DateTimeZone.UTC, minDays) else CopticChronology.getInstance(base.getZone, minDays)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return CopticChronology.INSTANCE_UTC
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
    return CopticChronology.getInstance(zone)
  }

  private[chrono] override def isLeapDay(instant: Long): Boolean = {
    return dayOfMonth.get(instant) == 6 && monthOfYear.isLeap(instant)
  }

  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long = {
    val relativeYear: Int = year - 1687
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
    return millis + (365L - 112) * DateTimeConstants.MILLIS_PER_DAY
  }

  private[chrono] def getMinYear: Int = {
    return CopticChronology.MIN_YEAR
  }

  private[chrono] def getMaxYear: Int = {
    return CopticChronology.MAX_YEAR
  }

  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long = {
    return (1686L * MILLIS_PER_YEAR + 112L * DateTimeConstants.MILLIS_PER_DAY) / 2
  }

  protected override def assemble(fields: AssembledChronology.Fields) {
    if (getBase == null) {
      super.assemble(fields)
      fields.year = new SkipDateTimeField(this, fields.year)
      fields.weekyear = new SkipDateTimeField(this, fields.weekyear)
      fields.era = CopticChronology.ERA_FIELD
      fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 13)
      fields.months = fields.monthOfYear.getDurationField
    }
  }
}