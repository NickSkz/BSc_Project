from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense

import keras
from matplotlib import pyplot as plt

dataset = loadtxt('cardio_traindata.csv', delimiter=';')
X = dataset[:60000, 1:12]
Y = dataset[:60000, 12]

model = Sequential()
model.add(Dense(9, input_dim=11, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(6, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(3, kernel_initializer=keras.initializers.TruncatedNormal(), activation='relu'))
model.add(Dense(1, kernel_initializer=keras.initializers.TruncatedNormal(), activation='sigmoid'))

model.summary()

model.compile(loss='binary_crossentropy', optimizer=keras.optimizers.Adam(), metrics=['accuracy'])
history = model.fit(X, Y, epochs=5000, batch_size=64, validation_split=0.1)

model.save('heart_disease_model.h5')


plt.plot(history.history['accuracy'])
plt.title('Accuracy function')
plt.xlabel('epoch')
plt.ylabel('accuracy')
plt.grid()
plt.savefig('figures/accuracy_graph.png')
plt.close()

plt.plot(history.history['loss'])
plt.title('Loss function')
plt.xlabel('epoch')
plt.ylabel('loss')
plt.grid()
plt.savefig('figures/loss_graph.png')
plt.close()