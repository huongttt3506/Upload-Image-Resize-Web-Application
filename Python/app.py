from flask import Flask, request, send_file, jsonify
import io
from resize_image2 import resize_image


app = Flask(__name__)

@app.route('/resize', methods=['POST'])
def resize_image_route(): 
    file = request.files['file']
    width = int(request.form['width'])
    height = int(request.form['height'])

    if 'file' not in request.files:
        return 'No file part', 400 
    
    if file.filename == '':
        return {"message": "No selected file"}, 400
    

    if width <= 0 or height <= 0:
        return jsonify({"error": "Width and height must be positive integers"}), 400
    
    resized_image, img_format = resize_image(file, width, height)

    mimetype = f'image/{img_format[1:]}'
    return send_file(io.BytesIO(resized_image), mimetype=mimetype)
 
if __name__ == '__main__':
    app.run(port=5000, debug=True)




