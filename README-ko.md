이 README는 한국어 버전입니다.  
[Click here for the English version.](./README.md)

# 첫번째 단계

이 글에서는 Avnoi 프레임워크의 핵심을 배우게 됩니다. 기본적인 CRUD 어플리케이션을 만들어보며 Avnoi 프레임워크와 친숙해져보겠습니다!

### 언어

여기서 제공하는 대부분의 예시는 자바를 사용하지만, 코틀린을 사용해서 구현할 수도 있습니다.

### 사전 준비

**IDE**: `IDE로 인텔리제이를 사용하여 개발하는 것을 추천합니다.`  
**Build System**: `프로젝트 빌드 시스템으로 Gradle 또는 Maven 중 하나를 선택합니다.`

#### 의존성 추가

**Gradle**  
`build.gradle` 파일의 `dependencies` 블록 안에 다음 내용을 추가합니다.
```gradle
dependencies {
    implementation group: 'io.github.3x-haust', name: 'avnoi-framework', version: '0.1.5'
    // ... 기타 의존성
}
```

**Maven**  
`pom.xml` 파일의 `<dependencies>` 태그 안에 다음 내용을 추가합니다.
```xml
<dependency>
    <groupId>io.github.3x-haust</groupId>
    <artifactId>avnoi-framework</artifactId>
    <version>0.1.5</version>
</dependency>
```

<br/>

### 프로젝트 생성

<img width="500" alt="image" src="https://github.com/user-attachments/assets/443d28cf-eb7f-41b3-b6cc-16759f094353">

<br></br>

프로젝트의 기본적인 구조는 아래와 같습니다.
```
src
├─ AppController.java
├─ AppModule.java
├─ AppService.java
└─ Main.java
```

<br></br>

위 핵심 파일들을 간단하게 설명하면 아래와 같습니다.

|파일명|설명|
|:---:|:---|
|`AppController.java`|라우트 하나만 있는 기본적인 컨트롤러가 있는 파일|
|`AppModule.java`|어플리케이션의 루트 모듈이 있는 파일|
|`AppService.java`|메서드 하나만 있는 기본적인 서비스가 있는 파일|
|`Main.java`|시작 파일(entry file). 핵심 함수인 `Avnoi`를 사용하여 Avnoi 어플리케이션 인스턴스를 만듭니다.|

> 위에서 서술한 컨트롤러, 서비스 등은 지금은 이해 못하셔도 괜찮습니다. 이후에 나올 챕터들에서 자세한 설명이 나옵니다!

<br><br/>

간단하게 Main.java 파일부터 만들어 보겠습니다. 해당 파일에는 어플리케이션을 시작해주는 Main 메소드가 있습니다.

```Java
//Main.java
package <패키지>;

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
<summary>코틀린</summary>

```kt
//Main.kt
package <패키지>

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


Avnoi 어플리케이션 인스턴스를 만들 땐 `Avnoi` 클래스를 사용합니다. 해당 클래스는 어플리케이션 인스턴스를 만들 때 사용하는 몇몇의 정적 메서드를 제공하는데, 그 중 위에서 쓰인 `run` 메서드는 Avnoi 애플리케이션을 실행합니다

위 예시의 코드를 통해 인바운드 HTTP 요청을 기다리는 리스너를 구동시킬 수 있습니다.

이처럼 Avnoi 프레임워크는 각각의 모듈이 자신의 전용 디렉토리를 갖는 구조로 만들어집니다.

<br><br/>

### 어플리케이션 실행하기

인텔리제이 IDE를 사용한다면 오른쪽 위의 초록색 세모 버튼을 눌러 애플리케이션을 실행시킬 수 있습니다

<br><br/>
<br><br/>

# 컨트롤러
컨트롤러는 들어오는 **요청**을 처리해서 클라이언트에게 **응답**을 반환하는 역할을 합니다.

<img width="600" alt="image" src="https://github.com/user-attachments/assets/8a6d3c0a-0957-4587-824b-22495662578a">

## 라우팅

아래 예제에서는 기본적인 컨트롤러를 정의할 때 필요한 `@Controller()` 어노테이션를 사용해 볼 것입니다. `@Controller()` 어노테이션에 경로를 지정하면 쉽게 관련된 라우트를 묶을 수 있고, 반복되는 코드를 최소화시킬 수 있습니다. 예를 들면, 고객 엔티티와 관련된 상호작용을 관리하는 라우트들을 `/users` 라우트로 묶을 수도 있습니다. 이 경우, `@Controller()` 어노테이션에 `users`라는 값을 넣어서 각각의 라우트 경로에 반복해서 넣을 필요 없이 경로를 지정할 수 있습니다.

```java
// UsersController.java
package <패키지>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("uesrs")
public class UsersController {
    @Get
    public String findAll() {
        return "이 역할은 모든 유저를 반환합니다";
    }
}
```

