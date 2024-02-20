package com.yazid.walk.view.googleSignin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yazid.walk.R
import com.yazid.walk.view.Map.Map
import com.yazid.walk.view.googleSignin.ui.theme.WalkTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalkTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black

                ) {
                    val context = LocalContext.current

                    Column(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .fillMaxHeight(),

                            verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Text(text = "WALK",
                            color = Color.White,
                            fontSize = 30.sp
                            )
                        Button(
                            shape =RoundedCornerShape(8.dp),
                            onClick = {
                                context.startActivity(Intent(context,Map::class.java))



                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            , modifier = Modifier
                                .border(BorderStroke(1.dp, Color.White),shape = RoundedCornerShape(8.dp))



                        ) {
                            Icon(painter = painterResource(id = R.drawable.icons8_google), contentDescription ="", tint = Color.White )
                            
                        }


                    }

                }
            }
        }
    }
}


