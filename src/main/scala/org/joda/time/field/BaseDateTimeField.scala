/*
 *  Copyright 2001-2005 Stephen Colebourne
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
package org.joda.time.field

import java.util.Locale
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.IllegalFieldValueException
import org.joda.time.ReadablePartial

/**
 * BaseDateTimeField provides the common behaviour for DateTimeField
 * implementations.
 * <p>
 * This class should generally not be used directly by API users. The
 * DateTimeField class should be used when different kinds of DateTimeField
 * objects are to be referenced.
 * <p>
 * BaseDateTimeField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see DecoratedDateTimeField
 */
abstract class BaseDateTimeField extends DateTimeField {
  /** The field type. */
  private final val iType: DateTimeFieldType = null

  /**
   * Constructor.
   */
  protected def this(`type`: DateTimeFieldType) {
    this()
    `super`
    if (`type` == null) {
      throw new IllegalArgumentException("The type must not be null")
    }
    iType = `type`
  }

  final def getType: DateTimeFieldType = {
    return iType
  }

  final def getName: String = {
    return iType.getName
  }

  /**
   * @return true always
   */
  final def isSupported: Boolean = {
    return true
  }

  /**
   * Get the value of this field from the milliseconds.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @return the value of the field, in the units of the field
   */
  def get(instant: Long): Int

  /**
   * Get the human-readable, text value of this field from the milliseconds.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns getAsText(get(instant), locale).
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @param locale the locale to use for selecting a text symbol, null means default
   * @return the text value of the field
   */
  def getAsText(instant: Long, locale: Locale): String = {
    return getAsText(get(instant), locale)
  }

  /**
   * Get the human-readable, text value of this field from the milliseconds.
   * <p>
   * The default implementation calls {@link #getAsText(long, Locale)}.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @return the text value of the field
   */
  final def getAsText(instant: Long): String = {
    return getAsText(instant, null)
  }

  /**
   * Get the human-readable, text value of this field from a partial instant.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns getAsText(fieldValue, locale).
   *
   * @param partial  the partial instant to query
   * @param fieldValue  the field value of this field, provided for performance
   * @param locale  the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  def getAsText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    return getAsText(fieldValue, locale)
  }

  /**
   * Get the human-readable, text value of this field from a partial instant.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation calls {@link ReadablePartial#get(DateTimeFieldType)}
   * and {@link #getAsText(ReadablePartial, int, Locale)}.
   *
   * @param partial  the partial instant to query
   * @param locale  the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  final def getAsText(partial: ReadablePartial, locale: Locale): String = {
    return getAsText(partial, partial.get(getType), locale)
  }

  /**
   * Get the human-readable, text value of this field from the field value.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns Integer.toString(get(instant)).
   * <p>
   * Note: subclasses that override this method should also override
   * getMaximumTextLength.
   *
   * @param fieldValue  the numeric value to convert to text
   * @param locale the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  def getAsText(fieldValue: Int, locale: Locale): String = {
    return Integer.toString(fieldValue)
  }

  /**
   * Get the human-readable, short text value of this field from the milliseconds.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns getAsShortText(get(instant), locale).
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @param locale the locale to use for selecting a text symbol, null means default
   * @return the text value of the field
   */
  def getAsShortText(instant: Long, locale: Locale): String = {
    return getAsShortText(get(instant), locale)
  }

  /**
   * Get the human-readable, short text value of this field from the milliseconds.
   * <p>
   * The default implementation calls {@link #getAsShortText(long, Locale)}.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @return the text value of the field
   */
  final def getAsShortText(instant: Long): String = {
    return getAsShortText(instant, null)
  }

