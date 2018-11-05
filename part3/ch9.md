# 구현: Composing a pipeline

```f#
let​ placeOrder unvalidatedOrder =
  unvalidatedOrder
  |> validateOrder
  |> priceOrder
  |> acknowledgeOrder
  |> createEvents
```

- 위 주문 과정은 두단계로 나눠서 설명할 예정, 1 개별 함수의 구현, 2 조합하기
- 조합하기는 2가지 이유로 어려움
  - 어떤 함수는 추가 파라미터가 필요함 (디팬던시)
  - 에러 핸들링을 위한 Result 같은 타입이 input/output 타입의 불일치를 만듬

## Working with Simple Type

- OrderId 타입을 String 타입 값으로 생성하는 create 핼퍼함수와 값을 리턴하는 value 핼퍼함수를 만듬

```f#
module​ Domain =
  ​type​ OrderId = ​private​ OrderId ​of​ ​string​

  ​module​ OrderId =
    ​/// Define a "Smart constructor" for OrderId​
    ​/// string -> OrderId​
    ​let​ create str =
      if​ String.IsNullOrEmpty(str) ​then​
        // use exceptions rather than Result for now​
        failwith ​"OrderId must not be null or empty"​
      ​elif​ str.Length > 50 ​then​
        failwith ​"OrderId must not be more than 50 chars"​
      ​else​
        OrderId str

    ​/// Extract the inner value from an OrderId​
    ​/// OrderId -> string​
    ​let​ value (OrderId str) = ​// unwrap in the parameter!​
      str                     ​// return the inner value’
```

## Using Function Types to Guide the Implementation

- validateOrder 함수

```f#
let​ validateOrder
  checkProductCodeExists ​// dependency​
  checkAddressExists     ​// dependency​
  unvalidatedOrder =     ​// input​
  ...
```

- 함수 시그네처를 따로 정의하고 람다 함수로 리턴하기

```f#
// define a function signature​
type​ MyFunctionSignature = Param1 -> Param2 -> Result
​ 
// define a function that implements that signature​
let​ myFunc: MyFunctionSignature =
  ​fun​ param1 param2 ->
```

- validateOrder 함수 시그네처를 따로 정의 하고 람다 함수로 리턴하기

```f#
let​ validateOrder : ValidateOrder =
  ​fun​ checkProductCodeExists checkAddressExists unvalidatedOrder ->
  //  ^dependency            ^dependency        ^input​
  ...
```

- 아래는 타입 체크의 예, checkProductCodeExists 함수에 ProductCode 타입이어야 하는데 int 타입이
  넘어감

```f#
let​ validateOrder : ValidateOrder =
  ​fun​ checkProductCodeExists checkAddressExists unvalidatedOrder ->
    if​ checkProductCodeExists 42 ​then​
    //         compiler error ^​
    ​// This expression was expected to have type ProductCode​
    // but here has type int​
    ...
    ...
```

## Implementation the Validation Step

- 원래 함수 시그네처

```f#
type​ CheckAddressExists =
  UnvalidatedAddress -> AsyncResult<CheckedAddress,AddressValidationError>
​ 
  ​type​ ValidateOrder =
    CheckProductCodeExists    ​// dependency​
      -> CheckAddressExists   ​// AsyncResult dependency​
      -> UnvalidatedOrder     ​// input​
      -> AsyncResult<ValidatedOrder,ValidationError ​list​>  ​// output​
```

- 일단 이 장에서는 effect(Result)를 제거하기 위해 아래 처럼 시그네처를 줄임

```f#
type​ CheckAddressExists =
  UnvalidatedAddress -> CheckedAddress

  type​ ValidateOrder =
    CheckProductCodeExists    ​// dependency​
      -> CheckAddressExists   ​// dependency​
      -> UnvalidatedOrder     ​// input​
      -> ValidatedOrder       ​// output​
```

- 이제 validateOrder 함수를 구현해보자.

```f#
let​ validateOrder : ValidateOrder =
  fun​ checkProductCodeExists checkAddressExists unvalidatedOrder ->
​ 
    ​let​ orderId =
      unvalidatedOrder.OrderId
      |> OrderId.create

    ​let​ customerInfo =
      unvalidatedOrder.CustomerInfo
      |> toCustomerInfo   ​// helper function​

    ​let​ shippingAddress =
      unvalidatedOrder.ShippingAddress
      |> toAddress        ​// helper function​
​ 
    ​// and so on, for each property of the unvalidatedOrder​
​ 
    ​// when all the fields are ready, use them to​
    ​// create and return a new "ValidatedOrder" record​
    {
      OrderId = orderId
      CustomerInfo = customerInfo
      ShippingAddress = shippingAddress
      BillingAddress = ...
      Lines = ...
    }
```

