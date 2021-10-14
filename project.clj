(defproject pedestal-sample "0.1.0-SNAPSHOT"
  :description "Pedestal Sample"

  :dependencies [[ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [hiccup "1.0.5"]
                 [io.pedestal/pedestal.service "0.5.9"]
                 [io.pedestal/pedestal.route "0.5.9"]
                 ;[io.pedestal/pedestal.jetty "0.5.9"]
                 [io.pedestal/pedestal.immutant "0.5.9"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [nubank/matcher-combinators "3.3.1"]]

  :main ^:skip-aot pedestal-sample.main

  :target-path "target/%s"

  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
