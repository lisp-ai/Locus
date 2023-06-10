(ns locus.set.copresheaf.cube.core.object
  (:require [locus.set.logic.core.set :refer :all]
            [locus.set.logic.limit.product :refer :all]
            [locus.set.logic.sequence.object :refer :all]
            [locus.con.core.setpart :refer :all]
            [locus.set.logic.structure.protocols :refer :all]
            [locus.set.mapping.general.core.object :refer :all]
            [locus.set.mapping.general.core.util :refer :all]
            [locus.set.mapping.effects.global.identity :refer :all]
            [locus.set.quiver.relation.binary.product :refer :all]
            [locus.set.quiver.relation.binary.br :refer :all]
            [locus.set.quiver.relation.binary.sr :refer :all]
            [locus.set.copresheaf.structure.core.protocols :refer :all]
            [locus.set.quiver.structure.core.protocols :refer :all]
            [locus.set.square.core.morphism :refer :all])
  (:import (locus.set.square.core.morphism SetSquare)
           (locus.set.mapping.general.core.object SetFunction)))

; Cubes are morphisms in the topos of diamonds
; At the same time, they themselves are also copresheaves in the topos of
; copresheaves over the cube shaped partial order. Diamond copresheaves play
; such a central role in mathematics, that we must have a special class
; defined for dealing with them.
(deftype Cube [source target f g h i]
  AbstractMorphism
  (source-object [this] source)
  (target-object [this] target))

; Component arrows of cube copresheaves
(defn source-input-function
  [^Cube cube]

  (SetFunction.
    (source-input-set (source-object cube))
    (source-input-set (target-object cube))
    (.-f cube)))

(defn source-output-function
  [^Cube cube]

  (SetFunction.
    (source-output-set (source-object cube))
    (source-output-set (target-object cube))
    (.-g cube)))

(defn target-input-function
  [^Cube cube]

  (SetFunction.
    (target-input-set (source-object cube))
    (target-input-set (target-object cube))
    (.-h cube)))

(defn target-output-function
  [^Cube cube]

  (SetFunction.
    (target-output-set (source-object cube))
    (target-output-set (target-object cube))
    (.-i cube)))

(defn cube-component-function
  [cube [a b]]

  (case [a b]
    [0 0] (source-input-function cube)
    [0 1] (source-output-function cube)
    [1 0] (target-input-function cube)
    [1 1] (target-output-function cube)))

; Components of cube copresheaves
(defmethod get-set Cube
  [cube [i v]]

  (case i
    0 (get-set (source-object cube) v)
    1 (get-set (target-object cube) v)))

(defmethod get-function Cube
  [cube [[i v] [j w]]]

  (case [i j]
    [0 0] (get-function (source-object cube) [v w])
    [1 1] (get-function (target-object cube) [v w])
    [0 1] (compose
            (get-function (target-object cube) [v w])
            (cube-component-function cube v))))

; Constructors for cubes
(defmethod identity-morphism SetSquare
  [square]

  (Cube. square square identity identity identity identity))

(defmethod compose* Cube
  [a b]

  (Cube.
    (source-object b)
    (target-object a)
    (comp (.f a) (.f b))
    (comp (.g a) (.g b))
    (comp (.h a) (.h b))
    (comp (.i a) (.i b))))

; Products and coproducts in the topos of cubes
(defmethod product Cube
  [& args]

  (->Cube
    (apply product (map source-object args))
    (apply product (map target-object args))
    (apply product (map source-input-function args))
    (apply product (map source-output-function args))
    (apply product (map target-input-function args))
    (apply product (map target-output-function args)) ))

(defmethod coproduct Cube
  [& args]

  (->Cube
    (apply coproduct (map source-object args))
    (apply coproduct (map target-object args))
    (apply coproduct (map source-input-function args))
    (apply coproduct (map source-output-function args))
    (apply coproduct (map target-input-function args))
    (apply coproduct (map target-output-function args))))

; Subobject classifiers in the topos Sets^{[1,2,1]}
(def truth-square
  (let [in '#{((0 0) (1/3 1/3) (1/3 1/2) (1/2 1/3) (1/2 1/2) (1 1))}
        middle '#{0 1/2 1}
        out #{0 1}
        upper-function (mapfn {0 0, 1/2 1, 1 1})
        first-function (SetFunction.
                         in
                         middle
                         (fn [[a b]]
                           (cond
                             (= a 0) 0
                             (= a 1/3) 1/2
                             :else 1)))
        second-function (SetFunction.
                          in
                          middle
                          (fn [[a b]]
                            (cond
                              (= b 0) 0
                              (= b 1/3) 1/2
                              :else 1)))]
    (SetSquare.
      first-function
      upper-function
      second-function
      upper-function)))

(defn subsquare-truth
  [diamond new-source-inputs new-source-outputs new-target-inputs new-target-outputs]

  (->Cube
    diamond
    truth-square
    (fn [source-input]
      (if (contains? new-source-inputs source-input)
        (list 1 1)
        (list
          (cond
            (contains? new-source-outputs ((source-object diamond) source-input)) 1/2
            (contains? new-target-outputs ((common-composite-set-function diamond) source-input)) 1/3
            :else 0)
          (cond
            (contains? new-target-inputs ((first-function diamond) source-input)) 1/2
            (contains? new-target-outputs ((common-composite-set-function diamond) source-input)) 1/3
            :else 0))))
    (fn [source-output]
      (cond
        (contains? new-source-outputs source-output) 1
        (contains? new-target-outputs ((second-function diamond) source-output)) 1/2
        :else 0))
    (fn [target-input]
      (cond
        (contains? new-target-inputs target-input) 1
        (contains? new-target-outputs ((target-object diamond) target-input)) 1/2
        :else 0))
    (fn [target-output]
      (cond
        (contains? new-source-outputs target-output) 1
        :else 0))))

; Ontology of cubes
(defn cube?
  [x]

  (= (type x) Cube))

(defn endocube?
  [cube]

  (and
    (cube? cube)
    (= (source-object cube) (target-object cube))))

(defn identity-cube?
  [cube]

  (and
    (cube? cube)
    (identity-function? (source-input-function cube))
    (identity-function? (source-output-function cube))
    (identity-function? (target-input-function cube))
    (identity-function? (target-output-function cube))))