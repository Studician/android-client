package app.milanherke.mystudiez

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Simple class that holds frequently used functions in a companion object
 * regarding Firebase.
 */
class FirebaseUtils() {

    companion object {

        // Sign in request code
        const val RC_SIGN_IN = 1001

        /**
         * Checks if user is logged in.
         *
         * @return true if logged in, otherwise false
         */
        fun userIsLoggedIn(): Boolean {
            val user = FirebaseAuth.getInstance().currentUser
            return user != null
        }

        /**
         * Gets the id of user.
         *
         * @return id of user
         * @throws IllegalStateException if the user is not logged in
         */
        fun getUserId(): String {
            val user = FirebaseAuth.getInstance().currentUser
            return user?.uid
                ?: throw IllegalStateException("User is not logged in, this function mustn't be called without logging in first")
        }

    }
}