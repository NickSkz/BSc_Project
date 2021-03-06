from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense

from tensorflow import keras

dataset = loadtxt('cardio_traindata.csv', delimiter=';')
X = dataset[60000:, 1:12]
Y = dataset[60000:, 12]

model = keras.models.load_model('heart_disease_model.h5')

_, accuracy = model.evaluate(X, Y)
print('Accuracy: %.2f' % (accuracy*100))