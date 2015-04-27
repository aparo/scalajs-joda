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

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Locale
import java.util.Map
import java.util.Map.Entry
import java.util.StringTokenizer
import java.util.TreeMap
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.MutableDateTime
import org.joda.time.chrono.ISOChronology
import org.joda.time.chrono.LenientChronology
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * Compiles IANA ZoneInfo database files into binary files for each time zone
 * in the database. {@link DateTimeZoneBuilder} is used to construct and encode
 * compiled data files. {@link ZoneInfoProvider} loads the encoded files and
 * converts them back into {@link DateTimeZone} objects.
 * <p>
 * Although this tool is similar to zic, the binary formats are not
 * compatible. The latest IANA time zone database files may be obtained
 * <a href="http://www.iana.org/time-zones">here</a>.
 * <p>
 * ZoneInfoCompiler is mutable and not thread-safe, although the main method
 * may be safely invoked by multiple threads.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
object ZoneInfoCompiler {
  private[tz] var cStartOfYear: ZoneInfoCompiler.DateTimeOfYear = null
  private[tz] var cLenientISO: Chronology = null

  /**
   * Launches the ZoneInfoCompiler tool.
   *
   * <pre>
   * Usage: java org.joda.time.tz.ZoneInfoCompiler &lt;options&gt; &lt;source files&gt;
   * where possible options include:
   * -src &lt;directory&gt;    Specify where to read source files
   * -dst &lt;directory&gt;    Specify where to write generated files
   * -verbose            Output verbosely (default false)
   * </pre>
   */
  @throws(classOf[Exception])
  def main(args: Array[String]) {
    if (args.length == 0) {
      printUsage
      return
    }
    var inputDir: File = null
    var outputDir: File = null
    var verbose: Boolean = false
    var i: Int = 0
    {
      i = 0
      while (i < args.length) {
        {
          try {
            if ("-src" == args(i)) {
              inputDir = new File(args(({
                i += 1; i
              })))
            }
            else if ("-dst" == args(i)) {
              outputDir = new File(args(({
                i += 1; i
              })))
            }
            else if ("-verbose" == args(i)) {
              verbose = true
            }
            else if ("-?" == args(i)) {
              printUsage
              return
            }
            else {
              break //todo: break is not supported
            }
          }
          catch {
            case e: IndexOutOfBoundsException => {
              printUsage
              return
            }
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    if (i >= args.length) {
      printUsage
      return
    }
    val sources: Array[File] = new Array[File](args.length - i)
    {
      var j: Int = 0
      while (i < args.length) {
        {
          sources(j) = if (inputDir == null) new File(args(i)) else new File(inputDir, args(i))
        }
        ({
          i += 1; i - 1
        })
        ({
          j += 1; j - 1
        })
      }
    }
    ZoneInfoLogger.set(verbose)
    val zic: ZoneInfoCompiler = new ZoneInfoCompiler
    zic.compile(outputDir, sources)
  }

  private def printUsage {
    System.out.println("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>")
    System.out.println("where possible options include:")
    System.out.println("  -src <directory>    Specify where to read source files")
    System.out.println("  -dst <directory>    Specify where to write generated files")
    System.out.println("  -verbose            Output verbosely (default false)")
  }

  private[tz] def getStartOfYear: ZoneInfoCompiler.DateTimeOfYear = {
    if (cStartOfYear == null) {
      cStartOfYear = new ZoneInfoCompiler.DateTimeOfYear
    }
    return cStartOfYear
  }

  private[tz] def getLenientISOChronology: Chronology = {
    if (cLenientISO == null) {
      cLenientISO = LenientChronology.getInstance(ISOChronology.getInstanceUTC)
    }
    return cLenientISO
  }

  /**
   * @param zimap maps string ids to DateTimeZone objects.
   */
  @throws(classOf[IOException])
  private[tz] def writeZoneInfoMap(dout: DataOutputStream, zimap: Map[String, DateTimeZone]) {
    val idToIndex: Map[String, Short] = new HashMap[String, Short](zimap.size)
    val indexToId: TreeMap[Short, String] = new TreeMap[Short, String]
    var count: Short = 0
    import scala.collection.JavaConversions._
    for (entry <- zimap.entrySet) {
      var id: String = entry.getKey.asInstanceOf[String]
      if (!idToIndex.containsKey(id)) {
        val index: Short = Short.valueOf(count)
        idToIndex.put(id, index)
        indexToId.put(index, id)
        if (({
          count += 1; count
        }) == 0) {
          throw new InternalError("Too many time zone ids")
        }
      }
      id = (entry.getValue.asInstanceOf[DateTimeZone]).getID
      if (!idToIndex.containsKey(id)) {
        val index: Short = Short.valueOf(count)
        idToIndex.put(id, index)
        indexToId.put(index, id)
        if (({
          count += 1; count
        }) == 0) {
          throw new InternalError("Too many time zone ids")
        }
      }
    }
    dout.writeShort(indexToId.size)
    import scala.collection.JavaConversions._
    for (id <- indexToId.values) {
      dout.writeUTF(id)
    }
    dout.writeShort(zimap.size)
    import scala.collection.JavaConversions._
    for (entry <- zimap.entrySet) {
      var id: String = entry.getKey
      dout.writeShort(idToIndex.get(id).shortValue)
      id = entry.getValue.getID
      dout.writeShort(idToIndex.get(id).shortValue)
    }
  }

  private[tz] def parseYear(str: String, `def`: Int): Int = {
    str = str.toLowerCase
    if ((str == "minimum") || (str == "min")) {
      return Integer.MIN_VALUE
    }
    else if ((str == "maximum") || (str == "max")) {
      return Integer.MAX_VALUE
    }
    else if (str == "only") {
      return `def`
    }
    return str.toInt
  }

  private[tz] def parseMonth(str: String): Int = {
    val field: DateTimeField = ISOChronology.getInstanceUTC.monthOfYear
    return field.get(field.set(0, str, Locale.ENGLISH))
  }

  private[tz] def parseDayOfWeek(str: String): Int = {
    val field: DateTimeField = ISOChronology.getInstanceUTC.dayOfWeek
    return field.get(field.set(0, str, Locale.ENGLISH))
  }

  private[tz] def parseOptional(str: String): String = {
    return if (((str == "-"))) null else str
  }

  private[tz] def parseTime(str: String): Int = {
    val p: DateTimeFormatter = ISODateTimeFormat.hourMinuteSecondFraction
    val mdt: MutableDateTime = new MutableDateTime(0, getLenientISOChronology)
    var pos: Int = 0
    if (str.startsWith("-")) {
      pos = 1
    }
    val newPos: Int = p.parseInto(mdt, str, pos)
    if (newPos == ~pos) {
      throw new IllegalArgumentException(str)
    }
    var millis: Int = mdt.getMillis.toInt
    if (pos == 1) {
      millis = -millis
    }
    return millis
  }

  private[tz] def parseZoneChar(c: Char): Char = {
    c match {
      case 's' =>
      case 'S' =>
        return 's'
      case 'u' =>
      case 'U' =>
      case 'g' =>
      case 'G' =>
      case 'z' =>
      case 'Z' =>
        return 'u'
      case 'w' =>
      case 'W' =>
      case _ =>
        return 'w'
    }
  }

  /**
   * @return false if error.
   */
  private[tz] def test(id: String, tz: DateTimeZone): Boolean = {
    if (!(id == tz.getID)) {
      return true
    }
    var millis: Long = ISOChronology.getInstanceUTC.year.set(0, 1850)
    var end: Long = ISOChronology.getInstanceUTC.year.set(0, 2050)
    var offset: Int = tz.getOffset(millis)
    var key: String = tz.getNameKey(millis)
    val transitions: List[Long] = new ArrayList[Long]
    while (true) {
      val next: Long = tz.nextTransition(millis)
      if (next == millis || next > end) {
        break //todo: break is not supported
      }
      millis = next
      val nextOffset: Int = tz.getOffset(millis)
      val nextKey: String = tz.getNameKey(millis)
      if (offset == nextOffset && (key == nextKey)) {
        System.out.println("*d* Error in " + tz.getID + " " + new DateTime(millis, ISOChronology.getInstanceUTC))
        return false
      }
      if (nextKey == null || (nextKey.length < 3 && !("??" == nextKey))) {
        System.out.println("*s* Error in " + tz.getID + " " + new DateTime(millis, ISOChronology.getInstanceUTC) + ", nameKey=" + nextKey)
        return false
      }
      transitions.add(Long.valueOf(millis))
      offset = nextOffset
      key = nextKey
    }
    millis = ISOChronology.getInstanceUTC.year.set(0, 2050)
    end = ISOChronology.getInstanceUTC.year.set(0, 1850)
    {
      var i: Int = transitions.size
      while (({
        i -= 1; i
      }) >= 0) {
        val prev: Long = tz.previousTransition(millis)
        if (prev == millis || prev < end) {
          break //todo: break is not supported
        }
        millis = prev
        val trans: Long = transitions.get(i).longValue
        if (trans - 1 != millis) {
          System.out.println("*r* Error in " + tz.getID + " " + new DateTime(millis, ISOChronology.getInstanceUTC) + " != " + new DateTime(trans - 1, ISOChronology.getInstanceUTC))
          return false
        }
      }
    }
    return true
  }

  private[tz] class DateTimeOfYear {
    final val iMonthOfYear: Int = 0
    final val iDayOfMonth: Int = 0
    final val iDayOfWeek: Int = 0
    final val iAdvanceDayOfWeek: Boolean = false
    final val iMillisOfDay: Int = 0
    final val iZoneChar: Char = 0

    private[tz] def this() {
      this()
      iMonthOfYear = 1
      iDayOfMonth = 1
      iDayOfWeek = 0
      iAdvanceDayOfWeek = false
      iMillisOfDay = 0
      iZoneChar = 'w'
    }

    private[tz] def this(st: StringTokenizer) {
      this()
      var month: Int = 1
      var day: Int = 1
      var dayOfWeek: Int = 0
      var millis: Int = 0
      var advance: Boolean = false
      var zoneChar: Char = 'w'
      if (st.hasMoreTokens) {
        month = parseMonth(st.nextToken)
        if (st.hasMoreTokens) {
          var str: String = st.nextToken
          if (str.startsWith("last")) {
            day = -1
            dayOfWeek = parseDayOfWeek(str.substring(4))
            advance = false
          }
          else {
            try {
              day = str.toInt
              dayOfWeek = 0
              advance = false
            }
            catch {
              case e: NumberFormatException => {
                var index: Int = str.indexOf(">=")
                if (index > 0) {
                  day = str.substring(index + 2).toInt
                  dayOfWeek = parseDayOfWeek(str.substring(0, index))
                  advance = true
                }
                else {
                  index = str.indexOf("<=")
                  if (index > 0) {
                    day = str.substring(index + 2).toInt
                    dayOfWeek = parseDayOfWeek(str.substring(0, index))
                    advance = false
                  }
                  else {
                    throw new IllegalArgumentException(str)
                  }
                }
              }
            }
          }
          if (st.hasMoreTokens) {
            str = st.nextToken
            zoneChar = parseZoneChar(str.charAt(str.length - 1))
            if (str == "24:00") {
              if (month == 12 && day == 31) {
                millis = parseTime("23:59:59.999")
              }
              else {
                val date: LocalDate = (if (day == -1) new LocalDate(2001, month, 1).plusMonths(1) else new LocalDate(2001, month, day).plusDays(1))
                advance = (day != -1 && dayOfWeek != 0)
                month = date.getMonthOfYear
                day = date.getDayOfMonth
                if (dayOfWeek != 0) {
                  dayOfWeek = ((dayOfWeek - 1 + 1) % 7) + 1
                }
              }
            }
            else {
              millis = parseTime(str)
            }
          }
        }
      }
      iMonthOfYear = month
      iDayOfMonth = day
      iDayOfWeek = dayOfWeek
      iAdvanceDayOfWeek = advance
      iMillisOfDay = millis
      iZoneChar = zoneChar
    }

    /**
     * Adds a recurring savings rule to the builder.
     */
    def addRecurring(builder: DateTimeZoneBuilder, nameKey: String, saveMillis: Int, fromYear: Int, toYear: Int) {
      builder.addRecurringSavings(nameKey, saveMillis, fromYear, toYear, iZoneChar, iMonthOfYear, iDayOfMonth, iDayOfWeek, iAdvanceDayOfWeek, iMillisOfDay)
    }

    /**
     * Adds a cutover to the builder.
     */
    def addCutover(builder: DateTimeZoneBuilder, year: Int) {
      builder.addCutover(year, iZoneChar, iMonthOfYear, iDayOfMonth, iDayOfWeek, iAdvanceDayOfWeek, iMillisOfDay)
    }

    override def toString: String = {
      return "MonthOfYear: " + iMonthOfYear + "\n" + "DayOfMonth: " + iDayOfMonth + "\n" + "DayOfWeek: " + iDayOfWeek + "\n" + "AdvanceDayOfWeek: " + iAdvanceDayOfWeek + "\n" + "MillisOfDay: " + iMillisOfDay + "\n" + "ZoneChar: " + iZoneChar + "\n"
    }
  }

  private class Rule {
    final val iName: String = null
    final val iFromYear: Int = 0
    final val iToYear: Int = 0
    final val iType: String = null
    final val iDateTimeOfYear: ZoneInfoCompiler.DateTimeOfYear = null
    final val iSaveMillis: Int = 0
    final val iLetterS: String = null

    private[tz] def this(st: StringTokenizer) {
      this()
      iName = st.nextToken.intern
      iFromYear = parseYear(st.nextToken, 0)
      iToYear = parseYear(st.nextToken, iFromYear)
      if (iToYear < iFromYear) {
        throw new IllegalArgumentException
      }
      iType = parseOptional(st.nextToken)
      iDateTimeOfYear = new ZoneInfoCompiler.DateTimeOfYear(st)
      iSaveMillis = parseTime(st.nextToken)
      iLetterS = parseOptional(st.nextToken)
    }

    /**
     * Adds a recurring savings rule to the builder.
     */
    def addRecurring(builder: DateTimeZoneBuilder, nameFormat: String) {
      val nameKey: String = formatName(nameFormat)
      iDateTimeOfYear.addRecurring(builder, nameKey, iSaveMillis, iFromYear, iToYear)
    }

    private def formatName(nameFormat: String): String = {
      var index: Int = nameFormat.indexOf('/')
      if (index > 0) {
        if (iSaveMillis == 0) {
          return nameFormat.substring(0, index).intern
        }
        else {
          return nameFormat.substring(index + 1).intern
        }
      }
      index = nameFormat.indexOf("%s")
      if (index < 0) {
        return nameFormat
      }
      val left: String = nameFormat.substring(0, index)
      val right: String = nameFormat.substring(index + 2)
      var name: String = null
      if (iLetterS == null) {
        name = left.concat(right)
      }
      else {
        name = left + iLetterS + right
      }
      return name.intern
    }

    override def toString: String = {
      return "[Rule]\n" + "Name: " + iName + "\n" + "FromYear: " + iFromYear + "\n" + "ToYear: " + iToYear + "\n" + "Type: " + iType + "\n" + iDateTimeOfYear + "SaveMillis: " + iSaveMillis + "\n" + "LetterS: " + iLetterS + "\n"
    }
  }

  private class RuleSet {
    private var iRules: List[ZoneInfoCompiler.Rule] = null

    private[tz] def this(rule: ZoneInfoCompiler.Rule) {
      this()
      iRules = new ArrayList[ZoneInfoCompiler.Rule]
      iRules.add(rule)
    }

    private[tz] def addRule(rule: ZoneInfoCompiler.Rule) {
      if (!((rule.iName == iRules.get(0).iName))) {
        throw new IllegalArgumentException("Rule name mismatch")
      }
      iRules.add(rule)
    }

    /**
     * Adds recurring savings rules to the builder.
     */
    def addRecurring(builder: DateTimeZoneBuilder, nameFormat: String) {
      {
        var i: Int = 0
        while (i < iRules.size) {
          {
            val rule: ZoneInfoCompiler.Rule = iRules.get(i)
            rule.addRecurring(builder, nameFormat)
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
  }

  private object Zone {
    private def addToBuilder(zone: ZoneInfoCompiler.Zone, builder: DateTimeZoneBuilder, ruleSets: Map[String, ZoneInfoCompiler.RuleSet]) {
      while (zone != null) {
        {
          builder.setStandardOffset(zone.iOffsetMillis)
          if (zone.iRules == null) {
            builder.setFixedSavings(zone.iFormat, 0)
          }
          else {
            try {
              val saveMillis: Int = parseTime(zone.iRules)
              builder.setFixedSavings(zone.iFormat, saveMillis)
            }
            catch {
              case e: Exception => {
                val rs: ZoneInfoCompiler.RuleSet = ruleSets.get(zone.iRules)
                if (rs == null) {
                  throw new IllegalArgumentException("Rules not found: " + zone.iRules)
                }
                rs.addRecurring(builder, zone.iFormat)
              }
            }
          }
          if (zone.iUntilYear == Integer.MAX_VALUE) {
            break //todo: break is not supported
          }
          zone.iUntilDateTimeOfYear.addCutover(builder, zone.iUntilYear)
        }
        zone = zone.iNext
      }
    }
  }

  private class Zone {
    final val iName: String = null
    final val iOffsetMillis: Int = 0
    final val iRules: String = null
    final val iFormat: String = null
    final val iUntilYear: Int = 0
    final val iUntilDateTimeOfYear: ZoneInfoCompiler.DateTimeOfYear = null
    private var iNext: ZoneInfoCompiler.Zone = null

    private[tz] def this(st: StringTokenizer) {
      this()
      `this`(st.nextToken, st)
    }

    private def this(name: String, st: StringTokenizer) {
      this()
      iName = name.intern
      iOffsetMillis = parseTime(st.nextToken)
      iRules = parseOptional(st.nextToken)
      iFormat = st.nextToken.intern
      var year: Int = Integer.MAX_VALUE
      var dtOfYear: ZoneInfoCompiler.DateTimeOfYear = getStartOfYear
      if (st.hasMoreTokens) {
        year = st.nextToken.toInt
        if (st.hasMoreTokens) {
          dtOfYear = new ZoneInfoCompiler.DateTimeOfYear(st)
        }
      }
      iUntilYear = year
      iUntilDateTimeOfYear = dtOfYear
    }

    private[tz] def chain(st: StringTokenizer) {
      if (iNext != null) {
        iNext.chain(st)
      }
      else {
        iNext = new ZoneInfoCompiler.Zone(iName, st)
      }
    }

    /**
     * Adds zone info to the builder.
     */
    def addToBuilder(builder: DateTimeZoneBuilder, ruleSets: Map[String, ZoneInfoCompiler.RuleSet]) {
      Zone.addToBuilder(this, builder, ruleSets)
    }

    override def toString: String = {
      val str: String = "[Zone]\n" + "Name: " + iName + "\n" + "OffsetMillis: " + iOffsetMillis + "\n" + "Rules: " + iRules + "\n" + "Format: " + iFormat + "\n" + "UntilYear: " + iUntilYear + "\n" + iUntilDateTimeOfYear
      if (iNext == null) {
        return str
      }
      return str + "...\n" + iNext.toString
    }
  }

}

class ZoneInfoCompiler {
  private var iRuleSets: Map[String, ZoneInfoCompiler.RuleSet] = null
  private var iZones: List[ZoneInfoCompiler.Zone] = null
  private var iGoodLinks: List[String] = null
  private var iBackLinks: List[String] = null

  def this() {
    this()
    iRuleSets = new HashMap[String, ZoneInfoCompiler.RuleSet]
    iZones = new ArrayList[ZoneInfoCompiler.Zone]
    iGoodLinks = new ArrayList[String]
    iBackLinks = new ArrayList[String]
  }

  /**
   * Returns a map of ids to DateTimeZones.
   *
   * @param outputDir optional directory to write compiled data files to
   * @param sources optional list of source files to parse
   */
  @throws(classOf[IOException])
  def compile(outputDir: File, sources: Array[File]): Map[String, DateTimeZone] = {
    if (sources != null) {
      {
        var i: Int = 0
        while (i < sources.length) {
          {
            val in: BufferedReader = new BufferedReader(new FileReader(sources(i)))
            parseDataFile(in, "backward" == sources(i).getName)
            in.close
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    if (outputDir != null) {
      if (!outputDir.exists) {
        if (!outputDir.mkdirs) {
          throw new IOException("Destination directory doesn't exist and cannot be created: " + outputDir)
        }
      }
      if (!outputDir.isDirectory) {
        throw new IOException("Destination is not a directory: " + outputDir)
      }
    }
    val map: Map[String, DateTimeZone] = new TreeMap[String, DateTimeZone]
    val sourceMap: Map[String, ZoneInfoCompiler.Zone] = new TreeMap[String, ZoneInfoCompiler.Zone]
    System.out.println("Writing zoneinfo files")
    {
      var i: Int = 0
      while (i < iZones.size) {
        {
          val zone: ZoneInfoCompiler.Zone = iZones.get(i)
          val builder: DateTimeZoneBuilder = new DateTimeZoneBuilder
          zone.addToBuilder(builder, iRuleSets)
          val tz: DateTimeZone = builder.toDateTimeZone(zone.iName, true)
          if (ZoneInfoCompiler.test(tz.getID, tz)) {
            map.put(tz.getID, tz)
            sourceMap.put(tz.getID, zone)
            if (outputDir != null) {
              writeZone(outputDir, builder, tz)
            }
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    {
      var i: Int = 0
      while (i < iGoodLinks.size) {
        {
          val baseId: String = iGoodLinks.get(i)
          val alias: String = iGoodLinks.get(i + 1)
          val sourceZone: ZoneInfoCompiler.Zone = sourceMap.get(baseId)
          if (sourceZone == null) {
            System.out.println("Cannot find source zone '" + baseId + "' to link alias '" + alias + "' to")
          }
          else {
            val builder: DateTimeZoneBuilder = new DateTimeZoneBuilder
            sourceZone.addToBuilder(builder, iRuleSets)
            val revived: DateTimeZone = builder.toDateTimeZone(alias, true)
            if (ZoneInfoCompiler.test(revived.getID, revived)) {
              map.put(revived.getID, revived)
              if (outputDir != null) {
                writeZone(outputDir, builder, revived)
              }
            }
            map.put(revived.getID, revived)
            if (ZoneInfoLogger.verbose) {
              System.out.println("Good link: " + alias + " -> " + baseId + " revived")
            }
          }
        }
        i += 2
      }
    }
    {
      var pass: Int = 0
      while (pass < 2) {
        {
          {
            var i: Int = 0
            while (i < iBackLinks.size) {
              {
                val id: String = iBackLinks.get(i)
                val alias: String = iBackLinks.get(i + 1)
                val tz: DateTimeZone = map.get(id)
                if (tz == null) {
                  if (pass > 0) {
                    System.out.println("Cannot find time zone '" + id + "' to link alias '" + alias + "' to")
                  }
                }
                else {
                  map.put(alias, tz)
                  if (ZoneInfoLogger.verbose) {
                    System.out.println("Back link: " + alias + " -> " + tz.getID)
                  }
                }
              }
              i += 2
            }
          }
        }
        ({
          pass += 1; pass - 1
        })
      }
    }
    if (outputDir != null) {
      System.out.println("Writing ZoneInfoMap")
      val file: File = new File(outputDir, "ZoneInfoMap")
      if (!file.getParentFile.exists) {
        file.getParentFile.mkdirs
      }
      val out: OutputStream = new FileOutputStream(file)
      val dout: DataOutputStream = new DataOutputStream(out)
      try {
        val zimap: Map[String, DateTimeZone] = new TreeMap[String, DateTimeZone](String.CASE_INSENSITIVE_ORDER)
        zimap.putAll(map)
        ZoneInfoCompiler.writeZoneInfoMap(dout, zimap)
      } finally {
        dout.close
      }
    }
    return map
  }

  @throws(classOf[IOException])
  private def writeZone(outputDir: File, builder: DateTimeZoneBuilder, tz: DateTimeZone) {
    if (ZoneInfoLogger.verbose) {
      System.out.println("Writing " + tz.getID)
    }
    val file: File = new File(outputDir, tz.getID)
    if (!file.getParentFile.exists) {
      file.getParentFile.mkdirs
    }
    val out: OutputStream = new FileOutputStream(file)
    try {
      builder.writeTo(tz.getID, out)
    } finally {
      out.close
    }
    val in: InputStream = new FileInputStream(file)
    val tz2: DateTimeZone = DateTimeZoneBuilder.readFrom(in, tz.getID)
    in.close
    if (!(tz == tz2)) {
      System.out.println("*e* Error in " + tz.getID + ": Didn't read properly from file")
    }
  }

  @throws(classOf[IOException])
  def parseDataFile(in: BufferedReader, backward: Boolean) {
    var zone: ZoneInfoCompiler.Zone = null
    var line: String = null
    while ((({
      line = in.readLine; line
    })) != null) {
      val trimmed: String = line.trim
      if (trimmed.length == 0 || trimmed.charAt(0) == '#') {
        continue //todo: continue is not supported
      }
      val index: Int = line.indexOf('#')
      if (index >= 0) {
        line = line.substring(0, index)
      }
      val st: StringTokenizer = new StringTokenizer(line, " \t")
      if (Character.isWhitespace(line.charAt(0)) && st.hasMoreTokens) {
        if (zone != null) {
          zone.chain(st)
        }
        continue //todo: continue is not supported
      }
      else {
        if (zone != null) {
          iZones.add(zone)
        }
        zone = null
      }
      if (st.hasMoreTokens) {
        val token: String = st.nextToken
        if (token.equalsIgnoreCase("Rule")) {
          val r: ZoneInfoCompiler.Rule = new ZoneInfoCompiler.Rule(st)
          var rs: ZoneInfoCompiler.RuleSet = iRuleSets.get(r.iName)
          if (rs == null) {
            rs = new ZoneInfoCompiler.RuleSet(r)
            iRuleSets.put(r.iName, rs)
          }
          else {
            rs.addRule(r)
          }
        }
        else if (token.equalsIgnoreCase("Zone")) {
          zone = new ZoneInfoCompiler.Zone(st)
        }
        else if (token.equalsIgnoreCase("Link")) {
          val real: String = st.nextToken
          val alias: String = st.nextToken
          if (backward || (alias == "US/Pacific-New") || alias.startsWith("Etc/") || (alias == "GMT")) {
            iBackLinks.add(real)
            iBackLinks.add(alias)
          }
          else {
            iGoodLinks.add(real)
            iGoodLinks.add(alias)
          }
        }
        else {
          System.out.println("Unknown line: " + line)
        }
      }
    }
    if (zone != null) {
      iZones.add(zone)
    }
  }
}