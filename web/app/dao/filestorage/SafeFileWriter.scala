package dao.filestorage

import org.apache.commons.io.FileUtils
import java.io.{IOException, File}

/**
 *
 * @author rodion
 */

class SafeFileWriter(val path: String, val enc: String) {
  val file = new File(path)
  val tmp = new File(file.getParentFile, file.getName + ".tmp")
  val bak = new File(file.getParentFile, file.getName + ".bak")

  def write(data: String) {
    if (!file.getParentFile.exists)
      if (!file.getParentFile.mkdirs)
        throw new IOException("Could not created db directory: " + file.getParentFile)
    init
    FileUtils.write(tmp, data, enc)
    file.renameTo(bak)
    tmp.renameTo(file)
    bak.delete
  }

  def read(): Option[String] = {
    init
    if (file.exists)
      Some(FileUtils.readFileToString(file, enc))
    else
      None
  }

  private def init {
    if (tmp.exists) tmp.delete
    if (bak.exists) {
      if (file.exists) file.delete
      bak.renameTo(file)
    }
  }
}
