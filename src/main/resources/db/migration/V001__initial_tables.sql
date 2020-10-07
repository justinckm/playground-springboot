-- -----------------------------------------------------
-- Tables to test inheritance
-- -----------------------------------------------------
CREATE TABLE gpls_case
(
    id               INT UNSIGNED AUTO_INCREMENT                                                                                                     NOT NULL,
    case_id          VARCHAR(255)                                                                                                                    NULL,
    validation_code  VARCHAR(255)                                                                                                                      NULL,
    PRIMARY KEY (id DESC)
);

CREATE TABLE child_info
(
    id          INT UNSIGNED              NOT NULL AUTO_INCREMENT,
    case_ref_id INT UNSIGNED              NOT NULL,
    birth_type  ENUM ('S', 'N', 'A', 'P') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT child_info_case_id
        FOREIGN KEY (case_ref_id)
            REFERENCES gpls_case (id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);

-- uncomment this if using @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
-- CREATE TABLE child_info_live_birth
-- (
--     id          INT UNSIGNED              NOT NULL AUTO_INCREMENT,
--     case_ref_id INT UNSIGNED              NOT NULL,
--     birth_type  ENUM ('S', 'N', 'A', 'P') NOT NULL,
--     nric        VARCHAR(9)       NULL,
--     live_age    INT             NULL,
--     PRIMARY KEY (id)
-- );
--
-- CREATE TABLE child_info_adoptive
-- (
--     id          INT UNSIGNED              NOT NULL AUTO_INCREMENT,
--     case_ref_id INT UNSIGNED              NOT NULL,
--     birth_type  ENUM ('S', 'N', 'A', 'P') NOT NULL,
--     nric              VARCHAR(9)   NULL,
--     adopt_age         INT         NULL,
--     PRIMARY KEY (id)
-- );

-- uncomment this if using @Inheritance(strategy = InheritanceType.JOINED)
CREATE TABLE child_info_live_birth
(
    id          INT UNSIGNED     NOT NULL,
    nric        VARCHAR(9)       NULL,
    live_age    INT             NULL,
    PRIMARY KEY (id),
    CONSTRAINT lb_child_info_id
        FOREIGN KEY (id)
            REFERENCES child_info (id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);

CREATE TABLE child_info_adoptive
(
    id                INT UNSIGNED NOT NULL,
    nric              VARCHAR(9)   NULL,
    adopt_age         INT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT adp_child_info_id
        FOREIGN KEY (id)
            REFERENCES child_info (id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);
