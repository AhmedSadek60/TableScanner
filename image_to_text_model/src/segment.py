import os
import cv2
from WordSegmentation import wordSegmentation, prepareImg


def segment():
	"""reads images from data/input and outputs the word-segmentation to data/input_segmented"""

	num_words_per_img = []

	# read input images from 'in' directory
	imgFiles = os.listdir('../data/input/')
	for (i,f) in enumerate(imgFiles):
		# read image, prepare it by resizing it to fixed height and converting it to grayscale
		img = prepareImg(cv2.imread('../data/input/%s'%f), 50)

		# execute segmentation with given parameters
		# -kernelSize: size of filter kernel (odd integer)
		# -sigma: standard deviation of Gaussian function used for filter kernel
		# -theta: approximated width/height ratio of words, filter function is distorted by this factor
		# - minArea: ignore word candidates smaller than specified area
		res = wordSegmentation(img, kernelSize=25, sigma=11, theta=7, minArea=100)

		num_words_per_img.append(len(res))

		# write output to 'data/input_segmented' directory
		if not os.path.exists('../data/input_segmented'):
			os.mkdir('../data/input_segmented')
			max_index = 0
		else:
			if len(os.listdir('../data/input_segmented')) != 0:
				max_index = sorted([ int(img[:-4]) for img in os.listdir('../data/input_segmented') ])[-1]
			else:
				max_index = 0

		# iterate over all segmented words
		# print('Segmented into %d words'%len(res))
		for (j, w) in enumerate(res):
			(wordBox, wordImg) = w
			# (x, y, w, h) = wordBox
			cv2.imwrite('../data/input_segmented/%d.png'%(max_index + j + 1), wordImg) # save word
			#cv2.rectangle(img,(x,y),(x+w,y+h),0,1) # draw bounding box in summary image
		max_index += len(res)

		# output summary image with bounding boxes around words
		#cv2.imwrite('../out/%s/summary.png'%f, img)

	return num_words_per_img


if __name__ == '__main__':
	segment()
