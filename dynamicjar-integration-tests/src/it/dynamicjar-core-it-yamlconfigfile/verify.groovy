File itLog = new File(basedir, "target/it.log");
assert itLog.isFile()

assert itLog.text.contains("[main] INFO  io.hakansson.dynamicjar.core.it.YamlConfigFileTestTarget - YamlConfigFileTestTarget: Hello world");