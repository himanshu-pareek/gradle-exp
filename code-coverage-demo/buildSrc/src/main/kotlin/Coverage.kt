class Coverage (
    val instruction: Double,
    val branch: Double,
    val line: Double,
    val complexity: Double,
    val method: Double
) {
    fun decreasedFrom(other: Coverage): Boolean {
        return lessThanWithDelta(instruction, other.instruction) ||
                lessThanWithDelta(branch, other.branch) ||
                lessThanWithDelta(line, other.line) ||
                lessThanWithDelta(complexity, other.complexity) ||
                lessThanWithDelta(method, other.method)
    }

    fun increasedFrom(other: Coverage): Boolean {
        if (decreasedFrom(other)) {
            return false
        }
        return other.decreasedFrom(this)
    }

    override fun toString(): String {
        return "Coverage(instruction=$instruction, branch=$branch, line=$line, complexity=$complexity, method=$method)"
    }

    companion object {
        private const val DELTA = 0.1

        fun lessThanWithDelta(a: Double, b: Double): Boolean {
            return b - a > DELTA
        }
    }
}
