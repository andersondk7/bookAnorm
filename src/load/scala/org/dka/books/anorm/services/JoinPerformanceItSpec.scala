package org.dka.books.anorm.services

import cats.data.Validated.*
import com.typesafe.scalalogging.Logger
import org.dka.books.anorm.services.DaoFactory
import org.dka.books.domain.config.*
import org.dka.books.domain.model.fields.ID
import org.dka.books.domain.model.query.BookAuthorSummary
import org.dka.books.domain.services.BookDao
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Outcome, ScalaTestVersion}

import java.util.UUID
//import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

class JoinPerformanceItSpec extends AnyFunSpec with Matchers {
  private val logger = Logger(getClass.getName)

  import JoinPerformanceItSpec.*

  describe("join testing: anorm") {
    it("getAuthorsForBooks single query, first") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val id: ID = ids.head
        println(s"books:  ${ids.length}")
        val now = System.currentTimeMillis()
        Await.result(factory.bookDao.getBookAuthorSummary(id), delay).getOrElse(throw new Exception("error"))
        val time = System.currentTimeMillis() - now
        logger.info(s"anorm: first single query, time: $time")
      }
    }
    it("getAuthorsForBooks concurrently") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val bookCount = ids.length

        val now = System.currentTimeMillis()
        val queries: Future[Seq[BookAuthorSummary]] = Future
          .sequence(ids.map { id =>
            factory.bookDao
              .getBookAuthorSummary(id)
              .map(_.getOrElse(throw new Exception(s"failed reading bookDao for $id")))
          })
          .map(_.flatten)
        val summaries: Seq[BookAuthorSummary] = Await.result(queries, delay)
        val time = System.currentTimeMillis() - now
        logger.info(s"anorm: concurrent for ${ids.size} queries, time: $time, avg time: ${time / ids.size.toDouble}")
        summaries.size shouldBe 40000
      }
    }
    it("getAuthorsForBooks sequentially") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val now = System.currentTimeMillis()
        // sequential queries
        val query: (ID, BookDao) => Seq[BookAuthorSummary] = (id, dao) =>
          Await
            .result(dao.getBookAuthorSummary(id), delay)
            .getOrElse(throw new Exception(s"could not get summary for $id"))
        val summaries: Seq[BookAuthorSummary] = ids.flatMap(query(_, factory.bookDao))
        val time = System.currentTimeMillis() - now
        logger.info(s"anorm: sequential for ${ids.size} queries, time: $time, avg time: ${time / ids.size.toDouble}")
        summaries.size shouldBe 40000
      }
    }
    it("getAuthorsForBooks single query, last") {
      withFactory { factory =>
        val ids: Seq[ID] = getIds(factory.bookDao)
        val id: ID = ids.head
        val now = System.currentTimeMillis()
        // make a call for each book (all 2000 of them)
        Await.result(factory.bookDao.getBookAuthorSummary(id), delay).getOrElse(throw new Exception("error"))
        val time = System.currentTimeMillis() - now
        logger.info(s"anorm: last single query, time: $time")
      }
    }
  }

  private val configure = DaoFactory.configure

  private def withFactory(testCode: DaoFactory => Any): Unit =
    configure match {
      case Invalid(chain) =>
        val reasons = ConfigException.reasons(chain).mkString(" : ")
        fail(new IllegalStateException(reasons))
      case Valid(factory) => testCode(factory)
    }

  private def getIds(bookDao: BookDao): Seq[ID] =
    Await
      .result(bookDao.getAllIds, delay)
      .getOrElse(fail("could not get ids"))
}

object JoinPerformanceItSpec {
  val delay: FiniteDuration = 30.seconds
}
