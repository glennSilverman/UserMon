package net.liftweb.usermon.snippet

import _root_.scala.xml._
import _root_.net.liftweb.util.Helpers
import Helpers._
import _root_.net.liftweb.http.{LiftRules}

class HelloWorld {
  def howdy(in: NodeSeq): NodeSeq =
    Helpers.bind("b", in, "time" -> (new _root_.java.util.Date).toString)


def init(xhtml:NodeSeq):NodeSeq = 
  <head>        
       <script type="text/javascript" src={"/" + LiftRules.resourceServerPath + "/asmselect/jquery.asmselect.js"}></script>
       <link rel="stylesheet" type="text/css" href={"/" + LiftRules.resourceServerPath + "/asmselect/jquery.asmselect.css"} />
         <script type="text/javascript" charset="utf-8">{
        Unparsed("""
         jQuery(document).ready(function() {
            jQuery("select[multiple]").asmSelect();
          })
         """)
       }
      </script>    
   </head> 

}