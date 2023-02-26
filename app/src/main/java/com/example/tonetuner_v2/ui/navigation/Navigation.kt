package com.example.tonetuner_v2.ui.navigation
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import com.example.tonetuner_v2.app.AppModel
//import com.example.tonetuner_v2.app.MainViewModel
//
//@Composable
//fun Navigation(viewModel: MainViewModel) {
//    val navController = rememberNavController()
//    NavHost(
//        navController = navController,
//        startDestination = Screen.MainScreen.route
//    ) {
//        composable(route = Screen.MainScreen.route) {
//            MainScreen(
//                modifier = Modifier.fillMaxSize(),
//                navController = navController,
//                color = Color.Green,
//                spectrumType = viewModel.spectrumType
//            )
//        }
//        composable(
//            route = Screen.DetailScreen.route + "/{name}",
//            arguments = listOf(
//                navArgument("name") {
//                    type = NavType.StringType
//                    defaultValue = "World"
//                    nullable = true
//                }
//            )
//        ) { entry ->
//            SettingsScreen(
//                name = entry.arguments?.getString("name"),
//                navController = navController
//            )
//        }
//    }
//}
