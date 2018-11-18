package org.miejski.questions.domain

class QuestionNotFound(market: String, id: Int) : RuntimeException("Not found: $market-$id")
