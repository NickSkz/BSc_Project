from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense

from tensorflow import keras

import numpy as np
import tensorflow as tf

X = [[2, 205]] 
# 1256

model = keras.models.load_model('calories_model.h5')
print(model.predict(X))