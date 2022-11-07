(ns locus.elementary.ternary-quiver.at.quiver
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.logic.limit.product :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.partition.core.setpart :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.relation.binary.br :refer :all]
            [locus.elementary.relation.binary.sr :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.elementary.quiver.core.object :refer :all]
            [locus.elementary.ternary-quiver.core.object :refer :all]
            ;[locus.elementary.semigroupoid.core.object :refer :all]
            ;[locus.elementary.category.core.object :refer :all]
            ;[locus.elementary.semigroup.core.object :refer :all]
            ))

; Algebraic ternary quivers: an algebraic ternary quiver is a thin ternary quiver in which all
; morphisms can be identified by the results of their first and second component functions.
; Then as it happens, the third component function is simply an algebraic binary operation on
; the morphisms of the ternary quiver, whose inputs are determined by their presentation as
; ordered pairs of their first and second parts. As it happens, the full subcategory of algebraic
; ternary quivers of the topos Sets^{T_{2,3}} is isomorphic to the category of partial magmas
; and partial magma homomorphisms.
(deftype ATQuiver [edges vertices op]
  StructuredDiset
  (first-set [this] edges)
  (second-set [this] vertices)

  ConcreteObject
  (underlying-set [this] (->CartesianCoproduct [(first-set [this]) (second-set [this])]))

  StructuredTernaryQuiver
  (first-component-fn [this] first)
  (second-component-fn [this] second)
  (third-component-fn [this] op))

(derive ATQuiver :locus.elementary.copresheaf.core.protocols/at-quiver)

; Create an algebraic ternary quiver if that is at all possible
(defn at-quiver
  [edges vertices op]

  (->ATQuiver
    edges
    vertices
    op))

(defn magma-quiver
  [vertices op]

  (->ATQuiver
    (->CompleteRelation vertices)
    vertices
    op))

; Get a multiplication map for an at-quiver
(defmethod display-table ATQuiver
  [^ATQuiver quiver] (display-table (third-component-function quiver)))

;(defmethod to-ternary-quiver :locus.elementary.copresheaf.core.protocols/semigroup
;  [semigroup]
;
;  (magma-quiver
;    (morphisms semigroup)
;    semigroup))
;
;(defmethod to-ternary-quiver :locus.elementary.copresheaf.core.protocols/semigroupoid
;  [semigroupoid]
;
;  (->ATQuiver
;    (composability-relation semigroupoid)
;    (morphisms semigroupoid)
;    semigroupoid))