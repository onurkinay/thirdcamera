package tr.com.onurkinay.thirdcamera


import android.support.v7.app.AppCompatActivity
import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.eventhubs.EventHubException
import com.microsoft.azure.eventhubs.EventPosition
import com.microsoft.azure.eventhubs.PartitionReceiver
import com.microsoft.azure.sdk.iot.device.*
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult
import java.io.IOException
import java.net.URISyntaxException
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.function.Consumer



class AzureClass(mainAc: MainActivity) : AppCompatActivity() {

    private var mainAcc = mainAc
    var connString = "HostName=picamera.azure-devices.net;DeviceId=pi;SharedAccessKey=Dbb9RnxK557FLUWgFEKbDW4XfM2YzeshqOyPINHjaEc="
    var sendMessage: Message = Message("{ \"mes\":\"Hi World\", \"id\":65 }")
    var protocol = IotHubClientProtocol.MQTT
    var client: DeviceClient = DeviceClient(connString, protocol)

    var result: MethodResult? = null

    // az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}
    val eventHubsCompatibleEndpoint = "sb://ihsuprodamres030dednamespace.servicebus.windows.net/"

    // az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}
    val eventHubsCompatiblePath = "iothub-ehub-picamera-1300123-505f13084f"

    // az iot hub policy show --name iothubowner --query primaryKey --hub-name {your IoT Hub name}
    val iotHubSasKey = "u4kE24S8eZLSR9wM0tct0b9K4ewm914ftx+i7toSDFU="
    val iotHubSasKeyName = "iothubowner"

    // Track all the PartitionReciever instances created.
    private val receivers = ArrayList<PartitionReceiver>()

    internal class EventCallback : IotHubEventCallback {
        override fun execute(status: IotHubStatusCode, context: Any) {
            val i = context as Int
            println(
                "IoT Hub responded to message " + i!!.toString()
                        + " with status " + status.name
            )

            if ((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY)) {
                //ok
            } else {
                //problem
            }
        }
    }

    @Throws(URISyntaxException::class, IOException::class)
    fun initClient() {

        try {

            client.open()


        } catch (e2: Exception) {
            System.err.println("Exception while opening IoTHub connection: " + e2.message)
            client.closeNow()
            println("Shutting down...")
        }

    }


    fun send(value: String) {

        try {
            val random = Random()
            sendMessage = Message("{\"checkImage\":\"0\"}")
            sendMessage.messageId = java.util.UUID.randomUUID().toString()

           // mainAcc.getMessage("The message was sent") // SEND D2C MESSAGE

            val eventCallback = EventCallback()
            client.sendEventAsync(sendMessage, eventCallback, 1)
        } catch (e: Exception) {
        }

    }

    @Throws(EventHubException::class, ExecutionException::class, InterruptedException::class)
    fun receiveMessages(ehClient: EventHubClient, partitionId: String) {

        val executorService = Executors.newSingleThreadExecutor()

        // Create the receiver using the default consumer group.
        // For the purposes of this sample, read only messages sent since
        // the time the receiver is created. Typically, you don't want to skip any messages.
        ehClient.createReceiver(
            EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.fromEnqueuedTime(Instant.now())
        ).thenAcceptAsync(Consumer<PartitionReceiver> { receiver ->
            println(String.format("Starting receive loop on partition: %s", partitionId))
            println(String.format("Reading messages sent since: %s", Instant.now().toString()))

            receivers.add(receiver)

            while (true) {
                try {
                    // Check for EventData - this methods times out if there is nothing to retrieve.
                    val receivedEvents = receiver.receiveSync(100)

                    // If there is data in the batch, process it.
                    if (receivedEvents != null) {
                        for (receivedEvent in receivedEvents!!) {

                            var message = String(receivedEvent.bytes, Charsets.ISO_8859_1)//RECEIVED MESSAGE'S CONTENT

                            if (message.contains("idImage", ignoreCase = true)) {
                               mainAcc.setImage(message)
                            }
                        }
                    }
                } catch (e: EventHubException) {
                    println("Error reading EventData")
                }

            }
        }, executorService)
    }

}