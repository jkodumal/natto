import shapeless._, HList._, Record._, TypeOperators._, Functions._, UnaryTCConstraint._
import com.google.template.soy.data.{ SoyMapData, SoyListData, SoyData }

package object natto {

  // For interoperability with shitty Java libraries
  private def returning[A](a: A)(f: A => Unit) = { f(a); a }

  trait Natto[A, R <: SoyData] {
    def ferment(a: A): R
  }

  type NattoData[A] = Natto[A, SoyData]
  type NattoList[A] = Natto[A, SoyListData]
  type NattoMap[A] = Natto[A, SoyMapData]
  
  def natto[T: NattoData](a: T) = implicitly[NattoData[T]].ferment(a)
  
  def nattoList[T: NattoList](a: T) = implicitly[NattoList[T]].ferment(a)

  def nattoMap[T: NattoMap](a: T) = implicitly[NattoMap[T]].ferment(a)
  
  implicit def IntNatto = new NattoData[Int] {
    def ferment(a: Int) = SoyData.createFromExistingData(a)
  }
  
  implicit def LongNatto = new NattoData[Long] {
    def ferment(l: Long) = SoyData.createFromExistingData(l.toString)
  }
  
  implicit def StringNatto = new NattoData[String] {
    def ferment(a: String) = SoyData.createFromExistingData(a)
  }
  
  implicit def HNilNatto = new NattoList[HNil] {
    def ferment(n : HNil) = new SoyListData()
  }

  implicit def HNilNattoMap = new NattoMap[HNil] {
    def ferment(n: HNil) = new SoyMapData()
  }

  // TODO : can we ditch this boilerplate?
  implicit def HNilNattoData = new NattoData[HNil] {
    def ferment(n : HNil) = new SoyMapData()
  }

  // TODO : can we ditch this boilerplate?
  implicit def hlistNattoData[H, T <: HList](implicit sh: NattoData[H], st: NattoList[T]): NattoData[H::T] = new NattoData[H :: T] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(a: H :: T) =  new SoyListData((natto(a.head):: nattoList(a.tail).toList).asJava)
  }

  // TODO : can we ditch this boilerplate?
  implicit def recordNattoData[D, F <: Field[D], T <: HList] (implicit sh: NattoData[D], st: NattoMap[T]): NattoData[(F,D)::T] = 
    new NattoData[(F, D) :: T] { 
      import scala.collection.JavaConversions._ 
      def ferment(a : (F, D) :: T) = returning(nattoMap(a.tail) )(_.putSingle(a.head._1.toString, natto(a.head._2)))
    }  

  implicit def hlistNatto[H, T <: HList](implicit sh: NattoData[H], st: NattoList[T]) = new NattoList[H :: T] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(a: H :: T) =  new SoyListData((natto(a.head):: nattoList(a.tail).toList).asJava)
  }

  implicit def recordNatto[D, F <: Field[D], T <: HList] (implicit sh: NattoData[D], st: NattoMap[T]) = 
    new NattoMap[(F, D) :: T] { 
      import scala.collection.JavaConversions._ 
      def ferment(a : (F, D) :: T) = returning(nattoMap(a.tail) )(_.putSingle(a.head._1.toString, natto(a.head._2)))
    }  

  implicit def listNatto[T](implicit sh: NattoData[T]) = new NattoList[List[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(lst : List[T]) = new SoyListData((lst map (natto(_))).asJava)
  }

  implicit def seqNatto[T](implicit sh: NattoData[T]) = new NattoList[Seq[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(seq : Seq[T]) = new SoyListData((seq map (natto(_))).asJava)
  }

  implicit def mapNatto[T](implicit sh: NattoData[T]) = new NattoMap[Map[String, T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(m : Map[String, T]) = new SoyMapData((m mapValues (natto(_))).asJava)    
  }

  implicit def setNatto[T](implicit sh: NattoData[T]) = new NattoList[Set[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def ferment(s: Set[T]) = new SoyListData((s map (natto(_))).asJava)    
  }


  // type-level witness demonstrating that L is a record (hlist of field, data tuples)
  trait IsRecord[L <: HList]

  implicit object hnilIsRecord extends IsRecord[HNil]

  implicit def hlistIsRecord[D, F <: Field[D], T <: HList](implicit irt: IsRecord[T]) = new IsRecord[(F, D) :: T]{}

  def isRecord[L <: HList](l: L)(implicit rec: IsRecord[L]) = true

  trait RecIso[T, R] {
    def iso(t: T) : R
  }

  trait RecId[T] extends RecIso[T, T] {
    def iso(t: T) : T = t
  }

  implicit object intRecIso extends RecId[Int]
  implicit object stringRecIso extends RecId[String]
  implicit object longRecIso extends RecId[Long]
  implicit object hnilRecIso extends RecId[HNil]

  // Recursively transform any hlist
  implicit def hlRecIso[D, T <: HList, R, S <: HList](implicit dri: RecIso[D, R], lri : RecIso[T, S]) = new RecIso[D :: T, R :: S] {
    def iso(a: D :: T): R :: S = dri.iso(a.head) :: lri.iso(a.tail)
  }

  // Recursively transform a record
  implicit def hlRecordIso[D, F <: Field[D], T <: HList, R, S <: HList](implicit dri: RecIso[D, R], lri: RecIso[T, S]) = new RecIso[(F,D) :: T, (F, R) :: S] {
    def iso(a : (F, D) :: T): (F, R) :: S = (a.head._1, dri.iso(a.head._2)) :: lri.iso(a.tail)
  }    

  // Recursively transform a case class with an isomorphism into an hlist
  implicit def ccRecIso[C <: Product, L <: HList, R <: HList](implicit is: HListIso[C, L], hlri: RecIso[L, R]) = new RecIso[C, R] { 
    import HListIso._
    def iso(c: C) = hlri.iso(toHList(c))
  }

  def makeIso[T, R](d: T)(implicit dri: RecIso[T, R]): R = dri.iso(d)
}
