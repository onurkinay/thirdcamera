package tr.com.onurkinay.thirdcamera

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventhubs.ConnectionStringBuilder

import java.util.concurrent.Executors
import java.net.URI
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*
import java.net.URL
import com.github.fluidsonic.fluid.json.*

class MainActivity : AppCompatActivity() {
    private val azure = AzureClass(this) //start azure class. Look AzureClass.kt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //azure starts
        try {
            azure.initClient()//connect azure iothub
        } catch (e: Exception) {
        }

        val connStr = ConnectionStringBuilder()
            .setEndpoint(URI(azure.eventHubsCompatibleEndpoint))
            .setEventHubName(azure.eventHubsCompatiblePath)
            .setSasKeyName(azure.iotHubSasKeyName)
            .setSasKey(azure.iotHubSasKey)

        // Create an EventHubClient instance to connect to the
        // IoT Hub Event Hubs-compatible endpoint.
        var executorService = Executors.newSingleThreadScheduledExecutor()
        val ehClient = EventHubClient.createSync(connStr.toString(), executorService)

        // Use the EventHubRunTimeInformation to find out how many partitions
        // there are on the hub.
        val eventHubInfo = ehClient.runtimeInformation.get()

        // Create a PartitionReciever for each partition on the hub.
        for (partitionId in eventHubInfo.partitionIds) {
            azure.receiveMessages(ehClient, partitionId)
        }



        fab.setOnClickListener { view ->
            azure.send("")
            Snackbar.make(view, "Please wait while the image is getting", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun setImage(imageLink: String){
        val parser  =  JSONParser.default.parseMap(imageLink)

        DownLoadImageTask(imageView1)
            .execute("https://thirdcamera.blob.core.windows.net/images/"+ parser.getValue("idImage").toString())

    }
// Class to download an image from url and display it into an image view
private class DownLoadImageTask(internal val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
    override fun doInBackground(vararg urls: String): Bitmap? {
        val urlOfImage = urls[0]
        return try {
            val inputStream = URL(urlOfImage).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) { // Catch the download exception
            e.printStackTrace()
            null
        }
    }
    override fun onPostExecute(result: Bitmap?) {
        if(result!=null){
            // Display the downloaded image into image view
            Toast.makeText(imageView.context,"download success",Toast.LENGTH_SHORT).show()
            imageView.setImageBitmap(result)
        }else{
            Toast.makeText(imageView.context,"Error downloading",Toast.LENGTH_SHORT).show()
        }
    }
}
}
