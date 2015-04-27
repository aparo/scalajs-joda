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
package org.joda.time.format

import java.io.IOException
import java.util.ArrayList
import java.util.List
import java.util.Locale
import java.util.Map
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.MutableDateTime
import org.joda.time.MutableDateTime.Property
import org.joda.time.ReadablePartial
import org.joda.time.field.MillisDurationField
import org.joda.time.field.PreciseDateTimeField

/**
 * Factory that creates complex instances of DateTimeFormatter via method calls.
 * <p>
 * Datetime formatting is performed by the {@link DateTimeFormatter} class.
 * Three classes provide factory methods to create formatters, and this is one.
 * The others are {@link DateTimeFormat} and {@link ISODateTimeFormat}.
 * <p>
 * DateTimeFormatterBuilder is used for constructing formatters which are then
 * used to print or parse. The formatters are built by appending specific fields
 * or other formatters to an instance of this builder.
 * <p>
 * For example, a formatter that prints month and year, like "January 1970",
 * can be constructed as follows:
 * <p>
 * <pre>
 * DateTimeFormatter monthAndYear = new DateTimeFormatterBuilder()
 * .appendMonthOfYearText()
 * .appendLiteral(' ')
 * .appendYear(4, 4)
 * .toFormatter();
 * </pre>
 * <p>
 * DateTimeFormatterBuilder itself is mutable and not thread-safe, but the
 * formatters that it builds are thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @author Fredrik Borgh
 * @since 1.0
 * @see DateTimeFormat
 * @see ISODateTimeFormat
 */
object DateTimeFormatterBuilder {
  @throws(classOf[IOException])
  private[format] def appendUnknownString(appendable: Appendable, len: Int) {
    {
      var i: Int = len
      while (({
        i -= 1; i
      }) >= 0) {
        appendable.append('\ufffd')
      }
    }
  }

  private[format] class CharacterLiteral extends InternalPrinter with InternalParser {
    private final val iValue: Char = 0

    private[format] def this(value: Char) {
      this()
      `super`
      iValue = value
    }

    def estimatePrintedLength: Int = {
      return 1
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      appendable.append(iValue)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      appendable.append(iValue)
    }

    def estimateParsedLength: Int = {
      return 1
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      if (position >= text.length) {
        return ~position
      }
      var a: Char = text.charAt(position)
      var b: Char = iValue
      if (a != b) {
        a = Character.toUpperCase(a)
        b = Character.toUpperCase(b)
        if (a != b) {
          a = Character.toLowerCase(a)
          b = Character.toLowerCase(b)
          if (a != b) {
            return ~position
          }
        }
      }
      return position + 1
    }
  }

  private[format] class StringLiteral extends InternalPrinter with InternalParser {
    private final val iValue: String = null

    private[format] def this(value: String) {
      this()
      `super`
      iValue = value
    }

    def estimatePrintedLength: Int = {
      return iValue.length
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      appendable.append(iValue)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      appendable.append(iValue)
    }

    def estimateParsedLength: Int = {
      return iValue.length
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      if (csStartsWithIgnoreCase(text, position, iValue)) {
        return position + iValue.length
      }
      return ~position
    }
  }

  private[format] abstract class NumberFormatter extends InternalPrinter with InternalParser {
    protected final val iFieldType: DateTimeFieldType = null
    protected final val iMaxParsedDigits: Int = 0
    protected final val iSigned: Boolean = false

    private[format] def this(fieldType: DateTimeFieldType, maxParsedDigits: Int, signed: Boolean) {
      this()
      `super`
      iFieldType = fieldType
      iMaxParsedDigits = maxParsedDigits
      iSigned = signed
    }

    def estimateParsedLength: Int = {
      return iMaxParsedDigits
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      var limit: Int = Math.min(iMaxParsedDigits, text.length - position)
      var negative: Boolean = false
      var length: Int = 0
      while (length < limit) {
        var c: Char = text.charAt(position + length)
        if (length == 0 && (c == '-' || c == '+') && iSigned) {
          negative = c == '-'
          if (length + 1 >= limit || (({
            c = text.charAt(position + length + 1); c
          })) < '0' || c > '9') {
            break //todo: break is not supported
          }
          if (negative) {
            length += 1
          }
          else {
            position += 1
          }
          limit = Math.min(limit + 1, text.length - position)
          continue //todo: continue is not supported
        }
        if (c < '0' || c > '9') {
          break //todo: break is not supported
        }
        length += 1
      }
      if (length == 0) {
        return ~position
      }
      var value: Int = 0
      if (length >= 9) {
        value = text.subSequence(position, position += length).toString.toInt
      }
      else {
        var i: Int = position
        if (negative) {
          i += 1
        }
        try {
          value = text.charAt(({
            i += 1; i - 1
          })) - '0'
        }
        catch {
          case e: StringIndexOutOfBoundsException => {
            return ~position
          }
        }
        position += length
        while (i < position) {
          value = ((value << 3) + (value << 1)) + text.charAt(({
            i += 1; i - 1
          })) - '0'
        }
        if (negative) {
          value = -value
        }
      }
      bucket.saveField(iFieldType, value)
      return position
    }
  }

