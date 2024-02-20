package com.yazid.walkapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yazid.walkapp.ui.theme.WalkappTheme

class MainActivity : ComponentActivity() {

    lateinit var auth:FirebaseAuth
    lateinit var GoogleSigninClient: GoogleSignInClient
    public fun LoginAuthInit(){
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.GoogleAuthID))
            .requestEmail()
            .build()
        GoogleSigninClient= GoogleSignIn.getClient(this,gso)

        FirebaseApp.initializeApp(this)
    }
    public fun SigninWithGoogle(){
        val signinIntent = GoogleSigninClient.signInIntent
        launcher.launch(signinIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {result->
        if(result.resultCode == Activity.RESULT_OK){
            val Task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(Task)
        }


    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account:GoogleSignInAccount?=task.result
            if (account !=null){

                updateUi(account)
            }
        }else{
            Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show()

        }
    }

    private fun updateUi(account: GoogleSignInAccount) {
        val credintial = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credintial).addOnCompleteListener {it->
            if (it.isSuccessful){

                Toast.makeText(this, "login", Toast.LENGTH_SHORT).show()



            }else{
                Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show()

            }
        }
    }

    // on create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoginAuthInit()
        setContent {
            WalkappTheme {
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
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                SigninWithGoogle()



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

