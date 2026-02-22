class Coverage (
    val instruction: Double,
    val branch: Double,
    val line: Double,
    val complexity: Double,
    val method: Double
) {
    fun decreasedFrom(other: Coverage): Boolean {
        return instruction < other.instruction ||
                branch < other.branch ||
                line < other.line ||
                complexity < other.complexity ||
                method < other.method
    }

    fun increasedFrom(other: Coverage): Boolean {
        if (decreasedFrom(other)) {
            return false
        }
        return instruction > other.instruction ||
                branch > other.branch ||
                line > other.line ||
                complexity > other.complexity ||
                method > other.method
    }

    override fun toString(): String {
        return "Coverage(instruction=$instruction, branch=$branch, line=$line, complexity=$complexity, method=$method)"
    }


}