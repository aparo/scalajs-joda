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
import java.io.Writer
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.List
import java.util.Locale
import java.util.Set
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import org.joda.time.DateTimeConstants
import org.joda.time.DurationFieldType
import org.joda.time.PeriodType
import org.joda.time.ReadWritablePeriod
import org.joda.time.ReadablePeriod

/**
 * Factory that creates complex instances of PeriodFormatter via method calls.
 * <p>
 * Period formatting is performed by the {@link PeriodFormatter} class.
 * Three classes provide factory methods to create formatters, and this is one.
 * The others are {@link PeriodFormat} and {@link ISOPeriodFormat}.
 * <p>
 * PeriodFormatterBuilder is used for constructing formatters which are then
 * used to print or parse. The formatters are built by appending specific fields
 * or other formatters to an instance of this builder.
 * <p>
 * For example, a formatter that prints years and months, like "15 years and 8 months",
 * can be constructed as follows:
 * <p>
 * <pre>
 * PeriodFormatter yearsAndMonths = new PeriodFormatterBuilder()
 * .printZeroAlways()
 * .appendYears()
 * .appendSuffix(" year", " years")
 * .appendSeparator(" and ")
 * .printZeroRarelyLast()
 * .appendMonths()
 * .appendSuffix(" month", " months")
 * .toFormatter();
 * </pre>
 * <p>
 * PeriodFormatterBuilder itself is mutable and not thread-safe, but the
 * formatters that it builds are thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see PeriodFormat
 */
object PeriodFormatterBuilder {
  private val PRINT_ZERO_RARELY_FIRST: Int = 1
  private val PRINT_ZERO_RARELY_LAST: Int = 2
  private val PRINT_ZERO_IF_SUPPORTED: Int = 3
  private val PRINT_ZERO_ALWAYS: Int = 4
  private val PRINT_ZERO_NEVER: Int = 5
  private val YEARS: Int = 0
  private val MONTHS: Int = 1
  private val WEEKS: Int = 2
  private val DAYS: Int = 3
  private val HOURS: Int = 4
  private val MINUTES: Int = 5
  private val SECONDS: Int = 6
  private val MILLIS: Int = 7
  private val SECONDS_MILLIS: Int = 8
  private val SECONDS_OPTIONAL_MILLIS: Int = 9
  private val MAX_FIELD: Int = SECONDS_OPTIONAL_MILLIS
  private val PATTERNS: ConcurrentMap[String, Pattern] = new ConcurrentHashMap[String, Pattern]

  private def toFormatter(elementPairs: List[AnyRef], notPrinter: Boolean, notParser: Boolean): PeriodFormatter = {
    if (notPrinter && notParser) {
      throw new IllegalStateException("Builder has created neither a printer nor a parser")
    }
    val size: Int = elementPairs.size
    if (size >= 2 && elementPairs.get(0).isInstanceOf[PeriodFormatterBuilder.Separator]) {
      var sep: PeriodFormatterBuilder.Separator = elementPairs.get(0).asInstanceOf[PeriodFormatterBuilder.Separator]
      if (sep.iAfterParser == null && sep.iAfterPrinter == null) {
        val f: PeriodFormatter = toFormatter(elementPairs.subList(2, size), notPrinter, notParser)
        sep = sep.finish(f.getPrinter, f.getParser)
        return new PeriodFormatter(sep, sep)
      }
    }
    val comp: Array[AnyRef] = createComposite(elementPairs)
    if (notPrinter) {
      return new PeriodFormatter(null, comp(1).asInstanceOf[PeriodParser])
    }
    else if (notParser) {
      return new PeriodFormatter(comp(0).asInstanceOf[PeriodPrinter], null)
    }
    else {
      return new PeriodFormatter(comp(0).asInstanceOf[PeriodPrinter], comp(1).asInstanceOf[PeriodParser])
    }
  }

  private def createComposite(elementPairs: List[AnyRef]): Array[AnyRef] = {
    elementPairs.size match {
      case 0 =>
        return Array[AnyRef](Literal.EMPTY, Literal.EMPTY)
      case 1 =>
        return Array[AnyRef](elementPairs.get(0), elementPairs.get(1))
      case _ =>
        val comp: PeriodFormatterBuilder.Composite = new PeriodFormatterBuilder.Composite(elementPairs)
        return Array[AnyRef](comp, comp)
    }
  }

  /**
   * Defines a formatted field's prefix or suffix text.
   * This can be used for fields such as 'n hours' or 'nH' or 'Hour:n'.
   */
  private[format] trait PeriodFieldAffix {
    def calculatePrintedLength(value: Int): Int

    def printTo(buf: StringBuffer, value: Int)

    @throws(classOf[IOException])
    def printTo(out: Writer, value: Int)

    /**
     * @return new position after parsing affix, or ~position of failure
     */
    def parse(periodStr: String, position: Int): Int

    /**
     * @return position where affix starts, or original ~position if not found
     */
    def scan(periodStr: String, position: Int): Int

    /**
     * @return a copy of array of affixes
     */
    def getAffixes: Array[String]

    /**
     * This method should be called only once.
     * After first call consecutive calls to this methods will have no effect.
     * Causes this affix to ignore a match (parse and scan
     * methods) if there is an affix in the passed list that holds
     * affix text which satisfy both following conditions:
     * - the affix text is also a match
     * - the affix text is longer than the match from this object
     *
     * @param affixesToIgnore
     */
    def finish(affixesToIgnore: Set[PeriodFormatterBuilder.PeriodFieldAffix])
  }

  /**
   * An affix that can be ignored.
   */
  private[format] abstract class IgnorableAffix extends PeriodFieldAffix {
    @volatile
    private var iOtherAffixes: Array[String] = null

    def finish(periodFieldAffixesToIgnore: Set[PeriodFormatterBuilder.PeriodFieldAffix]) {
      if (iOtherAffixes == null) {
        var shortestAffixLength: Int = Integer.MAX_VALUE
        var shortestAffix: String = null
        for (affix <- getAffixes) {
          if (affix.length < shortestAffixLength) {
            shortestAffixLength = affix.length
            shortestAffix = affix
          }
        }
        val affixesToIgnore: Set[String] = new HashSet[String]
        import scala.collection.JavaConversions._
        for (periodFieldAffixToIgnore <- periodFieldAffixesToIgnore) {
          if (periodFieldAffixToIgnore != null) {
            for (affixToIgnore <- periodFieldAffixToIgnore.getAffixes) {
              if (affixToIgnore.length > shortestAffixLength || (affixToIgnore.equalsIgnoreCase(shortestAffix) && !(affixToIgnore == shortestAffix))) {
                affixesToIgnore.add(affixToIgnore)
              }
            }
          }
        }
        iOtherAffixes = affixesToIgnore.toArray(new Array[String](affixesToIgnore.size))
      }
    }

    /**
     * Checks if there is a match among the other affixes (stored internally)
     * that is longer than the passed value (textLength).
     *
     * @param textLength  the length of the match
     * @param periodStr  the Period string that will be parsed
     * @param position  the position in the Period string at which the parsing should be started.
     * @return true if the other affixes (stored internally) contain a match
     *         that is longer than the textLength parameter, false otherwise
     */
    protected def matchesOtherAffix(textLength: Int, periodStr: String, position: Int): Boolean = {
      if (iOtherAffixes != null) {
        for (affixToIgnore <- iOtherAffixes) {
          val textToIgnoreLength: Int = affixToIgnore.length
          if ((textLength < textToIgnoreLength && periodStr.regionMatches(true, position, affixToIgnore, 0, textToIgnoreLength)) || (textLength == textToIgnoreLength && periodStr.regionMatches(false, position, affixToIgnore, 0, textToIgnoreLength))) {
            return true
          }
        }
      }
      return false
    }
  }

