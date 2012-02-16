package fileOperations

trait fileWatcherMessages 

//case class startWatch ()
case class fileChanges (changedFilesMap: Map[String, FileArguments]) extends fileWatcherMessages
case class searchChanges (folder: String, filesMap: Map[String, FileArguments]) extends fileWatcherMessages
