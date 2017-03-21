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
	* Distinction to Unit Tests: 
		> closely related to the difference between specifications and tests. (ScalaCheck: Definitive Guide, Nils, Artima)
* Property
	* Specifies behavior 
* Generator
	* Generates test data to be used when testing the property

### Code Example

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

#### Test Code (Property + Generator)

##### Property

```scala
val reverse2x: Prop = forAll(genListInt) { (list: MyList[Int]) => 
	reverse( reverse( list ) ) == list
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

```
scala> net.MySpec.reverse2x.check
+ OK, passed 100 tests.
```

## Reference

* [ScalaCheck: Definitive Guide](https://www.artima.com/shop/scalacheck) (Ricky Nils, Artima)
* [@lunaryorn](https://twitter.com/lunaryorn)'s [Stack Overflow answer](http://stackoverflow.com/a/42855840/409976) on generating 
  test recursive data structures