package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallAPILoginAsyncTask("Jakub", "123456").execute()
    }

    private inner class CallAPILoginAsyncTask(val username: String, val password: String): AsyncTask<Any, Void, String>() {

        private lateinit var customProgressDialog : Dialog

        override fun onPreExecute() {
            super.onPreExecute()

            showCustomProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result : String

            var connection : HttpURLConnection? = null

            try{
                val url = URL("https://run.mocky.io/v3/74bcfd75-d0ea-48a6-b973-a710a9c668fc")
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true

                connection.instanceFollowRedirects = false // Don't follow redirects

                connection.requestMethod = "POST" // POST request
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.useCaches = false

                //writing data
                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)

                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult : Int = connection.responseCode //retrieving data

                if(httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(
                        InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line : String?
                    try{
                        while(reader.readLine().also { line = it }!= null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException){
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            }catch (e : SocketTimeoutException){
                result = "Connection timed out"
            } catch (e : Exception){
                result = "Error : " + e.message
            }finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            cancelCustomProgressDialog()

            Log.i("JSON RESPONSE RESULT", result)

            val jsonObject = JSONObject(result)
            val message = jsonObject.optString("message")  // we pass the key and get the value
            Log.i("Message", message)
            val userId = jsonObject.optInt("user_id")
            Log.i("User Id", "$userId")
            val name = jsonObject.optString("name")
            Log.i("Name", name)

            val profileDetailsObject = jsonObject.optJSONObject("profile_details")  //acces json object
            val isProfileCompleted = profileDetailsObject.optBoolean("is_profile_completed")
            Log.i("Is Profile Completed", "$isProfileCompleted")

            //list of json objects
            val dataArray = jsonObject.optJSONArray("data_list")
            Log.i("Data List size", "${dataArray.length()}")

            for(item in 0 until dataArray.length()) {
                Log.i("Value $item", "${dataArray[item]}")

                val dataItemObject: JSONObject = dataArray[item] as JSONObject  //object of what is in the list

                val dataItemId = dataItemObject.optInt("id")
                Log.i("Data Item Id", "$dataItemId")
                val dataItemValue = dataItemObject.optString("value")
                Log.i("Data Item Value", "$dataItemValue")
            }

        }

        private fun showCustomProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)

            //Start the dialog and display it on screen
            customProgressDialog.show()
        }

        private fun cancelCustomProgressDialog() {
            customProgressDialog.dismiss()
        }
    }
}