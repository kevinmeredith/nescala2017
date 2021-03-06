package net

import org.scalacheck.Gen
import Gen.const          // A          => Gen[A]
import Gen.choose         // (Int, Int) => Gen[Int]*
import Gen.posNum         // Gen[Int] 
import org.scalacheck.Prop
import Prop.forAll        // Gen[A]     => Prop*

import MyList.reverse

object MySpec {

	// Create a generator to produce a random `MyList[Int]`
	val genListInt: Gen[MyList[Int]] = for {
		depth <- choose(0, 25)                // choose a max depth, i.e. # of Cons
		list  <- genList(posNum[Int], depth)  // pass the random 'depth' int to `genList`
	} yield list

	// Given a generator for type `A`, i.e. in `MyList[A]` and a max depth,
	// output a `MyList[A]` generator.
	private def genList[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = {
		if(depth <= 0) {        // terminate the recursive data structure with a `Empty`
			genEmpty
		}
		else {                  // Otherwise, use Cons to build up the generated `MyList[A]`
			genCons(gen, depth)
		}
	}

	// Empty case, i.e. termination
	private val genEmpty: Gen[MyList[Nothing]] = Gen.const(Empty)

	// Cons case, i.e. building up the `MyList[A]`
	private def genCons[A](gen: Gen[A], depth: Int): Gen[MyList[A]] = 
		for {
			list <- genList(gen, depth-1)
			a    <- gen
		} yield Cons(a, list)

	// Reversing MyList twice equals the same list
	val reverse2xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
		reverse( reverse( list ) ) == list
	}

	// A reversed List equals the same list
	val reverse1xSame: Prop = forAll(genListInt) { (list: MyList[Int]) => 
		reverse( list ) == list
	}
}
