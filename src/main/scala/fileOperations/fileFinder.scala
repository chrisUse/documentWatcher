package fileOperations

/*
 * run-main serverClient.fileFinder
 * 
 */

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException;
import scala.collection.mutable.ListBuffer

import akka.stm._
import akka.actor._

class FileOp {
  /*
  def findFilesRecursive (directoryName: String, filePattern: String): ListBuffer[String] = {
    val dir = new File(directoryName)
  
    val files = dir.listFiles();

    val matches = new ListBuffer[String]

    if (files != null) {
      for ( i <- 0 to files.length-1 ) {
        //if (files(i).getName().equalsIgnoreCase(filePattern)) { 
        //if ( files(i).getName().equals(filePattern) ) {
          
        //}
        if (files(i).isDirectory()) {
          matches += directoryName + files(i).getName()
          val folderFiles = findFilesRecursive((directoryName + files(i).getName() + "/") , filePattern)
          folderFiles.foreach(x => matches += x)
        } else {
          matches += directoryName + files(i).getName()
        }
      }
    }
    //files
    matches
  }
  */
  
  def checkFileChanges ( directoryName: String
                       , filePattern: String
                       , fileMap:Map[String, FileArguments] = Map()
                       ): Map[String, FileArguments] = {
    
    var fileChanges: Map[String, FileArguments] = Map()
    val dir = new File(directoryName)
    val files = dir.listFiles();
    
    var tmpfileMap = fileMap
    tmpfileMap.foreach(x => x._1 -> new FileArguments( x._2.folder
                                                     , x._2.fileType
                                                     , x._2.fileSize
                                                     , x._2.dateTime
                                                     , true))
    
    if (files != null) {
      for ( i <- 0 to files.length-1 ) {
        if ( ! files(i).isDirectory() ) {
          if ( ! fileMap.contains(directoryName + files(i).getName()) ) {
            fileChanges += directoryName + files(i).getName() ->
                           new FileArguments ( false, ""
                                             , files(i).length()
                                             , files(i).lastModified().toString()
                                             , false)
                           
            tmpfileMap += directoryName + files(i).getName() ->
                           new FileArguments ( false, ""
                                             , files(i).length()
                                             , files(i).lastModified().toString()
                                             , false)
          }
        }
      }
    }
    
    
    
    fileChanges
  }
  
  def findFilesRecursive ( directoryName: String
                         , filePattern: String
                         , fileMap:Map[String, FileArguments] = Map()
                         ): Map[String, FileArguments] = {
    val dir = new File(directoryName)
  
    val files = dir.listFiles();

    var matches  = fileMap

    if (files != null) {
      for ( i <- 0 to files.length-1 ) {
        //if (files(i).getName().equalsIgnoreCase(filePattern)) { 
        //if ( files(i).getName().equals(filePattern) ) {
          
        //}
        if (files(i).isDirectory()) {
          matches += directoryName + files(i).getName() -> 
                     new FileArguments (true, ""
                                       , files(i).length()
                                       , files(i).lastModified().toString()
                                       , false)
          //fileMap += directoryName + files(i).getName()
          val folderFiles = findFilesRecursive((directoryName + files(i).getName() + "/") , filePattern)
          folderFiles.foreach(x => matches += x)
        } else {
          matches += directoryName + files(i).getName() -> 
                     new FileArguments ( false, ""
                                       , files(i).length()
                                       , files(i).lastModified().toString()
                                       , false)
        }
      }
    }
    //files
    matches
  }  
}

class FileArguments (f: Boolean, fType: String, fSize: Long, dTime: String, deletet: Boolean) {
  val folder    = f
  val fileType  = fType
  val fileSize  = fSize
  val dateTime  = dTime
  val delfile   = deletet
}

object fileFinder extends App {
  var fileOps = new FileOp()
  
  val files = fileOps.findFilesRecursive ("./", "*")
  
  val input = readInt()
  
  val changedFiles = fileOps.checkFileChanges ("./", "*", files)
  
  changedFiles.foreach(x => println ("Key: " + x._1 + " \nValue: " + x._2.folder + " " 
                                                          + x._2.fileType + " " 
                                                          + x._2.fileSize + " " 
                                                          + x._2.dateTime
                                                          ))
  //val filesX = files.iterator
  
  //filesX.foreach(i => println ("> " + i))
}

object actorFileFinder extends App {
  val folder = "./"
  
  var fileOps = new FileOp()
  
  val files = fileOps.findFilesRecursive (folder, "*")
  
  var maxFolderDeep = 0
  
  files.foreach( x => if ( x._1.count(y => y=='/') > maxFolderDeep)  
                        { maxFolderDeep = x._1.count(y => y=='/') }
               )
  
  var t = folder.count(x => x=='/')
  
//  for ( t <- 0 to maxFolderDeep ) {
	// ==========================
	// Select only the elements at the actualy folder.
	var tmpFiles: Map[String, FileArguments] = Map()
	  
	files.foreach( x => if ( x._1.count(x => x=='/') == t && ! x._2.folder ) 
	                      tmpFiles += x._1 -> x._2
	             )
	               
	//t += 1
	// Starts the actor to looking for changes at the folder.
	val folderActorRef = Actor.actorOf(new fileWatcherActor(folder, tmpFiles, fileOps)).start()
	// ==========================
//  }
  //folderActorRef ! searchChanges (folder, files)
  
  ///val input = readInt()
  
  // Create and start the Actors
  
  /*
  val changedFiles = fileOps.checkFileChanges ("./", "*", files)
  
  changedFiles.foreach(x => println ("Key: " + x._1 + " \nValue: " + x._2.folder + " " 
                                                          + x._2.fileType + " " 
                                                          + x._2.fileSize + " " 
                                                          + x._2.dateTime
                                                          ))
  */
  //val filesX = files.iterator
  
  //filesX.foreach(i => println ("> " + i))
}

object tests extends App {
  println ("elemente anzahl: " + "./test/".count(x => x=='/') )
}

object mapTests extends App {
  var myMapTest: Map[String, FileArguments] = Map()
  
//  val test:[Boolean, String, Double, String] = (True, "pdf", 20, "30.01.2012")
  
  myMapTest += "hallo" -> new FileArguments (true, "pdf", 20, "30.01.2012",false)
  
  myMapTest.foreach(x => println ("Key: " + x._1 + " Value: " + x._2.fileSize))
  
  //myMapTest += "hallo" -> new FileArguments (true, "pdf", 30, "30.01.2012",false)
  myMapTest -= "hallo"
  
  myMapTest.foreach(x => println ("Key: " + x._1 + " Value: " + x._2.fileSize))
  
}