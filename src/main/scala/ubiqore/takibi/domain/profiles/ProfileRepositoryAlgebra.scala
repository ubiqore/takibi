package ubiqore.takibi.domain.profiles

import scala.language.higherKinds


trait ProfileRepositoryAlgebra[F[_]] {
  def create(pr: Profile): F[Profile]
  //  def validate(id:Long) :
  def read(id: Long): F[Option[Profile]]
  def update(pr: Profile) : F[Option[Profile]]
  def delete(id: Long): F[Option[Profile]]
  def findByIdentityAndMaturity(identity: String, maturity: Int): F[Option[Profile]]
}
