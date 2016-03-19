File itLog = new File(basedir, "target/it.log");
assert itLog.isFile()

assert itLog.text.contains("[main] INFO  org.dynamicjar.test.JsonConfigFileTestTarget - JsonConfigFileTestTarget: Hello world");