package org.dka.books.anorm.services

//import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, Executors, SynchronousQueue, ThreadPoolExecutor, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.util.UtilityElf
import com.zaxxer.hikari.util.UtilityElf.DefaultThreadFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.dka.books.domain.config.DBConfig
import org.dka.books.domain.config.DBConfig.ConfigErrorsOr
import org.dka.books.domain.services.*

import java.util.concurrent.{Executors, ForkJoinPool}

class DaoFactory(val dataSource: HikariDataSource, dbEx: ExecutionContext) {

  val countryDao: CountryDao = new CountryDaoImpl(dataSource, dbEx)

  val locationDao: LocationDao = new LocationDaoImpl(dataSource, dbEx)

  val publisherDao: PublisherDao = new PublisherDaoImpl(dataSource, dbEx)

  val authorDao: AuthorDao = new AuthorDaoImpl(dataSource, dbEx)

  val bookDao: BookDao = new BookDaoImpl(dataSource, dbEx)

}

object DaoFactory {

  private val logger = Logger(getClass.getName)

  lazy val configure: ConfigErrorsOr[DaoFactory] = {
    logger.info(s"loading configure")
//    Class.forName("org.postgresql.ds.PGSimpleDataSource")
    DBConfig.load
      .map { config =>
        val poolConfig = new HikariConfig()
        poolConfig.setJdbcUrl(config.url)
        poolConfig.setUsername(config.properties.user)
        poolConfig.setPassword(config.properties.password)
        poolConfig.setMaximumPoolSize(config.maxConnections)
        poolConfig.setPoolName(config.connectionPool)
        poolConfig.setSchema(config.properties.schema)
        logger.info(s"schema: ${poolConfig.getSchema}")
        // not really needed since defaults to "org.postgresql.Driver"
//        poolConfig.setDriverClassName(config.dataSourceClass)
//        val workingQueue = ArrayBlockingQueue(config.queueSize, true).asInstanceOf[BlockingQueue[Runnable]]
//        val dbEx: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
        val dbEx: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(config.numThreads))
//          ExecutionContext.fromExecutor(
//          new ThreadPoolExecutor(config.numThreads, config.numThreads, 0L, TimeUnit.MILLISECONDS, workingQueue)
//        )
        val dataSource: HikariDataSource = new HikariDataSource(poolConfig)
        val factory                      = new DaoFactory(dataSource, dbEx)
        logger.info(s"got factory")
        factory
      }

  }

}
