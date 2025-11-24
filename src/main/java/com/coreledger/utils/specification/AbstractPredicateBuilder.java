package com.coreledger.utils.specification;

import cn.hutool.core.util.ArrayUtil;
import com.coreledger.function.ThreeFunction;
import com.google.common.collect.Lists;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractPredicateBuilder<T> {

    private static final int IN_MAX_SIZE = 1000;

    private final Predicate.BooleanOperator operator;
    protected final List<Specification<T>> specifications;


    protected AbstractPredicateBuilder(Predicate.BooleanOperator operator) {
        this.operator = operator;
        this.specifications = new ArrayList<>();
    }

    // 这个方法主要用来前置Predicate创建
    protected Predicate autoPartitionPre(Root<T> rt,
                                         CriteriaBuilder cb,
                                         String attribute,
                                         Collection<?> inRes,
                                         ThreeFunction<Root<T>, String, Collection<?>, Predicate> applyFunc) {
        if (inRes.size() > IN_MAX_SIZE) {
            return cb.or(Lists.partition(new ArrayList<>(inRes), IN_MAX_SIZE).stream()
                    .map(groupRes -> applyFunc.apply(rt, attribute, groupRes)).toArray(Predicate[]::new));
        } else {
            return applyFunc.apply(rt, attribute, inRes);
        }
    }

    protected Specification<T> buildSpecification() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate[] predicates = specifications.stream()
                    .map(spec -> spec.toPredicate(root, query, cb)).
                    toArray(Predicate[]::new);
            if (ArrayUtil.isEmpty(predicates)){
                return null;
            }else {
                return Predicate.BooleanOperator.OR.equals(operator) ? cb.or(predicates) : cb.and(predicates);
            }
        };
    }
}
