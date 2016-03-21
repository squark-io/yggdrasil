File itLog = new File(basedir, "target/it.log");
assert itLog.isFile()

assert itLog.text.contains("[main] INFO  org.dynamicjar.test.YamlConfigFileTestTarget - YamlConfigFileTestTarget: Hello world");