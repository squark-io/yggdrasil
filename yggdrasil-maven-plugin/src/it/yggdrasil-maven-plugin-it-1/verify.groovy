import java.util.jar.JarEntry
import java.util.jar.JarFile

File configFile = new File(basedir, "target/classes/META-INF/yggdrasil.json");
assert configFile.isFile()
assert configFile.text.contains("\"mainClass\": \"mockclass\"");

JarFile jarFile = new JarFile(new File(basedir, "target/yggdrasil-maven-plugin-it-1-${projectVersion}-yggdrasil.jar"))

JarEntry configEntry = jarFile.getEntry("META-INF/yggdrasil.json") as JarEntry;
assert configEntry != null;

JarEntry classEntry = jarFile.getEntry("io/squark/yggdrasil/core/main/Bootstrap.class") as JarEntry
assert classEntry != null;
