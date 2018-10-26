# 함수 이해하기

## Functions, Functions, Everywhere

- 함수형 프로그래밍의 정의가 다양해서 객체지향 하는 사람들이 혼란스러워한다. 그래서 하나로 요약하면
- 함수형 프로그래밍은 함수를 정말 중하게 생각하는 프로그래밍이다.
- 객체지향에서는 큰 프로그램을 클래스로 나누고 함수형 패러다임에서는 큰 프로그램을 함수로 나눈다.
- 객체지향에서 컴포넌트 의존성을 줄이기 위해 인터페이스와 의존성 주입을 사용하고 함수형 패러다임에서는
  함수 파라미터를 사용한다?
- 코드의 재사용을 높이기 위해 객체지향은 상속이나 데코레터 패턴을 쓰고 함수형 패러다임은 재사용 코드를 함수에
  넣고 함수를 컴포지션하는 방법을 쓴다.

## Functions Are Things
- 함수는 인풋 값이나 아웃풋 값으로 쓸수 있고 함수의 행동을 조절하기 위해 파라미터로 전달될 수 있다?

## F#에서 함수

```f#
let​ plus3 x = x + 3            ​// plus3 : x:int -> int​
​let​ times2 x = x * 2           ​// times2 : x:int -> int​
​let​ square = (​fun​ x -> x * x)  ​// square : x:int -> int​
let​ addThree = plus3           ​// addThree : (int -> int)​
```

```f#
// listOfFunctions : (int -> int) list​
let​ listOfFunctions =
  [addThree; times2; square]
```

```f#
for​ fn ​in​ listOfFunctions ​do​
  ​let​ result = fn 100   ​// call the function​
  printfn ​"If 100 is the input, the output is %i"​ result

​// Result =>​
​// If 100 is the input, the output is 103​
​// If 100 is the input, the output is 200​
​// If 100 is the input, the output is 10000​
```

### 입력 값으로 함수

- 입력값으로 사용할 수 있는 예제 (특별한 내용 없음)

### 출력 값으로 함수

```f#
let​ add1 x = x + 1
​let​ add2 x = x + 2
let​ add3 x = x + 3
```

- 위 함수는 중복 코드가 있다. 아래 처럼 바꿀 수 있음

```f#
  ​let​ adderGenerator numberToAdd =
    ​// return a lambda​
  ​ fun​ x -> numberToAdd + x

  ​// val adderGenerator :​
  ​//    int -> (int -> int)​
```

- 익명 함수 대신 이름 있는 함수로 리턴할 수 도 있음

```f#
let​ adderGenerator numberToAdd =
  ​// define a nested inner function​
  ​let​ innerFn x =
    numberToAdd + x
​ 
  ​// return the inner function​
  innerFn
```

- 사용법은 아래와 같음

```
// test​
let​ add1 = adderGenerator 1
add1 2     ​// result => 3​
​ 
let​ add100 = adderGenerator 100
add100 2   ​// result => 102​
```

### 커링

- F#도 하스켈처럼 일부 파라미터만 적용하면 나머지 파리미터를 받는 함수를 리턴한다.

### 부분 적용

```f#
// sayGreeting: string -> string -> unit​
let​ sayGreeting greeting name =
  printfn ​"%s %s"​ greeting name
```

위 함수가 있을 때 첫번째 인자만 적용된 각기 다른 함수를 만들 수 있다.

```f#
// sayHello: string -> unit​
let​ sayHello = sayGreeting ​"Hello"​
 
// sayGoodbye: string -> unit​
​let​ sayGoodbye = sayGreeting ​"Goodbye"​

sayHello ​"Alex"​
​// output: "Hello Alex"
​ 
sayGoodbye ​"Alex"​
​// output: "Goodbye Alex"​
```

## Total Functions

아래 함수는 0인 경우 int 대신 예외가 발생하기 때문에 `int -> int` 함수 시그네처를 어긴다.

```f#
let​ twelveDividedBy n =
  ​match​ n ​with​
  | 6 -> 2
  | 5 -> 2
  | 4 -> 3
  | 3 -> 4
  | 2 -> 6
  | 1 -> 12
  | 0 -> failwith ​"Can't divide by zero
```

첫번째 해결 방법은 입력 값을 제한 하는 방법이다.

```f#
​type​ NonZeroInteger =
  // Defined to be constrained to non-zero ints.​
  ​// Add smart constructor, etc​
  ​private​ NonZeroInteger ​of​ ​int​

​/// Uses restricted input​
let​ twelveDividedBy (NonZeroInteger n) =
  match​ n ​with​
  | 6 -> 2
  ...
  // 0 can't be in the input​
  ​// so doesn't need to be handled​

twelveDividedBy : NonZeroInteger -> int
```

두번째 방법은 출력 타입을 올바른 값과 잘못된 값을 가질 수 있는 타입(Option)으로 정의 하는 방법이다.

```f#
/// Uses extended output​
let​ twelveDividedBy n =
  ​match​ n ​with​
  | 6 -> Some 2 ​// valid​
  | 5 -> Some 2 ​// valid​
  | 4 -> Some 3 ​// valid​
  ...
  | 0 -> None   ​// undefined​

twelveDividedBy : int -> int option
```

## Composition

- `사과 -> 바나나` 함수와 `바나나 -> 체리` 함수를 결합해서 `사과 -> 체리` 함수를 만들 수 있다. 이런
  결합을 바나나가 감춰졌다고 해서 `information hiding`이라고 한다.

### F#에서 Composition

- 파이핑(piping)으로 함수 결합을 할 수 있다. 유닉스 파이프와 비슷하다.

```f#
let​ add1 x = x + 1     ​// an int -> int function​
​let​ square x = x * x   ​// an int -> int function​
​ 
let​ add1ThenSquare x =
  x |> add1 |> square

​// test​
add1ThenSquare 5       ​// result is 36​
```

### Building an Entire Application from Functions

- `Low Level Operation`을 컴포지션 해서 `Service`를 만들고 `Service`를 컴포지션 해서
  `Workflow`를 만들 수 있다.
- 그리고 `Workflow`를 병렬(parallel)로 컴포지션해서 `Application`을 만들 수 있다.

### Challenges in Composing Functions

- 함수 타입이 맞지 않는 경우 조합이 어렵다.
- `... -> Option<int>` 함수와 `int -> ...` 함수는 연결할 수 없다.
- `... -> int` 함수와 `Option<int> -> ...` 함수는 연결할 수 없다.
- 가장 일반적인 방법은 양측의 동일한 유형, 즉 최소 공배수(lowest common multiple)로 연결하는 방법
  이다.
- `... -> int` 함수와 `Option<int> -> ...`의 연결은 두 타입의 lowest common multiple은
  `Option`이기 때문에 `int`를 `Some`으로 `Option<int>` 타입으로 변환해 두번째 함수로 넘길 수 있다.

```f#
​// a function that has an int as output​
let​ add1 x = x + 1

// a function that has an Option<int> as input​
let​ printOption x =
  ​match​ x ​with​
  | Some i -> printfn ​"The int is %i"​ i
  | None -> printfn ​"No value"​
```

두 함수를 아래처럼 조합할 수 있다.

```f#
5 |> add1 |> Some |> printOption
```
