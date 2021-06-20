enum class Direction {
    In,
    Out,
    Unknown
}

enum class Currency {

    Unknown { override fun toString(): String { return "未知" } },
    CNY {override fun toString(): String { return "人民币" }},
    USD {override fun toString(): String { return "美元" } },
    HKD {override fun toString(): String { return "港币" } },
    MOP {override fun toString(): String { return "澳门币" }}
}

