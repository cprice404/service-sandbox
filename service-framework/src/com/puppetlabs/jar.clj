(ns com.puppetlabs.jar
  (:import [java.io FileInputStream]
           [java.util.jar JarFile Manifest]))

(defn manifest->map
  [^Manifest manifest]
  (reduce
    (fn [m e]
      (assoc m (.. e getKey toString) (.getValue e)))
    {}
    (.getMainAttributes manifest)))

(defn parse-manifest-file
  [manifest-file]
  (when-let [m (Manifest. (FileInputStream. manifest-file))]
    (manifest->map m)))

(defn get-jar-manifest
  [jar-path]
  (when-let [j (JarFile. jar-path)]
    (when-let [m (.getManifest j)]
      (manifest->map m))))