File itLog = new File(basedir, "target/it.log");
assert itLog.isFile()

assert itLog.text.contains("dynamicjar-weld: HELLO");