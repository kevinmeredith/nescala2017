# Property-based Testing with ScalaCheck by Example 

https://github.com/rickynils/scalacheck

## Info

* Kevin Meredith (@Gentmen)
* Banno
	* We are working to allow better communication between financial institutions and their consumers.
	* We're Hiring - email careers@banno.com
* Talk - kevinmeredith/nescala2017

## Agenda

* Definitions
* Code Example
* References

### Definitions

* Property-Based Testing
	* In comparison to Unit Testing:
		* > "closely related to the difference between specifications and tests" (ScalaCheck: Definitive Guide)
		* More powerful and concise
* Property
	* Specifies behavior 
* Generator
	* Generates test data to be used when testing properties
* ScalaCheck integrates with ScalaTest and Specs2 Test Libraries

### Code Example

##### Setup

```
$cat build.sbt 
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4"
)
```

##### Data Structure 

```scala
sealed abstract class MyList[+A]
case class Cons[+A](elem: A, rest: MyList[A]) extends MyList[A]
case object Empty                             extends MyList[Nothing]
```

###### Example

```scala
import net.{Cons, Empty, MyList}

val ints: MyList[Int] = Cons(1, Cons(2, Empty) )
```

##### MyList#reverse

```scala
// reverse ( Cons(1, Cons(2, Empty)) ) == Cons(2, Cons(1, Empty)) 
def reverse[A](list: MyList[A]): MyList[A] = list match {
	case Cons(elem, rest) => append( reverse(rest), Cons(elem, Empty) )
	case Empty            => Empty
}
```

#### Test Code (Property + Generator)

##### Properties

```scala

import org.scalacheck.Prop
import Prop.forAll        // Gen[A] => (A => Boolean) => Prop *

val genListInt: Gen[MyList[Int]] = ... 

val reverse2xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
	reverse( reverse( list ) ) == list
}

val reverse1xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
	reverse( list ) == list
}
```
##### Generator

```scala

import org.scalacheck.Gen
import Gen.const          // A          => Gen[A]
import Gen.choose         // (Int, Int) => Gen[Int] *
import Gen.posNum         // Gen[Int] 

val genListInt: Gen[MyList[Int]] = for {
	depth <- choose(0, 25)                // choose a depth between 0 and 25
	list  <- genList(posNum[Int], depth)  // produce a `Gen[MyList[Int]]`
} yield list
```

```scala
private def genList[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = {
	if(depth <= 0) {        // terminate the recursive data structure with an `Empty`
		genEmpty
	}
	else {                  // Otherwise, use Cons to build up the generated `MyList[A]`
		genCons(gen, depth)
	}
}
```

##### Examples

```
scala> net.MySpec.genListInt.sample
res6: Option[net.MyList[Int]] = Some(Cons(27,Empty))

scala> net.MySpec.genListInt.sample
res7: Option[net.MyList[Int]] = Some(Cons(59,Cons(24,Cons(7,Cons(43,Cons(33, Empty))))))
```

##### `Empty` Generator

```scala
private val genEmpty: Gen[MyList[Nothing]] = Gen.const(Empty)
```		

###### `Cons` Generator

```scala
private def genCons[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = 
	for {
		list <- genList(gen, depth-1)
		a    <- gen
	} yield Cons(a, list)
```

### Check the Property

#### Reverse List 2x = Input List 

```
scala> net.MySpec.reverse2xSame.check
+ OK, passed 100 tests.
```

#### Reverse List 1x = Input List 

```
scala> net.MySpec.reverse1xSame.check
! Falsified after 2 passed tests.
> ARG_0: Cons(2,Cons(2,Cons(2,Cons(2,Cons(1,Cons(2,Cons(1,Empty)))))))
```

## References

* [ScalaCheck: Definitive Guide](https://www.artima.com/shop/scalacheck) (Ricky Nils, Artima)
* [@lunaryorn](https://twitter.com/lunaryorn)'s [Stack Overflow answer](http://stackoverflow.com/a/42855840/409976) on generating 
  test recursive data structures
  
