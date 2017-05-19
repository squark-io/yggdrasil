package test

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.FileAppender
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig
import org.apache.logging.log4j.core.layout.PatternLayout

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-03-26.
 * Copyright 2017
 *
 */
class TestDelegatedMainClass {

  companion object {

    class KBuilder : FileAppender.Builder<KBuilder>()

    @JvmStatic
    fun main(args: Array<String>) {
      val ctx = LogManager.getContext(false) as LoggerContext
      val config = ctx.configuration
      val layout = PatternLayout.newBuilder().withConfiguration(config).withPattern(
        PatternLayout.SIMPLE_CONVERSION_PATTERN)
      val builder = KBuilder().withFileName(args[0]).withLayout(layout.build()).setConfiguration(config).withName(
        "TEST")
      val appender = builder.build()
      appender.start()
      config.addAppender(appender)
      val ref = AppenderRef.createAppenderRef("File", null, null)
      val refs = arrayOf(ref)
      val loggerConfig = LoggerConfig.createLogger(false, Level.INFO, TestDelegatedMainClass::class.java.name,
        "true", refs, null, config, null)
      loggerConfig.addAppender(appender, null, null)
      config.addLogger(TestDelegatedMainClass::class.java.name, loggerConfig)
      ctx.updateLoggers()
      LogManager.getLogger(TestDelegatedMainClass::class.java.name).info(
        "${TestDelegatedMainClass::class.java.name} loaded")
    }
  }
}
