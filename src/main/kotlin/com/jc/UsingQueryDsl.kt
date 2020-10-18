package com.jc

import com.jc.querydsl.ExtendedQuerydslPredicateExecutor
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.JPQLQuery
import com.querydsl.jpa.impl.JPAQuery
import javax.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

val EMPTY_PREDICATE: Predicate = Expressions.TRUE.isTrue

@Component
class QueryDslBuilder(private val entityManager: EntityManager) {
    fun <T> createQuery(): JPQLQuery<T> = JPAQuery<T>(entityManager)
}

// "Tables"
private val qGplsCase: QGplsCase = QGplsCase.gplsCase
private val qChildInfo: QChildInfo = QChildInfo.childInfo
private val qLiveBirthChildInfo: QLiveBirthChildInfo = QLiveBirthChildInfo.liveBirthChildInfo
private val qAdoptiveChildInfo: QAdoptiveChildInfo = QAdoptiveChildInfo.adoptiveChildInfo

object GplsCaseSearchPredicate {
    fun byCaseOrCode(
        caseId: String? = null,
        validationCode: String? = null
    ): Predicate =
        ExpressionUtils.anyOf(
            byCaseId(caseId),
            byValidationCode((validationCode))
        ) ?: EMPTY_PREDICATE

    fun byCaseOrBirthType(
        caseId: String? = null,
        birthType: BirthType? = null
    ): Predicate =
        ExpressionUtils.anyOf(
            byCaseId(caseId),
            byBirthType(birthType)
        ) ?: EMPTY_PREDICATE

    fun byCaseOrChildNricCantReach(
        caseId: String? = null,
        childNric: String? = null
    ): Predicate =
        ExpressionUtils.anyOf(
            byCaseId(caseId),
            hasChildNricJoin(childNric)
        ) ?: EMPTY_PREDICATE

    fun byCaseOrChildNric(
        caseId: String? = null,
        childNric: String? = null
    ): Predicate =
        ExpressionUtils.anyOf(
            byCaseId(caseId),
            hasChildIdSubquery(childNric)
        ) ?: EMPTY_PREDICATE
    fun byCaseId(caseId: String?): Predicate? = caseId?.let { qGplsCase.caseId.eq(it) }
    fun byValidationCode(validationCode: String?): Predicate? = validationCode?.let { qGplsCase.validationCode.eq(it) }
    fun byBirthType(birthType: BirthType?): Predicate? = birthType?.let { qChildInfo.birthType.eq(birthType) }
    fun hasChildNricJoin(childNric: String?) = childNric?.let {
        ExpressionUtils.anyOf(qLiveBirthChildInfo.nric.eq(it), qAdoptiveChildInfo.nric.eq(it))
    }
    fun hasChildIdSubquery(childId: String?): Predicate? {
        return childId?.let {
            val lb = QLiveBirthChildInfo.liveBirthChildInfo
            val liveBirth = JPAExpressions.select(lb.id).from(lb).where(lb.nric.eq(childId))
            val ad = QAdoptiveChildInfo.adoptiveChildInfo
            val adoption = JPAExpressions.select(ad.id).from(ad).where(ad.nric.eq(childId))
            val childInfoId = qGplsCase.childInfo.id
            ExpressionUtils.anyOf(
                childInfoId.`in`(liveBirth),
                childInfoId.`in`(adoption)
            )
        }
    }
}

@Repository
interface CaseRepositoryWithDsl : JpaRepository<GplsCase, Long>, ExtendedQuerydslPredicateExecutor<GplsCase>

@RestController
@RequestMapping("/usingquerydsl")
class QueryDslController(
    private val caseRepositoryWithDsl: CaseRepositoryWithDsl,
    private val queryDslBuilder: QueryDslBuilder
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("t1")
    fun t1(): List<GplsCase> {
        logger.info("controller t1")
        return caseRepositoryWithDsl.findAll(GplsCaseSearchPredicate.byCaseOrCode("CASE-1", "CODE-2")).toList()
    }

    @GetMapping("t2")
    fun t2(): List<GplsCase> {
        logger.info("controller t2")
        return caseRepositoryWithDsl.findAll(queryDslBuilder.createQuery<GplsCase>()
            .from(qGplsCase)
            .where(
                (qGplsCase.caseId.eq("CASE-2").and(qChildInfo.birthType.eq(BirthType.N)).or(qGplsCase.caseId.eq("CASE-3").and(qChildInfo.birthType.eq(BirthType.A))))
            ))
    }

    // can reach join table
    @GetMapping("t3")
    fun t3(): List<GplsCase> {
        logger.info("controller t3")
        return caseRepositoryWithDsl.findAll(GplsCaseSearchPredicate.byCaseOrBirthType("CASE-1", BirthType.A)).toList()
    }

    // cannot reach inheritance table
    @GetMapping("t4")
    fun t4(): List<GplsCase> {
        logger.info("controller t4")
        return caseRepositoryWithDsl.findAll(GplsCaseSearchPredicate.byCaseOrChildNricCantReach("CASE-1", "T333")).toList()
    }

    // can reach inheritance table
    @GetMapping("t5")
    fun t5(): List<GplsCase> {
        logger.info("controller t5")
        return caseRepositoryWithDsl.findAll(GplsCaseSearchPredicate.byCaseOrChildNric("CASE-1", "T333")).toList()
    }

    // can reach inheritance table if use queryDslBuilder with joins
    @GetMapping("t6")
    fun t6(): List<GplsCase> {
        logger.info("controller t6")
        return caseRepositoryWithDsl.findAll(queryDslBuilder.createQuery<GplsCase>()
            .from(qGplsCase)
            .leftJoin(qLiveBirthChildInfo).on(qGplsCase.childInfo.id.eq(qLiveBirthChildInfo.id))
            .leftJoin(qAdoptiveChildInfo).on(qGplsCase.childInfo.id.eq(qAdoptiveChildInfo.id))
            .where(GplsCaseSearchPredicate.byCaseOrChildNricCantReach("CASE-1", "T333")))
    }
}
