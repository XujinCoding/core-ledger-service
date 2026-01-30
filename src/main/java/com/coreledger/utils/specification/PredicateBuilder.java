package com.coreledger.utils.specification;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public class PredicateBuilder<T> extends AbstractPredicateBuilder<T> {

    public static String escapeLikeValue(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }


    // switch 21支持模式匹配，在此处扩展
    java.util.function.Predicate<Object> NO_NULL_PREDICATE = ObjectUtil::isNotEmpty;

    private PredicateBuilder(Predicate.BooleanOperator operator) {
        super(operator);
    }

    // ======================= >>> 推荐使用
    // ======================= >>> apply by no_null_predicate.
    public PredicateBuilder<T> equal(String attribute, Object value) {
        return this.equal(NO_NULL_PREDICATE, attribute, value);
    }

    public PredicateBuilder<T> notEqual(String attribute, Object value) {
        return this.notEqual(NO_NULL_PREDICATE, attribute, value);
    }

    public PredicateBuilder<T> in(String attribute, Collection<?> collection) {
        return this.in(CollectionUtil::isNotEmpty, attribute, collection);
    }


    // ======================= >>> 推荐使用
    // ======================= >>> apply by predicate.
    public <PP> PredicateBuilder<T> equal(java.util.function.Predicate<PP> pre, String attribute, PP value) {
        return equal(pre.test(value), attribute, value);
    }

    public <PP> PredicateBuilder<T> notEqual(java.util.function.Predicate<PP> pre, String attribute, PP value) {
        return notEqual(pre.test(value), attribute, value);
    }

    public PredicateBuilder<T> in(java.util.function.Predicate<Collection<?>> pre, String attribute, Collection<?> collection) {
        return in(pre.test(collection), attribute, collection);
    }

    public PredicateBuilder<T> like(java.util.function.Predicate<String> pre, String attribute, String value) {
        value = escapeLikeValue(value);
        return like(pre.test(value), attribute, "%" + value + "%");
    }

    public PredicateBuilder<T> smartLike(java.util.function.Predicate<String> pre, String attribute, String value) {
        value = escapeLikeValue(value);
        return like(pre.test(value), attribute, value + "%");
    }


    // ======================= >>> apply by boolean.
    public PredicateBuilder<T> equal(boolean condition, String attribute, Object value) {
        return this.predicate(condition, (root, query, cb) -> cb.equal(root.get(attribute), value));
    }

    public PredicateBuilder<T> notEqual(boolean condition, String attribute, Object value) {
        return this.predicate(condition, (root, query, cb) -> cb.notEqual(root.get(attribute), value));
    }

    public PredicateBuilder<T> like(boolean condition, String attribute, String value) {
        return this.predicate(condition, (root, query, cb) -> cb.like(root.get(attribute), value, '\\'));
    }

    public PredicateBuilder<T> in(boolean condition, String attribute, Collection<?> collection) {
        return this.predicate(condition, new InSpecification<T>(attribute, collection));
    }

    public PredicateBuilder<T> betweenInstant(boolean condition, String attribute, Instant start, Instant end) {
        return this.predicate(condition, (root, query, cb) -> cb.between(root.get(attribute), start, end));
    }

    public PredicateBuilder<T> betweenLocalDate(boolean condition, String attribute, LocalDate start, LocalDate end) {
        return this.predicate(condition, (root, query, cb) -> cb.between(root.get(attribute), start, end));
    }

    public PredicateBuilder<T> greaterThan(boolean condition, String attribute, Instant value) {
        return this.predicate(condition, (root, query, cb) -> cb.greaterThan(root.get(attribute), value));
    }

    public PredicateBuilder<T> greaterThanOrEqualTo(boolean condition, String attribute, Instant value) {
        return this.predicate(condition, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(attribute), value));
    }

    public PredicateBuilder<T> greaterThanOrEqualTo(boolean condition, String attribute, LocalDate value) {
        return this.predicate(condition, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(attribute), value));
    }

    public PredicateBuilder<T> lessThanOrEqualTo(boolean condition, String attribute, Instant value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), value));
    }

    public PredicateBuilder<T> lessThanOrEqualTo(boolean condition, String attribute, LocalDate value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), value));
    }


    public PredicateBuilder<T> lessThan(boolean condition, String attribute, Instant value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThan(root.get(attribute), value));
    }

    public PredicateBuilder<T> greaterThanOrEqualTo(boolean condition, String attribute, LocalDateTime value) {
        return this.predicate(condition, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(attribute), value));
    }

    public PredicateBuilder<T> lessThan(boolean condition, String attribute, LocalDateTime value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThan(root.get(attribute), value));
    }

    public PredicateBuilder<T> lessThanOrEqualTo(boolean condition, String attribute, LocalDateTime value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), value));
    }

    public PredicateBuilder<T> lessThanOrEqualTo(boolean condition, String attribute, Long value) {
        return this.predicate(condition, (root, query, cb) -> cb.lessThanOrEqualTo(root.get(attribute), value));
    }

    public Specification<T> build() {
        return buildSpecification();
    }

    public static <T> PredicateBuilder<T> and() {
        return new PredicateBuilder<>(Predicate.BooleanOperator.AND);
    }

    public static <T> PredicateBuilder<T> or() {
        return new PredicateBuilder<>(Predicate.BooleanOperator.OR);
    }

    public PredicateBuilder<T> isStringBlank(boolean condition, String attribute) {
        return this.predicate(condition, (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get(attribute)),
                        cb.equal(cb.trim(root.get(attribute)), "")
                )
        );
    }

    public PredicateBuilder<T> isStringNotBlank(boolean condition, String attribute) {
        return this.predicate(condition, (root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get(attribute)),
                        cb.notEqual(cb.trim(root.get(attribute)), "")
                )
        );
    }
    public PredicateBuilder<T> allFieldsNull(boolean condition, String... attributes) {
        if (!condition || attributes.length == 0) {
            return this;
        }

        return this.predicate(true, (root, query, cb) -> {
            if (attributes.length == 1) {
                return cb.isNull(root.get(attributes[0]));
            }

            // 构建AND链：所有字段都为空
            Predicate result = cb.isNull(root.get(attributes[0]));
            for (int i = 1; i < attributes.length; i++) {
                result = cb.and(result, cb.isNull(root.get(attributes[i])));
            }
            return result;
        });
    }

    public PredicateBuilder<T> atLeastOneNotNull(boolean condition, String... attributes) {
        if (!condition || attributes.length == 0) {
            return this;
        }

        return this.predicate(true, (root, query, cb) -> {
            if (attributes.length == 1) {
                return cb.isNotNull(root.get(attributes[0]));
            }

            // 手动构建OR链
            Predicate result = cb.isNotNull(root.get(attributes[0]));
            for (int i = 1; i < attributes.length; i++) {
                result = cb.or(result, cb.isNotNull(root.get(attributes[i])));
            }
            return result;
        });
    }


    protected PredicateBuilder<T> predicate(boolean condition, Specification<T> specification) {
        if (condition) {
            this.specifications.add(specification);
        }
        return this;
    }

}
