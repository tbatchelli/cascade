; Copyright 2009 Howard M. Lewis Ship
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;   http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
; implied. See the License for the specific language governing permissions
; and limitations under the License.

(ns cascade.test-urls
  (:import javax.servlet.http.HttpServletRequest)
  (:use
    (clojure (test :only [is are deftest]))
    (cascade mock urls)))
    
(deftest convert-to-url-string
  (are [v s] (= (to-url-string v) s)
    "fred" "fred"
    23  "23"
    -42.7 "-42.7"
    :any-keyword "any-keyword"
    'any-symbol "any-symbol"))
    
(deftest test-split-path
  (is (vector? (split-path "foo/bar")))
  (are [input split] 
       (= (split-path input) split)
       "foo/bar" ["foo" "bar"]
       "/foo/bar/" ["foo" "bar"]
       "//foo//bar//baz" ["foo" "bar" "baz"]
       "" []
       "foo" ["foo"]))  
  
(deftest test-construct-absolute-path
  (are [context-path path extra-path parameters expected]
      (= (construct-absolute-path context-path (link-map-from-path path extra-path parameters)) expected)
      "" "foo" ["bar"] nil "/foo/bar"
      "/ctx" "account/list" [12 34] { :format :brief 'per-path 23 } "/ctx/account/list/12/34?format=brief&per-path=23"))  
      
(use 'cascade.logging)
      
(defn custom-parser      
  [value]
  (.toUpperCase value))
      
(deftest test-parse-url-positional
  (let [env { :cascade { :extra-path ["123" "beta" "456"]}}]
    (is (= (class (parse-url env [p0 :int] p0)) Integer) ":int parses to Integer")
    
    (is (= (parse-url env [p0 :int p1 :str p2 :int] [p0 p1 p2]) [123 "beta" 456]))
    
    (is (nil? (parse-url nil [p0 :int])) "Insufficient values become nil.")
    
    (is (= (parse-url env [p0 :int p1 custom-parser] p1) "BETA") "Custom value parser.")
    
    (is (= (parse-url env [p0 :int p1 #(apply str (reverse %))] p1) "ateb") "Inline custom parser.")))

(deftest test-parse-url-query-parameters
   (with-mocks
     [request HttpServletRequest]
     (:train
       (expect .getParameter request "buzby" "12345"))
     (:test
       (let [env {:servlet-api { :request request }}
            result (parse-url env [b [:buzby :int]] b)]
         (is (= (class result) Integer))
         (is (= result 12345))))))