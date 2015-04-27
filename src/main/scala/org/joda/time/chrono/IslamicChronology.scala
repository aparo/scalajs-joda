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

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone

/**
 * Implements the Islamic, or Hijri, calendar system using arithmetic rules.
 * <p>
 * This calendar is a lunar calendar with a shorter year than ISO.
 * Year 1 in the Islamic calendar began on July 16, 622 CE (Julian), thus
 * Islamic years do not begin at the same time as Julian years. This chronology
 * is not proleptic, as it does not allow dates before the first Islamic year.
 * <p>
 * There are two basic forms of the Islamic calendar, the tabular and the
 * observed. The observed form cannot easily be used by computers as it
 * relies on human observation of the new moon.
 * The tabular calendar, implemented here, is an arithmetical approximation
 * of the observed form that follows relatively simple rules.
 * <p>
 * The tabular form of the calendar defines 12 months of alternately
 * 30 and 29 days. The last month is extended to 30 days in a leap year.
 * Leap years occur according to a 30 year cycle. There are four recognised
 * patterns of leap years in the 30 year cycle:
 * <pre>
 * Years 2, 5, 7, 10, 13, 15, 18, 21, 24, 26 & 29 - 15-based, used by Microsoft
 * Years 2, 5, 7, 10, 13, 16, 18, 21, 24, 26 & 29 - 16-based, most commonly used
 * Years 2, 5, 8, 10, 13, 16, 19, 21, 24, 27 & 29 - Indian
 * Years 2, 5, 8, 11, 13, 16, 19, 21, 24, 27 & 30 - Habash al-Hasib
 * </pre>
 * You can select which pattern to use via the factory methods, or use the
 * default (16-based).
 * <p>
 * This implementation defines a day as midnight to midnight exactly as per
 * the ISO chronology. This correct start of day is at sunset on the previous
 * day, however this cannot readily be modelled and has been ignored.
 * <p>
 * IslamicChronology is thread-safe and immutable.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Islamic_calendar">Wikipedia</a>
 *
 * @author Stephen Colebourne
 * @since 1.2
 */
@SerialVersionUID(-3663823829888L)
object IslamicChronology {
  /**
   * Constant value for 'Anno Hegirae', equivalent
   * to the value returned for AD/CE.
   */
  val AH: Int = DateTimeConstants.CE
  /** A singleton era field. */
  private val ERA_FIELD: DateTimeField = new BasicSingleEraDateTimeField("AH")
  /** Leap year 15-based pattern. */
  val LEAP_YEAR_15_BASED: IslamicChronology.LeapYearPatternType = new IslamicChronology.LeapYearPatternType(0, 623158436)
  /** Leap year 16-based pattern. */
  val LEAP_YEAR_16_BASED: IslamicChronology.LeapYearPatternType = new IslamicChronology.LeapYearPatternType(1, 623191204)
  /** Leap year Indian pattern. */
  val LEAP_YEAR_INDIAN: IslamicChronology.LeapYearPatternType = new IslamicChronology.LeapYearPatternType(2, 690562340)
  /** Leap year Habash al-Hasib pattern. */
  val LEAP_YEAR_HABASH_AL_HASIB: IslamicChronology.LeapYearPatternType = new IslamicChronology.LeapYearPatternType(3, 153692453)
  /** The lowest year that can be fully supported. */
  private val MIN_YEAR: Int = -292269337
  /**
   * The highest year that can be fully supported.
   * Although calculateFirstDayOfYearMillis can go higher without
   * overflowing, the getYear method overflows when it adds the
   * approximate millis at the epoch.
   */
  private val MAX_YEAR: Int = 292271022
  /** The days in a pair of months. */
  private val MONTH_PAIR_LENGTH: Int = 59
  /** The length of the long month. */
  private val LONG_MONTH_LENGTH: Int = 30
  /** The length of the short month. */
  private val SHORT_MONTH_LENGTH: Int = 29
  /** The length of the long month in millis. */
  private val MILLIS_PER_MONTH_PAIR: Long = 59L * DateTimeConstants.MILLIS_PER_DAY
  /** The length of the long month in millis. */
  private val MILLIS_PER_MONTH: Long = (29.53056 * DateTimeConstants.MILLIS_PER_DAY).toLong
  /** The length of the long month in millis. */
  private val MILLIS_PER_LONG_MONTH: Long = 30L * DateTimeConstants.MILLIS_PER_DAY
  /** The typical millis per year. */
  private val MILLIS_PER_YEAR: Long = (354.36667 * DateTimeConstants.MILLIS_PER_DAY).toLong
  /** The typical millis per year. */
  private val MILLIS_PER_SHORT_YEAR: Long = 354L * DateTimeConstants.MILLIS_PER_DAY
  /** The typical millis per year. */
  private val MILLIS_PER_LONG_YEAR: Long = 355L * DateTimeConstants.MILLIS_PER_DAY
  /** The millis of 0001-01-01. */
  private val MILLIS_YEAR_1: Long = -42521587200000L
  /** The length of the cycle of leap years. */
  private val CYCLE: Int = 30
  /** The millis of a 30 year cycle. */
  private val MILLIS_PER_CYCLE: Long = ((19L * 354L + 11L * 355L) * DateTimeConstants.MILLIS_PER_DAY)
  /** Cache of zone to chronology arrays */
  private val cCache: ConcurrentHashMap[DateTimeZone, Array[IslamicChronology]] = new ConcurrentHashMap[DateTimeZone, Array[IslamicChronology]]
  /** Singleton instance of a UTC IslamicChronology */
  private val INSTANCE_UTC: IslamicChronology = null

