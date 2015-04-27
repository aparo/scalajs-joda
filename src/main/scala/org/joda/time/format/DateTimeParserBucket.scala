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

import java.util.Arrays
import java.util.Locale
import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.IllegalFieldValueException
import org.joda.time.IllegalInstantException

/**
 * DateTimeParserBucket is an advanced class, intended mainly for parser
 * implementations. It can also be used during normal parsing operations to
 * capture more information about the parse.
 * <p>
 * This class allows fields to be saved in any order, but be physically set in
 * a consistent order. This is useful for parsing against formats that allow
 * field values to contradict each other.
 * <p>
 * Field values are applied in an order where the "larger" fields are set
 * first, making their value less likely to stick.  A field is larger than
 * another when it's range duration is longer. If both ranges are the same,
 * then the larger field has the longer duration. If it cannot be determined
 * which field is larger, then the fields are set in the order they were saved.
 * <p>
 * For example, these fields were saved in this order: dayOfWeek, monthOfYear,
 * dayOfMonth, dayOfYear. When computeMillis is called, the fields are set in
 * this order: monthOfYear, dayOfYear, dayOfMonth, dayOfWeek.
 * <p>
 * DateTimeParserBucket is mutable and not thread-safe.
 *
 * @author Brian S O'Neill
 * @author Fredrik Borgh
 * @since 1.0
 */
