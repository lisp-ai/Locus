(ns locus.set.action.global.object
  (:require [locus.set.logic.core.set :refer :all]
            [locus.set.logic.limit.product :refer :all]
            [locus.set.logic.structure.protocols :refer :all]
            [locus.con.core.setpart :refer :all]
            [locus.con.core.object :refer [projection]]
            [locus.set.mapping.general.core.object :refer :all]
            [locus.set.mapping.effects.global.transformation :refer :all]
            [locus.set.mapping.effects.global.permutation :refer :all]
            [locus.set.quiver.relation.binary.br :refer :all]
            [locus.set.quiver.relation.binary.sr :refer :all]
            [locus.set.quiver.relation.binary.vertexset :refer :all]
            [locus.set.quiver.relation.binary.product :refer :all]
            [locus.set.quiver.structure.core.protocols :refer :all]
            [locus.set.copresheaf.structure.core.protocols :refer :all]
            [locus.set.copresheaf.incidence.system.family :refer :all]
            [locus.set.quiver.diset.core.object :refer :all]
            [locus.set.square.core.morphism :refer :all]
            [locus.order.lattice.core.object :refer :all]
            [locus.algebra.commutative.semigroup.object :refer :all]
            [locus.algebra.semigroup.core.object :refer :all]
            [locus.algebra.semigroup.core.morphism :refer :all]
            [locus.algebra.semigroup.monoid.object :refer :all]
            [locus.algebra.semigroup.monoid.morphism :refer :all]
            [locus.algebra.group.core.object :refer :all]
            [locus.algebra.semigroup.monoid.end :refer :all]
            [locus.set.action.core.protocols :refer :all]
            [locus.algebra.commutative.monoid.arithmetic :refer :all])
  (:import (locus.order.lattice.core.object Lattice)
           (locus.set.mapping.effects.global.transformation Transformation)
           (locus.set.mapping.effects.global.permutation Permutation)
           (locus.algebra.semigroup.monoid.object Monoid)
           (locus.algebra.group.core.object Group)))

; The topos of monoid actions is the topos of all set valued functors
; arising from a given base monoid M. Then all topos theoretic properties of the
; topos of monoid actions are addressed by reference to the general properties
; of the topos of copresheaves over a given category C.
(deftype MSet [monoid coll action]
  ConcreteObject
  (underlying-set [this]
    coll)

  EffectSystem
  (actions [this]
    (underlying-set monoid))
  (action-domain [this elem] coll)
  (apply-action [this elem arg] (action elem arg)))

(derive MSet :locus.set.copresheaf.structure.core.protocols/mset)

; The action functions of a monoid action
(defn action-function
  [ms action]

  (->SetFunction
    (underlying-set ms)
    (underlying-set ms)
    (fn [x]
      (apply-action ms action x))))

; Component sets and functions for monoid actions
(defmethod get-set :locus.set.copresheaf.structure.core.protocols/mset
  [mset x]

  (case x
    0 (underlying-set mset)))

(defmethod get-function :locus.set.copresheaf.structure.core.protocols/mset
  [mset m]

  (action-function mset m))

; Apply all actions of a MSet to a collection
(defn mset-set-image
  [mset coll]

  (apply
    union
    (set
      (map
        (fn [action]
          (set
            (map
              (fn [elem]
                (apply-action mset action elem))
              coll)))
        (actions mset)))))

