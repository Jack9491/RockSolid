package ie.tus.rocksolid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ie.tus.rocksolid.navigation.BuildNavigationGraph
import ie.tus.rocksolid.ui.theme.RockSolidTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RockSolidTheme{
                BuildNavigationGraph()
            }
        }
    }
}
