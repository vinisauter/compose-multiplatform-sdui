CREATE TABLE public.humming_edit_events (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    revision bigint NOT NULL CHECK (revision > 0),
    command_id varchar(256) NOT NULL,
    actor_id varchar(128) NOT NULL,
    kind varchar(8) NOT NULL CHECK (kind IN ('edit','undo')),
    command_source text NOT NULL CHECK (octet_length(command_source) <= 8000000),
    forward_patch text NOT NULL CHECK (octet_length(forward_patch) <= 32000000),
    inverse_patch text NOT NULL CHECK (octet_length(inverse_patch) <= 32000000),
    PRIMARY KEY (tenant_id,document_id,revision),
    UNIQUE (tenant_id,document_id,command_id),
    FOREIGN KEY (tenant_id,document_id,command_id) REFERENCES public.humming_edit_receipts(tenant_id,document_id,command_id)
);
CREATE TABLE public.humming_undo_links (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    original_command_id varchar(256) NOT NULL,
    undo_command_id varchar(256) NOT NULL,
    PRIMARY KEY (tenant_id,document_id,original_command_id),
    FOREIGN KEY (tenant_id,document_id,original_command_id) REFERENCES public.humming_edit_events(tenant_id,document_id,command_id),
    FOREIGN KEY (tenant_id,document_id,undo_command_id) REFERENCES public.humming_edit_events(tenant_id,document_id,command_id)
);
CREATE TABLE public.humming_presence (
    tenant_id varchar(128) NOT NULL,
    document_id varchar(128) NOT NULL,
    actor_id varchar(128) NOT NULL,
    expires_at timestamptz NOT NULL,
    PRIMARY KEY (tenant_id,document_id,actor_id),
    FOREIGN KEY (tenant_id,document_id) REFERENCES public.humming_drafts(tenant_id,document_id)
);
CREATE FUNCTION public.humming_reject_event_mutation() RETURNS trigger
LANGUAGE plpgsql AS 'BEGIN RAISE EXCEPTION ''Edit journal is append-only''; END;';
CREATE TRIGGER humming_edit_events_immutable BEFORE UPDATE OR DELETE ON public.humming_edit_events
FOR EACH ROW EXECUTE FUNCTION public.humming_reject_event_mutation();
