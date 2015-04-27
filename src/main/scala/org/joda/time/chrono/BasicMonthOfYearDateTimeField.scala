/*
 *  Copyright 2001-2013 Stephen Colebourne
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

import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeUtils
import org.joda.time.DurationField
import org.joda.time.ReadablePartial
import org.joda.time.field.FieldUtils
import org.joda.time.field.ImpreciseDateTimeField

/**
 * Provides time calculations for the month of the year component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.2, refactored from GJMonthOfYearDateTimeField
 */
@SerialVersionUID(-8258715387168736L)
object BasicMonthOfYearDateTimeField {
  private val MIN: Int = DateTimeConstants.JANUARY
}

@SerialVersionUID(-8258715387168736L)
class BasicMonthOfYearDateTimeField extends ImpreciseDateTimeField {
  private final val iChronology: BasicChronology = null
  private final val iMax: Int = 0
  private final val iLeapMonth: Int = 0

  /**
   * Restricted constructor.
   *
   * @param leapMonth the month of year that leaps
   */
  private[chrono] def this(chronology: BasicChronology, leapMonth: Int) {
    this()
    `super`(DateTimeFieldType.monthOfYear, chronology.getAverageMillisPerMonth)
    iChronology = chronology
    iMax = iChronology.getMaxMonth
    iLeapMonth = leapMonth
  }

  def isLenient: Boolean = {
    return false
  }

  /**
   * Get the Month component of the specified time instant.
   *
   * @see org.joda.time.DateTimeField#get(long)
   * @see org.joda.time.ReadableDateTime#getMonthOfYear()
   * @param instant  the time instant in millis to query.
   * @return the month extracted from the input.
   */
  def get(instant: Long): Int = {
    return iChronology.getMonthOfYear(instant)
  }

  /**
   * Add the specified month to the specified time instant.
   * The amount added may be negative.<p>
   * If the new month has less total days than the specified
   * day of the month, this value is coerced to the nearest
   * sane value. e.g.<p>
   * 07-31 - (1 month) = 06-30<p>
   * 03-31 - (1 month) = 02-28 or 02-29 depending<p>
   *
   * @see org.joda.time.DateTimeField#add
   * @see org.joda.time.ReadWritableDateTime#addMonths(int)
   * @param instant  the time instant in millis to update.
   * @param months  the months to add (can be negative).
   * @return the updated time instant.
   */
  def add(instant: Long, months: Int): Long = {
    if (months == 0) {
      return instant
    }
    val timePart: Long = iChronology.getMillisOfDay(instant)
    val thisYear: Int = iChronology.getYear(instant)
    val thisMonth: Int = iChronology.getMonthOfYear(instant, thisYear)
    var yearToUse: Int = 0
    var monthToUse: Int = thisMonth - 1 + months
    if (monthToUse >= 0) {
      yearToUse = thisYear + (monthToUse / iMax)
      monthToUse = (monthToUse % iMax) + 1
    }
    else {
      yearToUse = thisYear + (monthToUse / iMax) - 1
      monthToUse = Math.abs(monthToUse)
      var remMonthToUse: Int = monthToUse % iMax
      if (remMonthToUse == 0) {
        remMonthToUse = iMax
      }
      monthToUse = iMax - remMonthToUse + 1
      if (monthToUse == 1) {
        yearToUse += 1
      }
    }
    var dayToUse: Int = iChronology.getDayOfMonth(instant, thisYear, thisMonth)
    val maxDay: Int = iChronology.getDaysInYearMonth(yearToUse, monthToUse)
    if (dayToUse > maxDay) {
      dayToUse = maxDay
    }
    val datePart: Long = iChronology.getYearMonthDayMillis(yearToUse, monthToUse, dayToUse)
    return datePart + timePart
  }

  def add(instant: Long, months: Long): Long = {
    val i_months: Int = months.toInt
    if (i_months == months) {
      return add(instant, i_months)
    }
    val timePart: Long = iChronology.getMillisOfDay(instant)
    val thisYear: Int = iChronology.getYear(instant)
    val thisMonth: Int = iChronology.getMonthOfYear(instant, thisYear)
    var yearToUse: Long = 0L
    var monthToUse: Long = thisMonth - 1 + months
    if (monthToUse >= 0) {
      yearToUse = thisYear + (monthToUse / iMax)
      monthToUse = (monthToUse % iMax) + 1
    }
    else {
      yearToUse = thisYear + (monthToUse / iMax) - 1
      monthToUse = Math.abs(monthToUse)
      var remMonthToUse: Int = (monthToUse % iMax).toInt
      if (remMonthToUse == 0) {
        remMonthToUse = iMax
      }
      monthToUse = iMax - remMonthToUse + 1
      if (monthToUse == 1) {
        yearToUse += 1
      }
    }
    if (yearToUse < iChronology.getMinYear || yearToUse > iChronology.getMaxYear) {
      throw new IllegalArgumentException("Magnitude of add amount is too large: " + months)
    }
    val i_yearToUse: Int = yearToUse.toInt
    val i_monthToUse: Int = monthToUse.toInt
    var dayToUse: Int = iChronology.getDayOfMonth(instant, thisYear, thisMonth)
    val maxDay: Int = iChronology.getDaysInYearMonth(i_yearToUse, i_monthToUse)
    if (dayToUse > maxDay) {
      dayToUse = maxDay
    }
    val datePart: Long = iChronology.getYearMonthDayMillis(i_yearToUse, i_monthToUse, dayToUse)
    return datePart + timePart
  }