(defn mset-set-inverse-image
  [mset coll]

  (set
    (filter
      (fn [elem]
        (superset? (list (mset-set-image mset #{elem}) coll)))
      (underlying-set mset))))

(defmethod image
  [:locus.set.copresheaf.structure.core.protocols/mset :locus.set.logic.core.set/universal]
  [mset coll]

  (mset-set-image mset coll))

(defmethod inverse-image
  [:locus.set.copresheaf.structure.core.protocols/mset :locus.set.logic.core.set/universal]
  [mset coll]

  (mset-set-inverse-image mset coll))

; Change the monoid of an mset by using a monoid homomorphism
(defn change-of-monoid
  [func ms]

  (MSet.
    (source-object func)
    (.coll ms)
    (fn [action x]
      (apply-action ms (func action) x))))

; This creates a trivial monoid action from a set
(defn trivial-mset
  [coll]

  (MSet.
    trivial-monoid
    coll
    (fn [action x] x)))

; Action enumeration
(defn action-transformation
  [ms action]

  (->Transformation
    (underlying-set ms)
    (fn [x]
      (apply-action ms action x))))

(defn action-permutation
  [gs action]
  {:pre (group? (.monoid gs))}

  (let [inverse-action ((.inv ^Group (.monoid gs)) action)]
    (->Permutation
      (underlying-set gs)
      (fn [x]
        (apply-action gs action x))
      (fn [x]
        (apply-action gs inverse-action x)))))

; This is the equivalent of hom classes in action theory
(defmethod action-representatives MSet
  [ms a b]

  (set
    (filter
      (fn [action]
        (= (apply-action ms action a) b))
      (actions ms))))

; Action preorders
(defmethod action-preorder MSet
  [ms]

  (let [coll (underlying-set ms)]
    (apply
      union
      (map
        (fn [action]
          (set
            (map
              (fn [c]
                (list c (apply-action ms action c)))
              coll)))
        (actions ms)))))

; Action equality
(defmethod action-equality MSet
  [ms]

  (pn
    (fn [a b]
      (every?
        (fn [i]
          (= (apply-action ms a i)
             (apply-action ms b i)))
        (underlying-set ms)))
    (actions ms)))

; The stabilizer of an element under a monoid action
(defn element-stabilizing-actions
  [mset x]

  (set
    (filter
      (fn [action]
        (= x (apply-action mset action x)))
      (actions mset))))

(defn pointwise-set-stabilizing-actions
  [mset coll]

  (set
    (filter
      (fn [action]
        (every?
          (fn [i]
            (= i (apply-action mset action i)))
          coll))
      (actions mset))))

(defn setwise-set-stabilizing-actions
  [mset coll]

  (set
    (filter
      (fn [action]
        (every?
          (fn [i]
            (coll (apply-action mset action i)))
          coll))
      (actions mset))))

; Fixed and moved elements
(defn fixed-element?
  [mset elem]

  (every?
    (fn [action]
      (= elem (apply-action mset action elem)))
    (actions mset)))

(defn fixed-elements
  [mset]

  (set
    (filter
      (partial fixed-element? mset)
      (underlying-set mset))))

(defn moved-element?
  [mset elem]

  (not (fixed-element? mset elem)))

(defn moved-elements
  [mset]

  (set
    (filter
      (partial moved-element? mset)
      (underlying-set mset))))

; We can convert a monoid action into a homomorphism from the
; original monoid to the full transformation monoid of a set.
(defmethod action-homomorphism MSet
  [ms]

  (->MonoidMorphism
    (.monoid ms)
    (end (underlying-set ms))
    (fn [action]
      (underlying-function (action-transformation ms action)))))

; We need some way of computing the products and coproducts of
; monoid actions in each of their respective topoi.
(defn action-product
  [& monoid-actions]

  (MSet.
    (.monoid (first monoid-actions))
    (apply
      cartesian-product
      (map underlying-set monoid-actions))
    (fn [action coll]
      (map-indexed
        (fn [i v]
          (apply-action (nth monoid-actions i) action v))
        coll))))

(defn action-coproduct
  [& monoid-actions]

  (MSet.
    (.monoid (first monoid-actions))
    (apply
      cartesian-coproduct
      (map underlying-set monoid-actions))
    (fn [action [i v]]
      (list i (apply-action (nth monoid-actions i) action v)))))

(defmethod product MSet
  [& monoid-actions]

  (apply action-product monoid-actions))

(defmethod coproduct MSet
  [& monoid-actions]

  (apply action-coproduct monoid-actions))

; We need to be able to deal with the subobject lattices of monoid actions
(defn restrict-mset
  [ms coll]

  (MSet.
    (.monoid ms)
    coll
    (.action ms)))

(defn mset-subalgebras
  [ms]

  (filters (action-preorder ms)))

(defmethod sub MSet
  [ms]

  (Lattice.
    (mset-subalgebras ms)
    union
    intersection))

; This is our chance to define the congruence lattices of monoid actions
(defn mset-congruence?
  [ms partition]

  (every?
    (fn [action]
      (equal-congruence?
        (action-transformation ms action)
        partition))
    (actions ms)))

(defn mset-congruences
  [ms]

  (set
    (filter
      (fn [partition]
        (mset-congruence? ms partition))
      (set-partitions (underlying-set ms)))))

(defmethod con MSet
  [ms]

  (Lattice.
    (mset-congruences ms)
    join-set-partitions
    meet-set-partitions))

(defn quotient-mset
  [ms partition]

  (MSet.
    (.monoid ms)
    partition
    (fn [action part]
      (let [current-action-function (.action ms)]
        (projection partition (current-action-function action (first part)))))))

; Get an mset directly from a transformation or permutation
(defmulti to-mset type)

(defmethod to-mset MSet
  [coll] coll)

(defmethod to-mset Transformation
  [transformation]

  (MSet.
    (let [[index period] (index-period transformation)] (monogenic-monoid index period))
    (underlying-set transformation)
    (fn [n x]
      (iteratively-apply transformation n x))))

(defmethod to-mset Permutation
  [permutation]

  (MSet.
    (cyclic-group (permutation-period permutation))
    (underlying-set permutation)
    (fn [n x]
      (iteratively-apply permutation n x))))

; The topos of c2-sets is the topos Sets^{C_2} which is distinguished by the fact that
; each of its elements are like max size two set partitions.
(defn c2-set
  [partition]

  (let [perm (involution-permutation partition)]
    (MSet.
      (cyclic-group 2)
      (underlying-set permutation)
      (fn [n x]
        (iteratively-apply perm n x)))))

; Actions on special structures
(defn sets-action
  [mset]

  (MSet.
    (.monoid mset)
    (->PowerSet (underlying-set mset))
    (fn [action coll]
      (set
        (map
          (fn [i]
            (apply-action mset action i))
          coll)))))

(def families-action
  (comp sets-action sets-action))

(defn relations-action
  [mset]

  (MSet.
    (.monoid mset)
    (->PowerSet (->CompleteRelation (underlying-set mset)))
    (fn [action rel]
      (set
        (map
          (fn [[a b]]
            (list
              (apply-action mset action a)
              (apply-action mset action b)))
          rel)))))

(defn set-pairs-action
  [mset]

  (product (sets-action mset) (sets-action mset)))

(defn set-partitions-action
  [mset]
  {:pre (group? (.-monoid mset))}

  (MSet.
    (.monoid mset)
    (set-partitions (underlying-set mset))
    (fn [action partition]
      (set
        (map
          (fn [part]
            (set
              (map
                (fn [i]
                  (apply-action mset action i))
                part)))
          partition)))))

; Permutation related actions
(defn permute-list
  [perm coll]

  (map
    (fn [i]
      (nth coll (perm i)))
    (range (count coll))))

(defn permutation-actions
  [mset coll]

  (for [action (actions mset)]
    (map
      (fn [i]
        (apply-action mset action i))
      (range (count coll)))))

; The theory of self induced actions is a fundamental part of monoid
; theory and so it must be addressed at the start of our discussion
; of monoid actions. At some point this may need to be reorganized
; or moved to another location in the computer algebra system.
(defn left-self-transformation
  [monoid x]

  (->Transformation
    (underlying-set monoid)
    (fn [arg]
      (monoid (list x arg)))))

(defn left-self-action
  [monoid]

  (MSet.
    monoid
    (underlying-set monoid)
    (fn [action x]
      (monoid (list action x)))))

(defn right-self-transformation
  [monoid x]

  (->Transformation
    (underlying-set monoid)
    (fn [arg]
      (monoid (list arg x)))))

(defn right-self-action
  [monoid]

  (MSet.
    (dual monoid)
    (underlying-set monoid)
    (fn [action x]
      (monoid (list x action)))))

(defn two-sided-self-transformation
  [monoid left-action right-action]

  (->Transformation
    (underlying-set monoid)
    (fn [arg]
      (monoid (list left-action (monoid (list arg right-action)))))))

(defn two-sided-self-action
  [monoid]

  (MSet.
    (product monoid (dual monoid))
    (underlying-set monoid)
    (fn [[left-action right-action] arg]
      (monoid (list left-action (monoid (list arg right-action)))))))

; Subalgebra actions
(defn left-subaction
  [monoid coll]

  (MSet.
    (restrict-monoid monoid coll)
    (underlying-set monoid)
    (fn [action x]
      (monoid (list action x)))))

(defn right-subaction
  [monoid coll]

  (MSet.
    (dual (restrict-monoid monoid coll))
    (underlying-set monoid)
    (fn [action x]
      (monoid (list x action)))))

(defn two-sided-subaction
  [parent-monoid coll]

  (let [monoid (restrict-monoid parent-monoid coll)]
    (MSet.
      (product monoid (dual monoid))
      (underlying-set monoid)
      (fn [[left-action right-action] arg]
        (monoid (list left-action (monoid (list arg right-action))))))))

(def left-subaction-preorder
  (comp action-preorder left-subaction))

(def right-subaction-preorder
  (comp action-preorder right-subaction))

(def two-sided-subaction-preorder
  (comp action-preorder two-sided-subaction))

; Various extra methods
(defn iterate-semigroup-element
  [semigroup x n]

  (if (= n 1)
    x
    (semigroup (list x (iterate-semigroup-element semigroup x (dec n))))))

(defn iterate-monoid-element
  [monoid x n]

  (if (zero? n)
    (.id monoid)
    (iterate-semigroup-element monoid x n)))

(defn iterate-group-element
  [group x n]

  (if (zero? n)
    (.id group)
    (if (neg? n)
      (iterate-semigroup-element
        group
        ((.inv group) x)
        (- n))
      (iterate-semigroup-element group x n))))

(defmulti iteration-action type)

(defmethod iteration-action :default
  [semigroup]

  (MSet.
    positive-integer-multiplication
    (underlying-set semigroup)
    (fn [n x] (iterate-semigroup-element semigroup x n))))

(defmethod iteration-action Monoid
  [monoid]

  (MSet.
    natural-multiplication
    (underlying-set monoid)
    (fn [n x] (iterate-monoid-element monoid x n))))

(defmethod iteration-action Group
  [group]

  (MSet.
    integer-multiplication
    (underlying-set group)
    (fn [n x] (iterate-group-element group x n))))

; Conjugation self actions of groups
(defn left-conjugation-action
  [group]

  (->MSet
    group
    (underlying-set group)
    (partial left-conjugate group)))

(defn right-conjugation-action
  [group]

  (->MSet
    group
    (underlying-set group)
    (partial right-conjugate group)))

; Ontology of monoid actions
; The fundamental constructs of our topos theoretic ontology are objects of topoi
; such as the topos of sets. This naturally also includes the topos of monoid actions,
; with which we can model a number of situations that emerge in abstract algebra.
(defmulti mset? type)

(defmethod mset? :locus.set.copresheaf.structure.core.protocols/mset
  [x] true)

(defmethod mset? :default
  [x] false)

; Special types of monoid actions
(defn faithful-mset?
  [ms]

  (and
    (mset? ms)
    (unary-family? (action-equality ms))))

(defn trivial-mset?
  [ms]

  (and
    (mset? ms)
    (unique-family? (action-equality ms))))

(defn transitive-mset?
  [ms]

  (and
    (mset? ms)
    (complete-relation? (action-preorder ms))))

(defn antisymmetric-mset?
  [ms]

  (and
    (mset? ms)
    (antisymmetric? (action-preorder ms))))

(defn gset?
  [ms]

  (and
    (mset? ms)
    (group? (.monoid ms))))

(defn trivial-gset?
  [ms]

  (and
    (gset? ms)
    (unary-family? (action-equality ms))))

(defn transitive-gset?
  [ms]

  (and
    (gset? ms)
    (complete-relation? (action-preorder ms))))

(defn faithful-gset?
  [ms]

  (and
    (gset? ms)
    (unary-family? (action-equality ms))))

(defn primitive-gset?
  [ms]

  (and
    (transitive-gset? ms)
    (<= (count (mset-congruences ms)) 2)))

(defn semiregular-mset?
  [ms]

  (and
    (mset? ms)
    (every?
      (fn [point]
        (= (count (element-stabilizing-actions ms point)) 1))
      (underlying-set ms))))

(def semiregular-gset?
  (intersection
    semiregular-mset?
    gset?))

(def regular-mset?
  (intersection
    semiregular-mset?
    transitive-mset?))

(def regular-gset?
  (intersection
    semiregular-gset?
    transitive-gset?))