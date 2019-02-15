if (process.env.NODE_ENV !== 'production') {
    require('dotenv').load();
}
const PiCamera = require('pi-camera');
const path = require('path');
const storage = require('azure-storage');

//AZURE BLOB SERVICE -- BEGIN
const blobService = storage.createBlobService();

const uploadLocalFile = async (containerName, filePath) => {
    return new Promise((resolve, reject) => {
        const fullPath = path.resolve(filePath);
        const blobName = path.basename(filePath);
        blobService.createBlockBlobFromLocalFile(containerName, blobName, fullPath, err => {
            if (err) {
                reject(err);
            } else {
                resolve({
                    message: `Local file "${filePath}" is uploaded`
                });
            }
        });
    });
};
//AZURE BLOB SERVICE -- END

//AZURE IOT HUB -- BEGIN

//MUST CHANGE
var connectionString = 'HostName=picamera.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=u4kE24S8eZLSR9wM0tct0b9K4ewm914ftx+i7toSDFU=';
var {
    EventHubClient,
    EventPosition
} = require('@azure/event-hubs');

var uuid = require('uuid');

var Protocol = require('azure-iot-device-mqtt').Mqtt;
var Client = require('azure-iot-device').Client;
var Message = require('azure-iot-device').Message;


var connectionStringDevice = "HostName=picamera.azure-devices.net;DeviceId=pi;SharedAccessKey=Dbb9RnxK557FLUWgFEKbDW4XfM2YzeshqOyPINHjaEc="; //MUST CHANGE

if (!connectionStringDevice) {
    console.log('Please set the DEVICE_CONNECTION_STRING environment variable.');
    process.exit(-1);
}

var client = Client.fromConnectionString(connectionStringDevice, Protocol);
client.open(function (err) {
    if (err) {
        console.error('Could not connect: ' + err.message);
    } else {
        console.log('Client connected');

        client.on('error', function (err) {
            console.error(err.message);
            process.exit(-1);
        });
    }
});
// AZURE IOT HUB -- END


function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}


// IF THE APPLICATION GETS FROM AZURE D2C MESSAGE
var printMessage = function (message) { // PRINT MESSAGE
    if (message.body.checkImage != null) { // CHECK MESSAGE IS READ STATUS OR ISN'T


        //IF MESSAGE COMES FROM ANDROID
        execute().then(() => console.log("Done")).catch((e) => console.log(e));
        messageID = message.body.id;

        console.log("Message ID: " + messageID);
        console.log(message.body.mes);


        //!!IF MESSAGE COMES FROM ANDROID
    }
};
var printError = function (err) {
    console.log(err.message); // THERE IS ERROR
};

var ehClient;
EventHubClient.createFromIotHubConnectionString(connectionString).then(function (clientE) {
    console.log("Successully created the EventHub Client from iothub connection string.");
    ehClient = clientE;
    return ehClient.getPartitionIds();
}).then(function (ids) {
    console.log("The partition ids are: ", ids);


    return ids.map(function (id) {
        return ehClient.receive(id, printMessage, printError, {
            eventPosition: EventPosition.fromEnqueuedTime(Date.now())
        });
    });
}).catch(printError);
//!!IF THE APPLICATION GETS FROM AZURE D2C MESSAGE

const execute = async () => { 
    imageName = getRandomInt(500000) + ".jpg";
    const myCamera = new PiCamera({
        mode: 'photo',
        output: `${ __dirname }` + "/" + imageName,
        width: 640,
        height: 480,
        nopreview: true,
    });
    
    myCamera.snap()
        .then(async (result) => {
           
            const localFilePath = "./" + imageName;
            let response;

            response = await uploadLocalFile("images", localFilePath);
            console.log(response.message);

            var message = new Message(JSON.stringify({
                idImage
                : imageName
            }));

            message.messageId = uuid.v4();

            console.log('Sending message: ' + message.getData());
            client.sendEvent(message, function (err) {
                if (err) {
                    console.error('Could not send: ' + err.toString());
                } else {
                    console.log('Message sent: ' + message.messageId);
                }
            });
        })
        .catch((error) => {
            // Handle your error
        });

}