import khttp.get
import khttp.post
import khttp.responses.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun main(args: Array<String>) {
    val baseUrl = "https://www.bk-feedback-eire.com/"
    val getResponse: Response = get(baseUrl)
    val text: String = getResponse.text
    val document: Document = Jsoup.parse(text)
    val form: Element = document.select("form").first()
    val action: String = form.attr("action")
    val inputs: Elements = form.select("input")
    val inputsMap: HashMap<String, String> = hashMapOf()
    inputs.forEach {
        val name: String = it.attr("name")
        val value: String = it.`val`()
        inputsMap[name] = value
    }
    inputsMap.remove("NextButton")
    val url = "$baseUrl/$action"
    val postResponse: Response = post(
        url = url,
        headers = mapOf("Content-Type" to "application/x-www-form-urlencoded"),
        data = inputsMap
    )
    val postText: String = postResponse.text
    print(postText)
}
