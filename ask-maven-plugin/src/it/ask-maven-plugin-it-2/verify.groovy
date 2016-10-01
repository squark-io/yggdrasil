import java.util.jar.JarEntry
import java.util.jar.JarFile

File configFile = new File(basedir, "target/classes/META-INF/ask.json");
assert configFile.isFile()
assert configFile.text.contains("\"mainClass\": \"mockclass\"");

JarFile jarFile = new JarFile(new File(basedir, "target/ask-maven-plugin-it-2-${projectVersion}-ask.jar"))

JarEntry configEntry = jarFile.getEntry("META-INF/ask.json") as JarEntry;
assert configEntry != null;

JarEntry classEntry = jarFile.getEntry("io/squark/ask/core/main/Bootstrap.class") as JarEntry
assert classEntry != null;
