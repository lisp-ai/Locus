(ns locus.graphics.graph.view
  (:require [locus.elementary.logic.base.core :refer :all]
            [locus.elementary.incidence.system.setpart :refer :all]
            [locus.elementary.function.core.protocols :refer :all]
            [locus.elementary.function.core.object :refer :all]
            [locus.elementary.quiver.core.object :refer :all])
  (:import (locus.graph DenseDigraph IDigraph IGraph DenseGraph)
           (locus.graph.factories BasicGraphFactory BasicDigraphFactory)))

(require '[dorothy.core :as dot])
(require '[dorothy.jvm :refer (render save! show!)])

; Boolean matrices are good for Java interop
(defn boolean-matrix
  [coll]

  (if (empty? coll)
    (make-array Boolean/TYPE 0 0)
    (into-array
      (map
        boolean-array
        coll))))

(defn make-digraph
  [coll]

  (DenseDigraph.
    (boolean-matrix
      coll)))

(defn make-graph
  [coll]

  (DenseGraph.
    (boolean-matrix
      coll)))

; Unlabeled graph visualisation
(defn unlabeled-vertices
  [coll]

  (map
    (fn [i]
      [(.toString i) {:label "", :style "filled", :fillcolor "cadetblue1"}])
    coll))

(defn unlabeled-edges
  [coll]

  (map
    (fn [pair]
      (let [[a b] (if (universal? pair)
                    (if (= (count pair) 1)
                      [(first pair) (first pair)]
                      (seq pair))
                    pair)]
        [(.toString a) (.toString b) {:label ""}]))
    coll))

(defn unlabeled-digraph
  [vertices edges]

  (dot/digraph
    (vec
      (concat
        (list [{:rankdir "BT"}])
        (unlabeled-vertices vertices)
        (unlabeled-edges edges)))))

(defn unlabeled-graph
  [vertices edges]

  (dot/graph
    (vec
      (concat
        (unlabeled-vertices vertices)
        (unlabeled-edges edges)))))

(defmethod visualize IDigraph
  [^IDigraph digraph]

  (-> (dot/dot (unlabeled-digraph (set (range (.order digraph))) (set (.edgeLocations digraph)))) show!))

(defmethod visualize IGraph
  [^IGraph graph]


  (-> (dot/dot (unlabeled-graph (set (range (.order graph))) (set (.edgeLocations graph)))) show!))

