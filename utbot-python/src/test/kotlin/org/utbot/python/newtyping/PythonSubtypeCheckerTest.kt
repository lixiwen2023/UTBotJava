package org.utbot.python.newtyping

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.utbot.python.newtyping.PythonSubtypeChecker.Companion.checkIfRightIsSubtypeOfLeft
import org.utbot.python.newtyping.ast.visitor.hints.createBinaryProtocol
import org.utbot.python.newtyping.general.DefaultSubstitutionProvider
import org.utbot.python.newtyping.general.FunctionType
import org.utbot.python.newtyping.general.TypeParameter
import org.utbot.python.newtyping.mypy.MypyAnnotationStorage
import org.utbot.python.newtyping.mypy.readMypyAnnotationStorage

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PythonSubtypeCheckerTest {
    lateinit var storage: MypyAnnotationStorage
    lateinit var pythonTypeStorage: PythonTypeStorage
    @BeforeAll
    fun setup() {
        val sample = PythonSubtypeCheckerTest::class.java.getResource("/subtypes_sample.json")!!.readText()
        storage = readMypyAnnotationStorage(sample)
        pythonTypeStorage = PythonTypeStorage.get(storage)
    }

    @Test
    fun testCustomProtocol() {
        val protocolP = storage.definitions["subtypes"]!!["P"]!!.getUtBotType()
        assertTrue(protocolP.pythonDescription() is PythonProtocolDescription)
        val classS = storage.definitions["subtypes"]!!["S"]!!.getUtBotType()
        assertTrue(classS.pythonDescription() is PythonConcreteCompositeTypeDescription)
        val classS1 = storage.definitions["subtypes"]!!["S1"]!!.getUtBotType()
        assertTrue(classS1.pythonDescription() is PythonConcreteCompositeTypeDescription)
        assertTrue(checkIfRightIsSubtypeOfLeft(protocolP, classS, pythonTypeStorage))
        assertFalse(checkIfRightIsSubtypeOfLeft(protocolP, classS1, pythonTypeStorage))
    }

    @Test
    fun testCustomProtocolWithPossibleInfiniteRecursion() {
        val protocolR = storage.definitions["subtypes"]!!["R"]!!.getUtBotType()
        assertTrue(protocolR.pythonDescription() is PythonProtocolDescription)
        val classRImpl = storage.definitions["subtypes"]!!["RImpl"]!!.getUtBotType()
        assertTrue(classRImpl.pythonDescription() is PythonConcreteCompositeTypeDescription)
        assertTrue(checkIfRightIsSubtypeOfLeft(protocolR, classRImpl, pythonTypeStorage))
    }

    @Test
    fun testVariance() {
        val list = storage.definitions["builtins"]!!["list"]!!.getUtBotType()
        val frozenset = storage.definitions["builtins"]!!["frozenset"]!!.getUtBotType()
        val int = storage.definitions["builtins"]!!["int"]!!.getUtBotType()
        val obj = storage.definitions["builtins"]!!["object"]!!.getUtBotType()
        val listOfInt = DefaultSubstitutionProvider.substitute(list,
            mapOf(list.parameters.first() as TypeParameter to int)
        )
        val listOfObj = DefaultSubstitutionProvider.substitute(list,
            mapOf(list.parameters.first() as TypeParameter to obj)
        )
        val listOfAny = DefaultSubstitutionProvider.substitute(list,
            mapOf(list.parameters.first() as TypeParameter to pythonAnyType)
        )
        val frozensetOfInt = DefaultSubstitutionProvider.substitute(frozenset,
            mapOf(frozenset.parameters.first() as TypeParameter to int)
        )
        val frozensetOfObj = DefaultSubstitutionProvider.substitute(frozenset,
            mapOf(frozenset.parameters.first() as TypeParameter to obj)
        )

        // list is invariant
        assertFalse(checkIfRightIsSubtypeOfLeft(listOfObj, listOfInt, pythonTypeStorage))
        assertTrue(checkIfRightIsSubtypeOfLeft(listOfAny, listOfInt, pythonTypeStorage))
        assertTrue(checkIfRightIsSubtypeOfLeft(listOfInt, listOfAny, pythonTypeStorage))

        // frozenset is covariant
        assertTrue(checkIfRightIsSubtypeOfLeft(frozensetOfObj, frozensetOfInt, pythonTypeStorage))
        assertFalse(checkIfRightIsSubtypeOfLeft(frozensetOfInt, frozensetOfObj, pythonTypeStorage))
    }

    @Test
    fun testSimpleFunctionWithVariables() {
        val b = storage.definitions["subtypes"]!!["b"]!!.getUtBotType()
        val func = storage.definitions["subtypes"]!!["func_abs"]!!.getUtBotType() as FunctionType

        assertTrue(checkIfRightIsSubtypeOfLeft(func.arguments[0], b, pythonTypeStorage))
    }

    @Test
    fun testSyntheticProtocol() {
        val getItemProtocol = createBinaryProtocol("__getitem__", pythonAnyType, pythonAnyType)
        val list = DefaultSubstitutionProvider.substituteAll(
            storage.definitions["builtins"]!!["list"]!!.getUtBotType(),
            listOf(pythonAnyType)
        )

        assertTrue(checkIfRightIsSubtypeOfLeft(getItemProtocol, list, pythonTypeStorage))
    }

    @Test
    fun testNumpyArray() {
        val numpyArray = storage.definitions["numpy"]!!["ndarray"]!!.getUtBotType()
        val numpyArrayOfAny = DefaultSubstitutionProvider.substituteAll(numpyArray, listOf(pythonAnyType, pythonAnyType))
        val getItemProtocol = createBinaryProtocol("__getitem__", pythonTypeStorage.tupleOfAny, pythonAnyType)

        assertTrue(checkIfRightIsSubtypeOfLeft(getItemProtocol, numpyArrayOfAny, pythonTypeStorage))
    }

    @Test
    fun testTuple() {
        val tuple = storage.definitions["builtins"]!!["tuple"]!!.getUtBotType()
        val tupleOfAny = DefaultSubstitutionProvider.substituteAll(tuple, listOf(pythonAnyType))
        val int = storage.definitions["builtins"]!!["int"]!!.getUtBotType()
        val float = storage.definitions["builtins"]!!["float"]!!.getUtBotType()
        val tupleOfInt = DefaultSubstitutionProvider.substituteAll(tuple, listOf(int))
        val tupleOfIntAndFloat = createPythonTupleType(listOf(int, float))

        assertTrue(checkIfRightIsSubtypeOfLeft(tupleOfAny, tupleOfIntAndFloat, pythonTypeStorage))
        assertFalse(checkIfRightIsSubtypeOfLeft(tupleOfInt, tupleOfIntAndFloat, pythonTypeStorage))
    }
}