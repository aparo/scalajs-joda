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

import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.ReadablePartial
import org.joda.time.field.PreciseDurationDateTimeField

/**
 * Provides time calculations for the day of the month component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.1, refactored from GJDayOfMonthDateTimeField
 */
@SerialVersionUID(-4677223814028011723L)
final class BasicDayOfMonthDateTimeField extends PreciseDurationDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor.
   */
  private[chrono] def this(chronology: BasicChronology, days: DurationField) {
    this()
    `super`(DateTimeFieldType.dayOfMonth, days)
    iChronology = chronology
  }

  def get(instant: Long): Int = {
    return iChronology.getDayOfMonth(instant)
  }

  def getRangeDurationField: DurationField = {
    return iChronology.months
  }

  override def getMinimumValue: Int = {
    return 1
  }

  def getMaximumValue: Int = {
    return iChronology.getDaysInMonthMax
  }

  override def getMaximumValue(instant: Long): Int = {
    return iChronology.getDaysInMonthMax(instant)
  }

  override def getMaximumValue(partial: ReadablePartial): Int = {
    if (partial.isSupported(DateTimeFieldType.monthOfYear)) {
      val month: Int = partial.get(DateTimeFieldType.monthOfYear)
      if (partial.isSupported(DateTimeFieldType.year)) {
        val year: Int = partial.get(DateTimeFieldType.year)
        return iChronology.getDaysInYearMonth(year, month)
      }
      return iChronology.getDaysInMonthMax(month)
    }
    return getMaximumValue
  }

  override def getMaximumValue(partial: ReadablePartial, values: Array[Int]): Int = {
    val size: Int = partial.size
    {
      var i: Int = 0
      while (i < size) {
        {
          if (partial.getFieldType(i) eq DateTimeFieldType.monthOfYear) {
            val month: Int = values(i)
            {
              var j: Int = 0
              while (j < size) {
                {
                  if (partial.getFieldType(j) eq DateTimeFieldType.year) {
                    val year: Int = values(j)
                    return iChronology.getDaysInYearMonth(year, month)
                  }
                }
                ({
                  j += 1; j - 1
                })
              }
            }
            return iChronology.getDaysInMonthMax(month)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return getMaximumValue
  }

  protected override def getMaximumValueForSet(instant: Long, value: Int): Int = {
    return iChronology.getDaysInMonthMaxForSet(instant, value)
  }

  override def isLeap(instant: Long): Boolean = {
    return iChronology.isLeapDay(instant)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.dayOfMonth
  }
}