  /**
   * Implements an affix where the text does not vary by the amount.
   */
  private[format] class SimpleAffix extends IgnorableAffix {
    private final val iText: String = null

    private[format] def this(text: String) {
      this()
      iText = text
    }

    def calculatePrintedLength(value: Int): Int = {
      return iText.length
    }

    def printTo(buf: StringBuffer, value: Int) {
      buf.append(iText)
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, value: Int) {
      out.write(iText)
    }

    def parse(periodStr: String, position: Int): Int = {
      val text: String = iText
      val textLength: Int = text.length
      if (periodStr.regionMatches(true, position, text, 0, textLength)) {
        if (!matchesOtherAffix(textLength, periodStr, position)) {
          return position + textLength
        }
      }
      return ~position
    }

    def scan(periodStr: String, position: Int): Int = {
      val text: String = iText
      val textLength: Int = text.length
      val sourceLength: Int = periodStr.length
      {
        var pos: Int = position
        while (pos < sourceLength) {
          {
            if (periodStr.regionMatches(true, pos, text, 0, textLength)) {
              if (!matchesOtherAffix(textLength, periodStr, pos)) {
                return pos
              }
            }
            periodStr.charAt(pos) match {
              case '0' =>
              case '1' =>
              case '2' =>
              case '3' =>
              case '4' =>
              case '5' =>
              case '6' =>
              case '7' =>
              case '8' =>
              case '9' =>
              case '.' =>
              case ',' =>
              case '+' =>
              case '-' =>
                break //todo: break is not supported
              case _ =>
                break //todo: label break is not supported
            }
          }
          ({
            pos += 1; pos - 1
          })
        }
      } //todo: labels is not supported
      return ~position
    }

    def getAffixes: Array[String] = {
      return Array[String](iText)
    }
  }

  /**
   * Implements an affix where the text varies by the amount of the field.
   * Only singular (1) and plural (not 1) are supported.
   */
  private[format] class PluralAffix extends IgnorableAffix {
    private final val iSingularText: String = null
    private final val iPluralText: String = null

    private[format] def this(singularText: String, pluralText: String) {
      this()
      iSingularText = singularText
      iPluralText = pluralText
    }

    def calculatePrintedLength(value: Int): Int = {
      return (if (value == 1) iSingularText else iPluralText).length
    }

    def printTo(buf: StringBuffer, value: Int) {
      buf.append(if (value == 1) iSingularText else iPluralText)
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, value: Int) {
      out.write(if (value == 1) iSingularText else iPluralText)
    }

    def parse(periodStr: String, position: Int): Int = {
      var text1: String = iPluralText
      var text2: String = iSingularText
      if (text1.length < text2.length) {
        val temp: String = text1
        text1 = text2
        text2 = temp
      }
      if (periodStr.regionMatches(true, position, text1, 0, text1.length)) {
        if (!matchesOtherAffix(text1.length, periodStr, position)) {
          return position + text1.length
        }
      }
      if (periodStr.regionMatches(true, position, text2, 0, text2.length)) {
        if (!matchesOtherAffix(text2.length, periodStr, position)) {
          return position + text2.length
        }
      }
      return ~position
    }

    def scan(periodStr: String, position: Int): Int = {
      var text1: String = iPluralText
      var text2: String = iSingularText
      if (text1.length < text2.length) {
        val temp: String = text1
        text1 = text2
        text2 = temp
      }
      val textLength1: Int = text1.length
      val textLength2: Int = text2.length
      val sourceLength: Int = periodStr.length
      {
        var pos: Int = position
        while (pos < sourceLength) {
          {
            if (periodStr.regionMatches(true, pos, text1, 0, textLength1)) {
              if (!matchesOtherAffix(text1.length, periodStr, pos)) {
                return pos
              }
            }
            if (periodStr.regionMatches(true, pos, text2, 0, textLength2)) {
              if (!matchesOtherAffix(text2.length, periodStr, pos)) {
                return pos
              }
            }
          }
          ({
            pos += 1; pos - 1
          })
        }
      }
      return ~position
    }

    def getAffixes: Array[String] = {
      return Array[String](iSingularText, iPluralText)
    }
  }

  /**
   * Implements an affix where the text varies by the amount of the field.
   * Different amounts are supported based on the provided parameters.
   */
  private[format] object RegExAffix {
    private val LENGTH_DESC_COMPARATOR: Comparator[String] =
    new class null
    {
      def compare(o1: String, o2: String): Int = {
        return o2.length - o1.length
      }
    }
  }

