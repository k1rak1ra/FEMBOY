import tensorflow as tf
import tensorflow.keras as keras

model = keras.models.load_model("model.h5", compile=True)
tf.saved_model.save(model, "model/1/")

# Convert the model
converter = tf.lite.TFLiteConverter.from_saved_model("model/1/") # path to the SavedModel directory
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)

width = model.input_shape[2]
height = model.input_shape[1]

print(width)
print(height)
