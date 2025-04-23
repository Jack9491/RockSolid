package ie.tus.rocksolid.utils

object PasswordValidator {
    fun isValid(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }

    fun getValidationState(password: String): Map<String, Boolean> {
        return mapOf(
            "At least 6 characters" to (password.length >= 6),
            "At least 1 uppercase letter" to password.any { it.isUpperCase() },
            "At least 1 number" to password.any { it.isDigit() }
        )
    }
}
