package com.jc

import com.jc.querydsl.QuerydslJpaRepositoryFactoryBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(repositoryFactoryBeanClass = QuerydslJpaRepositoryFactoryBean::class)
@SpringBootApplication
@EnableJpaAuditing
class EntryMain

fun main(args: Array<String>) {
    runApplication<EntryMain>(*args)
}
