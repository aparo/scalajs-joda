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

import org.joda.time.Chronology
import org.joda.time.DateTimeConstants

/**
 * Abstract Chronology for implementing chronologies based on Gregorian/Julian formulae.
 * Most of the utility methods required by subclasses are package-private,
 * reflecting the intention that they be defined in the same package.
 * <p>
 * BasicGJChronology is thread-safe and immutable, and all subclasses must
 * be as well.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @author Guy Allard
 * @since 1.2, refactored from CommonGJChronology
 */
@SerialVersionUID(538276888268L)
object BasicGJChronology {
  private val MIN_DAYS_PER_MONTH_ARRAY: Array[Int] = Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  private val MAX_DAYS_PER_MONTH_ARRAY: Array[Int] = Array(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
  private val MIN_TOTAL_MILLIS_BY_MONTH_ARRAY: Array[Long] = null
  private val MAX_TOTAL_MILLIS_BY_MONTH_ARRAY: Array[Long] = null
  private val FEB_29: Long = (31L + 29 - 1) * DateTimeConstants.MILLIS_PER_DAY
  try {
    MIN_TOTAL_MILLIS_BY_MONTH_ARRAY = new Array[Long](12)
    MAX_TOTAL_MILLIS_BY_MONTH_ARRAY = new Array[Long](12)
    var minSum: Long = 0
    var maxSum: Long = 0
    {
      var i: Int = 0
      while (i < 11) {
        {
          var millis: Long = MIN_DAYS_PER_MONTH_ARRAY(i) * DateTimeConstants.MILLIS_PER_DAY.toLong
          minSum += millis
          MIN_TOTAL_MILLIS_BY_MONTH_ARRAY(i + 1) = minSum
          millis = MAX_DAYS_PER_MONTH_ARRAY(i) * DateTimeConstants.MILLIS_PER_DAY.toLong
          maxSum += millis
          MAX_TOTAL_MILLIS_BY_MONTH_ARRAY(i + 1) = maxSum
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }
}

@SerialVersionUID(538276888268L)
abstract class BasicGJChronology extends BasicChronology {
  /**
   * Constructor.
   */
  private[chrono] def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
    this()
    `super`(base, param, minDaysInFirstWeek)
  }

  private[chrono] override def isLeapDay(instant: Long): Boolean = {
    return dayOfMonth.get(instant) == 29 && monthOfYear.isLeap(instant)
  }

  private[chrono] def getMonthOfYear(millis: Long, year: Int): Int = {
    val i: Int = ((millis - getYearMillis(year)) >> 10).toInt
    return if ((isLeapYear(year))) (if ((i < 182 * 84375)) (if ((i < 91 * 84375)) (if ((i < 31 * 84375)) 1 else if ((i < 60 * 84375)) 2 else 3) else (if ((i < 121 * 84375)) 4 else if ((i < 152 * 84375)) 5 else 6)) else (if ((i < 274 * 84375)) (if ((i < 213 * 84375)) 7 else if ((i < 244 * 84375)) 8 else 9) else (if ((i < 305 * 84375)) 10 else if ((i < 335 * 84375)) 11 else 12))) else (if ((i < 181 * 84375)) (if ((i < 90 * 84375)) (if ((i < 31 * 84375)) 1 else if ((i < 59 * 84375)) 2 else 3) else (if ((i < 120 * 84375)) 4 else if ((i < 151 * 84375)) 5 else 6)) else (if ((i < 273 * 84375)) (if ((i < 212 * 84375)) 7 else if ((i < 243 * 84375)) 8 else 9) else (if ((i < 304 * 84375)) 10 else if ((i < 334 * 84375)) 11 else 12)))
  }

  /**
   * Gets the number of days in the specified month and year.
   *
   * @param year  the year
   * @param month  the month
   * @return the number of days
   */
  private[chrono] def getDaysInYearMonth(year: Int, month: Int): Int = {
    if (isLeapYear(year)) {
      return BasicGJChronology.MAX_DAYS_PER_MONTH_ARRAY(month - 1)
    }
    else {
      return BasicGJChronology.MIN_DAYS_PER_MONTH_ARRAY(month - 1)
    }
  }

  private[chrono] def getDaysInMonthMax(month: Int): Int = {
    return BasicGJChronology.MAX_DAYS_PER_MONTH_ARRAY(month - 1)
  }

  private[chrono] override def getDaysInMonthMaxForSet(instant: Long, value: Int): Int = {
    return (if ((value > 28 || value < 1)) getDaysInMonthMax(instant) else 28)
  }

  private[chrono] def getTotalMillisByYearMonth(year: Int, month: Int): Long = {
    if (isLeapYear(year)) {
      return BasicGJChronology.MAX_TOTAL_MILLIS_BY_MONTH_ARRAY(month - 1)
    }
    else {
      return BasicGJChronology.MIN_TOTAL_MILLIS_BY_MONTH_ARRAY(month - 1)
    }
  }

  private[chrono] def getYearDifference(minuendInstant: Long, subtrahendInstant: Long): Long = {
    val minuendYear: Int = getYear(minuendInstant)
    val subtrahendYear: Int = getYear(subtrahendInstant)
    var minuendRem: Long = minuendInstant - getYearMillis(minuendYear)
    var subtrahendRem: Long = subtrahendInstant - getYearMillis(subtrahendYear)
    if (subtrahendRem >= BasicGJChronology.FEB_29) {
      if (isLeapYear(subtrahendYear)) {
        if (!isLeapYear(minuendYear)) {
          subtrahendRem -= DateTimeConstants.MILLIS_PER_DAY
        }
      }
      else if (minuendRem >= BasicGJChronology.FEB_29 && isLeapYear(minuendYear)) {
        minuendRem -= DateTimeConstants.MILLIS_PER_DAY
      }
    }
    var difference: Int = minuendYear - subtrahendYear
    if (minuendRem < subtrahendRem) {
      difference -= 1
    }
    return difference
  }

  private[chrono] def setYear(instant: Long, year: Int): Long = {
    val thisYear: Int = getYear(instant)
    var dayOfYear: Int = getDayOfYear(instant, thisYear)
    val millisOfDay: Int = getMillisOfDay(instant)
    if (dayOfYear > (31 + 28)) {
      if (isLeapYear(thisYear)) {
        if (!isLeapYear(year)) {
          dayOfYear -= 1
        }
      }
      else {
        if (isLeapYear(year)) {
          dayOfYear += 1
        }
      }
    }
    instant = getYearMonthDayMillis(year, 1, dayOfYear)
    instant += millisOfDay
    return instant
  }
}