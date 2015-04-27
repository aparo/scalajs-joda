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
import org.joda.time.ReadablePartial
import org.joda.time.field.PreciseDurationDateTimeField

/**
 * Provides time calculations for the week of a week based year component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.1, refactored from GJWeekOfWeekyearDateTimeField
 */
@SerialVersionUID(-1587436826395135328L)
final class BasicWeekOfWeekyearDateTimeField extends PreciseDurationDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor
   */
  private[chrono] def this(chronology: BasicChronology, weeks: DurationField) {
    this()
    `super`(DateTimeFieldType.weekOfWeekyear, weeks)
    iChronology = chronology
  }

  /**
   * Get the week of a week based year component of the specified time instant.
   *
   * @see org.joda.time.DateTimeField#get(long)
   * @param instant  the time instant in millis to query.
   * @return the week of the year extracted from the input.
   */
  def get(instant: Long): Int = {
    return iChronology.getWeekOfWeekyear(instant)
  }

  def getRangeDurationField: DurationField = {
    return iChronology.weekyears
  }

  override def roundFloor(instant: Long): Long = {
    return super.roundFloor(instant + 3 * DateTimeConstants.MILLIS_PER_DAY) - 3 * DateTimeConstants.MILLIS_PER_DAY
  }

  override def roundCeiling(instant: Long): Long = {
    return super.roundCeiling(instant + 3 * DateTimeConstants.MILLIS_PER_DAY) - 3 * DateTimeConstants.MILLIS_PER_DAY
  }

  override def remainder(instant: Long): Long = {
    return super.remainder(instant + 3 * DateTimeConstants.MILLIS_PER_DAY)
  }

  override def getMinimumValue: Int = {
    return 1
  }

  def getMaximumValue: Int = {
    return 53
  }

  override def getMaximumValue(instant: Long): Int = {
    val weekyear: Int = iChronology.getWeekyear(instant)
    return iChronology.getWeeksInYear(weekyear)
  }

  override def getMaximumValue(partial: ReadablePartial): Int = {
    if (partial.isSupported(DateTimeFieldType.weekyear)) {
      val weekyear: Int = partial.get(DateTimeFieldType.weekyear)
      return iChronology.getWeeksInYear(weekyear)
    }
    return 53
  }

  override def getMaximumValue(partial: ReadablePartial, values: Array[Int]): Int = {
    val size: Int = partial.size
    {
      var i: Int = 0
      while (i < size) {
        {
          if (partial.getFieldType(i) eq DateTimeFieldType.weekyear) {
            val weekyear: Int = values(i)
            return iChronology.getWeeksInYear(weekyear)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return 53
  }

  protected override def getMaximumValueForSet(instant: Long, value: Int): Int = {
    return if (value > 52) getMaximumValue(instant) else 52
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.weekOfWeekyear
  }
}