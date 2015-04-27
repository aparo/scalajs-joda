/*
 *  Copyright 2001-2006 Stephen Colebourne
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

/**
 * Exception thrown when attempting to set a field outside its supported range.
 *
 * @author Brian S O'Neill
 * @since 1.1
 */
@SerialVersionUID(6305711765985447737L)
object IllegalFieldValueException {
  /**
   * Creates a message for the exception.
   *
   * @param fieldName  the field name
   * @param value  the value rejected
   * @param lowerBound  the lower bound allowed
   * @param upperBound  the uppe bound allowed
   * @param explain  an explanation
   * @return the message
   */
  private def createMessage(fieldName: String, value: Number, lowerBound: Number, upperBound: Number, explain: String): String = {
    val buf: StringBuilder = new StringBuilder().append("Value ").append(value).append(" for ").append(fieldName).append(' ')
    if (lowerBound == null) {
      if (upperBound == null) {
        buf.append("is not supported")
      }
      else {
        buf.append("must not be larger than ").append(upperBound)
      }
    }
    else if (upperBound == null) {
      buf.append("must not be smaller than ").append(lowerBound)
    }
    else {
      buf.append("must be in the range [").append(lowerBound).append(',').append(upperBound).append(']')
    }
    if (explain != null) {
      buf.append(": ").append(explain)
    }
    return buf.toString
  }

  /**
   * Creates a message for the exception.
   *
   * @param fieldName  the field name
   * @param value  the value rejected
   * @return the message
   */
  private def createMessage(fieldName: String, value: String): String = {
    val buf: StringBuffer = new StringBuffer().append("Value ")
    if (value == null) {
      buf.append("null")
    }
    else {
      buf.append('"')
      buf.append(value)
      buf.append('"')
    }
    buf.append(" for ").append(fieldName).append(' ').append("is not supported")
    return buf.toString
  }
}

@SerialVersionUID(6305711765985447737L)
class IllegalFieldValueException extends IllegalArgumentException {
  private final val iDateTimeFieldType: DateTimeFieldType = null
  private final val iDurationFieldType: DurationFieldType = null
  private final val iFieldName: String = null
  private final val iNumberValue: Number = null
  private final val iStringValue: String = null
  private final val iLowerBound: Number = null
  private final val iUpperBound: Number = null
  private var iMessage: String = null

  /**
   * Constructor.
   *
   * @param fieldType  type of field being set
   * @param value  illegal value being set
   * @param lowerBound  lower legal field value, or null if not applicable
   * @param upperBound  upper legal field value, or null if not applicable
   */
  def this(fieldType: DateTimeFieldType, value: Number, lowerBound: Number, upperBound: Number) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldType.getName, value, lowerBound, upperBound, null))
    iDateTimeFieldType = fieldType
    iDurationFieldType = null
    iFieldName = fieldType.getName
    iNumberValue = value
    iStringValue = null
    iLowerBound = lowerBound
    iUpperBound = upperBound
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldType  type of field being set
   * @param value  illegal value being set
   * @param explain  an explanation
   * @since 1.5
   */
  def this(fieldType: DateTimeFieldType, value: Number, explain: String) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldType.getName, value, null, null, explain))
    iDateTimeFieldType = fieldType
    iDurationFieldType = null
    iFieldName = fieldType.getName
    iNumberValue = value
    iStringValue = null
    iLowerBound = null
    iUpperBound = null
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldType  type of field being set
   * @param value  illegal value being set
   * @param lowerBound  lower legal field value, or null if not applicable
   * @param upperBound  upper legal field value, or null if not applicable
   */
  def this(fieldType: DurationFieldType, value: Number, lowerBound: Number, upperBound: Number) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldType.getName, value, lowerBound, upperBound, null))
    iDateTimeFieldType = null
    iDurationFieldType = fieldType
    iFieldName = fieldType.getName
    iNumberValue = value
    iStringValue = null
    iLowerBound = lowerBound
    iUpperBound = upperBound
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldName  name of field being set
   * @param value  illegal value being set
   * @param lowerBound  lower legal field value, or null if not applicable
   * @param upperBound  upper legal field value, or null if not applicable
   */
  def this(fieldName: String, value: Number, lowerBound: Number, upperBound: Number) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldName, value, lowerBound, upperBound, null))
    iDateTimeFieldType = null
    iDurationFieldType = null
    iFieldName = fieldName
    iNumberValue = value
    iStringValue = null
    iLowerBound = lowerBound
    iUpperBound = upperBound
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldType  type of field being set
   * @param value  illegal value being set
   */
  def this(fieldType: DateTimeFieldType, value: String) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldType.getName, value))
    iDateTimeFieldType = fieldType
    iDurationFieldType = null
    iFieldName = fieldType.getName
    iStringValue = value
    iNumberValue = null
    iLowerBound = null
    iUpperBound = null
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldType  type of field being set
   * @param value  illegal value being set
   */
  def this(fieldType: DurationFieldType, value: String) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldType.getName, value))
    iDateTimeFieldType = null
    iDurationFieldType = fieldType
    iFieldName = fieldType.getName
    iStringValue = value
    iNumberValue = null
    iLowerBound = null
    iUpperBound = null
    iMessage = super.getMessage
  }

  /**
   * Constructor.
   *
   * @param fieldName  name of field being set
   * @param value  illegal value being set
   */
  def this(fieldName: String, value: String) {
    this()
    `super`(IllegalFieldValueException.createMessage(fieldName, value))
    iDateTimeFieldType = null
    iDurationFieldType = null
    iFieldName = fieldName
    iStringValue = value
    iNumberValue = null
    iLowerBound = null
    iUpperBound = null
    iMessage = super.getMessage
  }

  /**
   * Returns the DateTimeFieldType whose value was invalid, or null if not applicable.
   *
   * @return the datetime field type
   */
  def getDateTimeFieldType: DateTimeFieldType = {
    return iDateTimeFieldType
  }

  /**
   * Returns the DurationFieldType whose value was invalid, or null if not applicable.
   *
   * @return the duration field type
   */
  def getDurationFieldType: DurationFieldType = {
    return iDurationFieldType
  }

  /**
   * Returns the name of the field whose value was invalid.
   *
   * @return the field name
   */
  def getFieldName: String = {
    return iFieldName
  }

  /**
   * Returns the illegal integer value assigned to the field, or null if not applicable.
   *
   * @return the value
   */
  def getIllegalNumberValue: Number = {
    return iNumberValue
  }

  /**
   * Returns the illegal string value assigned to the field, or null if not applicable.
   *
   * @return the value
   */
  def getIllegalStringValue: String = {
    return iStringValue
  }

  /**
   * Returns the illegal value assigned to the field as a non-null string.
   *
   * @return the value
   */
  def getIllegalValueAsString: String = {
    var value: String = iStringValue
    if (value == null) {
      value = String.valueOf(iNumberValue)
    }
    return value
  }

  /**
   * Returns the lower bound of the legal value range, or null if not applicable.
   *
   * @return the lower bound
   */
  def getLowerBound: Number = {
    return iLowerBound
  }

  /**
   * Returns the upper bound of the legal value range, or null if not applicable.
   *
   * @return the upper bound
   */
  def getUpperBound: Number = {
    return iUpperBound
  }

  override def getMessage: String = {
    return iMessage
  }

  /**
   * Provide additional detail by prepending a message to the existing message.
   * A colon is separator is automatically inserted between the messages.
   * @since 1.3
   */
  def prependMessage(message: String) {
    if (iMessage == null) {
      iMessage = message
    }
    else if (message != null) {
      iMessage = message + ": " + iMessage
    }
  }
}