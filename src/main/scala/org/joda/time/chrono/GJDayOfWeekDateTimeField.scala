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
import org.joda.time.field.PreciseDurationDateTimeField

/**
 * GJDayOfWeekDateTimeField provides time calculations for the
 * day of the week component of time.
 *
 * @since 1.0
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 */
@SerialVersionUID(-3857947176719041436L)
final class GJDayOfWeekDateTimeField extends PreciseDurationDateTimeField {
  private final val iChronology: BasicChronology = null

  /**
   * Restricted constructor.
   */
  private[chrono] def this(chronology: BasicChronology, days: DurationField) {
    this()
    `super`(DateTimeFieldType.dayOfWeek, days)
    iChronology = chronology
  }

  /**
   * Get the value of the specified time instant.
   *
   * @param instant  the time instant in millis to query
   * @return the day of the week extracted from the input
   */
  def get(instant: Long): Int = {
    return iChronology.getDayOfWeek(instant)
  }

  /**
   * Get the textual value of the specified time instant.
   *
   * @param fieldValue  the field value to query
   * @param locale  the locale to use
   * @return the day of the week, such as 'Monday'
   */
  override def getAsText(fieldValue: Int, locale: Locale): String = {
    return GJLocaleSymbols.forLocale(locale).dayOfWeekValueToText(fieldValue)
  }

  /**
   * Get the abbreviated textual value of the specified time instant.
   *
   * @param fieldValue  the field value to query
   * @param locale  the locale to use
   * @return the day of the week, such as 'Mon'
   */
  override def getAsShortText(fieldValue: Int, locale: Locale): String = {
    return GJLocaleSymbols.forLocale(locale).dayOfWeekValueToShortText(fieldValue)
  }

  /**
   * Convert the specified text and locale into a value.
   *
   * @param text  the text to convert
   * @param locale  the locale to convert using
   * @return the value extracted from the text
   * @throws IllegalArgumentException if the text is invalid
   */
  protected override def convertText(text: String, locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).dayOfWeekTextToValue(text)
  }

  def getRangeDurationField: DurationField = {
    return iChronology.weeks
  }

  /**
   * Get the minimum value that this field can have.
   *
   * @return the field's minimum value
   */
  override def getMinimumValue: Int = {
    return DateTimeConstants.MONDAY
  }

  /**
   * Get the maximum value that this field can have.
   *
   * @return the field's maximum value
   */
  def getMaximumValue: Int = {
    return DateTimeConstants.SUNDAY
  }

  /**
   * Get the maximum length of the text returned by this field.
   *
   * @param locale  the locale to use
   * @return the maximum textual length
   */
  override def getMaximumTextLength(locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).getDayOfWeekMaxTextLength
  }

  /**
   * Get the maximum length of the abbreviated text returned by this field.
   *
   * @param locale  the locale to use
   * @return the maximum abbreviated textual length
   */
  override def getMaximumShortTextLength(locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).getDayOfWeekMaxShortTextLength
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return iChronology.dayOfWeek
  }
}