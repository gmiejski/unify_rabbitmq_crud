package org.miejski.questions.domain

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "questions.kafka")
@Component
class QuestionServiceSettings {

    var applicationID: String = ""
    var bootstrapServers: String = ""


}