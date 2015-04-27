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
package org.joda.time.field

import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.DurationFieldType

/**
 * Abstract datetime field class that defines its own DurationField, which
 * delegates back into this ImpreciseDateTimeField.
 * <p>
 * This DateTimeField is useful for defining DateTimeFields that are composed
 * of imprecise durations. If both duration fields are precise, then a
 * {@link PreciseDateTimeField} should be used instead.
 * <p>
 * When defining imprecise DateTimeFields where a matching DurationField is
 * already available, just extend BaseDateTimeField directly so as not to
 * create redundant DurationField instances.
 * <p>
 * ImpreciseDateTimeField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @see PreciseDateTimeField
 * @since 1.0
 */
@SerialVersionUID(7190739608550251860L)
abstract class ImpreciseDateTimeField extends BaseDateTimeField {
  private[field] final val iUnitMillis: Long = 0L
  private final val iDurationField: DurationField = null

  /**
   * Constructor.
   *
   * @param type  the field type
   * @param unitMillis  the average duration unit milliseconds
   */
  def this(`type`: DateTimeFieldType, unitMillis: Long) {
    this()
    `super`(`type`)
    iUnitMillis = unitMillis
    iDurationField = new ImpreciseDateTimeField#LinkedDurationField(`type`.getDurationType)
  }

  def get(instant: Long): Int

  def set(instant: Long, value: Int): Long

  override def add(instant: Long, value: Int): Long

  override def add(instant: Long, value: Long): Long

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
   * <p>
   * The default implementation call getDifferenceAsLong and converts the
   * return value to an int.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return FieldUtils.safeToInt(getDifferenceAsLong(minuendInstant, subtrahendInstant))
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
   * <p>
   * The default implementation performs a guess-and-check algorithm using
   * getDurationField().getUnitMillis() and the add() method. Subclasses are
   * encouraged to provide a more efficient implementation.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    if (minuendInstant < subtrahendInstant) {
      return -getDifferenceAsLong(subtrahendInstant, minuendInstant)
    }
    var difference: Long = (minuendInstant - subtrahendInstant) / iUnitMillis
    if (add(subtrahendInstant, difference) < minuendInstant) {
      do {
        difference += 1
      } while (add(subtrahendInstant, difference) <= minuendInstant)
      difference -= 1
    }
    else if (add(subtrahendInstant, difference) > minuendInstant) {
      do {
        difference -= 1
      } while (add(subtrahendInstant, difference) > minuendInstant)
    }
    return difference
  }

  final def getDurationField: DurationField = {
    return iDurationField
  }

  def getRangeDurationField: DurationField

  def roundFloor(instant: Long): Long

  protected final def getDurationUnitMillis: Long = {
    return iUnitMillis
  }

  @SerialVersionUID(-203813474600094134L)
  private final class LinkedDurationField extends BaseDurationField {
    private[field] def this(`type`: DurationFieldType) {
      this()
      `super`(`type`)
    }

    def isPrecise: Boolean = {
      return false
    }

    def getUnitMillis: Long = {
      return iUnitMillis
    }

    override def getValue(duration: Long, instant: Long): Int = {
      return ImpreciseDateTimeField.this.getDifference(instant + duration, instant)
    }

    def getValueAsLong(duration: Long, instant: Long): Long = {
      return ImpreciseDateTimeField.this.getDifferenceAsLong(instant + duration, instant)
    }

    def getMillis(value: Int, instant: Long): Long = {
      return ImpreciseDateTimeField.this.add(instant, value) - instant
    }

    def getMillis(value: Long, instant: Long): Long = {
      return ImpreciseDateTimeField.this.add(instant, value) - instant
    }

    def add(instant: Long, value: Int): Long = {
      return ImpreciseDateTimeField.this.add(instant, value)
    }

    def add(instant: Long, value: Long): Long = {
      return ImpreciseDateTimeField.this.add(instant, value)
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      return ImpreciseDateTimeField.this.getDifference(minuendInstant, subtrahendInstant)
    }

    def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      return ImpreciseDateTimeField.this.getDifferenceAsLong(minuendInstant, subtrahendInstant)
    }
  }

}