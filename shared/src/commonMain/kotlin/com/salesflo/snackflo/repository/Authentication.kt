package com.salesflo.snackflo.repository

import com.salesflo.snackflo.AppConstant
import com.salesflo.snackflo.showToast
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where

object AuthFunc {


    suspend fun sendDeviceInfo(deviceInfo : String, userId : String){
        val db = Firebase.firestore
        try {

            val docRef = db.collection(AppConstant.AuthTableName).document(userId)
            val snapshot = docRef.get()

            if (snapshot.exists) {
                docRef.update(AppConstant.DEVICE_INFO to deviceInfo)
            } else {
                docRef.set(mapOf(AppConstant.DEVICE_INFO to deviceInfo))
            }

        }catch (e: Exception){

        }
    }

    suspend fun forgetPassword(mobileNo: String , password : String) : Boolean{
        val db = Firebase.firestore
        try {

            val docRef = db.collection(AppConstant.AuthTableName).document(mobileNo)
            val snapshot = docRef.get()

            if (snapshot.exists) {
                docRef.update(AppConstant.passFieldName to password)
                return  true
            } else {
              return  false
            }

        }catch (e: Exception){
            return  false

        }
    }

    suspend fun createUser(
        username: String,
        mobileNo: String,
        password: String,
        deviceName: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = Firebase.firestore
        try {
            val data = mapOf(
                AppConstant.usernameFieldName to username,
                AppConstant.mobileFieldName to mobileNo,
                AppConstant.passFieldName to password,
                AppConstant.userType to AppConstant.EMPLOYEE_TYPE,
                AppConstant.DEVICE_INFO to deviceName
            )

            db.collection(AppConstant.AuthTableName)
                .document(mobileNo)
                .set(data)

            showToast("Signup successful")
            onSuccess()

        } catch (e: Exception) {
            println("Account Not Created: $e")
            onFailure()
        }
    }



    suspend fun authenticateUser(password: String, mobileNo: String , deviceName : String,): AuthResult {
        val db = Firebase.firestore
        return try {
            val result = db.collection(AppConstant.AuthTableName)
                .where("mobileNo", equalTo = mobileNo)
                .where("password", equalTo = password)
                .get()

            for (doc in result.documents) {
                println("Found: ${doc.id}, password:")
            }
            println("usertype")
            println(result)
            println( result.documents)


            if (result.documents.isNotEmpty()) {
                val document = result.documents[0]
                val userType: String? = document.get(AppConstant.userType)
                val username: String? = document.get(AppConstant.usernameFieldName)
                val isDeviceFlag: Int = document.get(AppConstant.DEVICE_INFO_Flag)
                println(username)
                println(userType)
                if(isDeviceFlag == 1){
                    sendDeviceInfo(deviceInfo  = deviceName , userId = document.id)
                }

                AuthResult(
                    success = true,
                    userType = userType,
                    userId = document.id,
                    username = username,
                    isDeviceInfo = isDeviceFlag
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
        deviceName : String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = Firebase.firestore
        try {
            val result = db.collection(AppConstant.AuthTableName)
                .where(AppConstant.mobileFieldName, equalTo =  mobileNo)
                .get()


            if (result.documents.isEmpty()) {
                createUser(
                    username = username,
                    mobileNo = mobileNo,
                    password = password,
                    deviceName = deviceName,
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
