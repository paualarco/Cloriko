import com.datastax.driver.core.{ Cluster, ConsistencyLevel, QueryOptions }
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ CassandraMonixContext, SnakeCase }
import monix.eval.Task
import scala.util.Try
import monix.execution.Scheduler
import com.datastax.driver.core.Session


object BasicCass {
  val cluster: Cluster = Cluster.builder.addContactPoint("localhost").withPort(9042).withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)).build()
  val session = cluster.connect()


}

object Cass {
  val cluster: Cluster = Cluster.builder.addContactPoint("localhost").withPort(9042).withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE)).build()


  lazy val ctx: CassandraMonixContext[SnakeCase] = new CassandraMonixContext[SnakeCase](SnakeCase, cluster, "cloriko", 10)

  import ctx._

  final case class userpods(
                             username: String,
                             podid: String,
                             name: String)

  /*def getUserPods() = {
    ctx.run {
      quote {
        ctx.querySchema[UserPods]("userpods")
      }
    }
  }

  val result = Try {
    getUserPods().executeAsync.runSyncUnsafe().foreach(println)
  }*/


  def createTable() = {
    cluster.connect("cloriko").execute("CREATE TABLE USERPODS(username text, podId text, name text, PRIMARY KEY ((username), podId)) ;")
  }

  val q = quote {
    query[userpods].insert(_.username -> "MyName", _.podid -> "1011L", _.name -> "d")
  }

  def q2 =
    ctx.run {
      quote {
        query[userpods].insert(lift(userpods("a2", "aa", "v")))
      }
    }

  //def run = ctx.run(q2).runSyncUnsafe()
}
implicit val scheduler = Scheduler.Implicits.global

Cass.q2.runSyncUnsafe()