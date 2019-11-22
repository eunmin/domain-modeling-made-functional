# 타입으로 도메인 모델링 하기

첫번째 장에서 공유 멘탈 모델의 중요성에 대해 이야기 했다. 그리고 코드가 공유 모델을 반영해야 한다고 강조하고
개발자는 도메인 모델을 코드로 나타내는 것을 개을리 하면 안된다고 했다. 이상적으로 코드는 문서가 되는 것이
좋다. 이것의 의미는 개발자와 도메인 전문가가 코드를 함께 보면서 설계를 확인할 수 있다는 뜻이다.

F# 타입 시스템은 도메인 모델을 코드로 나타내기에 충분하고 또한 도메인 전문가나 다른 개발자가 읽고 이해할 수
있다. 앞으로 타입이 대부분의 문서를 대체할 수 있다는 것을 알게 될거다. 이것은 굉장한 이점이 있는데 설계가
곧 코드 자체라서 구현과 설계가 달라지지 않는다는 장점이 있다.

## 도메인 모델 다시보기

만들었던 도메인 모델을 다시보자.

```f#
context: Order-Taking

// ----------------------
// Simple types
// ----------------------

// Product codes
data ProductCode = WidgetCode OR GizmoCode
data WidgetCode = string starting with "W" then 4 digits
data GizmoCode = ...
// Order Quantity
data OrderQuantity = UnitQuantity OR KilogramQuantity
data UnitQuantity = ...
data KilogramQuantity = ...

// ----------------------
// Order life cycle
// ----------------------

// ----- unvalidated state -----
data UnvalidatedOrder =
    UnvalidatedCustomerInfo
    AND UnvalidatedShippingAddress
    AND UnvalidatedBillingAddress
    AND list of UnvalidatedOrderLine

data UnvalidatedOrderLine =
    UnvalidatedProductCode
    AND UnvalidatedOrderQuantity

// ----- validated state -----
data ValidatedOrder = ...
data ValidatedOrderLine =  ...

// ----- priced state -----
data PricedOrder = ...
data PricedOrderLine = ...

// ----- output events -----
​data OrderAcknowledgmentSent = ...
​data OrderPlaced = ...
​data BillableOrderPlaced = ...
​
​// ----------------------
​// Workflows
​// ----------------------
​
​workflow "Place Order" =
​    input: UnvalidatedOrder
​    output (on success):
​        OrderAcknowledgmentSent
​        AND OrderPlaced (to send to shipping)
​        AND BillableOrderPlaced (to send to billing)
​    output (on error):
​        InvalidOrder
​
​// etc
```

이번 장의 목표는 이 모델을 코드로 옮기는 것이다.

## 도메인 모델의 패턴을 살펴보기

도메인 모델이 달라도 반복되는 많은 패턴이 있다. 몇가지 전형적인 도메인 패턴을 살펴보고 모델 컴포넌트와
어떻게 연관되는지 알아보자.

- 단순 값. 문자열이나 숫자같은 기본 타입으로 표현할 수 있는 것들이 있다. 하지만 이것은 실제 숫자나 문자열
  이 아니다. 도메인 전문가는 이걸 `int`나 `string`이라고 보지 않고 `OrderId`나 `ProductCode`라고
  생각한다. - 유비쿼터스 언어
- `AND`로 표현되는 값 조합. 관련된 데이터끼리 연결된 그룹이 있다. 종이로 치면 문서나 문서에 있는 부분들
  이다.: 이름, 주소, 주문 등
- `OR`로 표현되는 선택적인 값. 도메인에서 선택할 수 있는 것을 나타낸다. `Order` 또는 `Quote`,
  `UnitQuantity` 또는 `KilogramQuantity`
- 워크플로우. 마지막으로 입력과 출력이 있는 비지니스 프로세스가 있다.

남은 장에서 F# 타입으로 이런 패턴들을 어떻게 표현하는지 알아본다.

## 단순 값을 모델링 하기

도메인 전문가는 `int`나 `string`이라고 보지 않고 `OrderId`나 `ProductCode`라고 생각한다고 앞에서
말했다. `OrderId`나 `ProductCode`는 섞어쓰지 않는 것이 중요하다. 기본 값을 나타내는 래퍼 타입을
만들거다.

- F#에서 한가지 경우만 있는 타입을 아래 처럼 표현할 수 있다.

```f#
type CustomerId =
  | Customer of int
```

- 하지만 더 간단하게 아래 처럼 표현할 수 있다.

```f#
type CustomerId = Customer of int
```

- 레코드 타입 같은 복합 타입과 구분하기 위해 위 타입을 심플 타입이라고 부르자. 이 타입은 `int`나 `string`
같은 기본 값을 포함하고 있다.

- 우리 도메인에서 심플 타입은 아래 처럼 표현할 수 있다.

```f#
type​ WidgetCode = WidgetCode ​of​ ​string​
​type​ UnitQuantity = UnitQuantity ​of​ ​int​
​type​ KilogramQuantity = KilogramQuantity ​of​ decimal
```



## 복합 데이터를 모델링 하기

## 함수로 워크플우를 모델링 하기

## A Question of Identity: 값 객체

## A Question of Identity: 엔티티

## Aggregates

## 모두 합치기

```f#
​namespace​ OrderTaking.Domain

// Product code related​
​type​ WidgetCode = WidgetCode ​of​ ​string​
  ​// constraint: starting with "W" then 4 digits​
​type​ GizmoCode = GizmoCode ​of​ ​string​
  ​// constraint: starting with "G" then 3 digits​
​type​ ProductCode =
  | Widget ​of​ WidgetCode
  | Gizmo ​of​ GizmoCode

​// Order Quantity related​
​type​ UnitQuantity = UnitQuantity ​of​ ​int​
​type​ KilogramQuantity = KilogramQuantity ​of​ decimal
​type​ OrderQuantity =
  | Unit ​of​ UnitQuantity
  | Kilos ​of​ KilogramQuantity

type​ OrderId = Undefined
type​ OrderLineId = Undefined
type​ CustomerId = Undefined

type​ CustomerInfo = Undefined
​type​ ShippingAddress = Undefined
​type​ BillingAddress = Undefined
​type​ Price = Undefined
​type​ BillingAmount = Undefined

​type​ Order = {
  Id : OrderId             ​// id for entity​
  CustomerId : CustomerId  ​// customer reference​
  ShippingAddress : ShippingAddress
  BillingAddress : BillingAddress
  OrderLines : OrderLine ​list​
  AmountToBill : BillingAmount
  }

​and​ OrderLine = {
  Id : OrderLineId  ​// id for entity​
  OrderId : OrderId
  ProductCode : ProductCode
  OrderQuantity : OrderQuantity
  Price : Price
  }

type​ UnvalidatedOrder = {
	  OrderId : ​string​
	  CustomerInfo : ...
	  ShippingAddress : ...
	  ...
	  }

type​ PlaceOrderEvents = {
 	  AcknowledgmentSent : ...
 	  OrderPlaced : ...
 	  BillableOrderPlaced : ...
 	  }

type​ PlaceOrderError =
  | ValidationError ​of​ ValidationError ​list​
  | ...  ​// other errors​

and​ ValidationError = {
	    FieldName : ​string​
	    ErrorDescription : ​string​
	    }

/// The "Place Order" process​
​type​ PlaceOrder =
​ 	  UnvalidatedOrder -> Result<PlaceOrderEvents,PlaceOrderError>
```
## 마무리
