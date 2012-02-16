package fileOperations

import akka.actor._

class fileWatcherActor ( folder: String, sfileMap: Map[String, FileArguments], fileOps: FileOp ) extends Actor {
  
  //var fileOps = new fileOp()
  
  var fileMap = sfileMap
  
  override def preStart () {
    self.id = folder
    println ("Initial: " + self.id)
/* 
    fileMap.foreach(x => println ("Key: " + x._1 + " \nValue: " + x._2.folder + " " 
                                                           + x._2.fileType + " " 
                                                           + x._2.fileSize + " " 
                                                           + x._2.dateTime
                                                           ))
*/    
    self ! searchChanges (folder, fileMap)
  }
  
  def receive = {
    case fileChanges (changedFilesMap) => 
      ///@TODO: delete deletet files out of the Map
      println ("\nChanges at: " + self.id)
      changedFilesMap.foreach(x => 
                               if ( ! x._2.delfile ) 
                                 fileMap += x._1 -> x._2
                               else
                                 fileMap -= x._1
                             )
      changedFilesMap.foreach(x => println ("Key: " + x._1 + " \nValue: " + x._2.folder + " " 
                                                           + x._2.fileType + " " 
                                                           + x._2.fileSize + " " 
                                                           + x._2.dateTime
                                                           ))
    case searchChanges (folder, filesMap) =>
      val newFiles = fileOps.checkFileChanges (folder, "*", fileMap)
      if ( ! newFiles.isEmpty ) {
        self ! fileChanges (newFiles)
      }
      //println ("Go sleeping")
      Thread.sleep(2000)
      //println ("Weak up")
      self ! searchChanges (folder, filesMap)
    case _ => 
  }
}