(ns locus.algebra.group.core.aut
  (:require [locus.set.logic.core.set :refer :all]
            [locus.set.logic.structure.protocols :refer :all]
            [locus.set.copresheaf.structure.core.protocols :refer :all]
            [locus.set.mapping.general.core.object :refer :all]
            [locus.set.mapping.general.core.util :refer :all]
            [locus.set.copresheaf.bijection.core.object :refer :all]
            [locus.set.square.core.morphism :refer :all]
            [locus.set.copresheaf.bijection.core.morphism :refer :all]
            [locus.set.quiver.diset.core.object :refer :all]
            [locus.set.quiver.diset.core.morphism :refer :all]
            [locus.algebra.category.hom.sethom :refer :all]
            [locus.algebra.category.hom.bhom :refer :all]
            [locus.algebra.category.hom.dhom :refer :all]
            [locus.algebra.category.hom.funhom :refer :all]
            [locus.algebra.commutative.semigroup.object :refer :all]
            [locus.algebra.semigroup.core.object :refer :all]
            [locus.algebra.semigroup.monoid.object :refer :all]
            [locus.algebra.group.core.object :refer :all]
            [locus.set.quiver.structure.core.protocols :refer :all])
  (:import (locus.set.mapping.general.core.object SetFunction)
           (locus.set.quiver.diset.core.object Diset)
           (locus.set.copresheaf.bijection.core.object Bijection)))

; Let C be a category, then the set of all isomorphisms of C forms a wide subcategory called
; the underlying groupoid of C. It follows that every category has an underlying groupoid.
; The automorphism group of an object X in a category is the hom class Hom(X,X) of X
; in the underlying groupoid. It follows that the definition of automorphism groups involves
; two components: the enumeration theory of hom classes and the construction of
; underlying groupoids of categories.

; The special case of sets
(defn set-automorphism-group
  [coll]

  (->Group
    (bijective-set-hom coll coll)
    (fn [[a b]]
      (compose a b))
    (identity-function coll)
    invert-function))

; Computation of automorphism groups by a multimethod
(defmulti aut type)

(defmethod aut :locus.set.logic.core.set/universal
  [coll]

  (set-automorphism-group coll))

(defmethod aut SetFunction
  [func]

  (->Monoid
    (function-isomorphisms func func)
    (fn [[a b]]
      (compose a b))
    (identity-morphism func)))

(defmethod aut Diset
  [pair]

  (->Monoid
    (diset-isomorphisms pair pair)
    (fn [[a b]]
      (compose a b))
    (identity-morphism pair)))

(defmethod aut Bijection
  [func]

  (->Monoid
    (bijection-isomorphisms func func)
    (fn [[a b]]
      (compose a b))
    (identity-morphism func)))