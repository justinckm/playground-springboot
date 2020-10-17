package com.jc.querydsl;

import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * I ripped this from
 * https://www.talkischeap.dev/querydsl-projections-spring-data/
 * https://github.com/michalperlak/querydsl-spring-data-projections
 * Credits to: Michal Perlak
 * @param <T> - the main(not projection) type we are interested in getting
 */
public interface ExtendedQuerydslPredicateExecutor<T> extends QuerydslPredicateExecutor<T> {
    /**
     * Find one with a JPQL (Querydsl) query
     * @param query - the query
     * @param <P> - the projection class
     * @return search result if one
     */
    <P> Optional<P> findOne(@NonNull JPQLQuery<P> query);

    /**
     * Find one with a JPQL (Querydsl) query
     * @param factoryExpression - the expression for projection
     * @param <P> - the projection class
     * @return search result if one
     */
    <P> Optional<P> findOne(@NonNull FactoryExpression<P> factoryExpression, @NonNull Predicate predicate);

    /**
     * Find all with a JPQL (Querydsl) query
     * @param query - the query
     * @param <P> - the projection class
     * @return search results if any
     */
    <P> List<P> findAll(@NonNull JPQLQuery<P> query);

    /**
     * Find all with a JPQL (Querydsl) query
     * @param query - the query
     * @param <P> - the projection class
     * @param pageable - pagable search results
     * @return search results if any
     */
    <P> Page<P> findAll(@NonNull JPQLQuery<P> query, @NonNull Pageable pageable);

    /**
     * Find one with a JPQL (Querydsl) query
     * @param factoryExpression - the expression for projection
     * @param predicate - the predicate for the query
     * @param <P> - the projection class
     * @return search result if one
     */
    <P> List<P> findAll(@NonNull FactoryExpression<P> factoryExpression, @NonNull Predicate predicate);

    /**
     * Find one with a JPQL (Querydsl) query
     * @param factoryExpression - the expression for projection
     * @param predicate - the predicate for the query
     * @param pageable - pagable search results
     * @param <P> - the projection class
     * @return search result if one
     */
    <P> Page<P> findAll(@NonNull FactoryExpression<P> factoryExpression, @NonNull Predicate predicate, @NonNull Pageable pageable);
}
