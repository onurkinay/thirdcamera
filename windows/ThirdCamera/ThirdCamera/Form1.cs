using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Blob;

namespace ThirdCamera
{
    public partial class Form1 : Form
    {
        CloudStorageAccount storageAccount = null;
        CloudBlobContainer cloudBlobContainer = null;
        string sourceFile = null;
        string destinationFile = null;

        string storageConnectionString = Environment.GetEnvironmentVariable("B5CnTtNkomo0qnu0aHhxGFgwup8y157oaO6shM8D63vlpI+OCb6ntTAnr5WPQ1PFEWV/b/usabQ1t88Bt+y3Ew==");
        public Form1()
        {
            InitializeComponent();
        }

        private async void Form1_Load(object sender, EventArgs e)
        {
            if (CloudStorageAccount.TryParse(storageConnectionString, out storageAccount))
            {
                try
                {
                    CloudBlobClient cloudBlobClient = storageAccount.CreateCloudBlobClient();
                    cloudBlobContainer = cloudBlobClient.GetContainerReference("images" + Guid.NewGuid().ToString());
                    // List the blobs in the container.
                    label1.Text = "List blobs in container.";
                    BlobContinuationToken blobContinuationToken = null;
                    do
                    {
                        var results = await cloudBlobContainer.ListBlobsSegmentedAsync(null, blobContinuationToken);
                        // Get the value of the continuation token returned by the listing call.
                        blobContinuationToken = results.ContinuationToken;
                        foreach (IListBlobItem item in results.Results)
                        {
                            label1.Text += item.Uri;
                        }
                    } while (blobContinuationToken != null); // Loop while the continuation token is not null. 
                }
                catch (StorageException ex)
                {
                    Console.WriteLine("Error returned from the service: {0}", ex.Message);
                }
            }
        }
    }
}
