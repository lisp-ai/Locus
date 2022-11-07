(ns locus.elementary.category.relation.set-relation
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.logic.limit.product :refer :all]
            [locus.elementary.relation.binary.product :refer :all]
            [locus.elementary.relation.binary.br :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.base.partition.core.object :refer [projection]]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.elementary.bijection.core.object :refer :all]
            [locus.base.function.image.image-function :refer :all])
  (:import (locus.base.function.core.object SetFunction)
           (locus.elementary.bijection.core.object Bijection)
           (clojure.lang IPersistentMap)
           (locus.base.function.image.image_function ImageFunction)))

; Set relations:
; The category Rel of sets and relations does not form an elementary topos. As a consequence,
; it lacks many of the desirable features of a topos. In order to get around this, we represent
; Rel as a concrete subcategory of Sets consisting of image functions, where an image
; function is a complete union homomorphism of power sets. The category Rel can then be
; embedded in the topos Sets, with reference to this special class of functions.

; In our implementation of set relations, you can convert a set relation into an image
; function by using the to-function method. In the other direction, we provide the
; singleton-images-relation function to convert a member of the image functions class
; into a set relation. This lets us transfer back and forth between the category Rel
; and the topos Sets.

; While a set relation in Rel is primarily related to an image function of power
; sets, another classes of functions corresponds to set relations: the set valued
; functions produced by singleton images. This correspondence states that a set
; relation from A to B is like a function from A to the power set of B. As a consequence,
; set relations implement the clojure.lang.IFn interface in such a manner that
; the application of an element a is the set of elements b that form ordered pairs
; in the set relation.

; Set relations are important in the topos theoretic foundations of computing
; as a means of defining an abstraction layer over the topoi of sets and functions.
; Therefore, the terminology that we use in much of this file is determined by the
; needs of topos theory. In particular, we use make the definition of the relational
; image and inverse image correspond to the definitions of partition images and
; inverse images used in the topos theoretic models of dataflow. The converse image
; is then defined separately from the relational inverse image.

; These concepts allow us to define a subalgebra lattice of a set relation, which
; is the lattice that is mapped into the lattice of congruences of a function.
; This subalgebra lattice is basically implemented in the lattice folder. It restores
; the subobject lattice of a function in the special case in which a function is
; expressed as a set relation.
(deftype SetRelation [source target func]
  AbstractMorphism
  (source-object [this] source)
  (target-object [this] target)

  StructuredDiset
  (first-set [this] source)
  (second-set [this] target)

  clojure.lang.IFn
  (invoke [this arg]
    (func arg))
  (applyTo [this args]
    (clojure.lang.AFn/applyToHelper this args)))

(defmethod to-function SetRelation
  [^SetRelation rel]

  (ImageFunction.
    (.source rel)
    (.target rel)
    (.func rel)))

; Underlying relations of visualisation of set relations
(defmethod underlying-relation SetRelation
  [rel]

  (apply
    union
    (map
      (fn [input]
        (set
          (map
            (fn [i]
              (list input i))
            (rel input))))
      (first-set rel))))

(defmethod visualize SetRelation
  [rel] (visualize (underlying-relation rel)))

; Set relations form a category Rel of sets and relations
(defmethod compose* SetRelation
  [a b]

  (SetRelation.
    (source-object b)
    (target-object a)
    (fn [x]
      (apply union (map a (b x))))))

