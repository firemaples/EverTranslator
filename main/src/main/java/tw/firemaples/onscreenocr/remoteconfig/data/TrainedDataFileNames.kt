package tw.firemaples.onscreenocr.remoteconfig.data

data class TrainedDataFileNames(val default: Array<String>, val others: Map<String, Array<String>>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrainedDataFileNames

        if (!default.contentEquals(other.default)) return false
        if (others != other.others) return false

        return true
    }

    override fun hashCode(): Int {
        var result = default.contentHashCode()
        result = 31 * result + others.hashCode()
        return result
    }
}
