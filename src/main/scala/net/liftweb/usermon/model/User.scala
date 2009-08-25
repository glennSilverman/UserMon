package net.liftweb.usermon.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http._
import js.JsCmds._
import S._
import _root_.scala.xml._
import Helpers._


/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {
  
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
			       <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
  locale, timezone, password, textArea)

  // comment this line out to require email validations
  override def skipEmailValidation = true
  
  /**
   * The menu item for listing all users
   */
  def listUsersMenuLoc: Box[Menu] = {
    Full(Menu(Loc("listUsers", "list" :: Nil, S.??("List Users"),               
                  testLogginIn)))
  }
  
  
 
 
 
 override lazy val sitemap: List[Menu] =
  List(loginMenuLoc, logoutMenuLoc, createUserMenuLoc,
       lostPasswordMenuLoc, resetPasswordMenuLoc,
       editUserMenuLoc, changePasswordMenuLoc,listUsersMenuLoc,
       validateUserMenuLoc).flatten(a => a)
  
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] with ManyToMany{  
  
  def getSingleton = User // what's the "meta" server

  // define an additional field for a personal essay
  object textArea extends MappedTextarea(this, 2048) {
    override def textareaRows  = 10
    override def textareaCols = 50
    override def displayName = "Personal Essay"
  }
  
   object roles
    extends MappedManyToMany(UserRole, UserRole.user, UserRole.role, Role)
    
    def validSelectValues: Box[Seq[(String, String)]] = 
      Full(
         Role.findAll.map( x => (x.id.is.toString, x.name.is) ))
      
    
     
                  
}
