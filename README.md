This is the English version of the README.  
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

<br><br/>
<br><br/>

# Controllers
Controllers are responsible for handling incoming **requests** and returning **responses** to the client.

<img width="600" alt="image" src="https://github.com/user-attachments/assets/8a6d3c0a-0957-4587-824b-22495662578a">

## Routing

In the following example, we'll use the `@Controller()` decorator which is required when defining a basic controller. By specifying the route path in the `@Controller()` decorator, you can easily group related routes and minimize repetitive code. For instance, you might group routes that manage interactions with the customer entity under the `/users` route. In this case, you can simply put the value `users` in the `@Controller()` decorator to specify the path without having to repeat it in each route path.

```java
// UsersController.java
package <package>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("uesrs")
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

The HTTP request method decorator `@Get()` above the `findAll()` method lets us create a handler for a particular endpoint in our application. An endpoint refers to the combination of the HTTP request method (GET in the above case) and the route path.
So how is the route path determined? It is determined by combining the path defined in the controller with the path defined in the method's decorator. We have defined all routes within the `UsersController` to use a route path starting with the string `users` and haven't provided any path information in the decorator. Therefore, Nest will map this handler to `GET /users` requests. In other words, the route path consists of the path defined in the controller and the path defined in the method decorator. For example, if the controller has a defined path of `users` and the method has `Get("profile")` defined, this will be mapped to `GET /users/profile` requests. 

### Resources

Earlier we defined an endpoint to fetch the `users` resource with **GET**.  Typically, you would also want to provide an endpoint for creating new data.  Let's create a **POST** handler for that:

```java
// UsersController.java
package <package>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("uesrs")
public class UsersController {
    @Get
    public String findAll() {
        return "This will return all users";
    }
    
    @Post
    public String create() {
        return "This will create a new user";
    }
}
```

<details>
<summary>Kotlin</summary>

```kt
// UsersController.kt
package io.github._3xhaust

import io.github._3xhaust.annotations.Controller
import io.github._3xhaust.annotations.Inject
import io.github._3xhaust.annotations.types.Get

@Controller("users")
class UsersController {

    @Get
    fun findAll(): String {
        return "This will return all users";
    }

    @Post
    fun create(): String {
        return "This will create a new user";
    }
}
```
</details>

Avnoi provides annotations for all standard HTTP methods in addition to `@Post`:  `@Get()`, `@Post()`, `@Put()`, `@Delete()`, `@Patch()`, `@Options()`, `@Header()`. You can also use `@All()` when you want to define the endpoint for all methods.

### Route Wildcards

You can also use pattern-based routing.  For example, an asterisk (`*`) can be used as a wildcard to match any combination of characters.

```java
@Get("ab*cd")
public String findAll() {
    return "This route uses wildcards";
}
```

<details>
<summary>Kotlin</summary>

```kt
@Get("ab*cd")
fun findAll(): String {
    return "This route uses wildcards"
}
```
</details>

The route path `'ab*cd'` would match `abcd`, `ab_cd`, `ab1234cd`, and so on. It would match any string that starts with `ab` and ends with `cd`.

### Status Codes

As mentioned above, the **status code** of all responses defaults to **200**, except for POST requests which return **201**.  You can change the status code of the response by annotating your handler with `@HttpCode(...)`.

```java
@Post
@HttpCode(204)
public String create() {
    return "This will create a new user";
}
```

<details>
<summary>Kotlin</summary>

```kt
@Post
@HttpCode(204)
fun create(): String {
    return "This will create a new user"
}
```
</details>
