package com.nexus.app.data

object AutoLinker {

    private val STOP_WORDS = setOf(

        "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por", "un", "para", "con",
        "no", "una", "su", "al", "lo", "como", "más", "mas", "pero", "sus", "le", "ya", "o", "este", "sí",
        "porque", "esta", "entre", "cuando", "muy", "sin", "sobre", "también", "me", "hasta", "hay",
        "donde", "quien", "desde", "todo", "nos", "durante", "todos", "uno", "les", "ni", "contra",
        "otros", "ese", "eso", "ante", "ellos", "e", "esto", "mí", "antes", "algunos", "qué", "unos",
        "yo", "otro", "otras", "otra", "él", "tanto", "esa", "estos", "mucho", "quienes", "nada",
        "muchos", "cual", "poco", "ella", "estar", "estas", "algunas", "algo", "nosotros", "mi", "mis",
        "tú", "te", "ti", "tu", "tus", "solo", "cada", "son", "fue", "han", "ser", "están", "es", "unas",

        "the", "a", "an", "and", "or", "in", "on", "at", "to", "for", "of", "with", "is", "are",
        "was", "were", "it", "this", "that", "be", "from", "by", "as", "about", "into", "through"
    )

    fun areNotesRelated(noteA: Note, noteB: Note): Boolean {
        if (noteA.id == noteB.id) return false

        val fullTextA = normalize("${noteA.title} ${noteA.content}")
        val fullTextB = normalize("${noteB.title} ${noteB.content}")

        val titleWordsA = extractKeywords(noteA.title)
        val titleWordsB = extractKeywords(noteB.title)

        // 1. Título o palabras del título que coincidan en otra nota
        for (word in titleWordsA) {
            if (word.length >= 2 && (fullTextB.contains(word) || titleWordsB.contains(word))) {
                return true
            }
        }
        for (word in titleWordsB) {
            if (word.length >= 2 && (fullTextA.contains(word) || titleWordsA.contains(word))) {
                return true
            }
        }

        // 2. Coincidencia de acrónimos o abreviaturas
        if (isAcronymMatch(noteA.title, fullTextB) || isAcronymMatch(noteB.title, fullTextA)) {
            return true
        }

        // 3. Palabras clave
        val keywordsA = extractKeywords("${noteA.title} ${noteA.content}")
        val keywordsB = extractKeywords("${noteB.title} ${noteB.content}")

        val commonKeywords = keywordsA.intersect(keywordsB)

        if (commonKeywords.isNotEmpty()) {
            return true
        }

        // 4. Puntuación de similitud
        val unionKeywords = keywordsA.union(keywordsB)
        if (unionKeywords.isNotEmpty()) {
            val jaccardScore = commonKeywords.size.toDouble() / unionKeywords.size.toDouble()
            if (jaccardScore >= 0.10 && commonKeywords.isNotEmpty()) return true
        }

        return false
    }

    private fun isAcronymMatch(shortTitle: String, targetText: String): Boolean {
        val cleanShort = shortTitle.trim().lowercase()
        if (cleanShort.length in 2..4) {
            if (cleanShort == "ia" && targetText.contains("inteligencia artificial")) return true
            if (cleanShort == "ml" && targetText.contains("machine learning")) return true
            if (cleanShort == "ui" && (targetText.contains("interface") || targetText.contains("interfaz"))) return true
            if (cleanShort == "ux" && targetText.contains("experiencia")) return true
        }
        return false
    }

    private fun extractKeywords(text: String): Set<String> {
        return normalize(text)
            .split(Regex("[^\\p{L}\\p{Nd}#]+"))
            .filter { word -> word.length >= 2 && !STOP_WORDS.contains(word) }
            .toSet()
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
            .replace('ñ', 'n')
    }
}
