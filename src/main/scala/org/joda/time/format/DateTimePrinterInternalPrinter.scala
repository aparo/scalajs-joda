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
object DateTimePrinterInternalPrinter {
  private[format] def of(underlying: DateTimePrinter): InternalPrinter = {
    if (underlying.isInstanceOf[InternalPrinterDateTimePrinter]) {
      return underlying.asInstanceOf[InternalPrinter]
    }
    if (underlying == null) {
      return null
    }
    return new DateTimePrinterInternalPrinter(underlying)
  }
}

class DateTimePrinterInternalPrinter extends InternalPrinter {
  private final val underlying: DateTimePrinter = null

  private def this(underlying: DateTimePrinter) {
    this()
    this.underlying = underlying
  }

  private[format] def getUnderlying: DateTimePrinter = {
    return underlying
  }

  def estimatePrintedLength: Int = {
    return underlying.estimatePrintedLength
  }

  @throws(classOf[IOException])
  def printTo(appendable: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
    if (appendable.isInstanceOf[StringBuffer]) {
      val buf: StringBuffer = appendable.asInstanceOf[StringBuffer]
      underlying.printTo(buf, instant, chrono, displayOffset, displayZone, locale)
    }
    if (appendable.isInstanceOf[Writer]) {
      val out: Writer = appendable.asInstanceOf[Writer]
      underlying.printTo(out, instant, chrono, displayOffset, displayZone, locale)
    }
    val buf: StringBuffer = new StringBuffer(estimatePrintedLength)
    underlying.printTo(buf, instant, chrono, displayOffset, displayZone, locale)
    appendable.append(buf)
  }

  @throws(classOf[IOException])
  def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
    if (appendable.isInstanceOf[StringBuffer]) {
      val buf: StringBuffer = appendable.asInstanceOf[StringBuffer]
      underlying.printTo(buf, partial, locale)
    }
    if (appendable.isInstanceOf[Writer]) {
      val out: Writer = appendable.asInstanceOf[Writer]
      underlying.printTo(out, partial, locale)
    }
    val buf: StringBuffer = new StringBuffer(estimatePrintedLength)
    underlying.printTo(buf, partial, locale)
    appendable.append(buf)
  }
}