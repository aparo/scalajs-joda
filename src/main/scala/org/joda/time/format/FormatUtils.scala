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

/**
 * Utility methods used by formatters.
 * <p>
 * FormatUtils is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
object FormatUtils {
  private val LOG_10: Double = Math.log(10)

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and appends it to the given buffer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param buf receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   */
  def appendPaddedInteger(buf: StringBuffer, value: Int, size: Int) {
    try {
      appendPaddedInteger(buf.asInstanceOf[Appendable], value, size)
    }
    catch {
      case e: IOException => {
      }
    }
  }

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and appends it to the given appendable.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param appenadble receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   * @since 2.4
   */
  @throws(classOf[IOException])
  def appendPaddedInteger(appenadble: Appendable, value: Int, size: Int) {
    if (value < 0) {
      appenadble.append('-')
      if (value != Integer.MIN_VALUE) {
        value = -value
      }
      else {
        while (size > 10) {
          {
            appenadble.append('0')
          }
          ({
            size -= 1; size + 1
          })
        }
        appenadble.append("" + -Integer.MIN_VALUE.toLong)
        return
      }
    }
    if (value < 10) {
      while (size > 1) {
        {
          appenadble.append('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      appenadble.append((value + '0').toChar)
    }
    else if (value < 100) {
      while (size > 2) {
        {
          appenadble.append('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      val d: Int = ((value + 1) * 13421772) >> 27
      appenadble.append((d + '0').toChar)
      appenadble.append((value - (d << 3) - (d << 1) + '0').toChar)
    }
    else {
      var digits: Int = 0
      if (value < 1000) {
        digits = 3
      }
      else if (value < 10000) {
        digits = 4
      }
      else {
        digits = (Math.log(value) / LOG_10).toInt + 1
      }
      while (size > digits) {
        {
          appenadble.append('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      appenadble.append(Integer.toString(value))
    }
  }

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and appends it to the given buffer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param buf receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   */
  def appendPaddedInteger(buf: StringBuffer, value: Long, size: Int) {
    try {
      appendPaddedInteger(buf.asInstanceOf[Appendable], value, size)
    }
    catch {
      case e: IOException => {
      }
    }
  }

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and appends it to the given buffer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param appendable receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   * @since 2.4
   */
  @throws(classOf[IOException])
  def appendPaddedInteger(appendable: Appendable, value: Long, size: Int) {
    val intValue: Int = value.toInt
    if (intValue == value) {
      appendPaddedInteger(appendable, intValue, size)
    }
    else if (size <= 19) {
      appendable.append(Long.toString(value))
    }
    else {
      if (value < 0) {
        appendable.append('-')
        if (value != Long.MIN_VALUE) {
          value = -value
        }
        else {
          while (size > 19) {
            {
              appendable.append('0')
            }
            ({
              size -= 1; size + 1
            })
          }
          appendable.append("9223372036854775808")
          return
        }
      }
      val digits: Int = (Math.log(value) / LOG_10).toInt + 1
      while (size > digits) {
        {
          appendable.append('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      appendable.append(Long.toString(value))
    }
  }

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and writes it to the given writer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param out receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   */
  @throws(classOf[IOException])
  def writePaddedInteger(out: Writer, value: Int, size: Int) {
    if (value < 0) {
      out.write('-')
      if (value != Integer.MIN_VALUE) {
        value = -value
      }
      else {
        while (size > 10) {
          {
            out.write('0')
          }
          ({
            size -= 1; size + 1
          })
        }
        out.write("" + -Integer.MIN_VALUE.toLong)
        return
      }
    }
    if (value < 10) {
      while (size > 1) {
        {
          out.write('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      out.write(value + '0')
    }
    else if (value < 100) {
      while (size > 2) {
        {
          out.write('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      val d: Int = ((value + 1) * 13421772) >> 27
      out.write(d + '0')
      out.write(value - (d << 3) - (d << 1) + '0')
    }
    else {
      var digits: Int = 0
      if (value < 1000) {
        digits = 3
      }
      else if (value < 10000) {
        digits = 4
      }
      else {
        digits = (Math.log(value) / LOG_10).toInt + 1
      }
      while (size > digits) {
        {
          out.write('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      out.write(Integer.toString(value))
    }
  }

  /**
   * Converts an integer to a string, prepended with a variable amount of '0'
   * pad characters, and writes it to the given writer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param out receives integer converted to a string
   * @param value value to convert to a string
   * @param size minimum amount of digits to append
   */
  @throws(classOf[IOException])
  def writePaddedInteger(out: Writer, value: Long, size: Int) {
    val intValue: Int = value.toInt
    if (intValue == value) {
      writePaddedInteger(out, intValue, size)
    }
    else if (size <= 19) {
      out.write(Long.toString(value))
    }
    else {
      if (value < 0) {
        out.write('-')
        if (value != Long.MIN_VALUE) {
          value = -value
        }
        else {
          while (size > 19) {
            {
              out.write('0')
            }
            ({
              size -= 1; size + 1
            })
          }
          out.write("9223372036854775808")
          return
        }
      }
      val digits: Int = (Math.log(value) / LOG_10).toInt + 1
      while (size > digits) {
        {
          out.write('0')
        }
        ({
          size -= 1; size + 1
        })
      }
      out.write(Long.toString(value))
    }
  }

  /**
   * Converts an integer to a string, and appends it to the given buffer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param buf receives integer converted to a string
   * @param value value to convert to a string
   */
  def appendUnpaddedInteger(buf: StringBuffer, value: Int) {
    try {
      appendUnpaddedInteger(buf.asInstanceOf[Appendable], value)
    }
    catch {
      case e: IOException => {
      }
    }
  }

  /**
   * Converts an integer to a string, and appends it to the given appendable.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param appendable receives integer converted to a string
   * @param value value to convert to a string
   * @since 2.4
   */
  @throws(classOf[IOException])
  def appendUnpaddedInteger(appendable: Appendable, value: Int) {
    if (value < 0) {
      appendable.append('-')
      if (value != Integer.MIN_VALUE) {
        value = -value
      }
      else {
        appendable.append("" + -Integer.MIN_VALUE.toLong)
        return
      }
    }
    if (value < 10) {
      appendable.append((value + '0').toChar)
    }
    else if (value < 100) {
      val d: Int = ((value + 1) * 13421772) >> 27
      appendable.append((d + '0').toChar)
      appendable.append((value - (d << 3) - (d << 1) + '0').toChar)
    }
    else {
      appendable.append(Integer.toString(value))
    }
  }

  /**
   * Converts an integer to a string, and appends it to the given buffer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param buf receives integer converted to a string
   * @param value value to convert to a string
   */
  def appendUnpaddedInteger(buf: StringBuffer, value: Long) {
    try {
      appendUnpaddedInteger(buf.asInstanceOf[Appendable], value)
    }
    catch {
      case e: IOException => {
      }
    }
  }

  /**
   * Converts an integer to a string, and appends it to the given appendable.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param appendable receives integer converted to a string
   * @param value value to convert to a string
   */
  @throws(classOf[IOException])
  def appendUnpaddedInteger(appendable: Appendable, value: Long) {
    val intValue: Int = value.toInt
    if (intValue == value) {
      appendUnpaddedInteger(appendable, intValue)
    }
    else {
      appendable.append(Long.toString(value))
    }
  }

  /**
   * Converts an integer to a string, and writes it to the given writer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param out receives integer converted to a string
   * @param value value to convert to a string
   */
  @throws(classOf[IOException])
  def writeUnpaddedInteger(out: Writer, value: Int) {
    if (value < 0) {
      out.write('-')
      if (value != Integer.MIN_VALUE) {
        value = -value
      }
      else {
        out.write("" + -Integer.MIN_VALUE.toLong)
        return
      }
    }
    if (value < 10) {
      out.write(value + '0')
    }
    else if (value < 100) {
      val d: Int = ((value + 1) * 13421772) >> 27
      out.write(d + '0')
      out.write(value - (d << 3) - (d << 1) + '0')
    }
    else {
      out.write(Integer.toString(value))
    }
  }

  /**
   * Converts an integer to a string, and writes it to the given writer.
   *
   * <p>This method is optimized for converting small values to strings.
   *
   * @param out receives integer converted to a string
   * @param value value to convert to a string
   */
  @throws(classOf[IOException])
  def writeUnpaddedInteger(out: Writer, value: Long) {
    val intValue: Int = value.toInt
    if (intValue == value) {
      writeUnpaddedInteger(out, intValue)
    }
    else {
      out.write(Long.toString(value))
    }
  }

  /**
   * Calculates the number of decimal digits for the given value,
   * including the sign.
   */
  def calculateDigitCount(value: Long): Int = {
    if (value < 0) {
      if (value != Long.MIN_VALUE) {
        return calculateDigitCount(-value) + 1
      }
      else {
        return 20
      }
    }
    return (if (value < 10) 1 else (if (value < 100) 2 else (if (value < 1000) 3 else (if (value < 10000) 4 else ((Math.log(value) / LOG_10).toInt + 1)))))
  }

  private[format] def parseTwoDigits(text: CharSequence, position: Int): Int = {
    val value: Int = text.charAt(position) - '0'
    return ((value << 3) + (value << 1)) + text.charAt(position + 1) - '0'
  }

  private[format] def createErrorMessage(text: String, errorPos: Int): String = {
    val sampleLen: Int = errorPos + 32
    var sampleText: String = null
    if (text.length <= sampleLen + 3) {
      sampleText = text
    }
    else {
      sampleText = text.substring(0, sampleLen).concat("...")
    }
    if (errorPos <= 0) {
      return "Invalid format: \"" + sampleText + '"'
    }
    if (errorPos >= text.length) {
      return "Invalid format: \"" + sampleText + "\" is too short"
    }
    return "Invalid format: \"" + sampleText + "\" is malformed at \"" + sampleText.substring(errorPos) + '"'
  }
}

class FormatUtils {
  /**
   * Restricted constructor.
   */
  private def this() {
    this()
  }
}