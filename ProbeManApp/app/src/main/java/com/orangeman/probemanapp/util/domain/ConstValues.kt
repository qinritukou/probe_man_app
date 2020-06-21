package com.orangeman.probemanapp.util.domain

object ConstValues {
    const val TAKE_PICTURE_ACTIVITY_RESULT = 1

    const val FaceMargin = 60
    const val ModelFileName = "probe_man.tflite"
    const val ObbFileName = "main.1.com.orangeman.probemanapp.obb"

    val LabelFileNames = arrayListOf(
        "drink.csv",
        "family_info.csv",
        "graduation.csv",
        "marriage_willness.csv",
        "salary.csv",
        "smoke.csv"
    )

    enum class LabelIndex(
        val value: Int
    ) {
        Drink(0),
        FamilyInfo(1),
        Graduation(2),
        MarriageWillness(3),
        Salary(4),
        Smoke(5),
    }
}