  /**
   * Get the human-readable, short text value of this field from a partial instant.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns getAsShortText(fieldValue, locale).
   *
   * @param partial  the partial instant to query
   * @param fieldValue  the field value of this field, provided for performance
   * @param locale  the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  def getAsShortText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    return getAsShortText(fieldValue, locale)
  }

  /**
   * Get the human-readable, short text value of this field from a partial instant.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation calls {@link ReadablePartial#get(DateTimeFieldType)}
   * and {@link #getAsText(ReadablePartial, int, Locale)}.
   *
   * @param partial  the partial instant to query
   * @param locale  the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  final def getAsShortText(partial: ReadablePartial, locale: Locale): String = {
    return getAsShortText(partial, partial.get(getType), locale)
  }

  /**
   * Get the human-readable, short text value of this field from the field value.
   * If the specified locale is null, the default locale is used.
   * <p>
   * The default implementation returns getAsText(fieldValue, locale).
   * <p>
   * Note: subclasses that override this method should also override
   * getMaximumShortTextLength.
   *
   * @param fieldValue  the numeric value to convert to text
   * @param locale the locale to use for selecting a text symbol, null for default
   * @return the text value of the field
   */
  def getAsShortText(fieldValue: Int, locale: Locale): String = {
    return getAsText(fieldValue, locale)
  }

  /**
   * Adds a value (which may be negative) to the instant value,
   * overflowing into larger fields if necessary.
   * <p>
   * The value will be added to this field. If the value is too large to be
   * added solely to this field, larger fields will increase as required.
   * Smaller fields should be unaffected, except where the result would be
   * an invalid value for a smaller field. In this case the smaller field is
   * adjusted to be in range.
   * <p>
   * For example, in the ISO chronology:<br>
   * 2000-08-20 add six months is 2001-02-20<br>
   * 2000-08-20 add twenty months is 2002-04-20<br>
   * 2000-08-20 add minus nine months is 1999-11-20<br>
   * 2001-01-31 add one month  is 2001-02-28<br>
   * 2001-01-31 add two months is 2001-03-31<br>
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to add to
   * @param value  the value to add, in the units of the field
   * @return the updated milliseconds
   */
  def add(instant: Long, value: Int): Long = {
    return getDurationField.add(instant, value)
  }

  /**
   * Adds a value (which may be negative) to the instant value,
   * overflowing into larger fields if necessary.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to add to
   * @param value  the long value to add, in the units of the field
   * @return the updated milliseconds
   * @throws IllegalArgumentException if value is too large
   * @see #add(long,int)
   */
  def add(instant: Long, value: Long): Long = {
    return getDurationField.add(instant, value)
  }

  /**
   * Adds a value (which may be negative) to the partial instant,
   * throwing an exception if the maximum size of the instant is reached.
   * <p>
   * The value will be added to this field, overflowing into larger fields
   * if necessary. Smaller fields should be unaffected, except where the
   * result would be an invalid value for a smaller field. In this case the
   * smaller field is adjusted to be in range.
   * <p>
   * Partial instants only contain some fields. This may result in a maximum
   * possible value, such as TimeOfDay being limited to 23:59:59:999. If this
   * limit is breached by the add an exception is thrown.
   * <p>
   * For example, in the ISO chronology:<br>
   * 2000-08-20 add six months is 2000-02-20<br>
   * 2000-08-20 add twenty months is 2000-04-20<br>
   * 2000-08-20 add minus nine months is 2000-11-20<br>
   * 2001-01-31 add one month  is 2001-02-28<br>
   * 2001-01-31 add two months is 2001-03-31<br>
   *
   * @param instant  the partial instant
   * @param fieldIndex  the index of this field in the partial
   * @param values  the values of the partial instant which should be updated
   * @param valueToAdd  the value to add, in the units of the field
   * @return the passed in values
   * @throws IllegalArgumentException if the value is invalid or the maximum instant is reached
   */
  def add(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    if (valueToAdd == 0) {
      return values
    }
    var nextField: DateTimeField = null
    while (valueToAdd > 0) {
      val max: Int = getMaximumValue(instant, values)
      val proposed: Long = values(fieldIndex) + valueToAdd
      if (proposed <= max) {
        values(fieldIndex) = proposed.toInt
        break //todo: break is not supported
      }
      if (nextField == null) {
        if (fieldIndex == 0) {
          throw new IllegalArgumentException("Maximum value exceeded for add")
        }
        nextField = instant.getField(fieldIndex - 1)
        if (getRangeDurationField.getType ne nextField.getDurationField.getType) {
          throw new IllegalArgumentException("Fields invalid for add")
        }
      }
      valueToAdd -= (max + 1) - values(fieldIndex)
      values = nextField.add(instant, fieldIndex - 1, values, 1)
      values(fieldIndex) = getMinimumValue(instant, values)
    }
    while (valueToAdd < 0) {
      val min: Int = getMinimumValue(instant, values)
      val proposed: Long = values(fieldIndex) + valueToAdd
      if (proposed >= min) {
        values(fieldIndex) = proposed.toInt
        break //todo: break is not supported
      }
      if (nextField == null) {
        if (fieldIndex == 0) {
          throw new IllegalArgumentException("Maximum value exceeded for add")
        }
        nextField = instant.getField(fieldIndex - 1)
        if (getRangeDurationField.getType ne nextField.getDurationField.getType) {
          throw new IllegalArgumentException("Fields invalid for add")
        }
      }
      valueToAdd -= (min - 1) - values(fieldIndex)
      values = nextField.add(instant, fieldIndex - 1, values, -1)
      values(fieldIndex) = getMaximumValue(instant, values)
    }
    return set(instant, fieldIndex, values, values(fieldIndex))
  }

