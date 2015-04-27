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
package org.joda.time.tz

import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.DateFormatSymbols
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import java.util.Iterator
import java.util.Locale
import java.util.Set
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.chrono.ISOChronology

/**
 * DateTimeZoneBuilder allows complex DateTimeZones to be constructed. Since
 * creating a new DateTimeZone this way is a relatively expensive operation,
 * built zones can be written to a file. Reading back the encoded data is a
 * quick operation.
 * <p>
 * DateTimeZoneBuilder itself is mutable and not thread-safe, but the
 * DateTimeZone objects that it builds are thread-safe and immutable.
 * <p>
 * It is intended that {@link ZoneInfoCompiler} be used to read time zone data
 * files, indirectly calling DateTimeZoneBuilder. The following complex
 * example defines the America/Los_Angeles time zone, with all historical
 * transitions:
 *
 * <pre>
 * DateTimeZone America_Los_Angeles = new DateTimeZoneBuilder()
 * .addCutover(-2147483648, 'w', 1, 1, 0, false, 0)
 * .setStandardOffset(-28378000)
 * .setFixedSavings("LMT", 0)
 * .addCutover(1883, 'w', 11, 18, 0, false, 43200000)
 * .setStandardOffset(-28800000)
 * .addRecurringSavings("PDT", 3600000, 1918, 1919, 'w',  3, -1, 7, false, 7200000)
 * .addRecurringSavings("PST",       0, 1918, 1919, 'w', 10, -1, 7, false, 7200000)
 * .addRecurringSavings("PWT", 3600000, 1942, 1942, 'w',  2,  9, 0, false, 7200000)
 * .addRecurringSavings("PPT", 3600000, 1945, 1945, 'u',  8, 14, 0, false, 82800000)
 * .addRecurringSavings("PST",       0, 1945, 1945, 'w',  9, 30, 0, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1948, 1948, 'w',  3, 14, 0, false, 7200000)
 * .addRecurringSavings("PST",       0, 1949, 1949, 'w',  1,  1, 0, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1950, 1966, 'w',  4, -1, 7, false, 7200000)
 * .addRecurringSavings("PST",       0, 1950, 1961, 'w',  9, -1, 7, false, 7200000)
 * .addRecurringSavings("PST",       0, 1962, 1966, 'w', 10, -1, 7, false, 7200000)
 * .addRecurringSavings("PST",       0, 1967, 2147483647, 'w', 10, -1, 7, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1967, 1973, 'w', 4, -1,  7, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1974, 1974, 'w', 1,  6,  0, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1975, 1975, 'w', 2, 23,  0, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1976, 1986, 'w', 4, -1,  7, false, 7200000)
 * .addRecurringSavings("PDT", 3600000, 1987, 2147483647, 'w', 4, 1, 7, true, 7200000)
 * .toDateTimeZone("America/Los_Angeles", true);
 * </pre>
 *
 * @author Brian S O'Neill
 * @see ZoneInfoCompiler
 * @see ZoneInfoProvider
 * @since 1.0
 */
object DateTimeZoneBuilder {
  /**
   * Decodes a built DateTimeZone from the given stream, as encoded by
   * writeTo.
   *
   * @param in input stream to read encoded DateTimeZone from.
   * @param id time zone id to assign
   */
  @throws(classOf[IOException])
  def readFrom(in: InputStream, id: String): DateTimeZone = {
    if (in.isInstanceOf[DataInput]) {
      return readFrom(in.asInstanceOf[DataInput], id)
    }
    else {
      return readFrom(new DataInputStream(in).asInstanceOf[DataInput], id)
    }
  }

  /**
   * Decodes a built DateTimeZone from the given stream, as encoded by
   * writeTo.
   *
   * @param in input stream to read encoded DateTimeZone from.
   * @param id time zone id to assign
   */
  @throws(classOf[IOException])
  def readFrom(in: DataInput, id: String): DateTimeZone = {
    in.readUnsignedByte match {
      case 'F' =>
        var fixed: DateTimeZone = new FixedDateTimeZone(id, in.readUTF, readMillis(in).toInt, readMillis(in).toInt)
        if (fixed == DateTimeZone.UTC) {
          fixed = DateTimeZone.UTC
        }
        return fixed
      case 'C' =>
        return CachedDateTimeZone.forZone(PrecalculatedZone.readFrom(in, id))
      case 'P' =>
        return PrecalculatedZone.readFrom(in, id)
      case _ =>
        throw new IOException("Invalid encoding")
    }
  }

  /**
   * Millisecond encoding formats:
   *
   * upper two bits  units       field length  approximate range
   * ---------------------------------------------------------------
   * 00              30 minutes  1 byte        +/- 16 hours
   * 01              minutes     4 bytes       +/- 1020 years
   * 10              seconds     5 bytes       +/- 4355 years
   * 11              millis      9 bytes       +/- 292,000,000 years
   *
   * Remaining bits in field form signed offset from 1970-01-01T00:00:00Z.
   */
  @throws(classOf[IOException])
  private[tz] def writeMillis(out: DataOutput, millis: Long) {
    if (millis % (30 * 60000L) == 0) {
      val units: Long = millis / (30 * 60000L)
      if (((units << (64 - 6)) >> (64 - 6)) == units) {
        out.writeByte((units & 0x3f).toInt)
        return
      }
    }
    if (millis % 60000L == 0) {
      val minutes: Long = millis / 60000L
      if (((minutes << (64 - 30)) >> (64 - 30)) == minutes) {
        out.writeInt(0x40000000 | (minutes & 0x3fffffff).toInt)
        return
      }
    }
    if (millis % 1000L == 0) {
      val seconds: Long = millis / 1000L
      if (((seconds << (64 - 38)) >> (64 - 38)) == seconds) {
        out.writeByte(0x80 | ((seconds >> 32) & 0x3f).toInt)
        out.writeInt((seconds & 0xffffffff).toInt)
        return
      }
    }
    out.writeByte(if (millis < 0) 0xff else 0xc0)
    out.writeLong(millis)
  }

  /**
   * Reads encoding generated by writeMillis.
   */
  @throws(classOf[IOException])
  private[tz] def readMillis(in: DataInput): Long = {
    var v: Int = in.readUnsignedByte
    v >> 6 match {
      case 0 =>
      case _ =>
        v = (v << (32 - 6)) >> (32 - 6)
        return v * (30 * 60000L)
      case 1 =>
        v = (v << (32 - 6)) >> (32 - 30)
        v |= (in.readUnsignedByte) << 16
        v |= (in.readUnsignedByte) << 8
        v |= (in.readUnsignedByte)
        return v * 60000L
      case 2 =>
        var w: Long = ((v.toLong) << (64 - 6)) >> (64 - 38)
        w |= (in.readUnsignedByte) << 24
        w |= (in.readUnsignedByte) << 16
        w |= (in.readUnsignedByte) << 8
        w |= (in.readUnsignedByte)
        return w * 1000L
      case 3 =>
        return in.readLong
    }
  }

