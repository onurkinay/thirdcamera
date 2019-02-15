if (process.env.NODE_ENV !== 'production') {
    require('dotenv').load();
}
const PiCamera = require('pi-camera');
const path = require('path');
const storage = require('azure-storage');

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

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
  }
imageName = getRandomInt(500000) + ".jpg";
const myCamera = new PiCamera({
    mode: 'photo',
    output: `${ __dirname }`+"/"+imageName,
    width: 640,
    height: 480,
    nopreview: true,
});


const execute = async () => {

     myCamera.snap()
        .then( async (result) =>  {

            const localFilePath = "./"+imageName;
            let response;

            response = await uploadLocalFile("images", localFilePath);
            console.log(response.message);
        })
        .catch((error) => {
            // Handle your error
        });

}

execute().then(() => console.log("Done")).catch((e) => console.log(e));