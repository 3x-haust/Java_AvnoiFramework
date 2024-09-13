## First Steps

This document will guide you through the core fundamentals of the Avnoi framework. Let's dive in and create a basic CRUD application!

### Prerequisites

**IDE**: `IntelliJ IDEA is the recommended IDE for development.`  
**Build System**: `Choose either Gradle or Maven as your project's build system.`

#### Installing Dependencies

**Gradle**  
Add the following to your `build.gradle` file under the `dependencies` block:

```gradle
dependencies {
    implementation group: 'io.github.3x-haust', name: 'avnoi-framework', version: '0.1.5'
    // ... other dependencies
}
```

**Maven**  
Add the following to your `pom.xml` file under the `<dependencies>` tag:

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

The basic structure of the project will be as follows:

```
src
├─ AppController.java
├─ AppModule.java
├─ AppService.java
└─ Main.java
```
> **Tip**
>
> You can find the above files [here](https://github.com/3x-haust/Java_AvnoiFramework/tree/main/src/main/java/io/github/_3xhaust/exmaple/initial).

<br></br>

A brief explanation of the core files is as follows:

|Filename|Description|
|:---:|:---|
|`AppController.java`|Contains a basic controller with a single route.|
|`AppModule.java`|Houses the root module of the application.|
|`AppService.java`|Contains a basic service with a single method.|
|`Main.java`|The entry point of your application. It uses the `Avnoi` class to create an Avnoi application instance.|

> Don't worry if you don't understand controllers, services, etc., for now. We will explain them in detail in the following chapters!

<br><br/>

Let's start by simply creating a Main.java file. This file contains the Main method that starts the application.

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


We use the `Avnoi` class to create an Avnoi application instance. The class offers several static methods for creating the application instance, including the `run` method used above to start the Avnoi application.

The code in the example above allows you to start a listener that waits for inbound HTTP requests.

The Avnoi framework is built in such a way that each module has its own dedicated directory.

<br><br/>

### Running the Application

If you are using the IntelliJ IDEA IDE, you can run the application by clicking the green triangle button in the upper right corner.

<br><br/>
<br><br/>

# Controllers

Controllers are responsible for handling incoming **requests** and returning **responses** to the client.

<img width="600" alt="image" src="https://github.com/user-attachments/assets/8a6d3c0a-0957-4587-824b-22495662578a">

## Routing

In the example below, we'll use the `@Controller()` annotation, which is required to define a basic controller. By specifying a path in the `@Controller()` annotation, we can easily group related routes, minimizing code repetition. For example, you might group the routes that manage interaction with the Customers entity under the `/users` route. In this case, you can specify the path by putting the value `users` in the `@Controller()` annotation without having to repeatedly include it in each route path.

```java
// UsersController.java
package <package>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("users")
public class UsersController {
    @Get
    public String findAll() {
        return "This action returns all users";
    }
}
```

<details>
<summary>Kotlin</summary>

```kt
// UsersController.kt
package <package>

import io.github._3xhaust.annotations.Controller
import io.github._3xhaust.annotations.Inject
import io.github._3xhaust.annotations.types.Get

@Controller("users")
class UsersController {

    @Get
    fun findAll(): String {
        return "This action returns all users";
    }
}
```
</details>

You can use the HTTP request method annotation `@Get()` above the `findAll()` method to create a handler for a specific endpoint for the HTTP request. Here, the endpoint refers to the HTTP request method (GET in the above case) and the route path.  
So how is the route path determined? The route path is determined by combining the path defined in the controller with the path defined in the method's annotation. We have defined that all routes within `UsersController` should use a path starting with the string `users`, and we haven't provided any information about the path in the annotation. Thus, Nest will map this handler to the `GET /users` request. In other words, the path consists of the path defined in the controller and the path defined in the method annotation. For example, if the controller has a path defined as `users` and the method is defined as `Get("profile")`, this would map to the `GET /users/profile` request.

### Resources

We've now defined an endpoint for retrieving 'users' resources using a **GET** request. Typically, you'll also want to provide an endpoint for creating new data. Let's go ahead and create a **POST** handler.

```java
// UsersController.java
package <package>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("users")
public class UsersController {
    @Get
    public String findAll() {
        return "This action returns all users";
    }
    
    @Post
    public String create() {
        return "This action adds a new user";
    }
}
```

<details>
<summary>Kotlin</summary>

```kt
// UsersController.kt
package <package>

import io.github._3xhaust.annotations.Controller
import io.github._3xhaust.annotations.Inject
import io.github._3xhaust.annotations.types.Get

@Controller("users")
class UsersController {

    @Get
    fun findAll(): String {
        return "This action returns all users";
    }

    @Post
    fun create(): string {
        return "This action adds a new user";
    }
}
```
</details>

In addition to `@Post`, Avnoi provides annotations for all standard HTTP methods: `@Get()`, `@Post()`, `@Put()`, `@Delete()`, `@Patch()`, `@Options()`, `@Header()`. Additionally, if you want to define an endpoint for all methods, you can use `@All()`.

### Route Wildcards

Pattern-based routing is also supported. For example, an asterisk (`*`) is used as a wildcard that matches any combination of characters.

```java
@Get("ab*cd")
public String findAll() {
    return "This route uses a wildcard";
}
```

<details>
<summary>Kotlin</summary>

```kt
@Get("ab*cd")
fun findAll(): String {
    return "This route uses a wildcard";
}
```
</details>

The route path `'ab*cd'` would match `abcd`, `ab_cd`, `ab1234cd`, and so on. It would match any string that begins with `ab` and ends with `cd`.

### Status Codes

As mentioned earlier, the **status code** for every response defaults to **200**, except for **POST** requests, which default to **201**. You can change this with the `@HttpCode(...)` decorator at the handler level.

```java
@Post
@HttpCode(204)
public String create() {
    return "This action adds a new user";
}
```

<details>
<summary>Kotlin</summary>

```kt
@Post
@HttpCode(204)
fun create(): String {
    return "This action adds a new user"
}
```
</details>

### Getting Up and Running

With all of our controllers defined, the framework still doesn't know that the `UsersController` exists, so no instance of the class is created.

Controllers should be associated with a module, so you need to add them to the `controllers` array within the `@Module()` annotation. Since we haven't defined any modules other than `AppModule` yet, let's use this module to make the `UsersController` known to the framework.

```java
//AppModule.java
package <package>;

import io.github._3xhaust.annotations.Module;

@Module(
    controllers = {UsersController.class}
)
public class AppModule {}
```

<details>
<summary>Kotlin</summary>

```kt
package <package>

import io.github._3xhaust.annotations.Module


@Module(
    controllers = [AppController::class]
)
class AppModule {}
```
</details>

We used the `@Module()` annotation to decorate the module class with metadata. Now the framework can easily determine which controller it should mount.
