package org.oterrier.gracyql


import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
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

    fun query(docClause : String,
              document: String,
              model: String = "en",
              disable : List<String>? = null): Result<GracyqlResponse, FuelError> {
        var nlpClause = mutableListOf<String>().let {
            if (model != null) it.add("model: ${gson.toJson(model)}")
            if (disable != null) it.add("disable: ${gson.toJson(disable)}")
            it.joinToString(",", prefix = "nlp(", postfix = ")")
        }
        var batchClause = mutableListOf<String>().let {
            if (document != null) it.add("text: ${gson.toJson(document)}")
            it.joinToString(",", prefix = "doc(", postfix = ")")
        }


        val query = """
            query {
              $nlpClause {
                $batchClause {
                    $docClause
                }
              }
            }
        """.trimIndent()
        val (_, _, result) = url
            .httpPost()
            .timeout(15000)
            .timeoutRead(60000)
            .header(Headers.ACCEPT, "gzip")
            .header(Headers.CONTENT_TYPE, "application/graphql")
            .body(query)
            .responseObject(gsonDeserializerOf(GracyqlResponse::class.java))
        return result
    }

    inner class BatchQuery(val docsClause : String,
                     val documents: List<String>? = null,
                     val batchSize: Int? = null,
                     val next: Int? = null,
                     val model: String = "en",
                     val disable : List<String>? = null
    ) {
        fun seedF(): Batch? {
            batchQuery(docsClause, documents, batchSize = batchSize, next=next).fold(
                { data -> return data?.data?.nlp?.batch },
                { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
            )
        }
        fun nextF(b : Batch?): Batch? {
            val batchId = b?.batch_id
            batchQuery(docsClause, batchId = batchId, next=next).fold(
                { data -> return data?.data?.nlp?.batch },
                { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
            )
        }
        fun toSequence() : Sequence<SpacyDoc?> = generateSequence(seedFunction=::seedF, nextFunction=::nextF).flatMap { it.docs!!.asSequence() }
    }


    fun batchQuery(docsClause : String,
                   documents: List<String>? = null,
                   batchId: String? = null,
                   batchSize: Int? = null,
                   next: Int? = null,
                   model: String = "en",
                   disable : List<String>? = null
    ): Result<GracyqlResponse, FuelError> {
        var nlpClause = mutableListOf<String>().let {
            if (model != null) it.add("model: ${gson.toJson(model)}")
            if (disable != null) it.add("disable: ${gson.toJson(disable)}")
            it.joinToString(",", prefix = "nlp(", postfix = ")")
        }
        var batchClause = mutableListOf<String>().let {
            if (documents != null) it.add("texts: ${gson.toJson(documents)}")
            if (batchId != null) it.add("batch_id: ${gson.toJson(batchId)}")
            if (batchSize != null) it.add("batch_size: $batchSize")
            if (next != null) it.add("next: $next")
            it.joinToString(",", prefix = "batch(", postfix = ")")
        }


        val query = """
            query {
              $nlpClause {
                $batchClause {
                    batch_id
                    docs {
                        $docsClause
                    }
                }
              }
            }
        """.trimIndent()

        val (_, _, result) = url
            .httpPost()
            .timeout(15000)
            .timeoutRead(5 * 60000)
            .header(Headers.ACCEPT, "gzip")
            .header(Headers.CONTENT_TYPE, "application/graphql")
            .body(query)
            .responseObject(gsonDeserializerOf(GracyqlResponse::class.java))
        return result
    }

    fun tag(document: String, model: String = "en", disable : List<String>? = null): SpacyDoc? {
        val docClause = """
                      text
                      tokens {
                          id
                          start
                          end
                          pos
                          lemma
                      }
        """.trimIndent()
        query(docClause, document, model, disable).fold(
            { data -> return data?.data?.nlp?.doc },
            { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
        )
    }

    fun batchTag(
        documents: List<String>? = null,
        batchId: String? = null,
        batchSize: Int? = null,
        next: Int? = null,
        model: String = "en",
        disable : List<String>? = null
    ): Sequence<SpacyDoc?> {

        val docsClause = """
                      text
                      tokens {
                          id
                          start
                          end
                          pos
                          lemma
                      }
        """.trimIndent()
        val bq = BatchQuery(docsClause, documents, batchSize, next, model, disable)
        return bq.toSequence()
    }

    fun tagWithSentences(document: String, model: String = "en", disable : List<String>? = null): SpacyDoc? {
        val docClause = """
                      text
                      sents {
                          start
                          end
                          tokens {
                              id
                              start
                              end
                              pos
                              lemma
                          }
                      }
        """.trimIndent()
        query(docClause, document, model, disable).fold(
            { data -> return data?.data?.nlp?.doc },
            { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
        )
    }

    fun ner(document: String, model: String = "en", disable : List<String>? = null): SpacyDoc? {
        val docClause = """
                      text
                      ents {
                          start
                          end
                          text
                          label
                      }
        """.trimIndent()
        query(docClause, document, model, disable).fold(
            { data -> return data?.data?.nlp?.doc },
            { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
        )
    }

    fun nerWithSentences(document: String, model: String = "en", disable : List<String>? = null): SpacyDoc? {
        val docClause = """
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
        """.trimIndent()
        query(docClause, document, model, disable).fold(
            { data -> return data?.data?.nlp?.doc },
            { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
        )
    }

    fun ping(document: String, model: String = "en", disable : List<String>? = null): SpacyDoc? {
        val docClause = """
                      text
        """.trimIndent()
        query(docClause, document, model, disable).fold(
            { data -> return data?.data?.nlp?.doc },
            { error -> println("An error of type ${error.exception} happened: ${error.message}"); return null }
        )
    }
    fun batchPing(
        documents: List<String>? = null,
        batchId: String? = null,
        batchSize: Int? = null,
        next: Int? = null,
        model: String = "en",
        disable : List<String>? = null
    ): Sequence<SpacyDoc?> {

        val docsClause = """
                      text
        """.trimIndent()
        val bq = BatchQuery(docsClause, documents, batchSize, next, model, disable)
        return bq.toSequence()
    }

}