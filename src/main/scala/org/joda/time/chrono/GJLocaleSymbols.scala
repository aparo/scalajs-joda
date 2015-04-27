/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.text.DateFormatSymbols
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeUtils
import org.joda.time.IllegalFieldValueException

/**
 * Utility class used by a few of the GJDateTimeFields.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
object GJLocaleSymbols {
  private var cCache: ConcurrentMap[Locale, GJLocaleSymbols] = new ConcurrentHashMap[Locale, GJLocaleSymbols]

  /**
   * Obtains the symbols for a locale.
   *
   * @param locale  the locale, null returns default
   * @return the symbols, not null
   */
  private[chrono] def forLocale(locale: Locale): GJLocaleSymbols = {
    if (locale == null) {
      locale = Locale.getDefault
    }
    var symbols: GJLocaleSymbols = cCache.get(locale)
    if (symbols == null) {
      symbols = new GJLocaleSymbols(locale)
      val oldSymbols: GJLocaleSymbols = cCache.putIfAbsent(locale, symbols)
      if (oldSymbols != null) {
        symbols = oldSymbols
      }
    }
    return symbols
  }

  private def realignMonths(months: Array[String]): Array[String] = {
    val a: Array[String] = new Array[String](13)
    {
      var i: Int = 1
      while (i < 13) {
        {
          a(i) = months(i - 1)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return a
  }

  private def realignDaysOfWeek(daysOfWeek: Array[String]): Array[String] = {
    val a: Array[String] = new Array[String](8)
    {
      var i: Int = 1
      while (i < 8) {
        {
          a(i) = daysOfWeek(if ((i < 7)) i + 1 else 1)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return a
  }

  private def addSymbols(map: TreeMap[String, Integer], symbols: Array[String], integers: Array[Integer]) {
    {
      var i: Int = symbols.length
      while (({
        i -= 1; i
      }) >= 0) {
        val symbol: String = symbols(i)
        if (symbol != null) {
          map.put(symbol, integers(i))
        }
      }
    }
  }

  private def addNumerals(map: TreeMap[String, Integer], start: Int, end: Int, integers: Array[Integer]) {
    {
      var i: Int = start
      while (i <= end) {
        {
          map.put(String.valueOf(i).intern, integers(i))
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }

  private def maxLength(a: Array[String]): Int = {
    var max: Int = 0
    {
      var i: Int = a.length
      while (({
        i -= 1; i
      }) >= 0) {
        val s: String = a(i)
        if (s != null) {
          val len: Int = s.length
          if (len > max) {
            max = len
          }
        }
      }
    }
    return max
  }
}

class GJLocaleSymbols {
  private final val iEras: Array[String] = null
  private final val iDaysOfWeek: Array[String] = null
  private final val iShortDaysOfWeek: Array[String] = null
  private final val iMonths: Array[String] = null
  private final val iShortMonths: Array[String] = null
  private final val iHalfday: Array[String] = null
  private final val iParseEras: TreeMap[String, Integer] = null
  private final val iParseDaysOfWeek: TreeMap[String, Integer] = null
  private final val iParseMonths: TreeMap[String, Integer] = null
  private final val iMaxEraLength: Int = 0
  private final val iMaxDayOfWeekLength: Int = 0
  private final val iMaxShortDayOfWeekLength: Int = 0
  private final val iMaxMonthLength: Int = 0
  private final val iMaxShortMonthLength: Int = 0
  private final val iMaxHalfdayLength: Int = 0

  /**
   * @param locale must not be null
   */
  private def this(locale: Locale) {
    this()
    val dfs: DateFormatSymbols = DateTimeUtils.getDateFormatSymbols(locale)
    iEras = dfs.getEras
    iDaysOfWeek = GJLocaleSymbols.realignDaysOfWeek(dfs.getWeekdays)
    iShortDaysOfWeek = GJLocaleSymbols.realignDaysOfWeek(dfs.getShortWeekdays)
    iMonths = GJLocaleSymbols.realignMonths(dfs.getMonths)
    iShortMonths = GJLocaleSymbols.realignMonths(dfs.getShortMonths)
    iHalfday = dfs.getAmPmStrings
    val integers: Array[Integer] = new Array[Integer](13)
    {
      var i: Int = 0
      while (i < 13) {
        {
          integers(i) = Integer.valueOf(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    iParseEras = new TreeMap[String, Integer](String.CASE_INSENSITIVE_ORDER)
    GJLocaleSymbols.addSymbols(iParseEras, iEras, integers)
    if ("en" == locale.getLanguage) {
      iParseEras.put("BCE", integers(0))
      iParseEras.put("CE", integers(1))
    }
    iParseDaysOfWeek = new TreeMap[String, Integer](String.CASE_INSENSITIVE_ORDER)
    GJLocaleSymbols.addSymbols(iParseDaysOfWeek, iDaysOfWeek, integers)
    GJLocaleSymbols.addSymbols(iParseDaysOfWeek, iShortDaysOfWeek, integers)
    GJLocaleSymbols.addNumerals(iParseDaysOfWeek, 1, 7, integers)
    iParseMonths = new TreeMap[String, Integer](String.CASE_INSENSITIVE_ORDER)
    GJLocaleSymbols.addSymbols(iParseMonths, iMonths, integers)
    GJLocaleSymbols.addSymbols(iParseMonths, iShortMonths, integers)
    GJLocaleSymbols.addNumerals(iParseMonths, 1, 12, integers)
    iMaxEraLength = GJLocaleSymbols.maxLength(iEras)
    iMaxDayOfWeekLength = GJLocaleSymbols.maxLength(iDaysOfWeek)
    iMaxShortDayOfWeekLength = GJLocaleSymbols.maxLength(iShortDaysOfWeek)
    iMaxMonthLength = GJLocaleSymbols.maxLength(iMonths)
    iMaxShortMonthLength = GJLocaleSymbols.maxLength(iShortMonths)
    iMaxHalfdayLength = GJLocaleSymbols.maxLength(iHalfday)
  }

  def eraValueToText(value: Int): String = {
    return iEras(value)
  }

  def eraTextToValue(text: String): Int = {
    val era: Integer = iParseEras.get(text)
    if (era != null) {
      return era.intValue
    }
    throw new IllegalFieldValueException(DateTimeFieldType.era, text)
  }

  def getEraMaxTextLength: Int = {
    return iMaxEraLength
  }

  def monthOfYearValueToText(value: Int): String = {
    return iMonths(value)
  }

  def monthOfYearValueToShortText(value: Int): String = {
    return iShortMonths(value)
  }

  def monthOfYearTextToValue(text: String): Int = {
    val month: Integer = iParseMonths.get(text)
    if (month != null) {
      return month.intValue
    }
    throw new IllegalFieldValueException(DateTimeFieldType.monthOfYear, text)
  }

  def getMonthMaxTextLength: Int = {
    return iMaxMonthLength
  }

  def getMonthMaxShortTextLength: Int = {
    return iMaxShortMonthLength
  }

  def dayOfWeekValueToText(value: Int): String = {
    return iDaysOfWeek(value)
  }

  def dayOfWeekValueToShortText(value: Int): String = {
    return iShortDaysOfWeek(value)
  }

  def dayOfWeekTextToValue(text: String): Int = {
    val day: Integer = iParseDaysOfWeek.get(text)
    if (day != null) {
      return day.intValue
    }
    throw new IllegalFieldValueException(DateTimeFieldType.dayOfWeek, text)
  }

  def getDayOfWeekMaxTextLength: Int = {
    return iMaxDayOfWeekLength
  }

  def getDayOfWeekMaxShortTextLength: Int = {
    return iMaxShortDayOfWeekLength
  }

  def halfdayValueToText(value: Int): String = {
    return iHalfday(value)
  }

  def halfdayTextToValue(text: String): Int = {
    val halfday: Array[String] = iHalfday
    {
      var i: Int = halfday.length
      while (({
        i -= 1; i
      }) >= 0) {
        if (halfday(i).equalsIgnoreCase(text)) {
          return i
        }
      }
    }
    throw new IllegalFieldValueException(DateTimeFieldType.halfdayOfDay, text)
  }

  def getHalfdayMaxTextLength: Int = {
    return iMaxHalfdayLength
  }
}