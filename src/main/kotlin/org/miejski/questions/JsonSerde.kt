package org.miejski.questions

import org.miejski.questions.events.QuestionModifier
import org.miejski.questions.source.MultiSourceEventProducer
import org.miejski.questions.source.RandomQuestionIDProvider
import org.miejski.questions.source.create.SourceQuestionCreateProducer
import org.miejski.questions.source.create.SourceQuestionCreated
import org.miejski.questions.source.delete.SourceQuestionDeletedProducer
import org.miejski.questions.source.update.SourceQuestionUpdatedProducer
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


class JsonSerde {

    val dumpPath = "src/integration/events"

    fun dumpAllEvents(events: Map<String, List<QuestionModifier>>, override: Boolean = false) {
        events.forEach { eventName, modifiers -> this.dump(eventName, modifiers, override) }
    }

    private fun dump(eventName: String, modifiers: List<QuestionModifier>, override: Boolean) {
        Files.createDirectories(Paths.get(dumpPath))
        if (!override && Files.exists(Paths.get(dumpPath, eventName))) {
            println("Not overwriting $dumpPath/$eventName files, as they already exist")
            return
        }
        println("Saving events to $dumpPath/$eventName")
        val objectMapper = QuestionObjectMapper.build()
        val path = Paths.get(dumpPath, eventName)

        var fileWriter: FileWriter? = null

        try {
            fileWriter = FileWriter(path.toFile())
            for (modifier in modifiers) {
                fileWriter.write(objectMapper.writeValueAsString(modifier) + "\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun readEvents(eventName: String, target: Class<out QuestionModifier>): List<QuestionModifier> {
        val objectMapper = QuestionObjectMapper.build()
        val allLines = Files.readAllLines(Paths.get(dumpPath, eventName))
        return allLines.map { objectMapper.readValue(it, target) }
    }
}

fun main(args: Array<String>) {
    val eventsCount = 1000

    val idProvider = RandomQuestionIDProvider(1500)
    val questionCreateProducer = MultiSourceEventProducer(SourceQuestionCreateProducer("us", idProvider))
    val questionUpdatedProducer = MultiSourceEventProducer(SourceQuestionUpdatedProducer("us", idProvider))
    val questionDeletedProducer = MultiSourceEventProducer(SourceQuestionDeletedProducer("us", idProvider))

    JsonSerde().dumpAllEvents(mapOf(
        Pair("create", questionCreateProducer.create(eventsCount, { x -> x.ID() })),
        Pair("update", questionUpdatedProducer.create(eventsCount)),
        Pair("delete", questionDeletedProducer.create(eventsCount))),
        false)

    val read = JsonSerde().readEvents("create", SourceQuestionCreated::class.java)

    if (read.size != eventsCount) {
        throw RuntimeException()
    }
}
