CREATE TABLE report (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    portfolio_id UUID        NOT NULL,
    report_type  VARCHAR(50) NOT NULL,
    report_format VARCHAR(10) NOT NULL DEFAULT 'PDF',
    report_status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    file_path    VARCHAR(500),
    period_from  TIMESTAMPTZ NOT NULL,
    period_to    TIMESTAMPTZ NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_report_user      FOREIGN KEY (user_id)      REFERENCES users(id)     ON DELETE RESTRICT,
    CONSTRAINT fk_report_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE RESTRICT
);

CREATE INDEX idx_report_user_id      ON report(user_id);
CREATE INDEX idx_report_portfolio_id ON report(portfolio_id);
