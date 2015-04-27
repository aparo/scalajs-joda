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

import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.ReadablePartial
import org.joda.time.field.DecoratedDateTimeField
import org.joda.time.field.FieldUtils

/**
 * Provides time calculations for the year of era component of time.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-5961050944769862059L)
final class GJYearOfEraDateTimeField extends DecoratedDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor.
   */
  private[chrono] def this(yearField: DateTimeField, chronology: BasicChronology) {
    this()
    `super`(yearField, DateTimeFieldType.yearOfEra)
    iChronology = chronology
  }

  override def getRangeDurationField: DurationField = {
    return iChronology.eras
  }

  override def get(instant: Long): Int = {
    var year: Int = getWrappedField.get(instant)
    if (year <= 0) {
      year = 1 - year
    }
    return year
  }

  override def add(instant: Long, years: Int): Long = {
    return getWrappedField.add(instant, years)
  }

  override def add(instant: Long, years: Long): Long = {
    return getWrappedField.add(instant, years)
  }

  override def addWrapField(instant: Long, years: Int): Long = {
    return getWrappedField.addWrapField(instant, years)
  }

  override def addWrapField(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], years: Int): Array[Int] = {
    return getWrappedField.addWrapField(instant, fieldIndex, values, years)
  }

  override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getWrappedField.getDifference(minuendInstant, subtrahendInstant)
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  /**
   * Set the year component of the specified time instant.
   *
   * @param instant  the time instant in millis to update.
   * @param year  the year (0,292278994) to update the time to.
   * @return the updated time instant.
   * @throws IllegalArgumentException  if year is invalid.
   */
  override def set(instant: Long, year: Int): Long = {
    FieldUtils.verifyValueBounds(this, year, 1, getMaximumValue)
    if (iChronology.getYear(instant) <= 0) {
      year = 1 - year
    }
    return super.set(instant, year)
  }

  override def getMinimumValue: Int = {
    return 1
  }

  override def getMaximumValue: Int = {
    return getWrappedField.getMaximumValue
  }

  override def roundFloor(instant: Long): Long = {
    return getWrappedField.roundFloor(instant)
  }

  override def roundCeiling(instant: Long): Long = {
    return getWrappedField.roundCeiling(instant)
  }

  override def remainder(instant: Long): Long = {
    return getWrappedField.remainder(instant)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.yearOfEra
  }
}