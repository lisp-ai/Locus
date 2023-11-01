(defproject locus "1.5.6-SNAPSHOT"
  :description "A specialized computer algebra system for topos theory."
  :license {:name "Apache License"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [; language libraries
                 [org.clojure/clojure "1.11.1"]

                 ; apache commons libraries
                 [org.apache.commons/commons-math3 "3.6.1"]

                 ; utility libraries
                 [org.ow2.sat4j/org.ow2.sat4j.core "2.3.6"]

                 ; javafx
                 [org.openjfx/javafx-base "19-ea+8"]
                 [org.openjfx/javafx-controls "19-ea+8"]
                 [org.openjfx/javafx-graphics "19-ea+8"]
                 [org.openjfx/javafx-swing "19-ea+8"]

                 ; utility visualisation libraries
                 [dorothy "0.0.7"]
                 [org.scilab.forge/jlatexmath "1.0.7"]]

  :main locus.sub.mapping.function

  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :target-path "target/%s")
