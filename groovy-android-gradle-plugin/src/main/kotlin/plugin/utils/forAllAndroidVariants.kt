package plugin.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project

internal fun Project.forAllAndroidVariants(action: (BaseVariant) -> Unit) {
    val androidExtension = this.extensions.getByName("android")
    when (androidExtension) {
        is AppExtension -> androidExtension.applicationVariants.all(action)
        is LibraryExtension -> androidExtension.libraryVariants.all(action)
        is TestExtension -> androidExtension.applicationVariants.all(action)
    }
    if (androidExtension is TestedExtension) {
        androidExtension.testVariants.all(action)
        androidExtension.unitTestVariants.all(action)
    }
}