  private def buildFixedZone(id: String, nameKey: String, wallOffset: Int, standardOffset: Int): DateTimeZone = {
    if (("UTC" == id) && (id == nameKey) && wallOffset == 0 && standardOffset == 0) {
      return DateTimeZone.UTC
    }
    return new FixedDateTimeZone(id, nameKey, wallOffset, standardOffset)
  }

  /**
   * Supports setting fields of year and moving between transitions.
   */
  private object OfYear {
    @throws(classOf[IOException])
    private[tz] def readFrom(in: DataInput): DateTimeZoneBuilder.OfYear = {
      return new DateTimeZoneBuilder.OfYear(in.readUnsignedByte.toChar, in.readUnsignedByte.toInt, in.readByte.toInt, in.readUnsignedByte.toInt, in.readBoolean, readMillis(in).toInt)
    }
  }

  private final class OfYear {
    private[tz] final val iMode: Char = 0
    private[tz] final val iMonthOfYear: Int = 0
    private[tz] final val iDayOfMonth: Int = 0
    private[tz] final val iDayOfWeek: Int = 0
    private[tz] final val iAdvance: Boolean = false
    private[tz] final val iMillisOfDay: Int = 0

    private[tz] def this(mode: Char, monthOfYear: Int, dayOfMonth: Int, dayOfWeek: Int, advanceDayOfWeek: Boolean, millisOfDay: Int) {
      this()
      if (mode != 'u' && mode != 'w' && mode != 's') {
        throw new IllegalArgumentException("Unknown mode: " + mode)
      }
      iMode = mode
      iMonthOfYear = monthOfYear
      iDayOfMonth = dayOfMonth
      iDayOfWeek = dayOfWeek
      iAdvance = advanceDayOfWeek
      iMillisOfDay = millisOfDay
    }

    /**
     * @param standardOffset standard offset just before instant
     */
    def setInstant(year: Int, standardOffset: Int, saveMillis: Int): Long = {
      var offset: Int = 0
      if (iMode == 'w') {
        offset = standardOffset + saveMillis
      }
      else if (iMode == 's') {
        offset = standardOffset
      }
      else {
        offset = 0
      }
      val chrono: Chronology = ISOChronology.getInstanceUTC
      var millis: Long = chrono.year.set(0, year)
      millis = chrono.monthOfYear.set(millis, iMonthOfYear)
      millis = chrono.millisOfDay.set(millis, iMillisOfDay)
      millis = setDayOfMonth(chrono, millis)
      if (iDayOfWeek != 0) {
        millis = setDayOfWeek(chrono, millis)
      }
      return millis - offset
    }

    /**
     * @param standardOffset standard offset just before next recurrence
     */
    def next(instant: Long, standardOffset: Int, saveMillis: Int): Long = {
      var offset: Int = 0
      if (iMode == 'w') {
        offset = standardOffset + saveMillis
      }
      else if (iMode == 's') {
        offset = standardOffset
      }
      else {
        offset = 0
      }
      instant += offset
      val chrono: Chronology = ISOChronology.getInstanceUTC
      var next: Long = chrono.monthOfYear.set(instant, iMonthOfYear)
      next = chrono.millisOfDay.set(next, 0)
      next = chrono.millisOfDay.add(next, iMillisOfDay)
      next = setDayOfMonthNext(chrono, next)
      if (iDayOfWeek == 0) {
        if (next <= instant) {
          next = chrono.year.add(next, 1)
          next = setDayOfMonthNext(chrono, next)
        }
      }
      else {
        next = setDayOfWeek(chrono, next)
        if (next <= instant) {
          next = chrono.year.add(next, 1)
          next = chrono.monthOfYear.set(next, iMonthOfYear)
          next = setDayOfMonthNext(chrono, next)
          next = setDayOfWeek(chrono, next)
        }
      }
      return next - offset
    }

