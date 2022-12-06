(ns locus.additive.field.core.object
  (:require [locus.base.logic.core.set :refer :all]
            [locus.base.function.core.object :refer :all]
            [locus.base.logic.structure.protocols :refer :all]
            [locus.quiver.base.core.protocols :refer :all]
            [locus.elementary.copresheaf.core.protocols :refer :all]
            [locus.algebra.semigroup.core.object :refer :all]
            [locus.algebra.semigroup.monoid.object :refer :all]
            [locus.algebra.semigroup.monoid.arithmetic :refer :all]
            [locus.algebra.group.core.object :refer :all]
            [locus.additive.base.core.protocols :refer :all]
            [locus.additive.semiring.core.object :refer :all]
            [locus.additive.semifield.core.object :refer :all]
            [locus.additive.ring.core.object :refer :all]))

; A field is an arithmetic structure contain all relevant operations of
; addition, multiplication, subtraction, and division. Fields often emerge in
; commutative algebra from the quotients of commutative rings by maximal ideals,
; such as the unique maximal ideals of local rings, or by the field of fractions
; of an integral domain such as a quotient domain of a prime ideal.
(deftype SkewField [elems add mul]
  ConcreteObject
  (underlying-set [this] elems))

(derive SkewField :locus.additive.base.core.protocols/skew-field)

(defmethod additive-semigroup SkewField
  [^SkewField field]

  (.add field))

(defmethod multiplicative-semigroup SkewField
  [^SkewField field]

  (.mul field))

; A field should be constructed from an additive group and a multiplicative group with zero
(defmethod make-ring [:locus.elementary.copresheaf.core.protocols/group,
                      :locus.elementary.copresheaf.core.protocols/group-with-zero]
  [a b]

  (SkewField. (underlying-set a) a b))

; We also need to implement the modular multiplicative inverse
; The unique prime field of characteristic zero
(def qq
  (make-ring rational-addition rational-multiplication))
