package com.phauer.svgbuddy.processing.optimizing

/**
 * generating short class names
 */
class ShortCssClassNameGenerator {

    private var currentLetter: Char = 'a'
    private var currentNumber: Int? = null

    fun getNextClassName(): String {
        if (currentLetter > 'z') {
            // reset
            currentNumber = if (currentNumber == null) 0 else currentNumber!! + 1
            currentLetter = 'a'
        }

        val postfix = if (currentNumber == null) "" else "${currentNumber!!}"
        return "${currentLetter++}$postfix"
    }
}