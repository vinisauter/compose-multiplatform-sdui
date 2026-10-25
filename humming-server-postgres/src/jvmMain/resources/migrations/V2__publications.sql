CREATE TABLE public.humming_publications (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    version char(64) NOT NULL CHECK (version ~ '^[0-9a-f]{64}$'),
    revision bigint NOT NULL CHECK (revision >= 0),
    source text NOT NULL CHECK (octet_length(source) <= 48000000),
    PRIMARY KEY (tenant_id, document_id, version),
    FOREIGN KEY (tenant_id, document_id) REFERENCES public.humming_drafts(tenant_id, document_id)
);

CREATE TABLE public.humming_publication_heads (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    version char(64) NOT NULL,
    PRIMARY KEY (tenant_id, document_id),
    FOREIGN KEY (tenant_id, document_id, version) REFERENCES public.humming_publications(tenant_id, document_id, version)
);

-- Immutable content is also enforced against accidental adapter UPDATE/DELETE.
CREATE FUNCTION public.humming_reject_publication_mutation() RETURNS trigger
LANGUAGE plpgsql AS 'BEGIN RAISE EXCEPTION ''Published snapshots are immutable''; END;';
CREATE TRIGGER humming_publications_immutable BEFORE UPDATE OR DELETE ON public.humming_publications
FOR EACH ROW EXECUTE FUNCTION public.humming_reject_publication_mutation();
