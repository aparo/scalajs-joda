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
package org.joda.time.chrono

import java.util.Locale
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.IllegalFieldValueException
import org.joda.time.field.BaseDateTimeField
import org.joda.time.field.FieldUtils
import org.joda.time.field.UnsupportedDurationField

/**
 * Provides time calculations for the coptic era component of time.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.2, refactored from CopticEraDateTimeField
 */
object BasicSingleEraDateTimeField {
  /**
   * Value of the era, which will be the same as DateTimeConstants.CE.
   */
  private val ERA_VALUE: Int = DateTimeConstants.CE
}

final class BasicSingleEraDateTimeField extends BaseDateTimeField {
  /**
   * Text value of the era.
   */
  private final val iEraText: String = null

  /**
   * Restricted constructor.
   */
  private[chrono] def this(text: String) {
    this()
    `super`(DateTimeFieldType.era)
    iEraText = text
  }

  /** @inheritDoc*/
  def isLenient: Boolean = {
    return false
  }

  /** @inheritDoc*/
  def get(instant: Long): Int = {
    return BasicSingleEraDateTimeField.ERA_VALUE
  }

  /** @inheritDoc*/
  def set(instant: Long, era: Int): Long = {
    FieldUtils.verifyValueBounds(this, era, BasicSingleEraDateTimeField.ERA_VALUE, BasicSingleEraDateTimeField.ERA_VALUE)
    return instant
  }

  /** @inheritDoc*/
  override def set(instant: Long, text: String, locale: Locale): Long = {
    if ((iEraText == text) == false && ("1" == text) == false) {
      throw new IllegalFieldValueException(DateTimeFieldType.era, text)
    }
    return instant
  }

  /** @inheritDoc*/
  def roundFloor(instant: Long): Long = {
    return Long.MIN_VALUE
  }

  /** @inheritDoc*/
  override def roundCeiling(instant: Long): Long = {
    return Long.MaxValue
  }

  /** @inheritDoc*/
  override def roundHalfFloor(instant: Long): Long = {
    return Long.MIN_VALUE
  }

  /** @inheritDoc*/
  override def roundHalfCeiling(instant: Long): Long = {
    return Long.MIN_VALUE
  }

  /** @inheritDoc*/
  override def roundHalfEven(instant: Long): Long = {
    return Long.MIN_VALUE
  }

  /** @inheritDoc*/
  def getDurationField: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.eras)
  }

  /** @inheritDoc*/
  def getRangeDurationField: DurationField = {
    return null
  }

  /** @inheritDoc*/
  def getMinimumValue: Int = {
    return BasicSingleEraDateTimeField.ERA_VALUE
  }

  /** @inheritDoc*/
  def getMaximumValue: Int = {
    return BasicSingleEraDateTimeField.ERA_VALUE
  }

  /** @inheritDoc*/
  override def getAsText(fieldValue: Int, locale: Locale): String = {
    return iEraText
  }

  /** @inheritDoc*/
  override def getMaximumTextLength(locale: Locale): Int = {
    return iEraText.length
  }
}