  private[format] class RegExAffix extends IgnorableAffix {
    private final val iSuffixes: Array[String] = null
    private final val iPatterns: Array[Pattern] = null
    private final val iSuffixesSortedDescByLength: Array[String] = null

    private[format] def this(regExes: Array[String], texts: Array[String]) {
      this()
      iSuffixes = texts.clone
      iPatterns = new Array[Pattern](regExes.length)
      {
        var i: Int = 0
        while (i < regExes.length) {
          {
            var pattern: Pattern = PATTERNS.get(regExes(i))
            if (pattern == null) {
              pattern = Pattern.compile(regExes(i))
              PATTERNS.putIfAbsent(regExes(i), pattern)
            }
            iPatterns(i) = pattern
          }
          ({
            i += 1; i - 1
          })
        }
      }
      iSuffixesSortedDescByLength = iSuffixes.clone
      Arrays.sort(iSuffixesSortedDescByLength, RegExAffix.LENGTH_DESC_COMPARATOR)
    }

    private def selectSuffixIndex(value: Int): Int = {
      val valueString: String = String.valueOf(value)
      {
        var i: Int = 0
        while (i < iPatterns.length) {
          {
            if (iPatterns(i).matcher(valueString).matches) {
              return i
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      return iPatterns.length - 1
    }

    def calculatePrintedLength(value: Int): Int = {
      return iSuffixes(selectSuffixIndex(value)).length
    }

    def printTo(buf: StringBuffer, value: Int) {
      buf.append(iSuffixes(selectSuffixIndex(value)))
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, value: Int) {
      out.write(iSuffixes(selectSuffixIndex(value)))
    }

    def parse(periodStr: String, position: Int): Int = {
      for (text <- iSuffixesSortedDescByLength) {
        if (periodStr.regionMatches(true, position, text, 0, text.length)) {
          if (!matchesOtherAffix(text.length, periodStr, position)) {
            return position + text.length
          }
        }
      }
      return ~position
    }

    def scan(periodStr: String, position: Int): Int = {
      val sourceLength: Int = periodStr.length
      {
        var pos: Int = position
        while (pos < sourceLength) {
          {
            for (text <- iSuffixesSortedDescByLength) {
              if (periodStr.regionMatches(true, pos, text, 0, text.length)) {
                if (!matchesOtherAffix(text.length, periodStr, pos)) {
                  return pos
                }
              }
            }
          }
          ({
            pos += 1; pos - 1
          })
        }
      }
      return ~position
    }

    def getAffixes: Array[String] = {
      return iSuffixes.clone
    }
  }

  /**
   * Builds a composite affix by merging two other affix implementations.
   */
  private[format] class CompositeAffix extends IgnorableAffix {
    private final val iLeft: PeriodFormatterBuilder.PeriodFieldAffix = null
    private final val iRight: PeriodFormatterBuilder.PeriodFieldAffix = null
    private final val iLeftRightCombinations: Array[String] = null

    private[format] def this(left: PeriodFormatterBuilder.PeriodFieldAffix, right: PeriodFormatterBuilder.PeriodFieldAffix) {
      this()
      iLeft = left
      iRight = right
      val result: Set[String] = new HashSet[String]
      for (leftText <- iLeft.getAffixes) {
        for (rightText <- iRight.getAffixes) {
          result.add(leftText + rightText)
        }
      }
      iLeftRightCombinations = result.toArray(new Array[String](result.size))
    }

    def calculatePrintedLength(value: Int): Int = {
      return iLeft.calculatePrintedLength(value) + iRight.calculatePrintedLength(value)
    }

    def printTo(buf: StringBuffer, value: Int) {
      iLeft.printTo(buf, value)
      iRight.printTo(buf, value)
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, value: Int) {
      iLeft.printTo(out, value)
      iRight.printTo(out, value)
    }

    def parse(periodStr: String, position: Int): Int = {
      var pos: Int = iLeft.parse(periodStr, position)
      if (pos >= 0) {
        pos = iRight.parse(periodStr, pos)
        if (pos >= 0 && matchesOtherAffix(parse(periodStr, pos) - pos, periodStr, position)) {
          return ~position
        }
      }
      return pos
    }

    def scan(periodStr: String, position: Int): Int = {
      val leftPosition: Int = iLeft.scan(periodStr, position)
      if (leftPosition >= 0) {
        val rightPosition: Int = iRight.scan(periodStr, iLeft.parse(periodStr, leftPosition))
        if (!(rightPosition >= 0 && matchesOtherAffix(iRight.parse(periodStr, rightPosition) - leftPosition, periodStr, position))) {
          if (leftPosition > 0) {
            return leftPosition
          }
          else {
            return rightPosition
          }
        }
      }
      return ~position
    }

    def getAffixes: Array[String] = {
      return iLeftRightCombinations.clone
    }
  }

  /**
   * Formats the numeric value of a field, potentially with prefix/suffix.
   */
  private[format] class FieldFormatter extends PeriodPrinter with PeriodParser {
    private final val iMinPrintedDigits: Int = 0
    private final val iPrintZeroSetting: Int = 0
    private final val iMaxParsedDigits: Int = 0
    private final val iRejectSignedValues: Boolean = false
    /** The index of the field type, 0=year, etc. */
    private final val iFieldType: Int = 0
    /**
     * The array of the latest formatter added for each type.
     * This is shared between all the field formatters in a formatter.
     */
    private final val iFieldFormatters: Array[PeriodFormatterBuilder.FieldFormatter] = null
    private final val iPrefix: PeriodFormatterBuilder.PeriodFieldAffix = null
    private final val iSuffix: PeriodFormatterBuilder.PeriodFieldAffix = null

    private[format] def this(minPrintedDigits: Int, printZeroSetting: Int, maxParsedDigits: Int, rejectSignedValues: Boolean, fieldType: Int, fieldFormatters: Array[PeriodFormatterBuilder.FieldFormatter], prefix: PeriodFormatterBuilder.PeriodFieldAffix, suffix: PeriodFormatterBuilder.PeriodFieldAffix) {
      this()
      iMinPrintedDigits = minPrintedDigits
      iPrintZeroSetting = printZeroSetting
      iMaxParsedDigits = maxParsedDigits
      iRejectSignedValues = rejectSignedValues
      iFieldType = fieldType
      iFieldFormatters = fieldFormatters
      iPrefix = prefix
      iSuffix = suffix
    }

    private[format] def this(field: PeriodFormatterBuilder.FieldFormatter, suffix: PeriodFormatterBuilder.PeriodFieldAffix) {
      this()
      iMinPrintedDigits = field.iMinPrintedDigits
      iPrintZeroSetting = field.iPrintZeroSetting
      iMaxParsedDigits = field.iMaxParsedDigits
      iRejectSignedValues = field.iRejectSignedValues
      iFieldType = field.iFieldType
      iFieldFormatters = field.iFieldFormatters
      iPrefix = field.iPrefix
      if (field.iSuffix != null) {
        suffix = new PeriodFormatterBuilder.CompositeAffix(field.iSuffix, suffix)
      }
      iSuffix = suffix
    }

    def finish(fieldFormatters: Array[PeriodFormatterBuilder.FieldFormatter]) {
      val prefixesToIgnore: Set[PeriodFormatterBuilder.PeriodFieldAffix] = new HashSet[PeriodFormatterBuilder.PeriodFieldAffix]
      val suffixesToIgnore: Set[PeriodFormatterBuilder.PeriodFieldAffix] = new HashSet[PeriodFormatterBuilder.PeriodFieldAffix]
      for (fieldFormatter <- fieldFormatters) {
        if (fieldFormatter != null && !(this == fieldFormatter)) {
          prefixesToIgnore.add(fieldFormatter.iPrefix)
          suffixesToIgnore.add(fieldFormatter.iSuffix)
        }
      }
      if (iPrefix != null) {
        iPrefix.finish(prefixesToIgnore)
      }
      if (iSuffix != null) {
        iSuffix.finish(suffixesToIgnore)
      }
    }

    def countFieldsToPrint(period: ReadablePeriod, stopAt: Int, locale: Locale): Int = {
      if (stopAt <= 0) {
        return 0
      }
      if (iPrintZeroSetting == PRINT_ZERO_ALWAYS || getFieldValue(period) != Long.MaxValue) {
        return 1
      }
      return 0
    }

    def calculatePrintedLength(period: ReadablePeriod, locale: Locale): Int = {
      var valueLong: Long = getFieldValue(period)
      if (valueLong == Long.MaxValue) {
        return 0
      }
      var sum: Int = Math.max(FormatUtils.calculateDigitCount(valueLong), iMinPrintedDigits)
      if (iFieldType >= SECONDS_MILLIS) {
        sum = (if (valueLong < 0) Math.max(sum, 5) else Math.max(sum, 4))
        sum += 1
        if (iFieldType == SECONDS_OPTIONAL_MILLIS && (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND) == 0) {
          sum -= 4
        }
        valueLong = valueLong / DateTimeConstants.MILLIS_PER_SECOND
      }
      val value: Int = valueLong.toInt
      if (iPrefix != null) {
        sum += iPrefix.calculatePrintedLength(value)
      }
      if (iSuffix != null) {
        sum += iSuffix.calculatePrintedLength(value)
      }
      return sum
    }

    def printTo(buf: StringBuffer, period: ReadablePeriod, locale: Locale) {
      val valueLong: Long = getFieldValue(period)
      if (valueLong == Long.MaxValue) {
        return
      }
      var value: Int = valueLong.toInt
      if (iFieldType >= SECONDS_MILLIS) {
        value = (valueLong / DateTimeConstants.MILLIS_PER_SECOND).toInt
      }
      if (iPrefix != null) {
        iPrefix.printTo(buf, value)
      }
      val bufLen: Int = buf.length
      val minDigits: Int = iMinPrintedDigits
      if (minDigits <= 1) {
        FormatUtils.appendUnpaddedInteger(buf, value)
      }
      else {
        FormatUtils.appendPaddedInteger(buf, value, minDigits)
      }
      if (iFieldType >= SECONDS_MILLIS) {
        val dp: Int = (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND).toInt
        if (iFieldType == SECONDS_MILLIS || dp > 0) {
          if (valueLong < 0 && valueLong > -DateTimeConstants.MILLIS_PER_SECOND) {
            buf.insert(bufLen, '-')
          }
          buf.append('.')
          FormatUtils.appendPaddedInteger(buf, dp, 3)
        }
      }
      if (iSuffix != null) {
        iSuffix.printTo(buf, value)
      }
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, period: ReadablePeriod, locale: Locale) {
      val valueLong: Long = getFieldValue(period)
      if (valueLong == Long.MaxValue) {
        return
      }
      var value: Int = valueLong.toInt
      if (iFieldType >= SECONDS_MILLIS) {
        value = (valueLong / DateTimeConstants.MILLIS_PER_SECOND).toInt
      }
      if (iPrefix != null) {
        iPrefix.printTo(out, value)
      }
      val minDigits: Int = iMinPrintedDigits
      if (minDigits <= 1) {
        FormatUtils.writeUnpaddedInteger(out, value)
      }
      else {
        FormatUtils.writePaddedInteger(out, value, minDigits)
      }
      if (iFieldType >= SECONDS_MILLIS) {
        val dp: Int = (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND).toInt
        if (iFieldType == SECONDS_MILLIS || dp > 0) {
          out.write('.')
          FormatUtils.writePaddedInteger(out, dp, 3)
        }
      }
      if (iSuffix != null) {
        iSuffix.printTo(out, value)
      }
    }

    def parseInto(period: ReadWritablePeriod, text: String, position: Int, locale: Locale): Int = {
      var mustParse: Boolean = (iPrintZeroSetting == PRINT_ZERO_ALWAYS)
      if (position >= text.length) {
        return if (mustParse) ~position else position
      }
      if (iPrefix != null) {
        position = iPrefix.parse(text, position)
        if (position >= 0) {
          mustParse = true
        }
        else {
          if (!mustParse) {
            return ~position
          }
          return position
        }
      }
      var suffixPos: Int = -1
      if (iSuffix != null && !mustParse) {
        suffixPos = iSuffix.scan(text, position)
        if (suffixPos >= 0) {
          mustParse = true
        }
        else {
          if (!mustParse) {
            return ~suffixPos
          }
          return suffixPos
        }
      }
      if (!mustParse && !isSupported(period.getPeriodType, iFieldType)) {
        return position
      }
      var limit: Int = 0
      if (suffixPos > 0) {
        limit = Math.min(iMaxParsedDigits, suffixPos - position)
      }
      else {
        limit = Math.min(iMaxParsedDigits, text.length - position)
      }
      var length: Int = 0
      var fractPos: Int = -1
      var hasDigits: Boolean = false
      var negative: Boolean = false
      while (length < limit) {
        var c: Char = text.charAt(position + length)
        if (length == 0 && (c == '-' || c == '+') && !iRejectSignedValues) {
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
        if (c >= '0' && c <= '9') {
          hasDigits = true
        }
        else {
          if ((c == '.' || c == ',') && (iFieldType == SECONDS_MILLIS || iFieldType == SECONDS_OPTIONAL_MILLIS)) {
            if (fractPos >= 0) {
              break //todo: break is not supported
            }
            fractPos = position + length + 1
            limit = Math.min(limit + 1, text.length - position)
          }
          else {
            break //todo: break is not supported
          }
        }
        length += 1
      }
      if (!hasDigits) {
        return ~position
      }
      if (suffixPos >= 0 && position + length != suffixPos) {
        return position
      }
      if (iFieldType != SECONDS_MILLIS && iFieldType != SECONDS_OPTIONAL_MILLIS) {
        setFieldValue(period, iFieldType, parseInt(text, position, length))
      }
      else if (fractPos < 0) {
        setFieldValue(period, SECONDS, parseInt(text, position, length))
        setFieldValue(period, MILLIS, 0)
      }
      else {
        val wholeValue: Int = parseInt(text, position, fractPos - position - 1)
        setFieldValue(period, SECONDS, wholeValue)
        val fractLen: Int = position + length - fractPos
        var fractValue: Int = 0
        if (fractLen <= 0) {
          fractValue = 0
        }
        else {
          if (fractLen >= 3) {
            fractValue = parseInt(text, fractPos, 3)
          }
          else {
            fractValue = parseInt(text, fractPos, fractLen)
            if (fractLen == 1) {
              fractValue *= 100
            }
            else {
              fractValue *= 10
            }
          }
          if (negative || wholeValue < 0) {
            fractValue = -fractValue
          }
        }
        setFieldValue(period, MILLIS, fractValue)
      }
      position += length
      if (position >= 0 && iSuffix != null) {
        position = iSuffix.parse(text, position)
      }
      return position
    }

    /**
     * @param text text to parse
     * @param position position in text
     * @param length exact count of characters to parse
     * @return parsed int value
     */
    private def parseInt(text: String, position: Int, length: Int): Int = {
      if (length >= 10) {
        return text.substring(position, position + length).toInt
      }
      if (length <= 0) {
        return 0
      }
      var value: Int = text.charAt(({
        position += 1; position - 1
      }))
      length -= 1
      var negative: Boolean = false
      if (value == '-') {
        if (({
          length -= 1; length
        }) < 0) {
          return 0
        }
        negative = true
        value = text.charAt(({
          position += 1; position - 1
        }))
      }
      else {
        negative = false
      }
      value -= '0'
      while (({
        length -= 1; length + 1
      }) > 0) {
        value = ((value << 3) + (value << 1)) + text.charAt(({
          position += 1; position - 1
        })) - '0'
      }
      return if (negative) -value else value
    }

    /**
     * @return Long.MaxValue if nothing to print, otherwise value
     */
    private[format] def getFieldValue(period: ReadablePeriod): Long = {
      var `type`: PeriodType = null
      if (iPrintZeroSetting == PRINT_ZERO_ALWAYS) {
        `type` = null
      }
      else {
        `type` = period.getPeriodType
      }
      if (`type` != null && isSupported(`type`, iFieldType) == false) {
        return Long.MaxValue
      }
      var value: Long = 0L
      iFieldType match {
        case _ =>
          return Long.MaxValue
        case YEARS =>
          value = period.get(DurationFieldType.years)
          break //todo: break is not supported
        case MONTHS =>
          value = period.get(DurationFieldType.months)
          break //todo: break is not supported
        case WEEKS =>
          value = period.get(DurationFieldType.weeks)
          break //todo: break is not supported
        case DAYS =>
          value = period.get(DurationFieldType.days)
          break //todo: break is not supported
        case HOURS =>
          value = period.get(DurationFieldType.hours)
          break //todo: break is not supported
        case MINUTES =>
          value = period.get(DurationFieldType.minutes)
          break //todo: break is not supported
        case SECONDS =>
          value = period.get(DurationFieldType.seconds)
          break //todo: break is not supported
        case MILLIS =>
          value = period.get(DurationFieldType.millis)
          break //todo: break is not supported
        case SECONDS_MILLIS =>
        case SECONDS_OPTIONAL_MILLIS =>
          val seconds: Int = period.get(DurationFieldType.seconds)
          val millis: Int = period.get(DurationFieldType.millis)
          value = (seconds * DateTimeConstants.MILLIS_PER_SECOND.toLong) + millis
          break //todo: break is not supported
      }
      if (value == 0) {
        iPrintZeroSetting match {
          case PRINT_ZERO_NEVER =>
            return Long.MaxValue
          case PRINT_ZERO_RARELY_LAST =>
            if (isZero(period) && iFieldFormatters(iFieldType) eq this) {
              {
                var i: Int = iFieldType + 1
                while (i <= MAX_FIELD) {
                  {
                    if (isSupported(`type`, i) && iFieldFormatters(i) != null) {
                      return Long.MaxValue
                    }
                  }
                  ({
                    i += 1; i - 1
                  })
                }
              }
            }
            else {
              return Long.MaxValue
            }
            break //todo: break is not supported
          case PRINT_ZERO_RARELY_FIRST =>
            if (isZero(period) && iFieldFormatters(iFieldType) eq this) {
              var i: Int = Math.min(iFieldType, 8)
              i -= 1
              while (i >= 0 && i <= MAX_FIELD) {
                {
                  if (isSupported(`type`, i) && iFieldFormatters(i) != null) {
                    return Long.MaxValue
                  }
                }
                ({
                  i -= 1; i + 1
                })
              }
            }
            else {
              return Long.MaxValue
            }
            break //todo: break is not supported
        }
      }
      return value
    }

    private[format] def isZero(period: ReadablePeriod): Boolean = {
      {
        var i: Int = 0
        val isize: Int = period.size
        while (i < isize) {
          {
            if (period.getValue(i) != 0) {
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

    private[format] def isSupported(`type`: PeriodType, field: Int): Boolean = {
      field match {
        case _ =>
          return false
        case YEARS =>
          return `type`.isSupported(DurationFieldType.years)
        case MONTHS =>
          return `type`.isSupported(DurationFieldType.months)
        case WEEKS =>
          return `type`.isSupported(DurationFieldType.weeks)
        case DAYS =>
          return `type`.isSupported(DurationFieldType.days)
        case HOURS =>
          return `type`.isSupported(DurationFieldType.hours)
        case MINUTES =>
          return `type`.isSupported(DurationFieldType.minutes)
        case SECONDS =>
          return `type`.isSupported(DurationFieldType.seconds)
        case MILLIS =>
          return `type`.isSupported(DurationFieldType.millis)
        case SECONDS_MILLIS =>
        case SECONDS_OPTIONAL_MILLIS =>
          return `type`.isSupported(DurationFieldType.seconds) || `type`.isSupported(DurationFieldType.millis)
      }
    }

    private[format] def setFieldValue(period: ReadWritablePeriod, field: Int, value: Int) {
      field match {
        case _ =>
          break //todo: break is not supported
        case YEARS =>
          period.setYears(value)
          break //todo: break is not supported
        case MONTHS =>
          period.setMonths(value)
          break //todo: break is not supported
        case WEEKS =>
          period.setWeeks(value)
          break //todo: break is not supported
        case DAYS =>
          period.setDays(value)
          break //todo: break is not supported
        case HOURS =>
          period.setHours(value)
          break //todo: break is not supported
        case MINUTES =>
          period.setMinutes(value)
          break //todo: break is not supported
        case SECONDS =>
          period.setSeconds(value)
          break //todo: break is not supported
        case MILLIS =>
          period.setMillis(value)
          break //todo: break is not supported
      }
    }

    private[format] def getFieldType: Int = {
      return iFieldType
    }
  }

  /**
   * Handles a simple literal piece of text.
   */
  private[format] object Literal {
    private[format] val EMPTY: PeriodFormatterBuilder.Literal = new PeriodFormatterBuilder.Literal("")
  }

  private[format] class Literal extends PeriodPrinter with PeriodParser {
    private final val iText: String = null

    private[format] def this(text: String) {
      this()
      iText = text
    }

    def countFieldsToPrint(period: ReadablePeriod, stopAt: Int, locale: Locale): Int = {
      return 0
    }

    def calculatePrintedLength(period: ReadablePeriod, locale: Locale): Int = {
      return iText.length
    }

    def printTo(buf: StringBuffer, period: ReadablePeriod, locale: Locale) {
      buf.append(iText)
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, period: ReadablePeriod, locale: Locale) {
      out.write(iText)
    }

    def parseInto(period: ReadWritablePeriod, periodStr: String, position: Int, locale: Locale): Int = {
      if (periodStr.regionMatches(true, position, iText, 0, iText.length)) {
        return position + iText.length
      }
      return ~position
    }
  }

  /**
   * Handles a separator, that splits the fields into multiple parts.
   * For example, the 'T' in the ISO8601 standard.
   */
  private[format] class Separator extends PeriodPrinter with PeriodParser {
    private final val iText: String = null
    private final val iFinalText: String = null
    private final val iParsedForms: Array[String] = null
    private final val iUseBefore: Boolean = false
    private final val iUseAfter: Boolean = false
    private final val iBeforePrinter: PeriodPrinter = null
    @volatile
    private var iAfterPrinter: PeriodPrinter = null
    private final val iBeforeParser: PeriodParser = null
    @volatile
    private var iAfterParser: PeriodParser = null

    private[format] def this(text: String, finalText: String, variants: Array[String], beforePrinter: PeriodPrinter, beforeParser: PeriodParser, useBefore: Boolean, useAfter: Boolean) {
      this()
      iText = text
      iFinalText = finalText
      if ((finalText == null || (text == finalText)) && (variants == null || variants.length == 0)) {
        iParsedForms = Array[String](text)
      }
      else {
        val parsedSet: TreeSet[String] = new TreeSet[String](String.CASE_INSENSITIVE_ORDER)
        parsedSet.add(text)
        parsedSet.add(finalText)
        if (variants != null) {
          {
            var i: Int = variants.length
            while (({
              i -= 1; i
            }) >= 0) {
              parsedSet.add(variants(i))
            }
          }
        }
        val parsedList: ArrayList[String] = new ArrayList[String](parsedSet)
        Collections.reverse(parsedList)
        iParsedForms = parsedList.toArray(new Array[String](parsedList.size))
      }
      iBeforePrinter = beforePrinter
      iBeforeParser = beforeParser
      iUseBefore = useBefore
      iUseAfter = useAfter
    }

    def countFieldsToPrint(period: ReadablePeriod, stopAt: Int, locale: Locale): Int = {
      var sum: Int = iBeforePrinter.countFieldsToPrint(period, stopAt, locale)
      if (sum < stopAt) {
        sum += iAfterPrinter.countFieldsToPrint(period, stopAt, locale)
      }
      return sum
    }

    def calculatePrintedLength(period: ReadablePeriod, locale: Locale): Int = {
      val before: PeriodPrinter = iBeforePrinter
      val after: PeriodPrinter = iAfterPrinter
      var sum: Int = before.calculatePrintedLength(period, locale) + after.calculatePrintedLength(period, locale)
      if (iUseBefore) {
        if (before.countFieldsToPrint(period, 1, locale) > 0) {
          if (iUseAfter) {
            val afterCount: Int = after.countFieldsToPrint(period, 2, locale)
            if (afterCount > 0) {
              sum += (if (afterCount > 1) iText else iFinalText).length
            }
          }
          else {
            sum += iText.length
          }
        }
      }
      else if (iUseAfter && after.countFieldsToPrint(period, 1, locale) > 0) {
        sum += iText.length
      }
      return sum
    }

    def printTo(buf: StringBuffer, period: ReadablePeriod, locale: Locale) {
      val before: PeriodPrinter = iBeforePrinter
      val after: PeriodPrinter = iAfterPrinter
      before.printTo(buf, period, locale)
      if (iUseBefore) {
        if (before.countFieldsToPrint(period, 1, locale) > 0) {
          if (iUseAfter) {
            val afterCount: Int = after.countFieldsToPrint(period, 2, locale)
            if (afterCount > 0) {
              buf.append(if (afterCount > 1) iText else iFinalText)
            }
          }
          else {
            buf.append(iText)
          }
        }
      }
      else if (iUseAfter && after.countFieldsToPrint(period, 1, locale) > 0) {
        buf.append(iText)
      }
      after.printTo(buf, period, locale)
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, period: ReadablePeriod, locale: Locale) {
      val before: PeriodPrinter = iBeforePrinter
      val after: PeriodPrinter = iAfterPrinter
      before.printTo(out, period, locale)
      if (iUseBefore) {
        if (before.countFieldsToPrint(period, 1, locale) > 0) {
          if (iUseAfter) {
            val afterCount: Int = after.countFieldsToPrint(period, 2, locale)
            if (afterCount > 0) {
              out.write(if (afterCount > 1) iText else iFinalText)
            }
          }
          else {
            out.write(iText)
          }
        }
      }
      else if (iUseAfter && after.countFieldsToPrint(period, 1, locale) > 0) {
        out.write(iText)
      }
      after.printTo(out, period, locale)
    }

    def parseInto(period: ReadWritablePeriod, periodStr: String, position: Int, locale: Locale): Int = {
      var oldPos: Int = position
      position = iBeforeParser.parseInto(period, periodStr, position, locale)
      if (position < 0) {
        return position
      }
      var found: Boolean = false
      var parsedFormLength: Int = -1
      if (position > oldPos) {
        val parsedForms: Array[String] = iParsedForms
        val length: Int = parsedForms.length
        {
          var i: Int = 0
          while (i < length) {
            {
              val parsedForm: String = parsedForms(i)
              if ((parsedForm == null || parsedForm.length == 0) || periodStr.regionMatches(true, position, parsedForm, 0, parsedForm.length)) {
                parsedFormLength = (if (parsedForm == null) 0 else parsedForm.length)
                position += parsedFormLength
                found = true
                break //todo: break is not supported
              }
            }
            ({
              i += 1; i - 1
            })
          }
        }
      }
      oldPos = position
      position = iAfterParser.parseInto(period, periodStr, position, locale)
      if (position < 0) {
        return position
      }
      if (found && position == oldPos && parsedFormLength > 0) {
        return ~oldPos
      }
      if (position > oldPos && !found && !iUseBefore) {
        return ~oldPos
      }
      return position
    }

    private[format] def finish(afterPrinter: PeriodPrinter, afterParser: PeriodParser): PeriodFormatterBuilder.Separator = {
      iAfterPrinter = afterPrinter
      iAfterParser = afterParser
      return this
    }
  }

  /**
   * Composite implementation that merges other fields to create a full pattern.
   */
  private[format] class Composite extends PeriodPrinter with PeriodParser {
    private final val iPrinters: Array[PeriodPrinter] = null
    private final val iParsers: Array[PeriodParser] = null

    private[format] def this(elementPairs: List[AnyRef]) {
      this()
      val printerList: List[AnyRef] = new ArrayList[AnyRef]
      val parserList: List[AnyRef] = new ArrayList[AnyRef]
      decompose(elementPairs, printerList, parserList)
      if (printerList.size <= 0) {
        iPrinters = null
      }
      else {
        iPrinters = printerList.toArray(new Array[PeriodPrinter](printerList.size))
      }
      if (parserList.size <= 0) {
        iParsers = null
      }
      else {
        iParsers = parserList.toArray(new Array[PeriodParser](parserList.size))
      }
    }

    def countFieldsToPrint(period: ReadablePeriod, stopAt: Int, locale: Locale): Int = {
      var sum: Int = 0
      val printers: Array[PeriodPrinter] = iPrinters
      {
        var i: Int = printers.length
        while (sum < stopAt && ({
          i -= 1; i
        }) >= 0) {
          sum += printers(i).countFieldsToPrint(period, Integer.MAX_VALUE, locale)
        }
      }
      return sum
    }

    def calculatePrintedLength(period: ReadablePeriod, locale: Locale): Int = {
      var sum: Int = 0
      val printers: Array[PeriodPrinter] = iPrinters
      {
        var i: Int = printers.length
        while (({
          i -= 1; i
        }) >= 0) {
          sum += printers(i).calculatePrintedLength(period, locale)
        }
      }
      return sum
    }

    def printTo(buf: StringBuffer, period: ReadablePeriod, locale: Locale) {
      val printers: Array[PeriodPrinter] = iPrinters
      val len: Int = printers.length
      {
        var i: Int = 0
        while (i < len) {
          {
            printers(i).printTo(buf, period, locale)
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }

    @throws(classOf[IOException])
    def printTo(out: Writer, period: ReadablePeriod, locale: Locale) {
      val printers: Array[PeriodPrinter] = iPrinters
      val len: Int = printers.length
      {
        var i: Int = 0
        while (i < len) {
          {
            printers(i).printTo(out, period, locale)
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }

    def parseInto(period: ReadWritablePeriod, periodStr: String, position: Int, locale: Locale): Int = {
      val parsers: Array[PeriodParser] = iParsers
      if (parsers == null) {
        throw new UnsupportedOperationException
      }
      val len: Int = parsers.length
      {
        var i: Int = 0
        while (i < len && position >= 0) {
          {
            position = parsers(i).parseInto(period, periodStr, position, locale)
          }
          ({
            i += 1; i - 1
          })
        }
      }
      return position
    }

    private def decompose(elementPairs: List[AnyRef], printerList: List[AnyRef], parserList: List[AnyRef]) {
      val size: Int = elementPairs.size
      {
        var i: Int = 0
        while (i < size) {
          {
            var element: AnyRef = elementPairs.get(i)
            if (element.isInstanceOf[PeriodPrinter]) {
              if (element.isInstanceOf[PeriodFormatterBuilder.Composite]) {
                addArrayToList(printerList, (element.asInstanceOf[PeriodFormatterBuilder.Composite]).iPrinters)
              }
              else {
                printerList.add(element)
              }
            }
            element = elementPairs.get(i + 1)
            if (element.isInstanceOf[PeriodParser]) {
              if (element.isInstanceOf[PeriodFormatterBuilder.Composite]) {
                addArrayToList(parserList, (element.asInstanceOf[PeriodFormatterBuilder.Composite]).iParsers)
              }
              else {
                parserList.add(element)
              }
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

}

class PeriodFormatterBuilder {
  private var iMinPrintedDigits: Int = 0
  private var iPrintZeroSetting: Int = 0
  private var iMaxParsedDigits: Int = 0
  private var iRejectSignedValues: Boolean = false
  private var iPrefix: PeriodFormatterBuilder.PeriodFieldAffix = null
  private var iElementPairs: List[AnyRef] = null
  /** Set to true if the formatter is not a printer. */
  private var iNotPrinter: Boolean = false
  /** Set to true if the formatter is not a parser. */
  private var iNotParser: Boolean = false
  private var iFieldFormatters: Array[PeriodFormatterBuilder.FieldFormatter] = null

  def this() {
    this()
    clear
  }

  /**
   * Constructs a PeriodFormatter using all the appended elements.
   * <p>
   * This is the main method used by applications at the end of the build
   * process to create a usable formatter.
   * <p>
   * Subsequent changes to this builder do not affect the returned formatter.
   * <p>
   * The returned formatter may not support both printing and parsing.
   * The methods {@link PeriodFormatter#isPrinter()} and
   * {@link PeriodFormatter#isParser()} will help you determine the state
   * of the formatter.
   *
   * @return the newly created formatter
   * @throws IllegalStateException if the builder can produce neither a printer nor a parser
   */
  def toFormatter: PeriodFormatter = {
    val formatter: PeriodFormatter = PeriodFormatterBuilder.toFormatter(iElementPairs, iNotPrinter, iNotParser)
    for (fieldFormatter <- iFieldFormatters) {
      if (fieldFormatter != null) {
        fieldFormatter.finish(iFieldFormatters)
      }
    }
    iFieldFormatters = iFieldFormatters.clone.asInstanceOf[Array[PeriodFormatterBuilder.FieldFormatter]]
    return formatter
  }

  /**
   * Internal method to create a PeriodPrinter instance using all the
   * appended elements.
   * <p>
   * Most applications will not use this method.
   * If you want a printer in an application, call {@link #toFormatter()}
   * and just use the printing API.
   * <p>
   * Subsequent changes to this builder do not affect the returned printer.
   *
   * @return the newly created printer, null if builder cannot create a printer
   */
  def toPrinter: PeriodPrinter = {
    if (iNotPrinter) {
      return null
    }
    return toFormatter.getPrinter
  }

  /**
   * Internal method to create a PeriodParser instance using all the
   * appended elements.
   * <p>
   * Most applications will not use this method.
   * If you want a printer in an application, call {@link #toFormatter()}
   * and just use the printing API.
   * <p>
   * Subsequent changes to this builder do not affect the returned parser.
   *
   * @return the newly created parser, null if builder cannot create a parser
   */
  def toParser: PeriodParser = {
    if (iNotParser) {
      return null
    }
    return toFormatter.getParser
  }

  /**
   * Clears out all the appended elements, allowing this builder to be reused.
   */
  def clear {
    iMinPrintedDigits = 1
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_RARELY_LAST
    iMaxParsedDigits = 10
    iRejectSignedValues = false
    iPrefix = null
    if (iElementPairs == null) {
      iElementPairs = new ArrayList[AnyRef]
    }
    else {
      iElementPairs.clear
    }
    iNotPrinter = false
    iNotParser = false
    iFieldFormatters = new Array[PeriodFormatterBuilder.FieldFormatter](10)
  }

  /**
   * Appends another formatter.
   *
   * @return this PeriodFormatterBuilder
   */
  def append(formatter: PeriodFormatter): PeriodFormatterBuilder = {
    if (formatter == null) {
      throw new IllegalArgumentException("No formatter supplied")
    }
    clearPrefix
    append0(formatter.getPrinter, formatter.getParser)
    return this
  }

  /**
   * Appends a printer parser pair.
   * <p>
   * Either the printer or the parser may be null, in which case the builder will
   * be unable to produce a parser or printer repectively.
   *
   * @param printer  appends a printer to the builder, null if printing is not supported
   * @param parser  appends a parser to the builder, null if parsing is not supported
   * @return this PeriodFormatterBuilder
   * @throws IllegalArgumentException if both the printer and parser are null
   */
  def append(printer: PeriodPrinter, parser: PeriodParser): PeriodFormatterBuilder = {
    if (printer == null && parser == null) {
      throw new IllegalArgumentException("No printer or parser supplied")
    }
    clearPrefix
    append0(printer, parser)
    return this
  }

  /**
   * Instructs the printer to emit specific text, and the parser to expect it.
   * The parser is case-insensitive.
   *
   * @return this PeriodFormatterBuilder
   * @throws IllegalArgumentException if text is null
   */
  def appendLiteral(text: String): PeriodFormatterBuilder = {
    if (text == null) {
      throw new IllegalArgumentException("Literal must not be null")
    }
    clearPrefix
    val literal: PeriodFormatterBuilder.Literal = new PeriodFormatterBuilder.Literal(text)
    append0(literal, literal)
    return this
  }

  /**
   * Set the minimum digits printed for the next and following appended
   * fields. By default, the minimum digits printed is one. If the field value
   * is zero, it is not printed unless a printZero rule is applied.
   *
   * @return this PeriodFormatterBuilder
   */
  def minimumPrintedDigits(minDigits: Int): PeriodFormatterBuilder = {
    iMinPrintedDigits = minDigits
    return this
  }

  /**
   * Set the maximum digits parsed for the next and following appended
   * fields. By default, the maximum digits parsed is ten.
   *
   * @return this PeriodFormatterBuilder
   */
  def maximumParsedDigits(maxDigits: Int): PeriodFormatterBuilder = {
    iMaxParsedDigits = maxDigits
    return this
  }

  /**
   * Reject signed values when parsing the next and following appended fields.
   *
   * @return this PeriodFormatterBuilder
   */
  def rejectSignedValues(v: Boolean): PeriodFormatterBuilder = {
    iRejectSignedValues = v
    return this
  }

  /**
   * Never print zero values for the next and following appended fields,
   * unless no fields would be printed. If no fields are printed, the printer
   * forces the last "printZeroRarely" field to print a zero.
   * <p>
   * This field setting is the default.
   *
   * @return this PeriodFormatterBuilder
   */
  def printZeroRarelyLast: PeriodFormatterBuilder = {
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_RARELY_LAST
    return this
  }

  /**
   * Never print zero values for the next and following appended fields,
   * unless no fields would be printed. If no fields are printed, the printer
   * forces the first "printZeroRarely" field to print a zero.
   *
   * @return this PeriodFormatterBuilder
   */
  def printZeroRarelyFirst: PeriodFormatterBuilder = {
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_RARELY_FIRST
    return this
  }

  /**
   * Print zero values for the next and following appened fields only if the
   * period supports it.
   *
   * @return this PeriodFormatterBuilder
   */
  def printZeroIfSupported: PeriodFormatterBuilder = {
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_IF_SUPPORTED
    return this
  }

  /**
   * Always print zero values for the next and following appended fields,
   * even if the period doesn't support it. The parser requires values for
   * fields that always print zero.
   *
   * @return this PeriodFormatterBuilder
   */
  def printZeroAlways: PeriodFormatterBuilder = {
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_ALWAYS
    return this
  }

  /**
   * Never print zero values for the next and following appended fields,
   * unless no fields would be printed. If no fields are printed, the printer
   * forces the last "printZeroRarely" field to print a zero.
   * <p>
   * This field setting is the default.
   *
   * @return this PeriodFormatterBuilder
   */
  def printZeroNever: PeriodFormatterBuilder = {
    iPrintZeroSetting = PeriodFormatterBuilder.PRINT_ZERO_NEVER
    return this
  }

  /**
   * Append a field prefix which applies only to the next appended field. If
   * the field is not printed, neither is the prefix.
   *
   * @param text text to print before field only if field is printed
   * @return this PeriodFormatterBuilder
   * @see #appendSuffix
   */
  def appendPrefix(text: String): PeriodFormatterBuilder = {
    if (text == null) {
      throw new IllegalArgumentException
    }
    return appendPrefix(new PeriodFormatterBuilder.SimpleAffix(text))
  }

  /**
   * Append a field prefix which applies only to the next appended field. If
   * the field is not printed, neither is the prefix.
   * <p>
   * During parsing, the singular and plural versions are accepted whether
   * or not the actual value matches plurality.
   *
   * @param singularText text to print if field value is one
   * @param pluralText text to print if field value is not one
   * @return this PeriodFormatterBuilder
   * @see #appendSuffix
   */
  def appendPrefix(singularText: String, pluralText: String): PeriodFormatterBuilder = {
    if (singularText == null || pluralText == null) {
      throw new IllegalArgumentException
    }
    return appendPrefix(new PeriodFormatterBuilder.PluralAffix(singularText, pluralText))
  }

  /**
   * Append a field prefix which applies only to the next appended field.
   * If the field is not printed, neither is the prefix.
   * <p>
   * The value is converted to String. During parsing, the prefix is selected based
   * on the match with the regular expression. The index of the first regular
   * expression that matches value converted to String nominates the prefix. If
   * none of the regular expressions match the value converted to String then the
   * last prefix is selected.
   * <p>
   * An example usage for English might look like this:
   *
   * <pre>
   * appendPrefix(new String[] { &quot;&circ;1$&quot;, &quot;.*&quot; }, new String[] { &quot; year&quot;, &quot; years&quot; })
   * </pre>
   *
   * <p>
   * Please note that for languages with simple mapping (singular and plural prefix
   * only - like the one above) the {@link #appendPrefix(String, String)} method
   * will produce in a slightly faster formatter and that
   * {@link #appendPrefix(String[], String[])} method should be only used when the
   * mapping between values and prefixes is more complicated than the difference between
   * singular and plural.
   *
   * @param regularExpressions  an array of regular expressions, at least one
   *                            element, length has to match the length of prefixes parameter
   * @param prefixes  an array of prefixes, at least one element, length has to
   *                  match the length of regularExpressions parameter
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if no field exists to append to
   * @see #appendPrefix
   * @since 2.5
   */
  def appendPrefix(regularExpressions: Array[String], prefixes: Array[String]): PeriodFormatterBuilder = {
    if (regularExpressions == null || prefixes == null || regularExpressions.length < 1 || regularExpressions.length != prefixes.length) {
      throw new IllegalArgumentException
    }
    return appendPrefix(new PeriodFormatterBuilder.RegExAffix(regularExpressions, prefixes))
  }

  /**
   * Append a field prefix which applies only to the next appended field. If
   * the field is not printed, neither is the prefix.
   *
   * @param prefix custom prefix
   * @return this PeriodFormatterBuilder
   * @see #appendSuffix
   */
  private def appendPrefix(prefix: PeriodFormatterBuilder.PeriodFieldAffix): PeriodFormatterBuilder = {
    if (prefix == null) {
      throw new IllegalArgumentException
    }
    if (iPrefix != null) {
      prefix = new PeriodFormatterBuilder.CompositeAffix(iPrefix, prefix)
    }
    iPrefix = prefix
    return this
  }

  /**
   * Instruct the printer to emit an integer years field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendYears: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.YEARS)
    return this
  }

  /**
   * Instruct the printer to emit an integer months field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendMonths: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.MONTHS)
    return this
  }

  /**
   * Instruct the printer to emit an integer weeks field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendWeeks: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.WEEKS)
    return this
  }

  /**
   * Instruct the printer to emit an integer days field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendDays: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.DAYS)
    return this
  }

  /**
   * Instruct the printer to emit an integer hours field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendHours: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.HOURS)
    return this
  }

  /**
   * Instruct the printer to emit an integer minutes field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendMinutes: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.MINUTES)
    return this
  }

  /**
   * Instruct the printer to emit an integer seconds field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendSeconds: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.SECONDS)
    return this
  }

  /**
   * Instruct the printer to emit a combined seconds and millis field, if supported.
   * The millis will overflow into the seconds if necessary.
   * The millis are always output.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendSecondsWithMillis: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.SECONDS_MILLIS)
    return this
  }

  /**
   * Instruct the printer to emit a combined seconds and millis field, if supported.
   * The millis will overflow into the seconds if necessary.
   * The millis are only output if non-zero.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendSecondsWithOptionalMillis: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.SECONDS_OPTIONAL_MILLIS)
    return this
  }

  /**
   * Instruct the printer to emit an integer millis field, if supported.
   * <p>
   * The number of printed and parsed digits can be controlled using
   * {@link #minimumPrintedDigits(int)} and {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendMillis: PeriodFormatterBuilder = {
    appendField(PeriodFormatterBuilder.MILLIS)
    return this
  }

  /**
   * Instruct the printer to emit an integer millis field, if supported.
   * <p>
   * The number of arsed digits can be controlled using {@link #maximumParsedDigits(int)}.
   *
   * @return this PeriodFormatterBuilder
   */
  def appendMillis3Digit: PeriodFormatterBuilder = {
    appendField(7, 3)
    return this
  }

  private def appendField(`type`: Int) {
    appendField(`type`, iMinPrintedDigits)
  }

  private def appendField(`type`: Int, minPrinted: Int) {
    val field: PeriodFormatterBuilder.FieldFormatter = new PeriodFormatterBuilder.FieldFormatter(minPrinted, iPrintZeroSetting, iMaxParsedDigits, iRejectSignedValues, `type`, iFieldFormatters, iPrefix, null)
    append0(field, field)
    iFieldFormatters(`type`) = field
    iPrefix = null
  }

  /**
   * Append a field suffix which applies only to the last appended field. If
   * the field is not printed, neither is the suffix.
   *
   * @param text text to print after field only if field is printed
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if no field exists to append to
   * @see #appendPrefix
   */
  def appendSuffix(text: String): PeriodFormatterBuilder = {
    if (text == null) {
      throw new IllegalArgumentException
    }
    return appendSuffix(new PeriodFormatterBuilder.SimpleAffix(text))
  }

  /**
   * Append a field suffix which applies only to the last appended field. If
   * the field is not printed, neither is the suffix.
   * <p>
   * During parsing, the singular and plural versions are accepted whether or
   * not the actual value matches plurality.
   *
   * @param singularText text to print if field value is one
   * @param pluralText text to print if field value is not one
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if no field exists to append to
   * @see #appendPrefix
   */
  def appendSuffix(singularText: String, pluralText: String): PeriodFormatterBuilder = {
    if (singularText == null || pluralText == null) {
      throw new IllegalArgumentException
    }
    return appendSuffix(new PeriodFormatterBuilder.PluralAffix(singularText, pluralText))
  }

  /**
   * Append a field suffix which applies only to the last appended field.
   * If the field is not printed, neither is the suffix.
   * <p>
   * The value is converted to String. During parsing, the suffix is selected based
   * on the match with the regular expression. The index of the first regular
   * expression that matches value converted to String nominates the suffix. If
   * none of the regular expressions match the value converted to String then the
   * last suffix is selected.
   * <p>
   * An example usage for English might look like this:
   *
   * <pre>
   * appendSuffix(new String[] { &quot;&circ;1$&quot;, &quot;.*&quot; }, new String[] { &quot; year&quot;, &quot; years&quot; })
   * </pre>
   *
   * <p>
   * Please note that for languages with simple mapping (singular and plural suffix
   * only - like the one above) the {@link #appendSuffix(String, String)} method
   * will result in a slightly faster formatter and that
   * {@link #appendSuffix(String[], String[])} method should be only used when the
   * mapping between values and prefixes is more complicated than the difference between
   * singular and plural.
   *
   * @param regularExpressions  an array of regular expressions, at least one
   *                            element, length has to match the length of suffixes parameter
   * @param suffixes  an array of suffixes, at least one element, length has to
   *                  match the length of regularExpressions parameter
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if no field exists to append to
   * @see #appendPrefix
   * @since 2.5
   */
  def appendSuffix(regularExpressions: Array[String], suffixes: Array[String]): PeriodFormatterBuilder = {
    if (regularExpressions == null || suffixes == null || regularExpressions.length < 1 || regularExpressions.length != suffixes.length) {
      throw new IllegalArgumentException
    }
    return appendSuffix(new PeriodFormatterBuilder.RegExAffix(regularExpressions, suffixes))
  }

  /**
   * Append a field suffix which applies only to the last appended field. If
   * the field is not printed, neither is the suffix.
   *
   * @param suffix custom suffix
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if no field exists to append to
   * @see #appendPrefix
   */
  private def appendSuffix(suffix: PeriodFormatterBuilder.PeriodFieldAffix): PeriodFormatterBuilder = {
    val originalPrinter: AnyRef = null
    val originalParser: AnyRef = null
    if (iElementPairs.size > 0) {
      originalPrinter = iElementPairs.get(iElementPairs.size - 2)
      originalParser = iElementPairs.get(iElementPairs.size - 1)
    }
    else {
      originalPrinter = null
      originalParser = null
    }
    if (originalPrinter == null || originalParser == null || originalPrinter ne originalParser || !(originalPrinter.isInstanceOf[PeriodFormatterBuilder.FieldFormatter])) {
      throw new IllegalStateException("No field to apply suffix to")
    }
    clearPrefix
    val newField: PeriodFormatterBuilder.FieldFormatter = new PeriodFormatterBuilder.FieldFormatter(originalPrinter.asInstanceOf[PeriodFormatterBuilder.FieldFormatter], suffix)
    iElementPairs.set(iElementPairs.size - 2, newField)
    iElementPairs.set(iElementPairs.size - 1, newField)
    iFieldFormatters(newField.getFieldType) = newField
    return this
  }

  /**
   * Append a separator, which is output if fields are printed both before
   * and after the separator.
   * <p>
   * For example, <code>builder.appendDays().appendSeparator(",").appendHours()</code>
   * will only output the comma if both the days and hours fields are output.
   * <p>
   * The text will be parsed case-insensitively.
   * <p>
   * Note: appending a separator discontinues any further work on the latest
   * appended field.
   *
   * @param text  the text to use as a separator
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if this separator follows a previous one
   */
  def appendSeparator(text: String): PeriodFormatterBuilder = {
    return appendSeparator(text, text, null, true, true)
  }

  /**
   * Append a separator, which is output only if fields are printed after the separator.
   * <p>
   * For example,
   * <code>builder.appendDays().appendSeparatorIfFieldsAfter(",").appendHours()</code>
   * will only output the comma if the hours fields is output.
   * <p>
   * The text will be parsed case-insensitively.
   * <p>
   * Note: appending a separator discontinues any further work on the latest
   * appended field.
   *
   * @param text  the text to use as a separator
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if this separator follows a previous one
   */
  def appendSeparatorIfFieldsAfter(text: String): PeriodFormatterBuilder = {
    return appendSeparator(text, text, null, false, true)
  }

  /**
   * Append a separator, which is output only if fields are printed before the separator.
   * <p>
   * For example,
   * <code>builder.appendDays().appendSeparatorIfFieldsBefore(",").appendHours()</code>
   * will only output the comma if the days fields is output.
   * <p>
   * The text will be parsed case-insensitively.
   * <p>
   * Note: appending a separator discontinues any further work on the latest
   * appended field.
   *
   * @param text  the text to use as a separator
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if this separator follows a previous one
   */
  def appendSeparatorIfFieldsBefore(text: String): PeriodFormatterBuilder = {
    return appendSeparator(text, text, null, true, false)
  }

  /**
   * Append a separator, which is output if fields are printed both before
   * and after the separator.
   * <p>
   * This method changes the separator depending on whether it is the last separator
   * to be output.
   * <p>
   * For example, <code>builder.appendDays().appendSeparator(",", "&").appendHours().appendSeparator(",", "&").appendMinutes()</code>
   * will output '1,2&3' if all three fields are output, '1&2' if two fields are output
   * and '1' if just one field is output.
   * <p>
   * The text will be parsed case-insensitively.
   * <p>
   * Note: appending a separator discontinues any further work on the latest
   * appended field.
   *
   * @param text  the text to use as a separator
   * @param finalText  the text used used if this is the final separator to be printed
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if this separator follows a previous one
   */
  def appendSeparator(text: String, finalText: String): PeriodFormatterBuilder = {
    return appendSeparator(text, finalText, null, true, true)
  }

  /**
   * Append a separator, which is output if fields are printed both before
   * and after the separator.
   * <p>
   * This method changes the separator depending on whether it is the last separator
   * to be output.
   * <p>
   * For example, <code>builder.appendDays().appendSeparator(",", "&").appendHours().appendSeparator(",", "&").appendMinutes()</code>
   * will output '1,2&3' if all three fields are output, '1&2' if two fields are output
   * and '1' if just one field is output.
   * <p>
   * The text will be parsed case-insensitively.
   * <p>
   * Note: appending a separator discontinues any further work on the latest
   * appended field.
   *
   * @param text  the text to use as a separator
   * @param finalText  the text used used if this is the final separator to be printed
   * @param variants  set of text values which are also acceptable when parsed
   * @return this PeriodFormatterBuilder
   * @throws IllegalStateException if this separator follows a previous one
   */
  def appendSeparator(text: String, finalText: String, variants: Array[String]): PeriodFormatterBuilder = {
    return appendSeparator(text, finalText, variants, true, true)
  }

  private def appendSeparator(text: String, finalText: String, variants: Array[String], useBefore: Boolean, useAfter: Boolean): PeriodFormatterBuilder = {
    if (text == null || finalText == null) {
      throw new IllegalArgumentException
    }
    clearPrefix
    var pairs: List[AnyRef] = iElementPairs
    if (pairs.size == 0) {
      if (useAfter && useBefore == false) {
        val separator: PeriodFormatterBuilder.Separator = new PeriodFormatterBuilder.Separator(text, finalText, variants, PeriodFormatterBuilder.Literal.EMPTY, PeriodFormatterBuilder.Literal.EMPTY, useBefore, useAfter)
        append0(separator, separator)
      }
      return this
    }
    var i: Int = 0
    var lastSeparator: PeriodFormatterBuilder.Separator = null
    {
      i = pairs.size
      while (({
        i -= 1; i
      }) >= 0) {
        if (pairs.get(i).isInstanceOf[PeriodFormatterBuilder.Separator]) {
          lastSeparator = pairs.get(i).asInstanceOf[PeriodFormatterBuilder.Separator]
          pairs = pairs.subList(i + 1, pairs.size)
          break //todo: break is not supported
        }
        i -= 1
      }
    }
    if (lastSeparator != null && pairs.size == 0) {
      throw new IllegalStateException("Cannot have two adjacent separators")
    }
    else {
      val comp: Array[AnyRef] = PeriodFormatterBuilder.createComposite(pairs)
      pairs.clear
      val separator: PeriodFormatterBuilder.Separator = new PeriodFormatterBuilder.Separator(text, finalText, variants, comp(0).asInstanceOf[PeriodPrinter], comp(1).asInstanceOf[PeriodParser], useBefore, useAfter)
      pairs.add(separator)
      pairs.add(separator)
    }
    return this
  }

  @throws(classOf[IllegalStateException])
  private def clearPrefix {
    if (iPrefix != null) {
      throw new IllegalStateException("Prefix not followed by field")
    }
    iPrefix = null
  }

  private def append0(printer: PeriodPrinter, parser: PeriodParser): PeriodFormatterBuilder = {
    iElementPairs.add(printer)
    iElementPairs.add(parser)
    iNotPrinter |= (printer == null)
    iNotParser |= (parser == null)
    return this
  }
}