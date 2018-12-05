# Domain Modeling Made Functional - Scott Wlaschin

https://www.youtube.com/watch?v=1pSH8kElmM4

시스템에 쓰레기를 넣으면 쓰레기가 나온다. 
F# 타입 시스템으로 도메인 모델링을 해보자.

## 뭐가 잘 못 된 건가?

```f#
type Contact = {
  FirstName: string
  MiddleInitial: string
  LastName: string

  EmailAddress: string
  IsEmailVerified: bool
}
```

- 옵셔널 타입에 대한 정의가 없다
- MiddleInitial 은 옵셔널
- FirstName,LastName,EmailAddress에 대한 제약 조건이 없다
- 이름과 이메일 영역은 함께 변경 되기 때문에 묶어야 한다.
- IsEmailVerified false가 초기값인지 인증 받지 않은 것인지 구분되지 않는다.

## DDD

- 코드와 개발자와 도메인 전문가가 공유 맨탈 모델을 공유한다.
- 같은 용어도 다른 컨텍스트(바운디드 컨텍스트)에서는 다르게 해석된다.
- 바운디드 컨텍스트 안에서 사용하는 용어가 유비쿼터스 언어다.

## 코드 예제

```f#
module CardGame =
  type Suit = Club | Diamond | Spade | Heart

  type Rank = Two | Three | Four | Five | Six | Seven | Eight
              | Nine | Ten | Jack | Queen | King | Ace

  type Card = Suit * Rank

  type Hand = Card list
  type Deck = Card list

  type Player = {Name:string; Hand:Hand}
  type Game = {Deck:Deck; Players:Player list}

  type Deal = Deck -> (Deck * Card)

  type PickupCard = (Hand * Card) -> Hand
```

- module인 CardGame은 바운디드 컨텍스트
- 그 안에 type은 유비쿼터스 언어
- `|` 표시는 하나를 선택
- `*` 표시는 페어
- `->` 함수의 입력과 출력
- 개발자가 아니라도 이해할 수 있다.
- 기술적인 내용이 없다.
- UML 같은 다른 문서는 필요 없다.

```clojure

```

## F# 타입 시스템

- 컴포저블 타입 시스템
- 곱하기(타입1 `x` 타입2) : 타입1, 타입2 조합 가능한 모든 페어
- 더하기(타입1 `+` 타입2) : 타이1, 타입2 둘 중 하나

  ```f#
  type Temp = F of int | C of float
  ```

```f#
type PaymentMethod =
  | Cash
  | Cheque of ChequeNumber
  | Card of CardType * CardNumber
```

- 타입 매칭을 쓸 수 있다.
- 컴파일 타임 체크는 타입 시스템의 장점

## 타입 설계

### 옵셔널 값

- null은 해롭다.
- String 타입에 null을 허용하지 않고 더하기 타입으로 표현할 수 있다.

  ```f#
  type OptionalString =
    | SomString of string
    | Nothing
  ```

- 타입별로 다 만들면 중복이기 때문에 제너릭 타입으로 만든다.

  ```f#
  type Option<'T> =
    | Some of 'T
    | None
  ```

- 첫번째 예제를 바꿔보자.

```
type PersonalName =
  {
  FirstName: string
  MiddleInitial: Option<string> ## string option을 f#에서 제공
  LastName: string
  }
```

### Single choice 타입

- EmailAddress는 그냥 string이 아니다.
- CustomerId는 그냥 int가 아니다.

```f#
type EmailAddress = EmailAddress of string
type PhoneNumber = PhoneNumber of string
```

- EmailAddress, PhoneNumber 둘다 string 타입이지만 다른 의미를 갖기 때문에 Single choice
  타입으로 만들어 주는 것이 좋다. (예를 들어 PhoneNumber 받을 곳에 EmailAddress를 받으면 안된다)
- f#은 변경 불가능한 데이터 타입을 가지고 있기 때문에 EmailAddress 생성자 함수에서 Validation 체크를
  하면 된다.

```f#
let createEmailAddress(s:string) =
  if Regex.IsMatch(s, @"^\S+@\S+\.\S+$")
    then Some (EmailAddress s)
    else None

createEmailAddress:
  string -> EmailAddress option
```

```f#
type String50 = String50 of String

let createString50 (s:string) =
  if s.Length <= 50
   then Some (String50 s)
   else None

createString50:
  string -> String50 option
```

- 첫번째 예제를 다시 변경해 보자.

```f#
type PersonalName = {
  FirstName: String50
  MiddleInitial: StringI option
  LastName: String50 }

type EmailContactInfo = {
  EmailAddress: EmailAddress
  IsEmailVerified: bool }

type Contact = {
  Name: PersonalName
  Email: EmailContactInfo }
```

- 이제 IsEmailVerified 타입을 생각해보자
  - 규칙1 EmailAddress 변경 되면 false가 된다.
  - 규칙2 특별한 서비스로 flag가 설정된다.

- 아래 처럼 VerifiedEmail 타입을 만들면 규칙을 타입으로 표현할 수 있다.

```f#
type VerifiedEmail = VerifiedEmail of EmailAddress

type VerificationService =
  (EmailAddress * VerificationHash) -> VerifiedEmail option

type EmailContactInfo =
  | Unverified of EmailAddress
  | Verified of VerifiedEmail
```
- 최종 버전

```f#
type PersonalName = {
  FirstName: String50
  MiddleInitial: StringI option
  LastName: String50 }

type VerifiedEmail = ...


type EmailContactInfo =
  | Unverified of EmailAddress
  | Verified of VerifiedEmail

type Contact = {
  Name: PersonalName
  Email: EmailContactInfo }
```

### 보너스

- 만약 아래 모델에서 email 또는 postal address 둘 중 하나는 반드시 있어야 한다는 것을 표현하려면?

```f#
type Contact = {
  Name: Name
  Email: EmailContactInfo
  Address: PostalContactInfo
}
```

- 둘다 option 으로 할 수는 없다. 이럴 때는 아래와 같이 표현 할 수 있다.

```f#
type ContactInfo =
  | EmailOnly of EmailContactInfo
  | AddrOnly of PostalContactInfo
  | EmailAndAddr of EmailContactInfo * PostalContactInfo

type Contact = {
  Name: Name
  ContactInfo: ContactInfo
}
```

- 연락 가능한 방법이 하나는 꼭 있어야 한다는 도메인은 아래 처럼 표현 할 수 있다.

```f#
type ContactInfo =
  | Email of EmailContactInfo
  | Addr of PostalContactInfo

type Contact = {
  Name: Name
  PrimaryContactInfo: ContactInfo
  SecondaryContactInfo: ContactInfo option
}
```
