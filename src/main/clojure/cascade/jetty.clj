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

(ns 
  #^{:doc "Run Jetty server embedded"}
  cascade.jetty
  (:require cascade.filter)
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet ServletContextHandler FilterMapping DefaultServlet)))

(defn run-jetty
  "Starts an instance of the Jetty Server running.
  webapp defines the folder containing ordinary static resources (the docroot). 
  The default for the port parameter is 8080. Returns the new Jetty Server instance. No web.xml is necessary, a
  cascade.filter will automatically be installed."
  ([webapp] (run-jetty webapp 8080))
  ([#^String webapp port] 
  
  (let [server (Server. port)
        context (ServletContextHandler. server "/" false false)]
  (doto context
    (.setResourceBase webapp)
    (.setClassLoader (.. (Thread/currentThread) getContextClassLoader))
    (.addFilter cascade.filter "/*" FilterMapping/DEFAULT)
  (.addServlet DefaultServlet "/*")) 
  (doto server (.setHandler context) .start .join))))
