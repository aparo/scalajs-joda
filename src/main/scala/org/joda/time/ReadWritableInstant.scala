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
package org.joda.time

/**
 * Defines an instant in the datetime continuum that can be queried and modified.
 * This interface expresses the datetime as milliseconds from 1970-01-01T00:00:00Z.
 * <p>
 * The implementation of this interface will be mutable.
 * It may provide more advanced methods than those in the interface.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
trait ReadWritableInstant extends ReadableInstant {
  /**
   * Sets the value as the number of milliseconds since
   * the epoch, 1970-01-01T00:00:00Z.
   *
   * @param instant  the milliseconds since 1970-01-01T00:00:00Z to set the
   *                 instant to
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMillis(instant: Long)

  /**
   * Sets the millisecond instant of this instant from another.
   * <p>
   * This method does not change the chronology of this instant, just the
   * millisecond instant.
   *
   * @param instant  the instant to use, null means now
   */
  def setMillis(instant: ReadableInstant)

  /**
   * Sets the chronology of the datetime, which has no effect if not applicable.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   * @throws IllegalArgumentException if the value is invalid
   */
  def setChronology(chronology: Chronology)

  /**
   * Sets the time zone of the datetime, changing the chronology and field values.
   * <p>
   * Changing the zone using this method retains the millisecond instant.
   * The millisecond instant is adjusted in the new zone to compensate.
   *
   * chronology. Setting the time zone does not affect the millisecond value
   * of this instant.
   * <p>
   * If the chronology already has this time zone, no change occurs.
   *
   * @param zone  the time zone to use, null means default zone
   * @see #setZoneRetainFields
   */
  def setZone(zone: DateTimeZone)

  /**
   * Sets the time zone of the datetime, changing the chronology and millisecond.
   * <p>
   * Changing the zone using this method retains the field values.
   * The millisecond instant is adjusted in the new zone to compensate.
   * <p>
   * If the chronology already has this time zone, no change occurs.
   *
   * @param zone  the time zone to use, null means default zone
   * @see #setZone
   */
  def setZoneRetainFields(zone: DateTimeZone)

  /**
   * Adds a millisecond duration to this instant.
   * <p>
   * This will typically change the value of ost fields.
   *
   * @param duration  the millis to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def add(duration: Long)

  /**
   * Adds a duration to this instant.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param duration  the duration to add, null means add zero
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(duration: ReadableDuration)

  /**
   * Adds a duration to this instant specifying how many times to add.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param duration  the duration to add, null means add zero
   * @param scalar  direction and amount to add, which may be negative
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(duration: ReadableDuration, scalar: Int)

  /**
   * Adds a period to this instant.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param period  the period to add, null means add zero
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(period: ReadablePeriod)

  /**
   * Adds a period to this instant specifying how many times to add.
   * <p>
   * This will typically change the value of most fields.
   *
   * @param period  the period to add, null means add zero
   * @param scalar  direction and amount to add, which may be negative
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(period: ReadablePeriod, scalar: Int)

  /**
   * Sets the value of one of the fields of the instant, such as hourOfDay.
   *
   * @param type  a field type, usually obtained from DateTimeFieldType, null ignored
   * @param value  the value to set the field to
   * @throws IllegalArgumentException if the value is invalid
   */
  def set(`type`: DateTimeFieldType, value: Int)

  /**
   * Adds to the instant specifying the duration and multiple to add.
   *
   * @param type  a field type, usually obtained from DateTimeFieldType, null ignored
   * @param amount  the amount to add of this duration
   * @throws ArithmeticException if the result exceeds the capacity of the instant
   */
  def add(`type`: DurationFieldType, amount: Int)
}