  private[format] class UnpaddedNumber extends NumberFormatter {
    protected def this(fieldType: DateTimeFieldType, maxParsedDigits: Int, signed: Boolean) {
      this()
      `super`(fieldType, maxParsedDigits, signed)
    }

    def estimatePrintedLength: Int = {
      return iMaxParsedDigits
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      try {
        val field: DateTimeField = iFieldType.getField(chrono)
        FormatUtils.appendUnpaddedInteger(appendable, field.get(instant))
      }
      catch {
        case e: RuntimeException => {
          appendable.append('\ufffd')
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      if (partial.isSupported(iFieldType)) {
        try {
          FormatUtils.appendUnpaddedInteger(appendable, partial.get(iFieldType))
        }
        catch {
          case e: RuntimeException => {
            appendable.append('\ufffd')
          }
        }
      }
      else {
        appendable.append('\ufffd')
      }
    }
  }

  private[format] class PaddedNumber extends NumberFormatter {
    protected final val iMinPrintedDigits: Int = 0

    protected def this(fieldType: DateTimeFieldType, maxParsedDigits: Int, signed: Boolean, minPrintedDigits: Int) {
      this()
      `super`(fieldType, maxParsedDigits, signed)
      iMinPrintedDigits = minPrintedDigits
    }

    def estimatePrintedLength: Int = {
      return iMaxParsedDigits
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      try {
        val field: DateTimeField = iFieldType.getField(chrono)
        FormatUtils.appendPaddedInteger(appendable, field.get(instant), iMinPrintedDigits)
      }
      catch {
        case e: RuntimeException => {
          appendUnknownString(appendable, iMinPrintedDigits)
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      if (partial.isSupported(iFieldType)) {
        try {
          FormatUtils.appendPaddedInteger(appendable, partial.get(iFieldType), iMinPrintedDigits)
        }
        catch {
          case e: RuntimeException => {
            appendUnknownString(appendable, iMinPrintedDigits)
          }
        }
      }
      else {
        appendUnknownString(appendable, iMinPrintedDigits)
      }
    }
  }

  private[format] class FixedNumber extends PaddedNumber {
    protected def this(fieldType: DateTimeFieldType, numDigits: Int, signed: Boolean) {
      this()
      `super`(fieldType, numDigits, signed, numDigits)
    }

    override def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val newPos: Int = super.parseInto(bucket, text, position)
      if (newPos < 0) {
        return newPos
      }
      var expectedPos: Int = position + iMaxParsedDigits
      if (newPos != expectedPos) {
        if (iSigned) {
          val c: Char = text.charAt(position)
          if (c == '-' || c == '+') {
            expectedPos += 1
          }
        }
        if (newPos > expectedPos) {
          return ~(expectedPos + 1)
        }
        else if (newPos < expectedPos) {
          return ~newPos
        }
      }
      return newPos
    }
  }

  private[format] class TwoDigitYear extends InternalPrinter with InternalParser {
    /** The field to print/parse. */
    private final val iType: DateTimeFieldType = null
    /** The pivot year. */
    private final val iPivot: Int = 0
    private final val iLenientParse: Boolean = false

    private[format] def this(`type`: DateTimeFieldType, pivot: Int, lenientParse: Boolean) {
      this()
      `super`
      iType = `type`
      iPivot = pivot
      iLenientParse = lenientParse
    }

    def estimateParsedLength: Int = {
      return if (iLenientParse) 4 else 2
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      var limit: Int = text.length - position
      if (!iLenientParse) {
        limit = Math.min(2, limit)
        if (limit < 2) {
          return ~position
        }
      }
      else {
        var hasSignChar: Boolean = false
        var negative: Boolean = false
        var length: Int = 0
        while (length < limit) {
          val c: Char = text.charAt(position + length)
          if (length == 0 && (c == '-' || c == '+')) {
            hasSignChar = true
            negative = c == '-'
            if (negative) {
              length += 1
            }
            else {
              position += 1
              limit -= 1
            }
            continue //todo: continue is not supported
          }
          if (c < '0' || c > '9') {
            break //todo: break is not supported
          }
          length += 1
        }
        if (length == 0) {
          return ~position
        }
        if (hasSignChar || length != 2) {
          var value: Int = 0
          if (length >= 9) {
            value = text.subSequence(position, position += length).toString.toInt
          }
          else {
            var i: Int = position
            if (negative) {
              i += 1
            }
            try {
              value = text.charAt(({
                i += 1; i - 1
              })) - '0'
            }
            catch {
              case e: StringIndexOutOfBoundsException => {
                return ~position
              }
            }
            position += length
            while (i < position) {
              value = ((value << 3) + (value << 1)) + text.charAt(({
                i += 1; i - 1
              })) - '0'
            }
            if (negative) {
              value = -value
            }
          }
          bucket.saveField(iType, value)
          return position
        }
      }
      var year: Int = 0
      var c: Char = text.charAt(position)
      if (c < '0' || c > '9') {
        return ~position
      }
      year = c - '0'
      c = text.charAt(position + 1)
      if (c < '0' || c > '9') {
        return ~position
      }
      year = ((year << 3) + (year << 1)) + c - '0'
      var pivot: Int = iPivot
      if (bucket.getPivotYear != null) {
        pivot = bucket.getPivotYear.intValue
      }
      val low: Int = pivot - 50
      var t: Int = 0
      if (low >= 0) {
        t = low % 100
      }
      else {
        t = 99 + ((low + 1) % 100)
      }
      year += low + (if ((year < t)) 100 else 0) - t
      bucket.saveField(iType, year)
      return position + 2
    }

    def estimatePrintedLength: Int = {
      return 2
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      val year: Int = getTwoDigitYear(instant, chrono)
      if (year < 0) {
        appendable.append('\ufffd')
        appendable.append('\ufffd')
      }
      else {
        FormatUtils.appendPaddedInteger(appendable, year, 2)
      }
    }

    private def getTwoDigitYear(instant: Long, chrono: Chronology): Int = {
      try {
        var year: Int = iType.getField(chrono).get(instant)
        if (year < 0) {
          year = -year
        }
        return year % 100
      }
      catch {
        case e: RuntimeException => {
          return -1
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      val year: Int = getTwoDigitYear(partial)
      if (year < 0) {
        appendable.append('\ufffd')
        appendable.append('\ufffd')
      }
      else {
        FormatUtils.appendPaddedInteger(appendable, year, 2)
      }
    }

    private def getTwoDigitYear(partial: ReadablePartial): Int = {
      if (partial.isSupported(iType)) {
        try {
          var year: Int = partial.get(iType)
          if (year < 0) {
            year = -year
          }
          return year % 100
        }
        catch {
          case e: RuntimeException => {
          }
        }
      }
      return -1
    }
  }

  private[format] object TextField {
    private var cParseCache: Map[Locale, Map[DateTimeFieldType, Array[AnyRef]]] = new ConcurrentHashMap[Locale, Map[DateTimeFieldType, Array[AnyRef]]]
  }

  private[format] class TextField extends InternalPrinter with InternalParser {
    private final val iFieldType: DateTimeFieldType = null
    private final val iShort: Boolean = false

    private[format] def this(fieldType: DateTimeFieldType, isShort: Boolean) {
      this()
      `super`
      iFieldType = fieldType
      iShort = isShort
    }

    def estimatePrintedLength: Int = {
      return if (iShort) 6 else 20
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      try {
        appendable.append(print(instant, chrono, locale))
      }
      catch {
        case e: RuntimeException => {
          appendable.append('\ufffd')
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      try {
        appendable.append(print(partial, locale))
      }
      catch {
        case e: RuntimeException => {
          appendable.append('\ufffd')
        }
      }
    }

    private def print(instant: Long, chrono: Chronology, locale: Locale): String = {
      val field: DateTimeField = iFieldType.getField(chrono)
      if (iShort) {
        return field.getAsShortText(instant, locale)
      }
      else {
        return field.getAsText(instant, locale)
      }
    }

    private def print(partial: ReadablePartial, locale: Locale): String = {
      if (partial.isSupported(iFieldType)) {
        val field: DateTimeField = iFieldType.getField(partial.getChronology)
        if (iShort) {
          return field.getAsShortText(partial, locale)
        }
        else {
          return field.getAsText(partial, locale)
        }
      }
      else {
        return "\ufffd"
      }
    }

    def estimateParsedLength: Int = {
      return estimatePrintedLength
    }

    @SuppressWarnings(Array("unchecked")) def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val locale: Locale = bucket.getLocale
      var validValues: Map[String, Boolean] = null
      var maxLength: Int = 0
      var innerMap: Map[DateTimeFieldType, Array[AnyRef]] = TextField.cParseCache.get(locale)
      if (innerMap == null) {
        innerMap = new ConcurrentHashMap[DateTimeFieldType, Array[AnyRef]]
        TextField.cParseCache.put(locale, innerMap)
      }
      var array: Array[AnyRef] = innerMap.get(iFieldType)
      if (array == null) {
        validValues = new ConcurrentHashMap[String, Boolean](32)
        val dt: MutableDateTime = new MutableDateTime(0L, DateTimeZone.UTC)
        val property: MutableDateTime.Property = dt.property(iFieldType)
        val min: Int = property.getMinimumValueOverall
        val max: Int = property.getMaximumValueOverall
        if (max - min > 32) {
          return ~position
        }
        maxLength = property.getMaximumTextLength(locale)
        {
          var i: Int = min
          while (i <= max) {
            {
              property.set(i)
              validValues.put(property.getAsShortText(locale), Boolean.TRUE)
              validValues.put(property.getAsShortText(locale).toLowerCase(locale), Boolean.TRUE)
              validValues.put(property.getAsShortText(locale).toUpperCase(locale), Boolean.TRUE)
              validValues.put(property.getAsText(locale), Boolean.TRUE)
              validValues.put(property.getAsText(locale).toLowerCase(locale), Boolean.TRUE)
              validValues.put(property.getAsText(locale).toUpperCase(locale), Boolean.TRUE)
            }
            ({
              i += 1; i - 1
            })
          }
        }
        if (("en" == locale.getLanguage) && iFieldType eq DateTimeFieldType.era) {
          validValues.put("BCE", Boolean.TRUE)
          validValues.put("bce", Boolean.TRUE)
          validValues.put("CE", Boolean.TRUE)
          validValues.put("ce", Boolean.TRUE)
          maxLength = 3
        }
        array = Array[AnyRef](validValues, Integer.valueOf(maxLength))
        innerMap.put(iFieldType, array)
      }
      else {
        validValues = array(0).asInstanceOf[Map[String, Boolean]]
        maxLength = (array(1).asInstanceOf[Integer]).intValue
      }
      val limit: Int = Math.min(text.length, position + maxLength)
      {
        var i: Int = limit
        while (i > position) {
          {
            val `match`: String = text.subSequence(position, i).toString
            if (validValues.containsKey(`match`)) {
              bucket.saveField(iFieldType, `match`, locale)
              return i
            }
          }
          ({
            i -= 1; i + 1
          })
        }
      }
      return ~position
    }
  }

  private[format] class Fraction extends InternalPrinter with InternalParser {
    private final val iFieldType: DateTimeFieldType = null
    protected var iMinDigits: Int = 0
    protected var iMaxDigits: Int = 0

    protected def this(fieldType: DateTimeFieldType, minDigits: Int, maxDigits: Int) {
      this()
      `super`
      iFieldType = fieldType
      if (maxDigits > 18) {
        maxDigits = 18
      }
      iMinDigits = minDigits
      iMaxDigits = maxDigits
    }

    def estimatePrintedLength: Int = {
      return iMaxDigits
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      printTo(appendable, instant, chrono)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      val millis: Long = partial.getChronology.set(partial, 0L)
      printTo(appendable, millis, partial.getChronology)
    }

    @throws(classOf[IOException])
    protected def printTo(appendable: Appendable, instant: Long, chrono: Chronology) {
      val field: DateTimeField = iFieldType.getField(chrono)
      var minDigits: Int = iMinDigits
      var fraction: Long = 0L
      try {
        fraction = field.remainder(instant)
      }
      catch {
        case e: RuntimeException => {
          appendUnknownString(appendable, minDigits)
          return
        }
      }
      if (fraction == 0) {
        while (({
          minDigits -= 1; minDigits
        }) >= 0) {
          appendable.append('0')
        }
        return
      }
      var str: String = null
      val fractionData: Array[Long] = getFractionData(fraction, field)
      val scaled: Long = fractionData(0)
      val maxDigits: Int = fractionData(1).toInt
      if ((scaled & 0x7fffffff) == scaled) {
        str = Integer.toString(scaled.toInt)
      }
      else {
        str = Long.toString(scaled)
      }
      var length: Int = str.length
      var digits: Int = maxDigits
      while (length < digits) {
        appendable.append('0')
        minDigits -= 1
        digits -= 1
      }
      if (minDigits < digits) {
        while (minDigits < digits) {
          if (length <= 1 || str.charAt(length - 1) != '0') {
            break //todo: break is not supported
          }
          digits -= 1
          length -= 1
        }
        if (length < str.length) {
          {
            var i: Int = 0
            while (i < length) {
              {
                appendable.append(str.charAt(i))
              }
              ({
                i += 1; i - 1
              })
            }
          }
          return
        }
      }
      appendable.append(str)
    }

    private def getFractionData(fraction: Long, field: DateTimeField): Array[Long] = {
      val rangeMillis: Long = field.getDurationField.getUnitMillis
      var scalar: Long = 0L
      var maxDigits: Int = iMaxDigits
      while (true) {
        maxDigits match {
          case _ =>
            scalar = 1L
            break //todo: break is not supported
          case 1 =>
            scalar = 10L
            break //todo: break is not supported
          case 2 =>
            scalar = 100L
            break //todo: break is not supported
          case 3 =>
            scalar = 1000L
            break //todo: break is not supported
          case 4 =>
            scalar = 10000L
            break //todo: break is not supported
          case 5 =>
            scalar = 100000L
            break //todo: break is not supported
          case 6 =>
            scalar = 1000000L
            break //todo: break is not supported
          case 7 =>
            scalar = 10000000L
            break //todo: break is not supported
          case 8 =>
            scalar = 100000000L
            break //todo: break is not supported
          case 9 =>
            scalar = 1000000000L
            break //todo: break is not supported
          case 10 =>
            scalar = 10000000000L
            break //todo: break is not supported
          case 11 =>
            scalar = 100000000000L
            break //todo: break is not supported
          case 12 =>
            scalar = 1000000000000L
            break //todo: break is not supported
          case 13 =>
            scalar = 10000000000000L
            break //todo: break is not supported
          case 14 =>
            scalar = 100000000000000L
            break //todo: break is not supported
          case 15 =>
            scalar = 1000000000000000L
            break //todo: break is not supported
          case 16 =>
            scalar = 10000000000000000L
            break //todo: break is not supported
          case 17 =>
            scalar = 100000000000000000L
            break //todo: break is not supported
          case 18 =>
            scalar = 1000000000000000000L
            break //todo: break is not supported
        }
        if (((rangeMillis * scalar) / scalar) == rangeMillis) {
          break //todo: break is not supported
        }
        maxDigits -= 1
      }
      return Array[Long](fraction * scalar / rangeMillis, maxDigits)
    }

    def estimateParsedLength: Int = {
      return iMaxDigits
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val field: DateTimeField = iFieldType.getField(bucket.getChronology)
      val limit: Int = Math.min(iMaxDigits, text.length - position)
      var value: Long = 0
      var n: Long = field.getDurationField.getUnitMillis * 10
      var length: Int = 0
      while (length < limit) {
        val c: Char = text.charAt(position + length)
        if (c < '0' || c > '9') {
          break //todo: break is not supported
        }
        length += 1
        val nn: Long = n / 10
        value += (c - '0') * nn
        n = nn
      }
      value /= 10
      if (length == 0) {
        return ~position
      }
      if (value > Integer.MAX_VALUE) {
        return ~position
      }
      val parseField: DateTimeField = new PreciseDateTimeField(DateTimeFieldType.millisOfSecond, MillisDurationField.INSTANCE, field.getDurationField)
      bucket.saveField(parseField, value.toInt)
      return position + length
    }
  }

  private[format] class TimeZoneOffset extends InternalPrinter with InternalParser {
    private final val iZeroOffsetPrintText: String = null
    private final val iZeroOffsetParseText: String = null
    private final val iShowSeparators: Boolean = false
    private final val iMinFields: Int = 0
    private final val iMaxFields: Int = 0

    private[format] def this(zeroOffsetPrintText: String, zeroOffsetParseText: String, showSeparators: Boolean, minFields: Int, maxFields: Int) {
      this()
      `super`
      iZeroOffsetPrintText = zeroOffsetPrintText
      iZeroOffsetParseText = zeroOffsetParseText
      iShowSeparators = showSeparators
      if (minFields <= 0 || maxFields < minFields) {
        throw new IllegalArgumentException
      }
      if (minFields > 4) {
        minFields = 4
        maxFields = 4
      }
      iMinFields = minFields
      iMaxFields = maxFields
    }

    def estimatePrintedLength: Int = {
      var est: Int = 1 + iMinFields << 1
      if (iShowSeparators) {
        est += iMinFields - 1
      }
      if (iZeroOffsetPrintText != null && iZeroOffsetPrintText.length > est) {
        est = iZeroOffsetPrintText.length
      }
      return est
    }

    @throws(classOf[IOException])
    def printTo(buf: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      if (displayZone == null) {
        return
      }
      if (displayOffset == 0 && iZeroOffsetPrintText != null) {
        buf.append(iZeroOffsetPrintText)
        return
      }
      if (displayOffset >= 0) {
        buf.append('+')
      }
      else {
        buf.append('-')
        displayOffset = -displayOffset
      }
      val hours: Int = displayOffset / DateTimeConstants.MILLIS_PER_HOUR
      FormatUtils.appendPaddedInteger(buf, hours, 2)
      if (iMaxFields == 1) {
        return
      }
      displayOffset -= hours * DateTimeConstants.MILLIS_PER_HOUR.toInt
      if (displayOffset == 0 && iMinFields <= 1) {
        return
      }
      val minutes: Int = displayOffset / DateTimeConstants.MILLIS_PER_MINUTE
      if (iShowSeparators) {
        buf.append(':')
      }
      FormatUtils.appendPaddedInteger(buf, minutes, 2)
      if (iMaxFields == 2) {
        return
      }
      displayOffset -= minutes * DateTimeConstants.MILLIS_PER_MINUTE
      if (displayOffset == 0 && iMinFields <= 2) {
        return
      }
      val seconds: Int = displayOffset / DateTimeConstants.MILLIS_PER_SECOND
      if (iShowSeparators) {
        buf.append(':')
      }
      FormatUtils.appendPaddedInteger(buf, seconds, 2)
      if (iMaxFields == 3) {
        return
      }
      displayOffset -= seconds * DateTimeConstants.MILLIS_PER_SECOND
      if (displayOffset == 0 && iMinFields <= 3) {
        return
      }
      if (iShowSeparators) {
        buf.append('.')
      }
      FormatUtils.appendPaddedInteger(buf, displayOffset, 3)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
    }

    def estimateParsedLength: Int = {
      return estimatePrintedLength
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      var limit: Int = text.length - position
      if (iZeroOffsetParseText != null) {
        if (iZeroOffsetParseText.length == 0) {
          if (limit > 0) {
            val c: Char = text.charAt(position)
            if (c == '-' || c == '+') {
              break //todo: label break is not supported
            }
          }
          bucket.setOffset(Integer.valueOf(0))
          return position
        }
        if (csStartsWithIgnoreCase(text, position, iZeroOffsetParseText)) {
          bucket.setOffset(Integer.valueOf(0))
          return position + iZeroOffsetParseText.length
        }
      } //todo: labels is not supported
      if (limit <= 1) {
        return ~position
      }
      var negative: Boolean = false
      var c: Char = text.charAt(position)
      if (c == '-') {
        negative = true
      }
      else if (c == '+') {
        negative = false
      }
      else {
        return ~position
      }
      limit -= 1
      position += 1
      if (digitCount(text, position, 2) < 2) {
        return ~position
      }
      var offset: Int = 0
      val hours: Int = FormatUtils.parseTwoDigits(text, position)
      if (hours > 23) {
        return ~position
      }
      offset = hours * DateTimeConstants.MILLIS_PER_HOUR
      limit -= 2
      position += 2 {
        if (limit <= 0) {
          break //todo: label break is not supported
        }
        var expectSeparators: Boolean = false
        c = text.charAt(position)
        if (c == ':') {
          expectSeparators = true
          limit -= 1
          position += 1
        }
        else if (c >= '0' && c <= '9') {
          expectSeparators = false
        }
        else {
          break //todo: label break is not supported
        }
        var count: Int = digitCount(text, position, 2)
        if (count == 0 && !expectSeparators) {
          break //todo: label break is not supported
        }
        else if (count < 2) {
          return ~position
        }
        val minutes: Int = FormatUtils.parseTwoDigits(text, position)
        if (minutes > 59) {
          return ~position
        }
        offset += minutes * DateTimeConstants.MILLIS_PER_MINUTE
        limit -= 2
        position += 2
        if (limit <= 0) {
          break //todo: label break is not supported
        }
        if (expectSeparators) {
          if (text.charAt(position) != ':') {
            break //todo: label break is not supported
          }
          limit -= 1
          position += 1
        }
        count = digitCount(text, position, 2)
        if (count == 0 && !expectSeparators) {
          break //todo: label break is not supported
        }
        else if (count < 2) {
          return ~position
        }
        val seconds: Int = FormatUtils.parseTwoDigits(text, position)
        if (seconds > 59) {
          return ~position
        }
        offset += seconds * DateTimeConstants.MILLIS_PER_SECOND
        limit -= 2
        position += 2
        if (limit <= 0) {
          break //todo: label break is not supported
        }
        if (expectSeparators) {
          if (text.charAt(position) != '.' && text.charAt(position) != ',') {
            break //todo: label break is not supported
          }
          limit -= 1
          position += 1
        }
        count = digitCount(text, position, 3)
        if (count == 0 && !expectSeparators) {
          break //todo: label break is not supported
        }
        else if (count < 1) {
          return ~position
        }
        offset += (text.charAt(({
          position += 1; position - 1
        })) - '0') * 100
        if (count > 1) {
          offset += (text.charAt(({
            position += 1; position - 1
          })) - '0') * 10
          if (count > 2) {
            offset += text.charAt(({
              position += 1; position - 1
            })) - '0'
          }
        }
      } //todo: labels is not supported
      bucket.setOffset(Integer.valueOf(if (negative) -offset else offset))
      return position
    }

    /**
     * Returns actual amount of digits to parse, but no more than original
     * 'amount' parameter.
     */
    private def digitCount(text: CharSequence, position: Int, amount: Int): Int = {
      var limit: Int = Math.min(text.length - position, amount)
      amount = 0
      while (limit > 0) {
        {
          val c: Char = text.charAt(position + amount)
          if (c < '0' || c > '9') {
            break //todo: break is not supported
          }
          amount += 1
        }
        ({
          limit -= 1; limit + 1
        })
      }
      return amount
    }
  }

  private[format] object TimeZoneName {
    private[format] val LONG_NAME: Int = 0
    private[format] val SHORT_NAME: Int = 1
  }

  private[format] class TimeZoneName extends InternalPrinter with InternalParser {
    private final val iParseLookup: Map[String, DateTimeZone] = null
    private final val iType: Int = 0

    private[format] def this(`type`: Int, parseLookup: Map[String, DateTimeZone]) {
      this()
      `super`
      iType = `type`
      iParseLookup = parseLookup
    }

    def estimatePrintedLength: Int = {
      return (if (iType == TimeZoneName.SHORT_NAME) 4 else 20)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      appendable.append(print(instant - displayOffset, displayZone, locale))
    }

    private def print(instant: Long, displayZone: DateTimeZone, locale: Locale): String = {
      if (displayZone == null) {
        return ""
      }
      iType match {
        case TimeZoneName.LONG_NAME =>
          return displayZone.getName(instant, locale)
        case TimeZoneName.SHORT_NAME =>
          return displayZone.getShortName(instant, locale)
      }
      return ""
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
    }

    def estimateParsedLength: Int = {
      return (if (iType == TimeZoneName.SHORT_NAME) 4 else 20)
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      var parseLookup: Map[String, DateTimeZone] = iParseLookup
      parseLookup = (if (parseLookup != null) parseLookup else DateTimeUtils.getDefaultTimeZoneNames)
      var matched: String = null
      import scala.collection.JavaConversions._
      for (name <- parseLookup.keySet) {
        if (csStartsWith(text, position, name)) {
          if (matched == null || name.length > matched.length) {
            matched = name
          }
        }
      }
      if (matched != null) {
        bucket.setZone(parseLookup.get(matched))
        return position + matched.length
      }
      return ~position
    }
  }

  private[format] object TimeZoneId extends Enumeration {
    type TimeZoneId = Value
    val INSTANCE = Value
    private[format] val ALL_IDS: Set[String] = DateTimeZone.getAvailableIDs
    private[format] val MAX_LENGTH: Int = 0
    try {
      var max: Int = 0
      import scala.collection.JavaConversions._
      for (id <- ALL_IDS) {
        max = Math.max(max, id.length)
      }
      MAX_LENGTH = max
    }
  }

  private[format] final class TimeZoneId extends InternalPrinter with InternalParser {
    def estimatePrintedLength: Int = {
      return TimeZoneId.MAX_LENGTH
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      appendable.append(if (displayZone != null) displayZone.getID else "")
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
    }

    def estimateParsedLength: Int = {
      return TimeZoneId.MAX_LENGTH
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      var best: String = null
      import scala.collection.JavaConversions._
      for (id <- TimeZoneId.ALL_IDS) {
        if (csStartsWith(text, position, id)) {
          if (best == null || id.length > best.length) {
            best = id
          }
        }
      }
      if (best != null) {
        bucket.setZone(DateTimeZone.forID(best))
        return position + best.length
      }
      return ~position
    }
  }

  private[format] class Composite extends InternalPrinter with InternalParser {
    private final val iPrinters: Array[InternalPrinter] = null
    private final val iParsers: Array[InternalParser] = null
    private final val iPrintedLengthEstimate: Int = 0
    private final val iParsedLengthEstimate: Int = 0

    private[format] def this(elementPairs: List[AnyRef]) {
      this()
      `super`
      val printerList: List[AnyRef] = new ArrayList[AnyRef]
      val parserList: List[AnyRef] = new ArrayList[AnyRef]
      decompose(elementPairs, printerList, parserList)
      if (printerList.contains(null) || printerList.isEmpty) {
        iPrinters = null
        iPrintedLengthEstimate = 0
      }
      else {
        val size: Int = printerList.size
        iPrinters = new Array[InternalPrinter](size)
        var printEst: Int = 0
        {
          var i: Int = 0
          while (i < size) {
            {
              val printer: InternalPrinter = printerList.get(i).asInstanceOf[InternalPrinter]
              printEst += printer.estimatePrintedLength
              iPrinters(i) = printer
            }
            ({
              i += 1; i - 1
            })
          }
        }
        iPrintedLengthEstimate = printEst
      }
      if (parserList.contains(null) || parserList.isEmpty) {
        iParsers = null
        iParsedLengthEstimate = 0
      }
      else {
        val size: Int = parserList.size
        iParsers = new Array[InternalParser](size)
        var parseEst: Int = 0
        {
          var i: Int = 0
          while (i < size) {
            {
              val parser: InternalParser = parserList.get(i).asInstanceOf[InternalParser]
              parseEst += parser.estimateParsedLength
              iParsers(i) = parser
            }
            ({
              i += 1; i - 1
            })
          }
        }
        iParsedLengthEstimate = parseEst
      }
    }

    def estimatePrintedLength: Int = {
      return iPrintedLengthEstimate
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      val elements: Array[InternalPrinter] = iPrinters
      if (elements == null) {
        throw new UnsupportedOperationException
      }
      if (locale == null) {
        locale = Locale.getDefault
      }
      val len: Int = elements.length
      {
        var i: Int = 0
        while (i < len) {
          {
            elements(i).printTo(appendable, instant, chrono, displayOffset, displayZone, locale)
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      val elements: Array[InternalPrinter] = iPrinters
      if (elements == null) {
        throw new UnsupportedOperationException
      }
      if (locale == null) {
        locale = Locale.getDefault
      }
      val len: Int = elements.length
      {
        var i: Int = 0
        while (i < len) {
          {
            elements(i).printTo(appendable, partial, locale)
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }

    def estimateParsedLength: Int = {
      return iParsedLengthEstimate
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val elements: Array[InternalParser] = iParsers
      if (elements == null) {
        throw new UnsupportedOperationException
      }
      val len: Int = elements.length
      {
        var i: Int = 0
        while (i < len && position >= 0) {
          {
            position = elements(i).parseInto(bucket, text, position)
          }
          ({
            i += 1; i - 1
          })
        }
      }
      return position
    }

    private[format] def isPrinter: Boolean = {
      return iPrinters != null
    }

    private[format] def isParser: Boolean = {
      return iParsers != null
    }

    /**
     * Processes the element pairs, putting results into the given printer
     * and parser lists.
     */
    private def decompose(elementPairs: List[AnyRef], printerList: List[AnyRef], parserList: List[AnyRef]) {
      val size: Int = elementPairs.size
      {
        var i: Int = 0
        while (i < size) {
          {
            var element: AnyRef = elementPairs.get(i)
            if (element.isInstanceOf[DateTimeFormatterBuilder.Composite]) {
              addArrayToList(printerList, (element.asInstanceOf[DateTimeFormatterBuilder.Composite]).iPrinters)
            }
            else {
              printerList.add(element)
            }
            element = elementPairs.get(i + 1)
            if (element.isInstanceOf[DateTimeFormatterBuilder.Composite]) {
              addArrayToList(parserList, (element.asInstanceOf[DateTimeFormatterBuilder.Composite]).iParsers)
            }
            else {
              parserList.add(element)
            }
          }
          i += 2
        }
      }
    }

    private def addArrayToList(list: List[AnyRef], array: Array[AnyRef]) {
      if (array != null) {
        {
          var i: Int = 0
          while (i < array.length) {
            {
              list.add(array(i))
            }
            ({
              i += 1; i - 1
            })
          }
        }
      }
    }
  }

  private[format] class MatchingParser extends InternalParser {
    private final val iParsers: Array[InternalParser] = null
    private final val iParsedLengthEstimate: Int = 0

    private[format] def this(parsers: Array[InternalParser]) {
      this()
      `super`
      iParsers = parsers
      var est: Int = 0
      {
        var i: Int = parsers.length
        while (({
          i -= 1; i
        }) >= 0) {
          val parser: InternalParser = parsers(i)
          if (parser != null) {
            val len: Int = parser.estimateParsedLength
            if (len > est) {
              est = len
            }
          }
        }
      }
      iParsedLengthEstimate = est
    }

    def estimateParsedLength: Int = {
      return iParsedLengthEstimate
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val parsers: Array[InternalParser] = iParsers
      val length: Int = parsers.length
      val originalState: AnyRef = bucket.saveState
      var isOptional: Boolean = false
      var bestValidPos: Int = position
      var bestValidState: AnyRef = null
      var bestInvalidPos: Int = position
      {
        var i: Int = 0
        while (i < length) {
          {
            val parser: InternalParser = parsers(i)
            if (parser == null) {
              if (bestValidPos <= position) {
                return position
              }
              isOptional = true
              break //todo: break is not supported
            }
            var parsePos: Int = parser.parseInto(bucket, text, position)
            if (parsePos >= position) {
              if (parsePos > bestValidPos) {
                if (parsePos >= text.length || (i + 1) >= length || parsers(i + 1) == null) {
                  return parsePos
                }
                bestValidPos = parsePos
                bestValidState = bucket.saveState
              }
            }
            else {
              if (parsePos < 0) {
                parsePos = ~parsePos
                if (parsePos > bestInvalidPos) {
                  bestInvalidPos = parsePos
                }
              }
            }
            bucket.restoreState(originalState)
          }
          ({
            i += 1; i - 1
          })
        }
      }
      if (bestValidPos > position || (bestValidPos == position && isOptional)) {
        if (bestValidState != null) {
          bucket.restoreState(bestValidState)
        }
        return bestValidPos
      }
      return ~bestInvalidPos
    }
  }

  private[format] def csStartsWith(text: CharSequence, position: Int, search: String): Boolean = {
    val searchLen: Int = search.length
    if ((text.length - position) < searchLen) {
      return false
    }
    {
      var i: Int = 0
      while (i < searchLen) {
        {
          if (text.charAt(position + i) != search.charAt(i)) {
            return false
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return true
  }

  private[format] def csStartsWithIgnoreCase(text: CharSequence, position: Int, search: String): Boolean = {
    val searchLen: Int = search.length
    if ((text.length - position) < searchLen) {
      return false
    }
    {
      var i: Int = 0
      while (i < searchLen) {
        {
          val ch1: Char = text.charAt(position + i)
          val ch2: Char = search.charAt(i)
          if (ch1 != ch2) {
            val u1: Char = Character.toUpperCase(ch1)
            val u2: Char = Character.toUpperCase(ch2)
            if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
              return false
            }
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return true
  }
}

class DateTimeFormatterBuilder {
  /** Array of printers and parsers (alternating). */
  private var iElementPairs: ArrayList[AnyRef] = null
  /** Cache of the last returned formatter. */
  private var iFormatter: AnyRef = null

  /**
   * Creates a DateTimeFormatterBuilder.
   */
  def this() {
    this()
    `super`
    iElementPairs = new ArrayList[AnyRef]
  }

  /**
   * Constructs a DateTimeFormatter using all the appended elements.
   * <p>
   * This is the main method used by applications at the end of the build
   * process to create a usable formatter.
   * <p>
   * Subsequent changes to this builder do not affect the returned formatter.
   * <p>
   * The returned formatter may not support both printing and parsing.
   * The methods {@link DateTimeFormatter#isPrinter()} and
   * {@link DateTimeFormatter#isParser()} will help you determine the state
   * of the formatter.
   *
   * @throws UnsupportedOperationException if neither printing nor parsing is supported
   */
  def toFormatter: DateTimeFormatter = {
    val f: AnyRef = getFormatter
    var printer: InternalPrinter = null
    if (isPrinter(f)) {
      printer = f.asInstanceOf[InternalPrinter]
    }
    var parser: InternalParser = null
    if (isParser(f)) {
      parser = f.asInstanceOf[InternalParser]
    }
    if (printer != null || parser != null) {
      return new DateTimeFormatter(printer, parser)
    }
    throw new UnsupportedOperationException("Both printing and parsing not supported")
  }

  /**
   * Internal method to create a DateTimePrinter instance using all the
   * appended elements.
   * <p>
   * Most applications will not use this method.
   * If you want a printer in an application, call {@link #toFormatter()}
   * and just use the printing API.
   * <p>
   * Subsequent changes to this builder do not affect the returned printer.
   *
   * @throws UnsupportedOperationException if printing is not supported
   */
  def toPrinter: DateTimePrinter = {
    val f: AnyRef = getFormatter
    if (isPrinter(f)) {
      val ip: InternalPrinter = f.asInstanceOf[InternalPrinter]
      return InternalPrinterDateTimePrinter.of(ip)
    }
    throw new UnsupportedOperationException("Printing is not supported")
  }

  /**
   * Internal method to create a DateTimeParser instance using all the
   * appended elements.
   * <p>
   * Most applications will not use this method.
   * If you want a parser in an application, call {@link #toFormatter()}
   * and just use the parsing API.
   * <p>
   * Subsequent changes to this builder do not affect the returned parser.
   *
   * @throws UnsupportedOperationException if parsing is not supported
   */
  def toParser: DateTimeParser = {
    val f: AnyRef = getFormatter
    if (isParser(f)) {
      val ip: InternalParser = f.asInstanceOf[InternalParser]
      return InternalParserDateTimeParser.of(ip)
    }
    throw new UnsupportedOperationException("Parsing is not supported")
  }

  /**
   * Returns true if toFormatter can be called without throwing an
   * UnsupportedOperationException.
   *
   * @return true if a formatter can be built
   */
  def canBuildFormatter: Boolean = {
    return isFormatter(getFormatter)
  }

  /**
   * Returns true if toPrinter can be called without throwing an
   * UnsupportedOperationException.
   *
   * @return true if a printer can be built
   */
  def canBuildPrinter: Boolean = {
    return isPrinter(getFormatter)
  }

  /**
   * Returns true if toParser can be called without throwing an
   * UnsupportedOperationException.
   *
   * @return true if a parser can be built
   */
  def canBuildParser: Boolean = {
    return isParser(getFormatter)
  }

  /**
   * Clears out all the appended elements, allowing this builder to be
   * reused.
   */
  def clear {
    iFormatter = null
    iElementPairs.clear
  }

  /**
   * Appends another formatter.
   * <p>
   * This extracts the underlying printer and parser and appends them
   * The printer and parser interfaces are the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @param formatter  the formatter to add
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if formatter is null or of an invalid type
   */
  def append(formatter: DateTimeFormatter): DateTimeFormatterBuilder = {
    if (formatter == null) {
      throw new IllegalArgumentException("No formatter supplied")
    }
    return append0(formatter.getPrinter0, formatter.getParser0)
  }

  /**
   * Appends just a printer. With no matching parser, a parser cannot be
   * built from this DateTimeFormatterBuilder.
   * <p>
   * The printer interface is part of the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @param printer  the printer to add, not null
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if printer is null or of an invalid type
   */
  def append(printer: DateTimePrinter): DateTimeFormatterBuilder = {
    checkPrinter(printer)
    return append0(DateTimePrinterInternalPrinter.of(printer), null)
  }

  /**
   * Appends just a parser. With no matching printer, a printer cannot be
   * built from this builder.
   * <p>
   * The parser interface is part of the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @param parser  the parser to add, not null
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if parser is null or of an invalid type
   */
  def append(parser: DateTimeParser): DateTimeFormatterBuilder = {
    checkParser(parser)
    return append0(null, DateTimeParserInternalParser.of(parser))
  }

  /**
   * Appends a printer/parser pair.
   * <p>
   * The printer and parser interfaces are the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @param printer  the printer to add, not null
   * @param parser  the parser to add, not null
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if printer or parser is null or of an invalid type
   */
  def append(printer: DateTimePrinter, parser: DateTimeParser): DateTimeFormatterBuilder = {
    checkPrinter(printer)
    checkParser(parser)
    return append0(DateTimePrinterInternalPrinter.of(printer), DateTimeParserInternalParser.of(parser))
  }

  /**
   * Appends a printer and a set of matching parsers. When parsing, the first
   * parser in the list is selected for parsing. If it fails, the next is
   * chosen, and so on. If none of these parsers succeeds, then the failed
   * position of the parser that made the greatest progress is returned.
   * <p>
   * Only the printer is optional. In addition, it is illegal for any but the
   * last of the parser array elements to be null. If the last element is
   * null, this represents the empty parser. The presence of an empty parser
   * indicates that the entire array of parse formats is optional.
   * <p>
   * The printer and parser interfaces are the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @param printer  the printer to add
   * @param parsers  the parsers to add
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if any printer or parser is of an invalid type
   * @throws IllegalArgumentException if any parser element but the last is null
   */
  def append(printer: DateTimePrinter, parsers: Array[DateTimeParser]): DateTimeFormatterBuilder = {
    if (printer != null) {
      checkPrinter(printer)
    }
    if (parsers == null) {
      throw new IllegalArgumentException("No parsers supplied")
    }
    val length: Int = parsers.length
    if (length == 1) {
      if (parsers(0) == null) {
        throw new IllegalArgumentException("No parser supplied")
      }
      return append0(DateTimePrinterInternalPrinter.of(printer), DateTimeParserInternalParser.of(parsers(0)))
    }
    val copyOfParsers: Array[InternalParser] = new Array[InternalParser](length)
    var i: Int = 0
    {
      i = 0
      while (i < length - 1) {
        {
          if ((({
            copyOfParsers(i) = DateTimeParserInternalParser.of(parsers(i)); copyOfParsers(i)
          })) == null) {
            throw new IllegalArgumentException("Incomplete parser array")
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    copyOfParsers(i) = DateTimeParserInternalParser.of(parsers(i))
    return append0(DateTimePrinterInternalPrinter.of(printer), new DateTimeFormatterBuilder.MatchingParser(copyOfParsers))
  }

  /**
   * Appends just a parser element which is optional. With no matching
   * printer, a printer cannot be built from this DateTimeFormatterBuilder.
   * <p>
   * The parser interface is part of the low-level part of the formatting API.
   * Normally, instances are extracted from another formatter.
   * Note however that any formatter specific information, such as the locale,
   * time-zone, chronology, offset parsing or pivot/default year, will not be
   * extracted by this method.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if parser is null or of an invalid type
   */
  def appendOptional(parser: DateTimeParser): DateTimeFormatterBuilder = {
    checkParser(parser)
    val parsers: Array[InternalParser] = Array[InternalParser](DateTimeParserInternalParser.of(parser), null)
    return append0(null, new DateTimeFormatterBuilder.MatchingParser(parsers))
  }

  /**
   * Checks if the parser is non null and a provider.
   *
   * @param parser  the parser to check
   */
  private def checkParser(parser: DateTimeParser) {
    if (parser == null) {
      throw new IllegalArgumentException("No parser supplied")
    }
  }

  /**
   * Checks if the printer is non null and a provider.
   *
   * @param printer  the printer to check
   */
  private def checkPrinter(printer: DateTimePrinter) {
    if (printer == null) {
      throw new IllegalArgumentException("No printer supplied")
    }
  }

  private def append0(element: AnyRef): DateTimeFormatterBuilder = {
    iFormatter = null
    iElementPairs.add(element)
    iElementPairs.add(element)
    return this
  }

  private def append0(printer: InternalPrinter, parser: InternalParser): DateTimeFormatterBuilder = {
    iFormatter = null
    iElementPairs.add(printer)
    iElementPairs.add(parser)
    return this
  }

  /**
   * Instructs the printer to emit a specific character, and the parser to
   * expect it. The parser is case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendLiteral(c: Char): DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.CharacterLiteral(c))
  }

  /**
   * Instructs the printer to emit specific text, and the parser to expect
   * it. The parser is case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if text is null
   */
  def appendLiteral(text: String): DateTimeFormatterBuilder = {
    if (text == null) {
      throw new IllegalArgumentException("Literal must not be null")
    }
    text.length match {
      case 0 =>
        return this
      case 1 =>
        return append0(new DateTimeFormatterBuilder.CharacterLiteral(text.charAt(0)))
      case _ =>
        return append0(new DateTimeFormatterBuilder.StringLiteral(text))
    }
  }

  /**
   * Instructs the printer to emit a field value as a decimal number, and the
   * parser to expect an unsigned decimal number.
   *
   * @param fieldType  type of field to append
   * @param minDigits  minimum number of digits to <i>print</i>
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null
   */
  def appendDecimal(fieldType: DateTimeFieldType, minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    if (maxDigits < minDigits) {
      maxDigits = minDigits
    }
    if (minDigits < 0 || maxDigits <= 0) {
      throw new IllegalArgumentException
    }
    if (minDigits <= 1) {
      return append0(new DateTimeFormatterBuilder.UnpaddedNumber(fieldType, maxDigits, false))
    }
    else {
      return append0(new DateTimeFormatterBuilder.PaddedNumber(fieldType, maxDigits, false, minDigits))
    }
  }

  /**
   * Instructs the printer to emit a field value as a fixed-width decimal
   * number (smaller numbers will be left-padded with zeros), and the parser
   * to expect an unsigned decimal number with the same fixed width.
   *
   * @param fieldType  type of field to append
   * @param numDigits  the exact number of digits to parse or print, except if
   *                   printed value requires more digits
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null or if <code>numDigits <= 0</code>
   * @since 1.5
   */
  def appendFixedDecimal(fieldType: DateTimeFieldType, numDigits: Int): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    if (numDigits <= 0) {
      throw new IllegalArgumentException("Illegal number of digits: " + numDigits)
    }
    return append0(new DateTimeFormatterBuilder.FixedNumber(fieldType, numDigits, false))
  }

  /**
   * Instructs the printer to emit a field value as a decimal number, and the
   * parser to expect a signed decimal number.
   *
   * @param fieldType  type of field to append
   * @param minDigits  minimum number of digits to <i>print</i>
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null
   */
  def appendSignedDecimal(fieldType: DateTimeFieldType, minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    if (maxDigits < minDigits) {
      maxDigits = minDigits
    }
    if (minDigits < 0 || maxDigits <= 0) {
      throw new IllegalArgumentException
    }
    if (minDigits <= 1) {
      return append0(new DateTimeFormatterBuilder.UnpaddedNumber(fieldType, maxDigits, true))
    }
    else {
      return append0(new DateTimeFormatterBuilder.PaddedNumber(fieldType, maxDigits, true, minDigits))
    }
  }

  /**
   * Instructs the printer to emit a field value as a fixed-width decimal
   * number (smaller numbers will be left-padded with zeros), and the parser
   * to expect an signed decimal number with the same fixed width.
   *
   * @param fieldType  type of field to append
   * @param numDigits  the exact number of digits to parse or print, except if
   *                   printed value requires more digits
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null or if <code>numDigits <= 0</code>
   * @since 1.5
   */
  def appendFixedSignedDecimal(fieldType: DateTimeFieldType, numDigits: Int): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    if (numDigits <= 0) {
      throw new IllegalArgumentException("Illegal number of digits: " + numDigits)
    }
    return append0(new DateTimeFormatterBuilder.FixedNumber(fieldType, numDigits, true))
  }

  /**
   * Instructs the printer to emit a field value as text, and the
   * parser to expect text.
   *
   * @param fieldType  type of field to append
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null
   */
  def appendText(fieldType: DateTimeFieldType): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    return append0(new DateTimeFormatterBuilder.TextField(fieldType, false))
  }

  /**
   * Instructs the printer to emit a field value as short text, and the
   * parser to expect text.
   *
   * @param fieldType  type of field to append
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null
   */
  def appendShortText(fieldType: DateTimeFieldType): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    return append0(new DateTimeFormatterBuilder.TextField(fieldType, true))
  }

  /**
   * Instructs the printer to emit a remainder of time as a decimal fraction,
   * without decimal point. For example, if the field is specified as
   * minuteOfHour and the time is 12:30:45, the value printed is 75. A
   * decimal point is implied, so the fraction is 0.75, or three-quarters of
   * a minute.
   *
   * @param fieldType  type of field to append
   * @param minDigits  minimum number of digits to print.
   * @param maxDigits  maximum number of digits to print or parse.
   * @return this DateTimeFormatterBuilder, for chaining
   * @throws IllegalArgumentException if field type is null
   */
  def appendFraction(fieldType: DateTimeFieldType, minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    if (fieldType == null) {
      throw new IllegalArgumentException("Field type must not be null")
    }
    if (maxDigits < minDigits) {
      maxDigits = minDigits
    }
    if (minDigits < 0 || maxDigits <= 0) {
      throw new IllegalArgumentException
    }
    return append0(new DateTimeFormatterBuilder.Fraction(fieldType, minDigits, maxDigits))
  }

  /**
   * Appends the print/parse of a fractional second.
   * <p>
   * This reliably handles the case where fractional digits are being handled
   * beyond a visible decimal point. The digits parsed will always be treated
   * as the most significant (numerically largest) digits.
   * Thus '23' will be parsed as 230 milliseconds.
   * Contrast this behaviour to {@link #appendMillisOfSecond}.
   * This method does not print or parse the decimal point itself.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to print or parse
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendFractionOfSecond(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendFraction(DateTimeFieldType.secondOfDay, minDigits, maxDigits)
  }

  /**
   * Appends the print/parse of a fractional minute.
   * <p>
   * This reliably handles the case where fractional digits are being handled
   * beyond a visible decimal point. The digits parsed will always be treated
   * as the most significant (numerically largest) digits.
   * Thus '23' will be parsed as 0.23 minutes (converted to milliseconds).
   * This method does not print or parse the decimal point itself.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to print or parse
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendFractionOfMinute(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendFraction(DateTimeFieldType.minuteOfDay, minDigits, maxDigits)
  }

  /**
   * Appends the print/parse of a fractional hour.
   * <p>
   * This reliably handles the case where fractional digits are being handled
   * beyond a visible decimal point. The digits parsed will always be treated
   * as the most significant (numerically largest) digits.
   * Thus '23' will be parsed as 0.23 hours (converted to milliseconds).
   * This method does not print or parse the decimal point itself.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to print or parse
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendFractionOfHour(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendFraction(DateTimeFieldType.hourOfDay, minDigits, maxDigits)
  }

  /**
   * Appends the print/parse of a fractional day.
   * <p>
   * This reliably handles the case where fractional digits are being handled
   * beyond a visible decimal point. The digits parsed will always be treated
   * as the most significant (numerically largest) digits.
   * Thus '23' will be parsed as 0.23 days (converted to milliseconds).
   * This method does not print or parse the decimal point itself.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to print or parse
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendFractionOfDay(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendFraction(DateTimeFieldType.dayOfYear, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a numeric millisOfSecond field.
   * <p>
   * This method will append a field that prints a three digit value.
   * During parsing the value that is parsed is assumed to be three digits.
   * If less than three digits are present then they will be counted as the
   * smallest parts of the millisecond. This is probably not what you want
   * if you are using the field as a fraction. Instead, a fractional
   * millisecond should be produced using {@link #appendFractionOfSecond}.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMillisOfSecond(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.millisOfSecond, minDigits, 3)
  }

  /**
   * Instructs the printer to emit a numeric millisOfDay field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMillisOfDay(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.millisOfDay, minDigits, 8)
  }

  /**
   * Instructs the printer to emit a numeric secondOfMinute field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendSecondOfMinute(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.secondOfMinute, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric secondOfDay field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendSecondOfDay(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.secondOfDay, minDigits, 5)
  }

  /**
   * Instructs the printer to emit a numeric minuteOfHour field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMinuteOfHour(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.minuteOfHour, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric minuteOfDay field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMinuteOfDay(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.minuteOfDay, minDigits, 4)
  }

  /**
   * Instructs the printer to emit a numeric hourOfDay field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendHourOfDay(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.hourOfDay, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric clockhourOfDay field.
   *
   * @param minDigits minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendClockhourOfDay(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.clockhourOfDay, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric hourOfHalfday field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendHourOfHalfday(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.hourOfHalfday, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric clockhourOfHalfday field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendClockhourOfHalfday(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.clockhourOfHalfday, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric dayOfWeek field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendDayOfWeek(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.dayOfWeek, minDigits, 1)
  }

  /**
   * Instructs the printer to emit a numeric dayOfMonth field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendDayOfMonth(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.dayOfMonth, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric dayOfYear field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendDayOfYear(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.dayOfYear, minDigits, 3)
  }

  /**
   * Instructs the printer to emit a numeric weekOfWeekyear field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendWeekOfWeekyear(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.weekOfWeekyear, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric weekyear field.
   *
   * @param minDigits  minimum number of digits to <i>print</i>
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendWeekyear(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendSignedDecimal(DateTimeFieldType.weekyear, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a numeric monthOfYear field.
   *
   * @param minDigits  minimum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMonthOfYear(minDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.monthOfYear, minDigits, 2)
  }

  /**
   * Instructs the printer to emit a numeric year field.
   *
   * @param minDigits  minimum number of digits to <i>print</i>
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendYear(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendSignedDecimal(DateTimeFieldType.year, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a numeric year field which always prints
   * and parses two digits. A pivot year is used during parsing to determine
   * the range of supported years as <code>(pivot - 50) .. (pivot + 49)</code>.
   *
   * <pre>
   * pivot   supported range   00 is   20 is   40 is   60 is   80 is
   * ---------------------------------------------------------------
   * 1950      1900..1999      1900    1920    1940    1960    1980
   * 1975      1925..2024      2000    2020    1940    1960    1980
   * 2000      1950..2049      2000    2020    2040    1960    1980
   * 2025      1975..2074      2000    2020    2040    2060    1980
   * 2050      2000..2099      2000    2020    2040    2060    2080
   * </pre>
   *
   * @param pivot  pivot year to use when parsing
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTwoDigitYear(pivot: Int): DateTimeFormatterBuilder = {
    return appendTwoDigitYear(pivot, false)
  }

  /**
   * Instructs the printer to emit a numeric year field which always prints
   * two digits. A pivot year is used during parsing to determine the range
   * of supported years as <code>(pivot - 50) .. (pivot + 49)</code>. If
   * parse is instructed to be lenient and the digit count is not two, it is
   * treated as an absolute year. With lenient parsing, specifying a positive
   * or negative sign before the year also makes it absolute.
   *
   * @param pivot  pivot year to use when parsing
   * @param lenientParse  when true, if digit count is not two, it is treated
   *                      as an absolute year
   * @return this DateTimeFormatterBuilder, for chaining
   * @since 1.1
   */
  def appendTwoDigitYear(pivot: Int, lenientParse: Boolean): DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TwoDigitYear(DateTimeFieldType.year, pivot, lenientParse))
  }

  /**
   * Instructs the printer to emit a numeric weekyear field which always prints
   * and parses two digits. A pivot year is used during parsing to determine
   * the range of supported years as <code>(pivot - 50) .. (pivot + 49)</code>.
   *
   * <pre>
   * pivot   supported range   00 is   20 is   40 is   60 is   80 is
   * ---------------------------------------------------------------
   * 1950      1900..1999      1900    1920    1940    1960    1980
   * 1975      1925..2024      2000    2020    1940    1960    1980
   * 2000      1950..2049      2000    2020    2040    1960    1980
   * 2025      1975..2074      2000    2020    2040    2060    1980
   * 2050      2000..2099      2000    2020    2040    2060    2080
   * </pre>
   *
   * @param pivot  pivot weekyear to use when parsing
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTwoDigitWeekyear(pivot: Int): DateTimeFormatterBuilder = {
    return appendTwoDigitWeekyear(pivot, false)
  }

  /**
   * Instructs the printer to emit a numeric weekyear field which always prints
   * two digits. A pivot year is used during parsing to determine the range
   * of supported years as <code>(pivot - 50) .. (pivot + 49)</code>. If
   * parse is instructed to be lenient and the digit count is not two, it is
   * treated as an absolute weekyear. With lenient parsing, specifying a positive
   * or negative sign before the weekyear also makes it absolute.
   *
   * @param pivot  pivot weekyear to use when parsing
   * @param lenientParse  when true, if digit count is not two, it is treated
   *                      as an absolute weekyear
   * @return this DateTimeFormatterBuilder, for chaining
   * @since 1.1
   */
  def appendTwoDigitWeekyear(pivot: Int, lenientParse: Boolean): DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TwoDigitYear(DateTimeFieldType.weekyear, pivot, lenientParse))
  }

  /**
   * Instructs the printer to emit a numeric yearOfEra field.
   *
   * @param minDigits  minimum number of digits to <i>print</i>
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendYearOfEra(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.yearOfEra, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a numeric year of century field.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendYearOfCentury(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendDecimal(DateTimeFieldType.yearOfCentury, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a numeric century of era field.
   *
   * @param minDigits  minimum number of digits to print
   * @param maxDigits  maximum number of digits to <i>parse</i>, or the estimated
   *                   maximum number of digits to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendCenturyOfEra(minDigits: Int, maxDigits: Int): DateTimeFormatterBuilder = {
    return appendSignedDecimal(DateTimeFieldType.centuryOfEra, minDigits, maxDigits)
  }

  /**
   * Instructs the printer to emit a locale-specific AM/PM text, and the
   * parser to expect it. The parser is case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendHalfdayOfDayText: DateTimeFormatterBuilder = {
    return appendText(DateTimeFieldType.halfdayOfDay)
  }

  /**
   * Instructs the printer to emit a locale-specific dayOfWeek text. The
   * parser will accept a long or short dayOfWeek text, case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendDayOfWeekText: DateTimeFormatterBuilder = {
    return appendText(DateTimeFieldType.dayOfWeek)
  }

  /**
   * Instructs the printer to emit a short locale-specific dayOfWeek
   * text. The parser will accept a long or short dayOfWeek text,
   * case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendDayOfWeekShortText: DateTimeFormatterBuilder = {
    return appendShortText(DateTimeFieldType.dayOfWeek)
  }

  /**
   * Instructs the printer to emit a short locale-specific monthOfYear
   * text. The parser will accept a long or short monthOfYear text,
   * case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMonthOfYearText: DateTimeFormatterBuilder = {
    return appendText(DateTimeFieldType.monthOfYear)
  }

  /**
   * Instructs the printer to emit a locale-specific monthOfYear text. The
   * parser will accept a long or short monthOfYear text, case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendMonthOfYearShortText: DateTimeFormatterBuilder = {
    return appendShortText(DateTimeFieldType.monthOfYear)
  }

  /**
   * Instructs the printer to emit a locale-specific era text (BC/AD), and
   * the parser to expect it. The parser is case-insensitive.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendEraText: DateTimeFormatterBuilder = {
    return appendText(DateTimeFieldType.era)
  }

  /**
   * Instructs the printer to emit a locale-specific time zone name.
   * Using this method prevents parsing, because time zone names are not unique.
   * See {@link #appendTimeZoneName(Map)}.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneName: DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TimeZoneName(DateTimeFormatterBuilder.TimeZoneName.LONG_NAME, null), null)
  }

  /**
   * Instructs the printer to emit a locale-specific time zone name, providing a lookup for parsing.
   * Time zone names are not unique, thus the API forces you to supply the lookup.
   * The names are searched in the order of the map, thus it is strongly recommended
   * to use a {@code LinkedHashMap} or similar.
   *
   * @param parseLookup  the table of names, not null
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneName(parseLookup: Map[String, DateTimeZone]): DateTimeFormatterBuilder = {
    val pp: DateTimeFormatterBuilder.TimeZoneName = new DateTimeFormatterBuilder.TimeZoneName(DateTimeFormatterBuilder.TimeZoneName.LONG_NAME, parseLookup)
    return append0(pp, pp)
  }

  /**
   * Instructs the printer to emit a short locale-specific time zone name.
   * Using this method prevents parsing, because time zone names are not unique.
   * See {@link #appendTimeZoneShortName(Map)}.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneShortName: DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TimeZoneName(DateTimeFormatterBuilder.TimeZoneName.SHORT_NAME, null), null)
  }

  /**
   * Instructs the printer to emit a short locale-specific time zone
   * name, providing a lookup for parsing.
   * Time zone names are not unique, thus the API forces you to supply the lookup.
   * The names are searched in the order of the map, thus it is strongly recommended
   * to use a {@code LinkedHashMap} or similar.
   *
   * @param parseLookup  the table of names, null to use the { @link DateTimeUtils#getDefaultTimeZoneNames() default names}
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneShortName(parseLookup: Map[String, DateTimeZone]): DateTimeFormatterBuilder = {
    val pp: DateTimeFormatterBuilder.TimeZoneName = new DateTimeFormatterBuilder.TimeZoneName(DateTimeFormatterBuilder.TimeZoneName.SHORT_NAME, parseLookup)
    return append0(pp, pp)
  }

  /**
   * Instructs the printer to emit the identifier of the time zone.
   * From version 2.0, this field can be parsed.
   *
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneId: DateTimeFormatterBuilder = {
    return append0(DateTimeFormatterBuilder.TimeZoneId.INSTANCE, DateTimeFormatterBuilder.TimeZoneId.INSTANCE)
  }

  /**
   * Instructs the printer to emit text and numbers to display time zone
   * offset from UTC. A parser will use the parsed time zone offset to adjust
   * the datetime.
   * <p>
   * If zero offset text is supplied, then it will be printed when the zone is zero.
   * During parsing, either the zero offset text, or the offset will be parsed.
   *
   * @param zeroOffsetText  the text to use if time zone offset is zero. If
   *                        null, offset is always shown.
   * @param showSeparators  if true, prints ':' separator before minute and
   *                        second field and prints '.' separator before fraction field.
   * @param minFields  minimum number of fields to print, stopping when no
   *                   more precision is required. 1=hours, 2=minutes, 3=seconds, 4=fraction
   * @param maxFields  maximum number of fields to print
   * @return this DateTimeFormatterBuilder, for chaining
   */
  def appendTimeZoneOffset(zeroOffsetText: String, showSeparators: Boolean, minFields: Int, maxFields: Int): DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TimeZoneOffset(zeroOffsetText, zeroOffsetText, showSeparators, minFields, maxFields))
  }

  /**
   * Instructs the printer to emit text and numbers to display time zone
   * offset from UTC. A parser will use the parsed time zone offset to adjust
   * the datetime.
   * <p>
   * If zero offset print text is supplied, then it will be printed when the zone is zero.
   * If zero offset parse text is supplied, then either it or the offset will be parsed.
   *
   * @param zeroOffsetPrintText  the text to print if time zone offset is zero. If
   *                             null, offset is always shown.
   * @param zeroOffsetParseText  the text to optionally parse to indicate that the time
   *                             zone offset is zero. If null, then always use the offset.
   * @param showSeparators  if true, prints ':' separator before minute and
   *                        second field and prints '.' separator before fraction field.
   * @param minFields  minimum number of fields to print, stopping when no
   *                   more precision is required. 1=hours, 2=minutes, 3=seconds, 4=fraction
   * @param maxFields  maximum number of fields to print
   * @return this DateTimeFormatterBuilder, for chaining
   * @since 2.0
   */
  def appendTimeZoneOffset(zeroOffsetPrintText: String, zeroOffsetParseText: String, showSeparators: Boolean, minFields: Int, maxFields: Int): DateTimeFormatterBuilder = {
    return append0(new DateTimeFormatterBuilder.TimeZoneOffset(zeroOffsetPrintText, zeroOffsetParseText, showSeparators, minFields, maxFields))
  }

  /**
   * Calls upon {@link DateTimeFormat} to parse the pattern and append the
   * results into this builder.
   *
   * @param pattern  pattern specification
   * @throws IllegalArgumentException if the pattern is invalid
   * @see DateTimeFormat
   */
  def appendPattern(pattern: String): DateTimeFormatterBuilder = {
    DateTimeFormat.appendPatternTo(this, pattern)
    return this
  }

  private def getFormatter: AnyRef = {
    var f: AnyRef = iFormatter
    if (f == null) {
      if (iElementPairs.size == 2) {
        val printer: AnyRef = iElementPairs.get(0)
        val parser: AnyRef = iElementPairs.get(1)
        if (printer != null) {
          if (printer eq parser || parser == null) {
            f = printer
          }
        }
        else {
          f = parser
        }
      }
      if (f == null) {
        f = new DateTimeFormatterBuilder.Composite(iElementPairs)
      }
      iFormatter = f
    }
    return f
  }

  private def isPrinter(f: AnyRef): Boolean = {
    if (f.isInstanceOf[InternalPrinter]) {
      if (f.isInstanceOf[DateTimeFormatterBuilder.Composite]) {
        return (f.asInstanceOf[DateTimeFormatterBuilder.Composite]).isPrinter
      }
      return true
    }
    return false
  }

  private def isParser(f: AnyRef): Boolean = {
    if (f.isInstanceOf[InternalParser]) {
      if (f.isInstanceOf[DateTimeFormatterBuilder.Composite]) {
        return (f.asInstanceOf[DateTimeFormatterBuilder.Composite]).isParser
      }
      return true
    }
    return false
  }

  private def isFormatter(f: AnyRef): Boolean = {
    return (isPrinter(f) || isParser(f))
  }
}