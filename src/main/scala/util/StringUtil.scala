package util

import java.util.Locale

object StringUtil {

  val LOCALE_TR = new Locale("tr", "TR")

  def replaceTurkishCharacters: String => String = _.replaceAll("\\u0131", "i")
    .replaceAll("\\u00e7", "c")
    .replaceAll("\\u011f", "g")
    .replaceAll("\\u00f6", "o")
    .replaceAll("\\u015f", "s")
    .replaceAll("\\u00fc", "u")

  def toSlug: String => String = str =>
    replaceTurkishCharacters(str.toLowerCase(LOCALE_TR))
      .replaceAll(" +", " ")
      .trim
      .replaceAll(" ", "-")
      .replaceAll("#", "")
}
