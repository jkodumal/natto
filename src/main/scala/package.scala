import shapeless._, HList._, Record._
import com.google.template.soy.data.{ SoyMapData, SoyListData, SoyData }

package object natto {

  // For interoperability with shitty Java libraries
  private def returning[A](a: A)(f: A => Unit) = { f(a); a }

  trait Natto[A, R <: SoyData] {
    def toNatto(a: A): R
  }

  type NattoData[A] = Natto[A, SoyData]
  type NattoList[A] = Natto[A, SoyListData]
  type NattoMap[A] = Natto[A, SoyMapData]
  
  implicit def natto[T: NattoData](a: T) = implicitly[NattoData[T]].toNatto(a)
  
  implicit def nattoList[T: NattoList](a: T) = implicitly[NattoList[T]].toNatto(a)

  implicit def nattoMap[T: NattoMap](a: T) = implicitly[NattoMap[T]].toNatto(a)
  
  implicit def IntNatto = new NattoData[Int] {
    def toNatto(a: Int) = SoyData.createFromExistingData(a)
  }
  
  implicit def LongNatto = new NattoData[Long] {
    def toNatto(l: Long) = SoyData.createFromExistingData(l.toString)
  }
  
  implicit def StringNatto = new NattoData[String] {
    def toNatto(a: String) = SoyData.createFromExistingData(a)
  }
  
  implicit def HNilNatto = new NattoList[HNil] {
    def toNatto(n : HNil) = new SoyListData()
  }

  implicit def HNilNattoMap = new NattoMap[HNil] {
    def toNatto(n: HNil) = new SoyMapData()
  }

  // TODO : can we ditch this boilerplate?
  implicit def HNilNattoData = new NattoData[HNil] {
    def toNatto(n : HNil) = new SoyMapData()
  }

  // TODO : can we ditch this boilerplate?
  implicit def hlistNattoData[H, T <: HList](implicit sh: NattoData[H], st: NattoList[T]): NattoData[H::T] = new NattoData[H :: T] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(a: H :: T) =  new SoyListData((natto(a.head):: nattoList(a.tail).toList).asJava)
  }

  // TODO : can we ditch this boilerplate?
  implicit def recordNattoData[D, F <: Field[D], T <: HList] (implicit sh: NattoData[D], st: NattoMap[T]): NattoData[(F,D)::T] = 
    new NattoData[(F, D) :: T] { 
      import scala.collection.JavaConversions._ 
      def toNatto(a : (F, D) :: T) = returning(nattoMap(a.tail) )(_.putSingle(a.head._1.toString, natto(a.head._2)))
    }  

  implicit def hlistNatto[H, T <: HList](implicit sh: NattoData[H], st: NattoList[T]) = new NattoList[H :: T] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(a: H :: T) =  new SoyListData((natto(a.head):: nattoList(a.tail).toList).asJava)
  }

  implicit def recordNatto[D, F <: Field[D], T <: HList] (implicit sh: NattoData[D], st: NattoMap[T]) = 
    new NattoMap[(F, D) :: T] { 
      import scala.collection.JavaConversions._ 
      def toNatto(a : (F, D) :: T) = returning(nattoMap(a.tail) )(_.putSingle(a.head._1.toString, natto(a.head._2)))
    }  

  implicit def listNatto[T](implicit sh: NattoData[T]) = new NattoList[List[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(lst : List[T]) = new SoyListData((lst map (natto(_))).asJava)
  }

  implicit def seqNatto[T](implicit sh: NattoData[T]) = new NattoList[Seq[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(seq : Seq[T]) = new SoyListData((seq map (natto(_))).asJava)
  }

  implicit def mapNatto[T](implicit sh: NattoData[T]) = new NattoMap[Map[String, T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(m : Map[String, T]) = new SoyMapData((m mapValues (natto(_))).asJava)    
  }

  implicit def setNatto[T](implicit sh: NattoData[T]) = new NattoList[Set[T]] {
    import scala.collection.JavaConversions._
    import scala.collection.JavaConverters._
    def toNatto(s: Set[T]) = new SoyListData((s map (natto(_))).asJava)    
  }
}