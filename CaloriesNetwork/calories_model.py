from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense

import keras
from matplotlib import pyplot as plt

dataset = loadtxt('calories_train.csv', delimiter=';')
X = dataset[:, 0:2]
Y = dataset[:, 2]

model = Sequential()
model.add(Dense(512, input_dim=2, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(512, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(512, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(512, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(256, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(256, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(128, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(128, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(64, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(32, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(32, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(16, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(4, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(1, kernel_initializer=keras.initializers.TruncatedNormal(), activation='linear'))

model.summary()

model.compile(loss='mse', optimizer=keras.optimizers.Adam(), metrics=['mse'])
history = model.fit(X, Y, epochs=10000, batch_size=4, shuffle=True)

model.save('calories_model.h5')

plt.plot(history.history['loss'])
plt.xlabel('epoch')
plt.ylabel('loss')
plt.show()

#plt.plot(history.history['accuracy'])
#plt.xlabel('epoch')
#plt.ylabel('accuracy')
#plt.show()
