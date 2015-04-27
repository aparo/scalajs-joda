/*
 *  Copyright 2001-2010 Stephen Colebourne
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
package org.joda.time

import org.joda.convert.FromString
import org.joda.convert.ToString
import org.joda.time.base.BaseSingleFieldPeriod
import org.joda.time.field.FieldUtils
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter

/**
 * An immutable time period representing a number of minutes.
 * <p>
 * <code>Minutes</code> is an immutable period that can only store minutes.
 * It does not store years, months or hours for example. As such it is a
 * type-safe way of representing a number of minutes in an application.
 * <p>
 * The number of minutes is set in the constructor, and may be queried using
 * <code>getMinutes()</code>. Basic mathematical operations are provided -
 * <code>plus()</code>, <code>minus()</code>, <code>multipliedBy()</code> and
 * <code>dividedBy()</code>.
 * <p>
 * <code>Minutes</code> is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @since 1.4
 */
@SerialVersionUID(87525275727380863L)
object Minutes {
  /** Constant representing zero minutes. */
  val ZERO: Minutes = new Minutes(0)
  /** Constant representing one minute. */
  val ONE: Minutes = new Minutes(1)
  /** Constant representing two minutes. */
  val TWO: Minutes = new Minutes(2)
  /** Constant representing three minutes. */
  val THREE: Minutes = new Minutes(3)
  /** Constant representing the maximum number of minutes that can be stored in this object. */
  val MAX_VALUE: Minutes = new Minutes(Integer.MAX_VALUE)
  /** Constant representing the minimum number of minutes that can be stored in this object. */
  val MIN_VALUE: Minutes = new Minutes(Integer.MIN_VALUE)
  /** The paser to use for this class. */
  private val PARSER: PeriodFormatter = ISOPeriodFormat.standard.withParseType(PeriodType.minutes)

  /**
   * Obtains an instance of <code>Minutes</code> that may be cached.
   * <code>Minutes</code> is immutable, so instances can be cached and shared.
   * This factory method provides access to shared instances.
   *
   * @param minutes  the number of minutes to obtain an instance for
   * @return the instance of Minutes
   */
  def minutes(minutes: Int): Minutes = {
    minutes match {
      case 0 =>
        return ZERO
      case 1 =>
        return ONE
      case 2 =>
        return TWO
      case 3 =>
        return THREE
      case Integer.MAX_VALUE =>
        return MAX_VALUE
      case Integer.MIN_VALUE =>
        return MIN_VALUE
      case _ =>
        return new Minutes(minutes)
    }
  }

  /**
   * Creates a <code>Minutes</code> representing the number of whole minutes
   * between the two specified datetimes.
   *
   * @param start  the start instant, must not be null
   * @param end  the end instant, must not be null
   * @return the period in minutes
   * @throws IllegalArgumentException if the instants are null or invalid
   */
  def minutesBetween(start: ReadableInstant, end: ReadableInstant): Minutes = {
    val amount: Int = BaseSingleFieldPeriod.between(start, end, DurationFieldType.minutes)
    return Minutes.minutes(amount)
  }

  /**
   * Creates a <code>Minutes</code> representing the number of whole minutes
   * between the two specified partial datetimes.
   * <p>
   * The two partials must contain the same fields, for example you can specify
   * two <code>LocalTime</code> objects.
   *
   * @param start  the start partial date, must not be null
   * @param end  the end partial date, must not be null
   * @return the period in minutes
   * @throws IllegalArgumentException if the partials are null or invalid
   */
  def minutesBetween(start: ReadablePartial, end: ReadablePartial): Minutes = {
    if (start.isInstanceOf[LocalTime] && end.isInstanceOf[LocalTime]) {
      val chrono: Chronology = DateTimeUtils.getChronology(start.getChronology)
      val minutes: Int = chrono.minutes.getDifference((end.asInstanceOf[LocalTime]).getLocalMillis, (start.asInstanceOf[LocalTime]).getLocalMillis)
      return Minutes.minutes(minutes)
    }
    val amount: Int = BaseSingleFieldPeriod.between(start, end, ZERO)
    return Minutes.minutes(amount)
  }