    /**
     * @param standardOffset standard offset just before previous recurrence
     */
    def previous(instant: Long, standardOffset: Int, saveMillis: Int): Long = {
      var offset: Int = 0
      if (iMode == 'w') {
        offset = standardOffset + saveMillis
      }
      else if (iMode == 's') {
        offset = standardOffset
      }
      else {
        offset = 0
      }
      instant += offset
      val chrono: Chronology = ISOChronology.getInstanceUTC
      var prev: Long = chrono.monthOfYear.set(instant, iMonthOfYear)
      prev = chrono.millisOfDay.set(prev, 0)
      prev = chrono.millisOfDay.add(prev, iMillisOfDay)
      prev = setDayOfMonthPrevious(chrono, prev)
      if (iDayOfWeek == 0) {
        if (prev >= instant) {
          prev = chrono.year.add(prev, -1)
          prev = setDayOfMonthPrevious(chrono, prev)
        }
      }
      else {
        prev = setDayOfWeek(chrono, prev)
        if (prev >= instant) {
          prev = chrono.year.add(prev, -1)
          prev = chrono.monthOfYear.set(prev, iMonthOfYear)
          prev = setDayOfMonthPrevious(chrono, prev)
          prev = setDayOfWeek(chrono, prev)
        }
      }
      return prev - offset
    }

    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DateTimeZoneBuilder.OfYear]) {
        val other: DateTimeZoneBuilder.OfYear = obj.asInstanceOf[DateTimeZoneBuilder.OfYear]
        return iMode == other.iMode && iMonthOfYear == other.iMonthOfYear && iDayOfMonth == other.iDayOfMonth && iDayOfWeek == other.iDayOfWeek && iAdvance == other.iAdvance && iMillisOfDay == other.iMillisOfDay
      }
      return false
    }

    @throws(classOf[IOException])
    def writeTo(out: DataOutput) {
      out.writeByte(iMode)
      out.writeByte(iMonthOfYear)
      out.writeByte(iDayOfMonth)
      out.writeByte(iDayOfWeek)
      out.writeBoolean(iAdvance)
      writeMillis(out, iMillisOfDay)
    }

    /**
     * If month-day is 02-29 and year isn't leap, advances to next leap year.
     */
    private def setDayOfMonthNext(chrono: Chronology, next: Long): Long = {
      try {
        next = setDayOfMonth(chrono, next)
      }
      catch {
        case e: IllegalArgumentException => {
          if (iMonthOfYear == 2 && iDayOfMonth == 29) {
            while (chrono.year.isLeap(next) == false) {
              next = chrono.year.add(next, 1)
            }
            next = setDayOfMonth(chrono, next)
          }
          else {
            throw e
          }
        }
      }
      return next
    }

    /**
     * If month-day is 02-29 and year isn't leap, retreats to previous leap year.
     */
    private def setDayOfMonthPrevious(chrono: Chronology, prev: Long): Long = {
      try {
        prev = setDayOfMonth(chrono, prev)
      }
      catch {
        case e: IllegalArgumentException => {
          if (iMonthOfYear == 2 && iDayOfMonth == 29) {
            while (chrono.year.isLeap(prev) == false) {
              prev = chrono.year.add(prev, -1)
            }
            prev = setDayOfMonth(chrono, prev)
          }
          else {
            throw e
          }
        }
      }
      return prev
    }

    private def setDayOfMonth(chrono: Chronology, instant: Long): Long = {
      if (iDayOfMonth >= 0) {
        instant = chrono.dayOfMonth.set(instant, iDayOfMonth)
      }
      else {
        instant = chrono.dayOfMonth.set(instant, 1)
        instant = chrono.monthOfYear.add(instant, 1)
        instant = chrono.dayOfMonth.add(instant, iDayOfMonth)
      }
      return instant
    }

    private def setDayOfWeek(chrono: Chronology, instant: Long): Long = {
      val dayOfWeek: Int = chrono.dayOfWeek.get(instant)
      var daysToAdd: Int = iDayOfWeek - dayOfWeek
      if (daysToAdd != 0) {
        if (iAdvance) {
          if (daysToAdd < 0) {
            daysToAdd += 7
          }
        }
        else {
          if (daysToAdd > 0) {
            daysToAdd -= 7
          }
        }
        instant = chrono.dayOfWeek.add(instant, daysToAdd)
      }
      return instant
    }
  }

  /**
   * Extends OfYear with a nameKey and savings.
   */
  private object Recurrence {
    @throws(classOf[IOException])
    private[tz] def readFrom(in: DataInput): DateTimeZoneBuilder.Recurrence = {
      return new DateTimeZoneBuilder.Recurrence(OfYear.readFrom(in), in.readUTF, readMillis(in).toInt)
    }
  }

  private final class Recurrence {
    private[tz] final val iOfYear: DateTimeZoneBuilder.OfYear = null
    private[tz] final val iNameKey: String = null
    private[tz] final val iSaveMillis: Int = 0

    private[tz] def this(ofYear: DateTimeZoneBuilder.OfYear, nameKey: String, saveMillis: Int) {
      this()
      iOfYear = ofYear
      iNameKey = nameKey
      iSaveMillis = saveMillis
    }

    def getOfYear: DateTimeZoneBuilder.OfYear = {
      return iOfYear
    }

    /**
     * @param standardOffset standard offset just before next recurrence
     */
    def next(instant: Long, standardOffset: Int, saveMillis: Int): Long = {
      return iOfYear.next(instant, standardOffset, saveMillis)
    }

    /**
     * @param standardOffset standard offset just before previous recurrence
     */
    def previous(instant: Long, standardOffset: Int, saveMillis: Int): Long = {
      return iOfYear.previous(instant, standardOffset, saveMillis)
    }

    def getNameKey: String = {
      return iNameKey
    }

    def getSaveMillis: Int = {
      return iSaveMillis
    }

    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DateTimeZoneBuilder.Recurrence]) {
        val other: DateTimeZoneBuilder.Recurrence = obj.asInstanceOf[DateTimeZoneBuilder.Recurrence]
        return iSaveMillis == other.iSaveMillis && (iNameKey == other.iNameKey) && (iOfYear == other.iOfYear)
      }
      return false
    }

    @throws(classOf[IOException])
    def writeTo(out: DataOutput) {
      iOfYear.writeTo(out)
      out.writeUTF(iNameKey)
      writeMillis(out, iSaveMillis)
    }

    private[tz] def rename(nameKey: String): DateTimeZoneBuilder.Recurrence = {
      return new DateTimeZoneBuilder.Recurrence(iOfYear, nameKey, iSaveMillis)
    }

    private[tz] def renameAppend(appendNameKey: String): DateTimeZoneBuilder.Recurrence = {
      return rename((iNameKey + appendNameKey).intern)
    }
  }

  /**
   * Extends Recurrence with inclusive year limits.
   */
  private final class Rule {
    private[tz] final val iRecurrence: DateTimeZoneBuilder.Recurrence = null
    private[tz] final val iFromYear: Int = 0
    private[tz] final val iToYear: Int = 0

    private[tz] def this(recurrence: DateTimeZoneBuilder.Recurrence, fromYear: Int, toYear: Int) {
      this()
      iRecurrence = recurrence
      iFromYear = fromYear
      iToYear = toYear
    }

    @SuppressWarnings(Array("unused")) def getFromYear: Int = {
      return iFromYear
    }

    def getToYear: Int = {
      return iToYear
    }

    @SuppressWarnings(Array("unused")) def getOfYear: DateTimeZoneBuilder.OfYear = {
      return iRecurrence.getOfYear
    }

    def getNameKey: String = {
      return iRecurrence.getNameKey
    }

    def getSaveMillis: Int = {
      return iRecurrence.getSaveMillis
    }

    def next(instant: Long, standardOffset: Int, saveMillis: Int): Long = {
      val chrono: Chronology = ISOChronology.getInstanceUTC
      val wallOffset: Int = standardOffset + saveMillis
      var testInstant: Long = instant
      var year: Int = 0
      if (instant == Long.MIN_VALUE) {
        year = Integer.MIN_VALUE
      }
      else {
        year = chrono.year.get(instant + wallOffset)
      }
      if (year < iFromYear) {
        testInstant = chrono.year.set(0, iFromYear) - wallOffset
        testInstant -= 1
      }
      var next: Long = iRecurrence.next(testInstant, standardOffset, saveMillis)
      if (next > instant) {
        year = chrono.year.get(next + wallOffset)
        if (year > iToYear) {
          next = instant
        }
      }
      return next
    }
  }

  private final class Transition {
    private final val iMillis: Long = 0L
    private final val iNameKey: String = null
    private final val iWallOffset: Int = 0
    private final val iStandardOffset: Int = 0

    private[tz] def this(millis: Long, tr: DateTimeZoneBuilder.Transition) {
      this()
      iMillis = millis
      iNameKey = tr.iNameKey
      iWallOffset = tr.iWallOffset
      iStandardOffset = tr.iStandardOffset
    }

    private[tz] def this(millis: Long, rule: DateTimeZoneBuilder.Rule, standardOffset: Int) {
      this()
      iMillis = millis
      iNameKey = rule.getNameKey
      iWallOffset = standardOffset + rule.getSaveMillis
      iStandardOffset = standardOffset
    }

    private[tz] def this(millis: Long, nameKey: String, wallOffset: Int, standardOffset: Int) {
      this()
      iMillis = millis
      iNameKey = nameKey
      iWallOffset = wallOffset
      iStandardOffset = standardOffset
    }

    def getMillis: Long = {
      return iMillis
    }

    def getNameKey: String = {
      return iNameKey
    }

    def getWallOffset: Int = {
      return iWallOffset
    }

    def getStandardOffset: Int = {
      return iStandardOffset
    }

    def getSaveMillis: Int = {
      return iWallOffset - iStandardOffset
    }

    /**
     * There must be a change in the millis, wall offsets or name keys.
     */
    def isTransitionFrom(other: DateTimeZoneBuilder.Transition): Boolean = {
      if (other == null) {
        return true
      }
      return iMillis > other.iMillis && (iWallOffset != other.iWallOffset || !((iNameKey == other.iNameKey)))
    }
  }

  private object RuleSet {
    private val YEAR_LIMIT: Int = 0
    try {
      val now: Long = DateTimeUtils.currentTimeMillis
      YEAR_LIMIT = ISOChronology.getInstanceUTC.year.get(now) + 100
    }
  }

  private final class RuleSet {
    private var iStandardOffset: Int = 0
    private var iRules: ArrayList[DateTimeZoneBuilder.Rule] = null
    private var iInitialNameKey: String = null
    private var iInitialSaveMillis: Int = 0
    private var iUpperYear: Int = 0
    private var iUpperOfYear: DateTimeZoneBuilder.OfYear = null

    private[tz] def this() {
      this()
      iRules = new ArrayList[DateTimeZoneBuilder.Rule](10)
      iUpperYear = Integer.MAX_VALUE
    }

    /**
     * Copy constructor.
     */
    private[tz] def this(rs: DateTimeZoneBuilder.RuleSet) {
      this()
      iStandardOffset = rs.iStandardOffset
      iRules = new ArrayList[DateTimeZoneBuilder.Rule](rs.iRules)
      iInitialNameKey = rs.iInitialNameKey
      iInitialSaveMillis = rs.iInitialSaveMillis
      iUpperYear = rs.iUpperYear
      iUpperOfYear = rs.iUpperOfYear
    }

    @SuppressWarnings(Array("unused")) def getStandardOffset: Int = {
      return iStandardOffset
    }

    def setStandardOffset(standardOffset: Int) {
      iStandardOffset = standardOffset
    }

    def setFixedSavings(nameKey: String, saveMillis: Int) {
      iInitialNameKey = nameKey
      iInitialSaveMillis = saveMillis
    }

    def addRule(rule: DateTimeZoneBuilder.Rule) {
      if (!iRules.contains(rule)) {
        iRules.add(rule)
      }
    }

    def setUpperLimit(year: Int, ofYear: DateTimeZoneBuilder.OfYear) {
      iUpperYear = year
      iUpperOfYear = ofYear
    }

    /**
     * Returns a transition at firstMillis with the first name key and
     * offsets for this rule set. This method may return null.
     *
     * @param firstMillis millis of first transition
     */
    def firstTransition(firstMillis: Long): DateTimeZoneBuilder.Transition = {
      if (iInitialNameKey != null) {
        return new DateTimeZoneBuilder.Transition(firstMillis, iInitialNameKey, iStandardOffset + iInitialSaveMillis, iStandardOffset)
      }
      val copy: ArrayList[DateTimeZoneBuilder.Rule] = new ArrayList[DateTimeZoneBuilder.Rule](iRules)
      var millis: Long = Long.MIN_VALUE
      var saveMillis: Int = 0
      var first: DateTimeZoneBuilder.Transition = null
      var next: DateTimeZoneBuilder.Transition = null
      while ((({
        next = nextTransition(millis, saveMillis); next
      })) != null) {
        millis = next.getMillis
        if (millis == firstMillis) {
          first = new DateTimeZoneBuilder.Transition(firstMillis, next)
          break //todo: break is not supported
        }
        if (millis > firstMillis) {
          if (first == null) {
            import scala.collection.JavaConversions._
            for (rule <- copy) {
              if (rule.getSaveMillis == 0) {
                first = new DateTimeZoneBuilder.Transition(firstMillis, rule, iStandardOffset)
                break //todo: break is not supported
              }
            }
          }
          if (first == null) {
            first = new DateTimeZoneBuilder.Transition(firstMillis, next.getNameKey, iStandardOffset, iStandardOffset)
          }
          break //todo: break is not supported
        }
        first = new DateTimeZoneBuilder.Transition(firstMillis, next)
        saveMillis = next.getSaveMillis
      }
      iRules = copy
      return first
    }

    /**
     * Returns null if RuleSet is exhausted or upper limit reached. Calling
     * this method will throw away rules as they each become
     * exhausted. Copy the RuleSet before using it to compute transitions.
     *
     * Returned transition may be a duplicate from previous
     * transition. Caller must call isTransitionFrom to filter out
     * duplicates.
     *
     * @param saveMillis savings before next transition
     */
    def nextTransition(instant: Long, saveMillis: Int): DateTimeZoneBuilder.Transition = {
      val chrono: Chronology = ISOChronology.getInstanceUTC
      var nextRule: DateTimeZoneBuilder.Rule = null
      var nextMillis: Long = Long.MaxValue
      val it: Iterator[DateTimeZoneBuilder.Rule] = iRules.iterator
      while (it.hasNext) {
        val rule: DateTimeZoneBuilder.Rule = it.next
        val next: Long = rule.next(instant, iStandardOffset, saveMillis)
        if (next <= instant) {
          it.remove
          continue //todo: continue is not supported
        }
        if (next <= nextMillis) {
          nextRule = rule
          nextMillis = next
        }
      }
      if (nextRule == null) {
        return null
      }
      if (chrono.year.get(nextMillis) >= RuleSet.YEAR_LIMIT) {
        return null
      }
      if (iUpperYear < Integer.MAX_VALUE) {
        val upperMillis: Long = iUpperOfYear.setInstant(iUpperYear, iStandardOffset, saveMillis)
        if (nextMillis >= upperMillis) {
          return null
        }
      }
      return new DateTimeZoneBuilder.Transition(nextMillis, nextRule, iStandardOffset)
    }

    /**
     * @param saveMillis savings before upper limit
     */
    def getUpperLimit(saveMillis: Int): Long = {
      if (iUpperYear == Integer.MAX_VALUE) {
        return Long.MaxValue
      }
      return iUpperOfYear.setInstant(iUpperYear, iStandardOffset, saveMillis)
    }

    /**
     * Returns null if none can be built.
     */
    def buildTailZone(id: String): DateTimeZoneBuilder.DSTZone = {
      if (iRules.size == 2) {
        val startRule: DateTimeZoneBuilder.Rule = iRules.get(0)
        val endRule: DateTimeZoneBuilder.Rule = iRules.get(1)
        if (startRule.getToYear == Integer.MAX_VALUE && endRule.getToYear == Integer.MAX_VALUE) {
          return new DateTimeZoneBuilder.DSTZone(id, iStandardOffset, startRule.iRecurrence, endRule.iRecurrence)
        }
      }
      return null
    }
  }

  @SerialVersionUID(6941492635554961361L)
  private object DSTZone {
    @throws(classOf[IOException])
    private[tz] def readFrom(in: DataInput, id: String): DateTimeZoneBuilder.DSTZone = {
      return new DateTimeZoneBuilder.DSTZone(id, readMillis(in).toInt, Recurrence.readFrom(in), Recurrence.readFrom(in))
    }
  }

  @SerialVersionUID(6941492635554961361L)
  private final class DSTZone extends DateTimeZone {
    private[tz] final val iStandardOffset: Int = 0
    private[tz] final val iStartRecurrence: DateTimeZoneBuilder.Recurrence = null
    private[tz] final val iEndRecurrence: DateTimeZoneBuilder.Recurrence = null

    private[tz] def this(id: String, standardOffset: Int, startRecurrence: DateTimeZoneBuilder.Recurrence, endRecurrence: DateTimeZoneBuilder.Recurrence) {
      this()
      `super`(id)
      iStandardOffset = standardOffset
      iStartRecurrence = startRecurrence
      iEndRecurrence = endRecurrence
    }

    def getNameKey(instant: Long): String = {
      return findMatchingRecurrence(instant).getNameKey
    }

    def getOffset(instant: Long): Int = {
      return iStandardOffset + findMatchingRecurrence(instant).getSaveMillis
    }

    def getStandardOffset(instant: Long): Int = {
      return iStandardOffset
    }

    def isFixed: Boolean = {
      return false
    }

    def nextTransition(instant: Long): Long = {
      val standardOffset: Int = iStandardOffset
      val startRecurrence: DateTimeZoneBuilder.Recurrence = iStartRecurrence
      val endRecurrence: DateTimeZoneBuilder.Recurrence = iEndRecurrence
      var start: Long = 0L
      var end: Long = 0L
      try {
        start = startRecurrence.next(instant, standardOffset, endRecurrence.getSaveMillis)
        if (instant > 0 && start < 0) {
          start = instant
        }
      }
      catch {
        case e: IllegalArgumentException => {
          start = instant
        }
        case e: ArithmeticException => {
          start = instant
        }
      }
      try {
        end = endRecurrence.next(instant, standardOffset, startRecurrence.getSaveMillis)
        if (instant > 0 && end < 0) {
          end = instant
        }
      }
      catch {
        case e: IllegalArgumentException => {
          end = instant
        }
        case e: ArithmeticException => {
          end = instant
        }
      }
      return if ((start > end)) end else start
    }

    def previousTransition(instant: Long): Long = {
      instant += 1
      val standardOffset: Int = iStandardOffset
      val startRecurrence: DateTimeZoneBuilder.Recurrence = iStartRecurrence
      val endRecurrence: DateTimeZoneBuilder.Recurrence = iEndRecurrence
      var start: Long = 0L
      var end: Long = 0L
      try {
        start = startRecurrence.previous(instant, standardOffset, endRecurrence.getSaveMillis)
        if (instant < 0 && start > 0) {
          start = instant
        }
      }
      catch {
        case e: IllegalArgumentException => {
          start = instant
        }
        case e: ArithmeticException => {
          start = instant
        }
      }
      try {
        end = endRecurrence.previous(instant, standardOffset, startRecurrence.getSaveMillis)
        if (instant < 0 && end > 0) {
          end = instant
        }
      }
      catch {
        case e: IllegalArgumentException => {
          end = instant
        }
        case e: ArithmeticException => {
          end = instant
        }
      }
      return (if ((start > end)) start else end) - 1
    }

    def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DateTimeZoneBuilder.DSTZone]) {
        val other: DateTimeZoneBuilder.DSTZone = obj.asInstanceOf[DateTimeZoneBuilder.DSTZone]
        return (getID == other.getID) && iStandardOffset == other.iStandardOffset && (iStartRecurrence == other.iStartRecurrence) && (iEndRecurrence == other.iEndRecurrence)
      }
      return false
    }

    @throws(classOf[IOException])
    def writeTo(out: DataOutput) {
      writeMillis(out, iStandardOffset)
      iStartRecurrence.writeTo(out)
      iEndRecurrence.writeTo(out)
    }

    private def findMatchingRecurrence(instant: Long): DateTimeZoneBuilder.Recurrence = {
      val standardOffset: Int = iStandardOffset
      val startRecurrence: DateTimeZoneBuilder.Recurrence = iStartRecurrence
      val endRecurrence: DateTimeZoneBuilder.Recurrence = iEndRecurrence
      var start: Long = 0L
      var end: Long = 0L
      try {
        start = startRecurrence.next(instant, standardOffset, endRecurrence.getSaveMillis)
      }
      catch {
        case e: IllegalArgumentException => {
          start = instant
        }
        case e: ArithmeticException => {
          start = instant
        }
      }
      try {
        end = endRecurrence.next(instant, standardOffset, startRecurrence.getSaveMillis)
      }
      catch {
        case e: IllegalArgumentException => {
          end = instant
        }
        case e: ArithmeticException => {
          end = instant
        }
      }
      return if ((start > end)) startRecurrence else endRecurrence
    }
  }

  @SerialVersionUID(7811976468055766265L)
  private object PrecalculatedZone {
    @throws(classOf[IOException])
    private[tz] def readFrom(in: DataInput, id: String): DateTimeZoneBuilder.PrecalculatedZone = {
      val poolSize: Int = in.readUnsignedShort
      val pool: Array[String] = new Array[String](poolSize)
      {
        var i: Int = 0
        while (i < poolSize) {
          {
            pool(i) = in.readUTF
          }
          ({
            i += 1; i - 1
          })
        }
      }
      val size: Int = in.readInt
      val transitions: Array[Long] = new Array[Long](size)
      val wallOffsets: Array[Int] = new Array[Int](size)
      val standardOffsets: Array[Int] = new Array[Int](size)
      val nameKeys: Array[String] = new Array[String](size)
      {
        var i: Int = 0
        while (i < size) {
          {
            transitions(i) = readMillis(in)
            wallOffsets(i) = readMillis(in).toInt
            standardOffsets(i) = readMillis(in).toInt
            try {
              var index: Int = 0
              if (poolSize < 256) {
                index = in.readUnsignedByte
              }
              else {
                index = in.readUnsignedShort
              }
              nameKeys(i) = pool(index)
            }
            catch {
              case e: ArrayIndexOutOfBoundsException => {
                throw new IOException("Invalid encoding")
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      var tailZone: DateTimeZoneBuilder.DSTZone = null
      if (in.readBoolean) {
        tailZone = DSTZone.readFrom(in, id)
      }
      return new DateTimeZoneBuilder.PrecalculatedZone(id, transitions, wallOffsets, standardOffsets, nameKeys, tailZone)
    }

    /**
     * Factory to create instance from builder.
     *
     * @param id  the zone id
     * @param outputID  true if the zone id should be output
     * @param transitions  the list of Transition objects
     * @param tailZone  optional zone for getting info beyond precalculated tables
     */
    private[tz] def create(id: String, outputID: Boolean, transitions: ArrayList[DateTimeZoneBuilder.Transition], tailZone: DateTimeZoneBuilder.DSTZone): DateTimeZoneBuilder.PrecalculatedZone = {
      val size: Int = transitions.size
      if (size == 0) {
        throw new IllegalArgumentException
      }
      val trans: Array[Long] = new Array[Long](size)
      val wallOffsets: Array[Int] = new Array[Int](size)
      val standardOffsets: Array[Int] = new Array[Int](size)
      val nameKeys: Array[String] = new Array[String](size)
      var last: DateTimeZoneBuilder.Transition = null
      {
        var i: Int = 0
        while (i < size) {
          {
            val tr: DateTimeZoneBuilder.Transition = transitions.get(i)
            if (!tr.isTransitionFrom(last)) {
              throw new IllegalArgumentException(id)
            }
            trans(i) = tr.getMillis
            wallOffsets(i) = tr.getWallOffset
            standardOffsets(i) = tr.getStandardOffset
            nameKeys(i) = tr.getNameKey
            last = tr
          }
          ({
            i += 1; i - 1
          })
        }
      }
      var zoneNameData: Array[String] = new Array[String](5)
      val zoneStrings: Array[Array[String]] = new DateFormatSymbols(Locale.ENGLISH).getZoneStrings
      {
        var j: Int = 0
        while (j < zoneStrings.length) {
          {
            val set: Array[String] = zoneStrings(j)
            if (set != null && set.length == 5 && (id == set(0))) {
              zoneNameData = set
            }
          }
          ({
            j += 1; j - 1
          })
        }
      }
      val chrono: Chronology = ISOChronology.getInstanceUTC
      {
        var i: Int = 0
        while (i < nameKeys.length - 1) {
          {
            val curNameKey: String = nameKeys(i)
            val nextNameKey: String = nameKeys(i + 1)
            val curOffset: Long = wallOffsets(i)
            val nextOffset: Long = wallOffsets(i + 1)
            val curStdOffset: Long = standardOffsets(i)
            val nextStdOffset: Long = standardOffsets(i + 1)
            val p: Period = new Period(trans(i), trans(i + 1), PeriodType.yearMonthDay, chrono)
            if (curOffset != nextOffset && curStdOffset == nextStdOffset && (curNameKey == nextNameKey) && p.getYears == 0 && p.getMonths > 4 && p.getMonths < 8 && (curNameKey == zoneNameData(2)) && (curNameKey == zoneNameData(4))) {
              if (ZoneInfoLogger.verbose) {
                System.out.println("Fixing duplicate name key - " + nextNameKey)
                System.out.println("     - " + new DateTime(trans(i), chrono) + " - " + new DateTime(trans(i + 1), chrono))
              }
              if (curOffset > nextOffset) {
                nameKeys(i) = (curNameKey + "-Summer").intern
              }
              else if (curOffset < nextOffset) {
                nameKeys(i + 1) = (nextNameKey + "-Summer").intern
                i += 1
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      if (tailZone != null) {
        if (tailZone.iStartRecurrence.getNameKey == tailZone.iEndRecurrence.getNameKey) {
          if (ZoneInfoLogger.verbose) {
            System.out.println("Fixing duplicate recurrent name key - " + tailZone.iStartRecurrence.getNameKey)
          }
          if (tailZone.iStartRecurrence.getSaveMillis > 0) {
            tailZone = new DateTimeZoneBuilder.DSTZone(tailZone.getID, tailZone.iStandardOffset, tailZone.iStartRecurrence.renameAppend("-Summer"), tailZone.iEndRecurrence)
          }
          else {
            tailZone = new DateTimeZoneBuilder.DSTZone(tailZone.getID, tailZone.iStandardOffset, tailZone.iStartRecurrence, tailZone.iEndRecurrence.renameAppend("-Summer"))
          }
        }
      }
      return new DateTimeZoneBuilder.PrecalculatedZone((if (outputID) id else ""), trans, wallOffsets, standardOffsets, nameKeys, tailZone)
    }
  }

  @SerialVersionUID(7811976468055766265L)
  private final class PrecalculatedZone extends DateTimeZone {
    private final val iTransitions: Array[Long] = null
    private final val iWallOffsets: Array[Int] = null
    private final val iStandardOffsets: Array[Int] = null
    private final val iNameKeys: Array[String] = null
    private final val iTailZone: DateTimeZoneBuilder.DSTZone = null

    /**
     * Constructor used ONLY for valid input, loaded via static methods.
     */
    private def this(id: String, transitions: Array[Long], wallOffsets: Array[Int], standardOffsets: Array[Int], nameKeys: Array[String], tailZone: DateTimeZoneBuilder.DSTZone) {
      this()
      `super`(id)
      iTransitions = transitions
      iWallOffsets = wallOffsets
      iStandardOffsets = standardOffsets
      iNameKeys = nameKeys
      iTailZone = tailZone
    }

    def getNameKey(instant: Long): String = {
      val transitions: Array[Long] = iTransitions
      var i: Int = Arrays.binarySearch(transitions, instant)
      if (i >= 0) {
        return iNameKeys(i)
      }
      i = ~i
      if (i < transitions.length) {
        if (i > 0) {
          return iNameKeys(i - 1)
        }
        return "UTC"
      }
      if (iTailZone == null) {
        return iNameKeys(i - 1)
      }
      return iTailZone.getNameKey(instant)
    }

    def getOffset(instant: Long): Int = {
      val transitions: Array[Long] = iTransitions
      var i: Int = Arrays.binarySearch(transitions, instant)
      if (i >= 0) {
        return iWallOffsets(i)
      }
      i = ~i
      if (i < transitions.length) {
        if (i > 0) {
          return iWallOffsets(i - 1)
        }
        return 0
      }
      if (iTailZone == null) {
        return iWallOffsets(i - 1)
      }
      return iTailZone.getOffset(instant)
    }

    def getStandardOffset(instant: Long): Int = {
      val transitions: Array[Long] = iTransitions
      var i: Int = Arrays.binarySearch(transitions, instant)
      if (i >= 0) {
        return iStandardOffsets(i)
      }
      i = ~i
      if (i < transitions.length) {
        if (i > 0) {
          return iStandardOffsets(i - 1)
        }
        return 0
      }
      if (iTailZone == null) {
        return iStandardOffsets(i - 1)
      }
      return iTailZone.getStandardOffset(instant)
    }

    def isFixed: Boolean = {
      return false
    }

    def nextTransition(instant: Long): Long = {
      val transitions: Array[Long] = iTransitions
      var i: Int = Arrays.binarySearch(transitions, instant)
      i = if ((i >= 0)) (i + 1) else ~i
      if (i < transitions.length) {
        return transitions(i)
      }
      if (iTailZone == null) {
        return instant
      }
      val end: Long = transitions(transitions.length - 1)
      if (instant < end) {
        instant = end
      }
      return iTailZone.nextTransition(instant)
    }

    def previousTransition(instant: Long): Long = {
      val transitions: Array[Long] = iTransitions
      var i: Int = Arrays.binarySearch(transitions, instant)
      if (i >= 0) {
        if (instant > Long.MIN_VALUE) {
          return instant - 1
        }
        return instant
      }
      i = ~i
      if (i < transitions.length) {
        if (i > 0) {
          val prev: Long = transitions(i - 1)
          if (prev > Long.MIN_VALUE) {
            return prev - 1
          }
        }
        return instant
      }
      if (iTailZone != null) {
        val prev: Long = iTailZone.previousTransition(instant)
        if (prev < instant) {
          return prev
        }
      }
      val prev: Long = transitions(i - 1)
      if (prev > Long.MIN_VALUE) {
        return prev - 1
      }
      return instant
    }

    def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DateTimeZoneBuilder.PrecalculatedZone]) {
        val other: DateTimeZoneBuilder.PrecalculatedZone = obj.asInstanceOf[DateTimeZoneBuilder.PrecalculatedZone]
        return (getID == other.getID) && Arrays.equals(iTransitions, other.iTransitions) && Arrays.equals(iNameKeys, other.iNameKeys) && Arrays.equals(iWallOffsets, other.iWallOffsets) && Arrays.equals(iStandardOffsets, other.iStandardOffsets) && (if ((iTailZone == null)) (null == other.iTailZone) else ((iTailZone == other.iTailZone)))
      }
      return false
    }

    @throws(classOf[IOException])
    def writeTo(out: DataOutput) {
      val size: Int = iTransitions.length
      val poolSet: Set[String] = new HashSet[String]
      {
        var i: Int = 0
        while (i < size) {
          {
            poolSet.add(iNameKeys(i))
          }
          ({
            i += 1; i - 1
          })
        }
      }
      val poolSize: Int = poolSet.size
      if (poolSize > 65535) {
        throw new UnsupportedOperationException("String pool is too large")
      }
      val pool: Array[String] = new Array[String](poolSize)
      val it: Iterator[String] = poolSet.iterator
      {
        var i: Int = 0
        while (it.hasNext) {
          {
            pool(i) = it.next
          }
          ({
            i += 1; i - 1
          })
        }
      }
      out.writeShort(poolSize)
      {
        var i: Int = 0
        while (i < poolSize) {
          {
            out.writeUTF(pool(i))
          }
          ({
            i += 1; i - 1
          })
        }
      }
      out.writeInt(size)
      {
        var i: Int = 0
        while (i < size) {
          {
            writeMillis(out, iTransitions(i))
            writeMillis(out, iWallOffsets(i))
            writeMillis(out, iStandardOffsets(i))
            val nameKey: String = iNameKeys(i)
            {
              var j: Int = 0
              while (j < poolSize) {
                {
                  if (pool(j) == nameKey) {
                    if (poolSize < 256) {
                      out.writeByte(j)
                    }
                    else {
                      out.writeShort(j)
                    }
                    break //todo: break is not supported
                  }
                }
                ({
                  j += 1; j - 1
                })
              }
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      out.writeBoolean(iTailZone != null)
      if (iTailZone != null) {
        iTailZone.writeTo(out)
      }
    }

    def isCachable: Boolean = {
      if (iTailZone != null) {
        return true
      }
      val transitions: Array[Long] = iTransitions
      if (transitions.length <= 1) {
        return false
      }
      var distances: Double = 0
      var count: Int = 0
      {
        var i: Int = 1
        while (i < transitions.length) {
          {
            val diff: Long = transitions(i) - transitions(i - 1)
            if (diff < ((366L + 365) * 24 * 60 * 60 * 1000)) {
              distances += diff.toDouble
              count += 1
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      if (count > 0) {
        var avg: Double = distances / count
        avg /= 24 * 60 * 60 * 1000
        if (avg >= 25) {
          return true
        }
      }
      return false
    }
  }

}

class DateTimeZoneBuilder {
  private final val iRuleSets: ArrayList[DateTimeZoneBuilder.RuleSet] = null

  def this() {
    this()
    iRuleSets = new ArrayList[DateTimeZoneBuilder.RuleSet](10)
  }

  /**
   * Adds a cutover for added rules. The standard offset at the cutover
   * defaults to 0. Call setStandardOffset afterwards to change it.
   *
   * @param year  the year of cutover
   * @param mode 'u' - cutover is measured against UTC, 'w' - against wall
   *             offset, 's' - against standard offset
   * @param monthOfYear  the month from 1 (January) to 12 (December)
   * @param dayOfMonth  if negative, set to ((last day of month) - ~dayOfMonth).
   *                    For example, if -1, set to last day of month
   * @param dayOfWeek  from 1 (Monday) to 7 (Sunday), if 0 then ignore
   * @param advanceDayOfWeek  if dayOfMonth does not fall on dayOfWeek, advance to
   *                          dayOfWeek when true, retreat when false.
   * @param millisOfDay  additional precision for specifying time of day of cutover
   */
  def addCutover(year: Int, mode: Char, monthOfYear: Int, dayOfMonth: Int, dayOfWeek: Int, advanceDayOfWeek: Boolean, millisOfDay: Int): DateTimeZoneBuilder = {
    if (iRuleSets.size > 0) {
      val ofYear: DateTimeZoneBuilder.OfYear = new DateTimeZoneBuilder.OfYear(mode, monthOfYear, dayOfMonth, dayOfWeek, advanceDayOfWeek, millisOfDay)
      val lastRuleSet: DateTimeZoneBuilder.RuleSet = iRuleSets.get(iRuleSets.size - 1)
      lastRuleSet.setUpperLimit(year, ofYear)
    }
    iRuleSets.add(new DateTimeZoneBuilder.RuleSet)
    return this
  }

  /**
   * Sets the standard offset to use for newly added rules until the next
   * cutover is added.
   * @param standardOffset  the standard offset in millis
   */
  def setStandardOffset(standardOffset: Int): DateTimeZoneBuilder = {
    getLastRuleSet.setStandardOffset(standardOffset)
    return this
  }

  /**
   * Set a fixed savings rule at the cutover.
   */
  def setFixedSavings(nameKey: String, saveMillis: Int): DateTimeZoneBuilder = {
    getLastRuleSet.setFixedSavings(nameKey, saveMillis)
    return this
  }

  /**
   * Add a recurring daylight saving time rule.
   *
   * @param nameKey  the name key of new rule
   * @param saveMillis  the milliseconds to add to standard offset
   * @param fromYear  the first year that rule is in effect, MIN_VALUE indicates
   *                  beginning of time
   * @param toYear  the last year (inclusive) that rule is in effect, MAX_VALUE
   *                indicates end of time
   * @param mode  'u' - transitions are calculated against UTC, 'w' -
   *              transitions are calculated against wall offset, 's' - transitions are
   *              calculated against standard offset
   * @param monthOfYear  the month from 1 (January) to 12 (December)
   * @param dayOfMonth  if negative, set to ((last day of month) - ~dayOfMonth).
   *                    For example, if -1, set to last day of month
   * @param dayOfWeek  from 1 (Monday) to 7 (Sunday), if 0 then ignore
   * @param advanceDayOfWeek  if dayOfMonth does not fall on dayOfWeek, advance to
   *                          dayOfWeek when true, retreat when false.
   * @param millisOfDay  additional precision for specifying time of day of transitions
   */
  def addRecurringSavings(nameKey: String, saveMillis: Int, fromYear: Int, toYear: Int, mode: Char, monthOfYear: Int, dayOfMonth: Int, dayOfWeek: Int, advanceDayOfWeek: Boolean, millisOfDay: Int): DateTimeZoneBuilder = {
    if (fromYear <= toYear) {
      val ofYear: DateTimeZoneBuilder.OfYear = new DateTimeZoneBuilder.OfYear(mode, monthOfYear, dayOfMonth, dayOfWeek, advanceDayOfWeek, millisOfDay)
      val recurrence: DateTimeZoneBuilder.Recurrence = new DateTimeZoneBuilder.Recurrence(ofYear, nameKey, saveMillis)
      val rule: DateTimeZoneBuilder.Rule = new DateTimeZoneBuilder.Rule(recurrence, fromYear, toYear)
      getLastRuleSet.addRule(rule)
    }
    return this
  }

  private def getLastRuleSet: DateTimeZoneBuilder.RuleSet = {
    if (iRuleSets.size == 0) {
      addCutover(Integer.MIN_VALUE, 'w', 1, 1, 0, false, 0)
    }
    return iRuleSets.get(iRuleSets.size - 1)
  }

  /**
   * Processes all the rules and builds a DateTimeZone.
   *
   * @param id  time zone id to assign
   * @param outputID  true if the zone id should be output
   */
  def toDateTimeZone(id: String, outputID: Boolean): DateTimeZone = {
    if (id == null) {
      throw new IllegalArgumentException
    }
    val transitions: ArrayList[DateTimeZoneBuilder.Transition] = new ArrayList[DateTimeZoneBuilder.Transition]
    var tailZone: DateTimeZoneBuilder.DSTZone = null
    var millis: Long = Long.MIN_VALUE
    var saveMillis: Int = 0
    val ruleSetCount: Int = iRuleSets.size
    {
      var i: Int = 0
      while (i < ruleSetCount) {
        {
          var rs: DateTimeZoneBuilder.RuleSet = iRuleSets.get(i)
          var next: DateTimeZoneBuilder.Transition = rs.firstTransition(millis)
          if (next == null) {
            continue //todo: continue is not supported
          }
          addTransition(transitions, next)
          millis = next.getMillis
          saveMillis = next.getSaveMillis
          rs = new DateTimeZoneBuilder.RuleSet(rs)
          while ((({
            next = rs.nextTransition(millis, saveMillis); next
          })) != null) {
            if (addTransition(transitions, next)) {
              if (tailZone != null) {
                break //todo: break is not supported
              }
            }
            millis = next.getMillis
            saveMillis = next.getSaveMillis
            if (tailZone == null && i == ruleSetCount - 1) {
              tailZone = rs.buildTailZone(id)
            }
          }
          millis = rs.getUpperLimit(saveMillis)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    if (transitions.size == 0) {
      if (tailZone != null) {
        return tailZone
      }
      return DateTimeZoneBuilder.buildFixedZone(id, "UTC", 0, 0)
    }
    if (transitions.size == 1 && tailZone == null) {
      val tr: DateTimeZoneBuilder.Transition = transitions.get(0)
      return DateTimeZoneBuilder.buildFixedZone(id, tr.getNameKey, tr.getWallOffset, tr.getStandardOffset)
    }
    val zone: DateTimeZoneBuilder.PrecalculatedZone = DateTimeZoneBuilder.PrecalculatedZone.create(id, outputID, transitions, tailZone)
    if (zone.isCachable) {
      return CachedDateTimeZone.forZone(zone)
    }
    return zone
  }

  private def addTransition(transitions: ArrayList[DateTimeZoneBuilder.Transition], tr: DateTimeZoneBuilder.Transition): Boolean = {
    val size: Int = transitions.size
    if (size == 0) {
      transitions.add(tr)
      return true
    }
    val last: DateTimeZoneBuilder.Transition = transitions.get(size - 1)
    if (!tr.isTransitionFrom(last)) {
      return false
    }
    var offsetForLast: Int = 0
    if (size >= 2) {
      offsetForLast = transitions.get(size - 2).getWallOffset
    }
    val offsetForNew: Int = last.getWallOffset
    val lastLocal: Long = last.getMillis + offsetForLast
    val newLocal: Long = tr.getMillis + offsetForNew
    if (newLocal != lastLocal) {
      transitions.add(tr)
      return true
    }
    transitions.remove(size - 1)
    return addTransition(transitions, tr)
  }

  /**
   * Encodes a built DateTimeZone to the given stream. Call readFrom to
   * decode the data into a DateTimeZone object.
   *
   * @param out  the output stream to receive the encoded DateTimeZone
   * @since 1.5 (parameter added)
   */
  @throws(classOf[IOException])
  def writeTo(zoneID: String, out: OutputStream) {
    if (out.isInstanceOf[DataOutput]) {
      writeTo(zoneID, out.asInstanceOf[DataOutput])
    }
    else {
      writeTo(zoneID, new DataOutputStream(out).asInstanceOf[DataOutput])
    }
  }

  /**
   * Encodes a built DateTimeZone to the given stream. Call readFrom to
   * decode the data into a DateTimeZone object.
   *
   * @param out  the output stream to receive the encoded DateTimeZone
   * @since 1.5 (parameter added)
   */
  @throws(classOf[IOException])
  def writeTo(zoneID: String, out: DataOutput) {
    var zone: DateTimeZone = toDateTimeZone(zoneID, false)
    if (zone.isInstanceOf[FixedDateTimeZone]) {
      out.writeByte('F')
      out.writeUTF(zone.getNameKey(0))
      DateTimeZoneBuilder.writeMillis(out, zone.getOffset(0))
      DateTimeZoneBuilder.writeMillis(out, zone.getStandardOffset(0))
    }
    else {
      if (zone.isInstanceOf[CachedDateTimeZone]) {
        out.writeByte('C')
        zone = (zone.asInstanceOf[CachedDateTimeZone]).getUncachedZone
      }
      else {
        out.writeByte('P')
      }
      (zone.asInstanceOf[DateTimeZoneBuilder.PrecalculatedZone]).writeTo(out)
    }
  }
}