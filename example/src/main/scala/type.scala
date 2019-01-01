package example {
  package contact {
    /*
    type Contact = {
      FirstName: string
      MiddleInitial: string
      LastName: string

      EmailAddress: string
      IsEmailVerified: bool
    }
    */
    package simple {

      class Contact(
                     val firstName: String,
                     val middleInitial: String,
                     val lastName: String,
                     val emailAddress: String,
                     val isEmailVerified: Boolean
                   )

    }
    package typed {
      /*
      let createEmailAddress(s:string) =
        if Regex.IsMatch(s, @"^\S+@\S+\.\S+$")
          then Some (EmailAddress s)
          else None

      createEmailAddress:
        string -> EmailAddress option
       */
      case class EmailAddress(emailAddress: String)

      object EmailAddress {
        private val emailRegx = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

        def apply(emailAddress: String): Option[EmailAddress] =
          emailAddress match {
            case s if emailRegx.findFirstIn(s).isDefined => Some(new EmailAddress(emailAddress.toUpperCase))
            case _ => None
          }
      }

      /*
      type String50 = String50 of String

      let createString50 (s:string) =
        if s.Length <= 50
         then Some (String50 s)
         else None

      createString50:
        string -> String50 option
       */
      case class String50(value: String)

      object String50 {
        def apply(value: String): Option[String50] =
          if (value.length() < 50) {
            Some(new String50(value))
          }
          else {
            None
          }
      }

      case class PersonalName(firstName: String50, middleInitial: Option[String50], lastName: String50)

      case class VerifiedEmail(verifiedEmail: EmailAddress)

      sealed abstract class EmailContactInfo;
      case class Unverified(emailAddress: EmailAddress) extends EmailContactInfo
      case class Verified(emailAddress: VerifiedEmail) extends EmailContactInfo

      case class PostalContactInfo()

      /*
      type ContactInfo =
        | EmailOnly of EmailContactInfo
        | AddrOnly of PostalContactInfo
        | EmailAndAddr of EmailContactInfo * PostalContactInfo

      type Contact = {
        Name: Name
        ContactInfo: ContactInfo
      }
      */
      abstract class ContactInfo
      case class EmailOnly(emailContactInfo: EmailContactInfo) extends ContactInfo
      case class AddrOnly(postalContactInfo: PostalContactInfo) extends ContactInfo
      case class EmailAndAddr(pair: (EmailContactInfo, PostalContactInfo))

      case class Contact(name: PersonalName, contactInfo: ContactInfo)

      /*
      type ContactInfo =
        | Email of EmailContactInfo
        | Addr of PostalContactInfo

      type Contact = {
        Name: Name
        PrimaryContactInfo: ContactInfo
        SecondaryContactInfo: ContactInfo option
      }
      */
      case class MultiContact(name: PersonalName, primaryContactInfo: ContactInfo, secondContactInfo: Option[ContactInfo])
    }
  }

  /*
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
    */

  package example.cardgame {

    object CardGame {

      sealed abstract class Suit

      case class Club() extends Suit

      case class Diamond() extends Suit

      case class Spade() extends Suit

      case class Heart() extends Suit

      sealed abstract class Rank

      case class Two() extends Rank

      case class Three() extends Rank

      case class Four() extends Rank

      case class Five() extends Rank

      case class Six() extends Rank

      case class Seven() extends Rank

      case class Eight() extends Rank

      case class Nine() extends Rank

      case class Ten() extends Rank

      case class Jack() extends Rank

      case class Queen() extends Rank

      case class King() extends Rank

      case class Ace() extends Rank

      case class Card(pair: (Suit, Rank))

      case class Hand(cards: List[Card])

      case class Deck(cards: List[Card])

      case class Player(name: String, hand: Hand)

      case class Game(deck: Deck, players: List[Player])

    }

    import CardGame._

    trait CardGame {
      def deal(deck: Deck): (Deck, Card)

      def pickupCard(pair: (Hand, Card)): Hand
    }
  }

  package temp {
    // type Temp = F of int | C of float
    sealed abstract class Temp
    case class F(value: Int) extends Temp
    case class C(value: Float) extends Temp
  }

  package payment {
    /*
    type PaymentMethod =
      | Cash
      | Cheque of ChequeNumber
      | Card of CardType * CardNumber
     */
    sealed abstract class CardType
    case class Visa() extends CardType
    case class Master() extends CardType

    case class CardNumber(cardNumber: Long)
    case class ChequeNumber(chequeNumber: Long)

    sealed abstract class PaymentMethod
    case class Cash() extends PaymentMethod
    case class Cheque(chequeNumber: ChequeNumber) extends PaymentMethod
    case class Card(pair: (CardType, CardNumber)) extends PaymentMethod
  }
}






