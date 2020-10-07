package com.jc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class EntryMain

fun main(args: Array<String>) {
    runApplication<EntryMain>(*args)
}
