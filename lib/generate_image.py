import h5py
import numpy
import matplotlib.pyplot as plt
import os
import time
# regular expression
import re

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
# fig: the Figure object
def saveToFile(fileName, fig):
	# image directory
	imgDir = 'images'
	if not os.path.exists(imgDir):
		os.makedirs(imgDir)

	pattern = re.compile(r'\.h5$')
	baseName = pattern.sub('.png', os.path.basename(fileName))
	imgFileName = os.path.join(os.path.abspath(imgDir), baseName)
	fig.savefig(imgFileName)
	print('Saved the generated figure of {} to {}!'.format(fileName, imgFileName))

# 2D plot the data in the h5 file with x-axis as time in milliseconds)
# and y-axis as data unit in acceleration, g 
# fileName: the absoulte path to h5 file
def generate_image(fileName):
	try:
		f = readH5File(fileName)
		v_label = f['/'].attrs.get('units')
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

			x = numpy.arange(0, 900000, 900000/sample_size)
			y = sample_tuple

			plt.plot(x, y, '.')
			plt.xlabel('millisecond')
			plt.ylabel(v_label.decode("utf-8"))
			plt.title('test_figure')
			#plt.show()
			fig = plt.gcf()
			saveToFile(fileName, fig)
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
