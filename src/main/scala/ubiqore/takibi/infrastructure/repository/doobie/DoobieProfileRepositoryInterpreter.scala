package ubiqore.takibi.infrastructure.repository.doobie

import cats._
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe.Json
import io.circe.jawn.parse
import org.postgresql.util.PGobject
import ubiqore.takibi.domain.profiles.{Profile, ProfileRepositoryAlgebra}
//import SQLPagination._

private object ProfileSQL {
  implicit val jsonGet: Get[Json] =
    Get.Advanced.other[PGobject](NonEmptyList.of("json")).tmap[Json] { o =>
      parse(o.getValue).leftMap[Json](throw _).merge
    }

  implicit val jsonPut: Put[Json] =
    Put.Advanced.other[PGobject](NonEmptyList.of("json")).tcontramap[Json] { j =>
      val o = new PGobject
      o.setType("json")
      println("j is object ? " + j.toString  +" " +  j.isObject)
      o.setValue(j.noSpaces)
      o
    }

  implicit val jsonMeta: Meta[Json] =
    Meta.Advanced.other[PGobject]("json").timap[Json](
      a => parse(a.getValue).leftMap[Json](e => throw e).merge)(
      a => {
        val o = new PGobject
        o.setType("json")
        println("a is object ? " + a.toString  +" " +  a.isObject)
        o.setValue(a.noSpaces)
        o
      }
    )

  def insert(pr: Profile): Update0 =
    sql"""
           INSERT INTO profile
           ( profile_identity, profile_name , profile_start_date , profile_flag , profile_content ,  profile_valid , profile_project_id  , profile_maturity)
           values (${pr.profile_identity}, ${pr.profile_name}, ${pr.profile_start_date}, ${pr.profile_flag}, ${pr.profile_content} , ${pr.profile_valid},  ${pr.profile_project_id} , ${pr.profile_maturity})
          RETURNING profile_id
       """.update




  def update(pr: Profile, id: Long) : Update0 = sql"""
    UPDATE profile
    SET profile_name = ${pr.profile_name}, profile_flag = ${pr.profile_flag},  profile_valid = ${pr.profile_valid}
    WHERE id = $id
  """.update

  def select(id: Long) : Query0[Profile] = sql"""
    SELECT *
    FROM profile
    WHERE profile_id = $id
  """.query

  def delete(id: Long) : Update0 = sql"""
    DELETE FROM profile WHERE profile_id = $id
  """.update

  def selectByIdentityAndMaturity(identity: String, maturity: Int) : Query0[Profile] =
    sql"""
      SELECT *
      FROM profile
      WHERE profile_identity = $identity and profile_maturity = $maturity
    """.query



}

class DoobieProfileRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
  extends ProfileRepositoryAlgebra[F] {
  import ProfileSQL._

  def create(valueSet: Profile): F[Profile] =
    insert(valueSet).withUniqueGeneratedKeys[Long]("profile_id").map(profile_id => valueSet.copy(profile_id = profile_id.some)).transact(xa)

  def update(valueSet: Profile): F[Option[Profile]] = OptionT.fromOption[ConnectionIO](valueSet.profile_id).semiflatMap(profile_id =>
    ProfileSQL.update(valueSet, profile_id).run.as(valueSet)
  ).value.transact(xa)



  def delete(id: Long): F[Option[Profile]] = OptionT(read(id)).semiflatMap(vs =>
   ProfileSQL.delete(id).run.transact(xa).as(vs)
  ).value

//  def findByNameAndFhirVersion(name: String, fhir_version: String): F[Set[Project]] =
//    selectByNameAndFhirVersion(name, fhir_version).to[List].transact(xa).map(_.toSet)
//
//  def list(pageSize: Int, offset: Int): F[List[Project]] =
//    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
//
//  def findByStatus(statuses: NonEmptyList[ProjectStatus]): F[List[Project]] =
//    selectByStatus(statuses).to[List].transact(xa)
//
//  def findByBaseIRI(bases: NonEmptyList[String]): F[List[Project]] =
//    selectBaseIRILikeString(bases).to[List].transact(xa)
  def read(id: Long): F[Option[Profile]] = select(id).option.transact(xa)

  override def findByIdentityAndMaturity(identity: String, maturity: Int): F[Option[Profile]] =
    selectByIdentityAndMaturity(identity,maturity).option.transact(xa)
}

object DoobieProfileRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieProfileRepositoryInterpreter[F] =
    new DoobieProfileRepositoryInterpreter(xa)
}
