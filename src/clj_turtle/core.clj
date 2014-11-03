(ns clj-turtle.core
  (:require [clojure.string :as string]))

(defn- md5
  "Generate a MD5 hash from an input string"
  [s]
  (apply str
         (map (partial format "%02x")
              (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                         .reset
                         (.update (.getBytes s)))))))

(defn- escape
  "Escape data literal for the Turtle data format."
  [s]
  (-> s
      str
      (string/replace #"\"" "\\\\\"")))

(defn rdf
  "Generate RDF/Turtle serialized data from a set of triples defined by clj-turtle."
  [& rest]
  (loop [forms rest
         n3 ""]
    (if-not (empty? forms)
      (if (and (get (into [] forms) 3)
               (vector? (nth forms 3)))
        (recur (drop 4 forms)
               (apply str n3 (nth forms 0) (nth forms 1) (nth forms 2)" .\n"
                      (let [iri (str "rei:" (md5 (str (nth forms 0) (nth forms 1) (nth forms 2))))]
                        (->> (nth forms 3)
                             (map #(str "<" iri "> " %1 " .\n"))
                             (apply str (str "<" iri "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n"
                                             "<" iri "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> " (nth forms 0) " .\n"
                                             "<" iri "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> " (nth forms 1) " .\n"
                                             "<" iri "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> " (nth forms 2) " .\n"))))))
        (recur (drop 3 forms)
               (apply str n3 (nth forms 0) (nth forms 1) (nth forms 2)" .\n")))
      n3)))

(def turtle "Alias for the (rdf) function" rdf)

(defmacro defns
  "Create a new namespace that can be used to create the clj-turtle triples"
  [prefix namespace]
  (let [entity (gensym "entity-")
        body (gensym "body-")]
    `(defn ~prefix
       [~entity & ~body]
       (str "<" ~namespace (name ~entity) "> " (apply str (rest ~body))))))

(defn iri
  "Serialize a URI where you provide the full URI as a string"
  [uri]
  (str "<" uri "> "))

(defn a
  "Specify the rdf:type of an entity being described"
  []  
  (str "<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "))

(defn literal
  "[v] String that represents the literal value to create.
   [lang] (optional) Keyword referring to the ISO 639-1 language code
   [type] (optional) Namespace function or string that refers to a
                     datatype to specify for the literal value

   Usages:
     ; specify a simple literal
     (literal \"bob\")

     ; specify the language of the literal
     (literal \"bob\" :lang :en)
  
     ; if the namespace 'xsd' has been defined
     (literal \"bob\" :type (xsd :string))

     ; if you want to use an abreviated URI you can do this that way
     (literal \"bob\" :type \"xsd:string\")"
  [v & {:keys [lang type]
        :or [lang nil
             type nil]}]
  (apply str
         (str "\"\"\"" (if (keyword? v)
                         (escape (name v))
                         (escape v))"\"\"\"")
         (if lang
           (str "@" (name lang))
           (when type
             (str "^^" (name type))))))

(defn rei
  "Reify a clj-turtle triple"
  [& rest]
  (loop [forms rest
         statements []]
    (if-not (empty? forms)
      (recur (drop 2 forms)
             (into statements [(apply str (nth forms 0) (nth forms 1))]))
      statements)))
