package me.developes.humming.sdui.studio

class OrderKeyException(val code: String, message: String) : IllegalArgumentException(message)

/** Ordinal base-62 ordering. No locale, sibling renumbering or automatic rebalance. */
fun generateKeyBetween(previous: String?, next: String?, maxLength: Int = 256): String {
    require(maxLength > 0)
    if (previous != null && !validOrderKey(previous, maxLength) || next != null && !validOrderKey(next, maxLength))
        throw OrderKeyException("INVALID_ORDER_KEY", "Neighbor order keys are invalid")
    if (previous != null && next != null && previous >= next)
        throw OrderKeyException("INVALID_KEY_NEIGHBORS", "Previous key must be less than next key")
    fun midpoint(lower: String, upper: String?, remaining: Int): String {
        if (remaining <= 0) throw OrderKeyException("KEY_SPACE_EXHAUSTED", "Key length budget exhausted; explicit rebalance is required")
        if (upper != null) {
            var common = 0
            while (common < upper.length && (lower.getOrNull(common) ?: '0') == upper[common]) common++
            if (common > 0) return upper.take(common) + midpoint(lower.drop(common), upper.drop(common), remaining - common)
        }
        val low = lower.firstOrNull()?.let { ORDER_DIGITS.indexOf(it) } ?: 0
        val high = upper?.firstOrNull()?.let { ORDER_DIGITS.indexOf(it) } ?: ORDER_DIGITS.length
        if (high - low > 1) return ORDER_DIGITS[(low + high) / 2].toString()
        if (upper != null && upper.length > 1) return upper[0].toString()
        return ORDER_DIGITS[low] + midpoint(lower.drop(1), null, remaining - 1)
    }
    return midpoint(previous ?: "", next, maxLength)
}
