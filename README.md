Here's the English version of the README:
[한국어 버전은 여기를 클릭하세요.](./README-ko.md)

# First Steps
In this guide, you'll learn the core of the Avnoi framework. We'll create a basic CRUD application to get familiar with Avnoi!

### Language
Most examples here use Java, but you can also implement using Kotlin.

### Prerequisites
**IDE**: `We recommend using IntelliJ IDEA for development.`  
**Build System**: `Choose either Gradle or Maven as your project build system.`

#### Adding Dependencies
**Gradle**  
Add the following to the `dependencies` block in your `build.gradle` file:
```gradle
dependencies {
    implementation group: 'io.github.3x-haust', name: 'avnoi-framework', version: '0.1.5'
    // ... other dependencies
}
```
**Maven**  
Add the following within the `<dependencies>` tag in your `pom.xml` file:
```xml
<dependency>
    <groupId>io.github.3x-haust</groupId>
    <artifactId>avnoi-framework</artifactId>
    <version>0.1.5</version>
</dependency>
```
<br/>

### Creating a Project
<img width="500" alt="image" src="https://github.com/user-attachments/assets/443d28cf-eb7f-41b3-b6cc-16759f094353">
<br></br>

The basic structure of the project is as follows:

```
src
├─ AppController.java
├─ AppModule.java
├─ AppService.java
└─ Main.java
```

<br></br>

Here's a brief explanation of these core files:
|File Name|Description|
|:---:|:---|
|`AppController.java`|File containing a basic controller with a single route|
|`AppModule.java`|File containing the root module of the application|
|`AppService.java`|File containing a basic service with a single method|
|`Main.java`|Entry file. Uses the core function `Avnoi` to create an Avnoi application instance.|
> It's okay if you don't understand the controller, service, etc. mentioned above. Detailed explanations will come in later chapters!

<br><br/>

Let's start by creating the Main.java file. This file contains the Main method that starts the application.
  
```Java
//Main.java
package <package>;
import io.github._3xhaust.Avnoi;
import io.github._3xhaust.annotations.AvnoiApplication;
@AvnoiApplication
public class Main {
    public static void main(String[] args) {
        Avnoi.listen(3000);
        Avnoi.run(AppModule.class);
    }
}
```

<details>
<summary>Kotlin</summary>
  
```kt
//Main.kt
package <package>
import AppModule
import io.github._3xhaust.annotations.AvnoiApplication
@AvnoiApplication
class MainApp
fun main() {
    Avnoi.listen(3000)
    Avnoi.run(AppModule::class.java)
}
```
</details>

When creating an Avnoi application instance, we use the `Avnoi` class. This class provides several static methods for creating application instances, among which the `run` method used above starts the Avnoi application.
The code in the above example can start a listener waiting for inbound HTTP requests.
As you can see, the Avnoi framework is structured so that each module has its own dedicated directory.

<br><br/>

### Running the Application
If you're using IntelliJ IDEA, you can run the application by clicking the green triangle button in the top right corner.
