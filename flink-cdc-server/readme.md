## Extend third party jar
* Modify the source code and package 
* Use the same Fully qualified class name in your project(This can override the original class in dependency)

## Class load order in Java If the fully qualified class name are the same
In Java, when two or more classes have the same fully qualified class name, the class loading order depends on the order in which the classpath elements are searched.
The classpath is an environment variable that tells the Java Virtual Machine (JVM) where to find classes at runtime. The classpath can include directories, JAR files, and other types of archives that contain Java class files.
When the JVM needs to load a class, it searches for the class in the following order:

* **Bootstrap classes:** The JVM first searches for the class in the bootstrap classes, which are the core classes that are part of the JVM implementation.
* **Extension classes:** If the class is not found in the bootstrap classes, the JVM searches for the class in the extension classes. The extension classes are located in the jre/lib/ext directory.
* **System classes:** If the class is not found in the extension classes, the JVM searches for the class in the system classes. The system classes are located in the directories and JAR files specified by the CLASSPATH environment variable.
* **Application classes:** If the class is not found in the system classes, the JVM searches for the class in the application classes. The application classes are located in the directories and JAR files specified by the classpath argument on the command line.
* **Third Party Dependencies** If the class is not found in the application classes,the JVM searches for the class in the third party depenedencides. The third party classes are load sequentially in pom.xml or load in alphabetical order in lib directory. 
If two or more classes have the same fully qualified class name and are located in different directories or JAR files in the classpath, the class that is loaded first depends on the order in which the directories or JAR files appear on the classpath. The JVM searches the classpath from left to right and loads the first class that matches the fully qualified class name.
If you have two classes with the same fully qualified class name and you want to make sure that a specific class is loaded first, you can rearrange the order of directories or JAR files on the classpath. Alternatively, you can give the classes different fully qualified class names to avoid name collisions.

## Package the project 

* **The following plugins in parent project pom.xml need to be commented out** 
  * com.diffplug.spotless
  * maven-checkstyle-plugin

* **Tick toggle skip test mode when package**
* Use following plugin in subprojects when package can export the depedencies as a lib
```xml
<build>
  <plugins>

    <!-- copy dependencies to target/lib and Manifest reply lib/** -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <executions>
        <execution>
          <id>copy-dependencies</id>
          <phase>prepare-package</phase>
          <goals>
            <goal>copy-dependencies</goal>
          </goals>
          <configuration>
            <outputDirectory>
              ${project.build.directory}/lib
            </outputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-jar-plugin</artifactId>
      <!-- <version>2.4</version>-->
      <!-- 对要打的jar包进行配置 -->
      <configuration>
        <!-- Configuration of the archiver -->
        <archive>
          <!--生成的jar中，不要包含pom.xml和pom.properties这两个文件-->
          <addMavenDescriptor>false</addMavenDescriptor>
          <!-- Manifest specific configuration -->
          <manifest>
            <mainClass>
              org.nepkisa.App
            </mainClass>
            <!--是否要把第三方jar放到manifest的classpath中-->
            <addClasspath>true</addClasspath>
            <!-- 生成的manifest中classpath的前缀， 因为要把第三方jar放到lib目录下， 所以classpath的前缀是lib/ -->
            <classpathPrefix>lib/</classpathPrefix>
          </manifest>
        </archive>
        <!--过滤掉不希望包含在jar中的文件-->
        <excludes>
          <!-- 排除不需要的文件夹(路径是jar包内部的路径) -->
          <exclude>**/assembly/</exclude>
        </excludes>
      </configuration>
    </plugin>
  </plugins>
</build> 
```