  /**
   * Creates a <code>Minutes</code> representing the number of whole minutes
   * in the specified interval.
   *
   * @param interval  the interval to extract minutes from, null returns zero
   * @return the period in minutes
   * @throws IllegalArgumentException if the partials are null or invalid
   */
  def minutesIn(interval: ReadableInterval): Minutes = {
    if (interval == null) {
      return Minutes.ZERO
    }
    val amount: Int = BaseSingleFieldPeriod.between(interval.getStart, interval.getEnd, DurationFieldType.minutes)
    return Minutes.minutes(amount)
  }

  /**
   * Creates a new <code>Minutes</code> representing the number of complete
   * standard length minutes in the specified period.
   * <p>
   * This factory method converts all fields from the period to minutes using standardised
   * durations for each field. Only those fields which have a precise duration in
   * the ISO UTC chronology can be converted.
   * <ul>
   * <li>One week consists of 7 days.
   * <li>One day consists of 24 hours.
   * <li>One hour consists of 60 minutes.
   * <li>One minute consists of 60 seconds.
   * <li>One second consists of 1000 milliseconds.
   * </ul>
   * Months and Years are imprecise and periods containing these values cannot be converted.
   *
   * @param period  the period to get the number of minutes from, null returns zero
   * @return the period in minutes
   * @throws IllegalArgumentException if the period contains imprecise duration values
   */
  def standardMinutesIn(period: ReadablePeriod): Minutes = {
    val amount: Int = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_MINUTE)
    return Minutes.minutes(amount)
  }

  /**
   * Creates a new <code>Minutes</code> by parsing a string in the ISO8601 format 'PTnM'.
   * <p>
   * The parse will accept the full ISO syntax of PnYnMnWnDTnHnMnS however only the
   * minutes component may be non-zero. If any other component is non-zero, an exception
   * will be thrown.
   *
   * @param periodStr  the period string, null returns zero
   * @return the period in minutes
   * @throws IllegalArgumentException if the string format is invalid
   */
  @FromString def parseMinutes(periodStr: String): Minutes = {
    if (periodStr == null) {
      return Minutes.ZERO
    }
    val p: Period = PARSER.parsePeriod(periodStr)
    return Minutes.minutes(p.getMinutes)
  }
}

@SerialVersionUID(87525275727380863L)
final class Minutes extends BaseSingleFieldPeriod {
  /**
   * Creates a new instance representing a number of minutes.
   * You should consider using the factory method {@link #minutes(int)}
   * instead of the constructor.
   *
   * @param minutes  the number of minutes to represent
   */
  private def this(minutes: Int) {
    this()
    `super`(minutes)
  }

  /**
   * Resolves singletons.
   *
   * @return the singleton instance
   */
  private def readResolve: AnyRef = {
    return Minutes.minutes(getValue)
  }

  /**
   * Gets the duration field type, which is <code>minutes</code>.
   *
   * @return the period type
   */
  def getFieldType: DurationFieldType = {
    return DurationFieldType.minutes
  }

  /**
   * Gets the period type, which is <code>minutes</code>.
   *
   * @return the period type
   */
  def getPeriodType: PeriodType = {
    return PeriodType.minutes
  }

  /**
   * Converts this period in minutes to a period in weeks assuming a
   * 7 days week, 24 hour day and 60 minute hour.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all weeks are
   * 7 days long, all days are 24 hours long and all hours are 60 minutes long.
   * This is not true when daylight savings is considered and may also not
   * be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of whole weeks for this number of minutes
   */
  def toStandardWeeks: Weeks = {
    return Weeks.weeks(getValue / DateTimeConstants.MINUTES_PER_WEEK)
  }

  /**
   * Converts this period in minutes to a period in days assuming a
   * 24 hour day and 60 minute hour.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all days are
   * 24 hours long and all hours are 60 minutes long.
   * This is not true when daylight savings is considered and may also not
   * be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of whole days for this number of minutes
   */
  def toStandardDays: Days = {
    return Days.days(getValue / DateTimeConstants.MINUTES_PER_DAY)
  }

  /**
   * Converts this period in minutes to a period in hours assuming a
   * 60 minute hour.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all hours are
   * 60 minutes long.
   * This may not be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of hours for this number of minutes
   */
  def toStandardHours: Hours = {
    return Hours.hours(getValue / DateTimeConstants.MINUTES_PER_HOUR)
  }

