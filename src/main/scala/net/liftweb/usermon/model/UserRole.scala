package net.liftweb.usermon.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import _root_.java.util.Date
import _root_.scala.xml._
import Helpers._

object UserRole extends UserRole with LongKeyedMetaMapper[UserRole] {
  override def fieldOrder = Nil   
  
}

class UserRole extends LongKeyedMapper[UserRole] with IdPK {
  def getSingleton = UserRole
  
  object role extends MappedLongForeignKey(this, Role){
    override def dbIndexed_? = true
  }
  
  object user extends MappedLongForeignKey(this, User) {
    override def dbIndexed_? = true
  } 
  
  def findByRole(role:Role):List[UserRole] = 
    UserRole.findAll(By(UserRole.role, role))  
  
}