package com.asimorphic.chirp

import com.asimorphic.chirp.infra.database.entities.UserEntity
import com.asimorphic.chirp.infra.database.repositories.UserRepository
import jakarta.annotation.PostConstruct
import org.aspectj.apache.bcel.Repository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ChirpApplication

fun main(args: Array<String>) {
	runApplication<ChirpApplication>(*args)
}