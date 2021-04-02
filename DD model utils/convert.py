import tensorflow as tf
import tensorflow.keras as keras

model = keras.models.load_model("model.h5", compile=True)
#tf.saved_model.save(model, "model/1/")

width = model.input_shape[2]
height = model.input_shape[1]

print(width)
print(height)