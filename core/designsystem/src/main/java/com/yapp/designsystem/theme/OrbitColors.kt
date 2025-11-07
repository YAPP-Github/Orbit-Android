package com.yapp.designsystem.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
class OrbitColors(
    main: Color = Color(0xFFFEFF65),
    sub_main: Color = Color(0xFFFDFE90),
    white: Color = Color(0xFFFFFFFF),
    gray_50: Color = Color(0xFFE6EDF8),
    gray_100: Color = Color(0xFFD7E1EE),
    gray_200: Color = Color(0xFFC8D3E3),
    gray_300: Color = Color(0xFFA5B2C5),
    gray_400: Color = Color(0xFF7B8696),
    gray_500: Color = Color(0xFF5D6470),
    gray_600: Color = Color(0xFF3D424B),
    gray_700: Color = Color(0xFF2A2F38),
    gray_800: Color = Color(0xFF1F2127),
    gray_900: Color = Color(0xFF17191F),
    alert: Color = Color(0xFFF2544A),
    alert_pressed: Color = Color(0xFFE53D33),
    success: Color = Color(0xFF22C55E),

    red: Color = Color(0xFFEF4444),
    pink: Color = Color(0xFFE682D7),
    babypink: Color = Color(0xFFFFA9A9),
    orange: Color = Color(0xFFFB923C),
    yellow: Color = Color(0xFFFDE047),
    green: Color = Color(0xFF4ADE80),
    blue: Color = Color(0xFF2563EB),
    blue_2: Color = Color(0xFF69A8F6),
    blue_3: Color = Color(0xFF7EA9DE),
    purple: Color = Color(0xFF735AFF),
    brown: Color = Color(0xFFCA8A04),
    gray: Color = Color(0xFF9DA7AE),
    indigo: Color = Color(0xFF12304C),

) {
    var main by mutableStateOf(main)
        private set
    var sub_main by mutableStateOf(sub_main)
        private set
    var white by mutableStateOf(white)
        private set
    var gray_50 by mutableStateOf(gray_50)
        private set
    var gray_100 by mutableStateOf(gray_100)
        private set
    var gray_200 by mutableStateOf(gray_200)
        private set
    var gray_300 by mutableStateOf(gray_300)
        private set
    var gray_400 by mutableStateOf(gray_400)
        private set
    var gray_500 by mutableStateOf(gray_500)
        private set
    var gray_600 by mutableStateOf(gray_600)
        private set
    var gray_700 by mutableStateOf(gray_700)
        private set
    var gray_800 by mutableStateOf(gray_800)
        private set
    var gray_900 by mutableStateOf(gray_900)
        private set
    var alert by mutableStateOf(alert)
        private set
    var alert_pressed by mutableStateOf(alert_pressed)
        private set
    var success by mutableStateOf(success)
        private set
    var red by mutableStateOf(red)
        private set
    var pink by mutableStateOf(pink)
        private set
    var babypink by mutableStateOf(babypink)
        private set
    var orange by mutableStateOf(orange)
        private set
    var yellow by mutableStateOf(yellow)
        private set
    var green by mutableStateOf(green)
        private set
    var blue by mutableStateOf(blue)
        private set
    var blue_2 by mutableStateOf(blue_2)
        private set
    var blue_3 by mutableStateOf(blue_3)
        private set
    var purple by mutableStateOf(purple)
        private set
    var brown by mutableStateOf(brown)
        private set
    var gray by mutableStateOf(gray)
        private set
    var indigo by mutableStateOf(indigo)
        private set

    fun copy(
        main: Color = this.main,
        sub_main: Color = this.sub_main,
        white: Color = this.white,
        gray_50: Color = this.gray_50,
        gray_100: Color = this.gray_100,
        gray_200: Color = this.gray_200,
        gray_300: Color = this.gray_300,
        gray_400: Color = this.gray_400,
        gray_500: Color = this.gray_500,
        gray_600: Color = this.gray_600,
        gray_700: Color = this.gray_700,
        gray_800: Color = this.gray_800,
        gray_900: Color = this.gray_900,
        alert: Color = this.alert,
        alert_pressed: Color = this.alert_pressed,
        success: Color = this.success,
        red: Color = this.red,
        pink: Color = this.pink,
        babypink: Color = this.babypink,
        orange: Color = this.orange,
        yellow: Color = this.yellow,
        green: Color = this.green,
        blue: Color = this.blue,
        blue_2: Color = this.blue_2,
        blue_3: Color = this.blue_3,
        purple: Color = this.purple,
        brown: Color = this.brown,
        gray: Color = this.gray,
        indigo: Color = this.indigo,
    ) = OrbitColors(
        main = main,
        sub_main = sub_main,
        white = white,
        gray_50 = gray_50,
        gray_100 = gray_100,
        gray_200 = gray_200,
        gray_300 = gray_300,
        gray_400 = gray_400,
        gray_500 = gray_500,
        gray_600 = gray_600,
        gray_700 = gray_700,
        gray_800 = gray_800,
        gray_900 = gray_900,
        alert = alert,
        alert_pressed = alert_pressed,
        success = success,
        red = red,
        pink = pink,
        babypink = babypink,
        orange = orange,
        yellow = yellow,
        green = green,
        blue = blue,
        blue_2 = blue_2,
        blue_3 = blue_3,
        purple = purple,
        brown = brown,
        gray = gray,
        indigo = indigo,
    )

    fun updateColorFrom(other: OrbitColors) {
        main = other.main
        sub_main = other.sub_main
        white = other.white
        gray_50 = other.gray_50
        gray_100 = other.gray_100
        gray_200 = other.gray_200
        gray_300 = other.gray_300
        gray_400 = other.gray_400
        gray_500 = other.gray_500
        gray_600 = other.gray_600
        gray_700 = other.gray_700
        gray_800 = other.gray_800
        gray_900 = other.gray_900
        alert = other.alert
        alert_pressed = other.alert_pressed
        success = other.success
        red = other.red
        pink = other.pink
        babypink = other.babypink
        orange = other.orange
        yellow = other.yellow
        green = other.green
        blue = other.blue
        blue_2 = other.blue_2
        blue_3 = other.blue_3
        purple = other.purple
        brown = other.brown
        gray = other.gray
        indigo = other.indigo
    }
}

val LocalColors = staticCompositionLocalOf { OrbitColors() }
