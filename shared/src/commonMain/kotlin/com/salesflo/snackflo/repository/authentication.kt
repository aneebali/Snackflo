package com.salesflo.snackflo.repository

import com.salesflo.snackflo.AppConstant
import com.salesflo.snackflo.showToast
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where

object AuthFunc {


    suspend fun createUser(
        username: String,
        mobileNo: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = Firebase.firestore
        try {
            val data = mapOf(
                AppConstant.usernameFieldName to username,
                AppConstant.mobileFieldName to mobileNo,
                AppConstant.passFieldName to password,
                AppConstant.userType to AppConstant.EMPLOYEE_TYPE
            )

            // Use mobileNo as document ID
            db.collection(AppConstant.AuthTableName)
                .document(mobileNo)
                .set(data)

            // Success callback
            showToast("Signup successful")
            onSuccess()

        } catch (e: Exception) {
            println("Account Not Created: $e")
            onFailure()
        }
    }

    suspend fun authenticateUser(password: String, mobileNo: String): AuthResult {
        val db = Firebase.firestore
        return try {
            val result = db.collection(AppConstant.AuthTableName)
                .where(AppConstant.mobileFieldName, "==", mobileNo)
                .where(AppConstant.passFieldName, "==", password)
                .get()

            if (result.documents.isNotEmpty()) {
                val document = result.documents[0]
                val userType: String? = document.get(AppConstant.userType)
                val username: String? = document.get(AppConstant.usernameFieldName)
                AuthResult(
                    success = true,
                    userType = userType,
                    userId = document.id,
                    username = username
                )
            } else {
                AuthResult(success = false)
            }

        } catch (e: Exception) {
            println("Login error: $e")
            AuthResult(success = false)
        }
    }

    suspend fun isMobileAlreadyRegistered(
        username: String,
        mobileNo: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = Firebase.firestore
        try {
            val result = db.collection(AppConstant.AuthTableName)
                .where(AppConstant.mobileFieldName, "==", mobileNo)
                .get()

            if (result.documents.isEmpty()) {
                createUser(
                    username = username,
                    mobileNo = mobileNo,
                    password = password,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            } else {
                showToast("Mobile Number is already exist")

                onFailure()
            }
        } catch (e: Exception) {
            println("Error checking mobile number: $e")
            onFailure()
        }
    }
}
