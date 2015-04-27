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
 * An immutable time period representing a number of hours.
 * <p>
 * <code>Hours</code> is an immutable period that can only store hours.
 * It does not store years, months or minutes for example. As such it is a
 * type-safe way of representing a number of hours in an application.
 * <p>
 * The number of hours is set in the constructor, and may be queried using
 * <code>getHours()</code>. Basic mathematical operations are provided -
 * <code>plus()</code>, <code>minus()</code>, <code>multipliedBy()</code> and
 * <code>dividedBy()</code>.
 * <p>
 * <code>Hours</code> is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @since 1.4
 */
@SerialVersionUID(87525275727380864L)
object Hours {
  /** Constant representing zero hours. */
  val ZERO: Hours = new Hours(0)
  /** Constant representing one hour. */
  val ONE: Hours = new Hours(1)
  /** Constant representing two hours. */
  val TWO: Hours = new Hours(2)
  /** Constant representing three hours. */
  val THREE: Hours = new Hours(3)
  /** Constant representing four hours. */
  val FOUR: Hours = new Hours(4)
  /** Constant representing five hours. */
  val FIVE: Hours = new Hours(5)
  /** Constant representing six hours. */
  val SIX: Hours = new Hours(6)
  /** Constant representing seven hours. */
  val SEVEN: Hours = new Hours(7)
  /** Constant representing eight hours. */
  val EIGHT: Hours = new Hours(8)
  /** Constant representing the maximum number of hours that can be stored in this object. */
  val MAX_VALUE: Hours = new Hours(Integer.MAX_VALUE)
  /** Constant representing the minimum number of hours that can be stored in this object. */
  val MIN_VALUE: Hours = new Hours(Integer.MIN_VALUE)
  /** The paser to use for this class. */
  private val PARSER: PeriodFormatter = ISOPeriodFormat.standard.withParseType(PeriodType.hours)

  /**
   * Obtains an instance of <code>Hours</code> that may be cached.
   * <code>Hours</code> is immutable, so instances can be cached and shared.
   * This factory method provides access to shared instances.
   *
   * @param hours  the number of hours to obtain an instance for
   * @return the instance of Hours
   */
  def hours(hours: Int): Hours = {
    hours match {
      case 0 =>
        return ZERO
      case 1 =>
        return ONE
      case 2 =>
        return TWO
      case 3 =>
        return THREE
      case 4 =>
        return FOUR
      case 5 =>
        return FIVE
      case 6 =>
        return SIX
      case 7 =>
        return SEVEN
      case 8 =>
        return EIGHT
      case Integer.MAX_VALUE =>
        return MAX_VALUE
      case Integer.MIN_VALUE =>
        return MIN_VALUE
      case _ =>
        return new Hours(hours)
    }
  }

  /**
   * Creates a <code>Hours</code> representing the number of whole hours
   * between the two specified datetimes.
   *
   * @param start  the start instant, must not be null
   * @param end  the end instant, must not be null
   * @return the period in hours
   * @throws IllegalArgumentException if the instants are null or invalid
   */
  def hoursBetween(start: ReadableInstant, end: ReadableInstant): Hours = {
    val amount: Int = BaseSingleFieldPeriod.between(start, end, DurationFieldType.hours)
    return Hours.hours(amount)
  }

  /**
   * Creates a <code>Hours</code> representing the number of whole hours
   * between the two specified partial datetimes.
   * <p>
   * The two partials must contain the same fields, for example you can specify
   * two <code>LocalTime</code> objects.
   *
   * @param start  the start partial date, must not be null
   * @param end  the end partial date, must not be null
   * @return the period in hours
   * @throws IllegalArgumentException if the partials are null or invalid
   */
  def hoursBetween(start: ReadablePartial, end: ReadablePartial): Hours = {
    if (start.isInstanceOf[LocalTime] && end.isInstanceOf[LocalTime]) {
      val chrono: Chronology = DateTimeUtils.getChronology(start.getChronology)
      val hours: Int = chrono.hours.getDifference((end.asInstanceOf[LocalTime]).getLocalMillis, (start.asInstanceOf[LocalTime]).getLocalMillis)
      return Hours.hours(hours)
    }
    val amount: Int = BaseSingleFieldPeriod.between(start, end, ZERO)
    return Hours.hours(amount)
  }

