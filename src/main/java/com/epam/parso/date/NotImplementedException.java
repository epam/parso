package com.epam.parso.date;

/**
 * Declared but not yet implemented formats throw this type of exception.
 * This exception is suppose to be caught internally to substitute
 * not implemented with on of fallback date format.
 */
class NotImplementedException extends RuntimeException {
}
