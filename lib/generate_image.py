#!/usr/bin/python
import h5py
import numpy
import matplotlib
# Force matplotlib to not use any Xwindows backend.
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import time
# regular expression
import re
from pylab import rcParams
# figure size in inches with the default being 8, 6
rcParams['figure.figsize'] = 80, 6

# read HDF5 file and raise exceptions
def readH5File(fileName):
	if not(fileName.endswith('.h5')): 
		raise NameError('File name must end with .h5')
	elif not(os.path.exists(fileName)):
		raise ValueError("No such file: {} exisits.".format(fileName))
	else:
		f = h5py.File(fileName, 'r')
		if f.mode == 'r':
			return f
		else: raise ValueError("File: {} is not readable.".format(fileName))


# get dataset from h5 file
def get_dataset(h5_file, key):
	return h5_file[key]

# save figure to image file
# fileName: h5 file
# the ith minute time frame within 15-minute range
# fig: the Figure object
def saveToFile(fileName, i, fig):
	# image directory
	imgDir = 'images'
	if not os.path.exists(imgDir):
		os.makedirs(imgDir)

	pattern = re.compile(r'\.h5$')
	imgNameTail = '_' + str(i+1) + '.png'
	baseName = pattern.sub(imgNameTail, os.path.basename(fileName))
	imgFileName = os.path.join(os.path.abspath(imgDir), baseName)
	fig.savefig(imgFileName)

# 2D plot the data in the h5 file with x-axis as time in milliseconds)
# and y-axis as data unit in acceleration, g 
# fileName: the absoulte path to h5 file
def generate_image(fileName):
	try:
		f = readH5File(fileName)
		attrs = f['/'].attrs
		attr_items = attrs.items()
		
		for attr in attr_items:
			print("{}: {}".format(attr[0], attr[1]))
		v_label = attrs.get('units')
		tic = time.clock()
		# get one of the dataset
		for key in f.keys():
			dataset = get_dataset(f, key)
			sample_tuple = dataset[0, :]
			sample_size = sample_tuple.size
		
			print("dataset size:", sample_size,
					"max:", max(sample_tuple), 
					"min:", min(sample_tuple), 
					"average:", numpy.mean(sample_tuple), 
					"median:", numpy.median(sample_tuple))

			# draw 15 figure corresponding to each 1-minute time frame
			for i, arr in enumerate(numpy.split(sample_tuple, 150)):
				x = numpy.arange(0, 900000/150, 900000/sample_size)
				y = arr

				# default marker is solid line
				plt.plot(x, y)
				plt.xlabel('millisecond')
				plt.ylabel(v_label.decode("utf-8"))
				plt.title('figure' + str(i+1))
			
				fig = plt.gcf()
				saveToFile(fileName, i, fig)
				plt.clf()
				plt.close()
		toc = time.clock()
		print("Total processing time:", toc-tic)
		
		f.close()
	except NameError as e:
		print('Bad file name:', e)
	except ValueError as e:
		print(e)
	except IOError as e:
		print('Cannot read file:', e)
	

def main():
	fileName = "data_channel_9_13-Sep-2014-1132.h5"

	generate_image(fileName)
		

# main() gets called at the end of the file
if __name__ == "__main__": main()
