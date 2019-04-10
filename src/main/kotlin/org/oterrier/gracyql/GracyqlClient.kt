package org.oterrier.gracyql


import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson


fun main(args: Array<String>) {

    val cli = GracyqlClient()

    try {
        val v = cli.tag("How are you Bob? What time is it in London?")

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        cli.close();
    }

}

data class GracyqlResponse(
    val data: SpacyData? = null,
    val errors: List<Any>? = null
)

class GracyqlClient(
    val host: String = "127.0.0.1",
    val port: Int = 8990,
    val model: String = "en"
) {
    var gson = Gson()
    val url = "http://$host:$port/"

    init {
        FuelManager.instance.basePath = "$this.host:$this.port"
    }

    fun close() {
    }

    fun queryOne(query: String): SpacyDoc? {
        val (_, _, result) = url
            .httpPost()
            .timeout(15000)
            .timeoutRead(60000)
            .header(Headers.ACCEPT, "gzip")
            .header(Headers.CONTENT_TYPE, "application/graphql")
            .body(query)
            .responseObject(gsonDeserializerOf(GracyqlResponse::class.java))
        return result.get()?.data?.nlp?.doc
    }

    fun queryMany(query: String): List<SpacyDoc?>? {
        val (_, _, result) = url
            .httpPost()
            .timeout(15000)
            .timeoutRead(5 * 60000)
            .header(Headers.ACCEPT, "gzip")
            .header(Headers.CONTENT_TYPE, "application/graphql")
            .body(query)
            .responseObject(gsonDeserializerOf(GracyqlResponse::class.java))
        return result.get()?.data?.nlp?.docs
    }

    fun tag(document: String, model: String = "en"): SpacyDoc? {
        val query = """
            fragment PosTagger on Token {
              id
              start
              end
              pos
              lemma
            }

            query PosTagger {
              nlp(model: "$model") {
                doc(text: ${gson.toJson(document)}) {
                  text
                  tokens {
                    ...PosTagger
                  }
                }
              }
            }
            """.trimIndent()
        return queryOne(query)
    }

    fun tag(documents: List<String>, batch: Int = 100, model: String = "en"): List<SpacyDoc?>? {
        val query = """
            fragment PosTagger on Token {
              id
              start
              end
              pos
              lemma
            }

            query PosTagger {
              nlp(model: "$model") {
                docs(texts: ${gson.toJson(documents)}, batch_size : $batch) {
                  text
                  tokens {
                    ...PosTagger
                  }
                }
              }
            }
            """.trimIndent()
        return queryMany(query)

    }

    fun tagWithSentences(document: String, model: String = "en"): SpacyDoc? {
        val query = """
            fragment PosTagger on Token {
              id
              start
              end
              pos
              lemma
            }

            query POSTaggerWithSentences {
              nlp(model: "$model") {
                doc(text: ${gson.toJson(document)}) {
                  text
                  sents {
                      start
                      end
                      tokens {
                        ...PosTagger
                      }
                  }
                }
              }
            }
            """.trimIndent()
        return queryOne(query)
    }

    fun ner(document: String, model: String = "en"): SpacyDoc? {
        val query = """
            query NER {
              nlp(model: "$model") {
                doc(text: ${gson.toJson(document)}) {
                  text
                  ents {
                    start
                    end
                    label
                  }
                }
              }
            }
            """.trimIndent()
        return queryOne(query)
    }

    fun nerWithSentences(document: String, model: String = "en"): SpacyDoc? {
        val query = """
            query NERWithSentences {
              nlp(model: "$model") {
                doc(text: ${gson.toJson(document)}) {
                  text
                  sents {
                      start
                      end
                      ents {
                        start
                        end
                        text
                        label
                      }
                  }
                }
              }
            }
            """.trimIndent()
        return queryOne(query)
    }

    fun ping(document: String, model: String = "en"): SpacyDoc? {
        val query = """
            {
              nlp(model: "$model") {
                doc(text: ${gson.toJson(document)}) {
                  text
                }
              }
            }
            """.trimIndent()
        return queryOne(query)
    }

    fun ping(documents: List<String>, batch: Int = 100, model: String = "en"): List<SpacyDoc?>? {
        val query = """
            {
              nlp(model: "$model") {
                docs(texts: ${gson.toJson(documents)}, batch_size : $batch) {
                  text
                }
              }
            }
            """.trimIndent()
        return queryMany(query)

    }
}