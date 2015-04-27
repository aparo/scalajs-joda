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

import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.field.FieldUtils
import org.joda.time.field.ImpreciseDateTimeField

/**
 * A year field suitable for many calendars.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.1, refactored from GJYearDateTimeField
 */
@SerialVersionUID(-98628754872287L)
class BasicYearDateTimeField extends ImpreciseDateTimeField {
  /** The underlying basic chronology. */
  protected final val iChronology: BasicChronology = null

  /**
   * Restricted constructor.
   *
   * @param chronology  the chronology this field belogs to
   */
  private[chrono] def this(chronology: BasicChronology) {
    this()
    `super`(DateTimeFieldType.year, chronology.getAverageMillisPerYear)
    iChronology = chronology
  }

  def isLenient: Boolean = {
    return false
  }

  def get(instant: Long): Int = {
    return iChronology.getYear(instant)
  }

  def add(instant: Long, years: Int): Long = {
    if (years == 0) {
      return instant
    }
    val thisYear: Int = get(instant)
    val newYear: Int = FieldUtils.safeAdd(thisYear, years)
    return set(instant, newYear)
  }

  def add(instant: Long, years: Long): Long = {
    return add(instant, FieldUtils.safeToInt(years))
  }

  override def addWrapField(instant: Long, years: Int): Long = {
    if (years == 0) {
      return instant
    }
    val thisYear: Int = iChronology.getYear(instant)
    val wrappedYear: Int = FieldUtils.getWrappedValue(thisYear, years, iChronology.getMinYear, iChronology.getMaxYear)
    return set(instant, wrappedYear)
  }

  def set(instant: Long, year: Int): Long = {
    FieldUtils.verifyValueBounds(this, year, iChronology.getMinYear, iChronology.getMaxYear)
    return iChronology.setYear(instant, year)
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    if (minuendInstant < subtrahendInstant) {
      return -iChronology.getYearDifference(subtrahendInstant, minuendInstant)
    }
    return iChronology.getYearDifference(minuendInstant, subtrahendInstant)
  }

  def getRangeDurationField: DurationField = {
    return null
  }

  override def isLeap(instant: Long): Boolean = {
    return iChronology.isLeapYear(get(instant))
  }

  override def getLeapAmount(instant: Long): Int = {
    if (iChronology.isLeapYear(get(instant))) {
      return 1
    }
    else {
      return 0
    }
  }

  override def getLeapDurationField: DurationField = {
    return iChronology.days
  }

  def getMinimumValue: Int = {
    return iChronology.getMinYear
  }

  def getMaximumValue: Int = {
    return iChronology.getMaxYear
  }

  def roundFloor(instant: Long): Long = {
    return iChronology.getYearMillis(get(instant))
  }

  override def roundCeiling(instant: Long): Long = {
    val year: Int = get(instant)
    val yearStartMillis: Long = iChronology.getYearMillis(year)
    if (instant != yearStartMillis) {
      instant = iChronology.getYearMillis(year + 1)
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
    return iChronology.year
  }
}