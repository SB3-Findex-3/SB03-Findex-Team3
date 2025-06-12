DROP TABLE IF EXISTS sync_job;
DROP TABLE IF EXISTS index_data;
DROP TABLE IF EXISTS auto_sync;
DROP TABLE IF EXISTS index_info;

CREATE TABLE index_info
(
    id                   SERIAL           NOT NULL PRIMARY KEY,
    index_classification VARCHAR(240)     NOT NULL,
    index_name           VARCHAR(240)     NOT NULL,
    employed_items_count INT              NOT NULL,
    base_point_in_time   DATE             NOT NULL,
    base_index           DECIMAL(10, 2)              NOT NULL,
    source_type          VARCHAR(10)      NOT NULL,
    favorite             BOOLEAN          NOT NULL,
    UNIQUE (index_classification, index_name),
    CONSTRAINT chk_source_type CHECK (source_type IN ('USER', 'OPEN_API'))
);


CREATE TABLE sync_job
(
    id            SERIAL                                NOT NULL PRIMARY KEY,
    index_info_id SERIAL                                NOT NULL,
    job_type      VARCHAR(10)                           NOT NULL,
    target_date   DATE,
    worker        VARCHAR(15)                           NOT NULL,
    job_time      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    result        VARCHAR(10)                            NOT NULL,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE,
    CONSTRAINT result_check CHECK (result IN ('SUCCESS', 'FAILED')),
    CONSTRAINT chk_sync_job_type CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA'))
);

CREATE TABLE auto_sync
(
    id                   SERIAL       NOT NULL PRIMARY KEY,
    index_info_id        SERIAL       NOT NULL,
    index_classification VARCHAR(240) NOT NULL,
    index_name           VARCHAR(240) NOT NULL,
    enabled              BOOLEAN      NOT NULL,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
);

CREATE TABLE index_data
(
    id                  SERIAL       NOT NULL PRIMARY KEY,
    index_info_id       SERIAL       NOT NULL,
    base_date           DATE         NOT NULL,
    source_type         VARCHAR(20)  NOT NULL,
    market_price        NUMERIC,
    closing_price       NUMERIC,
    high_price          NUMERIC,
    low_price           NUMERIC,
    versus              NUMERIC,
    fluctuation_rate    NUMERIC,
    trading_quantity    BIGINT,
    trading_price       BIGINT,
    market_total_amount BIGINT,
    UNIQUE (index_info_id, base_date),
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE,
    CONSTRAINT chk_source_type CHECK (source_type IN ('USER', 'OPEN_API'))
);