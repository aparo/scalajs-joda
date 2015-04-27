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
 * Implements the Ethiopic calendar system, which defines every fourth year as
 * leap, much like the Julian calendar. The year is broken down into 12 months,
 * each 30 days in length. An extra period at the end of the year is either 5
 * or 6 days in length. In this implementation, it is considered a 13th month.
 * <p>
 * Year 1 in the Ethiopic calendar began on August 29, 8 CE (Julian), thus
 * Ethiopic years do not begin at the same time as Julian years. This chronology
 * is not proleptic, as it does not allow dates before the first Ethiopic year.
 * <p>
 * This implementation defines a day as midnight to midnight exactly as per
 * the ISO chronology. Some references indicate that a coptic day starts at
 * sunset on the previous ISO day, but this has not been confirmed and is not
 * implemented.
 * <p>
 * EthiopicChronology is thread-safe and immutable.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Ethiopian_calendar">Wikipedia</a>
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.2
 */
@SerialVersionUID(-5972804258688333942L)
object EthiopicChronology {
  /**
   * Constant value for 'Ethiopean Era', equivalent
   * to the value returned for AD/CE.
   */
  val EE: Int = DateTimeConstants.CE
  /** A singleton era field. */
  private val ERA_FIELD: DateTimeField = new BasicSingleEraDateTimeField("EE")
  /** The lowest year that can be fully supported. */
  private val MIN_YEAR: Int = -292269337
  /** The highest year that can be fully supported. */
  private val MAX_YEAR: Int = 292272984
  /** Cache of zone to chronology arrays */
  private val cCache: ConcurrentHashMap[DateTimeZone, Array[EthiopicChronology]] = new ConcurrentHashMap[DateTimeZone, Array[EthiopicChronology]]
  /** Singleton instance of a UTC EthiopicChronology */
  private val INSTANCE_UTC: EthiopicChronology = null

  /**
   * Gets an instance of the EthiopicChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: EthiopicChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the EthiopicChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: EthiopicChronology = {
    return getInstance(DateTimeZone.getDefault, 4)
  }

  /**
   * Gets an instance of the EthiopicChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): EthiopicChronology = {
    return getInstance(zone, 4)
  }

  /**
   * Gets an instance of the EthiopicChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone, minDaysInFirstWeek: Int): EthiopicChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: EthiopicChronology = null
    var chronos: Array[EthiopicChronology] = cCache.get(zone)
    if (chronos == null) {
      chronos = new Array[EthiopicChronology](7)
      val oldChronos: Array[EthiopicChronology] = cCache.putIfAbsent(zone, chronos)
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
            chrono = new EthiopicChronology(null, null, minDaysInFirstWeek)
            val lowerLimit: DateTime = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono)
            chrono = new EthiopicChronology(LimitChronology.getInstance(chrono, lowerLimit, null), null, minDaysInFirstWeek)
          }
          else {
            chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek)
            chrono = new EthiopicChronology(ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek)
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
final class EthiopicChronology extends BasicFixedMonthChronology {
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
    return if (base == null) EthiopicChronology.getInstance(DateTimeZone.UTC, getMinimumDaysInFirstWeek) else EthiopicChronology.getInstance(base.getZone, getMinimumDaysInFirstWeek)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return EthiopicChronology.INSTANCE_UTC
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
    return EthiopicChronology.getInstance(zone)
  }

  private[chrono] override def isLeapDay(instant: Long): Boolean = {
    return dayOfMonth.get(instant) == 6 && monthOfYear.isLeap(instant)
  }

  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long = {
    val relativeYear: Int = year - 1963
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
    return EthiopicChronology.MIN_YEAR
  }

  private[chrono] def getMaxYear: Int = {
    return EthiopicChronology.MAX_YEAR
  }

  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long = {
    return (1962L * MILLIS_PER_YEAR + 112L * DateTimeConstants.MILLIS_PER_DAY) / 2
  }

  protected override def assemble(fields: AssembledChronology.Fields) {
    if (getBase == null) {
      super.assemble(fields)
      fields.year = new SkipDateTimeField(this, fields.year)
      fields.weekyear = new SkipDateTimeField(this, fields.weekyear)
      fields.era = EthiopicChronology.ERA_FIELD
      fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 13)
      fields.months = fields.monthOfYear.getDurationField
    }
  }
}