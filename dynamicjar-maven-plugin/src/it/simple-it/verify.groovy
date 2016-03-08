import java.util.jar.JarEntry
import java.util.jar.JarFile

File configFile = new File(basedir, "target/classes/META-INF/dynamicjar.json");
assert configFile.isFile()

JarFile jarFile = new JarFile(new File(basedir, "target/simple-it-1.0-SNAPSHOT-dynamicjar.jar"))

JarEntry configEntry = jarFile.getEntry("META-INF/dynamicjar.json");
assert configEntry != null;

JarEntry classEntry = jarFile.getEntry("org/dynamicjar/core/main/DynamicJar.class")
assert classEntry != null;
