from PIL import Image
import pytesseract
import cv2
import os
import numpy as np


input_dir = '../data/input/'
temp_dir = 'ocr_data'
imgSize = (128, 32)

def preprocess_camera_input(img, imgSize):
	"put img into target img of size imgSize"


	# increase contrast
	pxmin = np.min(img)
	pxmax = np.max(img)
	img = (img - pxmin) / (pxmax - pxmin) * 255

	# thresholding
	img = cv2.adaptiveThreshold(img.astype('uint8'), 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 41 , 20)

	# resize the image (while keeping the aspect ratio), then pad it.
	img_h, img_w = img.shape
	w, h = (512, 128)
	new_w = int(img_w * min(w/img_w, h/img_h))
	new_h = int(img_h * min(w/img_w, h/img_h))
	resized_img = cv2.resize(img, (new_w, new_h))
	img = resized_img
	canvas = np.full( (h, w), np.mean(img), dtype = img.dtype )
	canvas[ (h-new_h) // 2 : (h-new_h) // 2 + new_h, (w-new_w) // 2 : (w-new_w) // 2 + new_w] = resized_img
	target = canvas

	return img


def main():
	
	# get images filenames
	imgs = [img for img in os.listdir(input_dir)]
	
	# read and preprocess the images
	imgs_processed = [ preprocess_camera_input( cv2.imread(input_dir + img, 0), imgSize) for img in imgs ]
	
	# use the model for inference
	text = [ pytesseract.image_to_string(Image.open(input_dir + filename)) for filename in os.listdir(input_dir) ]

	print(text)

	return text

if __name__ == '__main__':
	main()




