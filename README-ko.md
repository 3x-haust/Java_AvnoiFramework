# 첫번째 단계

이 글에서는 Avnoi 프레임워크의 핵심을 배우게 됩니다. 기본적인 CRUD 어플리케이션을 만들어보며 Avnoi 프레임워크와 친숙해져보겠습니다!

### 언어

여기서 제공하는 대부분의 예시는 자바를 사용하지만, 코틀린을 사용해서 구현할 수도 있습니다.

### 사전 준비

**IDE**: `IDE로 인텔리제이를 사용하여 개발하는 것을 추천합니다.`  
**Build System**: `프로젝트 빌드 시스템으로 Gradle 또는 Maven 중 하나를 선택합니다.`

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
|`Main.java`|시작 파일(entry file). 핵심 함수인 `Avnoi`를 사용하여 Avnoi 어플리케이션 인스턴스를 만듭니다.|

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
