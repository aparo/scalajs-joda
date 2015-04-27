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
 * This field is not publicy exposed by ISOChronology, but rather it is used to
 * build the yearOfCentury and centuryOfEra fields. It merely drops the sign of
 * the year.
 *
 * @author Brian S O'Neill
 * @see GJYearOfEraDateTimeField
 * @since 1.0
 */
@SerialVersionUID(7037524068969447317L)
object ISOYearOfEraDateTimeField {
  /**
   * Singleton instance
   */
  private[chrono] val INSTANCE: DateTimeField = new ISOYearOfEraDateTimeField
}

@SerialVersionUID(7037524068969447317L)
class ISOYearOfEraDateTimeField extends DecoratedDateTimeField {
  /**
   * Restricted constructor.
   */
  private def this() {
    this()
    `super`(GregorianChronology.getInstanceUTC.year, DateTimeFieldType.yearOfEra)
  }

  override def getRangeDurationField: DurationField = {
    return GregorianChronology.getInstanceUTC.eras
  }

  override def get(instant: Long): Int = {
    val year: Int = getWrappedField.get(instant)
    return if (year < 0) -year else year
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

  override def set(instant: Long, year: Int): Long = {
    FieldUtils.verifyValueBounds(this, year, 0, getMaximumValue)
    if (getWrappedField.get(instant) < 0) {
      year = -year
    }
    return super.set(instant, year)
  }

  override def getMinimumValue: Int = {
    return 0
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
    return ISOYearOfEraDateTimeField.INSTANCE
  }
}