import h5py
import numpy
import matplotlib.pyplot as plt
from os import path
import time

# read HDF5 file and raise exceptions
def readH5File(fileName):
	if not(fileName.endswith('.h5')): 
		raise NameError('File name must end with .h5')
	elif not(path.exists(fileName)):
		raise ValueError("No such file: {} exisits.".format(fileName))
	else:
		f = h5py.File(fileName, 'r')
		if f.mode == 'r':
			return f
		else: raise ValueError("File: {} is not readable.".format(fileName))


# get dataset from h5 file
def get_dataset(h5_file, key):
	return h5_file[key]

# 2D plot the data in the h5 file with x-axis as time in milliseconds)
# and y-axis as data unit in acceleration, g 
# p_f: parent file or group
def generate_image(p_f):
	v_label = p_f['/'].attrs.get('units')
	tic = time.clock()
	# get one of the dataset
	for key in p_f.keys():
		dataset = get_dataset(p_f, key)
		sample_tuple = dataset[0, :]
		sample_size = sample_tuple.size
		
		print("dataset size:", sample_size, "max:", max(sample_tuple), "min:", min(sample_tuple), "average:", numpy.mean(sample_tuple), "median:", numpy.median(sample_tuple))

		x = numpy.arange(0, 900000, 900000/sample_size)
		y = sample_tuple

		plt.plot(x, y)
		plt.xlabel('millisecond)')
		plt.ylabel(v_label.decode("utf-8"))
		plt.title('test_figure')
		plt.show()
	toc = time.clock()
	print("Process time:", toc-tic)

def main():
	fileName = "data_channel_1_02-Sep-2014-1330.h5"
	try:
		f = readH5File(fileName)
		generate_image(f)
		
		print("The data from this HDF file has been successfully plotted as a figure!")
		f.close()
	except NameError as e:
		print('Bad file name:', e)
	except ValueError as e:
		print(e)
	except IOError as e:
		print('Cannot read file:', e)

# main() gets called at the end of the file
if __name__ == "__main__": main()
