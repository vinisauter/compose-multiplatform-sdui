CREATE TABLE public.humming_drafts (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    revision bigint NOT NULL CHECK (revision >= 0),
    source text NOT NULL CHECK (octet_length(source) <= 8000000),
    PRIMARY KEY (tenant_id, document_id)
);
CREATE TABLE public.humming_edit_receipts (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    command_id varchar(256) NOT NULL,
    fingerprint char(64) NOT NULL,
    applied_revision bigint NOT NULL CHECK (applied_revision > 0),
    diagnostics text NOT NULL,
    PRIMARY KEY (tenant_id, document_id, command_id),
    FOREIGN KEY (tenant_id, document_id) REFERENCES public.humming_drafts (tenant_id, document_id)
);
