package org.oterrier.gracyql

data class SpacyData(
        val nlp: SpacyNlp? = null
)

data class SpacyNlp(
        val meta: ModelMeta? = null,
        val doc: SpacyDoc? = null,
        val batch : Batch? = null
)

data class ModelMeta(
        val author: String? = null,
        val description: String? = null,
        val lang: String? = null,
        val license : String? = null,
        val name: String? = null,
        val pipeline: List<String>? = null,
        val sources: List<String>? = null,
        val spacy_version: String? = null,
        val version: String? = null
)

data class Batch(
    val batch_id : String?= null,
    val docs: List<SpacyDoc?>? = null
)

data class SpacyDoc(
        val text: String = "",
        val has_vector: Boolean? = null,
        val vector: Array<Float>? = null,
        val sentiment: Float? = null,
        val tokens: List<SpacyToken>? = null,
        val sents: List<SpacySpan>? = null,
        val ents: List<SpacySpan>? = null,
        val noun_chunks: List<SpacySpan>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpacyDoc

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "SpacyDoc(text='$text')"
    }
}

data class SpacySpan(
        val start: Int = 0,
        val end: Int = 0,
        val text: String? = null,
        val label: String? = null,
        val lemma: String? = null,
        val has_vector: Boolean? = null,
        val vector: Array<Float>? = null,
        val sentiment: Float? = null,
        // Span references
        val ents: Array<SpacySpan>? = null,
        // Token references
        val root: SpacyToken? = null,
        val tokens: List<SpacyToken>? = null,
        val conjuncts: List<SpacyToken>? = null,
        val subtree: List<SpacyToken>? = null,
        val rights: List<SpacyToken>? = null,
        val lefts: List<SpacyToken>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpacySpan

        if (start != other.start) return false
        if (end != other.end) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        result = 31 * result + (label?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SpacySpan(start=$start, end=$end, text=$text, label=$label)"
    }
}

data class SpacyToken(
        val id: Int = 0,
        val start: Int = 0,
        val end: Int = 0,
        val text: String? = null,
        val orth: String? = null,
        val pos: String? = null,
        val tag: String? = null,
        val lemma: String? = null,
        val text_with_ws: String? = null,
        val whitespace: String? = null,
        val has_vector: Boolean? = null,
        val vector: Array<Float>? = null,
        val ent_type: String? = null,
        val ent_iob: String? = null,
        val norm: String? = null,
        val lower: String? = null,
        val shape: String? = null,
        val prefix: String? = null,
        val suffix: String? = null,
        val is_sent_start: Boolean? = null,
        val is_alpha: Boolean? = null,
        val is_ascii: Boolean? = null,
        val is_digit: Boolean? = null,
        val is_lower: Boolean? = null,
        val is_upper: Boolean? = null,
        val is_title: Boolean? = null,
        val is_punct: Boolean? = null,
        val is_left_punct: Boolean? = null,
        val is_right_punct: Boolean? = null,
        val is_space: Boolean? = null,
        val is_bracket: Boolean? = null,
        val is_quote: Boolean? = null,
        val is_currency: Boolean? = null,
        val like_url: Boolean? = null,
        val like_num: Boolean? = null,
        val like_email: Boolean? = null,
        val is_oov: Boolean? = null,
        val is_stop: Boolean? = null,
        val dep: String? = null,
        val lang: String? = null,
        val prob: Float? = null,
        val sentiment: Float? = null,
        val cluster: Int? = null,
        // Token references
        val head: SpacyToken? = null,
        val left_edge: SpacyToken? = null,
        val right_edge: SpacyToken? = null,
        val children: List<SpacyToken>? = null,
        val ancestors: List<SpacyToken>? = null,
        val conjuncts: List<SpacyToken>? = null,
        val subtree: List<SpacyToken>? = null,
        val rights: List<SpacyToken>? = null,
        val lefts: List<SpacyToken>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpacyToken

        if (id != other.id) return false
        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + start
        result = 31 * result + end
        return result
    }

    override fun toString(): String {
        return "SpacyToken(id=$id, start=$start, end=$end, text=$text)"
    }
}