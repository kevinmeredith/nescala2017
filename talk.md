# Property-based Testing with ScalaCheck by Example (Mar-2017)

## Info

* Kevin Meredith (@Gentmen)
* Banno

## Agenda

* Definitions
* Code Example
* Summary

### Definitions

* Property-Based Testing -
	* Versus Unit Tests
		> "closely related to the difference between specifications and tests" (ScalaCheck: Definitive Guide).
* Property
	* Specifies behavior 
* Generator
	* Generates test data to be used when testing the property

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

##### MyList#reverse

```scala
def reverse[A](list: MyList[A]): MyList[A] = list match {
	case Cons(elem, rest) => append( reverse(rest), Cons(elem, Empty) )
	case Empty            => Empty
}
```

```scala
import net.{Cons, Empty, MyList}

val ints: MyList[Int] = Cons(1, Cons(2, Empty) )
```

#### Test Code (Property + Generator)

##### Properties

```scala
val reverse2xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
	reverse( reverse( list ) ) == list
}

val reverse1xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
	reverse( list ) == list
}
```
##### Generator

```scala
private def genList[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = {
	if(depth <= 0) {        // terminate the recursive data structure with a `Empty`
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
res7: Option[net.MyList[Int]] = Some(Cons(59,Cons(24,Cons(7,Cons(43,Cons(33,Cons(95,Cons(13,Cons(54,Cons(48,Cons(62,Cons(51,Cons(69,Cons(14,Cons(5,Cons(44,Cons(11,Empty)))))))))))))))))
```

##### `Empty` Generator

```scala
private val genEmpty: Gen[MyList[Nothing]] = Gen.const(Empty)
```		

###### `Cons` Generator

```scala
// Cons case, i.e. building up the `MyList[A]`
private def genCons[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = 
	for {
		list <- genList(gen, depth-1)
		a    <- gen
	} yield Cons(a, list)
```

### Check the Property

#### Valid Property

```
scala> net.MySpec.reverse2x.check
+ OK, passed 100 tests.
```

#### Invalid Property 

```
scala> net.MySpec.invalid.check
! Falsified after 2 passed tests.
> ARG_0: Cons(2,Cons(2,Cons(2,Cons(2,Cons(1,Cons(2,Cons(1,Empty)))))))
```

## References

* [ScalaCheck: Definitive Guide](https://www.artima.com/shop/scalacheck) (Ricky Nils, Artima)
* [@lunaryorn](https://twitter.com/lunaryorn)'s [Stack Overflow answer](http://stackoverflow.com/a/42855840/409976) on generating 
  test recursive data structures
  
