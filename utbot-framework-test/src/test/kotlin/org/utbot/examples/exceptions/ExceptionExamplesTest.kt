package org.utbot.examples.exceptions

import org.utbot.framework.plugin.api.CodegenLanguage
import org.junit.jupiter.api.Test
import org.utbot.testcheckers.eq
import org.utbot.testcheckers.withoutConcrete
import org.utbot.testing.CodeGeneration
import org.utbot.testing.UtValueTestCaseChecker
import org.utbot.testing.atLeast
import org.utbot.testing.ignoreExecutionsNumber
import org.utbot.testing.isException

internal class ExceptionExamplesTest : UtValueTestCaseChecker(
    testClass = ExceptionExamples::class,
    testCodeGeneration = true,
    pipelines = listOf(
        TestLastStage(CodegenLanguage.JAVA),
        TestLastStage(CodegenLanguage.KOTLIN, CodeGeneration) // TODO: fails because we construct lists with generics
    )
) {
    @Test
    fun testInitAnArray() {
        check(
            ExceptionExamples::initAnArray,
            ignoreExecutionsNumber,
            { n, r -> n < 0 && r == -2 },
            { n, r -> n == 0 || n == 1 && r == -3 },
            { n, r -> n > 1 && r == 2 * n + 3 },
            coverage = atLeast(80)
        )
    }

    @Test
    fun testNestedExceptions() {
        check(
            ExceptionExamples::nestedExceptions,
            eq(3),
            { i, r -> i < 0 && r == -100 },
            { i, r -> i > 0 && r == 100 },
            { i, r -> i == 0 && r == 0 },
        )
    }

    @Test
    fun testDoNotCatchNested() {
        checkWithException(
            ExceptionExamples::doNotCatchNested,
            eq(3),
            { i, r -> i < 0 && r.isException<IllegalArgumentException>() },
            { i, r -> i > 0 && r.isException<NullPointerException>() },
            { i, r -> i == 0 && r.getOrThrow() == 0 },
        )
    }

    @Test
    fun testFinallyThrowing() {
        checkWithException(
            ExceptionExamples::finallyThrowing,
            eq(2),
            { i, r -> i <= 0 && r.isException<IllegalStateException>() },
            { i, r -> i > 0 && r.isException<IllegalStateException>() },
        )
    }

    @Test
    fun testFinallyChanging() {
        check(
            ExceptionExamples::finallyChanging,
            eq(2),
            { i, r -> i * 2 <= 0 && r == i * 2 + 10 },
            { i, r -> i * 2 > 0 && r == i * 2 + 110 },
            coverage = atLeast(80) // differs from JaCoCo
        )
    }

    @Test
    fun testThrowException() {
        checkWithException(
            ExceptionExamples::throwException,
            eq(2),
            { i, r -> i <= 0 && r.getOrNull() == 101 },
            { i, r -> i > 0 && r.isException<NullPointerException>() },
            coverage = atLeast(66) // because of unexpected exception thrown
        )
    }

    @Test
    fun testCreateException() {
            check(
                ExceptionExamples::createException,
                eq(1),
                { r -> r is java.lang.IllegalArgumentException },
            )
    }

    /**
     * Used for path generation check in [org.utbot.engine.Traverser.fullPath]
     */
    @Test
    fun testCatchDeepNestedThrow() {
        checkWithException(
            ExceptionExamples::catchDeepNestedThrow,
            eq(2),
            { i, r -> i < 0 && r.isException<NullPointerException>() },
            { i, r -> i >= 0 && r.getOrThrow() == i },
            coverage = atLeast(66) // because of unexpected exception thrown
        )
    }

    /**
     * Covers [#656](https://github.com/UnitTestBot/UTBotJava/issues/656).
     */
    @Test
    fun testCatchExceptionAfterOtherPossibleException() {
        withoutConcrete {
            checkWithException(
                ExceptionExamples::catchExceptionAfterOtherPossibleException,
                eq(3),
                { i, r -> i == -1 && r.isException<ArithmeticException>() },
                { i, r -> i == 0 && r.getOrThrow() == 2 },
                { i, r -> r.getOrThrow() == 1 },
                coverage = atLeast(100)
            )
        }
    }

    /**
     * Used for path generation check in [org.utbot.engine.Traverser.fullPath]
     */
    @Test
    fun testDontCatchDeepNestedThrow() {
        checkWithException(
            ExceptionExamples::dontCatchDeepNestedThrow,
            eq(2),
            { i, r -> i < 0 && r.isException<IllegalArgumentException>() },
            { i, r -> i >= 0 && r.getOrThrow() == i },
            coverage = atLeast(66) // because of unexpected exception thrown
        )
    }
}