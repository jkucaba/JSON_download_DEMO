package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallAPILoginAsyncTask().execute()
    }

    private inner class CallAPILoginAsyncTask(): AsyncTask<Any, Void, String>() {

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