  /**
   * Gets an instance of the IslamicChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: IslamicChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the IslamicChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: IslamicChronology = {
    return getInstance(DateTimeZone.getDefault, LEAP_YEAR_16_BASED)
  }

  /**
   * Gets an instance of the IslamicChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): IslamicChronology = {
    return getInstance(zone, LEAP_YEAR_16_BASED)
  }

  /**
   * Gets an instance of the IslamicChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @param leapYears  the type defining the leap year pattern
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone, leapYears: IslamicChronology.LeapYearPatternType): IslamicChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: IslamicChronology = null
    var chronos: Array[IslamicChronology] = cCache.get(zone)
    if (chronos == null) {
      chronos = new Array[IslamicChronology](4)
      val oldChronos: Array[IslamicChronology] = cCache.putIfAbsent(zone, chronos)
      if (oldChronos != null) {
        chronos = oldChronos
      }
    }
    chrono = chronos(leapYears.index)
    if (chrono == null) {
      chronos synchronized {
        chrono = chronos(leapYears.index)
        if (chrono == null) {
          if (zone eq DateTimeZone.UTC) {
            chrono = new IslamicChronology(null, null, leapYears)
            val lowerLimit: DateTime = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono)
            chrono = new IslamicChronology(LimitChronology.getInstance(chrono, lowerLimit, null), null, leapYears)
          }
          else {
            chrono = getInstance(DateTimeZone.UTC, leapYears)
            chrono = new IslamicChronology(ZonedChronology.getInstance(chrono, zone), null, leapYears)
          }
          chronos(leapYears.index) = chrono
        }
      }
    }
    return chrono
  }

  /**
   * Opaque object describing a leap year pattern for the Islamic Chronology.
   *
   * @since 1.2
   */
  @SerialVersionUID(26581275372698L)
  class LeapYearPatternType extends Serializable {
    /** The index. */
    private[chrono] final val index: Byte = 0
    /** The leap year pattern, a bit-based 1=true pattern. */
    private[chrono] final val pattern: Int = 0

    /**
     * Constructor.
     * This constructor takes a bit pattern where bits 0-29 correspond
     * to years 0-29 in the 30 year Islamic cycle of years. This allows
     * a highly efficient lookup by bit-matching.
     *
     * @param index  the index
     * @param pattern  the bit pattern
     */
    private[chrono] def this(index: Int, pattern: Int) {
      this()
      `super`
      this.index = index.toByte
      this.pattern = pattern
    }

    /**
     * Is the year a leap year.
     * @param year  the year to query
     * @return true if leap
     */
    private[chrono] def isLeapYear(year: Int): Boolean = {
      val key: Int = 1 << (year % 30)
      return ((pattern & key) > 0)
    }

    /**
     * Ensure a singleton is returned if possible.
     * @return the singleton instance
     */
    private def readResolve: AnyRef = {
      index match {
        case 0 =>
          return LEAP_YEAR_15_BASED
        case 1 =>
          return LEAP_YEAR_16_BASED
        case 2 =>
          return LEAP_YEAR_INDIAN
        case 3 =>
          return LEAP_YEAR_HABASH_AL_HASIB
        case _ =>
          return this
      }
    }

