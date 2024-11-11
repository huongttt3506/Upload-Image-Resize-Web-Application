import sys
import cv2

def resize_image(input_path, output_path, width, height):
    # load image
    img = cv2.imread(input_path)
    if img is None:
        raise ValueError("Can not read input_path {}".format(input_path))

    # resize image
    resizedImg = cv2.resize(img, (width ,height))

    # save image into new file
    cv2.imwrite(output_path, resizedImg)

    # # show image
    # cv2.imshow("image", resizedImg)
    # cv2.waitKey()
    # cv2.destroyAllWindows()
    return 0


if __name__ == "__main__":
   input_path = sys.argv[1]
   output_path = sys.argv[2]
   width = int(sys.argv[3])
   height = int(sys.argv[4])
   
   resize_image(input_path, output_path, width, height)
