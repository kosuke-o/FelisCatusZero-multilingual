package us.feliscat.util.xml

import java.io.{File, IOException, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory, Validator}

import org.xml.sax.ErrorHandler

import scala.io.{BufferedSource, Source}
import scala.xml.{SAXException, SAXParseException}

/**
  * @author K.Sakamoto
  *         Created on 2016/10/28
  */
class XmlSchema(schemaPath: File) {
  def isValid(source: StreamSource): Boolean = {
    val factory: SchemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
    val schema: Schema = factory.newSchema(schemaPath)
    val validator: Validator = schema.newValidator
    validator.setErrorHandler(new MyErrorHandler())
    try {
      validator.validate(source)
      true
    } catch {
      case e: SAXException =>
        System.err.println(e.getMessage)
        false
    }
  }
}

/**
  * @author K.Sakamoto
  */
object MyLogFile {
  private val logFile: File = createLogFile

  private def createLogFile: File = {
    val dir = new File("%s%s%s" format (
      System.getProperty("user.home"),
      File.separator,
      ".essay_qa"))
    if (!dir.exists) {
      dir.mkdir
    }
    if (!dir.isDirectory) {
      dir.delete
      dir.mkdir
    }

    val file = new File("%s%s%s" format (
      dir.toString,
      File.separator,
      "log")
    )
    if (!file.exists) {
      file.createNewFile
    }
    if (!file.isFile) {
      file.delete
      file.createNewFile
    }

    file
  }

  def getLogFile: File = logFile

  def clearLogFile() {
    logFile.deleteOnExit()
  }

  def getLog: String = {
    try {
      if (logFile.canRead) {
        val builder = new StringBuilder()
        val source: BufferedSource = Source.fromFile(logFile)
        for (line <- source.getLines) {
          builder.
            append(line).
            append('\n')
        }
        Option(builder.result) match {
          case Some(result) =>
            result
          case None =>
            ""
        }
      } else {
        val logParentPath: Path = logFile.toPath.getParent
        if (!logParentPath.toFile.canRead) {
          Files.createDirectory(logParentPath)
        }
        logFile.createNewFile()
        ""
      }
    } catch {
      case e: IOException =>
        e.printStackTrace()
        ""
    }
  }
}

/**
  * @author K.Sakamoto
  */
class MyErrorHandler extends ErrorHandler {
  private val logPath: Path = MyLogFile.getLogFile.toPath

  private def getLogFormat(errorCode: String, lineNumber: Int, columnNumber: Int, message: String) = {
    "%s :: [%s] line %d, column %d, %s" format(
      //new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US) format new Date(Calendar.
      //  getInstance(TimeZone getTimeZone "Asia/Tokyo", Locale.US).getTimeInMillis),
      DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).
        format(ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))),
      errorCode, lineNumber, columnNumber, message)
  }

  @throws[SAXException]
  override def warning(e: SAXParseException) {
    val writer = new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8))
    try {
      val message: String = getLogFormat("warning", e.getLineNumber, e.getColumnNumber, e.toString)
      writer.println(message)
      System.err.println(message)
    } catch {
      case e: IOException =>
        e.printStackTrace(writer)
    } finally {
      try {
        writer.close()
      } catch {
        case e: IOException =>
          e.printStackTrace(writer)
      }
    }
  }

  @throws[SAXException]
  override def error(e: SAXParseException) {
    val writer = new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8))
    try {
      val message: String = getLogFormat("error", e.getLineNumber, e.getColumnNumber, e.toString)
      writer.println(message)
      System.err.println(message)
    } catch {
      case e: IOException =>
        e.printStackTrace(writer)
    } finally {
      try {
        writer.close()
      } catch {
        case e: IOException =>
          e.printStackTrace(writer)
      }
    }
  }

  @throws[SAXException]
  override def fatalError(e: SAXParseException) {
    val writer = new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8))
    try {
      val message: String = getLogFormat("fatal error", e.getLineNumber, e.getColumnNumber, e.toString)
      writer.println(message)
      System.err.println(message)
    } catch {
      case e: IOException =>
        e.printStackTrace(writer)
    } finally {
      try {
        writer.close()
      } catch {
        case e: IOException =>
          e.printStackTrace(writer)
      }
    }
  }
}
