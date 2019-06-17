package ubiqore.takibi.infrastructure.repository.doobie

import cats._
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe.Json
import io.circe.jawn.parse
import org.postgresql.util.PGobject
import ubiqore.takibi.domain.valuesets.{ValueSet, ValueSetRepositoryAlgebra}
//import SQLPagination._

private object ValueSetSQL {
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

  def insert(vs: ValueSet): Update0 =
    sql"""
           INSERT INTO valueset
           ( valueset_identity, valueset_name , valueset_start_date , valueset_flag , valueset_content ,  valueset_valid , valueset_project_id  , valueset_maturity)
           values (${vs.valueset_identity}, ${vs.valueset_name}, ${vs.valueset_start_date}, ${vs.valueset_flag}, ${vs.valueset_content} , ${vs.valueset_valid},  ${vs.valueset_project_id} , ${vs.valueset_maturity})
          RETURNING valueset_id
       """.update




  def update(vs: ValueSet, id: Long) : Update0 = sql"""
    UPDATE valueset
    SET valueset_name = ${vs.valueset_name}, valueset_flag = ${vs.valueset_flag}, valueset_valid = ${vs.valueset_valid} , valueset_maturity = ${vs.valueset_maturity}
    WHERE valueset_id = $id
  """.update

  def select(id: Long) : Query0[ValueSet] = sql"""
    SELECT *
    FROM valueset
    WHERE valueset_id = $id
  """.query

  def delete(id: Long) : Update0 = sql"""
    DELETE FROM valueset WHERE valueset_id = $id
  """.update

  def selectByIdentityAndMaturity(identity: String, maturity: Int) : Query0[ValueSet] =
    sql"""
      SELECT *
      FROM valueset
      WHERE valueset_identity = $identity and valueset_maturity = $maturity
    """.query



}

class DoobieValueSetRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
  extends ValueSetRepositoryAlgebra[F] {
  import ValueSetSQL._

  def create(valueSet: ValueSet): F[ValueSet] =
    insert(valueSet).withUniqueGeneratedKeys[Long]("valueset_id").map(valueset_id => valueSet.copy(valueset_id = valueset_id.some)).transact(xa)

  def update(valueSet: ValueSet): F[Option[ValueSet]] = OptionT.fromOption[ConnectionIO](valueSet.valueset_id).semiflatMap(valueset_id =>
    ValueSetSQL.update(valueSet, valueset_id).run.as(valueSet)
  ).value.transact(xa)



  def delete(id: Long): F[Option[ValueSet]] = OptionT(read(id)).semiflatMap(vs =>
   ValueSetSQL.delete(id).run.transact(xa).as(vs)
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
  def read(id: Long): F[Option[ValueSet]] = select(id).option.transact(xa)

  override def findByIdentityAndMaturity(identity: String, maturity: Int): F[Option[ValueSet]] =
    selectByIdentityAndMaturity(identity,maturity).option.transact(xa)
}

object DoobieValueSetRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieValueSetRepositoryInterpreter[F] =
    new DoobieValueSetRepositoryInterpreter(xa)
}
