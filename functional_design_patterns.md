# Functional Design Patterns

Scott Wlaschin

## 함수형 프로그래밍의 기본 원리

- 함수는 값이다 (Functions are things)
- 함수는 조합 가능하다
- 타입은 클래스가 아니다

### 함수는 값이다 (Functions are things)

- 사과를 넣으면 바나나가 나온다 (apple -> banana)

```f#
let z = 1

let add x y = x + y
```

- z, add는 값은 성질의 것이다
- 함수는 리턴 값으로 쓸 수 있다

```f#
let add x = (fun y -> x + y)
```

- 함수는 인자로 받을 수 있다. (Function as parameter)

```f#
let useFn f = (f 1) + 2
```

- 함수를 인자로 받아 내부 동작을 바꿀 수 있다. 복잡한 시스템도 이런 식으로 쓸 수 있다.

```f#
let transformInt f x = (f x) + 1
```

### 함수는 조합 가능하다

- apple -> banana 함수와 banana -> cherry 함수를 조합하면 apple -> cherry 함수를 만들 수
  있다.
- 저수준의 동작을 조합해 서비스를 만들로 서비스를 조합해 유즈케이스를 만들고 유즈케이스를 조합해 웹 어플리케이션
  을 만든다

### 타입은 클래스가 아니다

- 타입은 셑의 이름이다
- 함수는 입력 셑을 받아서 결과 셑을 출력한다
- 가능한 숫자를 입력 받아 가능한 문자를 출력한다
- 사람 이름을 출력 받아 가능한 과일을 출력한다
- 타입도 조합이 가능하다
- AND 타입은 모든 타입을 다 가지고 있는 새로운 타입이다

```f#
type FruitSalad = {
  Apple: AppleVariety
  Banana: BananaVariety
  Cherry: CherryVariety
}
```

- OR 타입은 여러 타입 중 하나를 선택할 수 있는 타입이다

```f#
type Snack = {
  | Apple of AppleVariety
  | Banana of BananaVariety
  | Cherry of CherryVariety
}
```

- 예제

```java
interface IPaymentMethod {
  ...
}

class Cash implements IPaymentMethod {
  ...
}

class Check implements IPaymentMethod {
  public Check(int checkNo) { ... }
}

class Card implements IPaymentMethod {
  public Card(String cardType, String cardNo) { ... }
}
```

```f#
type CheckNumber = int
type CardNumber = string
type CardType = Visa | Mastercard
type CreditCardInfo = CardType * CardNumber

type PaymentMethod =
  | Cash
  | Check of CheckNumber
  | Card of CreditCardInfo

type PaymentAmount = decimal
type Currency = EUR | USD

type Payment = {
  Amount: PaymentMethod
  Currency: Currency
  Method: PaymentMethod
}
```

## 디자인 원리

- 0으로 나눌 가능성이 있는 함수에서 에러 처리는 0을 허용하지 않는 입력 타입을 쓰거나 옵셔널 한 출력 값을
  리턴하는 것이다. 예외를 던지면 안된다.
