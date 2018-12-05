# 도메인 논리 패턴과 함수형 프로그래밍

마틴 파울러의 엔터프라이즈 애플리케이션 아키텍처 패턴에 도메인 논리을 위한 패턴 몇 개를 소개 했다.
그 중 저자가 반대의 성격이라고 이야기한 트랜짹션 스크립트와 도메인 모델에 대해 설명하고 함수형 언어를
사용한다면 도메인 논리를 어떻게 구성하면 좋을 지 생각해 봤다.

## 트랜잭션 스크립트

https://martinfowler.com/eaaCatalog/transactionScript.html

```java
ResultSet contracts = db.findContract(contractNumber);
contracts.next();
Money totalRevenue = Money.dollars(contracts.getBigDecimal("revenue"));
MfDate recognitionDate = new MfDate(contracts.getDate("dateSigned"));
String type = contracts.getString("type");

if (type.equals("S")) {
Money[] alloction = totalRevenue.allocate(3);
  db.insertRecognition(contractNumber, alloction[0], recognitionDate);
  db.insertRecognition(contractNumber, alloction[1], recognitionDate.addDays(60));
  db.insertRecognition(contractNumber, alloction[2], recognitionDate.addDays(90));
} else if (type.equals("W")) {
  db.insertRecognition(contractNumber, totalRevenue, recognitionDate);
} else if (type.equals("D")) {
Money[] allocation = totalRevenue.allocate(3);
  db.insertRecognition(contractNumber, allocation[0], recognitionDate);
  db.insertRecognition(contractNumber, allocation[1], recognitionDate.addDays(30));
  db.insertRecognition(contractNumber, allocation[2], recognitionDate.addDays(60));
}
```

## 도메인 모델

https://martinfowler.com/eaaCatalog/domainModel.html

```java

```

## 트랜잭션 스크립트 vs 도메인 모델

저자는 도메인 논리를 선택하는 기준을 도메인 논리의 복잡도와 데이터베이스 연결 난이도에 있다고 한다.

트랜잭션 스크립트를 사용하면 행 데이터 게이트웨이나 테이블 게이트웨이 패턴으로 데이터소스를 사용하는 것이
적합하다고 한다. 물론 데이터 매퍼를 사용하지 못하는 것은 아니다. 하지만 트랜잭션 스크립트에서는 테이블 구조와
가까운 형태로 데이터를 다루기 때문에 데이터 매퍼가 하는 일이 거의 없고 가치가 없다. 트랜잭션 스크립트를
사용하면 테이블 데이터 구조와 비슷한 데이터를 다루기 때문에 데이터베이스 연결 난이도가 낮다. 하지만
트랜잭션 스크립트는 도메인이 복잡해지는 경우 코드의 복잡성을 잘 다루지 못하기 때문에 유지 보수에 어려운
코드를 갖게 된다. 가장 쉬운 예가 많은 조건 문으로 인한 복잡성이다. 객체 지향 언어에서는 이러한 복잡성을
다형성으로 풀 수 있다.

도메인 모델은 객체 지향 패러다임을 이용해 복잡한 도메인을 표현하는 방식의 도메인 논리 패턴이다. 객체 지향은
데이터와 연산으로 구성된 객체로 데이터 분리해서 표현할 수 있다. 객체 지향에서 사용하는 다양한 기술로
복잡한 코드를 잘 다룰 수 있다. 책에 나온 예로는 반복되는 조건문으로 구성된 수익 인식 모델을 전략 패턴으로
반복되는 코드를 줄이고 도메인 논리도 더 명확하게 표현하고 있다. 도메인 모델의 단점으로는 데이터베이스 연결
난이도가 높다는 것이다. 잘 만들어진 도메인 모델은 거대한 데이터를 객체로 쪼개고 객체간 협력으로 문제를
풀어간다. 하지만 이 쪼개진 데이터는 데이터베이스 테이블 구조와 많은 차이를 만들게 된다. 이 차이로 인해
도메인 모델을 데이터베이스와 연결하는 일이 어려워진다. 데이터 매퍼 패턴을 사용해 도메인 모델과 테이블간
차이를 해소한다. 요즘은 데이터 매퍼 라이브러리들이 잘 나와 있어 객체/관계 매핑에 어려움을 많이 해결해
주지만 트랜잭션 스크립트에 비해 상대적으로 데이터베이스 연결 난이도가 높은 것은 사실이다. 책에는 객체
데이터베이스와 같은 기술을 언급했지만 당시 기술의 발달이 덜 된 이유로 많이 사용하지 않는다고 했다.
요즘에 다시 생각해볼 수 있는 것은 MongoDB와 같은 문서 기반 데이터베이스로 도메인 모델을 있는 그대로 영속화
해보는 것은 다시 생각해볼 수 있을 것 같다.

## 함수형 프로그래밍

언어의 유연성과 표현력은 구성 요소의 조합에서 나온다. 객체 지향 패러다임은 데이터와 연산을 갖고 있는 객체를
많이 만들고 객체간 조합으로 유연성과 표현력을 갖는다. 함수형 패러다임은 함수를 조합해 유연성과 표현력을 갖는다.
두 방식은 어느 패러다임이 더 좋다라고 말할 수 없다. 이 문제를 표현 문제(Expression Problem)이라고
부른다. 여기서는 함수형 패러다임을 사용하는 경우 도메인 모델을 어떻게 구성하면 좋을지에 대해 생각해보자.

함수형 패러다임을 사용하게되면 보통 데이터와 연산을 묶어 표현하지 않기 때문에 데이터를 쪼갤 필요는 없다.
따라서 트랜잭션 스크립트에서 테이블 형태와 비슷한 데이터 구조를 다루는 것 처럼 함수형 패러다임에서도
테이블 형태와 비슷한 데이터 구조를 다루는 것이 데이터베이스 연결 난이도를 낮출 수 있는 방법이다.
그럼 트랜잭션 스크립트에서 문제가 되는 도메인 복잡성을 어떻게 처리할 수 있을까?

많은 사례를 생각해 보진 못했지만 이 책에서 나온 트랜잭션 스크립트의 문제인 조건문 반복을 예를 들어보면

정리중...

Scott Wlaschin이 타입 시스템으로 도메인 논리를 표현한 자료
https://www.youtube.com/watch?v=1pSH8kElmM4

Peter Norvig의 객체 지향 언어가 아닌 언어로 Design Pattern을 설명한 자료
https://www.researchgate.net/publication/242609494_Design_patterns_in_dynamic_programming