    override def equals(obj: AnyRef): Boolean = {
      if (obj.isInstanceOf[IslamicChronology.LeapYearPatternType]) {
        return index == (obj.asInstanceOf[IslamicChronology.LeapYearPatternType]).index
      }
      return false
    }

    override def hashCode: Int = {
      return index
    }
  }

  try {
    INSTANCE_UTC = getInstance(DateTimeZone.UTC)
  }
}

@SerialVersionUID(-3663823829888L)
final class IslamicChronology extends BasicChronology {
  /** The leap years to use. */
  private final val iLeapYears: IslamicChronology.LeapYearPatternType = null

  /**
   * Restricted constructor.
   */
  private[chrono] def this(base: Chronology, param: AnyRef, leapYears: IslamicChronology.LeapYearPatternType) {
    this()
    `super`(base, param, 4)
    this.iLeapYears = leapYears
  }

  /**
   * Serialization singleton.
   */
  private def readResolve: AnyRef = {
    val base: Chronology = getBase
    return if (base == null) IslamicChronology.getInstanceUTC else IslamicChronology.getInstance(base.getZone)
  }

  /**
   * Gets the leap year pattern type.
   *
   * @return the pattern type
   */
  def getLeapYearPatternType: IslamicChronology.LeapYearPatternType = {
    return iLeapYears
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return IslamicChronology.INSTANCE_UTC
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
    return IslamicChronology.getInstance(zone)
  }

  /**
   * Checks if this chronology instance equals another.
   *
   * @param obj  the object to compare to
   * @return true if equal
   * @since 2.3
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[IslamicChronology]) {
      val chrono: IslamicChronology = obj.asInstanceOf[IslamicChronology]
      return getLeapYearPatternType.index == chrono.getLeapYearPatternType.index && (super == obj)
    }
    return false
  }

  /**
   * A suitable hash code for the chronology.
   *
   * @return the hash code
   * @since 1.6
   */
  override def hashCode: Int = {
    return super.hashCode * 13 + getLeapYearPatternType.hashCode
  }

  private[chrono] override def getYear(instant: Long): Int = {
    val millisIslamic: Long = instant - IslamicChronology.MILLIS_YEAR_1
    val cycles: Long = millisIslamic / IslamicChronology.MILLIS_PER_CYCLE
    var cycleRemainder: Long = millisIslamic % IslamicChronology.MILLIS_PER_CYCLE
    val year: Int = ((cycles * IslamicChronology.CYCLE) + 1L).toInt
    var yearMillis: Long = (if (isLeapYear(year)) IslamicChronology.MILLIS_PER_LONG_YEAR else IslamicChronology.MILLIS_PER_SHORT_YEAR)
    while (cycleRemainder >= yearMillis) {
      cycleRemainder -= yearMillis
      yearMillis = (if (isLeapYear(({
        year += 1; year
      }))) IslamicChronology.MILLIS_PER_LONG_YEAR
      else IslamicChronology.MILLIS_PER_SHORT_YEAR)
    }
    return year
  }

  private[chrono] def setYear(instant: Long, year: Int): Long = {
    val thisYear: Int = getYear(instant)
    var dayOfYear: Int = getDayOfYear(instant, thisYear)
    val millisOfDay: Int = getMillisOfDay(instant)
    if (dayOfYear > 354) {
      if (!isLeapYear(year)) {
        dayOfYear -= 1
      }
    }
    instant = getYearMonthDayMillis(year, 1, dayOfYear)
    instant += millisOfDay
    return instant
  }

  private[chrono] def getYearDifference(minuendInstant: Long, subtrahendInstant: Long): Long = {
    val minuendYear: Int = getYear(minuendInstant)
    val subtrahendYear: Int = getYear(subtrahendInstant)
    val minuendRem: Long = minuendInstant - getYearMillis(minuendYear)
    val subtrahendRem: Long = subtrahendInstant - getYearMillis(subtrahendYear)
    var difference: Int = minuendYear - subtrahendYear
    if (minuendRem < subtrahendRem) {
      difference -= 1
    }
    return difference
  }

