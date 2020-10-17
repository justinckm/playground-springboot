package com.jc

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Entity
@Table(name = "gpls_case")
data class GplsCase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    val id: Long? = null,

    @Column
    var caseId: String? = null,

    @Column
    var validationCode: String? = null,

    @JsonManagedReference
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "gplsCase", cascade = [CascadeType.ALL])
    var childInfo: ChildInfo? = null
)

enum class BirthType(val description: String, val mlDescription: String?, val spleDescription: String?) {
    S("Stillbirth", "Stillbirth", "Stillbirth"),
    N("Normal Birth", "Live Birth", "Biological"),
    A("Adoptive", null, "Adoptive"),
    P("Prebirth", null, "Biological")
}

@Entity
@Table(name = "child_info")
@Inheritance(strategy = InheritanceType.JOINED)
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class ChildInfo(
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    open val id: Long?,

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_ref_id", referencedColumnName = "id", nullable = false)
    open val gplsCase: GplsCase?,

    @Column(columnDefinition = "ENUM")
    @Enumerated(EnumType.STRING)
    open val birthType: BirthType?
)

@Entity
@Table(name = "child_info_live_birth")
open class LiveBirthChildInfo(
    id: Long? = null,
    gplsCase: GplsCase?,
    birthType: BirthType?,

    @Column
    open val nric: String? = null,

    @Column(name = "live_age")
    open val liveAge: Long? = null

) : ChildInfo(
    id,
    gplsCase,
    birthType
)

@Entity
@Table(name = "child_info_adoptive")
open class AdoptiveChildInfo(
    id: Long? = null,
    gplsCase: GplsCase?,
    birthType: BirthType?,

    @Column
    open val nric: String? = null,

    @Column(name = "adopt_age")
    open val adoptAge: Long? = null
) : ChildInfo(
    id,
    gplsCase,
    birthType
)

@Repository
interface CaseRepository : JpaRepository<GplsCase, Long>, JpaSpecificationExecutor<GplsCase> {
    @Query(value =
    """
        SELECT c FROM GplsCase c
        WHERE c.caseId = ?1
    """)
    fun testJpql_getByCaseId(
        caseId: String
    ): MutableList<GplsCase>

    @Query(value =
    """
        SELECT c FROM GplsCase c
        JOIN TREAT (c.childInfo AS LiveBirthChildInfo) p
        WHERE p.nric = ?1
    """)
    fun testJpql_getByLiveChildNric(
        nric: String
    ): MutableList<GplsCase>

    @Query(value =
    """
        SELECT c FROM GplsCase c
        JOIN TREAT (c.childInfo AS AdoptiveChildInfo) p
        WHERE p.nric = ?1
    """)
    fun testJpql_getByAdoptChildNric(
        nric: String
    ): MutableList<GplsCase>

    @Query(value =
    """
        SELECT c FROM GplsCase c
        JOIN TREAT (c.childInfo AS LiveBirthChildInfo) p
        WHERE p.liveAge = ?1
    """)
    fun testJpql_getByLiveChildAge(
        age: Long
    ): MutableList<GplsCase>

    @Query(value =
    """
        SELECT c FROM GplsCase c
        JOIN TREAT (c.childInfo AS AdoptiveChildInfo) p
        WHERE p.adoptAge = ?1
    """)
    fun testJpql_getByAdoptChildAge(
        age: Long
    ): MutableList<GplsCase>
}

@RestController
@RequestMapping("/testgpls")
class CaseController(
    private val caseRepository: CaseRepository
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    // http://localhost:8080/testgpls/create-live
    @GetMapping("create-live")
    fun createLiveCase(): GplsCase? {
        logger.info("controller create default live case")
        val case = GplsCase(1, "caseid-1", "LIVE")
        case.childInfo = LiveBirthChildInfo(1, case, BirthType.N, "T1111111A", 11)
        return caseRepository.save(case)
    }

    // http://localhost:8080/testgpls/create-adopt
    @GetMapping("create-adopt")
    fun createAdoptCase(): GplsCase? {
        logger.info("controller create default adopt case")
        val case = GplsCase(2, "caseid-2", "ADOPT")
        case.childInfo = AdoptiveChildInfo(2, case, BirthType.A, "T2222222B", 22)
        return caseRepository.save(case)
    }

    // http://localhost:8080/testgpls/getbycaseid/caseid-1
    @GetMapping("getbycaseid/{caseid}")
    fun getByCaseId(@PathVariable("caseid") caseid: String): List<GplsCase>? {
        logger.info("controller JPQL get by caseId")
        // ok
        return caseRepository.testJpql_getByCaseId(caseid)
    }

    // http://localhost:8080/testgpls/getbylivenric/T1111111A
    @GetMapping("getbylivenric/{nric}")
    fun getByLiveNric(@PathVariable("nric") nric: String): List<GplsCase>? {
        logger.info("controller JPQL get by live nric")
        // FAIL!! no result
        return caseRepository.testJpql_getByLiveChildNric(nric)
    }

    // http://localhost:8080/testgpls/getbyadoptnric/T2222222B
    @GetMapping("getbyadoptnric/{nric}")
    fun getByAdoptNric(@PathVariable("nric") nric: String): List<GplsCase>? {
        logger.info("controller JPQL get by adopt nric")
        // ok
        return caseRepository.testJpql_getByAdoptChildNric(nric)
    }

    // http://localhost:8080/testgpls/getbyliveage/11
    @GetMapping("getbyliveage/{age}")
    fun getByLiveAge(@PathVariable("age") age: Long): List<GplsCase>? {
        logger.info("controller JPQL get by live age")
        // ok
        return caseRepository.testJpql_getByLiveChildAge(age)
    }

    // http://localhost:8080/testgpls/getbyadoptage/22
    @GetMapping("getbyadoptage/{age}")
    fun getByAdoptAge(@PathVariable("age") age: Long): List<GplsCase>? {
        logger.info("controller JPQL get by adopt age")
        // ok
        return caseRepository.testJpql_getByAdoptChildAge(age)
    }
}
