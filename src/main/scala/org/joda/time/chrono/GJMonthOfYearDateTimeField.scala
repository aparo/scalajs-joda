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

/**
 * Provides time calculations for the month of the year component of time.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-4748157875845286249L)
final class GJMonthOfYearDateTimeField extends BasicMonthOfYearDateTimeField {
  /**
   * Restricted constructor
   */
  private[chrono] def this(chronology: BasicChronology) {
    this()
    `super`(chronology, 2)
  }

  override def getAsText(fieldValue: Int, locale: Locale): String = {
    return GJLocaleSymbols.forLocale(locale).monthOfYearValueToText(fieldValue)
  }

  override def getAsShortText(fieldValue: Int, locale: Locale): String = {
    return GJLocaleSymbols.forLocale(locale).monthOfYearValueToShortText(fieldValue)
  }

  protected override def convertText(text: String, locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).monthOfYearTextToValue(text)
  }

  override def getMaximumTextLength(locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).getMonthMaxTextLength
  }

  override def getMaximumShortTextLength(locale: Locale): Int = {
    return GJLocaleSymbols.forLocale(locale).getMonthMaxShortTextLength
  }
}