(defn identity-relation
  [coll]

  (SetRelation.
    coll
    coll
    (fn [i] #{i})))

; Generalized conversion mechanisms for set relations
(defmulti to-set-relation type)

(defmethod to-set-relation SetRelation
  [rel] rel)

(defmethod to-set-relation :locus.base.logic.core.set/universal
  [rel]

  (SetRelation.
    (relation-domain rel)
    (relation-codomain rel)
    (fn [x]
      (set (for [[a b] rel
                 :when (= a x)]
             b)))))

(defmethod to-set-relation :locus.base.logic.structure.protocols/set-function
  [func]

  (SetRelation.
    (inputs func)
    (outputs func)
    (fn [x]
      #{(func x)})))

(defmethod to-set-relation Bijection
  [bijection]

  (to-set-relation (underlying-function bijection)))

(defmethod to-set-relation IPersistentMap
  [coll]

  (SetRelation.
    (set (keys coll))
    (set (vals coll))
    (fn [i]
      #{(get coll i)})))

; Rel is a dagger category with its inverse being the converse operation
(defn relational-fiber
  [rel target-element]

  (set
    (filter
      (fn [i]
        (contains? (rel i) target-element))
      (first-set rel))))

(defn converse-set-relation
  [rel]

  (SetRelation.
    (target-object rel)
    (source-object rel)
    (fn [target-element]
      (relational-fiber rel target-element))))

(defn relational-image
  [rel coll]

  (apply
    union
    (map
      (fn [i]
        (rel i))
      coll)))

(defn converse-relation-image
  [rel coll]

  (apply
    union
    (map
      (fn [i]
        (relational-fiber rel i))
      coll)))

; Adjoin inputs and outputs to set relations
(defmethod adjoin-inputs SetRelation
  [rel coll]

  (SetRelation.
    (union coll (source-object rel))
    (target-object rel)
    (fn [i]
      (rel i))))

(defmethod adjoin-outputs SetRelation
  [rel coll]

  (SetRelation.
    (source-object rel)
    (union coll (target-object rel))
    (fn [i]
      (rel i))))

; Set relations have the property that every pair (X,Y) that is a subset of their inputs and their
; outputs can be used to create a subobject.
(defn set-subrelation
  [rel new-in new-out]

  (->SetFunction
    new-in
    new-out
    (fn [i]
      (intersection new-out (rel i)))))

(defn restrict-set-relation
  [rel new-in]

  (SetRelation.
    new-in
    (target-object rel)
    (fn [i]
      (rel i))))

(defn restrict-set-relation-target
  [rel new-out]

  (set-subrelation rel (source-object rel) new-out))

; Set relations also have the property that every pair (P,Q) of partitions induces a quotient
; relation as congruences are simply ways of keeping things single-valued and that is not
; an issue here.
(defn quotient-set-relation
  [rel in-partition out-partition]

  (SetRelation.
    in-partition
    out-partition
    (fn [in-part]
      (let [outs (relational-image rel in-part)]
        (set
          (map
           (fn [out]
             (projection out-partition out))
           outs))))))

; Although set relations have the property that every pair of sets (X,y) induces a corresponding
; subobject we still have a concept of closure that is worth examining.
(defn set-relation-closed-set?
  [rel a b]

  (every?
    (fn [i]
      (superset? (list (rel i) b)))
    a))

(defn set-relation-closed-sets
  [rel]

  (let [in (source-object rel)
        out (target-object rel)]
    (mapcat
      (fn [new-source]
        (let [current-image (relational-image rel new-source)]
          (map
            (fn [i]
              (list new-source (union current-image i)))
            (power-set (difference out current-image)))))
      (->PowerSet in))))

(defn relation-closed-inverse-image
  [rel coll]

  (set
    (filter
      (fn [i]
        (superset? (list (rel i) coll)))
      (first-set rel))))

; Set relation triples
(defn relation-triple
  [rel]

  (list
    (source-object rel)
    (target-object rel)
    (underlying-relation rel)))

; Convert an image function into a set relation
(defn singleton-images-relation
  [^ImageFunction func]

  (->SetRelation
    (.source func)
    (.target func)
    (.func func)))

; Hom classes in Rel are partially ordered and complemented
(defn empty-set-relation
  [source target]

  (SetRelation.
    source
    target
    (fn [i]
      #{})))

(defn complete-set-relation
  [source target]

  (SetRelation.
    source
    target
    (fn [i]
      target)))

(defn complement-set-relation
  [rel]

  (let [in (source-object rel)
        out (target-object rel)]
    (SetRelation.
      in
      out
      (fn [i]
        (difference in (rel i))))))

; Convert between set relations and multivalued functions
(defn set-relation->multivalued-function
  [func]

  (SetFunction.
    (source-object func)
    (->PowerSet (target-object func))
    (fn [x]
      (func x))))

(defn multivalued-function->set-relation
  [func]

  (SetRelation.
    (inputs func)
    (dimembers (outputs func))
    (fn [i]
      (func i))))

; Products and coproducts of set relations
(defmethod product SetRelation
  [& relations]

  (SetRelation.
    (apply product (map source-object relations))
    (apply product (map target-object relations))
    (fn [coll]
      (apply
        product
        (map-indexed
         (fn [i v]
           ((nth relations i) v))
         coll)))))

(defmethod coproduct SetRelation
  [& relations]

  (SetRelation.
    (apply coproduct (map source-object relations))
    (apply coproduct (map target-object relations))
    (fn [[i v]]
      (set
        (map
          (fn [w]
            (list i w))
          ((nth relations i) v))))))

; The relational hom of two sets
(defn included-set-relation?
  [a b]

  (and
    (superset? (list (source-object a) (source-object b)))
    (superset? (list (target-object a) (target-object b)))
    (every?
      (fn [i]
        (superset? (list (a i) (b i))))
      (source-object a))))

(defn relational-hom-class
  [a b]

  (->Universal
    (fn [rel]
      (and
        (= (type rel) SetRelation)
        (equal-universals? a (source-object rel))
        (equal-universals? b (target-object rel))))))

; Ontology of morphisms in the allegory Rel of sets and relations
(defn set-relation?
  [rel]

  (= (type rel) SetRelation))

(defn functional-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (<= (count (rel i)) 1))
      (first-set rel))))

(defn reversible-functional-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (loop [coll (seq (first-set rel))
           outputs #{}]
      (if (empty? coll)
        true
        (let [next-input (first coll)
              current-outputs (rel next-input)]
          (and
            (= (count current-outputs) 1)
            (let [next-output (first current-outputs)]
              (and
                (not (contains? outputs next-output))
                (recur
                  (rest coll)
                  (conj outputs next-output))))))))))

(defn functional-set-endorelation?
  [rel]

  (and
    (functional-set-endorelation? rel)
    (= (source-object rel) (target-object rel))))

(defn reversible-functional-set-endorelation?
  [rel]

  (and
    (reversible-functional-set-relation? rel)
    (= (source-object rel) (target-object rel))))

(defn coreflexive-set-relation?
  [rel]

  (and
    (functional-set-endorelation? rel)
    (every?
      (fn [i]
        (or
          (= (rel i) #{})
          (= (rel i) #{i})))
      rel)))

(defn total-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (not (empty? (rel i))))
      (first-set rel))))

(defn functional-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (= (count (rel i)) 1))
      (first-set rel))))

(defn inverse-functional-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (= (count (converse-relation-image rel #{i})) 1))
      (second-set rel))))

(defn set-endorelation?
  [rel]

  (and
    (set-relation? rel)
    (= (source-object rel) (target-object rel))))

(defn reflexive-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (contains? (rel i) i))
      (source-object rel))))

(defn irreflexive-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (every?
      (fn [i]
        (not (contains? (rel i) i)))
      (source-object rel))))

(defn reflexive-set-endorelation?
  [rel]

  (and
    (set-endorelation? rel)
    (reflexive-set-relation? rel)))

(defn irreflexive-set-endorelation?
  [rel]

  (and
    (set-endorelation? rel)
    (irreflexive-set-relation? rel)))

(defn symmetric-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (= (source-object rel) (target-object rel))
    (symmetric-binary-relation? (underlying-relation rel))))

(defn antisymmetric-set-relation?
  [rel]

  (and
    (set-relation? rel)
    (antisymmetric? (underlying-relation rel))))