  /**
   * Adds a value (which may be negative) to the partial instant,
   * wrapping the whole partial if the maximum size of the partial is reached.
   * <p>
   * The value will be added to this field, overflowing into larger fields
   * if necessary. Smaller fields should be unaffected, except where the
   * result would be an invalid value for a smaller field. In this case the
   * smaller field is adjusted to be in range.
   * <p>
   * Partial instants only contain some fields. This may result in a maximum
   * possible value, such as TimeOfDay normally being limited to 23:59:59:999.
   * If ths limit is reached by the addition, this method will wrap back to
   * 00:00:00.000. In fact, you would generally only use this method for
   * classes that have a limitation such as this.
   * <p>
   * For example, in the ISO chronology:<br>
   * 10:20:30 add 20 minutes is 10:40:30<br>
   * 10:20:30 add 45 minutes is 11:05:30<br>
   * 10:20:30 add 16 hours is 02:20:30<br>
   *
   * @param instant  the partial instant
   * @param fieldIndex  the index of this field in the partial
   * @param values  the values of the partial instant which should be updated
   * @param valueToAdd  the value to add, in the units of the field
   * @return the passed in values
   * @throws IllegalArgumentException if the value is invalid or the maximum instant is reached
   */
  def addWrapPartial(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    if (valueToAdd == 0) {
      return values
    }
    var nextField: DateTimeField = null
    while (valueToAdd > 0) {
      val max: Int = getMaximumValue(instant, values)
      val proposed: Long = values(fieldIndex) + valueToAdd
      if (proposed <= max) {
        values(fieldIndex) = proposed.toInt
        break //todo: break is not supported
      }
      if (nextField == null) {
        if (fieldIndex == 0) {
          valueToAdd -= (max + 1) - values(fieldIndex)
          values(fieldIndex) = getMinimumValue(instant, values)
          continue //todo: continue is not supported
        }
        nextField = instant.getField(fieldIndex - 1)
        if (getRangeDurationField.getType ne nextField.getDurationField.getType) {
          throw new IllegalArgumentException("Fields invalid for add")
        }
      }
      valueToAdd -= (max + 1) - values(fieldIndex)
      values = nextField.addWrapPartial(instant, fieldIndex - 1, values, 1)
      values(fieldIndex) = getMinimumValue(instant, values)
    }
    while (valueToAdd < 0) {
      val min: Int = getMinimumValue(instant, values)
      val proposed: Long = values(fieldIndex) + valueToAdd
      if (proposed >= min) {
        values(fieldIndex) = proposed.toInt
        break //todo: break is not supported
      }
      if (nextField == null) {
        if (fieldIndex == 0) {
          valueToAdd -= (min - 1) - values(fieldIndex)
          values(fieldIndex) = getMaximumValue(instant, values)
          continue //todo: continue is not supported
        }
        nextField = instant.getField(fieldIndex - 1)
        if (getRangeDurationField.getType ne nextField.getDurationField.getType) {
          throw new IllegalArgumentException("Fields invalid for add")
        }
      }
      valueToAdd -= (min - 1) - values(fieldIndex)
      values = nextField.addWrapPartial(instant, fieldIndex - 1, values, -1)
      values(fieldIndex) = getMaximumValue(instant, values)
    }
    return set(instant, fieldIndex, values, values(fieldIndex))
  }

