from flask import Flask, request, send_file
import cv2
import numpy as np
import io

app = Flask(__name__)

def resize_image(file, width, height):
    # Read the image from the file (multipart) into a numpy array
    npimg = np.frombuffer(file.read(), np.uint8)
    img = cv2.imdecode(npimg, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("Cannot process the uploaded file")

    # Resize image
    resized_img = cv2.resize(img, (width, height))

    # get extension
    file_extension = file.filename.split('.')[-1].lower()
    valid_formats = {'jpg': '.jpg', 'jpeg': '.jpg', 'png': '.png', 'bmp': '.bmp', 'tiff': '.tiff'}

    img_format = valid_formats.get(file_extension, '.jpg')

    # Encode the resized image to send back as a response
    _, img_encoded = cv2.imencode(img_format, resized_img)
    return img_encoded.tobytes(), img_format

