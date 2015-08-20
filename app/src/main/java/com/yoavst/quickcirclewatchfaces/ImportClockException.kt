package com.yoavst.quickcirclewatchfaces

class ImportClockException(public val error: ImportClockException.Error) : RuntimeException() {
    public enum class Error {
        NO_CLOCK_XML,
        INVALID_CLOCK_XML,
        READ_ERROR,
        MISSING_FILE
    }

    public val errorMessage: String
        get() {
            when (error) {
                ImportClockException.Error.NO_CLOCK_XML -> return "No clock.xml found"
                ImportClockException.Error.INVALID_CLOCK_XML -> return "clock.xml is not valid"
                ImportClockException.Error.READ_ERROR -> return "read error"
                ImportClockException.Error.MISSING_FILE -> return "a file is missing"
                else -> return getMessage().orEmpty()
            }
        }
}