  /**
   * Adds a value (which may be negative) to the instant value,
   * wrapping within this field.
   * <p>
   * The value will be added to this field. If the value is too large to be
   * added solely to this field then it wraps. Larger fields are always
   * unaffected. Smaller fields should be unaffected, except where the
   * result would be an invalid value for a smaller field. In this case the
   * smaller field is adjusted to be in range.
   * <p>
   * For example, in the ISO chronology:<br>
   * 2000-08-20 addWrapField six months is 2000-02-20<br>
   * 2000-08-20 addWrapField twenty months is 2000-04-20<br>
   * 2000-08-20 addWrapField minus nine months is 2000-11-20<br>
   * 2001-01-31 addWrapField one month  is 2001-02-28<br>
   * 2001-01-31 addWrapField two months is 2001-03-31<br>
   * <p>
   * The default implementation internally calls set. Subclasses are
   * encouraged to provide a more efficient implementation.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to add to
   * @param value  the value to add, in the units of the field
   * @return the updated milliseconds
   */
  def addWrapField(instant: Long, value: Int): Long = {
    val current: Int = get(instant)
    val wrapped: Int = FieldUtils.getWrappedValue(current, value, getMinimumValue(instant), getMaximumValue(instant))
    return set(instant, wrapped)
  }

  /**
   * Adds a value (which may be negative) to the partial instant,
   * wrapping within this field.
   * <p>
   * The value will be added to this field. If the value is too large to be
   * added solely to this field then it wraps. Larger fields are always
   * unaffected. Smaller fields should be unaffected, except where the
   * result would be an invalid value for a smaller field. In this case the
   * smaller field is adjusted to be in range.
   * <p>
   * For example, in the ISO chronology:<br>
   * 2000-08-20 addWrapField six months is 2000-02-20<br>
   * 2000-08-20 addWrapField twenty months is 2000-04-20<br>
   * 2000-08-20 addWrapField minus nine months is 2000-11-20<br>
   * 2001-01-31 addWrapField one month  is 2001-02-28<br>
   * 2001-01-31 addWrapField two months is 2001-03-31<br>
   * <p>
   * The default implementation internally calls set. Subclasses are
   * encouraged to provide a more efficient implementation.
   *
   * @param instant  the partial instant
   * @param fieldIndex  the index of this field in the instant
   * @param values  the values of the partial instant which should be updated
   * @param valueToAdd  the value to add, in the units of the field
   * @return the passed in values
   * @throws IllegalArgumentException if the value is invalid
   */
  def addWrapField(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    val current: Int = values(fieldIndex)
    val wrapped: Int = FieldUtils.getWrappedValue(current, valueToAdd, getMinimumValue(instant), getMaximumValue(instant))
    return set(instant, fieldIndex, values, wrapped)
  }

  /**
   * Computes the difference between two instants, as measured in the units
   * of this field. Any fractional units are dropped from the result. Calling
   * getDifference reverses the effect of calling add. In the following code:
   *
   * <pre>
   * long instant = ...
   * int v = ...
   * int age = getDifference(add(instant, v), instant);
   * </pre>
   *
   * The value 'age' is the same as the value 'v'.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getDurationField.getDifference(minuendInstant, subtrahendInstant)
  }

  /**
   * Computes the difference between two instants, as measured in the units
   * of this field. Any fractional units are dropped from the result. Calling
   * getDifference reverses the effect of calling add. In the following code:
   *
   * <pre>
   * long instant = ...
   * long v = ...
   * long age = getDifferenceAsLong(add(instant, v), instant);
   * </pre>
   *
   * The value 'age' is the same as the value 'v'.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getDurationField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  /**
   * Sets a value in the milliseconds supplied.
   * <p>
   * The value of this field will be set.
   * If the value is invalid, an exception if thrown.
   * <p>
   * If setting this field would make other fields invalid, then those fields
   * may be changed. For example if the current date is the 31st January, and
   * the month is set to February, the day would be invalid. Instead, the day
   * would be changed to the closest value - the 28th/29th February as appropriate.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to set in
   * @param value  the value to set, in the units of the field
   * @return the updated milliseconds
   * @throws IllegalArgumentException if the value is invalid
   */
  def set(instant: Long, value: Int): Long

