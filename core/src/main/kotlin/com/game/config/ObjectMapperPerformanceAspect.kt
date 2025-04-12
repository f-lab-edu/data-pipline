package com.game.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Aspect
class ObjectMapperPerformanceAspect {

    private val logger = LoggerFactory.getLogger(ObjectMapperPerformanceAspect::class.java)

    @Around(
        "execution(* com.fasterxml.jackson.databind.ObjectMapper.writeValueAsString(..)) || " +
                "execution(* com.fasterxml.jackson.databind.ObjectMapper.writeValueAsBytes(..))"
    )
    fun measureSerializationPerformance(joinPoint: ProceedingJoinPoint): Any {
        val start = System.nanoTime()
        val result = joinPoint.proceed()
        val durationMs = (System.nanoTime() - start) / 1_000_000.0
        logger.info("Serialization [{}] took {} ms", joinPoint.signature.name, durationMs)
        return result
    }

    @Around(
        "execution(* com.fasterxml.jackson.databind.ObjectMapper.readValue(..)) || " +
                "execution(* com.fasterxml.jackson.databind.ObjectMapper.readTree(..)) || " +
                "execution(* com.fasterxml.jackson.databind.ObjectMapper.convertValue(..))"
    )
    fun measureDeserializationPerformance(joinPoint: ProceedingJoinPoint): Any {
        val start = System.nanoTime()
        val result = joinPoint.proceed()
        val durationMs = (System.nanoTime() - start) / 1_000_000.0
        logger.info("Deserialization [{}] took {} ms", joinPoint.signature.name, durationMs)
        return result
    }

    @Around("execution(* com.fasterxml.jackson.databind.ObjectReader.readValue(byte[]))")
    fun measureKafkaDeserialization(joinPoint: ProceedingJoinPoint): Any {
        val start = System.nanoTime()
        val result = joinPoint.proceed()
        val durationMs = (System.nanoTime() - start) / 1_000_000.0
        logger.info(
            "Consumer Deserialization [{}] took {} ms [type={}]",
            joinPoint.signature.name,
            durationMs,
            result?.javaClass?.simpleName ?: "unknown"
        )
        return result
    }
}