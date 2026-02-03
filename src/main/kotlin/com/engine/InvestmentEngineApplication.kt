package com.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InvestmentEngineApplication

fun main(args: Array<String>) {
    runApplication<InvestmentEngineApplication>(*args)
}
