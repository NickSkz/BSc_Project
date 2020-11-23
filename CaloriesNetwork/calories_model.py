from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense

import keras
from matplotlib import pyplot as plt

dataset = loadtxt('calories_train.csv', delimiter=';')
X = dataset[:, 0:2]
Y = dataset[:, 2]

model = Sequential()
model.add(Dense(128, input_dim=2, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(96, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(64, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(32, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(8, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(1, kernel_initializer=keras.initializers.TruncatedNormal(), activation='linear'))

model.summary()

model.compile(loss='mse', optimizer=keras.optimizers.Adam(), metrics=['mse'])
history = model.fit(X, Y, epochs=10000, batch_size=3, validation_split=0.25)

model.save('calories_model.h5')

plt.plot(history.history['mse'])
plt.title('MSE function')
plt.xlabel('epoch')
plt.ylabel('mse')
plt.grid()
plt.savefig('figures/amse_graph.png')
plt.close()

plt.plot(history.history['loss'])
plt.title('Loss function')
plt.xlabel('epoch')
plt.ylabel('loss')
plt.grid()
plt.savefig('figures/loss_graph.png')
plt.close()