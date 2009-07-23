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

; Contains the basic dispatchers

(ns com.howardlewisship.cascade.internal.dispatcher
  (:import (javax.servlet ServletResponse))
  (:use (com.howardlewisship.cascade config dom logging)))

(defn render-xml-response
  [view-fn env]
  (let [#^ServletResponse response (-> env :servlet-api :response)
        dom (view-fn env)]
    (with-open [writer (.getWriter response)]
      (render-xml dom writer))))

(defn is-view-fn
  "Checks to see if the function exists and has meta :cascade-type equal to :view."
  ; TODO: check that function has just one / at least one parameter?
  [view-fn]
  (and view-fn (= (^view-fn :cascade-type) :view)))

(defn view-dispatcher 
  "Mapped to /view, this attempts to identify a namespace and a view function
  which is then invoked to render a DOM which is then streamed to the client."
  [env]
  (let [split-path (-> env :cascade :split-path)
        [_ fn-namespace fn-name] split-path
        view-ns (and fn-namespace (find-ns (symbol fn-namespace)))
        view-fn (and view-ns fn-name (ns-resolve view-ns (symbol fn-name)))]
    (if (is-view-fn view-fn)
      (do (render-xml-response view-fn env) true)
      false)))

(assoc-in-config [:dispatchers "/view/"] view-dispatcher)  