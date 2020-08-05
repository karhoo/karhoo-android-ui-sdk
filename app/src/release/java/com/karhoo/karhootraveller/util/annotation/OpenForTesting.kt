package com.karhoo.karhootraveller.util.annotation

/**
 * Annotate a class with [OpenForTesting] if you want it to be extendable in debug builds.
 */
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting