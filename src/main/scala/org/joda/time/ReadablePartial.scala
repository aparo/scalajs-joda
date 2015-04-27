/*
 *  Copyright 2001-2011 Stephen Colebourne
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
package org.joda.time

/**
 * Defines a partial time that does not support every datetime field, and is
 * thus a local time.
 * <p>
 * A {@code ReadablePartial} supports a subset of those fields on the chronology.
 * It cannot be compared to a {@code ReadableInstant}, as it does not fully
 * specify an instant in time. The time it does specify is a local time, and does
 * not include a time zone.
 * <p>
 * A {@code ReadablePartial} can be converted to a {@code ReadableInstant}
 * using the {@code toDateTime} method. This works by providing a full base
 * instant that can be used to 'fill in the gaps' and specify a time zone.
 * <p>
 * {@code ReadablePartial} is {@code Comparable} from v2.0.
 * The comparison is based on the fields, compared in order, from largest to smallest.
 * The first field that is non-equal is used to determine the result.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
trait ReadablePartial extends Comparable[ReadablePartial] {
  /**
   * Gets the number of fields that this partial supports.
   *
   * @return the number of fields supported
   */
  def size: Int

  /**
   * Gets the field type at the specified index.
   *
   * @param index  the index to retrieve
   * @return the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getFieldType(index: Int): DateTimeFieldType

  /**
   * Gets the field at the specified index.
   *
   * @param index  the index to retrieve
   * @return the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getField(index: Int): DateTimeField

  /**
   * Gets the value at the specified index.
   *
   * @param index  the index to retrieve
   * @return the value of the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getValue(index: Int): Int

  /**
   * Gets the chronology of the partial which is never null.
   * <p>
   * The {@link org.joda.Chronology} is the calculation engine behind the partial and
   * provides conversion and validation of the fields in a particular calendar system.
   *
   * @return the chronology, never null
   */
  def getChronology: Chronology

  /**
   * Gets the value of one of the fields.
   * <p>
   * The field type specified must be one of those that is supported by the partial.
   *
   * @param field  a DateTimeFieldType instance that is supported by this partial
   * @return the value of that field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def get(field: DateTimeFieldType): Int

  /**
   * Checks whether the field type specified is supported by this partial.
   *
   * @param field  the field to check, may be null which returns false
   * @return true if the field is supported
   */
  def isSupported(field: DateTimeFieldType): Boolean

  /**
   * Converts this partial to a full datetime by resolving it against another
   * datetime.
   * <p>
   * This method takes the specified datetime and sets the fields from this
   * instant on top. The chronology from the base instant is used.
   * <p>
   * For example, if this partial represents a time, then the result of this
   * method will be the datetime from the specified base instant plus the
   * time from this partial.
   *
   * @param baseInstant  the instant that provides the missing fields, null means now
   * @return the combined datetime
   */
  def toDateTime(baseInstant: ReadableInstant): DateTime

  /**
   * Compares this partial with the specified object for equality based
   * on the supported fields, chronology and values.
   * <p>
   * Two instances of ReadablePartial are equal if they have the same
   * chronology, same field types (in same order) and same values.
   *
   * @param partial  the object to compare to
   * @return true if equal
   */
  override def equals(partial: AnyRef): Boolean

  /**
   * Gets a hash code for the partial that is compatible with the
   * equals method.
   * <p>
   * The formula used must be:
   * <pre>
   * int total = 157;
   * for (int i = 0; i < fields.length; i++) {
   * total = 23 * total + values[i];
   * total = 23 * total + fieldTypes[i].hashCode();
   * }
   * total += chronology.hashCode();
   * return total;
   * </pre>
   *
   * @return a suitable hash code
   */
  override def hashCode: Int

  /**
   * Get the value as a String in a recognisable ISO8601 format, only
   * displaying supported fields.
   * <p>
   * The string output is in ISO8601 format to enable the String
   * constructor to correctly parse it.
   *
   * @return the value as an ISO8601 string
   */
  override def toString: String
}