- 서브 컴포넌트인 toCustomerInfo 함수는 아래 처럼 만들 수 있다.

```f#
let​ toCustomerInfo (customer:UnvalidatedCustomerInfo) : CustomerInfo =
  // create the various CustomerInfo properties​
  ​// and throw exceptions if invalid​
  let​ firstName = customer.FirstName |> String50.create
  ​let​ lastName = customer.LastName |> String50.create
  ​let​ emailAddress = customer.EmailAddress |> EmailAddress.create

  ​// create a PersonalName​
  ​let​ name : PersonalName = {
    FirstName = firstName
    LastName = lastName
  }

  // create a CustomerInfo​
  ​let​ customerInfo : CustomerInfo = {
    Name = name
    EmailAddress = emailAddress
  }
  // ... and return it​
  customerInfo
```
- toAddress 함수는 checkAddressExists 디팬던시를 사용하기 때문에 조금 복잡하다

```f#
let​ toAddress (checkAddressExists:CheckAddressExists) unvalidatedAddress =
  ​// call the remote service​
  ​let​ checkedAddress = checkAddressExists unvalidatedAddress
  ​// extract the inner value using pattern matching​
  ​let​ (CheckedAddress checkedAddress) = checkedAddress

  ​let​ addressLine1 =
    checkedAddress.AddressLine1 |> String50.create
  ​let​ addressLine2 =
    checkedAddress.AddressLine2 |> String50.createOption
  ​let​ addressLine3 =
    checkedAddress.AddressLine3 |> String50.createOption
  ​let​ addressLine4 =
    checkedAddress.AddressLine4 |> String50.createOption
  ​let​ city =
    checkedAddress.City |> String50.create
  ​let​ zipCode =
    checkedAddress.ZipCode |> ZipCode.create
  ​// create the address​
  ​let​ address : Address = {
    AddressLine1 = addressLine1
    AddressLine2 = addressLine2
    AddressLine3 = addressLine3
    AddressLine4 = addressLine4
    City = city
    ZipCode = zipCode
  }
  // return the address​
  address
```

- CheckAddressExists 디팬던시는 toAddress를 사용하는 validateOrder에 가지고 있기 때문에
  전달해줄 수 있다.

```f#
let​ validateOrder : ValidateOrder =
  ​fun​ checkProductCodeExists checkAddressExists unvalidatedOrder ->
    ​let​ orderId = ...
    ​let​ customerInfo = ...
    ​let​ shippingAddress =
      unvalidatedOrder.ShippingAddress
      |> toAddress checkAddressExists ​// new parameter​
      ​ 
    ...
```

- 위에 보면 toAddress는 인자가 두개 인데 하나만 전달해 주는 이유는 나머지 인자는 파이프닝으로 전달되기
  때문이다.

### Creating the Order Lines

- 먼저 UnvalidatedOrderLine 하나를 ValidatedOrderLine으로 변환하는 함수를 만들어보자

```f#
​let​ toValidatedOrderLine checkProductCodeExists
  (unvalidatedOrderLine:UnvalidatedOrderLine) =
    let​ orderLineId =
      unvalidatedOrderLine.OrderLineId
      |> OrderLineId.create
    ​let​ productCode =
      unvalidatedOrderLine.ProductCode
      |> toProductCode checkProductCodeExists ​// helper function​
    ​let​ quantity =
      unvalidatedOrderLine.Quantity
      |> toOrderQuantity productCode  ​// helper function​
    ​let​ validatedOrderLine = {
      OrderLineId = orderLineId
      ProductCode = productCode
      Quantity = quantity
    }
    validatedOrderLine
```

- 이제 리스트 형식의 OrderLine을 변환하는 코드를 만들어보자.

```f#
​let​ validateOrder : ValidateOrder =
  fun​ checkProductCodeExists checkAddressExists unvalidatedOrder ->

    ​let​ orderId = ...
    ​let​ customerInfo = ...
    ​let​ shippingAddress = ...
    ​ 
    ​let​ orderLines =
      unvalidatedOrder.Lines
      ​// convert each line using `toValidatedOrderLine`​
      |> List.map (toValidatedOrderLine checkProductCodeExists)
      ...
```

- 다음은 toOrderQuantity 헬퍼 함수다.