  /**
   * Converts this period in minutes to a period in seconds assuming a
   * 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all minutes are
   * 60 seconds long.
   * This may not be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of seconds for this number of minutes
   * @throws ArithmeticException if the number of seconds is too large to be represented
   */
  def toStandardSeconds: Seconds = {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue, DateTimeConstants.SECONDS_PER_MINUTE))
  }

  /**
   * Converts this period in minutes to a duration in milliseconds assuming a
   * 60 second minute.
   * <p>
   * This method allows you to convert from a period to a duration.
   * However to achieve this it makes the assumption that all minutes are
   * 60 seconds long. This might not be true for an unusual chronology,
   * for example one that takes leap seconds into account.
   * However, the method is included as it is a useful operation for many
   * applications and business rules.
   *
   * @return a duration equivalent to this number of minutes
   */
  def toStandardDuration: Duration = {
    val minutes: Long = getValue
    return new Duration(minutes * DateTimeConstants.MILLIS_PER_MINUTE)
  }

  /**
   * Gets the number of minutes that this period represents.
   *
   * @return the number of minutes in the period
   */
  def getMinutes: Int = {
    return getValue
  }

  /**
   * Returns a new instance with the specified number of minutes added.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to add, may be negative
   * @return the new period plus the specified number of minutes
   * @throws ArithmeticException if the result overflows an int
   */
  def plus(minutes: Int): Minutes = {
    if (minutes == 0) {
      return this
    }
    return Minutes.minutes(FieldUtils.safeAdd(getValue, minutes))
  }

  /**
   * Returns a new instance with the specified number of minutes added.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to add, may be negative, null means zero
   * @return the new period plus the specified number of minutes
   * @throws ArithmeticException if the result overflows an int
   */
  def plus(minutes: Minutes): Minutes = {
    if (minutes == null) {
      return this
    }
    return plus(minutes.getValue)
  }

  /**
   * Returns a new instance with the specified number of minutes taken away.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to take away, may be negative
   * @return the new period minus the specified number of minutes
   * @throws ArithmeticException if the result overflows an int
   */
  def minus(minutes: Int): Minutes = {
    return plus(FieldUtils.safeNegate(minutes))
  }

  /**
   * Returns a new instance with the specified number of minutes taken away.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to take away, may be negative, null means zero
   * @return the new period minus the specified number of minutes
   * @throws ArithmeticException if the result overflows an int
   */
  def minus(minutes: Minutes): Minutes = {
    if (minutes == null) {
      return this
    }
    return minus(minutes.getValue)
  }

  /**
   * Returns a new instance with the minutes multiplied by the specified scalar.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param scalar  the amount to multiply by, may be negative
   * @return the new period multiplied by the specified scalar
   * @throws ArithmeticException if the result overflows an int
   */
  def multipliedBy(scalar: Int): Minutes = {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue, scalar))
  }

  /**
   * Returns a new instance with the minutes divided by the specified divisor.
   * The calculation uses integer division, thus 3 divided by 2 is 1.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param divisor  the amount to divide by, may be negative
   * @return the new period divided by the specified divisor
   * @throws ArithmeticException if the divisor is zero
   */
  def dividedBy(divisor: Int): Minutes = {
    if (divisor == 1) {
      return this
    }
    return Minutes.minutes(getValue / divisor)
  }

  /**
   * Returns a new instance with the minutes value negated.
   *
   * @return the new period with a negated value
   * @throws ArithmeticException if the result overflows an int
   */
  def negated: Minutes = {
    return Minutes.minutes(FieldUtils.safeNegate(getValue))
  }

  /**
   * Is this minutes instance greater than the specified number of minutes.
   *
   * @param other  the other period, null means zero
   * @return true if this minutes instance is greater than the specified one
   */
  def isGreaterThan(other: Minutes): Boolean = {
    if (other == null) {
      return getValue > 0
    }
    return getValue > other.getValue
  }

  /**
   * Is this minutes instance less than the specified number of minutes.
   *
   * @param other  the other period, null means zero
   * @return true if this minutes instance is less than the specified one
   */
  def isLessThan(other: Minutes): Boolean = {
    if (other == null) {
      return getValue < 0
    }
    return getValue < other.getValue
  }

  /**
   * Gets this instance as a String in the ISO8601 duration format.
   * <p>
   * For example, "PT4M" represents 4 minutes.
   *
   * @return the value as an ISO8601 string
   */
  /*@ToString*/ override def toString: String = {
    return "PT" + String.valueOf(getValue) + "M"
  }
}