import example.contact.simple.{Contact => SimpleContact}

new SimpleContact("Eunmin", null, "Kim", "eunmin@eunmin.com", false)

import example.example.cardgame.CardGame._

Card (Club (), Two () ) match {
  case Card ((_, y) ) => println (y)
}

import example.temp._

def printTemp(temp: Temp) {
  temp match {
    case F(value) => println(s"F: $value")
    case C(value) => println(s"C: $value")
  }
}

printTemp(F(10))
printTemp(C(1.0F))

import example.payment.{Card => CreditCard, _}

def printPaymentMethod(paymentMethod: PaymentMethod): Unit = {
  paymentMethod match {
    case Cash() => println("Cash")
    case Cheque(ChequeNumber(number)) => println(s"Cheque: $number")
    case CreditCard((cardType, CardNumber(number))) => println(s"Card: $cardType, $number")
  }
}

printPaymentMethod(Cash())
printPaymentMethod(Cheque(ChequeNumber(1234)))
printPaymentMethod(CreditCard((Master(), CardNumber(4321))))

import example.contact.typed._

EmailAddress("valid@emailaddress.com")
EmailAddress("invalidEmailAddress")

String50("abcd")
String50("012345678901234567890123456789012345678901234567890123456789")