  /**
   * Sets a value using the specified partial instant.
   * <p>
   * The value of this field (specified by the index) will be set.
   * If the value is invalid, an exception if thrown.
   * <p>
   * If setting this field would make other fields invalid, then those fields
   * may be changed. For example if the current date is the 31st January, and
   * the month is set to February, the day would be invalid. Instead, the day
   * would be changed to the closest value - the 28th/29th February as appropriate.
   *
   * @param partial  the partial instant
   * @param fieldIndex  the index of this field in the instant
   * @param values  the values to update
   * @param newValue  the value to set, in the units of the field
   * @return the updated values
   * @throws IllegalArgumentException if the value is invalid
   */
  def set(partial: ReadablePartial, fieldIndex: Int, values: Array[Int], newValue: Int): Array[Int] = {
    FieldUtils.verifyValueBounds(this, newValue, getMinimumValue(partial, values), getMaximumValue(partial, values))
    values(fieldIndex) = newValue
    {
      var i: Int = fieldIndex + 1
      while (i < partial.size) {
        {
          val field: DateTimeField = partial.getField(i)
          if (values(i) > field.getMaximumValue(partial, values)) {
            values(i) = field.getMaximumValue(partial, values)
          }
          if (values(i) < field.getMinimumValue(partial, values)) {
            values(i) = field.getMinimumValue(partial, values)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return values
  }

  /**
   * Sets a value in the milliseconds supplied from a human-readable, text value.
   * If the specified locale is null, the default locale is used.
   * <p>
   * This implementation uses <code>convertText(String, Locale)</code> and
   * {@link #set(long, int)}.
   * <p>
   * Note: subclasses that override this method should also override
   * getAsText.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to set in
   * @param text  the text value to set
   * @param locale the locale to use for selecting a text symbol, null for default
   * @return the updated milliseconds
   * @throws IllegalArgumentException if the text value is invalid
   */
  def set(instant: Long, text: String, locale: Locale): Long = {
    val value: Int = convertText(text, locale)
    return set(instant, value)
  }

  /**
   * Sets a value in the milliseconds supplied from a human-readable, text value.
   * <p>
   * This implementation uses {@link #set(long, String, Locale)}.
   * <p>
   * Note: subclasses that override this method should also override getAsText.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to set in
   * @param text  the text value to set
   * @return the updated milliseconds
   * @throws IllegalArgumentException if the text value is invalid
   */
  final def set(instant: Long, text: String): Long = {
    return set(instant, text, null)
  }

  /**
   * Sets a value in the milliseconds supplied from a human-readable, text value.
   * If the specified locale is null, the default locale is used.
   * <p>
   * This implementation uses <code>convertText(String, Locale)</code> and
   * {@link #set(ReadablePartial, int, int[], int)}.
   *
   * @param instant  the partial instant
   * @param fieldIndex  the index of this field in the instant
   * @param values  the values of the partial instant which should be updated
   * @param text  the text value to set
   * @param locale the locale to use for selecting a text symbol, null for default
   * @return the passed in values
   * @throws IllegalArgumentException if the text value is invalid
   */
  def set(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], text: String, locale: Locale): Array[Int] = {
    val value: Int = convertText(text, locale)
    return set(instant, fieldIndex, values, value)
  }

  /**
   * Convert the specified text and locale into a value.
   *
   * @param text  the text to convert
   * @param locale  the locale to convert using
   * @return the value extracted from the text
   * @throws IllegalArgumentException if the text is invalid
   */
  protected def convertText(text: String, locale: Locale): Int = {
    try {
      return text.toInt
    }
    catch {
      case ex: NumberFormatException => {
        throw new IllegalFieldValueException(getType, text)
      }
    }
  }

  /**
   * Returns the duration per unit value of this field. For example, if this
   * field represents "hour of day", then the unit duration is an hour.
   *
   * @return the duration of this field, or UnsupportedDurationField if field
   *         has no duration
   */
  def getDurationField: DurationField

  /**
   * Returns the range duration of this field. For example, if this field
   * represents "hour of day", then the range duration is a day.
   *
   * @return the range duration of this field, or null if field has no range
   */
  def getRangeDurationField: DurationField

  /**
   * Returns whether this field is 'leap' for the specified instant.
   * <p>
   * For example, a leap year would return true, a non leap year would return
   * false.
   * <p>
   * This implementation returns false.
   *
   * @return true if the field is 'leap'
   */
  def isLeap(instant: Long): Boolean = {
    return false
  }

  /**
   * Gets the amount by which this field is 'leap' for the specified instant.
   * <p>
   * For example, a leap year would return one, a non leap year would return
   * zero.
   * <p>
   * This implementation returns zero.
   */
  def getLeapAmount(instant: Long): Int = {
    return 0
  }

  /**
   * If this field were to leap, then it would be in units described by the
   * returned duration. If this field doesn't ever leap, null is returned.
   * <p>
   * This implementation returns null.
   */
  def getLeapDurationField: DurationField = {
    return null
  }

  /**
   * Get the minimum allowable value for this field.
   *
   * @return the minimum valid value for this field, in the units of the
   *         field
   */
  def getMinimumValue: Int

  /**
   * Get the minimum value for this field evaluated at the specified time.
   * <p>
   * This implementation returns the same as {@link #getMinimumValue()}.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @return the minimum value for this field, in the units of the field
   */
  def getMinimumValue(instant: Long): Int = {
    return getMinimumValue
  }

  /**
   * Get the minimum value for this field evaluated at the specified instant.
   * <p>
   * This implementation returns the same as {@link #getMinimumValue()}.
   *
   * @param instant  the partial instant to query
   * @return the minimum value for this field, in the units of the field
   */
  def getMinimumValue(instant: ReadablePartial): Int = {
    return getMinimumValue
  }

  /**
   * Get the minimum value for this field using the partial instant and
   * the specified values.
   * <p>
   * This implementation returns the same as {@link #getMinimumValue(ReadablePartial)}.
   *
   * @param instant  the partial instant to query
   * @param values  the values to use
   * @return the minimum value for this field, in the units of the field
   */
  def getMinimumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return getMinimumValue(instant)
  }

  /**
   * Get the maximum allowable value for this field.
   *
   * @return the maximum valid value for this field, in the units of the
   *         field
   */
  def getMaximumValue: Int

  /**
   * Get the maximum value for this field evaluated at the specified time.
   * <p>
   * This implementation returns the same as {@link #getMaximumValue()}.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to query
   * @return the maximum value for this field, in the units of the field
   */
  def getMaximumValue(instant: Long): Int = {
    return getMaximumValue
  }

  /**
   * Get the maximum value for this field evaluated at the specified instant.
   * <p>
   * This implementation returns the same as {@link #getMaximumValue()}.
   *
   * @param instant  the partial instant to query
   * @return the maximum value for this field, in the units of the field
   */
  def getMaximumValue(instant: ReadablePartial): Int = {
    return getMaximumValue
  }

  /**
   * Get the maximum value for this field using the partial instant and
   * the specified values.
   * <p>
   * This implementation returns the same as {@link #getMaximumValue(ReadablePartial)}.
   *
   * @param instant  the partial instant to query
   * @param values  the values to use
   * @return the maximum value for this field, in the units of the field
   */
  def getMaximumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return getMaximumValue(instant)
  }

  /**
   * Get the maximum text value for this field. The default implementation
   * returns the equivalent of Integer.toString(getMaximumValue()).length().
   *
   * @param locale  the locale to use for selecting a text symbol
   * @return the maximum text length
   */
  def getMaximumTextLength(locale: Locale): Int = {
    val max: Int = getMaximumValue
    if (max >= 0) {
      if (max < 10) {
        return 1
      }
      else if (max < 100) {
        return 2
      }
      else if (max < 1000) {
        return 3
      }
    }
    return Integer.toString(max).length
  }

  /**
   * Get the maximum short text value for this field. The default
   * implementation returns getMaximumTextLength().
   *
   * @param locale  the locale to use for selecting a text symbol
   * @return the maximum short text length
   */
  def getMaximumShortTextLength(locale: Locale): Int = {
    return getMaximumTextLength(locale)
  }

  /**
   * Round to the lowest whole unit of this field. After rounding, the value
   * of this field and all fields of a higher magnitude are retained. The
   * fractional millis that cannot be expressed in whole increments of this
   * field are set to minimum.
   * <p>
   * For example, a datetime of 2002-11-02T23:34:56.789, rounded to the
   * lowest whole hour is 2002-11-02T23:00:00.000.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to round
   * @return rounded milliseconds
   */
  def roundFloor(instant: Long): Long

  /**
   * Round to the highest whole unit of this field. The value of this field
   * and all fields of a higher magnitude may be incremented in order to
   * achieve this result. The fractional millis that cannot be expressed in
   * whole increments of this field are set to minimum.
   * <p>
   * For example, a datetime of 2002-11-02T23:34:56.789, rounded to the
   * highest whole hour is 2002-11-03T00:00:00.000.
   * <p>
   * The default implementation calls roundFloor, and if the instant is
   * modified as a result, adds one field unit. Subclasses are encouraged to
   * provide a more efficient implementation.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to round
   * @return rounded milliseconds
   */
  def roundCeiling(instant: Long): Long = {
    val newInstant: Long = roundFloor(instant)
    if (newInstant != instant) {
      instant = add(newInstant, 1)
    }
    return instant
  }

  /**
   * Round to the nearest whole unit of this field. If the given millisecond
   * value is closer to the floor or is exactly halfway, this function
   * behaves like roundFloor. If the millisecond value is closer to the
   * ceiling, this function behaves like roundCeiling.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to round
   * @return rounded milliseconds
   */
  def roundHalfFloor(instant: Long): Long = {
    val floor: Long = roundFloor(instant)
    val ceiling: Long = roundCeiling(instant)
    val diffFromFloor: Long = instant - floor
    val diffToCeiling: Long = ceiling - instant
    if (diffFromFloor <= diffToCeiling) {
      return floor
    }
    else {
      return ceiling
    }
  }

  /**
   * Round to the nearest whole unit of this field. If the given millisecond
   * value is closer to the floor, this function behaves like roundFloor. If
   * the millisecond value is closer to the ceiling or is exactly halfway,
   * this function behaves like roundCeiling.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to round
   * @return rounded milliseconds
   */
  def roundHalfCeiling(instant: Long): Long = {
    val floor: Long = roundFloor(instant)
    val ceiling: Long = roundCeiling(instant)
    val diffFromFloor: Long = instant - floor
    val diffToCeiling: Long = ceiling - instant
    if (diffToCeiling <= diffFromFloor) {
      return ceiling
    }
    else {
      return floor
    }
  }

  /**
   * Round to the nearest whole unit of this field. If the given millisecond
   * value is closer to the floor, this function behaves like roundFloor. If
   * the millisecond value is closer to the ceiling, this function behaves
   * like roundCeiling.
   * <p>
   * If the millisecond value is exactly halfway between the floor and
   * ceiling, the ceiling is chosen over the floor only if it makes this
   * field's value even.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to round
   * @return rounded milliseconds
   */
  def roundHalfEven(instant: Long): Long = {
    val floor: Long = roundFloor(instant)
    val ceiling: Long = roundCeiling(instant)
    val diffFromFloor: Long = instant - floor
    val diffToCeiling: Long = ceiling - instant
    if (diffFromFloor < diffToCeiling) {
      return floor
    }
    else if (diffToCeiling < diffFromFloor) {
      return ceiling
    }
    else {
      if ((get(ceiling) & 1) == 0) {
        return ceiling
      }
      return floor
    }
  }

  /**
   * Returns the fractional duration milliseconds of this field. In other
   * words, calling remainder returns the duration that roundFloor would
   * subtract.
   * <p>
   * For example, on a datetime of 2002-11-02T23:34:56.789, the remainder by
   * hour is 34 minutes and 56.789 seconds.
   * <p>
   * The default implementation computes
   * <code>instant - roundFloor(instant)</code>. Subclasses are encouraged to
   * provide a more efficient implementation.
   *
   * @param instant the milliseconds from 1970-01-01T00:00:00Z to get the
   *                remainder
   * @return remainder duration, in milliseconds
   */
  def remainder(instant: Long): Long = {
    return instant - roundFloor(instant)
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  def toString: String = {
    return "DateTimeField[" + getName + ']'
  }
}