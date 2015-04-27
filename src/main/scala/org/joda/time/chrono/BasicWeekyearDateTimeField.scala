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
import org.joda.time.DurationField
import org.joda.time.field.FieldUtils
import org.joda.time.field.ImpreciseDateTimeField

/**
 * Provides time calculations for the week of the weekyear component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.1, refactored from GJWeekyearDateTimeField
 */
@SerialVersionUID(6215066916806820644L)
object BasicWeekyearDateTimeField {
  private val WEEK_53: Long = (53L - 1) * DateTimeConstants.MILLIS_PER_WEEK
}

@SerialVersionUID(6215066916806820644L)
final class BasicWeekyearDateTimeField extends ImpreciseDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor
   */
  private[chrono] def this(chronology: BasicChronology) {
    this()
    `super`(DateTimeFieldType.weekyear, chronology.getAverageMillisPerYear)
    iChronology = chronology
  }

  def isLenient: Boolean = {
    return false
  }

  /**
   * Get the Year of a week based year component of the specified time instant.
   *
   * @see org.joda.time.DateTimeField#get
   * @param instant  the time instant in millis to query.
   * @return the year extracted from the input.
   */
  def get(instant: Long): Int = {
    return iChronology.getWeekyear(instant)
  }

  /**
   * Add the specified years to the specified time instant.
   *
   * @see org.joda.time.DateTimeField#add
   * @param instant  the time instant in millis to update.
   * @param years  the years to add (can be negative).
   * @return the updated time instant.
   */
  def add(instant: Long, years: Int): Long = {
    if (years == 0) {
      return instant
    }
    return set(instant, get(instant) + years)
  }

  def add(instant: Long, value: Long): Long = {
    return add(instant, FieldUtils.safeToInt(value))
  }

  /**
   * Add to the year component of the specified time instant
   * wrapping around within that component if necessary.
   *
   * @see org.joda.time.DateTimeField#addWrapField
   * @param instant  the time instant in millis to update.
   * @param years  the years to add (can be negative).
   * @return the updated time instant.
   */
  override def addWrapField(instant: Long, years: Int): Long = {
    return add(instant, years)
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    if (minuendInstant < subtrahendInstant) {
      return -getDifference(subtrahendInstant, minuendInstant)
    }
    val minuendWeekyear: Int = get(minuendInstant)
    val subtrahendWeekyear: Int = get(subtrahendInstant)
    val minuendRem: Long = remainder(minuendInstant)
    var subtrahendRem: Long = remainder(subtrahendInstant)
    if (subtrahendRem >= BasicWeekyearDateTimeField.WEEK_53 && iChronology.getWeeksInYear(minuendWeekyear) <= 52) {
      subtrahendRem -= DateTimeConstants.MILLIS_PER_WEEK
    }
    var difference: Int = minuendWeekyear - subtrahendWeekyear
    if (minuendRem < subtrahendRem) {
      difference -= 1
    }
    return difference
  }

  /**
   * Set the Year of a week based year component of the specified time instant.
   *
   * @see org.joda.time.DateTimeField#set
   * @param instant  the time instant in millis to update.
   * @param year  the year (-9999,9999) to set the date to.
   * @return the updated DateTime.
   * @throws IllegalArgumentException  if year is invalid.
   */
  def set(instant: Long, year: Int): Long = {
    FieldUtils.verifyValueBounds(this, Math.abs(year), iChronology.getMinYear, iChronology.getMaxYear)
    val thisWeekyear: Int = get(instant)
    if (thisWeekyear == year) {
      return instant
    }
    val thisDow: Int = iChronology.getDayOfWeek(instant)
    val weeksInFromYear: Int = iChronology.getWeeksInYear(thisWeekyear)
    val weeksInToYear: Int = iChronology.getWeeksInYear(year)
    val maxOutWeeks: Int = if ((weeksInToYear < weeksInFromYear)) weeksInToYear else weeksInFromYear
    var setToWeek: Int = iChronology.getWeekOfWeekyear(instant)
    if (setToWeek > maxOutWeeks) {
      setToWeek = maxOutWeeks
    }
    var workInstant: Long = instant
    workInstant = iChronology.setYear(workInstant, year)
    val workWoyYear: Int = get(workInstant)
    if (workWoyYear < year) {
      workInstant += DateTimeConstants.MILLIS_PER_WEEK
    }
    else if (workWoyYear > year) {
      workInstant -= DateTimeConstants.MILLIS_PER_WEEK
    }
    val currentWoyWeek: Int = iChronology.getWeekOfWeekyear(workInstant)
    workInstant = workInstant + (setToWeek - currentWoyWeek) * DateTimeConstants.MILLIS_PER_WEEK.toLong
    workInstant = iChronology.dayOfWeek.set(workInstant, thisDow)
    return workInstant
  }

  def getRangeDurationField: DurationField = {
    return null
  }

  override def isLeap(instant: Long): Boolean = {
    return iChronology.getWeeksInYear(iChronology.getWeekyear(instant)) > 52
  }

  override def getLeapAmount(instant: Long): Int = {
    return iChronology.getWeeksInYear(iChronology.getWeekyear(instant)) - 52
  }

  override def getLeapDurationField: DurationField = {
    return iChronology.weeks
  }

  def getMinimumValue: Int = {
    return iChronology.getMinYear
  }

  def getMaximumValue: Int = {
    return iChronology.getMaxYear
  }

  def roundFloor(instant: Long): Long = {
    instant = iChronology.weekOfWeekyear.roundFloor(instant)
    val wow: Int = iChronology.getWeekOfWeekyear(instant)
    if (wow > 1) {
      instant -= (DateTimeConstants.MILLIS_PER_WEEK.toLong) * (wow - 1)
    }
    return instant
  }

  override def remainder(instant: Long): Long = {
    return instant - roundFloor(instant)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.weekyear
  }
}