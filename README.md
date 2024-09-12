This is the English version of the README.  
[한국어 버전은 여기를 클릭하세요.](./README-ko.md)

# First Steps

This document will guide you through the core of the Avnoi framework. We'll be building a basic CRUD application to get you acquainted with the framework!

### Language

While most examples here are in Java, you can implement this using Kotlin as well.

### Prerequisites

**IDE**: `We recommend using IntelliJ IDEA as your IDE.`  
**Build** `System: Choose either Gradle or Maven for your project's build system.`

<br/>

### Creating a Project
<img width="500" alt="image" src="https://github.com/user-attachments/assets/443d28cf-eb7f-41b3-b6cc-16759f094353">


<br></br>

The basic structure of the project will be as follows:
```
src
├─ AppController.java
├─ AppModule.java
├─ AppService.java
└─ Main.java
```

<br></br>

Here's a brief description of these core files:

|File Name|Description|
|:---:|:---|
|`AppController.java`|Contains a basic controller with a single route.|
|`AppModule.java`|Contains the root module for the application.|
|`AppService.java`|Contains a basic service with a single method.|
|`Main.java`|The entry point of the application. This is where you'll create an instance of an Avnoi application using the core Avnoi class.|

> Don't worry if you don't fully grasp concepts like controllers and services yet. We'll delve deeper into them in subsequent chapters!

<br><br/>

Let's start by creating the Main.java file. This file will house the main method, the entry point of our application.

```java
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


You use the Avnoi class to create an instance of an Avnoi application. This class provides several static methods that you can use to create an application instance. The run method used above starts the Avnoi application.

This code will set up a listener that waits for inbound HTTP requests.

The Avnoi framework is structured so that each module has its own dedicated directory.

<br><br/>

### Running the Application

If you are using IntelliJ IDEA, you can run the application by clicking the green triangle button in the top right corner.