  /**
   * Creates a <code>Hours</code> representing the number of whole hours
   * in the specified interval.
   *
   * @param interval  the interval to extract hours from, null returns zero
   * @return the period in hours
   * @throws IllegalArgumentException if the partials are null or invalid
   */
  def hoursIn(interval: ReadableInterval): Hours = {
    if (interval == null) {
      return Hours.ZERO
    }
    val amount: Int = BaseSingleFieldPeriod.between(interval.getStart, interval.getEnd, DurationFieldType.hours)
    return Hours.hours(amount)
  }

  /**
   * Creates a new <code>Hours</code> representing the number of complete
   * standard length hours in the specified period.
   * <p>
   * This factory method converts all fields from the period to hours using standardised
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
   * @param period  the period to get the number of hours from, null returns zero
   * @return the period in hours
   * @throws IllegalArgumentException if the period contains imprecise duration values
   */
  def standardHoursIn(period: ReadablePeriod): Hours = {
    val amount: Int = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_HOUR)
    return Hours.hours(amount)
  }

  /**
   * Creates a new <code>Hours</code> by parsing a string in the ISO8601 format 'PTnH'.
   * <p>
   * The parse will accept the full ISO syntax of PnYnMnWnDTnHnMnS however only the
   * hours component may be non-zero. If any other component is non-zero, an exception
   * will be thrown.
   *
   * @param periodStr  the period string, null returns zero
   * @return the period in hours
   * @throws IllegalArgumentException if the string format is invalid
   */
  @FromString def parseHours(periodStr: String): Hours = {
    if (periodStr == null) {
      return Hours.ZERO
    }
    val p: Period = PARSER.parsePeriod(periodStr)
    return Hours.hours(p.getHours)
  }
}

@SerialVersionUID(87525275727380864L)
final class Hours extends BaseSingleFieldPeriod {
  /**
   * Creates a new instance representing a number of hours.
   * You should consider using the factory method {@link #hours(int)}
   * instead of the constructor.
   *
   * @param hours  the number of hours to represent
   */
  private def this(hours: Int) {
    this()
    `super`(hours)
  }

  /**
   * Resolves singletons.
   *
   * @return the singleton instance
   */
  private def readResolve: AnyRef = {
    return Hours.hours(getValue)
  }

  /**
   * Gets the duration field type, which is <code>hours</code>.
   *
   * @return the period type
   */
  def getFieldType: DurationFieldType = {
    return DurationFieldType.hours
  }

  /**
   * Gets the period type, which is <code>hours</code>.
   *
   * @return the period type
   */
  def getPeriodType: PeriodType = {
    return PeriodType.hours
  }

  /**
   * Converts this period in hours to a period in weeks assuming a
   * 7 day week and 24 hour day.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all weeks are 7 days
   * long and all days are 24 hours long.
   * This is not true when daylight savings time is considered, and may also
   * not be true for some unusual chronologies. However, it is included as it
   * is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of whole weeks for this number of hours
   */
  def toStandardWeeks: Weeks = {
    return Weeks.weeks(getValue / DateTimeConstants.HOURS_PER_WEEK)
  }

  /**
   * Converts this period in hours to a period in days assuming a
   * 24 hour day.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all days are 24 hours long.
   * This is not true when daylight savings time is considered, and may also
   * not be true for some unusual chronologies. However, it is included as it
   * is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of whole days for this number of hours
   */
  def toStandardDays: Days = {
    return Days.days(getValue / DateTimeConstants.HOURS_PER_DAY)
  }

  /**
   * Converts this period in hours to a period in minutes assuming a
   * 60 minute hour.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all hours are 60 minutes long.
   * This may not be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of minutes for this number of hours
   * @throws ArithmeticException if the number of minutes is too large to be represented
   */
  def toStandardMinutes: Minutes = {
    return Minutes.minutes(FieldUtils.safeMultiply(getValue, DateTimeConstants.MINUTES_PER_HOUR))
  }

  /**
   * Converts this period in hours to a period in seconds assuming a
   * 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all hours are
   * 60 minutes long and all minutes are 60 seconds long.
   * This may not be true for some unusual chronologies. However, it is included
   * as it is a useful operation for many applications and business rules.
   *
   * @return a period representing the number of seconds for this number of hours
   * @throws ArithmeticException if the number of seconds is too large to be represented
   */
  def toStandardSeconds: Seconds = {
    return Seconds.seconds(FieldUtils.safeMultiply(getValue, DateTimeConstants.SECONDS_PER_HOUR))
  }

