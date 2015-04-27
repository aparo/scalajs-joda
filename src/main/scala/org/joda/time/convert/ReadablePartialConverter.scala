/*
 *  Copyright 2001-2009 Stephen Colebourne
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
package org.joda.time.convert

import org.joda.time.Chronology
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.ReadablePartial

/**
 * ReadablePartialConverter extracts partial fields and chronology from a ReadablePartial.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
object ReadablePartialConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: ReadablePartialConverter = new ReadablePartialConverter
}

class ReadablePartialConverter extends AbstractConverter with PartialConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the chronology, which is taken from the ReadablePartial.
   *
   * @param object  the ReadablePartial to convert, must not be null
   * @param zone  the specified zone to use, null means default zone
   * @return the chronology, never null
   */
  override def getChronology(`object`: AnyRef, zone: DateTimeZone): Chronology = {
    return getChronology(`object`, null.asInstanceOf[Chronology]).withZone(zone)
  }

  /**
   * Gets the chronology, which is taken from the ReadableInstant.
   * <p>
   * If the passed in chronology is non-null, it is used.
   * Otherwise the chronology from the instant is used.
   *
   * @param object  the ReadablePartial to convert, must not be null
   * @param chrono  the chronology to use, null means use that from object
   * @return the chronology, never null
   */
  override def getChronology(`object`: AnyRef, chrono: Chronology): Chronology = {
    if (chrono == null) {
      chrono = (`object`.asInstanceOf[ReadablePartial]).getChronology
      chrono = DateTimeUtils.getChronology(chrono)
    }
    return chrono
  }

  /**
   * Extracts the values of the partial from an object of this converter's type.
   * The chrono parameter is a hint to the converter, should it require a
   * chronology to aid in conversion.
   *
   * @param fieldSource  a partial that provides access to the fields.
   *                     This partial may be incomplete and only getFieldType(int) should be used
   * @param object  the object to convert
   * @param chrono  the chronology to use, which is the non-null result of getChronology()
   * @return the array of field values that match the fieldSource, must be non-null valid
   * @throws ClassCastException if the object is invalid
   */
  override def getPartialValues(fieldSource: ReadablePartial, `object`: AnyRef, chrono: Chronology): Array[Int] = {
    val input: ReadablePartial = `object`.asInstanceOf[ReadablePartial]
    val size: Int = fieldSource.size
    val values: Array[Int] = new Array[Int](size)
    {
      var i: Int = 0
      while (i < size) {
        {
          values(i) = input.get(fieldSource.getFieldType(i))
        }
        ({
          i += 1; i - 1
        })
      }
    }
    chrono.validate(fieldSource, values)
    return values
  }

  /**
   * Returns ReadableInstant.class.
   *
   * @return ReadableInstant.class
   */
  def getSupportedType: Class[_] = {
    return classOf[ReadablePartial]
  }
}