  override def add(partial: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    if (valueToAdd == 0) {
      return values
    }
    if (partial.size > 0 && (partial.getFieldType(0) == DateTimeFieldType.monthOfYear) && fieldIndex == 0) {
      val curMonth0: Int = values(0) - 1
      val newMonth: Int = ((curMonth0 + (valueToAdd % 12) + 12) % 12) + 1
      return set(partial, 0, values, newMonth)
    }
    if (DateTimeUtils.isContiguous(partial)) {
      var instant: Long = 0L
      {
        var i: Int = 0
        val isize: Int = partial.size
        while (i < isize) {
          {
            instant = partial.getFieldType(i).getField(iChronology).set(instant, values(i))
          }
          ({
            i += 1; i - 1
          })
        }
      }
      instant = add(instant, valueToAdd)
      return iChronology.get(partial, instant)
    }
    else {
      return super.add(partial, fieldIndex, values, valueToAdd)
    }
  }

  /**
   * Add to the Month component of the specified time instant
   * wrapping around within that component if necessary.
   *
   * @see org.joda.time.DateTimeField#addWrapField
   * @param instant  the time instant in millis to update.
   * @param months  the months to add (can be negative).
   * @return the updated time instant.
   */
  override def addWrapField(instant: Long, months: Int): Long = {
    return set(instant, FieldUtils.getWrappedValue(get(instant), months, BasicMonthOfYearDateTimeField.MIN, iMax))
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    if (minuendInstant < subtrahendInstant) {
      return -getDifference(subtrahendInstant, minuendInstant)
    }
    val minuendYear: Int = iChronology.getYear(minuendInstant)
    val minuendMonth: Int = iChronology.getMonthOfYear(minuendInstant, minuendYear)
    val subtrahendYear: Int = iChronology.getYear(subtrahendInstant)
    val subtrahendMonth: Int = iChronology.getMonthOfYear(subtrahendInstant, subtrahendYear)
    var difference: Long = (minuendYear - subtrahendYear) * (iMax.toLong) + minuendMonth - subtrahendMonth
    val minuendDom: Int = iChronology.getDayOfMonth(minuendInstant, minuendYear, minuendMonth)
    if (minuendDom == iChronology.getDaysInYearMonth(minuendYear, minuendMonth)) {
      val subtrahendDom: Int = iChronology.getDayOfMonth(subtrahendInstant, subtrahendYear, subtrahendMonth)
      if (subtrahendDom > minuendDom) {
        subtrahendInstant = iChronology.dayOfMonth.set(subtrahendInstant, minuendDom)
      }
    }
    val minuendRem: Long = minuendInstant - iChronology.getYearMonthMillis(minuendYear, minuendMonth)
    val subtrahendRem: Long = subtrahendInstant - iChronology.getYearMonthMillis(subtrahendYear, subtrahendMonth)
    if (minuendRem < subtrahendRem) {
      difference -= 1
    }
    return difference
  }

  /**
   * Set the Month component of the specified time instant.<p>
   * If the new month has less total days than the specified
   * day of the month, this value is coerced to the nearest
   * sane value. e.g.<p>
   * 07-31 to month 6 = 06-30<p>
   * 03-31 to month 2 = 02-28 or 02-29 depending<p>
   *
   * @param instant  the time instant in millis to update.
   * @param month  the month (1,12) to update the time to.
   * @return the updated time instant.
   * @throws IllegalArgumentException  if month is invalid
   */
  def set(instant: Long, month: Int): Long = {
    FieldUtils.verifyValueBounds(this, month, BasicMonthOfYearDateTimeField.MIN, iMax)
    val thisYear: Int = iChronology.getYear(instant)
    var thisDom: Int = iChronology.getDayOfMonth(instant, thisYear)
    val maxDom: Int = iChronology.getDaysInYearMonth(thisYear, month)
    if (thisDom > maxDom) {
      thisDom = maxDom
    }
    return iChronology.getYearMonthDayMillis(thisYear, month, thisDom) + iChronology.getMillisOfDay(instant)
  }

  def getRangeDurationField: DurationField = {
    return iChronology.years
  }

  override def isLeap(instant: Long): Boolean = {
    val thisYear: Int = iChronology.getYear(instant)
    if (iChronology.isLeapYear(thisYear)) {
      return (iChronology.getMonthOfYear(instant, thisYear) == iLeapMonth)
    }
    return false
  }

  override def getLeapAmount(instant: Long): Int = {
    return if (isLeap(instant)) 1 else 0
  }

  override def getLeapDurationField: DurationField = {
    return iChronology.days
  }

  def getMinimumValue: Int = {
    return BasicMonthOfYearDateTimeField.MIN
  }

  def getMaximumValue: Int = {
    return iMax
  }

  def roundFloor(instant: Long): Long = {
    val year: Int = iChronology.getYear(instant)
    val month: Int = iChronology.getMonthOfYear(instant, year)
    return iChronology.getYearMonthMillis(year, month)
  }

  override def remainder(instant: Long): Long = {
    return instant - roundFloor(instant)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.monthOfYear
  }
}