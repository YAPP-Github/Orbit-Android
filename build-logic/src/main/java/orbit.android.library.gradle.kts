import com.yapp.convention.configureCoroutine
import com.yapp.convention.configureHiltAndroid
import com.yapp.convention.configureKotlinAndroid
import com.yapp.convention.configureTestAndroid
import com.yapp.convention.configureTestCoverage
import com.yapp.convention.configureTestKotlin

plugins {
    id("com.android.library")
}

configureKotlinAndroid()
configureCoroutine()
configureHiltAndroid()
configureTestAndroid()
configureTestKotlin()
configureTestCoverage()