object DateTimeParserBucket {
  /**
   * Sorts elements [0,high). Calling java.util.Arrays isn't always the right
   * choice since it always creates an internal copy of the array, even if it
   * doesn't need to. If the array slice is small enough, an insertion sort
   * is chosen instead, but it doesn't need a copy!
   * <p>
   * This method has a modified version of that insertion sort, except it
   * doesn't create an unnecessary array copy. If high is over 10, then
   * java.util.Arrays is called, which will perform a merge sort, which is
   * faster than insertion sort on large lists.
   * <p>
   * The end result is much greater performance when computeMillis is called.
   * Since the amount of saved fields is small, the insertion sort is a
   * better choice. Additional performance is gained since there is no extra
   * array allocation and copying. Also, the insertion sort here does not
   * perform any casting operations. The version in java.util.Arrays performs
   * casts within the insertion sort loop.
   */
  private def sort(array: Array[DateTimeParserBucket.SavedField], high: Int) {
    if (high > 10) {
      Arrays.sort(array, 0, high)
    }
    else {
      {
        var i: Int = 0
        while (i < high) {
          {
            {
              var j: Int = i
              while (j > 0 && (array(j - 1)).compareTo(array(j)) > 0) {
                {
                  val t: DateTimeParserBucket.SavedField = array(j)
                  array(j) = array(j - 1)
                  array(j - 1) = t
                }
                ({
                  j -= 1; j + 1
                })
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
  }

  private[format] class SavedField extends Comparable[DateTimeParserBucket.SavedField] {
    private[format] var iField: DateTimeField = null
    private[format] var iValue: Int = 0
    private[format] var iText: String = null
    private[format] var iLocale: Locale = null

    private[format] def this() {
      this()
    }

    private[format] def init(field: DateTimeField, value: Int) {
      iField = field
      iValue = value
      iText = null
      iLocale = null
    }

    private[format] def init(field: DateTimeField, text: String, locale: Locale) {
      iField = field
      iValue = 0
      iText = text
      iLocale = locale
    }

    private[format] def set(millis: Long, reset: Boolean): Long = {
      if (iText == null) {
        millis = iField.set(millis, iValue)
      }
      else {
        millis = iField.set(millis, iText, iLocale)
      }
      if (reset) {
        millis = iField.roundFloor(millis)
      }
      return millis
    }

    /**
     * The field with the longer range duration is ordered first, where
     * null is considered infinite. If the ranges match, then the field
     * with the longer duration is ordered first.
     */
    def compareTo(obj: DateTimeParserBucket.SavedField): Int = {
      val other: DateTimeField = obj.iField
      val result: Int = compareReverse(iField.getRangeDurationField, other.getRangeDurationField)
      if (result != 0) {
        return result
      }
      return compareReverse(iField.getDurationField, other.getDurationField)
    }
  }

  private[format] def compareReverse(a: DurationField, b: DurationField): Int = {
    if (a == null || !a.isSupported) {
      if (b == null || !b.isSupported) {
        return 0
      }
      return -1
    }
    if (b == null || !b.isSupported) {
      return 1
    }
    return -a.compareTo(b)
  }
}

class DateTimeParserBucket {
  /** The chronology to use for parsing. */
  private final val iChrono: Chronology = null
  /** The initial millis. */
  private final val iMillis: Long = 0L
  /** The locale to use for parsing. */
  private final val iLocale: Locale = null
  /** Used for parsing month/day without year. */
  private final val iDefaultYear: Int = 0
  /** The default zone from the constructor. */
  private final val iDefaultZone: DateTimeZone = null
  /** The default pivot year from the constructor. */
  private final val iDefaultPivotYear: Integer = null
  /** The parsed zone, initialised to formatter zone. */
  private var iZone: DateTimeZone = null
  /** The parsed offset. */
  private var iOffset: Integer = null
  /** Used for parsing two-digit years. */
  private var iPivotYear: Integer = null
  private var iSavedFields: Array[DateTimeParserBucket.SavedField] = null
  private var iSavedFieldsCount: Int = 0
  private var iSavedFieldsShared: Boolean = false
  private var iSavedState: AnyRef = null

  /**
   * Constructs a bucket.
   *
   * @param instantLocal  the initial millis from 1970-01-01T00:00:00, local time
   * @param chrono  the chronology to use
   * @param locale  the locale to use
   * @deprecated Use longer constructor
   */
  @deprecated def this(instantLocal: Long, chrono: Chronology, locale: Locale) {
    this()
    `this`(instantLocal, chrono, locale, null, 2000)
  }

  /**
   * Constructs a bucket, with the option of specifying the pivot year for
   * two-digit year parsing.
   *
   * @param instantLocal  the initial millis from 1970-01-01T00:00:00, local time
   * @param chrono  the chronology to use
   * @param locale  the locale to use
   * @param pivotYear  the pivot year to use when parsing two-digit years
   * @since 1.1
   * @deprecated Use longer constructor
   */
  @deprecated def this(instantLocal: Long, chrono: Chronology, locale: Locale, pivotYear: Integer) {
    this()
    `this`(instantLocal, chrono, locale, pivotYear, 2000)
  }

  /**
   * Constructs a bucket, with the option of specifying the pivot year for
   * two-digit year parsing.
   *
   * @param instantLocal  the initial millis from 1970-01-01T00:00:00, local time
   * @param chrono  the chronology to use
   * @param locale  the locale to use
   * @param pivotYear  the pivot year to use when parsing two-digit years
   * @param defaultYear  the default year to use when parsing month-day
   * @since 2.0
   */
  def this(instantLocal: Long, chrono: Chronology, locale: Locale, pivotYear: Integer, defaultYear: Int) {
    this()
    `super`
    chrono = DateTimeUtils.getChronology(chrono)
    iMillis = instantLocal
    iDefaultZone = chrono.getZone
    iChrono = chrono.withUTC
    iLocale = (if (locale == null) Locale.getDefault else locale)
    iDefaultYear = defaultYear
    iDefaultPivotYear = pivotYear
    iZone = iDefaultZone
    iPivotYear = iDefaultPivotYear
    iSavedFields = new Array[DateTimeParserBucket.SavedField](8)
  }

  /**
   * Resets the state back to that when the object was constructed.
   * <p>
   * This resets the state of the bucket, allowing a single bucket to be re-used
   * for many parses. The bucket must not be shared between threads.
   *
   * @since 2.4
   */
  def reset {
    iZone = iDefaultZone
    iOffset = null
    iPivotYear = iDefaultPivotYear
    iSavedFieldsCount = 0
    iSavedFieldsShared = false
    iSavedState = null
  }

  /**
   * Parses a datetime from the given text, returning the number of
   * milliseconds since the epoch, 1970-01-01T00:00:00Z.
   * <p>
   * This parses the text using the parser into this bucket.
   * The bucket is reset before parsing begins, allowing the bucket to be re-used.
   * The bucket must not be shared between threads.
   *
   * @param parser  the parser to use, see { @link DateTimeFormatter#getParser()}, not null
   * @param text  text to parse, not null
   * @return parsed value expressed in milliseconds since the epoch
   * @throws UnsupportedOperationException if parsing is not supported
   * @throws IllegalArgumentException if the text to parse is invalid
   * @since 2.4
   */
  def parseMillis(parser: DateTimeParser, text: CharSequence): Long = {
    reset
    return doParseMillis(DateTimeParserInternalParser.of(parser), text)
  }

  private[format] def doParseMillis(parser: InternalParser, text: CharSequence): Long = {
    var newPos: Int = parser.parseInto(this, text, 0)
    if (newPos >= 0) {
      if (newPos >= text.length) {
        return computeMillis(true, text)
      }
    }
    else {
      newPos = ~newPos
    }
    throw new IllegalArgumentException(FormatUtils.createErrorMessage(text.toString, newPos))
  }

  /**
   * Gets the chronology of the bucket, which will be a local (UTC) chronology.
   */
  def getChronology: Chronology = {
    return iChrono
  }

  /**
   * Returns the locale to be used during parsing.
   *
   * @return the locale to use
   */
  def getLocale: Locale = {
    return iLocale
  }

  /**
   * Returns the time zone used by computeMillis.
   */
  def getZone: DateTimeZone = {
    return iZone
  }

  /**
   * Set a time zone to be used when computeMillis is called.
   */
  def setZone(zone: DateTimeZone) {
    iSavedState = null
    iZone = zone
  }

  /**
   * Returns the time zone offset in milliseconds used by computeMillis.
   * @deprecated use Integer version
   */
  @deprecated def getOffset: Int = {
    return (if (iOffset != null) iOffset else 0)
  }

  /**
   * Returns the time zone offset in milliseconds used by computeMillis.
   */
  def getOffsetInteger: Integer = {
    return iOffset
  }

  /**
   * Set a time zone offset to be used when computeMillis is called.
   * @deprecated use Integer version
   */
  @deprecated def setOffset(offset: Int) {
    iSavedState = null
    iOffset = offset
  }

  /**
   * Set a time zone offset to be used when computeMillis is called.
   */
  def setOffset(offset: Integer) {
    iSavedState = null
    iOffset = offset
  }

  /**
   * Returns the default year used when information is incomplete.
   * <p>
   * This is used for two-digit years and when the largest parsed field is
   * months or days.
   * <p>
   * A null value for two-digit years means to use the value from DateTimeFormatterBuilder.
   * A null value for month/day only parsing will cause the default of 2000 to be used.
   *
   * @return Integer value of the pivot year, null if not set
   * @since 1.1
   */
  def getPivotYear: Integer = {
    return iPivotYear
  }

  /**
   * Sets the pivot year to use when parsing two digit years.
   * <p>
   * If the value is set to null, this will indicate that default
   * behaviour should be used.
   *
   * @param pivotYear  the pivot year to use
   * @since 1.1
   * @deprecated this method should never have been public
   */
  @deprecated def setPivotYear(pivotYear: Integer) {
    iPivotYear = pivotYear
  }

  /**
   * Saves a datetime field value.
   *
   * @param field  the field, whose chronology must match that of this bucket
   * @param value  the value
   */
  def saveField(field: DateTimeField, value: Int) {
    obtainSaveField.init(field, value)
  }

  /**
   * Saves a datetime field value.
   *
   * @param fieldType  the field type
   * @param value  the value
   */
  def saveField(fieldType: DateTimeFieldType, value: Int) {
    obtainSaveField.init(fieldType.getField(iChrono), value)
  }

  /**
   * Saves a datetime field text value.
   *
   * @param fieldType  the field type
   * @param text  the text value
   * @param locale  the locale to use
   */
  def saveField(fieldType: DateTimeFieldType, text: String, locale: Locale) {
    obtainSaveField.init(fieldType.getField(iChrono), text, locale)
  }

  private def obtainSaveField: DateTimeParserBucket.SavedField = {
    val savedFields: Array[DateTimeParserBucket.SavedField] = iSavedFields
    val savedFieldsCount: Int = iSavedFieldsCount
    if (savedFieldsCount == savedFields.length || iSavedFieldsShared) {
      val newArray: Array[DateTimeParserBucket.SavedField] = new Array[DateTimeParserBucket.SavedField](if (savedFieldsCount == savedFields.length) savedFieldsCount * 2 else savedFields.length)
      System.arraycopy(savedFields, 0, newArray, 0, savedFieldsCount)
      iSavedFields = ({
        savedFields = newArray; savedFields
      })
      iSavedFieldsShared = false
    }
    iSavedState = null
    var saved: DateTimeParserBucket.SavedField = savedFields(savedFieldsCount)
    if (saved == null) {
      saved = ({
        savedFields(savedFieldsCount) = new DateTimeParserBucket.SavedField; savedFields(savedFieldsCount)
      })
    }
    iSavedFieldsCount = savedFieldsCount + 1
    return saved
  }

  /**
   * Saves the state of this bucket, returning it in an opaque object. Call
   * restoreState to undo any changes that were made since the state was
   * saved. Calls to saveState may be nested.
   *
   * @return opaque saved state, which may be passed to restoreState
   */
  def saveState: AnyRef = {
    if (iSavedState == null) {
      iSavedState = new DateTimeParserBucket#SavedState
    }
    return iSavedState
  }

  /**
   * Restores the state of this bucket from a previously saved state. The
   * state object passed into this method is not consumed, and it can be used
   * later to restore to that state again.
   *
   * @param savedState opaque saved state, returned from saveState
   * @return true state object is valid and state restored
   */
  def restoreState(savedState: AnyRef): Boolean = {
    if (savedState.isInstanceOf[DateTimeParserBucket#SavedState]) {
      if ((savedState.asInstanceOf[DateTimeParserBucket#SavedState]).restoreState(this)) {
        iSavedState = savedState
        return true
      }
    }
    return false
  }

  /**
   * Computes the parsed datetime by setting the saved fields.
   * This method is idempotent, but it is not thread-safe.
   *
   * @return milliseconds since 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if any field is out of range
   */
  def computeMillis: Long = {
    return computeMillis(false, null.asInstanceOf[CharSequence])
  }

  /**
   * Computes the parsed datetime by setting the saved fields.
   * This method is idempotent, but it is not thread-safe.
   *
   * @param resetFields false by default, but when true, unsaved field values are cleared
   * @return milliseconds since 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if any field is out of range
   */
  def computeMillis(resetFields: Boolean): Long = {
    return computeMillis(resetFields, null.asInstanceOf[CharSequence])
  }

  /**
   * Computes the parsed datetime by setting the saved fields.
   * This method is idempotent, but it is not thread-safe.
   *
   * @param resetFields false by default, but when true, unsaved field values are cleared
   * @param text optional text being parsed, to be included in any error message
   * @return milliseconds since 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if any field is out of range
   * @since 1.3
   */
  def computeMillis(resetFields: Boolean, text: String): Long = {
    return computeMillis(resetFields, text.asInstanceOf[CharSequence])
  }

  /**
   * Computes the parsed datetime by setting the saved fields.
   * This method is idempotent, but it is not thread-safe.
   *
   * @param resetFields false by default, but when true, unsaved field values are cleared
   * @param text optional text being parsed, to be included in any error message
   * @return milliseconds since 1970-01-01T00:00:00Z
   * @throws IllegalArgumentException if any field is out of range
   * @since 2.4
   */
  def computeMillis(resetFields: Boolean, text: CharSequence): Long = {
    val savedFields: Array[DateTimeParserBucket.SavedField] = iSavedFields
    val count: Int = iSavedFieldsCount
    if (iSavedFieldsShared) {
      iSavedFields = ({
        savedFields = iSavedFields.clone.asInstanceOf[Array[DateTimeParserBucket.SavedField]]; savedFields
      })
      iSavedFieldsShared = false
    }
    DateTimeParserBucket.sort(savedFields, count)
    if (count > 0) {
      val months: DurationField = DurationFieldType.months.getField(iChrono)
      val days: DurationField = DurationFieldType.days.getField(iChrono)
      val first: DurationField = savedFields(0).iField.getDurationField
      if (DateTimeParserBucket.compareReverse(first, months) >= 0 && DateTimeParserBucket.compareReverse(first, days) <= 0) {
        saveField(DateTimeFieldType.year, iDefaultYear)
        return computeMillis(resetFields, text)
      }
    }
    var millis: Long = iMillis
    try { {
      var i: Int = 0
      while (i < count) {
        {
          millis = savedFields(i).set(millis, resetFields)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    if (resetFields) {
      {
        var i: Int = 0
        while (i < count) {
          {
            millis = savedFields(i).set(millis, i == (count - 1))
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    }
    catch {
      case e: IllegalFieldValueException => {
        if (text != null) {
          e.prependMessage("Cannot parse \"" + text + '"')
        }
        throw e
      }
    }
    if (iOffset != null) {
      millis -= iOffset
    }
    else if (iZone != null) {
      val offset: Int = iZone.getOffsetFromLocal(millis)
      millis -= offset
      if (offset != iZone.getOffset(millis)) {
        var message: String = "Illegal instant due to time zone offset transition (" + iZone + ')'
        if (text != null) {
          message = "Cannot parse \"" + text + "\": " + message
        }
        throw new IllegalInstantException(message)
      }
    }
    return millis
  }

  private[format] class SavedState {
    private[format] final val iZone: DateTimeZone = null
    private[format] final val iOffset: Integer = null
    private[format] final val iSavedFields: Array[DateTimeParserBucket.SavedField] = null
    private[format] final val iSavedFieldsCount: Int = 0

    private[format] def this() {
      this()
      this.iZone = DateTimeParserBucket.this.iZone
      this.iOffset = DateTimeParserBucket.this.iOffset
      this.iSavedFields = DateTimeParserBucket.this.iSavedFields
      this.iSavedFieldsCount = DateTimeParserBucket.this.iSavedFieldsCount
    }

    private[format] def restoreState(enclosing: DateTimeParserBucket): Boolean = {
      if (enclosing ne DateTimeParserBucket.this) {
        return false
      }
      enclosing.iZone = this.iZone
      enclosing.iOffset = this.iOffset
      enclosing.iSavedFields = this.iSavedFields
      if (this.iSavedFieldsCount < enclosing.iSavedFieldsCount) {
        enclosing.iSavedFieldsShared = true
      }
      enclosing.iSavedFieldsCount = this.iSavedFieldsCount
      return true
    }
  }

}