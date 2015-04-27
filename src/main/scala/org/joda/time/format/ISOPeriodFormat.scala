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
package org.joda.time.format

/**
 * Factory that creates instances of PeriodFormatter for the ISO8601 standard.
 * <p>
 * Period formatting is performed by the {@link PeriodFormatter} class.
 * Three classes provide factory methods to create formatters, and this is one.
 * The others are {@link PeriodFormat} and {@link PeriodFormatterBuilder}.
 * <p>
 * ISOPeriodFormat is thread-safe and immutable, and the formatters it
 * returns are as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see PeriodFormat
 * @see PeriodFormatterBuilder
 */
object ISOPeriodFormat {
  /** Cache of standard format. */
  private var cStandard: PeriodFormatter = null
  /** Cache of alternate months format. */
  private var cAlternate: PeriodFormatter = null
  /** Cache of alternate extended months format. */
  private var cAlternateExtended: PeriodFormatter = null
  /** Cache of alternate weeks format. */
  private var cAlternateWithWeeks: PeriodFormatter = null
  /** Cache of alternate extended weeks format. */
  private var cAlternateExtendedWihWeeks: PeriodFormatter = null

  /**
   * The standard ISO format - PyYmMwWdDThHmMsS.
   * Milliseconds are not output.
   * Note that the ISO8601 standard actually indicates weeks should not
   * be shown if any other field is present and vice versa.
   *
   * @return the formatter
   */
  def standard: PeriodFormatter = {
    if (cStandard == null) {
      cStandard = new PeriodFormatterBuilder().appendLiteral("P").appendYears.appendSuffix("Y").appendMonths.appendSuffix("M").appendWeeks.appendSuffix("W").appendDays.appendSuffix("D").appendSeparatorIfFieldsAfter("T").appendHours.appendSuffix("H").appendMinutes.appendSuffix("M").appendSecondsWithOptionalMillis.appendSuffix("S").toFormatter
    }
    return cStandard
  }

  /**
   * The alternate ISO format, PyyyymmddThhmmss, which excludes weeks.
   * <p>
   * Even if weeks are present in the period, they are not output.
   * Fractional seconds (milliseconds) will appear if required.
   *
   * @return the formatter
   */
  def alternate: PeriodFormatter = {
    if (cAlternate == null) {
      cAlternate = new PeriodFormatterBuilder().appendLiteral("P").printZeroAlways.minimumPrintedDigits(4).appendYears.minimumPrintedDigits(2).appendMonths.appendDays.appendSeparatorIfFieldsAfter("T").appendHours.appendMinutes.appendSecondsWithOptionalMillis.toFormatter
    }
    return cAlternate
  }

  /**
   * The alternate ISO format, Pyyyy-mm-ddThh:mm:ss, which excludes weeks.
   * <p>
   * Even if weeks are present in the period, they are not output.
   * Fractional seconds (milliseconds) will appear if required.
   *
   * @return the formatter
   */
  def alternateExtended: PeriodFormatter = {
    if (cAlternateExtended == null) {
      cAlternateExtended = new PeriodFormatterBuilder().appendLiteral("P").printZeroAlways.minimumPrintedDigits(4).appendYears.appendSeparator("-").minimumPrintedDigits(2).appendMonths.appendSeparator("-").appendDays.appendSeparatorIfFieldsAfter("T").appendHours.appendSeparator(":").appendMinutes.appendSeparator(":").appendSecondsWithOptionalMillis.toFormatter
    }
    return cAlternateExtended
  }

  /**
   * The alternate ISO format, PyyyyWwwddThhmmss, which excludes months.
   * <p>
   * Even if months are present in the period, they are not output.
   * Fractional seconds (milliseconds) will appear if required.
   *
   * @return the formatter
   */
  def alternateWithWeeks: PeriodFormatter = {
    if (cAlternateWithWeeks == null) {
      cAlternateWithWeeks = new PeriodFormatterBuilder().appendLiteral("P").printZeroAlways.minimumPrintedDigits(4).appendYears.minimumPrintedDigits(2).appendPrefix("W").appendWeeks.appendDays.appendSeparatorIfFieldsAfter("T").appendHours.appendMinutes.appendSecondsWithOptionalMillis.toFormatter
    }
    return cAlternateWithWeeks
  }

  /**
   * The alternate ISO format, Pyyyy-Www-ddThh:mm:ss, which excludes months.
   * <p>
   * Even if months are present in the period, they are not output.
   * Fractional seconds (milliseconds) will appear if required.
   *
   * @return the formatter
   */
  def alternateExtendedWithWeeks: PeriodFormatter = {
    if (cAlternateExtendedWihWeeks == null) {
      cAlternateExtendedWihWeeks = new PeriodFormatterBuilder().appendLiteral("P").printZeroAlways.minimumPrintedDigits(4).appendYears.appendSeparator("-").minimumPrintedDigits(2).appendPrefix("W").appendWeeks.appendSeparator("-").appendDays.appendSeparatorIfFieldsAfter("T").appendHours.appendSeparator(":").appendMinutes.appendSeparator(":").appendSecondsWithOptionalMillis.toFormatter
    }
    return cAlternateExtendedWihWeeks
  }
}

class ISOPeriodFormat {
  /**
   * Constructor.
   *
   * @since 1.1 (previously private)
   */
  protected def this() {
    this()
    `super`
  }
}