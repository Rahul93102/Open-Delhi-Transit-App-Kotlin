package com.opendelhitransit.metroapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetroApiApplication

fun main(args: Array<String>) {
    runApplication<MetroApiApplication>(*args)
} 