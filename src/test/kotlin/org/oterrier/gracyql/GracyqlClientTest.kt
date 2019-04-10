package com.kairntech.kspacy

import org.oterrier.gracyql.GracyqlClient
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import kotlin.system.measureTimeMillis

class GracyqlClientTest {

    private lateinit var cli: GracyqlClient

    @Before
    fun setUp() {
        cli = GracyqlClient("127.0.0.1", 8990)
    }

    @After
    fun tearDown() {
        cli.close()
    }

    @Test
    fun most_similar() {
    }

    @Test
    fun ping() {
        val doc_text = "How are you Bob? What time is it in London?"

        val doc = cli.ping(doc_text)
        assertEquals(doc?.text, doc_text)
    }

    @Test
    fun tag() {
        val doc_text = "How are you Bob? What time is it in London?"

        val doc = cli.tag(doc_text)
        assertEquals(doc?.text, doc_text)
        assertEquals(doc?.tokens?.get(0)?.lemma , "how")
        assertEquals(doc?.tokens?.get(0)?.pos , "ADV")
    }

    @Test
    fun tagWithSentences() {
        val doc_text = "How are you Bob? What time is it in London?"

        val doc = cli.tagWithSentences(doc_text)
        assertEquals(doc?.text, doc_text)
        assertEquals(doc?.sents?.get(0)?.tokens?.get(0)?.lemma , "how")
        assertEquals(doc?.sents?.get(0)?.tokens?.get(0)?.pos , "ADV")
    }

    @Test
    fun nerWithSentences() {
        val doc_text = "How are you Bob? What time is it in London?"

        val doc = cli.nerWithSentences(doc_text)
        assertEquals(doc?.text, doc_text)
        assertEquals(doc?.sents?.get(0)?.ents?.get(0)?.label, "PERSON")
        assertEquals(doc?.sents?.get(0)?.ents?.get(0)?.text, "Bob")
        // First entity of second sentence is a geo
        assertEquals(doc?.sents?.get(1)?.ents?.get(0)?.label, "GPE")
    }

    @Test
    fun tag_2docs() {
        val v = cli.tag(listOf("Test1", "Test2"), batch = 10)
        assertEquals(v?.size, 2)
    }
    @Test
    fun bulk_tag() {

        val docs = (0..10_000).map { "Hello world $it !" }
        val miniTimeElapsed = measureTimeMillis {
            val v = cli.ping(docs, batch = 1000)
        }
        println("$miniTimeElapsed")
        val maxiTimeElapsed = measureTimeMillis {
            val v = cli.tag(docs, batch = 1000)
        }
        println("$maxiTimeElapsed")
        assertTrue(maxiTimeElapsed > miniTimeElapsed)
    }
}