(ns tnoda.syobochim-advent-calendar-2014
  (:import (org.opencv.core Core Mat MatOfRect MatOfPoint Point Rect Scalar CvType)
           org.opencv.highgui.Highgui
           org.opencv.objdetect.CascadeClassifier)
  (:require [clojure.java.io :as io]))

(defn- resource-path
  [path]
  (-> path io/resource .getPath))

(defn- rect->radius
  [^Rect r]
  (-> (max (.width r) (.height r))
      (/ 1.4)
      long))

(defn- rect->center
  [^Rect r]
  (let [x (.x r)
        y (.y r)
        w (.width r)
        h (.height r)]
    (Point. (+ x (quot w 2)) (+ y (quot h 2)))))

(defn -main
  [& args]
  (clojure.lang.RT/loadLibrary org.opencv.core.Core/NATIVE_LIBRARY_NAME)
  (let [img (-> args first resource-path Highgui/imread)
        xmls ["haarcascade_eye.xml"
              "haarcascade_mcs_mouth.xml"
              "haarcascade_mcs_nose.xml"]
        xml->detector #(CascadeClassifier. (resource-path %))
        detectors (map xml->detector xmls)
        detect (fn
                 [^CascadeClassifier c]
                 (let [res (MatOfRect.)]
                   (.detectMultiScale c img res)
                   (.toArray res)))
        fill-green (fn
                     [^Rect r]
                     (Core/circle img
                                  (rect->center r)
                                  (rect->radius r)
                                  (Scalar. 0 255 0)
                                  -1))]
    (->> detectors
         (mapcat detect)
         (map fill-green)
         dorun)
    (Highgui/imwrite "face2.jpg", img)))
