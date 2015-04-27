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
 * Provides time calculations for the day of the year component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.1, refactored from GJDayOfYearDateTimeField
 */
@SerialVersionUID(-6821236822336841037L)
final class BasicDayOfYearDateTimeField extends PreciseDurationDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor
   */
  private[chrono] def this(chronology: BasicChronology, days: DurationField) {
    this()
    `super`(DateTimeFieldType.dayOfYear, days)
    iChronology = chronology
  }

  /**
   * Get the day of the year component of the specified time instant.
   *
   * @param instant  the time instant in millis to query.
   * @return the day of the year extracted from the input.
   */
  def get(instant: Long): Int = {
    return iChronology.getDayOfYear(instant)
  }

  def getRangeDurationField: DurationField = {
    return iChronology.years
  }

  override def getMinimumValue: Int = {
    return 1
  }

  def getMaximumValue: Int = {
    return iChronology.getDaysInYearMax
  }

  override def getMaximumValue(instant: Long): Int = {
    val year: Int = iChronology.getYear(instant)
    return iChronology.getDaysInYear(year)
  }

  override def getMaximumValue(partial: ReadablePartial): Int = {
    if (partial.isSupported(DateTimeFieldType.year)) {
      val year: Int = partial.get(DateTimeFieldType.year)
      return iChronology.getDaysInYear(year)
    }
    return iChronology.getDaysInYearMax
  }

  override def getMaximumValue(partial: ReadablePartial, values: Array[Int]): Int = {
    val size: Int = partial.size
    {
      var i: Int = 0
      while (i < size) {
        {
          if (partial.getFieldType(i) eq DateTimeFieldType.year) {
            val year: Int = values(i)
            return iChronology.getDaysInYear(year)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return iChronology.getDaysInYearMax
  }

  protected override def getMaximumValueForSet(instant: Long, value: Int): Int = {
    val maxLessOne: Int = iChronology.getDaysInYearMax - 1
    return if ((value > maxLessOne || value < 1)) getMaximumValue(instant) else maxLessOne
  }

  override def isLeap(instant: Long): Boolean = {
    return iChronology.isLeapDay(instant)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.dayOfYear
  }
}