  /**
   * Converts this period in hours to a duration in milliseconds assuming a
   * 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert from a period to a duration.
   * However to achieve this it makes the assumption that all hours are
   * 60 minutes and all minutes are 60 seconds. This might not be true for an
   * unusual chronology, for example one that takes leap seconds into account.
   * However, the method is included as it is a useful operation for many
   * applications and business rules.
   *
   * @return a duration equivalent to this number of hours
   */
  def toStandardDuration: Duration = {
    val hours: Long = getValue
    return new Duration(hours * DateTimeConstants.MILLIS_PER_HOUR)
  }

  /**
   * Gets the number of hours that this period represents.
   *
   * @return the number of hours in the period
   */
  def getHours: Int = {
    return getValue
  }

  /**
   * Returns a new instance with the specified number of hours added.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to add, may be negative
   * @return the new period plus the specified number of hours
   * @throws ArithmeticException if the result overflows an int
   */
  def plus(hours: Int): Hours = {
    if (hours == 0) {
      return this
    }
    return Hours.hours(FieldUtils.safeAdd(getValue, hours))
  }

  /**
   * Returns a new instance with the specified number of hours added.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to add, may be negative, null means zero
   * @return the new period plus the specified number of hours
   * @throws ArithmeticException if the result overflows an int
   */
  def plus(hours: Hours): Hours = {
    if (hours == null) {
      return this
    }
    return plus(hours.getValue)
  }

  /**
   * Returns a new instance with the specified number of hours taken away.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to take away, may be negative
   * @return the new period minus the specified number of hours
   * @throws ArithmeticException if the result overflows an int
   */
  def minus(hours: Int): Hours = {
    return plus(FieldUtils.safeNegate(hours))
  }

  /**
   * Returns a new instance with the specified number of hours taken away.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to take away, may be negative, null means zero
   * @return the new period minus the specified number of hours
   * @throws ArithmeticException if the result overflows an int
   */
  def minus(hours: Hours): Hours = {
    if (hours == null) {
      return this
    }
    return minus(hours.getValue)
  }

  /**
   * Returns a new instance with the hours multiplied by the specified scalar.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param scalar  the amount to multiply by, may be negative
   * @return the new period multiplied by the specified scalar
   * @throws ArithmeticException if the result overflows an int
   */
  def multipliedBy(scalar: Int): Hours = {
    return Hours.hours(FieldUtils.safeMultiply(getValue, scalar))
  }

  /**
   * Returns a new instance with the hours divided by the specified divisor.
   * The calculation uses integer division, thus 3 divided by 2 is 1.
   * <p>
   * This instance is immutable and unaffected by this method call.
   *
   * @param divisor  the amount to divide by, may be negative
   * @return the new period divided by the specified divisor
   * @throws ArithmeticException if the divisor is zero
   */
  def dividedBy(divisor: Int): Hours = {
    if (divisor == 1) {
      return this
    }
    return Hours.hours(getValue / divisor)
  }

  /**
   * Returns a new instance with the hours value negated.
   *
   * @return the new period with a negated value
   * @throws ArithmeticException if the result overflows an int
   */
  def negated: Hours = {
    return Hours.hours(FieldUtils.safeNegate(getValue))
  }

  /**
   * Is this hours instance greater than the specified number of hours.
   *
   * @param other  the other period, null means zero
   * @return true if this hours instance is greater than the specified one
   */
  def isGreaterThan(other: Hours): Boolean = {
    if (other == null) {
      return getValue > 0
    }
    return getValue > other.getValue
  }

  /**
   * Is this hours instance less than the specified number of hours.
   *
   * @param other  the other period, null means zero
   * @return true if this hours instance is less than the specified one
   */
  def isLessThan(other: Hours): Boolean = {
    if (other == null) {
      return getValue < 0
    }
    return getValue < other.getValue
  }

  /**
   * Gets this instance as a String in the ISO8601 duration format.
   * <p>
   * For example, "PT4H" represents 4 hours.
   *
   * @return the value as an ISO8601 string
   */
  /*@ToString*/ override def toString: String = {
    return "PT" + String.valueOf(getValue) + "H"
  }
}