  private[chrono] def getTotalMillisByYearMonth(year: Int, month: Int): Long = {
    if (({
      month -= 1; month
    }) % 2 == 1) {
      month /= 2
      return month * IslamicChronology.MILLIS_PER_MONTH_PAIR + IslamicChronology.MILLIS_PER_LONG_MONTH
    }
    else {
      month /= 2
      return month * IslamicChronology.MILLIS_PER_MONTH_PAIR
    }
  }

  private[chrono] override def getDayOfMonth(millis: Long): Int = {
    val doy: Int = getDayOfYear(millis) - 1
    if (doy == 354) {
      return 30
    }
    return (doy % IslamicChronology.MONTH_PAIR_LENGTH) % IslamicChronology.LONG_MONTH_LENGTH + 1
  }

  private[chrono] def isLeapYear(year: Int): Boolean = {
    return iLeapYears.isLeapYear(year)
  }

  private[chrono] override def getDaysInYearMax: Int = {
    return 355
  }

  private[chrono] override def getDaysInYear(year: Int): Int = {
    return if (isLeapYear(year)) 355 else 354
  }

  private[chrono] def getDaysInYearMonth(year: Int, month: Int): Int = {
    if (month == 12 && isLeapYear(year)) {
      return IslamicChronology.LONG_MONTH_LENGTH
    }
    return (if (({
      month -= 1; month
    }) % 2 == 0) IslamicChronology.LONG_MONTH_LENGTH
    else IslamicChronology.SHORT_MONTH_LENGTH)
  }

  private[chrono] override def getDaysInMonthMax: Int = {
    return IslamicChronology.LONG_MONTH_LENGTH
  }

  private[chrono] def getDaysInMonthMax(month: Int): Int = {
    if (month == 12) {
      return IslamicChronology.LONG_MONTH_LENGTH
    }
    return (if (({
      month -= 1; month
    }) % 2 == 0) IslamicChronology.LONG_MONTH_LENGTH
    else IslamicChronology.SHORT_MONTH_LENGTH)
  }

  private[chrono] def getMonthOfYear(millis: Long, year: Int): Int = {
    val doyZeroBased: Int = ((millis - getYearMillis(year)) / DateTimeConstants.MILLIS_PER_DAY).toInt
    if (doyZeroBased == 354) {
      return 12
    }
    return ((doyZeroBased * 2) / IslamicChronology.MONTH_PAIR_LENGTH) + 1
  }

  private[chrono] def getAverageMillisPerYear: Long = {
    return IslamicChronology.MILLIS_PER_YEAR
  }

  private[chrono] def getAverageMillisPerYearDividedByTwo: Long = {
    return IslamicChronology.MILLIS_PER_YEAR / 2
  }

  private[chrono] def getAverageMillisPerMonth: Long = {
    return IslamicChronology.MILLIS_PER_MONTH
  }

  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long = {
    if (year > IslamicChronology.MAX_YEAR) {
      throw new ArithmeticException("Year is too large: " + year + " > " + IslamicChronology.MAX_YEAR)
    }
    if (year < IslamicChronology.MIN_YEAR) {
      throw new ArithmeticException("Year is too small: " + year + " < " + IslamicChronology.MIN_YEAR)
    }
    year -= 1
    val cycle: Long = year / IslamicChronology.CYCLE
    var millis: Long = IslamicChronology.MILLIS_YEAR_1 + cycle * IslamicChronology.MILLIS_PER_CYCLE
    val cycleRemainder: Int = (year % IslamicChronology.CYCLE) + 1
    {
      var i: Int = 1
      while (i < cycleRemainder) {
        {
          millis += (if (isLeapYear(i)) IslamicChronology.MILLIS_PER_LONG_YEAR else IslamicChronology.MILLIS_PER_SHORT_YEAR)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return millis
  }

  private[chrono] def getMinYear: Int = {
    return 1
  }

  private[chrono] def getMaxYear: Int = {
    return IslamicChronology.MAX_YEAR
  }

  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long = {
    return (-IslamicChronology.MILLIS_YEAR_1) / 2
  }

  protected override def assemble(fields: AssembledChronology.Fields) {
    if (getBase == null) {
      super.assemble(fields)
      fields.era = IslamicChronology.ERA_FIELD
      fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 12)
      fields.months = fields.monthOfYear.getDurationField
    }
  }
}