package com.sixelasacul.urfat

import khttp.get
import khttp.post
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun main(args: Array<String>) {
    val baseUrl = "https://www.bk-feedback-eire.com/"
    val code = browsePage(baseUrl)
    println(code)
}

fun browsePage(url: String, headers: Map<String, String>? = null, body: Map<String, String>? = null): String {
    val document: Document = getDocument(url, headers, body)
    val code: String? = getCode(document)
    if (code == null) {
        val form: Element = document.select("form").first()
        val action: String = form.attr("action")
        val fields: Elements = form.select("input, select")
        val newBody = generateValues(fields)
        val newHeaders: MutableMap<String, String> = if (headers == null) {
            mutableMapOf()
        } else {
            headers as MutableMap<String, String>
        }
        newHeaders["Content-Type"] = "application/x-www-form-urlencoded"
        return browsePage(buildUrl(url, action), newHeaders, newBody)
    } else {
        return code
    }
}

fun buildUrl(previousUrl: String, action: String): String {
    val regex: Regex = Regex("(\\w+.aspx\\?c=)(\\d{6})")
    val result: MatchResult? = regex.find(previousUrl)
    return if (result != null) {
        val temp: String = previousUrl.removeSuffix(result.value)
        "$temp$action"
    } else {
        "$previousUrl$action"
    }
}

fun generateValues(fields: Elements): HashMap<String, String> {
    val values: HashMap<String, String> = hashMapOf()
    fields.forEach {
        val name: String = it.attr("name")
        if (!values.containsKey(name)) {
            var doAdd: Boolean = true
            val value: String = when (it.tagName()) {
                "input" -> {
                    when (it.attr("type")) {
                        "radio" -> valueForRadioInput(getRadioInputs(fields, it.attr("name")))
                        "hidden" -> valueForHiddenInput(it)
                        "text" -> valueForTextInput(it)
                        else -> {
                            doAdd = false
                            ""
                        }
                    }
                }
                "select" -> valueForSelectInput(it)
                else -> valueForTextInput(it)
            }
            if (doAdd) {
                values[name] = value
            }
        }
    }
    return values
}

fun valueForTextInput(input: Element): String {
    if (input.id() == "SurveyCode")
        return "8962"
    return ""
}

fun getRadioInputs(inputs: Elements, name: String): List<Element> =
    inputs.filter { radio -> radio.attr("name") === name }

fun valueForRadioInput(inputs: Elements): String = valueForRadioInput(inputs.toList())

fun valueForRadioInput(inputs: List<Element>): String = inputs[0].`val`()

fun valueForHiddenInput(input: Element): String = input.`val`()

fun valueForSelectInput(input: Element): String {
    input.children().filter { it.tagName() === "option" }.forEach {
        if (it.`val`().isNotBlank())
            return it.`val`()
    }
    return ""
}

fun getCode(document: Document): String? {
    val codeSelection: Elements = document.select("#finishIncentiveHolder .ValCode")
    if (codeSelection.size == 0) {
        return null
    } else {
        val regex: Regex = Regex("Validation Code: (.+)")
        val result: MatchResult? = regex.find(codeSelection.html())
        if (result != null) {
            return result.value
        }
        return null
    }
}

fun getDocument(url: String, headers: Map<String, String>? = null, body: Map<String, String>? = null): Document {
    if (headers != null && body != null) {
        return Jsoup.parse(post(url = url, headers = headers, json = body).text)
    } else {
        return Jsoup.parse(get(url).text)
    }
}