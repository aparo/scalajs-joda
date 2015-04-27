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

import java.util.Locale
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.field.BaseDateTimeField
import org.joda.time.field.FieldUtils
import org.joda.time.field.UnsupportedDurationField

/**
 * Provides time calculations for the era component of time.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(4240986525305515528L)
final class GJEraDateTimeField extends BaseDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor
   */
  private[chrono] def this(chronology: BasicChronology) {
    this()
    `super`(DateTimeFieldType.era)
    iChronology = chronology
  }

  def isLenient: Boolean = {
    return false
  }

  /**
   * Get the Era component of the specified time instant.
   *
   * @param instant  the time instant in millis to query.
   */
  def get(instant: Long): Int = {
    if (iChronology.getYear(instant) <= 0) {
      return DateTimeConstants.BCE
    }
    else {
      return DateTimeConstants.CE
    }
  }

  override def getAsText(fieldValue: Int, locale: Locale): String = {
    return GJLocaleSymbols.forLocale(locale).eraValueToText(fieldValue)
  }

  /**
   * Set the Era component of the specified time instant.
   *
   * @param instant  the time instant in millis to update.
   * @param era  the era to update the time to.
   * @return the updated time instant.
   * @throws IllegalArgumentException  if era is invalid.
   */
  def set(instant: Long, era: Int): Long = {
    FieldUtils.verifyValueBounds(this, era, DateTimeConstants.BCE, DateTimeConstants.CE)
    val oldEra: Int = get(instant)
    if (oldEra != era) {
      val year: Int = iChronology.getYear(instant)
      return iChronology.setYear(instant, -year)
    }
    else {
      return instant
    }
  }

  override def set(instant: Long, text: String, locale: Locale): Long = {
    return set(instant, GJLocaleSymbols.forLocale(locale).eraTextToValue(text))
  }

  def roundFloor(instant: Long): Long = {
    if (get(instant) == DateTimeConstants.CE) {
      return iChronology.setYear(0, 1)
    }
    else {
      return Long.MIN_VALUE
    }
  }

  override def roundCeiling(instant: Long): Long = {
    if (get(instant) == DateTimeConstants.BCE) {
      return iChronology.setYear(0, 1)
    }
    else {
      return Long.MaxValue
    }
  }

  override def roundHalfFloor(instant: Long): Long = {
    return roundFloor(instant)
  }

  override def roundHalfCeiling(instant: Long): Long = {
    return roundFloor(instant)
  }

  override def roundHalfEven(instant: Long): Long = {
    return roundFloor(instant)
  }

  def getDurationField: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.eras)
  }

  def getRangeDurationField: DurationField = {
    return null
  }

  def getMinimumValue: Int = {
    return DateTimeConstants.BCE
  }

  def getMaximumValue: Int = {
    return DateTimeConstants.CE
  }

  override def getMaximumTextLength(locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).getEraMaxTextLength
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.era
  }
}