```f#
let​ toOrderQuantity productCode quantity =
  match​ productCode ​with​
    | Widget _ ->
      quantity
      |> ​int​                  ​// convert decimal to int​
      |> UnitQuantity.create  ​// to UnitQuantity​
      |> OrderQuantity.Unit   ​// lift to OrderQuantity type​
    | Gizmo _ ->
      quantity
      |> KilogramQuantity.create  ​// to KilogramQuantity​
      |> OrderQuantity.Kilogram   ​// lift to OrderQuantity type​’
```

- productCode가 Widget 형식 또는 Gizmo 형식에 따라 분기가 되어 있다. 리턴 타입을 맞추기 위해서
  OrderQuantity 타입으로 마지막에 맞춰줬다.

- 다음은 toProductCode 헬퍼 함수다.

```f#
let​ toProductCode (checkProductCodeExists:CheckProductCodeExists) productCode =
  productCode
  |> ProductCode.create
  |> checkProductCodeExists
  ​// returns a bool :(​
```

- 이 함수는 ProductCode를 리턴해야하는데 checkProductCodeExists로 끝나기 때문에 bool 값을 리턴하는
  문제가 있다.

### Creating Function Adapters

- 앞에 toProductCode에서 bool을 리턴하는 문제를 checkProductCodeExists 스펙을 변경하지 않고
  adapter 함수를 만들어 해결할 수 있다.

```f#
let​ convertToPassthru checkProductCodeExists productCode =
  if​ checkProductCodeExists productCode ​then​
    productCode
  ​else​
    failwith ​"Invalid Product Code"​
```

- 이 함수는 더 일반화 할 수 있다.

```f#
​let​ predicateToPassthru errorMsg f x =
  ​if​ f x ​then​
    x
  ​else​
    failwith errorMsg

val​ predicateToPassthru : errorMsg:​string​ -> f:(​'​a -> ​bool​) -> x:​'​a -> ​'​a
```

- 이 함수를 적용해 toProductCode 함수를 고쳐보자

```f#
​let​ toProductCode (checkProductCodeExists:CheckProductCodeExists) productCode =
  // create a local ProductCode -> ProductCode function​
  // suitable for using in a pipeline​
  ​let​ checkProduct productCode =
    ​let​ errorMsg = sprintf ​"Invalid: %A"​ productCode
      predicateToPassthru errorMsg checkProductCodeExists productCode
​ 
  ​// assemble the pipeline​
  productCode
  |> ProductCode.create
  |> checkProduct
```

## Implementing the Rest of the Steps

- validateOrder는 위에서 해봤고 이제 priceOrder를 만들어보자. 역시 원래 버전에서 effect를
  제거한 버전으로 타입을 바꾸자.

```f#
type PriceOrder =
    GetPricingFunction  // dependency
     -> ValidatedOrder  // input
     -> Result<PricedOrder, PricingError>  // output
```

```f#
type GetProductPrice = ProductCode -> Price
type PriceOrder =
    GetPricingFunction  // dependency
     -> ValidatedOrder  // input
     -> PricedOrder     // output
```

- 구현은 아래와 같다.

```f#
​let​ priceOrder : PriceOrder =
​ 	  ​fun​ getProductPrice validatedOrder ->
​ 	    ​let​ lines =
​ 	      validatedOrder.Lines
​ 	      |> List.map (toPricedOrderLine getProductPrice)
​ 	    ​let​ amountToBill =
​ 	      lines
​ 	      ​// get each line price​
​ 	      |> List.map (​fun​ line -> line.LinePrice)
​ 	      ​// add them together as a BillingAmount​
​ 	      |> BillingAmount.sumPrices
      ​let​ pricedOrder : PricedOrder = {
​ 	      OrderId  = validatedOrder.OrderId
​ 	      CustomerInfo = validatedOrder.CustomerInfo
​ 	      ShippingAddress = validatedOrder.ShippingAddress
​ 	      BillingAddress = validatedOrder.BillingAddress
​ 	      Lines = lines
​ 	      AmountToBill = amountToBill
​ 	      }
​ 	    pricedOrder
```

- `BillingAmount.sumPrices` 는 다음과 같다.

```f#
/// Sum a list of prices to make a billing amount​
/// Raise exception if total is out of bounds​
​let​ sumPrices prices =
  ​let​ total = prices |> List.map Price.value |> List.sum
  create total
```

- `toPricedOrderLine` 함수는 다음과 같다.

