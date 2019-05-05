# Table Scanner
  
  - Mobile application for digitalising your handwritten/printed images of tables, by converting the image into a csv file readable by programs as excel.
  - Example:  (include a sample input/output images)
    

## Input:
  - An image of your table with any format (jpg, png,..etc)
    
## Output:
  - A Csv file containg the digital form of your table.
    
    
## Modules:

#### 1. Mobile Application
  - Android application that enables you to capture an image of a handwritten/printed table, sends it to a server, and apply the appropriate image processing before segmenting the table cells.
  - On the server an OCR model is run to extract the text in each field in the table.
  - The application then saves the recognised texts into a .CSV file, and preview it using an app like: Excel or google sheets.
  
  Django was used to make REST API to send the catured image from TableScanner to the server.

#### 2. Table Cells Segmentation
  - Detecting the vertical and horizontal lines of the table, then using them to segment the table cells.

#### 3. Text Recognition
  - A character based CNN-to-RNN model, trained on IAM (handwritten) dataset, is used to recognise the text in the extracted table cells.

#### 4. Converting to CSV
  - The recognised texts are then converted into a csv file. 

## Algorithm :

#### 1. Pre_processing
  - Detecting the vertical and horizontal lines of the table.
  - Segment the table to cells and save them as images. 
  - Return no. of columns and no. of rows.

#### 2 Text Recognition

##### 2.1 Handwriting Recognition Model
  - Gaussian Blur is applied on the input images (table cells) to get rid of the background noise
  - Adaptive Thresholding is applied to binarise the image.
  - Resizing to the model's input shape (32, 128) is done while keeping the aspect ratio unchanged and padding the rest of the image size.
   * **We used the model implemented [here](https://github.com/githubharald/SimpleHTR)**
  - The model's max word length is 32 characters, so we apply word segmentation on each table cell image and only run the model on the word-segmented output images.
  - The model is 5 CNN layers reduced to 32 * 256 features that are then fed to 2 layers of RNNs. The output is decoded to give the text.
  - The text's set of characters is:
      ”#&’()*+,-./0123456789:;?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
  - The model's final output is a list of strings, each string is the text in one table cell.
  
##### 2.2 Printed Text Recognition
  - we used [pytessaract](https://pypi.org/project/pytesseract/) on the tabe cells images.
    
#### 3. Converting the detected text into a csv file
  - The model's output (list of strings) is converted into a JSON of the format show below, which is used to create the csv file:
    
``` javascript
{
    "columns": 2,
    "rows": 2,
    "rowsdata": [
        {"col1": "hello", "col2": "27.5"}, #row1
        {"col1": "world", "col2": "24.1"}  #row2
    ]
}
```
