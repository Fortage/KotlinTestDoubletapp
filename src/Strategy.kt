import kotlin.math.*

fun main (){
    Strategy.processInput()
}

class Strategy {
    companion object {
        fun processInput() {
            val state = SystemState()
            val stateParams: MutableMap<ParamType, String> = mutableMapOf()

            for (i in 0 until 6) {
                val paramArr = readLine()!!.split(" ".toRegex()).toTypedArray()
                stateParams[ParamType[paramArr[0]]] = paramArr[1]
            }

            state.params = stateParams

            val pushQuantity = readLine()!!.toInt()
            val processor = PushProcessor()
            val validPushes: MutableList<Push> = (0 until pushQuantity).map { readPush() }
                .filter { push -> processor.validatePush(push, state) }
                .toMutableList()

            if (validPushes.isEmpty()) {
                println(-1)
            } else {
                validPushes.forEach { push: Push -> println(push.text) }
            }
        }

        /**
         * Считывание Пушей
         * @return Объект типа push
         */
        private fun readPush(): Push {
            val push = Push()
            val params: MutableMap<ParamType, String> = mutableMapOf()
            val paramsQuantity = Integer.parseInt(readLine())

            push.type = PushType.valueOf(readLine()!!.split(" ")[1])
            push.text = (readLine()!!.split(" ")[1])

            for (i in 2 until paramsQuantity) {
                val paramArr = readLine()!!.split(" ".toRegex()).toTypedArray()
                params[ParamType[paramArr[0]]] = paramArr[1]
            }

            push.params = params

            return push
        }
    }

    /**
     * Класс для проверки валидности пушей.
     */
    class PushProcessor {
        companion object {
            private val AGE_PREDICATE  =
                { push : Push, state : SystemState -> (push.params[ParamType.AGE]
                    ?: error("null push AGE")).toInt() <= (state.params[ParamType.AGE]
                    ?: error("null State AGE")).toInt() }

            private val LOCATION_PREDICATE =
                { push : Push, state : SystemState ->
                    val x : Float = (push.params[ParamType.X_COORD] ?: error("push x_coord null")).toFloat()
                    val stateX : Float = (state.params[ParamType.X_COORD] ?: error("state x_coord null")).toFloat()
                    val y : Float  = (push.params[ParamType.Y_COORD] ?: error("push y_coord null")).toFloat()
                    val stateY : Float = (state.params[ParamType.Y_COORD] ?: error("state y_coord null")).toFloat()
                    val radius : Int = (push.params[ParamType.RADIUS] ?: error("push radius null")).toInt()
                    sqrt((stateX - x).pow(2) + (stateY - y).pow(2)) <= radius
                }

            private val GENDER_PREDICATE =
                { push : Push, state : SystemState -> (state.params[ParamType.GENDER]
                    ?: error("state null gender"))[0] == (push.params[ParamType.GENDER]
                    ?: error("push null Gender"))[0] }

            private val  TECH_PREDICATE =
                { push : Push, state : SystemState -> (push.params[ParamType.OS_VERSION]
                    ?: error("push OS NULL")).toInt() >= (state.params[ParamType.OS_VERSION]
                    ?: error("push OS NULL")).toInt() }

            private val TIME_PREDICATE =
                { push : Push, state : SystemState -> (push.params[ParamType.EXPIRY_DATE]
                    ?: error("push Null Expiry_date")).toLong() >= (state.params[ParamType.TIME]
                    ?: error("state Null time")).toLong() }

            private val VALIDATORS_MAP : MutableMap<PushType, List<(Push, SystemState) -> Boolean>> = mutableMapOf(
                PushType.LocationPush to listOf(LOCATION_PREDICATE, TIME_PREDICATE)
                ,PushType.LocationAgePush to listOf(LOCATION_PREDICATE, AGE_PREDICATE)
                ,PushType.AgeSpecificPush to listOf(AGE_PREDICATE, TIME_PREDICATE)
                ,PushType.GenderPush to listOf(GENDER_PREDICATE)
                ,PushType.TechPush to listOf(TECH_PREDICATE)
                ,PushType.GenderAgePush to listOf(GENDER_PREDICATE, AGE_PREDICATE))
        }

        /**
         * Проивзодит проверку пушей по всем условиям для отсеивания не нужных.
         */
        fun validatePush(push : Push, systemState : SystemState ) : Boolean {
            return VALIDATORS_MAP.getOrDefault(push.type, listOf { _: Push, _: SystemState -> false })
                .all{ predicate -> predicate(push, systemState) }
        }
    }

    class SystemState {
        lateinit var params: Map<ParamType, String>
    }

    /**
     * Список типов пушей
     */
    enum class PushType {
        LocationPush,
        AgeSpecificPush,
        TechPush,
        LocationAgePush,
        GenderAgePush,
        GenderPush
    }

    /**
     * Список параметров пушей
     */
    enum class ParamType {
        TIME,
        GENDER,
        AGE,
        OS_VERSION,
        X_COORD,
        Y_COORD,
        RADIUS,
        EXPIRY_DATE;

        companion object {
            operator fun get(name: String): ParamType {
                return valueOf(name.toUpperCase())
            }
        }
    }

    class Push {
        lateinit var text : String
        lateinit var type : PushType
        lateinit var params : Map<ParamType, String>
    }
}