```f#
/// Transform a ValidatedOrderLine to a PricedOrderLine​
let​ toPricedOrderLine getProductPrice (line:ValidatedOrderLine) : PricedOrderLine =
  let​ qty = line.Quantity |> OrderQuantity.value
  let​ price = line.ProductCode |> getProductPrice
  let​ linePrice = price |> Price.multiply qty
  {
    OrderLineId = line.OrderLineId
    ProductCode = line.ProductCode
    Quantity = line.Quantity
    LinePrice = linePrice
  }
```

- pricing 단계는 끝났고 다음은 acknowledgment 단계를 구현해보자.

### Implementing the Acknowledgment Step

- 아래는 effect를 제거한 acknowledgment 단계다.

```f#
type​ HtmlString = HtmlString ​of​ ​string​
type​ CreateOrderAcknowledgmentLetter =
  PricedOrder -> HtmlString

  type​ OrderAcknowledgment = {
    EmailAddress : EmailAddress
    Letter : HtmlString
  }
  type​ SendResult = Sent | NotSent
  type​ SendOrderAcknowledgment =
    OrderAcknowledgment -> SendResult

  ​type​ AcknowledgeOrder =
    CreateOrderAcknowledgmentLetter     ​// dependency​
      -> SendOrderAcknowledgment        ​// dependency​
      -> PricedOrder                    ​// input​
      -> OrderAcknowledgmentSent option ​// output​
```

- 아래는 구현 부분이다.

```f#
let​ acknowledgeOrder : AcknowledgeOrder =
  ​fun​ createAcknowledgmentLetter sendAcknowledgment pricedOrder ->
    ​let​ letter = createAcknowledgmentLetter pricedOrder
    ​let​ acknowledgment = {
      EmailAddress = pricedOrder.CustomerInfo.EmailAddress
      Letter = letter
    }

    ​// if the acknowledgment was successfully sent,​
    ​// return the corresponding event, else return None​
    ​match​ sendAcknowledgment acknowledgment ​with​
    | Sent ->
      ​let​ ​event​ = {
        OrderId = pricedOrder.OrderId
        EmailAddress = pricedOrder.CustomerInfo.EmailAddress
      }
      Some ​event​
    | NotSent ->
      None
```

- 특별한 부분은 없다.

## Creating the Event

```f#
/// Event to send to shipping context​
type​ OrderPlaced = PricedOrder

​/// Event to send to billing context​
/// Will only be created if the AmountToBill is not zero​
type​ BillableOrderPlaced = {
  OrderId : OrderId
  BillingAddress: Address
  AmountToBill : BillingAmount
}
​
type​ PlaceOrderEvent =
    | OrderPlaced ​of​ OrderPlaced
    | BillableOrderPlaced ​of​ BillableOrderPlaced
    | AcknowledgmentSent  ​of​ OrderAcknowledgmentSent

​type​ CreateEvents =
  PricedOrder                            ​// input​
    -> OrderAcknowledgmentSent option    ​// input (event from previous step)​
    -> PlaceOrderEvent ​list​              ​// output​
```

```f#
// PricedOrder -> BillableOrderPlaced option​
let​ createBillingEvent (placedOrder:PricedOrder) : BillableOrderPlaced option =
  ​let​ billingAmount = placedOrder.AmountToBill |> BillingAmount.value
    if​ billingAmount > 0M ​then​
      ​let​ order = {
        OrderId = placedOrder.OrderId
        BillingAddress = placedOrder.BillingAddress
        AmountToBill = placedOrder.AmountToBill
      }
      Some order
    ​else​
      None
```

```f#
  ​let​ createEvents : CreateEvents =
    ​fun​ pricedOrder acknowledgmentEventOpt ->
      ​let​ event1 =
        pricedOrder
        ​// convert to common choice type​
        |> PlaceOrderEvent.OrderPlaced
      ​let​ event2Opt =
        acknowledgmentEventOpt
        ​// convert to common choice type​
        |> Option.map PlaceOrderEvent.AcknowledgmentSent
      ​let​ event3Opt =
        pricedOrder
        |> createBillingEvent
        ​// convert to common choice type​
        |> Option.map PlaceOrderEvent.BillableOrderPlaced

      ​// return all the events how?​
​ 	    ...
```

- 일부 이벤트는 결과가 Optional 타입이기 때문아 아래 helper 함수로 리턴 타입을 맞춰준다.

```f#
/// convert an Option into a List​
let​ listOfOption opt =
  match​ opt ​with​
    | Some x -> [x]
    | None -> []
```

