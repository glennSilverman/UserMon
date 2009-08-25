package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import _root_.net.liftweb.mapper.view._
import _root_.java.sql.{Connection, DriverManager}
import _root_.net.liftweb.usermon.model._
import auth._
 

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?)
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)    
    
    // where to search snippet
    LiftRules.addToPackages("net.liftweb.usermon")
    Schemifier.schemify(true, Log.infoF _, User, Role, UserRole)
    
    //TableEditor.registerTable("Users", User, "Site Users")


    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: 
    Menu(Loc("Static", Link(List("static"), true, "/static/index"), "Static Content")) ::
    Menu(Loc("Edit User", List("edit"), "Edit User", Hidden)) :: 
    Role.menus :::
    User.sitemap 
    

    LiftRules.setSiteMap(SiteMap(entries:_*))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)
    
    import _root_.net.liftweb.http.ResourceServer
    ResourceServer.allow({
      case "asmselect" :: _ => true 
      })

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}

/**
* Database connection calculation
*/
object DBVendor extends ConnectionManager {
  private var pool: List[Connection] = Nil
  private var poolSize = 0
  private val maxPoolSize = 4

  private def createOne: Box[Connection] = try {
     val driverName: String = Props.get("db.driver") openOr
    "com.mysql.jdbc.Driver"

    val dbUrl: String = Props.get("db.url") openOr
    "jdbc:mysql://localhost:3306/lift_usermon?user=<username>&password=<password>"
    
    Class.forName(driverName)

    val dm = (Props.get("db.user"), Props.get("db.password")) match {
      case (Full(user), Full(pwd)) =>
	DriverManager.getConnection(dbUrl, user, pwd)

      case _ => DriverManager.getConnection(dbUrl)
    }

    Full(dm)
  } catch {
    case e: Exception => e.printStackTrace; Empty
  }

  def newConnection(name: ConnectionIdentifier): Box[Connection] =
    synchronized {
      pool match {
	case Nil if poolSize < maxPoolSize =>
	  val ret = createOne
        poolSize = poolSize + 1
        ret.foreach(c => pool = c :: pool)
        ret

	case Nil => wait(1000L); newConnection(name)
	case x :: xs => try {
          x.setAutoCommit(false)
          Full(x)
        } catch {
          case e => try {
            pool = xs
            poolSize = poolSize - 1
            x.close
            newConnection(name)
          } catch {
            case e => newConnection(name)
          }
        }
      }
    }

  def releaseConnection(conn: Connection): Unit = synchronized {
    pool = conn :: pool
    notify
  }
}


