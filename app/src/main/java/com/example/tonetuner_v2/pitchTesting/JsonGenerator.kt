package com.example.tonetuner_v2.pitchTesting

import com.example.signallib.enums.Note
import com.example.signallib.enums.Note.Companion.plus
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import java.io.File
import java.lang.StringBuilder
import kotlin.math.absoluteValue



// https://stackabuse.com/reading-and-writing-json-in-kotlin-with-jackson/
//fun Collection<PitchTestResults>.toJson(): String {
//    val sb = StringBuilder()
//    val jsonMapper = jacksonObjectMapper()
//    sb.append("{\n")
//    this.forEach {
//        sb.append(jsonMapper.writeValueAsString(it))
//        sb.append("\n")
//    }
//    sb.append("}")
//
//    return sb.toString()
//}



