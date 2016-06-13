import java.util.jar.JarEntry
import java.util.jar.JarFile

File configFile = new File(basedir, "target/classes/META-INF/dynamicjar.json");
assert configFile.isFile()
assert configFile.text.contains("\"mainClass\": \"mockclass\"");

JarFile jarFile = new JarFile(new File(basedir, "target/dynamicjar-maven-plugin-it-2-${projectVersion}-dynamicjar.jar"))

JarEntry configEntry = jarFile.getEntry("META-INF/dynamicjar.json") as JarEntry;
assert configEntry != null;

JarEntry classEntry = jarFile.getEntry("io/hakansson/dynamicjar/core/main/Bootstrap.class") as JarEntry
assert classEntry != null;
