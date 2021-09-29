(defproject pedestal-sample "0.1.0-SNAPSHOT"
  :description "Pedestal Sample"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.9"]
                 [io.pedestal/pedestal.route "0.5.9"]
                 [io.pedestal/pedestal.jetty "0.5.9"]
                 [org.slf4j/slf4j-simple "1.7.32"]
                 [org.clojure/data.json "2.4.0"]]

  :main ^:skip-aot pedestal-sample.main

  :target-path "target/%s"

  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
