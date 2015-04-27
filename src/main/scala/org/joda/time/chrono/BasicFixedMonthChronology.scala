/*
 *  Copyright 2001-2005 Stephen Colebourne
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

import org.joda.time.Chronology
import org.joda.time.DateTimeConstants

/**
 * Abstract implementation of a calendar system based around fixed length months.
 * <p>
 * As the month length is fixed various calculations can be optimised.
 * This implementation assumes any additional days after twelve
 * months fall into a thirteenth month.
 * <p>
 * BasicFixedMonthChronology is thread-safe and immutable, and all
 * subclasses must be as well.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.2, refactored from CopticChronology
 */
@SerialVersionUID(261387371998L)
object BasicFixedMonthChronology {
  /** The length of the month. */
  private[chrono] val MONTH_LENGTH: Int = 30
  /** The typical millis per year. */
  private[chrono] val MILLIS_PER_YEAR: Long = (365.25 * DateTimeConstants.MILLIS_PER_DAY).toLong
  /** The length of the month in millis. */
  private[chrono] val MILLIS_PER_MONTH: Long = (MONTH_LENGTH.toLong) * DateTimeConstants.MILLIS_PER_DAY
}

@SerialVersionUID(261387371998L)
abstract class BasicFixedMonthChronology extends BasicChronology {
  /**
   * Restricted constructor.
   *
   * @param base  the base chronology
   * @param param  the init parameter
   * @param minDaysInFirstWeek  the minimum days in the first week
   */
  private[chrono] def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
    this()
    `super`(base, param, minDaysInFirstWeek)
  }

  private[chrono] def setYear(instant: Long, year: Int): Long = {
    val thisYear: Int = getYear(instant)
    var dayOfYear: Int = getDayOfYear(instant, thisYear)
    val millisOfDay: Int = getMillisOfDay(instant)
    if (dayOfYear > 365) {
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
    return ((month - 1) * BasicFixedMonthChronology.MILLIS_PER_MONTH)
  }

  private[chrono] override def getDayOfMonth(millis: Long): Int = {
    return (getDayOfYear(millis) - 1) % BasicFixedMonthChronology.MONTH_LENGTH + 1
  }

  private[chrono] def isLeapYear(year: Int): Boolean = {
    return (year & 3) == 3
  }

  private[chrono] def getDaysInYearMonth(year: Int, month: Int): Int = {
    return if ((month != 13)) BasicFixedMonthChronology.MONTH_LENGTH else (if (isLeapYear(year)) 6 else 5)
  }

  private[chrono] override def getDaysInMonthMax: Int = {
    return BasicFixedMonthChronology.MONTH_LENGTH
  }

  private[chrono] def getDaysInMonthMax(month: Int): Int = {
    return (if (month != 13) BasicFixedMonthChronology.MONTH_LENGTH else 6)
  }

  private[chrono] override def getMonthOfYear(millis: Long): Int = {
    return (getDayOfYear(millis) - 1) / BasicFixedMonthChronology.MONTH_LENGTH + 1
  }

  private[chrono] def getMonthOfYear(millis: Long, year: Int): Int = {
    val monthZeroBased: Long = (millis - getYearMillis(year)) / BasicFixedMonthChronology.MILLIS_PER_MONTH
    return (monthZeroBased.toInt) + 1
  }

  private[chrono] override def getMaxMonth: Int = {
    return 13
  }

  private[chrono] def getAverageMillisPerYear: Long = {
    return BasicFixedMonthChronology.MILLIS_PER_YEAR
  }

  private[chrono] def getAverageMillisPerYearDividedByTwo: Long = {
    return BasicFixedMonthChronology.MILLIS_PER_YEAR / 2
  }

  private[chrono] def getAverageMillisPerMonth: Long = {
    return BasicFixedMonthChronology.MILLIS_PER_MONTH
  }
}