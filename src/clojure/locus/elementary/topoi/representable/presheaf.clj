(ns locus.elementary.topoi.representable.presheaf
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.sequence.core.object :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.quiver.base.core.protocols :refer :all]
            [locus.quiver.relation.binary.sr :refer :all]
            [locus.quiver.unary.core.morphism :refer :all]
            [locus.quiver.binary.core.object :refer :all]
            [locus.quiver.binary.core.morphism :refer :all]
            [locus.elementary.quiver.unital.object :refer :all]
            [locus.elementary.quiver.unital.morphism :refer :all]
            [locus.order.general.core.object :refer :all]
            [locus.order.general.core.morphism :refer :all]
            [locus.algebra.category.core.object :refer :all]
            [locus.algebra.category.core.morphism :refer :all]
            [locus.algebra.category.core.contravariant-functor :refer :all]
            [locus.elementary.topoi.copresheaf.object :refer :all]
            [locus.elementary.topoi.presheaf.object :refer :all]))

; A presheaf Hom(-,b)
(deftype RepresentablePresheaf [category object]
  AbstractMorphism
  (source-object [this] (dual category))
  (target-object [this] sets)

  StructuredDifunction
  (first-function [this]
    (fn [arrow]
      (let [[x y] (transition category arrow)]
        (->SetFunction
          (quiver-hom-class category y object)
          (quiver-hom-class category x object)
          (fn [morphism]
            (category (list morphism arrow)))))))
  (second-function [this]
    (fn [x]
      (quiver-hom-class category x object))))

; Get the sets and objects of representable presheaves
(defmethod get-set RepresentablePresheaf
  [presheaf x]

  ((second-function presheaf) x))

(defmethod get-function RepresentablePresheaf
  [presheaf x]

  ((first-function presheaf) x))

; The index categories of representable presheaves
(defmethod index RepresentablePresheaf
  [presheaf] (source-object presheaf))

; Convert representable presheaves into functors
(defmethod to-functor RepresentablePresheaf
  [presheaf]

  (->Functor
    (source-object presheaf)
    (target-object presheaf)
    (partial get-set presheaf)
    (partial get-function presheaf)))

; A natural transformation of Hom(-,b) functors
(deftype MorphismOfRepresentablePresheaves [category morphism]
  AbstractMorphism
  (source-object [this] (RepresentablePresheaf. category (source-element category morphism)))
  (target-object [this] (RepresentablePresheaf. category (target-element category morphism)))

  clojure.lang.IFn
  (invoke [this x]
    (let [[b c] (transition category morphism)]
      (->SetFunction
        (quiver-hom-class category x b)
        (quiver-hom-class category x c)
        (fn [argument-arrow]
          (category (list morphism argument-arrow))))))
  (applyTo [this args]
    (clojure.lang.AFn/applyToHelper this args)))

(defmethod identity-morphism RepresentablePresheaf
  [^RepresentablePresheaf presheaf]

  (let [category (.-category presheaf)
        obj (.-object presheaf)]
    (MorphismOfRepresentablePresheaves. category (identity-morphism-of category obj))))

(defmethod compose* MorphismOfRepresentablePresheaves
  [^MorphismOfRepresentablePresheaves a, ^MorphismOfRepresentablePresheaves b]

  (let [category (.-category b)
        f (.-morphism a)
        g (.-morphism b)]
    (MorphismOfRepresentablePresheaves.
      category
      (category (list f g)))))