####################################################################################################
# importing useful libs
####################################################################################################
%matplotlib inline
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
from math import hypot, pi, cos, sin
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

##########################################################################################
def pre_processing (img):
# ####################################################################################################
# # 1-Apply gaussian blurring to reduce noise
# # 2-convert image to grayscale and apply hough transform on it
# # 3-convert hough output to :
# #         - vlines array that contain vertical lines
# #         - hlines array that contain horizontal lines 
# ####################################################################################################
    vlines = []
    hlines = []

    blur = cv.GaussianBlur(img,(3,3),0)
    gray = cv.cvtColor(blur,cv.COLOR_BGR2GRAY)
    edges = cv.Canny(gray,25,150,apertureSize = 3)
    lines = cv.HoughLines(edges,1,pi/180,100)
    for i in range(len(lines)):
        for rho,theta in lines[i]:
            a = np.cos(theta)
            b = np.sin(theta)
            x0 = a*rho
            y0 = b*rho
            x1 = int(x0 + 375*(-b))
            y1 = int(y0 + 375*(a))
            x2 = int(x0 - 500*(-b))
            y2 = int(y0 - 500*(a))
        if(-70 <= (x1-x2) <= 70):
            vlines.append([x1,y1,x2,y2])

        if(-70 <= (y1-y2) <= 70):
            hlines.append([x1,y1,x2,y2])

                       
# ####################################################################################################
# # remove duplication from vlines array (some of vlines in the table detected by more than one line)
# ####################################################################################################                
    dup = []
    for i in range(len(vlines)) :
        for j in range(len(vlines)):
            if i >= j :
                continue
            if  ((-15 <= vlines[i][0]-vlines[j][0] <= 15 ) or (-15 <= vlines[i][2]-vlines[j][2] <= 15 )):
                dup.append(j)
            
    dup.sort()
    dup = list(set(dup))
    for i in range(len(dup)):
        vlines.pop(dup[i]-i)
               
# #################################################################################################### 
# # remove duplication from hlines array (some of hlines in the table detected by more than one line)
# ####################################################################################################     
    hdup = []  
    for i in range(len(hlines)) :
        for j in range(len(hlines)):
            if i >= j :
                continue
            if ((-15 <= (hlines[i][3]-hlines[j][3]) <= 15 ) or (-15 <= (hlines[i][1]-hlines[j][1]) <= 15 )):
                hdup.append(j)       
    hdup.sort()
    hdup = list(set(hdup))
    for i in range(len(hdup)):
        z = hdup[i]-i
        hlines.pop(z) ,z , len(hlines)

# #################################################################################################### 
# # sort vlines and hlines w.r.t. x1 and y1 respectively
# #################################################################################################### 
    vlines_x1s = [ line[0] for line in vlines ]
    vlines = [line for x1, line in sorted(zip(vlines_x1s, vlines)) ]

    hlines_y1s = [ line[1] for line in hlines ]
    hlines = [line for y1, line in sorted(zip(hlines_y1s, hlines))]
# #################################################################################################### 
# # Raise an exception if there is no tables in the input image detected
# ####################################################################################################     
    n_cols = len(vlines) - 1
    n_rows = len(hlines) - 1
    if (n_cols == 0) or (n_rows == 0) :
        raise Exception('There is no tables can be found')
# #################################################################################################### 
# # get the intersection points
# #################################################################################################### 
    eps = 1e-10
    inter_pts = []
    for h in hlines:
        for v in vlines:
            x_h1, y_h1, x_h2, y_h2 = h
            x_v1, y_v1, x_v2, y_v2 = v
        
            m_h = (y_h2 - y_h1) / (x_h2 - x_h1)
            c_h = y_h1 - m_h * x_h1
        
            m_v = (y_v2 - y_v1) / (x_v2 - x_v1 + eps)
            c_v = y_v1 - m_v * x_v1
        
            x_inter = int((c_v - c_h) / (m_h - m_v + eps))
            y_inter = int(m_h * x_inter + c_h)
        
            inter_pts.append( (x_inter, y_inter) )
# #################################################################################################### 
# # 1- segmentation :
# #     using vlines & hlines & inter_pts (intersection points) arrays we can detect cells in the table and saving each cell as separeted image 
# # 2- return no. of columns and rows oof the original table
# #################################################################################################### 

    cells = []
    max_cell_width = -1
    max_cell_height = -1
    for i in range(n_cols * n_rows):
        # offset w.r.t. to the cell row
        offset = i // n_cols
        start = i + offset
        cell_coords = [ inter_pts[start], inter_pts[start + 1], inter_pts[start + n_cols + 1], inter_pts[start + n_cols + 2] ]
        cells.append(cell_coords)
        y1 = cell_coords[0][1] + 4
        y2 = cell_coords[2][1] - 4
        x1 = cell_coords[0][0] + 4
        x2 = cell_coords[1][0] - 4
        cell_array = img[y1:y2, x1:x2, :]
        cv.imwrite('{}.png'.format(i), cell_array)
    return (n_cols,n_rows)

####################################################################################################
# testing on test image
####################################################################################################
img = cv.imread("1.jpg")
x,y=pre_processing(img)
print(x,y)
