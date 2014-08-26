package helper

import java.io.File
import org.apache.commons.io.FileUtils
import java.math.BigInteger
import java.security.MessageDigest

object HashHelper {

  private val md5 = MessageDigest.getInstance("MD5")

  def hashFileName(file: File): String = {
    md5(file.getAbsolutePath)
  }

  def base64(file: File): String = {
    new sun.misc.BASE64Encoder().encode(FileUtils.readFileToByteArray(file))
  }

  private def md5(fileName: String): String = {
    new BigInteger(1, md5.digest(fileName.getBytes)).toString(16)
  }
}
