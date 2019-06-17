package ubiqore.takibi.domain.valuesets

import scala.language.higherKinds

trait ValueSetRepositoryAlgebra[F[_]] {


  def create(vs: ValueSet): F[ValueSet]
 //  def validate(id:Long) :
  def read(id: Long): F[Option[ValueSet]]
  def update(vs: ValueSet) : F[Option[ValueSet]]
  def delete(id: Long): F[Option[ValueSet]]
  def findByIdentityAndMaturity(identity: String, maturity: Int): F[Option[ValueSet]]
}