```f#
let​ createEvents : CreateEvents =
  ​fun​ pricedOrder acknowledgmentEventOpt ->
    ​let​ events1 =
      pricedOrder
      ​// convert to common choice type​
      |> PlaceOrderEvent.OrderPlaced
      ​// convert to list​
      |> List.singleton
    ​let​ events2 =
      acknowledgmentEventOpt
      ​// convert to common choice type​
      |> Option.map PlaceOrderEvent.AcknowledgmentSent
      ​// convert to list​
      |> listOfOption
    ​let​ events3 =
      pricedOrder
      |> createBillingEvent
      ​// convert to common choice type​
      |> Option.map PlaceOrderEvent.BillableOrderPlaced
      ​// convert to list​
      |> listOfOption

    ​// return all the events​
    [
      ​yield​! events1
      ​yield​! events2
      ​yield​! events3
    ]
```

## Composing the Pipeline Steps Together

- 워크플로우는 아래 처럼 생겼는데 문제가 있다.

```f#
let​ placeOrder : PlaceOrderWorkflow =
  ​fun​ unvalidatedOrder ->
    unvalidatedOrder
    |> validateOrder
    |> priceOrder
    |> acknowledgeOrder
    |> createEvents
```

- validateOrder 함수는 인자로 디팬던시 함수 CheckProductCodeExists, CheckAddressExists와
  입력값 UnvalidatedOrder을 받아야하지만 이전 플로우에서는 unvalidatedOrder만 준다.
- 마찬가지로 priceOrder 함수도 인자로 디팬던시 함수 getProductPrice와 입력값 validatedOrder가
  필요하지만 이전 플로우인 validateOrder 함수에서는 validatedOrder만 리턴하기 때문에 입력값이 달라
  Composing할 수 없다.
- 이 문제를 푸는 일반적인 방법은 모나드를 사용하는 것이지만 여기서는 partial로 간단하게 해결해보자.

- 디팬던시를 이미 포함하고 있는 validateOrder 함수를 아래 처럼 만들어보자.

```f#
let​ validateOrderWithDependenciesBakedIn =
  validateOrder checkProductCodeExists checkAddressExists

  // new function signature after partial application:​
  // UnvalidatedOrder -> ValidatedOrder​
```

- F#은 다행이 쉐도윙이라고 부르는 방법으로 `validateOrder` 함수이름을 그대로 쓸 수 있다.

```f#
let​ validateOrder =
  validateOrder checkProductCodeExists checkAddressExists
```

- 다른 언어들은 이런 식으로 이름을 바꿔써도 좋다.

```f#
​let​ validateOrder' =
  validateOrder checkProductCodeExists checkAddressExists
```

- 이렇게 하면 앞에 두 디팬던시 파라미터가 부분적용되어 입력값이 UnvalidatedOrder 만 받을 수 있는
  함수가 생겨 조합이 가능하다.
- 그래서 디팬던시를 부분적용한 PlaceOrderWorkflow는 아래 처럼 만들 수 있다.

```f#
​let​ placeOrder : PlaceOrderWorkflow =
  ​// set up local versions of the pipeline stages​
  ​// using partial application to bake in the dependencies​
  let​ validateOrder =
    validateOrder checkProductCodeExists checkAddressExists
  ​let​ priceOrder =
    priceOrder getProductPrice
  ​let​ acknowledgeOrder =
    acknowledgeOrder createAcknowledgmentLetter sendAcknowledgment

  // return the workflow function​
  ​fun​ unvalidatedOrder ->
    ​// compose the pipeline from the new one-parameter functions​
    unvalidatedOrder
      |> validateOrder
      |> priceOrder
      |> acknowledgeOrder
      |> createEvents
```

- 이렇게해도 함수 조합이 힘든 경우가 있는데 acknowledgeOrder 함수의 결과가 그냥 이벤트이고
  pricedOrder가 아니기 때문에 createEvents에 전달 할 수 없다.
- 이런경우에는 절차형 프로그래밍 처람 각 단계의 결과를 변수에 할하는 방법으로 해결 할 수 있다.

```f#
let​ placeOrder : PlaceOrderWorkflow =
  ​// return the workflow function​
  fun​ unvalidatedOrder ->
    ​let​ validatedOrder =
      unvalidatedOrder
      |> validateOrder checkProductCodeExists checkAddressExists
    ​let​ pricedOrder =
      validatedOrder
      |> priceOrder getProductPrice
    let​ acknowledgmentOption =
      pricedOrder
      |> acknowledgeOrder createAcknowledgmentLetter sendAcknowledgment
    let​ events =
      createEvents pricedOrder acknowledgmentOption
    events
```

- 다음은 디팬던시를 전역으로 선언하지 않고 inject 하는 방법을 살펴보자.

## Injecting Dependencies
