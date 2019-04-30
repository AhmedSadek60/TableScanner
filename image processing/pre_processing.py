####################################################################################################
# importing useful libs
####################################################################################################
%matplotlib inline
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from math import hypot, pi, cos, sin
from scipy import signal as sig
import numpy as np
import cv2 as cv

####################################################################################################
# define function that used to show the images using plt lib (if needed)
####################################################################################################
def showImg(img):
    temp = cv.cvtColor(img, cv.COLOR_BGR2RGB)
    testimageplot = plt.imshow(temp,'gray')
    return temp

####################################################################################################
# define the pre_processing function 
#   - it takes input image and return no. of cols and rows and image cells
####################################################################################################
def pre_processing (image):
####################################################################################################
# 1-convert image to grayscale and apply hough transform on it
# 2-convert hough output to :
#         - vlines array that contain vertical lines
#         - hlines array that contain horizontal lines 
####################################################################################################
    vlines = []
    hlines = [] 
    gray = cv.cvtColor(image,cv.COLOR_BGR2GRAY)
    edges = cv.Canny(gray,50,150,apertureSize = 3)
    lines = cv.HoughLines(edges,1,pi/180,100)
    for i in range(len(lines)):
        for rho,theta in lines[i]:
            a = np.cos(theta)
            b = np.sin(theta)
            x0 = a*rho
            y0 = b*rho
            x1 = int(x0 + 1000*(-b))
            y1 = int(y0 + 1000*(a))
            x2 = int(x0 - 1000*(-b))
            y2 = int(y0 - 1000*(a))
        
            if(-3 <= (x1-x2) <= 3):
                vlines.append([x1,y1,x2,y2])

            if(-3 <= (y1-y2) <= 3):
                hlines.append([x1,y1,x2,y2])
                
####################################################################################################
# remove duplication from vlines array (some of vlines in the table detected by more than one line)
####################################################################################################                
    dup = []
    for i in range(len(vlines)) :
        for j in range(len(vlines)):
            if i >= j :
                continue
            if(-5 <= vlines[i][0]-vlines[j][0] <= 5 ):
                dup.append(j)
    dup.sort()
    for i in range(len(dup)):
        vlines.pop(dup[i]-i)

#################################################################################################### 
# remove duplication from hlines array (some of hlines in the table detected by more than one line)
#################################################################################################### 
    hdup = []    
    for i in range(len(hlines)) :
        for j in range(len(hlines)):
            if i >= j :
                continue
            if(-5 <= (hlines[i][3]-hlines[j][3]) <= 5 ):
                hdup.append(j)       
    hdup.sort()
    for i in range(len(hdup)):
        z = hdup[i]-i
        hlines.pop(z) ,z , len(hlines)
        
####################################################################################################
# 1- segmentation :
#     using vlines & hlines array we can detect cells in the table and saving each cell as separeted image 
# 2- return no. of columns and rows oof the original table
#################################################################################################### 
    vlines.sort(key=lambda x: x[:][0])
    hlines.sort(key=lambda x: x[:][1])

    counter = 0 

    for i in range(len(vlines)-1) :
        for j in range(len(hlines)-1) :
            cell = np.zeros((100,100),np.uint8)
            cell = img[vlines[i][0]:vlines[i+1][0] , hlines[j][1]:hlines[j+1][1]]
            counter+=1
            cv.imwrite(str(counter)+'.png',cell)
    return len(vlines)-1 , len(hlines)-1

####################################################################################################
# testing on test image
####################################################################################################
img = cv.imread("test3.png")
x,y=pre_processing(img)
print(x,y)