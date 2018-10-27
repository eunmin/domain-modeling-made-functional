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
