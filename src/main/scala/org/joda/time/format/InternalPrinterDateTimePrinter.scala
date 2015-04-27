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
import java.util.Locale
import org.joda.time.Chronology
import org.joda.time.DateTimeZone
import org.joda.time.ReadablePartial

/**
 * Adapter between old and new printer interface.
 *
 * @author Stephen Colebourne
 * @since 2.4
 */
object InternalPrinterDateTimePrinter {
  private[format] def of(underlying: InternalPrinter): DateTimePrinter = {
    if (underlying.isInstanceOf[DateTimePrinterInternalPrinter]) {
      return (underlying.asInstanceOf[DateTimePrinterInternalPrinter]).getUnderlying
    }
    if (underlying.isInstanceOf[DateTimePrinter]) {
      return underlying.asInstanceOf[DateTimePrinter]
    }
    if (underlying == null) {
      return null
    }
    return new InternalPrinterDateTimePrinter(underlying)
  }
}

class InternalPrinterDateTimePrinter extends DateTimePrinter with InternalPrinter {
  private final val underlying: InternalPrinter = null

  private def this(underlying: InternalPrinter) {
    this()
    this.underlying = underlying
  }

  def estimatePrintedLength: Int = {
    return underlying.estimatePrintedLength
  }

  def printTo(buf: StringBuffer, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
    try {
      underlying.printTo(buf, instant, chrono, displayOffset, displayZone, locale)
    }
    catch {
      case ex: IOException => {
      }
    }
  }

  @throws(classOf[IOException])
  def printTo(out: Writer, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
    underlying.printTo(out, instant, chrono, displayOffset, displayZone, locale)
  }

  @throws(classOf[IOException])
  def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
    underlying.printTo(appendable, instant, chrono, displayOffset, displayZone, locale)
  }

  def printTo(buf: StringBuffer, partial: ReadablePartial, locale: Locale) {
    try {
      underlying.printTo(buf, partial, locale)
    }
    catch {
      case ex: IOException => {
      }
    }
  }

  @throws(classOf[IOException])
  def printTo(out: Writer, partial: ReadablePartial, locale: Locale) {
    underlying.printTo(out, partial, locale)
  }

  @throws(classOf[IOException])
  def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
    underlying.printTo(appendable, partial, locale)
  }

  override def equals(obj: AnyRef): Boolean = {
    if (obj eq this) {
      return true
    }
    if (obj.isInstanceOf[InternalPrinterDateTimePrinter]) {
      val other: InternalPrinterDateTimePrinter = obj.asInstanceOf[InternalPrinterDateTimePrinter]
      return underlying == other.underlying
    }
    return false
  }
}