<details>
<summary>코틀린</summary>

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
        return "이 역할은 모든 유저를 반환합니다";
    }
}
```
</details>

`findAll()` 메서드 위에 있는 HTTP 요청 메서드 어노테이션 `@Get()`를 통해 HTTP 요청의 특정 엔드포엔트에 대한 핸들러를 만들 수 있습니다. 여기서 엔드포인트는 HTTP 요청 메서드(위의 경우 GET)와 라우트 경로를 말합니다.  
그렇다면 라우트 경로는 어떻게 정해질까요? 라우트 경로는 컨트롤러에 정의되어 있는 경로와, 메서드의 어노테이션에 정의되어 있는 경로가 합쳐져서 정해집니다. `UsersController` 내의 모든 라우트에 `users`라는 문자로 시작되는 경로를 사용하도록 정의하였고, 어노테이션에는 경로에 관한 아무 정보도 주지 않았습니다. 따라서 Nest는 해당 핸들러를 `GET /users` 요청과 매핑시킵니다. 즉, 경로는 컨트롤러에 정의된 경로와 메서드 어노테이션에 정의된 경로를 포함합니다. 예를 들어, 컨트롤러에 `users`라고 경로가 정의되어 있고 메서드에 `Get("profile")`이라 정의되어 있다면, 이는 `GET /users/profile` 요청에 매핑됩니다.

### 자원

앞에서 'users' 자원을 가져오는 엔드포인트를 **GET**으로 정의했습니다. 일반적으로는, 새로운 데이터를 만들기 위한 엔드포인트도 제공하고 싶어질 겁니다. 그러면, **POST** 핸들러를 만들어봅시다.

```java
// UsersController.java
package <패키지>;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Inject;
import io.github._3xhaust.annotations.types.Get;

@Controller("uesrs")
public class UsersController {
    @Get
    public String findAll() {
        return "이 작업은 모든 유저를 반환합니다";
    }
    
    @Post
    public String create() {
        return "이 작업은 새로운 유저를 추가합니다";
    }
}
```

<details>
<summary>코틀린</summary>

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
        return "이 작업은 모든 유저를 반환합니다";
    }

    @Post
    fun create(): string {
        return "이 작업은 새로운 유저를 추가합니다";
    }
}
```
</details>

Avnoi는 `@Post` 외에도 모든 표준 HTTP 메서드에 대한 어노테이션를 제공합니다: `@Get()`, `@Post()`, `@Put()`, `@Delete()`, `@Patch()`, `@Options()`, `@Header()`. 또, 모든 메서드에 대한 엔드포인트를 정의하고 싶을 때에는 `@All()`을 쓰면 됩니다.

### 라우트 와일드카드

패턴 기반 라우팅도 지원합니다. 예를 들면, 애스터리크(별표, *)는 모든 문자 조합과 매치되는 와일드카드로 사용됩니다.

```java
@Get("ab*cd")
public String findAll() {
    return "이 경로는 와일드 카드를 사용합니다";
}
```

<details>
<summary>코틀린</summary>

```kt
@Get("ab*cd")
fun findAll(): String {
    return "이 경로는 와일드 카드를 사용합니다";
}
```
</details>

`'ab*cd'` 라우트 경로는 `abcd`, `ab_cd`, `ab1234cd` 등에 매치됩니다. 이는 `ab`로 시작하고 `cd`로 끝나는 모든 문자열에 매치됩니다.

### 상태 코드

위에서 말했던 것처럼, POST 요청이 **201**인 것을 제외하면 모든 응답의 **상태 코드**는 기본적으로 **200**입니다. 응답의 상태 코드를 바꾸려면 핸들러에 `@HttpCode(...)`를 붙이면 됩니다.

```java
@Post
@HttpCode(204)
public String create() {
    return "이 작업은 새로운 유저를 추가합니다";
}
```

<details>
<summary>코틀린</summary>

```kt
@Post
@HttpCode(204)
fun create(): String {
    return "이 작업은 새로운 유저를 추가합니다"
}
```
</details>

### 시작 및 실행

위의 컨트롤러가 모두 정의되어도, 프레임워크는 `UsersController`가 존재한다는 사실을 모르며, 이 때문에 클래스의 인스턴스가 생성되지 않습니다.

컨트롤러는 항상 모듈에 속해아 하므로, `@Module()` 어노테이션 내의 `controllers` 배열에 추가해주어야 합니다. 아직 `AppModule`을 제외하고는 아무 모듈도 정의하지 않았으므로, 이 모듈을 이용해서 프레임워크에게 `UsersController`를 알려줍시다.

```java
//AppModule.java
package <패키지>;

import io.github._3xhaust.annotations.Module;

@Module(
    controllers = {UsersController.class}
)
public class AppModule {}
```

<details>
<summary>코틀린</summary>

```kt
package <패키지>

import io.github._3xhaust.annotations.Module


@Module(
    controllers = [AppController::class]
)
class AppModule {}
```
</details>

`@Module()` 어노테이션을 달아서, 모듈 클래스에 메타데이터를 설정했습니다. 이제, 프레임워크는 어떤 컨트롤러를 마운트해야 하는지 쉽게 알 수 있습니다.


