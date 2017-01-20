package tests

import com.malliina.jenkinsctrl.models.Url
import org.scalatest.FunSuite

class UrlTests extends FunSuite {
  test("can get http protocol from Url") {
    val url = Url.build("http://www.google.com")
    assert(url.protocol === "http")
  }

  test("can convert http Url to https Url") {
    val url = Url.build("http://malliina.com/a?key=value")
    val httpsUrl = url.toHttps
    assert(httpsUrl.protocol === "https")
    assert(httpsUrl.url === "https://malliina.com/a?key=value")
  }
}
