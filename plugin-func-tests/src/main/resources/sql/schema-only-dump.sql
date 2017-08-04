--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.6
-- Dumped by pg_dump version 9.5.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: tools; Type: SCHEMA; Schema: -; Owner: postgres
--

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO jira;
GRANT ALL ON SCHEMA public TO public;

CREATE SCHEMA IF NOT EXISTS tools;


ALTER SCHEMA tools OWNER TO postgres;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA tools;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


SET search_path = public, pg_catalog;

--
-- Name: customfield_stringvalue; Type: TYPE; Schema: public; Owner: jira
--

CREATE TYPE customfield_stringvalue AS (
	customfield bigint,
	stringvalue character varying
);


ALTER TYPE customfield_stringvalue OWNER TO jira;

--
-- Name: delete_baseurl_constraint_f4ed3a(); Type: FUNCTION; Schema: public; Owner: jira
--

CREATE FUNCTION delete_baseurl_constraint_f4ed3a() RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
  BEGIN
    ALTER TABLE connect_addons_f4ed3a DROP CONSTRAINT connect_addons_f4ed3a_base_url_key;
    EXCEPTION WHEN others
    THEN
      NULL;
  END;
  RETURN NULL;
END$$;


ALTER FUNCTION public.delete_baseurl_constraint_f4ed3a() OWNER TO jira;

--
-- Name: migrate_ao_a0b856(); Type: FUNCTION; Schema: public; Owner: jira
--

CREATE FUNCTION migrate_ao_a0b856() RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    BEGIN
        CREATE TABLE "AO_A0B856_WEB_HOOK_LISTENER_AO" (
            "DESCRIPTION" TEXT,
            "ENABLED" BOOLEAN,
            "EVENTS" TEXT,
            "EXCLUDE_BODY" BOOLEAN,
            "FILTERS" TEXT,
            "ID" SERIAL NOT NULL,
            "LAST_UPDATED" TIMESTAMP NOT NULL,
            "LAST_UPDATED_USER" VARCHAR(255),
            "NAME" TEXT NOT NULL,
            "PARAMETERS" TEXT,
            "REGISTRATION_METHOD" VARCHAR(255) NOT NULL,
            "URL" TEXT NOT NULL,
            PRIMARY KEY("ID")
        );
        EXCEPTION WHEN duplicate_table
        THEN
            NULL;
    END;
    RETURN NULL;
END$$;


ALTER FUNCTION public.migrate_ao_a0b856() OWNER TO jira;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: AO_013613_EXPENSE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_EXPENSE" (
    "AMOUNT" double precision DEFAULT 0.0 NOT NULL,
    "CREATED" timestamp without time zone NOT NULL,
    "CREATED_BY" character varying(255) NOT NULL,
    "DATE" timestamp without time zone NOT NULL,
    "DESCRIPTION" character varying(255) NOT NULL,
    "EXPENSE_CATEGORY_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "SCOPE" bigint DEFAULT 0 NOT NULL,
    "SCOPE_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_EXPENSE" OWNER TO jira;

--
-- Name: AO_013613_EXPENSE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_EXPENSE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_EXPENSE_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_EXPENSE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_EXPENSE_ID_seq" OWNED BY "AO_013613_EXPENSE"."ID";


--
-- Name: AO_013613_EXP_CATEGORY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_EXP_CATEGORY" (
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_EXP_CATEGORY" OWNER TO jira;

--
-- Name: AO_013613_EXP_CATEGORY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_EXP_CATEGORY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_EXP_CATEGORY_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_EXP_CATEGORY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_EXP_CATEGORY_ID_seq" OWNED BY "AO_013613_EXP_CATEGORY"."ID";


--
-- Name: AO_013613_HD_SCHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_HD_SCHEME" (
    "DEFAULT_SCHEME" boolean,
    "DESCRIPTION" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_HD_SCHEME" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_DAY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_HD_SCHEME_DAY" (
    "DATE" timestamp without time zone,
    "DESCRIPTION" character varying(255),
    "DURATION_SECONDS" bigint DEFAULT 0,
    "HOLIDAY_SCHEME_ID" integer,
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "TYPE" character varying(255)
);


ALTER TABLE "AO_013613_HD_SCHEME_DAY" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_DAY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_HD_SCHEME_DAY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_HD_SCHEME_DAY_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_DAY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_HD_SCHEME_DAY_ID_seq" OWNED BY "AO_013613_HD_SCHEME_DAY"."ID";


--
-- Name: AO_013613_HD_SCHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_HD_SCHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_HD_SCHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_HD_SCHEME_ID_seq" OWNED BY "AO_013613_HD_SCHEME"."ID";


--
-- Name: AO_013613_HD_SCHEME_MEMBER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_HD_SCHEME_MEMBER" (
    "HOLIDAY_SCHEME_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_HD_SCHEME_MEMBER" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_MEMBER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_HD_SCHEME_MEMBER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_HD_SCHEME_MEMBER_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_HD_SCHEME_MEMBER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_HD_SCHEME_MEMBER_ID_seq" OWNED BY "AO_013613_HD_SCHEME_MEMBER"."ID";


--
-- Name: AO_013613_PERMISSION_GROUP; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_PERMISSION_GROUP" (
    "GROUP_KEY" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "PERMISSION_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_PERMISSION_GROUP" OWNER TO jira;

--
-- Name: AO_013613_PERMISSION_GROUP_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_PERMISSION_GROUP_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_PERMISSION_GROUP_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_PERMISSION_GROUP_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_PERMISSION_GROUP_ID_seq" OWNED BY "AO_013613_PERMISSION_GROUP"."ID";


--
-- Name: AO_013613_PROJECT_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_PROJECT_CONFIG" (
    "ID" integer NOT NULL,
    "PROJECT_COLOR" character varying(255) NOT NULL,
    "PROJECT_ID" bigint DEFAULT 0 NOT NULL
);


ALTER TABLE "AO_013613_PROJECT_CONFIG" OWNER TO jira;

--
-- Name: AO_013613_PROJECT_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_PROJECT_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_PROJECT_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_PROJECT_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_PROJECT_CONFIG_ID_seq" OWNED BY "AO_013613_PROJECT_CONFIG"."ID";


--
-- Name: AO_013613_WA_VALUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_WA_VALUE" (
    "ID" bigint NOT NULL,
    "VALUE" text,
    "WORKLOG_ID" bigint DEFAULT 0 NOT NULL,
    "WORK_ATTRIBUTE_ID" integer NOT NULL
);


ALTER TABLE "AO_013613_WA_VALUE" OWNER TO jira;

--
-- Name: AO_013613_WA_VALUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_WA_VALUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_WA_VALUE_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_WA_VALUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_WA_VALUE_ID_seq" OWNED BY "AO_013613_WA_VALUE"."ID";


--
-- Name: AO_013613_WL_SCHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_WL_SCHEME" (
    "DEFAULT_SCHEME" boolean,
    "DESCRIPTION" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_WL_SCHEME" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_DAY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_WL_SCHEME_DAY" (
    "DAY" integer DEFAULT 0,
    "ID" integer NOT NULL,
    "REQUIRED_SECONDS" bigint DEFAULT 0,
    "WORKLOAD_SCHEME_ID" integer
);


ALTER TABLE "AO_013613_WL_SCHEME_DAY" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_DAY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_WL_SCHEME_DAY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_WL_SCHEME_DAY_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_DAY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_WL_SCHEME_DAY_ID_seq" OWNED BY "AO_013613_WL_SCHEME_DAY"."ID";


--
-- Name: AO_013613_WL_SCHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_WL_SCHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_WL_SCHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_WL_SCHEME_ID_seq" OWNED BY "AO_013613_WL_SCHEME"."ID";


--
-- Name: AO_013613_WL_SCHEME_MEMBER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_WL_SCHEME_MEMBER" (
    "ID" integer NOT NULL,
    "MEMBER_KEY" character varying(255) NOT NULL,
    "WORKLOAD_SCHEME_ID" integer NOT NULL
);


ALTER TABLE "AO_013613_WL_SCHEME_MEMBER" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_MEMBER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_WL_SCHEME_MEMBER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_WL_SCHEME_MEMBER_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_WL_SCHEME_MEMBER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_WL_SCHEME_MEMBER_ID_seq" OWNED BY "AO_013613_WL_SCHEME_MEMBER"."ID";


--
-- Name: AO_013613_WORK_ATTRIBUTE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_013613_WORK_ATTRIBUTE" (
    "EXTERNAL_URL" text,
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "REQUIRED" boolean DEFAULT false,
    "SEQUENCE" integer DEFAULT 0,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_013613_WORK_ATTRIBUTE" OWNER TO jira;

--
-- Name: AO_013613_WORK_ATTRIBUTE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_013613_WORK_ATTRIBUTE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_013613_WORK_ATTRIBUTE_ID_seq" OWNER TO jira;

--
-- Name: AO_013613_WORK_ATTRIBUTE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_013613_WORK_ATTRIBUTE_ID_seq" OWNED BY "AO_013613_WORK_ATTRIBUTE"."ID";


--
-- Name: AO_0201F0_KB_HELPFUL_AGGR; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_0201F0_KB_HELPFUL_AGGR" (
    "COUNT" bigint,
    "ID" bigint NOT NULL,
    "SERVICE_DESK_ID" bigint,
    "START_TIME" bigint
);


ALTER TABLE "AO_0201F0_KB_HELPFUL_AGGR" OWNER TO jira;

--
-- Name: AO_0201F0_KB_HELPFUL_AGGR_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_0201F0_KB_HELPFUL_AGGR_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_0201F0_KB_HELPFUL_AGGR_ID_seq" OWNER TO jira;

--
-- Name: AO_0201F0_KB_HELPFUL_AGGR_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_0201F0_KB_HELPFUL_AGGR_ID_seq" OWNED BY "AO_0201F0_KB_HELPFUL_AGGR"."ID";


--
-- Name: AO_0201F0_KB_VIEW_AGGR; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_0201F0_KB_VIEW_AGGR" (
    "COUNT" bigint,
    "ID" bigint NOT NULL,
    "SERVICE_DESK_ID" bigint,
    "START_TIME" bigint
);


ALTER TABLE "AO_0201F0_KB_VIEW_AGGR" OWNER TO jira;

--
-- Name: AO_0201F0_KB_VIEW_AGGR_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_0201F0_KB_VIEW_AGGR_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_0201F0_KB_VIEW_AGGR_ID_seq" OWNER TO jira;

--
-- Name: AO_0201F0_KB_VIEW_AGGR_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_0201F0_KB_VIEW_AGGR_ID_seq" OWNED BY "AO_0201F0_KB_VIEW_AGGR"."ID";


--
-- Name: AO_0201F0_STATS_EVENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_0201F0_STATS_EVENT" (
    "EVENT_KEY" character varying(255),
    "EVENT_TIME" bigint,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_0201F0_STATS_EVENT" OWNER TO jira;

--
-- Name: AO_0201F0_STATS_EVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_0201F0_STATS_EVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_0201F0_STATS_EVENT_ID_seq" OWNER TO jira;

--
-- Name: AO_0201F0_STATS_EVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_0201F0_STATS_EVENT_ID_seq" OWNED BY "AO_0201F0_STATS_EVENT"."ID";


--
-- Name: AO_0201F0_STATS_EVENT_PARAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_0201F0_STATS_EVENT_PARAM" (
    "ID" bigint NOT NULL,
    "PARAM_NAME" character varying(127),
    "PARAM_VALUE" character varying(450),
    "STATS_EVENT_ID" bigint
);


ALTER TABLE "AO_0201F0_STATS_EVENT_PARAM" OWNER TO jira;

--
-- Name: AO_0201F0_STATS_EVENT_PARAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_0201F0_STATS_EVENT_PARAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_0201F0_STATS_EVENT_PARAM_ID_seq" OWNER TO jira;

--
-- Name: AO_0201F0_STATS_EVENT_PARAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_0201F0_STATS_EVENT_PARAM_ID_seq" OWNED BY "AO_0201F0_STATS_EVENT_PARAM"."ID";


--
-- Name: AO_098293_ANNOUNCEMENT_ENTITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_098293_ANNOUNCEMENT_ENTITY" (
    "CREATOR" character varying(255),
    "END_TIME" timestamp with time zone,
    "EVENT_TIME" timestamp with time zone,
    "ID" integer NOT NULL,
    "MESSAGE" character varying(255),
    "REFERENCE" character varying(255),
    "SOURCE" character varying(255),
    "START_TIME" timestamp with time zone,
    "TARGET_GROUPS" character varying(255),
    "TARGET_PATH" character varying(255),
    "URL" character varying(255)
);


ALTER TABLE "AO_098293_ANNOUNCEMENT_ENTITY" OWNER TO jira;

--
-- Name: AO_098293_ANNOUNCEMENT_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_098293_ANNOUNCEMENT_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_098293_ANNOUNCEMENT_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_098293_ANNOUNCEMENT_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_098293_ANNOUNCEMENT_ENTITY_ID_seq" OWNED BY "AO_098293_ANNOUNCEMENT_ENTITY"."ID";


--
-- Name: AO_21D670_WHITELIST_RULES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_21D670_WHITELIST_RULES" (
    "ALLOWINBOUND" boolean,
    "EXPRESSION" text NOT NULL,
    "ID" integer NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_21D670_WHITELIST_RULES" OWNER TO jira;

--
-- Name: AO_21D670_WHITELIST_RULES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_21D670_WHITELIST_RULES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_21D670_WHITELIST_RULES_ID_seq" OWNER TO jira;

--
-- Name: AO_21D670_WHITELIST_RULES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_21D670_WHITELIST_RULES_ID_seq" OWNED BY "AO_21D670_WHITELIST_RULES"."ID";


--
-- Name: AO_2C4E5C_MAILCHANNEL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILCHANNEL" (
    "CREATED_BY" character varying(255),
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "ENABLED" boolean,
    "ID" integer NOT NULL,
    "MAIL_CHANNEL_KEY" character varying(255),
    "MAIL_CONNECTION_ID" integer,
    "MAX_RETRY_ON_FAILURE" integer DEFAULT 0,
    "MODIFIED_BY" character varying(255),
    "PROJECT_ID" bigint DEFAULT 0,
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILCHANNEL" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILCHANNEL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILCHANNEL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILCHANNEL_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILCHANNEL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILCHANNEL_ID_seq" OWNED BY "AO_2C4E5C_MAILCHANNEL"."ID";


--
-- Name: AO_2C4E5C_MAILCONNECTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILCONNECTION" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "EMAIL_ADDRESS" character varying(255) NOT NULL,
    "FOLDER" character varying(255) NOT NULL,
    "HOST_NAME" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "PASSWORD" character varying(255) NOT NULL,
    "PORT" integer DEFAULT 0 NOT NULL,
    "PROTOCOL" character varying(255) NOT NULL,
    "PULL_FROM_DATE" bigint DEFAULT 0,
    "TIMEOUT" bigint DEFAULT 0,
    "TLS" boolean DEFAULT false,
    "UPDATED_TIMESTAMP" bigint DEFAULT 0,
    "USER_NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_2C4E5C_MAILCONNECTION" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILCONNECTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILCONNECTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILCONNECTION_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILCONNECTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILCONNECTION_ID_seq" OWNED BY "AO_2C4E5C_MAILCONNECTION"."ID";


--
-- Name: AO_2C4E5C_MAILGLOBALHANDLER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILGLOBALHANDLER" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "HANDLER_TYPE" character varying(255),
    "ID" integer NOT NULL,
    "MODULE_COMPLETE_KEY" character varying(255),
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILGLOBALHANDLER" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILGLOBALHANDLER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILGLOBALHANDLER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILGLOBALHANDLER_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILGLOBALHANDLER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILGLOBALHANDLER_ID_seq" OWNED BY "AO_2C4E5C_MAILGLOBALHANDLER"."ID";


--
-- Name: AO_2C4E5C_MAILHANDLER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILHANDLER" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "HANDLER_TYPE" character varying(255),
    "ID" integer NOT NULL,
    "MAIL_CHANNEL_ID" integer,
    "MODULE_COMPLETE_KEY" character varying(255),
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILHANDLER" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILHANDLER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILHANDLER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILHANDLER_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILHANDLER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILHANDLER_ID_seq" OWNED BY "AO_2C4E5C_MAILHANDLER"."ID";


--
-- Name: AO_2C4E5C_MAILITEM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILITEM" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "ID" integer NOT NULL,
    "MAIL_CONNECTION_ID" integer DEFAULT 0 NOT NULL,
    "STATUS" character varying(255),
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILITEM" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEMAUDIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILITEMAUDIT" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "FROM_ADDRESS" character varying(255),
    "HANDLER_NAME_KEY" character varying(255),
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "MAIL_CHANNEL_ID" integer DEFAULT 0,
    "MAIL_CHANNEL_NAME" character varying(255),
    "MAIL_ITEM_ID" integer,
    "MESSAGE" character varying(255),
    "NO_OF_RETRY" integer DEFAULT 0,
    "RESULT_STATUS" character varying(255),
    "SUBJECT" character varying(255),
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILITEMAUDIT" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEMAUDIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILITEMAUDIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILITEMAUDIT_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEMAUDIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILITEMAUDIT_ID_seq" OWNED BY "AO_2C4E5C_MAILITEMAUDIT"."ID";


--
-- Name: AO_2C4E5C_MAILITEMCHUNK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILITEMCHUNK" (
    "ID" integer NOT NULL,
    "MAIL_ITEM_ID" integer,
    "MIME_MSG_CHUNK" text,
    "MIME_MSG_CHUNK_IDX" integer DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILITEMCHUNK" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEMCHUNK_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILITEMCHUNK_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILITEMCHUNK_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEMCHUNK_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILITEMCHUNK_ID_seq" OWNED BY "AO_2C4E5C_MAILITEMCHUNK"."ID";


--
-- Name: AO_2C4E5C_MAILITEM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILITEM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILITEM_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILITEM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILITEM_ID_seq" OWNED BY "AO_2C4E5C_MAILITEM"."ID";


--
-- Name: AO_2C4E5C_MAILRUNAUDIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2C4E5C_MAILRUNAUDIT" (
    "CREATED_TIMESTAMP" bigint DEFAULT 0,
    "FAILURE_MESSAGE" character varying(255),
    "ID" integer NOT NULL,
    "MAIL_CHANNEL_NAME" character varying(255),
    "MAIL_CONNECTION_ID" integer DEFAULT 0 NOT NULL,
    "NO_OF_RETRY" integer DEFAULT 0,
    "RUN_OUTCOME" character varying(255),
    "UPDATED_TIMESTAMP" bigint DEFAULT 0
);


ALTER TABLE "AO_2C4E5C_MAILRUNAUDIT" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILRUNAUDIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2C4E5C_MAILRUNAUDIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2C4E5C_MAILRUNAUDIT_ID_seq" OWNER TO jira;

--
-- Name: AO_2C4E5C_MAILRUNAUDIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2C4E5C_MAILRUNAUDIT_ID_seq" OWNED BY "AO_2C4E5C_MAILRUNAUDIT"."ID";


--
-- Name: AO_2D3BEA_ALLOCATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_ALLOCATION" (
    "DELETED_DATE" timestamp without time zone,
    "END_DATE" timestamp without time zone NOT NULL,
    "ID" bigint NOT NULL,
    "PERCENTAGE" double precision NOT NULL,
    "POSITION_ID" bigint NOT NULL,
    "START_DATE" timestamp without time zone NOT NULL
);


ALTER TABLE "AO_2D3BEA_ALLOCATION" OWNER TO jira;

--
-- Name: AO_2D3BEA_ALLOCATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_ALLOCATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_ALLOCATION_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_ALLOCATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_ALLOCATION_ID_seq" OWNED BY "AO_2D3BEA_ALLOCATION"."ID";


--
-- Name: AO_2D3BEA_ATTACHMENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_ATTACHMENT" (
    "AUTHOR" character varying(255) NOT NULL,
    "COMMENT" character varying(255),
    "CREATED" timestamp without time zone NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "EXPENSE_ID" bigint,
    "FILENAME" character varying(255) NOT NULL,
    "FILESIZE" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "MIME_TYPE" character varying(255) NOT NULL,
    "POSITION_ID" bigint,
    "ACCOUNT_KEY" character varying(255),
    "CURRENCY_CODE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_ATTACHMENT" OWNER TO jira;

--
-- Name: AO_2D3BEA_ATTACHMENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_ATTACHMENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_ATTACHMENT_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_ATTACHMENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_ATTACHMENT_ID_seq" OWNED BY "AO_2D3BEA_ATTACHMENT"."ID";


--
-- Name: AO_2D3BEA_BASELINE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_BASELINE" (
    "APPROVED_BY" character varying(255),
    "CREATED" timestamp without time zone NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "DESCRIPTION" text,
    "EXPENDITURE_TYPE" character varying(255) NOT NULL,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_BASELINE" OWNER TO jira;

--
-- Name: AO_2D3BEA_BASELINE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_BASELINE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_BASELINE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_BASELINE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_BASELINE_ID_seq" OWNED BY "AO_2D3BEA_BASELINE"."ID";


--
-- Name: AO_2D3BEA_COMMENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_COMMENT" (
    "AUTHOR" character varying(255) NOT NULL,
    "COMMENT" text,
    "COMPONENT_ID" bigint NOT NULL,
    "COMPONENT_TYPE" character varying(255) NOT NULL,
    "CREATED" timestamp without time zone NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "LEVEL" character varying(255) NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_COMMENT" OWNER TO jira;

--
-- Name: AO_2D3BEA_COMMENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_COMMENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_COMMENT_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_COMMENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_COMMENT_ID_seq" OWNED BY "AO_2D3BEA_COMMENT"."ID";


--
-- Name: AO_2D3BEA_CUSTOMFIELD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_CUSTOMFIELD" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "OPTIONS" text,
    "REQUIRED" boolean,
    "STYLE" character varying(255),
    "TYPE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_CUSTOMFIELD" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELDPVALUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_CUSTOMFIELDPVALUE" (
    "CUSTOM_FIELD_ID" bigint NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "POSITION_ID" bigint NOT NULL,
    "VALUE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_CUSTOMFIELDPVALUE" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq" OWNED BY "AO_2D3BEA_CUSTOMFIELDPVALUE"."ID";


--
-- Name: AO_2D3BEA_CUSTOMFIELDVALUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_CUSTOMFIELDVALUE" (
    "CUSTOM_FIELD_ID" bigint NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "EXPENSE_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "VALUE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_CUSTOMFIELDVALUE" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq" OWNED BY "AO_2D3BEA_CUSTOMFIELDVALUE"."ID";


--
-- Name: AO_2D3BEA_CUSTOMFIELD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_CUSTOMFIELD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_CUSTOMFIELD_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_CUSTOMFIELD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_CUSTOMFIELD_ID_seq" OWNED BY "AO_2D3BEA_CUSTOMFIELD"."ID";


--
-- Name: AO_2D3BEA_ENTITY_CHANGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_ENTITY_CHANGE" (
    "AUTHOR" character varying(255),
    "CHANGED_FIELD" character varying(255),
    "CHANGE_TIMESTAMP" timestamp without time zone,
    "DELETED_DATE" timestamp without time zone,
    "ENTITY" character varying(255),
    "ENTITY_ID" bigint,
    "ID" bigint NOT NULL,
    "NEW_VALUE" text,
    "OLD_VALUE" text,
    "OPERATION" character varying(255)
);


ALTER TABLE "AO_2D3BEA_ENTITY_CHANGE" OWNER TO jira;

--
-- Name: AO_2D3BEA_ENTITY_CHANGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_ENTITY_CHANGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_ENTITY_CHANGE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_ENTITY_CHANGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_ENTITY_CHANGE_ID_seq" OWNED BY "AO_2D3BEA_ENTITY_CHANGE"."ID";


--
-- Name: AO_2D3BEA_EXCHANGERATE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_EXCHANGERATE" (
    "DELETED_DATE" timestamp without time zone,
    "EFFECTIVE_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "RATE" double precision NOT NULL,
    "SOURCE_CURRENCY_CODE" character varying(255) NOT NULL,
    "TARGET_CURRENCY_CODE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_EXCHANGERATE" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXCHANGERATE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_EXCHANGERATE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_EXCHANGERATE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXCHANGERATE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_EXCHANGERATE_ID_seq" OWNED BY "AO_2D3BEA_EXCHANGERATE"."ID";


--
-- Name: AO_2D3BEA_EXPENSE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_EXPENSE" (
    "ACTUAL" boolean NOT NULL,
    "AMORTIZED" boolean,
    "AMOUNT" double precision NOT NULL,
    "BASELINE_ID" bigint,
    "CATEGORY" character varying(255) NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "DESCRIPTION" text,
    "END_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "FREQUENCY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "INTERVAL" integer DEFAULT 0,
    "LABELS" character varying(255),
    "MONTHLY_BY" character varying(255),
    "NAME" character varying(255) NOT NULL,
    "NUMBER_OF_OCCURRENCES" integer DEFAULT 0,
    "ON_WEEK_DAYS" character varying(255),
    "REPORTER" character varying(255) NOT NULL,
    "REVENUE" boolean,
    "START_DATE" timestamp without time zone,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_EXPENSE" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXPENSE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_EXPENSE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_EXPENSE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXPENSE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_EXPENSE_ID_seq" OWNED BY "AO_2D3BEA_EXPENSE"."ID";


--
-- Name: AO_2D3BEA_EXTERNALTEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_EXTERNALTEAM" (
    "COMMITMENT" double precision NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "EXTERNAL_TEAM_ID" integer NOT NULL,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "REPORTER" character varying(255)
);


ALTER TABLE "AO_2D3BEA_EXTERNALTEAM" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXTERNALTEAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_EXTERNALTEAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_EXTERNALTEAM_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_EXTERNALTEAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_EXTERNALTEAM_ID_seq" OWNED BY "AO_2D3BEA_EXTERNALTEAM"."ID";


--
-- Name: AO_2D3BEA_FILTER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FILTER" (
    "ADVANCED" boolean NOT NULL,
    "CREATOR" character varying(255) NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "IS_ADVANCED" boolean,
    "MIN_AVAILABILITY" double precision NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "VALUE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_FILTER" OWNER TO jira;

--
-- Name: AO_2D3BEA_FILTER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FILTER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FILTER_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FILTER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FILTER_ID_seq" OWNED BY "AO_2D3BEA_FILTER"."ID";


--
-- Name: AO_2D3BEA_FOLIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIO" (
    "ALL_PROGRESSIVELY_EARNED" boolean,
    "APPLYING_USER_RATES" boolean,
    "AS_CLIENT_PERSPECTIVE" boolean,
    "BASED_ON_REVENUE" boolean,
    "CAPITALIZED_ISSUES_LABELS" character varying(255),
    "CLOSED_DATE" timestamp without time zone,
    "COMPLETION_BASED_ON_DAYS" boolean,
    "CONTINGENCY" double precision,
    "CURRENCY_CODE" character varying(255) NOT NULL,
    "DEFAULT_CONFIG" boolean,
    "DELETED_DATE" timestamp without time zone,
    "DESCRIPTION" text,
    "EARNED_FIELD_ID" character varying(255),
    "END_DATE" timestamp without time zone NOT NULL,
    "HOLIDAY_SCHEME_ID" integer,
    "HOURS_PER_DAY" double precision,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "OWNER_USER_NAME" character varying(255) NOT NULL,
    "PROGRESSIVELY_EARNED_LABELS" character varying(255),
    "RAPID_VIEW_ID" bigint,
    "RESERVE" double precision NOT NULL,
    "SAVED_FILTER_ID" bigint NOT NULL,
    "START_DATE" timestamp without time zone NOT NULL,
    "SYNCHED_WITH_ACCOUNTS" boolean,
    "SYNCHED_WITH_JIRA" boolean,
    "SYNCHED_WITH_TEMPO" boolean,
    "TARGETED_UNITS" double precision,
    "TASK_PROGRESS_METHOD" character varying(255),
    "TOLERANCE" double precision,
    "WITH_EARNED" boolean,
    "WITH_REVENUE" boolean,
    "WORKLOAD_SCHEME_ID" integer
);


ALTER TABLE "AO_2D3BEA_FOLIO" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOCF; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIOCF" (
    "DELETED_DATE" timestamp without time zone,
    "DESCENDANT" boolean,
    "DISPLAY" boolean,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "OPTIONS" text,
    "RANKING" boolean,
    "REQUIRED" boolean,
    "STYLE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_FOLIOCF" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOCFVALUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIOCFVALUE" (
    "CUSTOM_FIELD_ID" bigint NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "VALUE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_FOLIOCFVALUE" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOCFVALUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIOCFVALUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIOCFVALUE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOCFVALUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIOCFVALUE_ID_seq" OWNED BY "AO_2D3BEA_FOLIOCFVALUE"."ID";


--
-- Name: AO_2D3BEA_FOLIOCF_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIOCF_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIOCF_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOCF_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIOCF_ID_seq" OWNED BY "AO_2D3BEA_FOLIOCF"."ID";


--
-- Name: AO_2D3BEA_FOLIOTOPORTFOLIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIOTOPORTFOLIO" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint,
    "ID" bigint NOT NULL,
    "PORTFOLIO_ID" bigint
);


ALTER TABLE "AO_2D3BEA_FOLIOTOPORTFOLIO" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq" OWNED BY "AO_2D3BEA_FOLIOTOPORTFOLIO"."ID";


--
-- Name: AO_2D3BEA_FOLIO_ADMIN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIO_ADMIN" (
    "ADMIN" character varying(255) NOT NULL,
    "CAN_ACCESS_RATES" boolean,
    "CAN_READ" boolean,
    "CAN_WRITE" boolean,
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_2D3BEA_FOLIO_ADMIN" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_ADMIN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIO_ADMIN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIO_ADMIN_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_ADMIN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIO_ADMIN_ID_seq" OWNED BY "AO_2D3BEA_FOLIO_ADMIN"."ID";


--
-- Name: AO_2D3BEA_FOLIO_FORMAT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIO_FORMAT" (
    "DATE_FORMAT" character varying(255),
    "DECIMAL_SEPARATOR" character varying(255),
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "GROUPING_SEPARATOR" character varying(255),
    "ID" bigint NOT NULL,
    "TYPE" character varying(255)
);


ALTER TABLE "AO_2D3BEA_FOLIO_FORMAT" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_FORMAT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIO_FORMAT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIO_FORMAT_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_FORMAT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIO_FORMAT_ID_seq" OWNED BY "AO_2D3BEA_FOLIO_FORMAT"."ID";


--
-- Name: AO_2D3BEA_FOLIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIO_ID_seq" OWNED BY "AO_2D3BEA_FOLIO"."ID";


--
-- Name: AO_2D3BEA_FOLIO_USER_AO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_FOLIO_USER_AO" (
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "LAST_VERSION_SEEN" character varying(255),
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_FOLIO_USER_AO" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_USER_AO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_FOLIO_USER_AO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_FOLIO_USER_AO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_FOLIO_USER_AO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_FOLIO_USER_AO_ID_seq" OWNED BY "AO_2D3BEA_FOLIO_USER_AO"."ID";


--
-- Name: AO_2D3BEA_NWDS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_NWDS" (
    "DATE" timestamp without time zone NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_2D3BEA_NWDS" OWNER TO jira;

--
-- Name: AO_2D3BEA_NWDS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_NWDS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_NWDS_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_NWDS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_NWDS_ID_seq" OWNED BY "AO_2D3BEA_NWDS"."ID";


--
-- Name: AO_2D3BEA_OTRULETOFOLIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_OTRULETOFOLIO" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint,
    "ID" bigint NOT NULL,
    "OTRULE_ID" bigint
);


ALTER TABLE "AO_2D3BEA_OTRULETOFOLIO" OWNER TO jira;

--
-- Name: AO_2D3BEA_OTRULETOFOLIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_OTRULETOFOLIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_OTRULETOFOLIO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_OTRULETOFOLIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_OTRULETOFOLIO_ID_seq" OWNED BY "AO_2D3BEA_OTRULETOFOLIO"."ID";


--
-- Name: AO_2D3BEA_OVERTIME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_OVERTIME" (
    "COEFFICIENT" double precision NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "FREQUENCY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "REGULAR_HOURS" double precision NOT NULL
);


ALTER TABLE "AO_2D3BEA_OVERTIME" OWNER TO jira;

--
-- Name: AO_2D3BEA_OVERTIME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_OVERTIME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_OVERTIME_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_OVERTIME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_OVERTIME_ID_seq" OWNED BY "AO_2D3BEA_OVERTIME"."ID";


--
-- Name: AO_2D3BEA_PERMISSION_GROUP; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_PERMISSION_GROUP" (
    "CAN_ACCESS_RATES" boolean,
    "CAN_WRITE" boolean,
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "GRANTED_GROUP" character varying(255) NOT NULL,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_2D3BEA_PERMISSION_GROUP" OWNER TO jira;

--
-- Name: AO_2D3BEA_PERMISSION_GROUP_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_PERMISSION_GROUP_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_PERMISSION_GROUP_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_PERMISSION_GROUP_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_PERMISSION_GROUP_ID_seq" OWNED BY "AO_2D3BEA_PERMISSION_GROUP"."ID";


--
-- Name: AO_2D3BEA_PLAN_ALLOCATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_PLAN_ALLOCATION" (
    "ASSIGNEE_KEY" character varying(255) NOT NULL,
    "ASSIGNEE_TYPE" character varying(255) NOT NULL,
    "COMMITMENT" double precision NOT NULL,
    "CREATED" timestamp without time zone NOT NULL,
    "CREATED_BY" character varying(255) NOT NULL,
    "DESCRIPTION" text,
    "END_TIME" timestamp without time zone NOT NULL,
    "ID" integer NOT NULL,
    "PLAN_ITEM_ID" bigint DEFAULT 0 NOT NULL,
    "PLAN_ITEM_TYPE" character varying(255) NOT NULL,
    "RECURRENCE_END_DATE" timestamp without time zone,
    "RULE" character varying(255) NOT NULL,
    "SCOPE_ID" bigint DEFAULT 0 NOT NULL,
    "SCOPE_TYPE" character varying(255) NOT NULL,
    "START_TIME" timestamp without time zone NOT NULL,
    "UPDATED" timestamp without time zone NOT NULL,
    "UPDATED_BY" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_PLAN_ALLOCATION" OWNER TO jira;

--
-- Name: AO_2D3BEA_PLAN_ALLOCATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_PLAN_ALLOCATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_PLAN_ALLOCATION_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_PLAN_ALLOCATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_PLAN_ALLOCATION_ID_seq" OWNED BY "AO_2D3BEA_PLAN_ALLOCATION"."ID";


--
-- Name: AO_2D3BEA_PORTFOLIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_PORTFOLIO" (
    "ASC_ORDER" boolean,
    "COLOR" character varying(255) NOT NULL,
    "CURRENCY_CODE" character varying(255),
    "DELETED_DATE" timestamp without time zone,
    "DESCRIPTION" text,
    "FOLIO_FIELD_ID" bigint,
    "FOLIO_FIELD_VALUE" character varying(255),
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "RANKING_FIELD_ID" bigint,
    "REPORTER" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_PORTFOLIO" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIOTOPORTFOLIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_PORTFOLIOTOPORTFOLIO" (
    "CHILD_ID" bigint,
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "PARENT_ID" bigint
);


ALTER TABLE "AO_2D3BEA_PORTFOLIOTOPORTFOLIO" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq" OWNED BY "AO_2D3BEA_PORTFOLIOTOPORTFOLIO"."ID";


--
-- Name: AO_2D3BEA_PORTFOLIO_ADMIN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_PORTFOLIO_ADMIN" (
    "ADMIN" character varying(255) NOT NULL,
    "CAN_WRITE" boolean,
    "DELETED_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "PORTFOLIO_ID" bigint NOT NULL
);


ALTER TABLE "AO_2D3BEA_PORTFOLIO_ADMIN" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq" OWNED BY "AO_2D3BEA_PORTFOLIO_ADMIN"."ID";


--
-- Name: AO_2D3BEA_PORTFOLIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_PORTFOLIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_PORTFOLIO_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_PORTFOLIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_PORTFOLIO_ID_seq" OWNED BY "AO_2D3BEA_PORTFOLIO"."ID";


--
-- Name: AO_2D3BEA_POSITION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_POSITION" (
    "ACTUAL" boolean NOT NULL,
    "APPLY_USER_RATES" boolean,
    "BASELINE_ID" bigint,
    "CATEGORY" character varying(255) NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "DESCRIPTION" text,
    "EFFORT" bigint,
    "EXTERNAL_TEAM_ID" integer,
    "EXTERNAL_TEAM_MEMBER_ID" integer,
    "FOLIO_ID" bigint NOT NULL,
    "FREQUENCY" character varying(255) NOT NULL,
    "HOLIDAY_SCHEME_ID" integer,
    "ID" bigint NOT NULL,
    "LABELS" character varying(255),
    "MEMBER" character varying(255),
    "NAME" character varying(255) NOT NULL,
    "OTRULE_ID" bigint,
    "REPORTER" character varying(255) NOT NULL,
    "REVENUE" boolean,
    "SYNCHED_WITH_JIRA" boolean,
    "TEAM_ROLE_ID" integer,
    "TYPE" character varying(255) NOT NULL,
    "WORKLOAD_SCHEME_ID" integer,
    "ACCOUNT_KEY" character varying(255)
);


ALTER TABLE "AO_2D3BEA_POSITION" OWNER TO jira;

--
-- Name: AO_2D3BEA_POSITION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_POSITION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_POSITION_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_POSITION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_POSITION_ID_seq" OWNED BY "AO_2D3BEA_POSITION"."ID";


--
-- Name: AO_2D3BEA_RATE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_RATE" (
    "AMOUNT" double precision NOT NULL,
    "CURRENCY_CODE" character varying(3) NOT NULL,
    "DELETED_DATE" timestamp without time zone,
    "EFFECTIVE_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "LINK_KEY" character varying(255) NOT NULL,
    "LINK_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_RATE" OWNER TO jira;

--
-- Name: AO_2D3BEA_RATE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_RATE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_RATE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_RATE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_RATE_ID_seq" OWNED BY "AO_2D3BEA_RATE"."ID";


--
-- Name: AO_2D3BEA_STATUS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_STATUS" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "STATUS_ID" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_STATUS" OWNER TO jira;

--
-- Name: AO_2D3BEA_STATUS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_STATUS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_STATUS_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_STATUS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_STATUS_ID_seq" OWNED BY "AO_2D3BEA_STATUS"."ID";


--
-- Name: AO_2D3BEA_TIMELINE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_TIMELINE" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "ISSUE_START_FIELD_ID" character varying(255)
);


ALTER TABLE "AO_2D3BEA_TIMELINE" OWNER TO jira;

--
-- Name: AO_2D3BEA_TIMELINE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_TIMELINE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_TIMELINE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_TIMELINE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_TIMELINE_ID_seq" OWNED BY "AO_2D3BEA_TIMELINE"."ID";


--
-- Name: AO_2D3BEA_WAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_WAGE" (
    "DELETED_DATE" timestamp without time zone,
    "END_DATE" timestamp without time zone,
    "ID" bigint NOT NULL,
    "POSITION_ID" bigint NOT NULL,
    "START_DATE" timestamp without time zone NOT NULL,
    "WAGE" double precision NOT NULL,
    "WEIGHT" double precision
);


ALTER TABLE "AO_2D3BEA_WAGE" OWNER TO jira;

--
-- Name: AO_2D3BEA_WAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_WAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_WAGE_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_WAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_WAGE_ID_seq" OWNED BY "AO_2D3BEA_WAGE"."ID";


--
-- Name: AO_2D3BEA_WEEKDAY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_WEEKDAY" (
    "DELETED_DATE" timestamp without time zone,
    "FOLIO_ID" bigint NOT NULL,
    "HOURS" double precision,
    "ID" bigint NOT NULL,
    "WEEK_DAY" integer NOT NULL
);


ALTER TABLE "AO_2D3BEA_WEEKDAY" OWNER TO jira;

--
-- Name: AO_2D3BEA_WEEKDAY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_WEEKDAY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_WEEKDAY_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_WEEKDAY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_WEEKDAY_ID_seq" OWNED BY "AO_2D3BEA_WEEKDAY"."ID";


--
-- Name: AO_2D3BEA_WORKED_HOURS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_WORKED_HOURS" (
    "DELETED_DATE" timestamp without time zone,
    "EFFORT" double precision NOT NULL,
    "END_DATE" timestamp without time zone NOT NULL,
    "ID" bigint NOT NULL,
    "OT_EFFORT" double precision,
    "POSITION_ID" bigint NOT NULL,
    "REPORTER" character varying(255) NOT NULL,
    "START_DATE" timestamp without time zone NOT NULL
);


ALTER TABLE "AO_2D3BEA_WORKED_HOURS" OWNER TO jira;

--
-- Name: AO_2D3BEA_WORKED_HOURS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_WORKED_HOURS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_WORKED_HOURS_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_WORKED_HOURS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_WORKED_HOURS_ID_seq" OWNED BY "AO_2D3BEA_WORKED_HOURS"."ID";


--
-- Name: AO_2D3BEA_WORKFLOW; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_2D3BEA_WORKFLOW" (
    "ALLOCATION_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "REQUESTER" character varying(255) NOT NULL,
    "REVIEWER" character varying(255) NOT NULL,
    "STATUS" character varying(255) NOT NULL
);


ALTER TABLE "AO_2D3BEA_WORKFLOW" OWNER TO jira;

--
-- Name: AO_2D3BEA_WORKFLOW_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_2D3BEA_WORKFLOW_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_2D3BEA_WORKFLOW_ID_seq" OWNER TO jira;

--
-- Name: AO_2D3BEA_WORKFLOW_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_2D3BEA_WORKFLOW_ID_seq" OWNED BY "AO_2D3BEA_WORKFLOW"."ID";


--
-- Name: AO_319474_MESSAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_319474_MESSAGE" (
    "CLAIMANT" character varying(127),
    "CLAIMANT_TIME" bigint,
    "CLAIM_COUNT" integer NOT NULL,
    "CONTENT_TYPE" character varying(255) NOT NULL,
    "CREATED_TIME" bigint NOT NULL,
    "EXPIRY_TIME" bigint,
    "ID" bigint NOT NULL,
    "MSG_DATA" text,
    "MSG_ID" character varying(127) NOT NULL,
    "MSG_LENGTH" bigint NOT NULL,
    "PRIORITY" integer NOT NULL,
    "QUEUE_ID" bigint NOT NULL,
    "VERSION" integer NOT NULL
);


ALTER TABLE "AO_319474_MESSAGE" OWNER TO jira;

--
-- Name: AO_319474_MESSAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_319474_MESSAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_319474_MESSAGE_ID_seq" OWNER TO jira;

--
-- Name: AO_319474_MESSAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_319474_MESSAGE_ID_seq" OWNED BY "AO_319474_MESSAGE"."ID";


--
-- Name: AO_319474_MESSAGE_PROPERTY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_319474_MESSAGE_PROPERTY" (
    "ID" bigint NOT NULL,
    "LONG_VALUE" bigint,
    "MESSAGE_ID" bigint NOT NULL,
    "NAME" character varying(450) NOT NULL,
    "PROPERTY_TYPE" character varying(1) NOT NULL,
    "STRING_VALUE" character varying(450)
);


ALTER TABLE "AO_319474_MESSAGE_PROPERTY" OWNER TO jira;

--
-- Name: AO_319474_MESSAGE_PROPERTY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_319474_MESSAGE_PROPERTY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_319474_MESSAGE_PROPERTY_ID_seq" OWNER TO jira;

--
-- Name: AO_319474_MESSAGE_PROPERTY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_319474_MESSAGE_PROPERTY_ID_seq" OWNED BY "AO_319474_MESSAGE_PROPERTY"."ID";


--
-- Name: AO_319474_QUEUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_319474_QUEUE" (
    "CLAIMANT" character varying(127),
    "CLAIMANT_TIME" bigint,
    "CREATED_TIME" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "MESSAGE_COUNT" bigint NOT NULL,
    "MODIFIED_TIME" bigint NOT NULL,
    "NAME" character varying(450) NOT NULL,
    "PURPOSE" character varying(450) NOT NULL,
    "TOPIC" character varying(450)
);


ALTER TABLE "AO_319474_QUEUE" OWNER TO jira;

--
-- Name: AO_319474_QUEUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_319474_QUEUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_319474_QUEUE_ID_seq" OWNER TO jira;

--
-- Name: AO_319474_QUEUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_319474_QUEUE_ID_seq" OWNED BY "AO_319474_QUEUE"."ID";


--
-- Name: AO_319474_QUEUE_PROPERTY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_319474_QUEUE_PROPERTY" (
    "ID" bigint NOT NULL,
    "LONG_VALUE" bigint,
    "NAME" character varying(450) NOT NULL,
    "PROPERTY_TYPE" character varying(1) NOT NULL,
    "QUEUE_ID" bigint NOT NULL,
    "STRING_VALUE" character varying(450)
);


ALTER TABLE "AO_319474_QUEUE_PROPERTY" OWNER TO jira;

--
-- Name: AO_319474_QUEUE_PROPERTY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_319474_QUEUE_PROPERTY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_319474_QUEUE_PROPERTY_ID_seq" OWNER TO jira;

--
-- Name: AO_319474_QUEUE_PROPERTY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_319474_QUEUE_PROPERTY_ID_seq" OWNED BY "AO_319474_QUEUE_PROPERTY"."ID";


--
-- Name: AO_3A3ECC_JIRACOMMENT_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_JIRACOMMENT_MAPPING" (
    "COMMENT_ID" bigint,
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint,
    "IS_NATIVE" boolean,
    "NATIVE" boolean,
    "PARENT_ID" character varying(255),
    "REMOTE_ID" character varying(255),
    "REMOTE_OBJECT_TYPE" character varying(255),
    "REMOTE_SYSTEM_ID" character varying(255)
);


ALTER TABLE "AO_3A3ECC_JIRACOMMENT_MAPPING" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq" OWNED BY "AO_3A3ECC_JIRACOMMENT_MAPPING"."ID";


--
-- Name: AO_3A3ECC_JIRAMAPPING_BEAN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_JIRAMAPPING_BEAN" (
    "ATTACHMENT_SYNC_ENABLED" boolean DEFAULT false,
    "AUTO_PULL_COMMENTS_ENABLED" boolean DEFAULT true,
    "AUTO_PUSH_COMMENTS_ENABLED" boolean DEFAULT true,
    "AUTO_PUSH_COMMENT_FOR_MS" boolean DEFAULT true,
    "ENABLE_RESTRICTED_COMMENTS" boolean DEFAULT false,
    "EXTERNAL_TO_JIRA_MAPPING_ID" integer DEFAULT 0,
    "ID" integer NOT NULL,
    "JIRAMAPPING_SET_ID" integer,
    "JIRA_TO_EXTERNAL_MAPPING_ID" integer DEFAULT 0,
    "MODIFY_REMOTE_COMMENTS_ALLOWED" boolean DEFAULT false,
    "PULL_COMMENT_PRIVACY" text,
    "QUERY_OPTIMIZED" boolean DEFAULT false,
    "REMOTE_OBJECT_TYPE" character varying(255),
    "REMOTE_SYSTEM_ID" character varying(255),
    "REQUIRED_FIELD_CHECK_EN" boolean DEFAULT false
);


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_BEAN" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq" OWNED BY "AO_3A3ECC_JIRAMAPPING_BEAN"."ID";


--
-- Name: AO_3A3ECC_JIRAMAPPING_SCHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_JIRAMAPPING_SCHEME" (
    "DESCRIPTION" character varying(255),
    "ID" integer NOT NULL,
    "JSON_MAPPING" text,
    "NAME" character varying(255)
);


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_SCHEME" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq" OWNED BY "AO_3A3ECC_JIRAMAPPING_SCHEME"."ID";


--
-- Name: AO_3A3ECC_JIRAMAPPING_SET; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_JIRAMAPPING_SET" (
    "DESCRIPTION" text,
    "ID" integer NOT NULL,
    "NAME" character varying(255)
);


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_SET" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_SET_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_JIRAMAPPING_SET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_JIRAMAPPING_SET_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAMAPPING_SET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_JIRAMAPPING_SET_ID_seq" OWNED BY "AO_3A3ECC_JIRAMAPPING_SET"."ID";


--
-- Name: AO_3A3ECC_JIRAPROJECT_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_JIRAPROJECT_MAPPING" (
    "ID" integer NOT NULL,
    "JIRAMAPPING_SCHEME_ID" integer,
    "PROJECT_ID" bigint DEFAULT 0
);


ALTER TABLE "AO_3A3ECC_JIRAPROJECT_MAPPING" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq" OWNED BY "AO_3A3ECC_JIRAPROJECT_MAPPING"."ID";


--
-- Name: AO_3A3ECC_REMOTE_IDCF; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_REMOTE_IDCF" (
    "CUSTOM_FIELD_ID" bigint DEFAULT 0,
    "ID" integer NOT NULL,
    "REMOTE_OBJECT_TYPE" character varying(255),
    "REMOTE_SYSTEM_ID" character varying(255)
);


ALTER TABLE "AO_3A3ECC_REMOTE_IDCF" OWNER TO jira;

--
-- Name: AO_3A3ECC_REMOTE_IDCF_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_REMOTE_IDCF_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_REMOTE_IDCF_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_REMOTE_IDCF_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_REMOTE_IDCF_ID_seq" OWNED BY "AO_3A3ECC_REMOTE_IDCF"."ID";


--
-- Name: AO_3A3ECC_REMOTE_LINK_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3A3ECC_REMOTE_LINK_CONFIG" (
    "CONNECTION_ID" integer DEFAULT 0,
    "DISPLAY_TEMPLATE" text,
    "ENABLED" boolean,
    "ID" integer NOT NULL,
    "OBJECT_TYPE" character varying(255)
);


ALTER TABLE "AO_3A3ECC_REMOTE_LINK_CONFIG" OWNER TO jira;

--
-- Name: AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq" OWNED BY "AO_3A3ECC_REMOTE_LINK_CONFIG"."ID";


--
-- Name: AO_3B1893_LOOP_DETECTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_3B1893_LOOP_DETECTION" (
    "COUNTER" integer DEFAULT 0 NOT NULL,
    "EXPIRES_AT" bigint DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "SENDER_EMAIL" text NOT NULL
);


ALTER TABLE "AO_3B1893_LOOP_DETECTION" OWNER TO jira;

--
-- Name: AO_3B1893_LOOP_DETECTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_3B1893_LOOP_DETECTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_3B1893_LOOP_DETECTION_ID_seq" OWNER TO jira;

--
-- Name: AO_3B1893_LOOP_DETECTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_3B1893_LOOP_DETECTION_ID_seq" OWNED BY "AO_3B1893_LOOP_DETECTION"."ID";


--
-- Name: AO_4AEACD_WEBHOOK_DAO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_4AEACD_WEBHOOK_DAO" (
    "ENABLED" boolean,
    "ENCODED_EVENTS" text,
    "FILTER" text,
    "ID" integer NOT NULL,
    "JQL" character varying(255),
    "LAST_UPDATED" timestamp with time zone NOT NULL,
    "LAST_UPDATED_USER" character varying(255) NOT NULL,
    "NAME" text NOT NULL,
    "REGISTRATION_METHOD" character varying(255) NOT NULL,
    "URL" text NOT NULL,
    "EXCLUDE_ISSUE_DETAILS" boolean,
    "PARAMETERS" text
);


ALTER TABLE "AO_4AEACD_WEBHOOK_DAO" OWNER TO jira;

--
-- Name: AO_4AEACD_WEBHOOK_DAO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_4AEACD_WEBHOOK_DAO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_4AEACD_WEBHOOK_DAO_ID_seq" OWNER TO jira;

--
-- Name: AO_4AEACD_WEBHOOK_DAO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_4AEACD_WEBHOOK_DAO_ID_seq" OWNED BY "AO_4AEACD_WEBHOOK_DAO"."ID";


--
-- Name: AO_4E8AE6_NOTIF_BATCH_QUEUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_4E8AE6_NOTIF_BATCH_QUEUE" (
    "AUTHOR_ID" bigint,
    "CONTEXT" character varying(63),
    "EVENT_TIME" bigint NOT NULL,
    "HTML_CONTENT" text,
    "ID" bigint NOT NULL,
    "ISSUE_ID" bigint,
    "PROJECT_ID" bigint NOT NULL,
    "RECIPIENT_ID" bigint NOT NULL,
    "SENT_TIME" bigint,
    "TEXT_CONTENT" text
);


ALTER TABLE "AO_4E8AE6_NOTIF_BATCH_QUEUE" OWNER TO jira;

--
-- Name: AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq" OWNER TO jira;

--
-- Name: AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq" OWNED BY "AO_4E8AE6_NOTIF_BATCH_QUEUE"."ID";


--
-- Name: AO_4E8AE6_OUT_EMAIL_SETTINGS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_4E8AE6_OUT_EMAIL_SETTINGS" (
    "CSS" text,
    "HTML_LINGO_ID" bigint,
    "ID" integer NOT NULL,
    "PLAINTEXT_LINGO_ID" bigint,
    "PROJECT_ID" bigint NOT NULL,
    "SUBJECT_LINGO_ID" bigint
);


ALTER TABLE "AO_4E8AE6_OUT_EMAIL_SETTINGS" OWNER TO jira;

--
-- Name: AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq" OWNER TO jira;

--
-- Name: AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq" OWNED BY "AO_4E8AE6_OUT_EMAIL_SETTINGS"."ID";


--
-- Name: AO_54307E_ASYNCUPGRADERECORD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_ASYNCUPGRADERECORD" (
    "ACTION" character varying(255) NOT NULL,
    "CREATED_DATE" timestamp without time zone NOT NULL,
    "EXCEPTION" text,
    "ID" integer NOT NULL,
    "MESSAGE" text,
    "SERVICE_DESK_VERSION" character varying(255) NOT NULL,
    "UPGRADE_TASK_NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_ASYNCUPGRADERECORD" OWNER TO jira;

--
-- Name: AO_54307E_ASYNCUPGRADERECORD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_ASYNCUPGRADERECORD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_ASYNCUPGRADERECORD_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_ASYNCUPGRADERECORD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_ASYNCUPGRADERECORD_ID_seq" OWNED BY "AO_54307E_ASYNCUPGRADERECORD"."ID";


--
-- Name: AO_54307E_CAPABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CAPABILITY" (
    "CAPABILITY_NAME" character varying(255) NOT NULL,
    "CAPABILITY_VALUE" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL,
    "USER_KEY" character varying(255)
);


ALTER TABLE "AO_54307E_CAPABILITY" OWNER TO jira;

--
-- Name: AO_54307E_CAPABILITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CAPABILITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CAPABILITY_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CAPABILITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CAPABILITY_ID_seq" OWNED BY "AO_54307E_CAPABILITY"."ID";


--
-- Name: AO_54307E_CONFLUENCEKB; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CONFLUENCEKB" (
    "APPLINKS_APPLICATION_ID" character varying(63),
    "APPLINK_NAME" character varying(255),
    "APPLINK_URL" text,
    "ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL,
    "SPACE_KEY" character varying(255),
    "SPACE_NAME" character varying(255),
    "SPACE_URL" text
);


ALTER TABLE "AO_54307E_CONFLUENCEKB" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKBENABLED; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CONFLUENCEKBENABLED" (
    "CONFLUENCE_KBID" integer NOT NULL,
    "ENABLED" boolean,
    "FORM_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_CONFLUENCEKBENABLED" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKBENABLED_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CONFLUENCEKBENABLED_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CONFLUENCEKBENABLED_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKBENABLED_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CONFLUENCEKBENABLED_ID_seq" OWNED BY "AO_54307E_CONFLUENCEKBENABLED"."ID";


--
-- Name: AO_54307E_CONFLUENCEKBLABELS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CONFLUENCEKBLABELS" (
    "CONFLUENCE_KBID" integer NOT NULL,
    "FORM_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "LABEL" character varying(255),
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_CONFLUENCEKBLABELS" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKBLABELS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CONFLUENCEKBLABELS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CONFLUENCEKBLABELS_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKBLABELS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CONFLUENCEKBLABELS_ID_seq" OWNED BY "AO_54307E_CONFLUENCEKBLABELS"."ID";


--
-- Name: AO_54307E_CONFLUENCEKB_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CONFLUENCEKB_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CONFLUENCEKB_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CONFLUENCEKB_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CONFLUENCEKB_ID_seq" OWNED BY "AO_54307E_CONFLUENCEKB"."ID";


--
-- Name: AO_54307E_CUSTOMGLOBALTHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CUSTOMGLOBALTHEME" (
    "CONTENT_LINK_COLOR" character varying(255),
    "CONTENT_TEXT_COLOR" character varying(255),
    "CUSTOM_CSS" text,
    "HEADER_BADGE_BGCOLOR" character varying(255),
    "HEADER_BGCOLOR" character varying(255),
    "HEADER_LINK_COLOR" character varying(255),
    "HEADER_LINK_HOVER_BGCOLOR" character varying(255),
    "HEADER_LINK_HOVER_COLOR" character varying(255),
    "HELP_CENTER_TITLE" character varying(255),
    "ID" integer NOT NULL,
    "LOGO_ID" integer
);


ALTER TABLE "AO_54307E_CUSTOMGLOBALTHEME" OWNER TO jira;

--
-- Name: AO_54307E_CUSTOMGLOBALTHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CUSTOMGLOBALTHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CUSTOMGLOBALTHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CUSTOMGLOBALTHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CUSTOMGLOBALTHEME_ID_seq" OWNED BY "AO_54307E_CUSTOMGLOBALTHEME"."ID";


--
-- Name: AO_54307E_CUSTOMTHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_CUSTOMTHEME" (
    "HEADER_BGCOLOR" character varying(255),
    "HEADER_LINK_COLOR" character varying(255),
    "HEADER_LINK_HOVER_BGCOLOR" character varying(255),
    "HEADER_LINK_HOVER_COLOR" character varying(255),
    "ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_CUSTOMTHEME" OWNER TO jira;

--
-- Name: AO_54307E_CUSTOMTHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_CUSTOMTHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_CUSTOMTHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_CUSTOMTHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_CUSTOMTHEME_ID_seq" OWNED BY "AO_54307E_CUSTOMTHEME"."ID";


--
-- Name: AO_54307E_EMAILCHANNELSETTING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_EMAILCHANNELSETTING" (
    "EMAIL_ADDRESS" character varying(255),
    "ID" integer NOT NULL,
    "LAST_PROCEEDED_TIME" bigint,
    "MAIL_CHANNEL_KEY" character varying(255) NOT NULL,
    "ON_DEMAND" boolean,
    "REQUEST_TYPE_ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_EMAILCHANNELSETTING" OWNER TO jira;

--
-- Name: AO_54307E_EMAILCHANNELSETTING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_EMAILCHANNELSETTING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_EMAILCHANNELSETTING_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_EMAILCHANNELSETTING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_EMAILCHANNELSETTING_ID_seq" OWNED BY "AO_54307E_EMAILCHANNELSETTING"."ID";


--
-- Name: AO_54307E_EMAILSETTINGS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_EMAILSETTINGS" (
    "EMAIL_ADDRESS" character varying(255),
    "ENABLED" boolean,
    "ID" integer NOT NULL,
    "JIRA_MAIL_SERVER_ID" bigint NOT NULL,
    "LAST_PROCEEDED_TIME" bigint,
    "ON_DEMAND" boolean,
    "REQUEST_TYPE_ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_EMAILSETTINGS" OWNER TO jira;

--
-- Name: AO_54307E_EMAILSETTINGS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_EMAILSETTINGS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_EMAILSETTINGS_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_EMAILSETTINGS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_EMAILSETTINGS_ID_seq" OWNED BY "AO_54307E_EMAILSETTINGS"."ID";


--
-- Name: AO_54307E_GOAL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_GOAL" (
    "DEFAULT_GOAL" boolean,
    "ID" integer NOT NULL,
    "JQL_QUERY" text,
    "POS" integer DEFAULT 0 NOT NULL,
    "TARGET_DURATION" bigint,
    "TIME_METRIC_ID" integer NOT NULL,
    "TIME_UPDATED_DATE" timestamp without time zone,
    "CALENDAR_ID" integer,
    "TIME_UPDATED_MS_EPOCH" bigint
);


ALTER TABLE "AO_54307E_GOAL" OWNER TO jira;

--
-- Name: AO_54307E_GOAL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_GOAL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_GOAL_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_GOAL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_GOAL_ID_seq" OWNED BY "AO_54307E_GOAL"."ID";


--
-- Name: AO_54307E_GROUP; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_GROUP" (
    "GROUP_NAME" character varying(127),
    "ID" integer NOT NULL,
    "VIEWPORT_ID" integer,
    "DELETED_TIME" bigint,
    "ORDER" integer
);


ALTER TABLE "AO_54307E_GROUP" OWNER TO jira;

--
-- Name: AO_54307E_GROUPTOREQUESTTYPE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_GROUPTOREQUESTTYPE" (
    "GROUP_ID" integer,
    "ID" integer NOT NULL,
    "REQUEST_TYPE_ID" integer,
    "ORDER" integer
);


ALTER TABLE "AO_54307E_GROUPTOREQUESTTYPE" OWNER TO jira;

--
-- Name: AO_54307E_GROUPTOREQUESTTYPE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_GROUPTOREQUESTTYPE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_GROUPTOREQUESTTYPE_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_GROUPTOREQUESTTYPE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_GROUPTOREQUESTTYPE_ID_seq" OWNED BY "AO_54307E_GROUPTOREQUESTTYPE"."ID";


--
-- Name: AO_54307E_GROUP_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_GROUP_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_GROUP_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_GROUP_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_GROUP_ID_seq" OWNED BY "AO_54307E_GROUP"."ID";


--
-- Name: AO_54307E_IMAGES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_IMAGES" (
    "CONTENTS" text,
    "ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_IMAGES" OWNER TO jira;

--
-- Name: AO_54307E_IMAGES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_IMAGES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_IMAGES_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_IMAGES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_IMAGES_ID_seq" OWNED BY "AO_54307E_IMAGES"."ID";


--
-- Name: AO_54307E_METRICCONDITION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_METRICCONDITION" (
    "CONDITION_ID" character varying(255) NOT NULL,
    "FACTORY_KEY" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "PLUGIN_KEY" character varying(255) NOT NULL,
    "TIME_METRIC_ID" integer NOT NULL,
    "TYPE_NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_METRICCONDITION" OWNER TO jira;

--
-- Name: AO_54307E_METRICCONDITION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_METRICCONDITION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_METRICCONDITION_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_METRICCONDITION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_METRICCONDITION_ID_seq" OWNED BY "AO_54307E_METRICCONDITION"."ID";


--
-- Name: AO_54307E_ORGANIZATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_ORGANIZATION" (
    "ID" integer NOT NULL,
    "LOWER_NAME" character varying(255) NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "SEARCH_NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_ORGANIZATION" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_ORGANIZATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_ORGANIZATION_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_ORGANIZATION_ID_seq" OWNED BY "AO_54307E_ORGANIZATION"."ID";


--
-- Name: AO_54307E_ORGANIZATION_MEMBER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_ORGANIZATION_MEMBER" (
    "DEFAULT_ORGANIZATION" boolean DEFAULT false NOT NULL,
    "ID" integer NOT NULL,
    "ORGANIZATION_ID" integer,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_ORGANIZATION_MEMBER" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_MEMBER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_ORGANIZATION_MEMBER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_ORGANIZATION_MEMBER_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_MEMBER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_ORGANIZATION_MEMBER_ID_seq" OWNED BY "AO_54307E_ORGANIZATION_MEMBER"."ID";


--
-- Name: AO_54307E_ORGANIZATION_PROJECT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_ORGANIZATION_PROJECT" (
    "ID" integer NOT NULL,
    "ORGANIZATION_ID" integer,
    "PROJECT_ID" bigint NOT NULL
);


ALTER TABLE "AO_54307E_ORGANIZATION_PROJECT" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_PROJECT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_ORGANIZATION_PROJECT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_ORGANIZATION_PROJECT_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_ORGANIZATION_PROJECT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_ORGANIZATION_PROJECT_ID_seq" OWNED BY "AO_54307E_ORGANIZATION_PROJECT"."ID";


--
-- Name: AO_54307E_OUT_EMAIL_SETTINGS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_OUT_EMAIL_SETTINGS" (
    "EMAIL_SUBJECT_PREFIX_ENABLED" boolean NOT NULL,
    "ID" integer NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_OUT_EMAIL_SETTINGS" OWNER TO jira;

--
-- Name: AO_54307E_OUT_EMAIL_SETTINGS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_OUT_EMAIL_SETTINGS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_OUT_EMAIL_SETTINGS_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_OUT_EMAIL_SETTINGS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_OUT_EMAIL_SETTINGS_ID_seq" OWNED BY "AO_54307E_OUT_EMAIL_SETTINGS"."ID";


--
-- Name: AO_54307E_PARTICIPANTSETTINGS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_PARTICIPANTSETTINGS" (
    "AUTOCOMPLETE_ENABLED" boolean,
    "ID" integer NOT NULL,
    "MANAGE_ENABLED" boolean,
    "SERVICE_DESK_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_PARTICIPANTSETTINGS" OWNER TO jira;

--
-- Name: AO_54307E_PARTICIPANTSETTINGS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_PARTICIPANTSETTINGS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_PARTICIPANTSETTINGS_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_PARTICIPANTSETTINGS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_PARTICIPANTSETTINGS_ID_seq" OWNED BY "AO_54307E_PARTICIPANTSETTINGS"."ID";


--
-- Name: AO_54307E_QUEUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_QUEUE" (
    "ID" integer NOT NULL,
    "JQL" text,
    "PROJECT_ID" bigint NOT NULL,
    "PROJECT_KEY" character varying(255) NOT NULL,
    "QUEUE_ORDER" integer,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_QUEUE" OWNER TO jira;

--
-- Name: AO_54307E_QUEUECOLUMN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_QUEUECOLUMN" (
    "COLUMN_ID" character varying(255),
    "COLUMN_ORDER" integer,
    "ID" integer NOT NULL,
    "QUEUE_ID" integer
);


ALTER TABLE "AO_54307E_QUEUECOLUMN" OWNER TO jira;

--
-- Name: AO_54307E_QUEUECOLUMN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_QUEUECOLUMN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_QUEUECOLUMN_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_QUEUECOLUMN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_QUEUECOLUMN_ID_seq" OWNED BY "AO_54307E_QUEUECOLUMN"."ID";


--
-- Name: AO_54307E_QUEUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_QUEUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_QUEUE_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_QUEUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_QUEUE_ID_seq" OWNED BY "AO_54307E_QUEUE"."ID";


--
-- Name: AO_54307E_REPORT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_REPORT" (
    "CREATED_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "REPORT_ORDER" integer,
    "REPORT_TYPE" character varying(63),
    "SERVICE_DESK_ID" integer,
    "UPDATED_DATE" timestamp without time zone,
    "TARGET" bigint
);


ALTER TABLE "AO_54307E_REPORT" OWNER TO jira;

--
-- Name: AO_54307E_REPORT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_REPORT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_REPORT_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_REPORT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_REPORT_ID_seq" OWNED BY "AO_54307E_REPORT"."ID";


--
-- Name: AO_54307E_SERIES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_SERIES" (
    "COLOR" character varying(63),
    "CREATED_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "JQL" text,
    "REPORT_ID" integer,
    "SERIES_DATA_TYPE" character varying(255),
    "SERIES_LABEL" character varying(63),
    "UPDATED_DATE" timestamp without time zone,
    "TIME_METRIC_ID" bigint,
    "GOAL_ID" integer
);


ALTER TABLE "AO_54307E_SERIES" OWNER TO jira;

--
-- Name: AO_54307E_SERIES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_SERIES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_SERIES_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_SERIES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_SERIES_ID_seq" OWNED BY "AO_54307E_SERIES"."ID";


--
-- Name: AO_54307E_SERVICEDESK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_SERVICEDESK" (
    "ID" integer NOT NULL,
    "PROJECT_ID" bigint,
    "PROJECT_KEY" character varying(255) DEFAULT 'N/A'::character varying NOT NULL,
    "DISABLED" boolean,
    "PUBLIC_SIGNUP" integer,
    "CREATED_BY_USER_KEY" character varying(255),
    "CREATED_WITH_EMPTY_PROJECT" boolean,
    "LEGACY_TRANSITION_DISABLED" boolean,
    "VERSION_CREATED_AT" character varying(255),
    "CREATED_DATE" timestamp without time zone,
    "OPEN_CUSTOMER_ACCESS" integer
);


ALTER TABLE "AO_54307E_SERVICEDESK" OWNER TO jira;

--
-- Name: AO_54307E_SERVICEDESK_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_SERVICEDESK_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_SERVICEDESK_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_SERVICEDESK_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_SERVICEDESK_ID_seq" OWNED BY "AO_54307E_SERVICEDESK"."ID";


--
-- Name: AO_54307E_STATUSMAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_STATUSMAPPING" (
    "FORM_ID" integer,
    "ID" integer NOT NULL,
    "STATUS_ID" character varying(255),
    "STATUS_NAME" character varying(255)
);


ALTER TABLE "AO_54307E_STATUSMAPPING" OWNER TO jira;

--
-- Name: AO_54307E_STATUSMAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_STATUSMAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_STATUSMAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_STATUSMAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_STATUSMAPPING_ID_seq" OWNED BY "AO_54307E_STATUSMAPPING"."ID";


--
-- Name: AO_54307E_SUBSCRIPTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_SUBSCRIPTION" (
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint NOT NULL,
    "SUBSCRIBED" boolean NOT NULL,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_SUBSCRIPTION" OWNER TO jira;

--
-- Name: AO_54307E_SUBSCRIPTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_SUBSCRIPTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_SUBSCRIPTION_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_SUBSCRIPTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_SUBSCRIPTION_ID_seq" OWNED BY "AO_54307E_SUBSCRIPTION"."ID";


--
-- Name: AO_54307E_SYNCUPGRADERECORD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_SYNCUPGRADERECORD" (
    "ACTION" character varying(255) NOT NULL,
    "CREATED_DATE" timestamp without time zone NOT NULL,
    "EXCEPTION" text,
    "ID" integer NOT NULL,
    "MESSAGE" text,
    "SERVICE_DESK_VERSION" character varying(255) NOT NULL,
    "UPGRADE_TASK_NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_54307E_SYNCUPGRADERECORD" OWNER TO jira;

--
-- Name: AO_54307E_SYNCUPGRADERECORD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_SYNCUPGRADERECORD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_SYNCUPGRADERECORD_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_SYNCUPGRADERECORD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_SYNCUPGRADERECORD_ID_seq" OWNED BY "AO_54307E_SYNCUPGRADERECORD"."ID";


--
-- Name: AO_54307E_THRESHOLD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_THRESHOLD" (
    "ID" integer NOT NULL,
    "REMAINING_TIME" bigint NOT NULL,
    "TIME_METRIC_ID" integer NOT NULL
);


ALTER TABLE "AO_54307E_THRESHOLD" OWNER TO jira;

--
-- Name: AO_54307E_THRESHOLD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_THRESHOLD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_THRESHOLD_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_THRESHOLD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_THRESHOLD_ID_seq" OWNED BY "AO_54307E_THRESHOLD"."ID";


--
-- Name: AO_54307E_TIMEMETRIC; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_TIMEMETRIC" (
    "CUSTOM_FIELD_ID" bigint,
    "DEFINITION_CHANGE_DATE" timestamp without time zone,
    "GOALS_CHANGE_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "SERVICE_DESK_ID" integer NOT NULL,
    "GOALS_CHANGE_MS_EPOCH" bigint,
    "THRESHOLDS_CHANGE_MS_EPOCH" bigint,
    "CREATED_DATE" bigint,
    "THRESHOLDS_CONFIG_CHANGE_DATE" timestamp without time zone,
    "DEFINITION_CHANGE_MS_EPOCH" bigint
);


ALTER TABLE "AO_54307E_TIMEMETRIC" OWNER TO jira;

--
-- Name: AO_54307E_TIMEMETRIC_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_TIMEMETRIC_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_TIMEMETRIC_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_TIMEMETRIC_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_TIMEMETRIC_ID_seq" OWNED BY "AO_54307E_TIMEMETRIC"."ID";


--
-- Name: AO_54307E_VIEWPORT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_VIEWPORT" (
    "DESCRIPTION" text,
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "LOGO_ID" integer,
    "NAME" character varying(255) NOT NULL,
    "PROJECT_ID" bigint NOT NULL,
    "SEND_EMAIL_NOTIFICATIONS" boolean,
    "THEME_ID" integer
);


ALTER TABLE "AO_54307E_VIEWPORT" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFIELD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_VIEWPORTFIELD" (
    "DESCRIPTION" text,
    "DISPLAYED" boolean,
    "FIELD_ID" character varying(255) NOT NULL,
    "FIELD_TYPE" character varying(255) NOT NULL,
    "FORM_ID" integer,
    "ID" integer NOT NULL,
    "LABEL" character varying(255),
    "REQUIRED" boolean,
    "FIELD_ORDER" integer
);


ALTER TABLE "AO_54307E_VIEWPORTFIELD" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFIELDVALUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_VIEWPORTFIELDVALUE" (
    "FIELD_ID" integer,
    "FIELD_NAME" character varying(255),
    "ID" integer NOT NULL,
    "VALUE" text,
    "VALUE_ORDER" integer
);


ALTER TABLE "AO_54307E_VIEWPORTFIELDVALUE" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFIELDVALUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_VIEWPORTFIELDVALUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_VIEWPORTFIELDVALUE_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFIELDVALUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_VIEWPORTFIELDVALUE_ID_seq" OWNED BY "AO_54307E_VIEWPORTFIELDVALUE"."ID";


--
-- Name: AO_54307E_VIEWPORTFIELD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_VIEWPORTFIELD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_VIEWPORTFIELD_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFIELD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_VIEWPORTFIELD_ID_seq" OWNED BY "AO_54307E_VIEWPORTFIELD"."ID";


--
-- Name: AO_54307E_VIEWPORTFORM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_54307E_VIEWPORTFORM" (
    "CALL_TO_ACTION" text,
    "DESCRIPTION" text,
    "ICON" integer,
    "ID" integer NOT NULL,
    "INTRO" text,
    "ISSUE_TYPE_ID" bigint NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "VIEWPORT_ID" integer,
    "FORM_ORDER" integer,
    "ICON_ID" bigint
);


ALTER TABLE "AO_54307E_VIEWPORTFORM" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFORM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_VIEWPORTFORM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_VIEWPORTFORM_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORTFORM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_VIEWPORTFORM_ID_seq" OWNED BY "AO_54307E_VIEWPORTFORM"."ID";


--
-- Name: AO_54307E_VIEWPORT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_54307E_VIEWPORT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_54307E_VIEWPORT_ID_seq" OWNER TO jira;

--
-- Name: AO_54307E_VIEWPORT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_54307E_VIEWPORT_ID_seq" OWNED BY "AO_54307E_VIEWPORT"."ID";


--
-- Name: AO_550953_SHORTCUT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_550953_SHORTCUT" (
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "PROJECT_ID" bigint,
    "SHORTCUT_URL" text,
    "URL" character varying(255),
    "ICON" character varying(255),
    "SHORTCUT_TYPE" character varying,
    "IS_PENDING_APPROVAL" boolean DEFAULT false,
    "DVCS_TYPE" character varying
);


ALTER TABLE "AO_550953_SHORTCUT" OWNER TO jira;

--
-- Name: AO_550953_SHORTCUT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_550953_SHORTCUT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_550953_SHORTCUT_ID_seq" OWNER TO jira;

--
-- Name: AO_550953_SHORTCUT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_550953_SHORTCUT_ID_seq" OWNED BY "AO_550953_SHORTCUT"."ID";


--
-- Name: ao_563aee_activity_entity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_563aee_activity_entity (
    activity_id bigint NOT NULL,
    actor_id integer,
    content text,
    generator_display_name character varying(255),
    generator_id character varying(450),
    icon_id integer,
    id character varying(450),
    issue_key character varying(255),
    object_id integer,
    poster character varying(255),
    project_key character varying(255),
    published timestamp with time zone,
    target_id integer,
    title character varying(255),
    url character varying(450),
    username character varying(255),
    verb character varying(450)
);


ALTER TABLE ao_563aee_activity_entity OWNER TO jira;

--
-- Name: AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq" OWNER TO jira;

--
-- Name: AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq" OWNED BY ao_563aee_activity_entity.activity_id;


--
-- Name: ao_563aee_actor_entity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_563aee_actor_entity (
    full_name character varying(255),
    id integer NOT NULL,
    profile_page_uri character varying(450),
    profile_picture_uri character varying(450),
    username character varying(255)
);


ALTER TABLE ao_563aee_actor_entity OWNER TO jira;

--
-- Name: AO_563AEE_ACTOR_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_563AEE_ACTOR_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_563AEE_ACTOR_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_563AEE_ACTOR_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_563AEE_ACTOR_ENTITY_ID_seq" OWNED BY ao_563aee_actor_entity.id;


--
-- Name: ao_563aee_media_link_entity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_563aee_media_link_entity (
    duration integer,
    height integer,
    id integer NOT NULL,
    url character varying(450),
    width integer
);


ALTER TABLE ao_563aee_media_link_entity OWNER TO jira;

--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_563AEE_MEDIA_LINK_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_563AEE_MEDIA_LINK_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_563AEE_MEDIA_LINK_ENTITY_ID_seq" OWNED BY ao_563aee_media_link_entity.id;


--
-- Name: ao_563aee_object_entity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_563aee_object_entity (
    content character varying(255),
    display_name character varying(255),
    id integer NOT NULL,
    image_id integer,
    object_id character varying(450),
    object_type character varying(450),
    summary character varying(255),
    url character varying(450)
);


ALTER TABLE ao_563aee_object_entity OWNER TO jira;

--
-- Name: AO_563AEE_OBJECT_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_563AEE_OBJECT_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_563AEE_OBJECT_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_563AEE_OBJECT_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_563AEE_OBJECT_ENTITY_ID_seq" OWNED BY ao_563aee_object_entity.id;


--
-- Name: ao_563aee_target_entity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_563aee_target_entity (
    content character varying(255),
    display_name character varying(255),
    id integer NOT NULL,
    image_id integer,
    object_id character varying(450),
    object_type character varying(450),
    summary character varying(255),
    url character varying(450)
);


ALTER TABLE ao_563aee_target_entity OWNER TO jira;

--
-- Name: AO_563AEE_TARGET_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_563AEE_TARGET_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_563AEE_TARGET_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_563AEE_TARGET_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_563AEE_TARGET_ENTITY_ID_seq" OWNED BY ao_563aee_target_entity.id;


--
-- Name: AO_56464C_APPROVAL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_56464C_APPROVAL" (
    "APPROVE_CONDITION_TYPE" character varying(255) NOT NULL,
    "APPROVE_CONDITION_VALUE" character varying(255) NOT NULL,
    "COMPLETED_DATE" bigint,
    "CREATED_DATE" bigint NOT NULL,
    "DECISION" character varying(255),
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "STATUS_ID" character varying(255)
);


ALTER TABLE "AO_56464C_APPROVAL" OWNER TO jira;

--
-- Name: AO_56464C_APPROVAL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_56464C_APPROVAL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_56464C_APPROVAL_ID_seq" OWNER TO jira;

--
-- Name: AO_56464C_APPROVAL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_56464C_APPROVAL_ID_seq" OWNED BY "AO_56464C_APPROVAL"."ID";


--
-- Name: AO_56464C_APPROVER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_56464C_APPROVER" (
    "APPROVAL_ID" integer,
    "ID" integer NOT NULL,
    "TYPE" character varying(255) NOT NULL,
    "VALUE" character varying(255) NOT NULL
);


ALTER TABLE "AO_56464C_APPROVER" OWNER TO jira;

--
-- Name: AO_56464C_APPROVERDECISION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_56464C_APPROVERDECISION" (
    "APPROVAL_ID" integer,
    "DECISION" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "SENT_DATE" bigint NOT NULL,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_56464C_APPROVERDECISION" OWNER TO jira;

--
-- Name: AO_56464C_APPROVERDECISION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_56464C_APPROVERDECISION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_56464C_APPROVERDECISION_ID_seq" OWNER TO jira;

--
-- Name: AO_56464C_APPROVERDECISION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_56464C_APPROVERDECISION_ID_seq" OWNED BY "AO_56464C_APPROVERDECISION"."ID";


--
-- Name: AO_56464C_APPROVER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_56464C_APPROVER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_56464C_APPROVER_ID_seq" OWNER TO jira;

--
-- Name: AO_56464C_APPROVER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_56464C_APPROVER_ID_seq" OWNED BY "AO_56464C_APPROVER"."ID";


--
-- Name: AO_56464C_NOTIFICATIONRECORD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_56464C_NOTIFICATIONRECORD" (
    "APPROVAL_ID" integer,
    "ID" integer NOT NULL,
    "SENT_DATE" bigint NOT NULL,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_56464C_NOTIFICATIONRECORD" OWNER TO jira;

--
-- Name: AO_56464C_NOTIFICATIONRECORD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_56464C_NOTIFICATIONRECORD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_56464C_NOTIFICATIONRECORD_ID_seq" OWNER TO jira;

--
-- Name: AO_56464C_NOTIFICATIONRECORD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_56464C_NOTIFICATIONRECORD_ID_seq" OWNED BY "AO_56464C_NOTIFICATIONRECORD"."ID";


--
-- Name: AO_575BF5_ISSUE_SUMMARY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_575BF5_ISSUE_SUMMARY" (
    "COMMITS" bigint DEFAULT 0,
    "FAILING_BUILDS" bigint DEFAULT 0,
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint DEFAULT 0 NOT NULL,
    "OPEN_PRS" bigint DEFAULT 0,
    "OPEN_REVIEWS" bigint DEFAULT 0,
    "PRS" bigint DEFAULT 0,
    "REVIEWS" bigint DEFAULT 0,
    "UPDATED_ON" timestamp with time zone
);


ALTER TABLE "AO_575BF5_ISSUE_SUMMARY" OWNER TO jira;

--
-- Name: AO_575BF5_ISSUE_SUMMARY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_575BF5_ISSUE_SUMMARY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_575BF5_ISSUE_SUMMARY_ID_seq" OWNER TO jira;

--
-- Name: AO_575BF5_ISSUE_SUMMARY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_575BF5_ISSUE_SUMMARY_ID_seq" OWNED BY "AO_575BF5_ISSUE_SUMMARY"."ID";


--
-- Name: AO_575BF5_PROCESSED_COMMITS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_575BF5_PROCESSED_COMMITS" (
    "COMMIT_HASH" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "METADATA_HASH" character varying(255)
);


ALTER TABLE "AO_575BF5_PROCESSED_COMMITS" OWNER TO jira;

--
-- Name: AO_575BF5_PROCESSED_COMMITS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_575BF5_PROCESSED_COMMITS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_575BF5_PROCESSED_COMMITS_ID_seq" OWNER TO jira;

--
-- Name: AO_575BF5_PROCESSED_COMMITS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_575BF5_PROCESSED_COMMITS_ID_seq" OWNED BY "AO_575BF5_PROCESSED_COMMITS"."ID";


--
-- Name: AO_575BF5_PROVIDER_ISSUE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_575BF5_PROVIDER_ISSUE" (
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint DEFAULT 0 NOT NULL,
    "PROVIDER_SOURCE_ID" character varying(255) NOT NULL,
    "STALE_AFTER" bigint
);


ALTER TABLE "AO_575BF5_PROVIDER_ISSUE" OWNER TO jira;

--
-- Name: AO_575BF5_PROVIDER_ISSUE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_575BF5_PROVIDER_ISSUE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_575BF5_PROVIDER_ISSUE_ID_seq" OWNER TO jira;

--
-- Name: AO_575BF5_PROVIDER_ISSUE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_575BF5_PROVIDER_ISSUE_ID_seq" OWNED BY "AO_575BF5_PROVIDER_ISSUE"."ID";


--
-- Name: AO_587B34_GLANCE_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_587B34_GLANCE_CONFIG" (
    "ROOM_ID" bigint DEFAULT 0 NOT NULL,
    "STATE" character varying(255),
    "SYNC_NEEDED" boolean,
    "JQL" character varying(255),
    "APPLICATION_USER_KEY" character varying(255),
    "NAME" character varying(255)
);


ALTER TABLE "AO_587B34_GLANCE_CONFIG" OWNER TO jira;

--
-- Name: AO_587B34_PROJECT_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_587B34_PROJECT_CONFIG" (
    "CONFIGURATION_GROUP_ID" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "NAME_UNIQUE_CONSTRAINT" character varying(255) NOT NULL,
    "PROJECT_ID" bigint DEFAULT 0 NOT NULL,
    "ROOM_ID" bigint DEFAULT 0 NOT NULL,
    "VALUE" character varying(255)
);


ALTER TABLE "AO_587B34_PROJECT_CONFIG" OWNER TO jira;

--
-- Name: AO_587B34_PROJECT_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_587B34_PROJECT_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_587B34_PROJECT_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_587B34_PROJECT_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_587B34_PROJECT_CONFIG_ID_seq" OWNED BY "AO_587B34_PROJECT_CONFIG"."ID";


--
-- Name: AO_5FB9D7_AOHIP_CHAT_LINK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_5FB9D7_AOHIP_CHAT_LINK" (
    "ADDON_TOKEN_EXPIRY" timestamp with time zone,
    "API_URL" character varying(255),
    "CONNECT_DESCRIPTOR" text,
    "GROUP_ID" integer DEFAULT 0,
    "GROUP_NAME" character varying(255),
    "ID" integer NOT NULL,
    "OAUTH_ID" character varying(255),
    "SECRET_KEY" character varying(255),
    "SYSTEM_PASSWORD" character varying(255),
    "SYSTEM_TOKEN_EXPIRY" timestamp with time zone,
    "SYSTEM_USER" character varying(255),
    "SYSTEM_USER_TOKEN" character varying(255),
    "TOKEN" character varying(255)
);


ALTER TABLE "AO_5FB9D7_AOHIP_CHAT_LINK" OWNER TO jira;

--
-- Name: AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq" OWNER TO jira;

--
-- Name: AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq" OWNED BY "AO_5FB9D7_AOHIP_CHAT_LINK"."ID";


--
-- Name: AO_5FB9D7_AOHIP_CHAT_USER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_5FB9D7_AOHIP_CHAT_USER" (
    "HIP_CHAT_LINK_ID" integer,
    "HIP_CHAT_USER_ID" character varying(255),
    "HIP_CHAT_USER_NAME" character varying(255),
    "ID" integer NOT NULL,
    "REFRESH_CODE" character varying(255),
    "USER_KEY" character varying(255),
    "USER_SCOPES" character varying(255),
    "USER_TOKEN" character varying(255),
    "USER_TOKEN_EXPIRY" timestamp with time zone
);


ALTER TABLE "AO_5FB9D7_AOHIP_CHAT_USER" OWNER TO jira;

--
-- Name: AO_5FB9D7_AOHIP_CHAT_USER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_5FB9D7_AOHIP_CHAT_USER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_5FB9D7_AOHIP_CHAT_USER_ID_seq" OWNER TO jira;

--
-- Name: AO_5FB9D7_AOHIP_CHAT_USER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_5FB9D7_AOHIP_CHAT_USER_ID_seq" OWNED BY "AO_5FB9D7_AOHIP_CHAT_USER"."ID";


--
-- Name: AO_60DB71_AUDITENTRY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_AUDITENTRY" (
    "CATEGORY" character varying(255) NOT NULL,
    "DATA" text NOT NULL,
    "ENTITY_CLASS" character varying(255) NOT NULL,
    "ENTITY_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "TIME" bigint,
    "USER" character varying(255)
);


ALTER TABLE "AO_60DB71_AUDITENTRY" OWNER TO jira;

--
-- Name: AO_60DB71_AUDITENTRY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_AUDITENTRY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_AUDITENTRY_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_AUDITENTRY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_AUDITENTRY_ID_seq" OWNED BY "AO_60DB71_AUDITENTRY"."ID";


--
-- Name: AO_60DB71_BOARDADMINS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_BOARDADMINS" (
    "ID" bigint NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_BOARDADMINS" OWNER TO jira;

--
-- Name: AO_60DB71_BOARDADMINS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_BOARDADMINS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_BOARDADMINS_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_BOARDADMINS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_BOARDADMINS_ID_seq" OWNED BY "AO_60DB71_BOARDADMINS"."ID";


--
-- Name: AO_60DB71_CARDCOLOR; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_CARDCOLOR" (
    "COLOR" character varying(255),
    "ID" bigint NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "STRATEGY" character varying(255),
    "VAL" character varying(255)
);


ALTER TABLE "AO_60DB71_CARDCOLOR" OWNER TO jira;

--
-- Name: AO_60DB71_CARDCOLOR_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_CARDCOLOR_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_CARDCOLOR_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_CARDCOLOR_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_CARDCOLOR_ID_seq" OWNED BY "AO_60DB71_CARDCOLOR"."ID";


--
-- Name: AO_60DB71_CARDLAYOUT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_CARDLAYOUT" (
    "FIELD_ID" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "MODE_NAME" character varying(255) NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_CARDLAYOUT" OWNER TO jira;

--
-- Name: AO_60DB71_CARDLAYOUT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_CARDLAYOUT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_CARDLAYOUT_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_CARDLAYOUT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_CARDLAYOUT_ID_seq" OWNED BY "AO_60DB71_CARDLAYOUT"."ID";


--
-- Name: AO_60DB71_COLUMN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_COLUMN" (
    "ID" bigint NOT NULL,
    "MAXIM" double precision,
    "MINIM" double precision,
    "NAME" character varying(255),
    "POS" integer DEFAULT 0 NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_COLUMN" OWNER TO jira;

--
-- Name: AO_60DB71_COLUMNSTATUS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_COLUMNSTATUS" (
    "COLUMN_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "STATUS_ID" character varying(255)
);


ALTER TABLE "AO_60DB71_COLUMNSTATUS" OWNER TO jira;

--
-- Name: AO_60DB71_COLUMNSTATUS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_COLUMNSTATUS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_COLUMNSTATUS_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_COLUMNSTATUS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_COLUMNSTATUS_ID_seq" OWNED BY "AO_60DB71_COLUMNSTATUS"."ID";


--
-- Name: AO_60DB71_COLUMN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_COLUMN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_COLUMN_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_COLUMN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_COLUMN_ID_seq" OWNED BY "AO_60DB71_COLUMN"."ID";


--
-- Name: AO_60DB71_CREATIONCONVERSATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_CREATIONCONVERSATION" (
    "CREATED_TIME" bigint DEFAULT 0 NOT NULL,
    "ID" bigint NOT NULL,
    "STATUS" character varying(255) NOT NULL,
    "TOKEN" character varying(255) NOT NULL,
    "USER_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_CREATIONCONVERSATION" OWNER TO jira;

--
-- Name: AO_60DB71_CREATIONCONVERSATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_CREATIONCONVERSATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_CREATIONCONVERSATION_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_CREATIONCONVERSATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_CREATIONCONVERSATION_ID_seq" OWNED BY "AO_60DB71_CREATIONCONVERSATION"."ID";


--
-- Name: AO_60DB71_DETAILVIEWFIELD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_DETAILVIEWFIELD" (
    "FIELD_ID" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_DETAILVIEWFIELD" OWNER TO jira;

--
-- Name: AO_60DB71_DETAILVIEWFIELD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_DETAILVIEWFIELD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_DETAILVIEWFIELD_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_DETAILVIEWFIELD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_DETAILVIEWFIELD_ID_seq" OWNED BY "AO_60DB71_DETAILVIEWFIELD"."ID";


--
-- Name: AO_60DB71_ESTIMATESTATISTIC; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_ESTIMATESTATISTIC" (
    "FIELD_ID" character varying(255),
    "ID" bigint NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "TYPE_ID" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_ESTIMATESTATISTIC" OWNER TO jira;

--
-- Name: AO_60DB71_ESTIMATESTATISTIC_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_ESTIMATESTATISTIC_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_ESTIMATESTATISTIC_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_ESTIMATESTATISTIC_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_ESTIMATESTATISTIC_ID_seq" OWNED BY "AO_60DB71_ESTIMATESTATISTIC"."ID";


--
-- Name: AO_60DB71_ISSUERANKING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_ISSUERANKING" (
    "CUSTOM_FIELD_ID" bigint DEFAULT 0 NOT NULL,
    "ID" bigint NOT NULL,
    "ISSUE_ID" bigint DEFAULT 0 NOT NULL,
    "NEXT_ID" bigint
);


ALTER TABLE "AO_60DB71_ISSUERANKING" OWNER TO jira;

--
-- Name: AO_60DB71_ISSUERANKINGLOG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_ISSUERANKINGLOG" (
    "CUSTOM_FIELD_ID" bigint,
    "ID" bigint NOT NULL,
    "ISSUE_ID" bigint,
    "LOG_TYPE" character varying(255),
    "NEW_NEXT_ID" bigint,
    "NEW_PREVIOUS_ID" bigint,
    "OLD_NEXT_ID" bigint,
    "OLD_PREVIOUS_ID" bigint,
    "SANITY_CHECKED" character varying(255)
);


ALTER TABLE "AO_60DB71_ISSUERANKINGLOG" OWNER TO jira;

--
-- Name: AO_60DB71_ISSUERANKINGLOG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_ISSUERANKINGLOG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_ISSUERANKINGLOG_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_ISSUERANKINGLOG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_ISSUERANKINGLOG_ID_seq" OWNED BY "AO_60DB71_ISSUERANKINGLOG"."ID";


--
-- Name: AO_60DB71_ISSUERANKING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_ISSUERANKING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_ISSUERANKING_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_ISSUERANKING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_ISSUERANKING_ID_seq" OWNED BY "AO_60DB71_ISSUERANKING"."ID";


--
-- Name: AO_60DB71_LEXORANK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_LEXORANK" (
    "FIELD_ID" bigint DEFAULT 0 NOT NULL,
    "ID" bigint NOT NULL,
    "ISSUE_ID" bigint DEFAULT 0 NOT NULL,
    "LOCK_HASH" character varying(255),
    "LOCK_TIME" bigint,
    "RANK" character varying(255) NOT NULL,
    "TYPE" integer DEFAULT 0 NOT NULL
);


ALTER TABLE "AO_60DB71_LEXORANK" OWNER TO jira;

--
-- Name: AO_60DB71_LEXORANKBALANCER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_LEXORANKBALANCER" (
    "DISABLE_RANK_OPERATIONS" boolean NOT NULL,
    "FIELD_ID" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "REBALANCE_TIME" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_LEXORANKBALANCER" OWNER TO jira;

--
-- Name: AO_60DB71_LEXORANKBALANCER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_LEXORANKBALANCER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_LEXORANKBALANCER_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_LEXORANKBALANCER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_LEXORANKBALANCER_ID_seq" OWNED BY "AO_60DB71_LEXORANKBALANCER"."ID";


--
-- Name: AO_60DB71_LEXORANK_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_LEXORANK_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_LEXORANK_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_LEXORANK_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_LEXORANK_ID_seq" OWNED BY "AO_60DB71_LEXORANK"."ID";


--
-- Name: AO_60DB71_NONWORKINGDAY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_NONWORKINGDAY" (
    "ID" bigint NOT NULL,
    "ISO8601_DATE" character varying(255) NOT NULL,
    "WORKING_DAYS_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_NONWORKINGDAY" OWNER TO jira;

--
-- Name: AO_60DB71_NONWORKINGDAY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_NONWORKINGDAY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_NONWORKINGDAY_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_NONWORKINGDAY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_NONWORKINGDAY_ID_seq" OWNED BY "AO_60DB71_NONWORKINGDAY"."ID";


--
-- Name: AO_60DB71_QUICKFILTER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_QUICKFILTER" (
    "DESCRIPTION" character varying(255),
    "ID" bigint NOT NULL,
    "LONG_QUERY" text,
    "NAME" character varying(255) NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "QUERY" character varying(255),
    "RAPID_VIEW_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_QUICKFILTER" OWNER TO jira;

--
-- Name: AO_60DB71_QUICKFILTER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_QUICKFILTER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_QUICKFILTER_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_QUICKFILTER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_QUICKFILTER_ID_seq" OWNED BY "AO_60DB71_QUICKFILTER"."ID";


--
-- Name: AO_60DB71_RANKABLEOBJECT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_RANKABLEOBJECT" (
    "ID" bigint NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_RANKABLEOBJECT" OWNER TO jira;

--
-- Name: AO_60DB71_RANKABLEOBJECT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_RANKABLEOBJECT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_RANKABLEOBJECT_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_RANKABLEOBJECT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_RANKABLEOBJECT_ID_seq" OWNED BY "AO_60DB71_RANKABLEOBJECT"."ID";


--
-- Name: AO_60DB71_RAPIDVIEW; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_RAPIDVIEW" (
    "CARD_COLOR_STRATEGY" character varying(255),
    "ID" bigint NOT NULL,
    "KAN_PLAN_ENABLED" boolean,
    "NAME" character varying(255) NOT NULL,
    "OWNER_USER_NAME" character varying(255) NOT NULL,
    "SAVED_FILTER_ID" bigint NOT NULL,
    "SHOW_DAYS_IN_COLUMN" boolean,
    "SPRINTS_ENABLED" boolean,
    "SWIMLANE_STRATEGY" character varying(255),
    "SHOW_EPIC_AS_PANEL" boolean,
    "PARENT_PROJECT_ID" bigint,
    "SIMPLE_BOARD" boolean,
    "USER_LOCATION_ID" bigint,
    CONSTRAINT chk_unique_container CHECK ((("PARENT_PROJECT_ID" IS NULL) OR ("USER_LOCATION_ID" IS NULL)))
);


ALTER TABLE "AO_60DB71_RAPIDVIEW" OWNER TO jira;

--
-- Name: AO_60DB71_RAPIDVIEW_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_RAPIDVIEW_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_RAPIDVIEW_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_RAPIDVIEW_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_RAPIDVIEW_ID_seq" OWNED BY "AO_60DB71_RAPIDVIEW"."ID";


--
-- Name: AO_60DB71_SPRINT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_SPRINT" (
    "CLOSED" boolean NOT NULL,
    "COMPLETE_DATE" bigint,
    "END_DATE" bigint,
    "GOAL" text,
    "ID" bigint NOT NULL,
    "NAME" character varying(255) NOT NULL,
    "RAPID_VIEW_ID" bigint,
    "SEQUENCE" bigint,
    "STARTED" boolean,
    "START_DATE" bigint
);


ALTER TABLE "AO_60DB71_SPRINT" OWNER TO jira;

--
-- Name: AO_60DB71_SPRINT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_SPRINT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_SPRINT_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_SPRINT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_SPRINT_ID_seq" OWNED BY "AO_60DB71_SPRINT"."ID";


--
-- Name: AO_60DB71_STATSFIELD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_STATSFIELD" (
    "ID" bigint NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "TYPE_ID" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_STATSFIELD" OWNER TO jira;

--
-- Name: AO_60DB71_STATSFIELD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_STATSFIELD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_STATSFIELD_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_STATSFIELD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_STATSFIELD_ID_seq" OWNED BY "AO_60DB71_STATSFIELD"."ID";


--
-- Name: AO_60DB71_SUBQUERY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_SUBQUERY" (
    "ID" bigint NOT NULL,
    "LONG_QUERY" text,
    "QUERY" character varying(255),
    "RAPID_VIEW_ID" bigint,
    "SECTION" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_SUBQUERY" OWNER TO jira;

--
-- Name: AO_60DB71_SUBQUERY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_SUBQUERY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_SUBQUERY_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_SUBQUERY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_SUBQUERY_ID_seq" OWNED BY "AO_60DB71_SUBQUERY"."ID";


--
-- Name: AO_60DB71_SWIMLANE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_SWIMLANE" (
    "DEFAULT_LANE" boolean NOT NULL,
    "DESCRIPTION" character varying(255),
    "ID" bigint NOT NULL,
    "LONG_QUERY" text,
    "NAME" character varying(255) NOT NULL,
    "POS" integer DEFAULT 0 NOT NULL,
    "QUERY" character varying(255),
    "RAPID_VIEW_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_SWIMLANE" OWNER TO jira;

--
-- Name: AO_60DB71_SWIMLANE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_SWIMLANE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_SWIMLANE_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_SWIMLANE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_SWIMLANE_ID_seq" OWNED BY "AO_60DB71_SWIMLANE"."ID";


--
-- Name: AO_60DB71_TRACKINGSTATISTIC; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_TRACKINGSTATISTIC" (
    "FIELD_ID" character varying(255),
    "ID" bigint NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "TYPE_ID" character varying(255) NOT NULL
);


ALTER TABLE "AO_60DB71_TRACKINGSTATISTIC" OWNER TO jira;

--
-- Name: AO_60DB71_TRACKINGSTATISTIC_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_TRACKINGSTATISTIC_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_TRACKINGSTATISTIC_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_TRACKINGSTATISTIC_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_TRACKINGSTATISTIC_ID_seq" OWNED BY "AO_60DB71_TRACKINGSTATISTIC"."ID";


--
-- Name: AO_60DB71_VERSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_VERSION" (
    "ID" bigint NOT NULL,
    "START_DATE" bigint,
    "VERSION_ID" bigint NOT NULL
);


ALTER TABLE "AO_60DB71_VERSION" OWNER TO jira;

--
-- Name: AO_60DB71_VERSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_VERSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_VERSION_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_VERSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_VERSION_ID_seq" OWNED BY "AO_60DB71_VERSION"."ID";


--
-- Name: AO_60DB71_WORKINGDAYS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_60DB71_WORKINGDAYS" (
    "FRIDAY" boolean NOT NULL,
    "ID" bigint NOT NULL,
    "MONDAY" boolean NOT NULL,
    "RAPID_VIEW_ID" bigint NOT NULL,
    "SATURDAY" boolean NOT NULL,
    "SUNDAY" boolean NOT NULL,
    "THURSDAY" boolean NOT NULL,
    "TIMEZONE" character varying(255) NOT NULL,
    "TUESDAY" boolean NOT NULL,
    "WEDNESDAY" boolean NOT NULL
);


ALTER TABLE "AO_60DB71_WORKINGDAYS" OWNER TO jira;

--
-- Name: AO_60DB71_WORKINGDAYS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_60DB71_WORKINGDAYS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_60DB71_WORKINGDAYS_ID_seq" OWNER TO jira;

--
-- Name: AO_60DB71_WORKINGDAYS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_60DB71_WORKINGDAYS_ID_seq" OWNED BY "AO_60DB71_WORKINGDAYS"."ID";


--
-- Name: AO_68DACE_CONNECT_APPLICATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_68DACE_CONNECT_APPLICATION" (
    "ADDON_KEY" character varying(255),
    "APPLICATION_ID" character varying(255) NOT NULL,
    "DESCRIPTOR" text,
    "ID" integer NOT NULL,
    "REMOTE_APPLICATION" character varying(255)
);


ALTER TABLE "AO_68DACE_CONNECT_APPLICATION" OWNER TO jira;

--
-- Name: AO_68DACE_CONNECT_APPLICATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_68DACE_CONNECT_APPLICATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_68DACE_CONNECT_APPLICATION_ID_seq" OWNER TO jira;

--
-- Name: AO_68DACE_CONNECT_APPLICATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_68DACE_CONNECT_APPLICATION_ID_seq" OWNED BY "AO_68DACE_CONNECT_APPLICATION"."ID";


--
-- Name: AO_68DACE_INSTALLATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_68DACE_INSTALLATION" (
    "CLIENT_KEY" character varying(255) NOT NULL,
    "CONNECT_APPLICATION_ID" integer,
    "ID" integer NOT NULL,
    "INSTALL_PAYLOAD" text,
    "LIFE_CYCLE_STAGE" character varying(255),
    "PRINCIPAL_UUID" character varying(255),
    "UNINSTALL_PAYLOAD" text
);


ALTER TABLE "AO_68DACE_INSTALLATION" OWNER TO jira;

--
-- Name: AO_68DACE_INSTALLATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_68DACE_INSTALLATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_68DACE_INSTALLATION_ID_seq" OWNER TO jira;

--
-- Name: AO_68DACE_INSTALLATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_68DACE_INSTALLATION_ID_seq" OWNED BY "AO_68DACE_INSTALLATION"."ID";


--
-- Name: AO_7A2604_CALENDAR; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_7A2604_CALENDAR" (
    "CONTEXT" text NOT NULL,
    "ID" integer NOT NULL,
    "NAME" character varying(63) NOT NULL,
    "TIMEZONE" character varying(63) NOT NULL
);


ALTER TABLE "AO_7A2604_CALENDAR" OWNER TO jira;

--
-- Name: AO_7A2604_CALENDAR_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_7A2604_CALENDAR_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_7A2604_CALENDAR_ID_seq" OWNER TO jira;

--
-- Name: AO_7A2604_CALENDAR_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_7A2604_CALENDAR_ID_seq" OWNED BY "AO_7A2604_CALENDAR"."ID";


--
-- Name: AO_7A2604_HOLIDAY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_7A2604_HOLIDAY" (
    "CALENDAR_ID" integer,
    "DATE_STRING" character varying(63) NOT NULL,
    "ID" integer NOT NULL,
    "NAME" character varying(63) NOT NULL,
    "RECURRING" boolean
);


ALTER TABLE "AO_7A2604_HOLIDAY" OWNER TO jira;

--
-- Name: AO_7A2604_HOLIDAY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_7A2604_HOLIDAY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_7A2604_HOLIDAY_ID_seq" OWNER TO jira;

--
-- Name: AO_7A2604_HOLIDAY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_7A2604_HOLIDAY_ID_seq" OWNED BY "AO_7A2604_HOLIDAY"."ID";


--
-- Name: AO_7A2604_WORKINGTIME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_7A2604_WORKINGTIME" (
    "CALENDAR_ID" integer,
    "DAY" character varying(63) NOT NULL,
    "END_TIME" bigint NOT NULL,
    "ID" integer NOT NULL,
    "START_TIME" bigint NOT NULL
);


ALTER TABLE "AO_7A2604_WORKINGTIME" OWNER TO jira;

--
-- Name: AO_7A2604_WORKINGTIME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_7A2604_WORKINGTIME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_7A2604_WORKINGTIME_ID_seq" OWNER TO jira;

--
-- Name: AO_7A2604_WORKINGTIME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_7A2604_WORKINGTIME_ID_seq" OWNED BY "AO_7A2604_WORKINGTIME"."ID";


--
-- Name: AO_82B313_ABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_ABILITY" (
    "COMBINED_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "PERSON_ID" bigint,
    "SKILL_ID" bigint
);


ALTER TABLE "AO_82B313_ABILITY" OWNER TO jira;

--
-- Name: AO_82B313_ABILITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_ABILITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_ABILITY_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_ABILITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_ABILITY_ID_seq" OWNED BY "AO_82B313_ABILITY"."ID";


--
-- Name: AO_82B313_ABSENCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_ABSENCE" (
    "END" bigint,
    "ID" bigint NOT NULL,
    "PERSON_ID" bigint,
    "START" bigint,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_82B313_ABSENCE" OWNER TO jira;

--
-- Name: AO_82B313_ABSENCE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_ABSENCE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_ABSENCE_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_ABSENCE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_ABSENCE_ID_seq" OWNED BY "AO_82B313_ABSENCE"."ID";


--
-- Name: AO_82B313_AVAILABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_AVAILABILITY" (
    "END" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "RESOURCE_ID" bigint,
    "START" bigint NOT NULL,
    "TITLE" character varying(255),
    "WEEKLY_HOURS" double precision
);


ALTER TABLE "AO_82B313_AVAILABILITY" OWNER TO jira;

--
-- Name: AO_82B313_AVAILABILITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_AVAILABILITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_AVAILABILITY_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_AVAILABILITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_AVAILABILITY_ID_seq" OWNED BY "AO_82B313_AVAILABILITY"."ID";


--
-- Name: AO_82B313_INIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_INIT" (
    "ID" bigint NOT NULL,
    "KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_82B313_INIT" OWNER TO jira;

--
-- Name: AO_82B313_INIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_INIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_INIT_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_INIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_INIT_ID_seq" OWNED BY "AO_82B313_INIT"."ID";


--
-- Name: AO_82B313_PERSON; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_PERSON" (
    "AVATAR_URL" character varying(255),
    "END" bigint,
    "ID" bigint NOT NULL,
    "JIRA_USER_ID" character varying(255),
    "START" bigint,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_82B313_PERSON" OWNER TO jira;

--
-- Name: AO_82B313_PERSON_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_PERSON_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_PERSON_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_PERSON_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_PERSON_ID_seq" OWNED BY "AO_82B313_PERSON"."ID";


--
-- Name: AO_82B313_RESOURCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_RESOURCE" (
    "ID" bigint NOT NULL,
    "JOIN_DATE" bigint,
    "LEAVE_DATE" bigint,
    "PERSON_ID" bigint,
    "TEAM_ID" bigint,
    "WEEKLY_HOURS" double precision DEFAULT 0.0
);


ALTER TABLE "AO_82B313_RESOURCE" OWNER TO jira;

--
-- Name: AO_82B313_RESOURCE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_RESOURCE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_RESOURCE_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_RESOURCE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_RESOURCE_ID_seq" OWNED BY "AO_82B313_RESOURCE"."ID";


--
-- Name: AO_82B313_SKILL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_SKILL" (
    "ID" bigint NOT NULL,
    "SHAREABLE" boolean,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_82B313_SKILL" OWNER TO jira;

--
-- Name: AO_82B313_SKILL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_SKILL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_SKILL_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_SKILL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_SKILL_ID_seq" OWNED BY "AO_82B313_SKILL"."ID";


--
-- Name: AO_82B313_TEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_82B313_TEAM" (
    "AVATAR_URL" character varying(255),
    "ID" bigint NOT NULL,
    "SHAREABLE" boolean,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_82B313_TEAM" OWNER TO jira;

--
-- Name: AO_82B313_TEAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_82B313_TEAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_82B313_TEAM_ID_seq" OWNER TO jira;

--
-- Name: AO_82B313_TEAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_82B313_TEAM_ID_seq" OWNED BY "AO_82B313_TEAM"."ID";


--
-- Name: AO_86ED1B_GRACE_PERIOD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_GRACE_PERIOD" (
    "FROM_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "OPEN_UNTIL" timestamp without time zone,
    "RECEIVER" character varying(255) NOT NULL,
    "TO_DATE" timestamp without time zone
);


ALTER TABLE "AO_86ED1B_GRACE_PERIOD" OWNER TO jira;

--
-- Name: AO_86ED1B_GRACE_PERIOD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_GRACE_PERIOD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_GRACE_PERIOD_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_GRACE_PERIOD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_GRACE_PERIOD_ID_seq" OWNED BY "AO_86ED1B_GRACE_PERIOD"."ID";


--
-- Name: AO_86ED1B_PROJECT_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_PROJECT_CONFIG" (
    "ID" integer NOT NULL,
    "PLANNING_COLOUR" character varying(255),
    "PROJECT_ID" bigint DEFAULT 0 NOT NULL
);


ALTER TABLE "AO_86ED1B_PROJECT_CONFIG" OWNER TO jira;

--
-- Name: AO_86ED1B_PROJECT_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_PROJECT_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_PROJECT_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_PROJECT_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_PROJECT_CONFIG_ID_seq" OWNED BY "AO_86ED1B_PROJECT_CONFIG"."ID";


--
-- Name: AO_86ED1B_STREAMS_ENTRY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_STREAMS_ENTRY" (
    "ACTOR" character varying(255),
    "ENTRY_CLASS" character varying(255),
    "ENTRY_TYPE" character varying(255),
    "ID" integer NOT NULL,
    "POSTED_DATE" timestamp without time zone,
    "RAW_PARAMS" character varying(450),
    "RAW_PARAMS_LONG" text,
    "RECEIVER" character varying(255),
    "RAW_PARAMS_JSON" text
);


ALTER TABLE "AO_86ED1B_STREAMS_ENTRY" OWNER TO jira;

--
-- Name: AO_86ED1B_STREAMS_ENTRY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_STREAMS_ENTRY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_STREAMS_ENTRY_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_STREAMS_ENTRY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_STREAMS_ENTRY_ID_seq" OWNED BY "AO_86ED1B_STREAMS_ENTRY"."ID";


--
-- Name: AO_86ED1B_TIMEPLAN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_TIMEPLAN" (
    "COLLABORATOR" character varying(255),
    "COMMENT" character varying(255),
    "FROM_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "PLAN_ID" bigint DEFAULT 0,
    "PLAN_TYPE" character varying(255),
    "SECONDS" bigint DEFAULT 0,
    "TIME_TYPE" character varying(255),
    "TO_DATE" timestamp without time zone,
    "WORKFLOW_STATUS" character varying(255),
    "DESCRIPTION" text,
    "REQUESTER" character varying(255),
    "REVIEWER" character varying(255)
);


ALTER TABLE "AO_86ED1B_TIMEPLAN" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMEPLAN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_TIMEPLAN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_TIMEPLAN_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMEPLAN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_TIMEPLAN_ID_seq" OWNED BY "AO_86ED1B_TIMEPLAN"."ID";


--
-- Name: AO_86ED1B_TIMESHEET_APPROVAL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_TIMESHEET_APPROVAL" (
    "ACTION" character varying(255) NOT NULL,
    "ACTOR_KEY" character varying(255) NOT NULL,
    "DATE_CREATED" timestamp without time zone NOT NULL,
    "DATE_FROM" timestamp without time zone NOT NULL,
    "DATE_TO" timestamp without time zone NOT NULL,
    "ID" integer NOT NULL,
    "PERIOD" character varying(255) NOT NULL,
    "PERIOD_TYPE" character varying(255) NOT NULL,
    "PERIOD_VIEW" character varying(255) NOT NULL,
    "REASON" text,
    "REQUIRED_TIME" bigint NOT NULL,
    "REVIEWER_KEY" character varying(255),
    "STATUS" character varying(255) NOT NULL,
    "SUBMITTED_TIME" bigint NOT NULL,
    "USER_KEY" character varying(255) NOT NULL,
    "NUMBER_OF_INVALID_WORKLOGS" integer
);


ALTER TABLE "AO_86ED1B_TIMESHEET_APPROVAL" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMESHEET_APPROVAL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_TIMESHEET_APPROVAL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_TIMESHEET_APPROVAL_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMESHEET_APPROVAL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_TIMESHEET_APPROVAL_ID_seq" OWNED BY "AO_86ED1B_TIMESHEET_APPROVAL"."ID";


--
-- Name: AO_86ED1B_TIMESHEET_STATUS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_86ED1B_TIMESHEET_STATUS" (
    "ACTOR" character varying(255),
    "DATE_CHANGED" timestamp without time zone,
    "DATE_CREATED" timestamp without time zone,
    "DATE_FROM" timestamp without time zone,
    "DATE_TO" timestamp without time zone,
    "ID" integer NOT NULL,
    "PERIOD" character varying(255),
    "PERIOD_TYPE" character varying(255),
    "PERIOD_VIEW" character varying(255),
    "REVIEWER_USER" character varying(255),
    "STATUS" character varying(255),
    "USERNAME" character varying(255),
    "REASON" character varying(255)
);


ALTER TABLE "AO_86ED1B_TIMESHEET_STATUS" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMESHEET_STATUS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_86ED1B_TIMESHEET_STATUS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_86ED1B_TIMESHEET_STATUS_ID_seq" OWNER TO jira;

--
-- Name: AO_86ED1B_TIMESHEET_STATUS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_86ED1B_TIMESHEET_STATUS_ID_seq" OWNED BY "AO_86ED1B_TIMESHEET_STATUS"."ID";


--
-- Name: AO_88DE6A_AGREEMENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_AGREEMENT" (
    "ACCEPTED_DATE" timestamp without time zone,
    "ID" integer NOT NULL,
    "VERSION" character varying(255)
);


ALTER TABLE "AO_88DE6A_AGREEMENT" OWNER TO jira;

--
-- Name: AO_88DE6A_AGREEMENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_AGREEMENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_AGREEMENT_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_AGREEMENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_AGREEMENT_ID_seq" OWNED BY "AO_88DE6A_AGREEMENT"."ID";


--
-- Name: AO_88DE6A_CONNECTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_CONNECTION" (
    "APP_LINK_ID" character varying(255),
    "CACHE_EXPIRY_ENABLED" boolean DEFAULT false,
    "CACHE_TTL" bigint DEFAULT 1800000,
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "SYSTEM_TYPE_ID" character varying(255),
    "VISIBLE_ASSOCIATE_OPTION" boolean DEFAULT true,
    "VISIBLE_ASSOCIATE_PULL_OPTION" boolean DEFAULT true,
    "VISIBLE_ASSOCIATE_PUSH_OPTION" boolean DEFAULT true
);


ALTER TABLE "AO_88DE6A_CONNECTION" OWNER TO jira;

--
-- Name: AO_88DE6A_CONNECTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_CONNECTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_CONNECTION_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_CONNECTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_CONNECTION_ID_seq" OWNED BY "AO_88DE6A_CONNECTION"."ID";


--
-- Name: AO_88DE6A_LICENSE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_LICENSE" (
    "CONTENT" text,
    "ID" integer NOT NULL,
    "SYSTEM_TYPE_ID" character varying(255)
);


ALTER TABLE "AO_88DE6A_LICENSE" OWNER TO jira;

--
-- Name: AO_88DE6A_LICENSE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_LICENSE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_LICENSE_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_LICENSE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_LICENSE_ID_seq" OWNED BY "AO_88DE6A_LICENSE"."ID";


--
-- Name: AO_88DE6A_MAPPING_BEAN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_MAPPING_BEAN" (
    "FROM_OBJECT_TYPE" character varying(255),
    "FROM_SYSTEM_ID" character varying(255),
    "ID" integer NOT NULL,
    "LABEL" character varying(255),
    "TO_OBJECT_TYPE" character varying(255),
    "TO_SYSTEM_ID" character varying(255)
);


ALTER TABLE "AO_88DE6A_MAPPING_BEAN" OWNER TO jira;

--
-- Name: AO_88DE6A_MAPPING_BEAN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_MAPPING_BEAN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_MAPPING_BEAN_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_MAPPING_BEAN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_MAPPING_BEAN_ID_seq" OWNED BY "AO_88DE6A_MAPPING_BEAN"."ID";


--
-- Name: AO_88DE6A_MAPPING_ENTRY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_MAPPING_ENTRY" (
    "DEFAULT_VALUE" character varying(255),
    "FROM_FIELD" character varying(255),
    "ID" integer NOT NULL,
    "JSONTRANSFORMER_ATTRIBUTES" text,
    "MAPPING_BEAN_ID" integer,
    "TO_FIELD" character varying(255),
    "TRANSFORMER" character varying(255),
    "TYPE" character varying(255)
);


ALTER TABLE "AO_88DE6A_MAPPING_ENTRY" OWNER TO jira;

--
-- Name: AO_88DE6A_MAPPING_ENTRY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_MAPPING_ENTRY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_MAPPING_ENTRY_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_MAPPING_ENTRY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_MAPPING_ENTRY_ID_seq" OWNED BY "AO_88DE6A_MAPPING_ENTRY"."ID";


--
-- Name: AO_88DE6A_TRANSACTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_TRANSACTION" (
    "ACTION" character varying(255),
    "ID" integer NOT NULL,
    "LOCAL_OBJECT_ID" character varying(255),
    "REMOTE_OBJECT_ID" character varying(255),
    "REMOTE_SYSTEM_ID" character varying(255),
    "REMOTE_SYSTEM_TYPE" character varying(255),
    "RESULT" character varying(255),
    "STATUS" character varying(255),
    "TIME" timestamp without time zone,
    "USER" character varying(255)
);


ALTER TABLE "AO_88DE6A_TRANSACTION" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_CONTENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_TRANSACTION_CONTENT" (
    "CONTENT" text,
    "ID" integer NOT NULL,
    "TRANSACTION_ID" integer
);


ALTER TABLE "AO_88DE6A_TRANSACTION_CONTENT" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_CONTENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_TRANSACTION_CONTENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_TRANSACTION_CONTENT_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_CONTENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_TRANSACTION_CONTENT_ID_seq" OWNED BY "AO_88DE6A_TRANSACTION_CONTENT"."ID";


--
-- Name: AO_88DE6A_TRANSACTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_TRANSACTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_TRANSACTION_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_TRANSACTION_ID_seq" OWNED BY "AO_88DE6A_TRANSACTION"."ID";


--
-- Name: AO_88DE6A_TRANSACTION_LOG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_88DE6A_TRANSACTION_LOG" (
    "ID" integer NOT NULL,
    "LEVEL" character varying(255),
    "MESSAGE" text,
    "TIME" timestamp without time zone,
    "TRANSACTION_ID" integer
);


ALTER TABLE "AO_88DE6A_TRANSACTION_LOG" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_LOG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_88DE6A_TRANSACTION_LOG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_88DE6A_TRANSACTION_LOG_ID_seq" OWNER TO jira;

--
-- Name: AO_88DE6A_TRANSACTION_LOG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_88DE6A_TRANSACTION_LOG_ID_seq" OWNED BY "AO_88DE6A_TRANSACTION_LOG"."ID";


--
-- Name: AO_9B2E3B_EXEC_RULE_MSG_ITEM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_EXEC_RULE_MSG_ITEM" (
    "ID" bigint NOT NULL,
    "RULE_EXECUTION_ID" bigint NOT NULL,
    "RULE_MESSAGE_KEY" character varying(127) NOT NULL,
    "RULE_MESSAGE_VALUE" text
);


ALTER TABLE "AO_9B2E3B_EXEC_RULE_MSG_ITEM" OWNER TO jira;

--
-- Name: AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq" OWNED BY "AO_9B2E3B_EXEC_RULE_MSG_ITEM"."ID";


--
-- Name: AO_9B2E3B_IF_CONDITION_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_CONDITION_CONFIG" (
    "ID" bigint NOT NULL,
    "IF_THEN_ID" bigint NOT NULL,
    "MODULE_KEY" character varying(450) NOT NULL,
    "ORDINAL" integer NOT NULL
);


ALTER TABLE "AO_9B2E3B_IF_CONDITION_CONFIG" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq" OWNED BY "AO_9B2E3B_IF_CONDITION_CONFIG"."ID";


--
-- Name: AO_9B2E3B_IF_COND_CONF_DATA; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_COND_CONF_DATA" (
    "CONFIG_DATA_KEY" character varying(127) NOT NULL,
    "CONFIG_DATA_VALUE" text,
    "ID" bigint NOT NULL,
    "IF_CONDITION_CONFIG_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_IF_COND_CONF_DATA" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_COND_CONF_DATA_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_COND_CONF_DATA_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_COND_CONF_DATA_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_COND_CONF_DATA_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_COND_CONF_DATA_ID_seq" OWNED BY "AO_9B2E3B_IF_COND_CONF_DATA"."ID";


--
-- Name: AO_9B2E3B_IF_COND_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_COND_EXECUTION" (
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "IF_CONDITION_CONFIG_ID" bigint NOT NULL,
    "IF_EXECUTION_ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "START_TIME_MILLIS" bigint
);


ALTER TABLE "AO_9B2E3B_IF_COND_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_COND_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_COND_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_COND_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_COND_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_COND_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_IF_COND_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_IF_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_EXECUTION" (
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "IF_THEN_EXECUTION_ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "START_TIME_MILLIS" bigint
);


ALTER TABLE "AO_9B2E3B_IF_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_IF_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_IF_THEN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_THEN" (
    "ID" bigint NOT NULL,
    "ORDINAL" integer NOT NULL,
    "RULE_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_IF_THEN" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_THEN_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_IF_THEN_EXECUTION" (
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "IF_THEN_ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "RULE_EXECUTION_ID" bigint NOT NULL,
    "START_TIME_MILLIS" bigint
);


ALTER TABLE "AO_9B2E3B_IF_THEN_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_THEN_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_THEN_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_THEN_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_THEN_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_THEN_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_IF_THEN_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_IF_THEN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_IF_THEN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_IF_THEN_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_IF_THEN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_IF_THEN_ID_seq" OWNED BY "AO_9B2E3B_IF_THEN"."ID";


--
-- Name: AO_9B2E3B_PROJECT_USER_CONTEXT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_PROJECT_USER_CONTEXT" (
    "ID" bigint NOT NULL,
    "PROJECT_ID" bigint NOT NULL,
    "STRATEGY" character varying(127) NOT NULL,
    "USER_KEY" character varying(255)
);


ALTER TABLE "AO_9B2E3B_PROJECT_USER_CONTEXT" OWNER TO jira;

--
-- Name: AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq" OWNED BY "AO_9B2E3B_PROJECT_USER_CONTEXT"."ID";


--
-- Name: AO_9B2E3B_RSETREV_PROJ_CONTEXT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RSETREV_PROJ_CONTEXT" (
    "ID" bigint NOT NULL,
    "PROJECT_ID" bigint NOT NULL,
    "RULESET_REVISION_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_RSETREV_PROJ_CONTEXT" OWNER TO jira;

--
-- Name: AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq" OWNED BY "AO_9B2E3B_RSETREV_PROJ_CONTEXT"."ID";


--
-- Name: AO_9B2E3B_RSETREV_USER_CONTEXT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RSETREV_USER_CONTEXT" (
    "ID" bigint NOT NULL,
    "RULESET_REVISION_ID" bigint NOT NULL,
    "STRATEGY" character varying(127) NOT NULL,
    "USER_KEY" character varying(255)
);


ALTER TABLE "AO_9B2E3B_RSETREV_USER_CONTEXT" OWNER TO jira;

--
-- Name: AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq" OWNED BY "AO_9B2E3B_RSETREV_USER_CONTEXT"."ID";


--
-- Name: AO_9B2E3B_RULE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RULE" (
    "ENABLED" boolean DEFAULT true NOT NULL,
    "ID" bigint NOT NULL,
    "ORDINAL" integer NOT NULL,
    "RULESET_REVISION_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_RULE" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULESET; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RULESET" (
    "ACTIVE_REVISION_ID" bigint,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_RULESET" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULESET_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RULESET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RULESET_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULESET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RULESET_ID_seq" OWNED BY "AO_9B2E3B_RULESET"."ID";


--
-- Name: AO_9B2E3B_RULESET_REVISION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RULESET_REVISION" (
    "CREATED_BY" character varying(127) NOT NULL,
    "CREATED_TIMESTAMP_MILLIS" bigint,
    "DESCRIPTION" character varying(450),
    "ID" bigint NOT NULL,
    "IS_SYSTEM_RULE_SET" boolean DEFAULT false NOT NULL,
    "NAME" character varying(127) NOT NULL,
    "RULE_SET_ID" bigint NOT NULL,
    "TRIGGER_FROM_OTHER_RULES" boolean
);


ALTER TABLE "AO_9B2E3B_RULESET_REVISION" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULESET_REVISION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RULESET_REVISION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RULESET_REVISION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULESET_REVISION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RULESET_REVISION_ID_seq" OWNED BY "AO_9B2E3B_RULESET_REVISION"."ID";


--
-- Name: AO_9B2E3B_RULE_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_RULE_EXECUTION" (
    "EXECUTOR_USER_KEY" character varying(127),
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "RULE_ID" bigint NOT NULL,
    "START_TIME_MILLIS" bigint
);


ALTER TABLE "AO_9B2E3B_RULE_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULE_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RULE_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RULE_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULE_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RULE_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_RULE_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_RULE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_RULE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_RULE_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_RULE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_RULE_ID_seq" OWNED BY "AO_9B2E3B_RULE"."ID";


--
-- Name: AO_9B2E3B_THEN_ACTION_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_THEN_ACTION_CONFIG" (
    "ID" bigint NOT NULL,
    "IF_THEN_ID" bigint NOT NULL,
    "MODULE_KEY" character varying(450) NOT NULL,
    "ORDINAL" integer NOT NULL
);


ALTER TABLE "AO_9B2E3B_THEN_ACTION_CONFIG" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq" OWNED BY "AO_9B2E3B_THEN_ACTION_CONFIG"."ID";


--
-- Name: AO_9B2E3B_THEN_ACT_CONF_DATA; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_THEN_ACT_CONF_DATA" (
    "CONFIG_DATA_KEY" character varying(127) NOT NULL,
    "CONFIG_DATA_VALUE" text,
    "ID" bigint NOT NULL,
    "THEN_ACTION_CONFIG_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_THEN_ACT_CONF_DATA" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq" OWNED BY "AO_9B2E3B_THEN_ACT_CONF_DATA"."ID";


--
-- Name: AO_9B2E3B_THEN_ACT_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_THEN_ACT_EXECUTION" (
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "START_TIME_MILLIS" bigint,
    "THEN_ACTION_CONFIG_ID" bigint NOT NULL,
    "THEN_EXECUTION_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_THEN_ACT_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_THEN_ACT_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_THEN_EXECUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_THEN_EXECUTION" (
    "FINISH_TIME_MILLIS" bigint,
    "ID" bigint NOT NULL,
    "IF_THEN_EXECUTION_ID" bigint NOT NULL,
    "MESSAGE" text,
    "OUTCOME" character varying(127) NOT NULL,
    "START_TIME_MILLIS" bigint
);


ALTER TABLE "AO_9B2E3B_THEN_EXECUTION" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_EXECUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_THEN_EXECUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_THEN_EXECUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_THEN_EXECUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_THEN_EXECUTION_ID_seq" OWNED BY "AO_9B2E3B_THEN_EXECUTION"."ID";


--
-- Name: AO_9B2E3B_WHEN_HANDLER_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_WHEN_HANDLER_CONFIG" (
    "ID" bigint NOT NULL,
    "MODULE_KEY" character varying(450) NOT NULL,
    "ORDINAL" integer NOT NULL,
    "RULE_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_WHEN_HANDLER_CONFIG" OWNER TO jira;

--
-- Name: AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq" OWNED BY "AO_9B2E3B_WHEN_HANDLER_CONFIG"."ID";


--
-- Name: AO_9B2E3B_WHEN_HAND_CONF_DATA; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_9B2E3B_WHEN_HAND_CONF_DATA" (
    "CONFIG_DATA_KEY" character varying(127) NOT NULL,
    "CONFIG_DATA_VALUE" text,
    "ID" bigint NOT NULL,
    "WHEN_HANDLER_CONFIG_ID" bigint NOT NULL
);


ALTER TABLE "AO_9B2E3B_WHEN_HAND_CONF_DATA" OWNER TO jira;

--
-- Name: AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq" OWNER TO jira;

--
-- Name: AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq" OWNED BY "AO_9B2E3B_WHEN_HAND_CONF_DATA"."ID";


--
-- Name: ao_a0b856_web_hook_listener_ao; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE ao_a0b856_web_hook_listener_ao (
    description text,
    enabled boolean,
    events text,
    exclude_body boolean,
    filters text,
    id integer NOT NULL,
    last_updated timestamp with time zone NOT NULL,
    last_updated_user character varying(255),
    name text NOT NULL,
    parameters text,
    registration_method character varying(255) NOT NULL,
    url text NOT NULL
);


ALTER TABLE ao_a0b856_web_hook_listener_ao OWNER TO jira;

--
-- Name: AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq" OWNER TO jira;

--
-- Name: AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq" OWNED BY ao_a0b856_web_hook_listener_ao.id;


--
-- Name: AO_A415DF_AOABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOABILITY" (
    "ABILITY_VALUE" double precision,
    "AOPERSON_ID" integer,
    "ID_OTHER" integer NOT NULL,
    "TARGET_ID" integer,
    "TARGET_TYPE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOABILITY" OWNER TO jira;

--
-- Name: AO_A415DF_AOABILITY_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOABILITY_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOABILITY_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOABILITY_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOABILITY_ID_OTHER_seq" OWNED BY "AO_A415DF_AOABILITY"."ID_OTHER";


--
-- Name: AO_A415DF_AOABSENCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOABSENCE" (
    "AOPERSON_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "END_DATE" bigint,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "START_DATE" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOABSENCE" OWNER TO jira;

--
-- Name: AO_A415DF_AOABSENCE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOABSENCE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOABSENCE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOABSENCE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOABSENCE_ID_OTHER_seq" OWNED BY "AO_A415DF_AOABSENCE"."ID_OTHER";


--
-- Name: AO_A415DF_AOAVAILABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOAVAILABILITY" (
    "AORESOURCE_ID" integer,
    "AVAILABILITY" double precision,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "END_DATE" bigint,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "START_DATE" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOAVAILABILITY" OWNER TO jira;

--
-- Name: AO_A415DF_AOAVAILABILITY_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOAVAILABILITY_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOAVAILABILITY_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOAVAILABILITY_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOAVAILABILITY_ID_OTHER_seq" OWNED BY "AO_A415DF_AOAVAILABILITY"."ID_OTHER";


--
-- Name: AO_A415DF_AOCONFIGURATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOCONFIGURATION" (
    "ID_OTHER" integer NOT NULL,
    "INITIALIZED" boolean,
    "INIT_STATE" integer
);


ALTER TABLE "AO_A415DF_AOCONFIGURATION" OWNER TO jira;

--
-- Name: AO_A415DF_AOCONFIGURATION_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOCONFIGURATION_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOCONFIGURATION_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOCONFIGURATION_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOCONFIGURATION_ID_OTHER_seq" OWNED BY "AO_A415DF_AOCONFIGURATION"."ID_OTHER";


--
-- Name: AO_A415DF_AOCUSTOM_WORDING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOCUSTOM_WORDING" (
    "ID_OTHER" integer NOT NULL,
    "WORD_KEY" character varying(255),
    "WORD_VALUE" character varying(255)
);


ALTER TABLE "AO_A415DF_AOCUSTOM_WORDING" OWNER TO jira;

--
-- Name: AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq" OWNED BY "AO_A415DF_AOCUSTOM_WORDING"."ID_OTHER";


--
-- Name: AO_A415DF_AODEPENDENCY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AODEPENDENCY" (
    "DEPENDEE" integer,
    "DEPENDENT" integer,
    "ID_OTHER" integer NOT NULL
);


ALTER TABLE "AO_A415DF_AODEPENDENCY" OWNER TO jira;

--
-- Name: AO_A415DF_AODEPENDENCY_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AODEPENDENCY_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AODEPENDENCY_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AODEPENDENCY_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AODEPENDENCY_ID_OTHER_seq" OWNED BY "AO_A415DF_AODEPENDENCY"."ID_OTHER";


--
-- Name: AO_A415DF_AODOOR_STOP; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AODOOR_STOP" (
    "ID" integer NOT NULL
);


ALTER TABLE "AO_A415DF_AODOOR_STOP" OWNER TO jira;

--
-- Name: AO_A415DF_AODOOR_STOP_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AODOOR_STOP_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AODOOR_STOP_ID_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AODOOR_STOP_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AODOOR_STOP_ID_seq" OWNED BY "AO_A415DF_AODOOR_STOP"."ID";


--
-- Name: AO_A415DF_AOESTIMATE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOESTIMATE" (
    "AOWORK_ITEM_ID" integer,
    "CURRENCY" integer,
    "ESTIMATE" double precision,
    "ID_OTHER" integer NOT NULL,
    "ORIGINAL" boolean,
    "REPLANNING" boolean,
    "TARGET_ID" integer,
    "TARGET_TYPE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOESTIMATE" OWNER TO jira;

--
-- Name: AO_A415DF_AOESTIMATE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOESTIMATE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOESTIMATE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOESTIMATE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOESTIMATE_ID_OTHER_seq" OWNED BY "AO_A415DF_AOESTIMATE"."ID_OTHER";


--
-- Name: AO_A415DF_AOEXTENSION_LINK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOEXTENSION_LINK" (
    "AOEXTENDABLE_ID" integer,
    "AOEXTENDABLE_TYPE" character varying(127),
    "EXTENSION_KEY" character varying(255),
    "EXTENSION_LINK" character varying(255),
    "ID_OTHER" integer NOT NULL,
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOEXTENSION_LINK" OWNER TO jira;

--
-- Name: AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq" OWNED BY "AO_A415DF_AOEXTENSION_LINK"."ID_OTHER";


--
-- Name: AO_A415DF_AONON_WORKING_DAYS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AONON_WORKING_DAYS" (
    "AOPLAN_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "END_DATE" bigint,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "START_DATE" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AONON_WORKING_DAYS" OWNER TO jira;

--
-- Name: AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq" OWNED BY "AO_A415DF_AONON_WORKING_DAYS"."ID_OTHER";


--
-- Name: AO_A415DF_AOPERMISSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOPERMISSION" (
    "HOLDER_ID" character varying(255),
    "HOLDER_TYPE" character varying(255),
    "ID_OTHER" integer NOT NULL,
    "PERMISSION" integer,
    "TARGET_ID" character varying(255),
    "TARGET_TYPE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOPERMISSION" OWNER TO jira;

--
-- Name: AO_A415DF_AOPERMISSION_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOPERMISSION_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOPERMISSION_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOPERMISSION_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOPERMISSION_ID_OTHER_seq" OWNED BY "AO_A415DF_AOPERMISSION"."ID_OTHER";


--
-- Name: AO_A415DF_AOPERSON; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOPERSON" (
    "AOEXTERNAL_ID" character varying(255),
    "AOPLAN_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "EXTERNAL" boolean,
    "ID_OTHER" integer NOT NULL,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOPERSON" OWNER TO jira;

--
-- Name: AO_A415DF_AOPERSON_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOPERSON_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOPERSON_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOPERSON_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOPERSON_ID_OTHER_seq" OWNED BY "AO_A415DF_AOPERSON"."ID_OTHER";


--
-- Name: AO_A415DF_AOPLAN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOPLAN" (
    "AODATE" bigint,
    "AOREPLANNING_DATE" bigint,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "IN_REPLANNING" boolean,
    "IN_STREAM_MODE" boolean,
    "PLAN_VERSION" bigint,
    "REPLANNING_VERSION" bigint,
    "SCHEDULING_VERSION" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOPLAN" OWNER TO jira;

--
-- Name: AO_A415DF_AOPLAN_CONFIGURATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOPLAN_CONFIGURATION" (
    "AOPLAN_ID" integer,
    "AOPROGRESS_TRACKER_TYPE" character varying(255),
    "AOWEEKDAY_CONFIG" integer,
    "BACKLOG_RECORD_LIMIT" integer,
    "DEFAULT_EPIC_ESTIMATE" double precision,
    "DEFAULT_STORY_ESTIMATE" double precision,
    "EPIC_SYNC_MODE" character varying(255),
    "GLOBAL_DEFAULT_VELOCITY" double precision,
    "GLOBAL_SPRINT_LENGTH" integer,
    "HAS_SPRINT_CONSTRAINT" boolean,
    "HEURISTIC_THRESHOLD" integer,
    "HOURS_PER_DAY" double precision,
    "ID_OTHER" integer NOT NULL,
    "IMPORT_LIMIT" integer,
    "INITIATIVE_SYNC_MODE" character varying(255),
    "LINKING_MODE" character varying(255),
    "MAX_RESOURCES_PER_STORY" bigint,
    "MIN_LOAD_UNSTR_EPICS" bigint,
    "PLANNING_UNIT" character varying(255),
    "PROG_CMPLT_IF_RSLVD" boolean,
    "PROG_DSPL_UNEST_RTIO" boolean,
    "PROG_FIELD_NAME" character varying(255),
    "PROG_STRY_SUB_TASK_MODE" character varying(255),
    "SPRINT_EXCEEDED_WARN" boolean,
    "STORY_SYNC_MODE" character varying(255),
    "STRICT_STAGE_DIVISION" boolean,
    "SUGGEST_REPL_ESTIMATES" boolean,
    "SYNC_DESCRIPTION" boolean,
    "SYNC_EPICS" boolean,
    "SYNC_ESTIMATES" boolean,
    "SYNC_INITIATIVES" boolean,
    "SYNC_START_ENABLED" boolean,
    "SYNC_STORIES" boolean,
    "SYNC_SUMMARY" boolean,
    "TEMPLATE_TYPE" character varying(255)
);


ALTER TABLE "AO_A415DF_AOPLAN_CONFIGURATION" OWNER TO jira;

--
-- Name: AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq" OWNED BY "AO_A415DF_AOPLAN_CONFIGURATION"."ID_OTHER";


--
-- Name: AO_A415DF_AOPLAN_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOPLAN_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOPLAN_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOPLAN_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOPLAN_ID_OTHER_seq" OWNED BY "AO_A415DF_AOPLAN"."ID_OTHER";


--
-- Name: AO_A415DF_AOPRESENCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOPRESENCE" (
    "AOPERSON_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "END_DATE" bigint,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "START_DATE" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOPRESENCE" OWNER TO jira;

--
-- Name: AO_A415DF_AOPRESENCE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOPRESENCE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOPRESENCE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOPRESENCE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOPRESENCE_ID_OTHER_seq" OWNED BY "AO_A415DF_AOPRESENCE"."ID_OTHER";


--
-- Name: AO_A415DF_AORELEASE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AORELEASE" (
    "AODELTA_START_DATE" bigint,
    "AOFIXED_END_DATE" bigint,
    "AOFIXED_START_DATE" bigint,
    "AOPLAN_ID" integer,
    "AOSTREAM_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "IS_LATER_RELEASE" boolean,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PRIMARY_VERSION" character varying(255),
    "SORT_ORDER" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AORELEASE" OWNER TO jira;

--
-- Name: AO_A415DF_AORELEASE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AORELEASE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AORELEASE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AORELEASE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AORELEASE_ID_OTHER_seq" OWNED BY "AO_A415DF_AORELEASE"."ID_OTHER";


--
-- Name: AO_A415DF_AOREPLANNING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOREPLANNING" (
    "ID_OTHER" integer NOT NULL,
    "TARGET_ID" character varying(255),
    "TARGET_TYPE" character varying(255),
    "WORK_ITEM_ID" integer
);


ALTER TABLE "AO_A415DF_AOREPLANNING" OWNER TO jira;

--
-- Name: AO_A415DF_AOREPLANNING_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOREPLANNING_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOREPLANNING_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOREPLANNING_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOREPLANNING_ID_OTHER_seq" OWNED BY "AO_A415DF_AOREPLANNING"."ID_OTHER";


--
-- Name: AO_A415DF_AORESOURCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AORESOURCE" (
    "AOPERSON_ID" integer,
    "AOTEAM_ID" integer,
    "AVAILABILITY" double precision,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AORESOURCE" OWNER TO jira;

--
-- Name: AO_A415DF_AORESOURCE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AORESOURCE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AORESOURCE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AORESOURCE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AORESOURCE_ID_OTHER_seq" OWNED BY "AO_A415DF_AORESOURCE"."ID_OTHER";


--
-- Name: AO_A415DF_AOSKILL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSKILL" (
    "AOSTAGE_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PERCENTAGE" double precision,
    "SORT_ORDER" bigint,
    "STAGE_ID" character varying(255),
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOSKILL" OWNER TO jira;

--
-- Name: AO_A415DF_AOSKILL_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSKILL_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSKILL_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSKILL_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSKILL_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSKILL"."ID_OTHER";


--
-- Name: AO_A415DF_AOSOLUTION_STORE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSOLUTION_STORE" (
    "AOPLAN_ID" integer NOT NULL,
    "ID_OTHER" integer NOT NULL,
    "SOLUTION" text NOT NULL,
    "SOLUTION_VERSION" bigint NOT NULL
);


ALTER TABLE "AO_A415DF_AOSOLUTION_STORE" OWNER TO jira;

--
-- Name: AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSOLUTION_STORE"."ID_OTHER";


--
-- Name: AO_A415DF_AOSPRINT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSPRINT" (
    "AOTEAM_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "END_DATE" bigint,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SORT_ORDER" bigint,
    "START_DATE" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOSPRINT" OWNER TO jira;

--
-- Name: AO_A415DF_AOSPRINT_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSPRINT_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSPRINT_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSPRINT_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSPRINT_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSPRINT"."ID_OTHER";


--
-- Name: AO_A415DF_AOSTAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSTAGE" (
    "AOPLAN_ID" integer,
    "COLOR" character varying(255),
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PERCENTAGE" double precision,
    "SORT_ORDER" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOSTAGE" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTAGE_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSTAGE_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSTAGE_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTAGE_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSTAGE_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSTAGE"."ID_OTHER";


--
-- Name: AO_A415DF_AOSTREAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSTREAM" (
    "AOPLAN_ID" integer,
    "COLOR" character varying(255),
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "DYNAMIC_START_STREAM" boolean,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "SHORT_NAME" character varying(255),
    "SORT_ORDER" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOSTREAM" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTREAM_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSTREAM_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSTREAM_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTREAM_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSTREAM_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSTREAM"."ID_OTHER";


--
-- Name: AO_A415DF_AOSTREAM_TO_TEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOSTREAM_TO_TEAM" (
    "AOSTREAM_ID" integer,
    "AOTEAM_ID" integer,
    "ID_OTHER" integer NOT NULL,
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOSTREAM_TO_TEAM" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq" OWNED BY "AO_A415DF_AOSTREAM_TO_TEAM"."ID_OTHER";


--
-- Name: AO_A415DF_AOTEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOTEAM" (
    "AOPLAN_ID" integer,
    "AUTO_ADJUST_TO_ABSENCES" boolean,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "INCREMENTAL_ADJUSTMENT" double precision,
    "ITERATION_START_TYPE" character varying(255),
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PLANNING_MODE" character varying(255),
    "SORT_ORDER" bigint,
    "TITLE" character varying(255),
    "VELOCITY" double precision,
    "VERSION" bigint,
    "WEEKS_PER_SPRINT" integer
);


ALTER TABLE "AO_A415DF_AOTEAM" OWNER TO jira;

--
-- Name: AO_A415DF_AOTEAM_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOTEAM_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOTEAM_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOTEAM_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOTEAM_ID_OTHER_seq" OWNED BY "AO_A415DF_AOTEAM"."ID_OTHER";


--
-- Name: AO_A415DF_AOTHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOTHEME" (
    "AOPLAN_ID" integer,
    "COLOR" character varying(255),
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PERCENTAGE" double precision,
    "SORT_ORDER" bigint,
    "TITLE" character varying(255),
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOTHEME" OWNER TO jira;

--
-- Name: AO_A415DF_AOTHEME_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOTHEME_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOTHEME_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOTHEME_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOTHEME_ID_OTHER_seq" OWNED BY "AO_A415DF_AOTHEME"."ID_OTHER";


--
-- Name: AO_A415DF_AOWORK_ITEM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOWORK_ITEM" (
    "AOBUSINESS_VALUE" double precision,
    "AOEARLIEST_START" bigint,
    "AOPARENT_ID" integer,
    "AOPLAN_ID" integer,
    "AORELEASE_ID" integer,
    "AOSPRINT_ID" integer,
    "AOSTREAM_ID" integer,
    "AOTARGET_END" bigint,
    "AOTARGET_START" bigint,
    "AOTEAM_ID" integer,
    "AOTHEME_ID" integer,
    "DESCRIPTION" character varying(255),
    "DETAILS" text,
    "EARLIEST_START" bigint,
    "HAS_ORIGINAL_ESTIMATES" boolean,
    "ID_OTHER" integer NOT NULL,
    "ORDER_RANGE_IDENTIFIER" character varying(255),
    "PARENT_ID" character varying(255),
    "REPLANNING_STATUS" integer,
    "SORT_ORDER" bigint,
    "STATUS" integer,
    "TITLE" character varying(255),
    "TYPE" integer,
    "VERSION" bigint
);


ALTER TABLE "AO_A415DF_AOWORK_ITEM" OWNER TO jira;

--
-- Name: AO_A415DF_AOWORK_ITEM_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOWORK_ITEM_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOWORK_ITEM_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOWORK_ITEM_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOWORK_ITEM_ID_OTHER_seq" OWNED BY "AO_A415DF_AOWORK_ITEM"."ID_OTHER";


--
-- Name: AO_A415DF_AOWORK_ITEM_TO_RES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A415DF_AOWORK_ITEM_TO_RES" (
    "AORESOURCE_ID" integer,
    "AOWORK_ITEM_ID" integer,
    "ID_OTHER" integer NOT NULL,
    "REPLANNING" boolean
);


ALTER TABLE "AO_A415DF_AOWORK_ITEM_TO_RES" OWNER TO jira;

--
-- Name: AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq" OWNER TO jira;

--
-- Name: AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq" OWNED BY "AO_A415DF_AOWORK_ITEM_TO_RES"."ID_OTHER";


--
-- Name: AO_A44657_HEALTH_CHECK_ENTITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_A44657_HEALTH_CHECK_ENTITY" (
    "ID" integer NOT NULL
);


ALTER TABLE "AO_A44657_HEALTH_CHECK_ENTITY" OWNER TO jira;

--
-- Name: AO_A44657_HEALTH_CHECK_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_A44657_HEALTH_CHECK_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_A44657_HEALTH_CHECK_ENTITY_ID_seq" OWNER TO jira;

--
-- Name: AO_A44657_HEALTH_CHECK_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_A44657_HEALTH_CHECK_ENTITY_ID_seq" OWNED BY "AO_A44657_HEALTH_CHECK_ENTITY"."ID";


--
-- Name: AO_AEFED0_MEMBERSHIP; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_MEMBERSHIP" (
    "AVAILABILITY" integer DEFAULT 0 NOT NULL,
    "FROM_DATE" timestamp with time zone,
    "ID" integer NOT NULL,
    "TEAM_MEMBER_ID" integer NOT NULL,
    "TEAM_ROLE_ID" integer NOT NULL,
    "TO_DATE" timestamp with time zone
);


ALTER TABLE "AO_AEFED0_MEMBERSHIP" OWNER TO jira;

--
-- Name: AO_AEFED0_MEMBERSHIP_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_MEMBERSHIP_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_MEMBERSHIP_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_MEMBERSHIP_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_MEMBERSHIP_ID_seq" OWNED BY "AO_AEFED0_MEMBERSHIP"."ID";


--
-- Name: AO_AEFED0_PROGRAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_PROGRAM" (
    "ID" integer NOT NULL,
    "MANAGER" character varying(255),
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_AEFED0_PROGRAM" OWNER TO jira;

--
-- Name: AO_AEFED0_PROGRAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_PROGRAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_PROGRAM_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_PROGRAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_PROGRAM_ID_seq" OWNED BY "AO_AEFED0_PROGRAM"."ID";


--
-- Name: AO_AEFED0_TEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM" (
    "DESCRIPTION" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_ID_seq" OWNED BY "AO_AEFED0_TEAM"."ID";


--
-- Name: AO_AEFED0_TEAM_LINK; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_LINK" (
    "ID" integer NOT NULL,
    "SCOPE" bigint DEFAULT 0 NOT NULL,
    "TEAM_ID" integer NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM_LINK" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_LINK_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_LINK_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_LINK_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_LINK_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_LINK_ID_seq" OWNED BY "AO_AEFED0_TEAM_LINK"."ID";


--
-- Name: AO_AEFED0_TEAM_MEMBER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_MEMBER" (
    "ID" integer NOT NULL,
    "MEMBER_KEY" character varying(255) NOT NULL,
    "MEMBER_TYPE" character varying(255) NOT NULL,
    "ROLE_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM_MEMBER" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_MEMBER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_MEMBER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_MEMBER_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_MEMBER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_MEMBER_ID_seq" OWNED BY "AO_AEFED0_TEAM_MEMBER"."ID";


--
-- Name: AO_AEFED0_TEAM_MEMBER_V2; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_MEMBER_V2" (
    "ID" integer NOT NULL,
    "MEMBER_KEY" character varying(255) NOT NULL,
    "MEMBER_TYPE" character varying(255) NOT NULL,
    "TEAM_ID" integer NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM_MEMBER_V2" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_MEMBER_V2_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_MEMBER_V2_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_MEMBER_V2_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_MEMBER_V2_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_MEMBER_V2_ID_seq" OWNED BY "AO_AEFED0_TEAM_MEMBER_V2"."ID";


--
-- Name: AO_AEFED0_TEAM_PERMISSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_PERMISSION" (
    "ID" integer NOT NULL,
    "MEMBER_KEY" character varying(255) NOT NULL,
    "PERMISSION_KEY" character varying(255) NOT NULL,
    "PERMISSION_TYPE" character varying(255) NOT NULL,
    "TEAM_ID" integer NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM_PERMISSION" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_PERMISSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_PERMISSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_PERMISSION_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_PERMISSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_PERMISSION_ID_seq" OWNED BY "AO_AEFED0_TEAM_PERMISSION"."ID";


--
-- Name: AO_AEFED0_TEAM_ROLE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_ROLE" (
    "DEFAULT" boolean DEFAULT false NOT NULL,
    "I18N_KEY" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_AEFED0_TEAM_ROLE" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_ROLE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_ROLE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_ROLE_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_ROLE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_ROLE_ID_seq" OWNED BY "AO_AEFED0_TEAM_ROLE"."ID";


--
-- Name: AO_AEFED0_TEAM_TO_MEMBER; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_TO_MEMBER" (
    "ID" integer NOT NULL,
    "TEAM_ID" integer,
    "TEAM_MEMBER_ID" integer
);


ALTER TABLE "AO_AEFED0_TEAM_TO_MEMBER" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_TO_MEMBER_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_TO_MEMBER_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_TO_MEMBER_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_TO_MEMBER_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_TO_MEMBER_ID_seq" OWNED BY "AO_AEFED0_TEAM_TO_MEMBER"."ID";


--
-- Name: AO_AEFED0_TEAM_V2; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_AEFED0_TEAM_V2" (
    "ID" integer NOT NULL,
    "LEAD" character varying(255),
    "MISSION_V2" text,
    "NAME" character varying(255) NOT NULL,
    "PROGRAM_ID" integer,
    "SUMMARY" character varying(255)
);


ALTER TABLE "AO_AEFED0_TEAM_V2" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_V2_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_AEFED0_TEAM_V2_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_AEFED0_TEAM_V2_ID_seq" OWNER TO jira;

--
-- Name: AO_AEFED0_TEAM_V2_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_AEFED0_TEAM_V2_ID_seq" OWNED BY "AO_AEFED0_TEAM_V2"."ID";


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_B9A0F0_APPLIED_TEMPLATE" (
    "ID" integer NOT NULL,
    "PROJECT_ID" bigint DEFAULT 0,
    "PROJECT_TEMPLATE_MODULE_KEY" character varying(255),
    "PROJECT_TEMPLATE_WEB_ITEM_KEY" character varying(255)
);


ALTER TABLE "AO_B9A0F0_APPLIED_TEMPLATE" OWNER TO jira;

--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_B9A0F0_APPLIED_TEMPLATE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_B9A0F0_APPLIED_TEMPLATE_ID_seq" OWNER TO jira;

--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_B9A0F0_APPLIED_TEMPLATE_ID_seq" OWNED BY "AO_B9A0F0_APPLIED_TEMPLATE"."ID";


--
-- Name: AO_C3C6E8_ACCOUNT_V1; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_ACCOUNT_V1" (
    "CATEGORY_ID" integer,
    "CONTACT" character varying(255),
    "CUSTOMER_ID" integer,
    "GLOBAL" boolean DEFAULT false NOT NULL,
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "LEAD" character varying(255) NOT NULL,
    "MONTHLY_BUDGET" integer,
    "NAME" character varying(255) NOT NULL,
    "STATUS" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_ACCOUNT_V1" OWNER TO jira;

--
-- Name: AO_C3C6E8_ACCOUNT_V1_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_ACCOUNT_V1_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_ACCOUNT_V1_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_ACCOUNT_V1_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_ACCOUNT_V1_ID_seq" OWNED BY "AO_C3C6E8_ACCOUNT_V1"."ID";


--
-- Name: AO_C3C6E8_BUDGET; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_BUDGET" (
    "ID" integer NOT NULL,
    "SCOPE_ID" integer DEFAULT 0 NOT NULL,
    "SCOPE_TYPE" character varying(255) NOT NULL,
    "VALUE" double precision DEFAULT 0.0 NOT NULL,
    "VALUE_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_BUDGET" OWNER TO jira;

--
-- Name: AO_C3C6E8_BUDGET_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_BUDGET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_BUDGET_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_BUDGET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_BUDGET_ID_seq" OWNED BY "AO_C3C6E8_BUDGET"."ID";


--
-- Name: AO_C3C6E8_CATEGORY_TYPE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_CATEGORY_TYPE" (
    "I18N_KEY" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_CATEGORY_TYPE" OWNER TO jira;

--
-- Name: AO_C3C6E8_CATEGORY_TYPE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_CATEGORY_TYPE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_CATEGORY_TYPE_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_CATEGORY_TYPE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_CATEGORY_TYPE_ID_seq" OWNED BY "AO_C3C6E8_CATEGORY_TYPE"."ID";


--
-- Name: AO_C3C6E8_CATEGORY_V1; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_CATEGORY_V1" (
    "CATEGORY_TYPE_ID" integer,
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_CATEGORY_V1" OWNER TO jira;

--
-- Name: AO_C3C6E8_CATEGORY_V1_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_CATEGORY_V1_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_CATEGORY_V1_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_CATEGORY_V1_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_CATEGORY_V1_ID_seq" OWNED BY "AO_C3C6E8_CATEGORY_V1"."ID";


--
-- Name: AO_C3C6E8_CUSTOMER_PERMISSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_CUSTOMER_PERMISSION" (
    "CUSTOMER_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "MEMBER_KEY" character varying(255) NOT NULL,
    "PERMISSION_KEY" character varying(255) NOT NULL,
    "PERMISSION_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_CUSTOMER_PERMISSION" OWNER TO jira;

--
-- Name: AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq" OWNED BY "AO_C3C6E8_CUSTOMER_PERMISSION"."ID";


--
-- Name: AO_C3C6E8_CUSTOMER_V1; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_CUSTOMER_V1" (
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "NAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_CUSTOMER_V1" OWNER TO jira;

--
-- Name: AO_C3C6E8_CUSTOMER_V1_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_CUSTOMER_V1_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_CUSTOMER_V1_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_CUSTOMER_V1_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_CUSTOMER_V1_ID_seq" OWNED BY "AO_C3C6E8_CUSTOMER_V1"."ID";


--
-- Name: AO_C3C6E8_LINK_V1; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_LINK_V1" (
    "ACCOUNT_ID" integer NOT NULL,
    "DEFAULT_ACCOUNT" boolean DEFAULT false NOT NULL,
    "ID" integer NOT NULL,
    "LINK_TYPE" character varying(255) NOT NULL,
    "SCOPE" bigint DEFAULT 0 NOT NULL,
    "SCOPE_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_C3C6E8_LINK_V1" OWNER TO jira;

--
-- Name: AO_C3C6E8_LINK_V1_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_LINK_V1_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_LINK_V1_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_LINK_V1_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_LINK_V1_ID_seq" OWNED BY "AO_C3C6E8_LINK_V1"."ID";


--
-- Name: AO_C3C6E8_RATE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_RATE" (
    "AMOUNT" double precision DEFAULT 0.0 NOT NULL,
    "ID" integer NOT NULL,
    "LINK_ID" integer DEFAULT 0,
    "LINK_TYPE" character varying(255) NOT NULL,
    "RATE_TABLE_ID" integer NOT NULL
);


ALTER TABLE "AO_C3C6E8_RATE" OWNER TO jira;

--
-- Name: AO_C3C6E8_RATE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_RATE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_RATE_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_RATE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_RATE_ID_seq" OWNED BY "AO_C3C6E8_RATE"."ID";


--
-- Name: AO_C3C6E8_RATE_TABLE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C3C6E8_RATE_TABLE" (
    "CURRENCY_CODE" character varying(255) DEFAULT 'USD'::character varying NOT NULL,
    "DEFAULT_TABLE" boolean DEFAULT false NOT NULL,
    "DISCOUNT" double precision,
    "EFFECTIVE_DATE" timestamp without time zone NOT NULL,
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "PARENT_ID" integer,
    "SCOPE_ID" integer,
    "SCOPE_TYPE" character varying(255)
);


ALTER TABLE "AO_C3C6E8_RATE_TABLE" OWNER TO jira;

--
-- Name: AO_C3C6E8_RATE_TABLE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C3C6E8_RATE_TABLE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C3C6E8_RATE_TABLE_ID_seq" OWNER TO jira;

--
-- Name: AO_C3C6E8_RATE_TABLE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C3C6E8_RATE_TABLE_ID_seq" OWNED BY "AO_C3C6E8_RATE_TABLE"."ID";


--
-- Name: AO_C7F17E_LINGO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C7F17E_LINGO" (
    "CATEGORY" character varying(255),
    "CREATED_TIMESTAMP" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "LOGICAL_ID" character varying(255),
    "PROJECT_ID" bigint,
    "SYSTEM_I18N" character varying(255)
);


ALTER TABLE "AO_C7F17E_LINGO" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C7F17E_LINGO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C7F17E_LINGO_ID_seq" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C7F17E_LINGO_ID_seq" OWNED BY "AO_C7F17E_LINGO"."ID";


--
-- Name: AO_C7F17E_LINGO_REVISION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C7F17E_LINGO_REVISION" (
    "CREATED_TIMESTAMP" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "LINGO_ID" bigint
);


ALTER TABLE "AO_C7F17E_LINGO_REVISION" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_REVISION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C7F17E_LINGO_REVISION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C7F17E_LINGO_REVISION_ID_seq" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_REVISION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C7F17E_LINGO_REVISION_ID_seq" OWNED BY "AO_C7F17E_LINGO_REVISION"."ID";


--
-- Name: AO_C7F17E_LINGO_TRANSLATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_C7F17E_LINGO_TRANSLATION" (
    "CONTENT" text NOT NULL,
    "CREATED_TIMESTAMP" bigint NOT NULL,
    "ID" bigint NOT NULL,
    "LANGUAGE" character varying(63) NOT NULL,
    "LINGO_REVISION_ID" bigint,
    "LOCALE" character varying(63) NOT NULL
);


ALTER TABLE "AO_C7F17E_LINGO_TRANSLATION" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_TRANSLATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_C7F17E_LINGO_TRANSLATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_C7F17E_LINGO_TRANSLATION_ID_seq" OWNER TO jira;

--
-- Name: AO_C7F17E_LINGO_TRANSLATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_C7F17E_LINGO_TRANSLATION_ID_seq" OWNED BY "AO_C7F17E_LINGO_TRANSLATION"."ID";


--
-- Name: AO_CFF990_AOTRANSITION_FAILURE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_CFF990_AOTRANSITION_FAILURE" (
    "ERROR_MESSAGES" text,
    "FAILURE_TIME" timestamp with time zone,
    "ID" integer NOT NULL,
    "ISSUE_ID" bigint DEFAULT 0,
    "LOG_REFERRAL_HASH" character varying(255),
    "TRANSITION_ID" bigint DEFAULT 0,
    "TRIGGER_ID" bigint DEFAULT 0,
    "USER_KEY" character varying(255),
    "WORKFLOW_ID" character varying(255)
);


ALTER TABLE "AO_CFF990_AOTRANSITION_FAILURE" OWNER TO jira;

--
-- Name: AO_CFF990_AOTRANSITION_FAILURE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_CFF990_AOTRANSITION_FAILURE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_CFF990_AOTRANSITION_FAILURE_ID_seq" OWNER TO jira;

--
-- Name: AO_CFF990_AOTRANSITION_FAILURE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_CFF990_AOTRANSITION_FAILURE_ID_seq" OWNED BY "AO_CFF990_AOTRANSITION_FAILURE"."ID";


--
-- Name: AO_D9132D_ASSIGNMENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_ASSIGNMENT" (
    "ID" bigint NOT NULL,
    "INTERVAL_END" bigint NOT NULL,
    "INTERVAL_START" bigint NOT NULL,
    "ISSUE" character varying(255) NOT NULL,
    "PLAN" bigint NOT NULL,
    "PROJECT" character varying(255) NOT NULL,
    "RESOURCE" character varying(255) NOT NULL,
    "SKILL" character varying(255) NOT NULL,
    "SOLUTION_ID" bigint,
    "SPRINT_INDEX" integer NOT NULL,
    "STAGE" character varying(255) NOT NULL,
    "TEAM" character varying(255) NOT NULL,
    "VERSION" character varying(255) NOT NULL,
    "WORKLOAD" double precision NOT NULL
);


ALTER TABLE "AO_D9132D_ASSIGNMENT" OWNER TO jira;

--
-- Name: AO_D9132D_ASSIGNMENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_ASSIGNMENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_ASSIGNMENT_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_ASSIGNMENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_ASSIGNMENT_ID_seq" OWNED BY "AO_D9132D_ASSIGNMENT"."ID";


--
-- Name: AO_D9132D_CONFIGURATION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_CONFIGURATION" (
    "HIERARCHY_ISSUE_LIMIT" bigint,
    "ID" bigint NOT NULL,
    "ISSUE_LIMIT" bigint,
    "PROJECT_LIMIT" bigint
);


ALTER TABLE "AO_D9132D_CONFIGURATION" OWNER TO jira;

--
-- Name: AO_D9132D_CONFIGURATION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_CONFIGURATION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_CONFIGURATION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_CONFIGURATION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_CONFIGURATION_ID_seq" OWNED BY "AO_D9132D_CONFIGURATION"."ID";


--
-- Name: AO_D9132D_DEP_ISSUE_LINK_TYPES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_DEP_ISSUE_LINK_TYPES" (
    "ID" bigint NOT NULL,
    "LINK_ID" bigint DEFAULT 0 NOT NULL,
    "OUTWARD" boolean NOT NULL
);


ALTER TABLE "AO_D9132D_DEP_ISSUE_LINK_TYPES" OWNER TO jira;

--
-- Name: AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq" OWNED BY "AO_D9132D_DEP_ISSUE_LINK_TYPES"."ID";


--
-- Name: AO_D9132D_DISTRIBUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_DISTRIBUTION" (
    "ID" bigint NOT NULL,
    "SCENARIO_ISSUE_ID" bigint,
    "SKILL_ITEM_KEY" character varying(255),
    "WEIGHT" double precision
);


ALTER TABLE "AO_D9132D_DISTRIBUTION" OWNER TO jira;

--
-- Name: AO_D9132D_DISTRIBUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_DISTRIBUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_DISTRIBUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_DISTRIBUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_DISTRIBUTION_ID_seq" OWNED BY "AO_D9132D_DISTRIBUTION"."ID";


--
-- Name: AO_D9132D_EXCLUDED_VERSIONS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_EXCLUDED_VERSIONS" (
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "VERSION" bigint
);


ALTER TABLE "AO_D9132D_EXCLUDED_VERSIONS" OWNER TO jira;

--
-- Name: AO_D9132D_EXCLUDED_VERSIONS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_EXCLUDED_VERSIONS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_EXCLUDED_VERSIONS_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_EXCLUDED_VERSIONS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_EXCLUDED_VERSIONS_ID_seq" OWNED BY "AO_D9132D_EXCLUDED_VERSIONS"."ID";


--
-- Name: AO_D9132D_HIERARCHY_CONFIG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_HIERARCHY_CONFIG" (
    "ICON_URL" character varying(255),
    "ID" bigint NOT NULL,
    "ISSUE_TYPE_IDS" text,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_D9132D_HIERARCHY_CONFIG" OWNER TO jira;

--
-- Name: AO_D9132D_HIERARCHY_CONFIG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_HIERARCHY_CONFIG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_HIERARCHY_CONFIG_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_HIERARCHY_CONFIG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_HIERARCHY_CONFIG_ID_seq" OWNED BY "AO_D9132D_HIERARCHY_CONFIG"."ID";


--
-- Name: AO_D9132D_INIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_INIT" (
    "ID" bigint NOT NULL,
    "KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_D9132D_INIT" OWNER TO jira;

--
-- Name: AO_D9132D_INIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_INIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_INIT_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_INIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_INIT_ID_seq" OWNED BY "AO_D9132D_INIT"."ID";


--
-- Name: AO_D9132D_ISSUE_SOURCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_ISSUE_SOURCE" (
    "CONVERSION_FACTOR" double precision,
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "SOURCE_TYPE" character varying(255),
    "SOURCE_VALUE" text
);


ALTER TABLE "AO_D9132D_ISSUE_SOURCE" OWNER TO jira;

--
-- Name: AO_D9132D_ISSUE_SOURCE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_ISSUE_SOURCE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_ISSUE_SOURCE_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_ISSUE_SOURCE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_ISSUE_SOURCE_ID_seq" OWNED BY "AO_D9132D_ISSUE_SOURCE"."ID";


--
-- Name: AO_D9132D_NONWORKINGDAYS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_NONWORKINGDAYS" (
    "END" bigint,
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "START" bigint,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_D9132D_NONWORKINGDAYS" OWNER TO jira;

--
-- Name: AO_D9132D_NONWORKINGDAYS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_NONWORKINGDAYS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_NONWORKINGDAYS_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_NONWORKINGDAYS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_NONWORKINGDAYS_ID_seq" OWNED BY "AO_D9132D_NONWORKINGDAYS"."ID";


--
-- Name: AO_D9132D_PERMISSIONS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PERMISSIONS" (
    "HOLDER_KEY" character varying(255) NOT NULL,
    "HOLDER_TYPE" integer DEFAULT 0 NOT NULL,
    "ID" bigint NOT NULL,
    "PERMISSION" integer DEFAULT 0 NOT NULL,
    "PLAN_ID" bigint
);


ALTER TABLE "AO_D9132D_PERMISSIONS" OWNER TO jira;

--
-- Name: AO_D9132D_PERMISSIONS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PERMISSIONS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PERMISSIONS_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PERMISSIONS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PERMISSIONS_ID_seq" OWNED BY "AO_D9132D_PERMISSIONS"."ID";


--
-- Name: AO_D9132D_PLAN; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PLAN" (
    "ASSIGNEE_SCHEDULING_LEVEL" bigint DEFAULT 0,
    "COMMIT_ISSUE_ASSIGNEE" integer,
    "DEFAULT_ESTIMATES" text,
    "DEFAULT_TEAM_WEEKLY_CAPACITY" double precision DEFAULT 0.0,
    "DEF_EST_MAP" character varying(255),
    "DEPENDENCY_MODE" bigint,
    "GLOBAL_DEFAULT_VELOCITY" double precision DEFAULT 0.0,
    "GLOBAL_SPRINT_LENGTH" integer DEFAULT 0,
    "HAS_SPRINT_CONSTRAINT" integer DEFAULT 0,
    "HEURISTIC_THRESHOLD" integer DEFAULT 0,
    "HOURS_PER_DAY" double precision DEFAULT 0.0,
    "ID" bigint NOT NULL,
    "MAX_RESOURCES_PER_STORY" bigint DEFAULT 0,
    "MIN_LOAD_UNSTR_EPICS" bigint DEFAULT 0,
    "PLANNING_UNIT" bigint,
    "SCHEDULING_VERBOSITY" bigint DEFAULT 0,
    "SPRINT_EXCEEDED_WARN" integer DEFAULT 0,
    "STRICT_STAGE_DIVISION" integer DEFAULT 0,
    "SYNC_START_ENABLED" integer DEFAULT 0,
    "TITLE" character varying(255),
    "UNESTIMATED_ISSUES_OPTION" bigint DEFAULT 0,
    "WEEKDAY_CONFIGURATION" integer DEFAULT 0
);


ALTER TABLE "AO_D9132D_PLAN" OWNER TO jira;

--
-- Name: AO_D9132D_PLANSKILL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PLANSKILL" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "SKILL_ID" bigint,
    "STAGE" bigint,
    "WEIGHT" double precision
);


ALTER TABLE "AO_D9132D_PLANSKILL" OWNER TO jira;

--
-- Name: AO_D9132D_PLANSKILL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PLANSKILL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PLANSKILL_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PLANSKILL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PLANSKILL_ID_seq" OWNED BY "AO_D9132D_PLANSKILL"."ID";


--
-- Name: AO_D9132D_PLANTEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PLANTEAM" (
    "ID" bigint NOT NULL,
    "ISSUE_SOURCE_ID" bigint,
    "ITERATION_LENGTH" bigint,
    "PLAN_ID" bigint,
    "SCHEDULING_MODE" bigint,
    "TEAM_ID" bigint,
    "VELOCITY" double precision,
    "WEEKLY_HOURS" double precision
);


ALTER TABLE "AO_D9132D_PLANTEAM" OWNER TO jira;

--
-- Name: AO_D9132D_PLANTEAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PLANTEAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PLANTEAM_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PLANTEAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PLANTEAM_ID_seq" OWNED BY "AO_D9132D_PLANTEAM"."ID";


--
-- Name: AO_D9132D_PLANTHEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PLANTHEME" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "THEME_ID" bigint,
    "WEIGHT" double precision
);


ALTER TABLE "AO_D9132D_PLANTHEME" OWNER TO jira;

--
-- Name: AO_D9132D_PLANTHEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PLANTHEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PLANTHEME_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PLANTHEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PLANTHEME_ID_seq" OWNED BY "AO_D9132D_PLANTHEME"."ID";


--
-- Name: AO_D9132D_PLANVERSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_PLANVERSION" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "VERSION_ID" bigint,
    "XPROJECT_VERSION_ID" bigint
);


ALTER TABLE "AO_D9132D_PLANVERSION" OWNER TO jira;

--
-- Name: AO_D9132D_PLANVERSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PLANVERSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PLANVERSION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PLANVERSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PLANVERSION_ID_seq" OWNED BY "AO_D9132D_PLANVERSION"."ID";


--
-- Name: AO_D9132D_PLAN_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_PLAN_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_PLAN_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_PLAN_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_PLAN_ID_seq" OWNED BY "AO_D9132D_PLAN"."ID";


--
-- Name: AO_D9132D_RANK_ITEM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_RANK_ITEM" (
    "DOMAIN" character varying(255),
    "ID" bigint NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "RANGE_ID" bigint DEFAULT 0 NOT NULL,
    "UNIQUE" character varying(255) NOT NULL
);


ALTER TABLE "AO_D9132D_RANK_ITEM" OWNER TO jira;

--
-- Name: AO_D9132D_RANK_ITEM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_RANK_ITEM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_RANK_ITEM_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_RANK_ITEM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_RANK_ITEM_ID_seq" OWNED BY "AO_D9132D_RANK_ITEM"."ID";


--
-- Name: AO_D9132D_SCENARIO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO" (
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint
);


ALTER TABLE "AO_D9132D_SCENARIO" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ABILITY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_ABILITY" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "PERSON_ITEM_KEY" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "SKILL_ITEM_KEY" character varying(255),
    "U_AB" character varying(255) NOT NULL
);


ALTER TABLE "AO_D9132D_SCENARIO_ABILITY" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ABILITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_ABILITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_ABILITY_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ABILITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_ABILITY_ID_seq" OWNED BY "AO_D9132D_SCENARIO_ABILITY"."ID";


--
-- Name: AO_D9132D_SCENARIO_AVLBLTY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_AVLBLTY" (
    "END" bigint,
    "ID" bigint NOT NULL,
    "SCENARIO_RESOURCE_ID" bigint,
    "START" bigint,
    "TITLE" character varying(255),
    "WEEKLY_HOURS" double precision
);


ALTER TABLE "AO_D9132D_SCENARIO_AVLBLTY" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_AVLBLTY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_AVLBLTY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_AVLBLTY_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_AVLBLTY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_AVLBLTY_ID_seq" OWNED BY "AO_D9132D_SCENARIO_AVLBLTY"."ID";


--
-- Name: AO_D9132D_SCENARIO_CHANGES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_CHANGES" (
    "COUNTER" bigint NOT NULL,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "SCENARIO_ID" bigint,
    "T_TYPE" integer NOT NULL
);


ALTER TABLE "AO_D9132D_SCENARIO_CHANGES" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_CHANGES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_CHANGES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_CHANGES_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_CHANGES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_CHANGES_ID_seq" OWNED BY "AO_D9132D_SCENARIO_CHANGES"."ID";


--
-- Name: AO_D9132D_SCENARIO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_ID_seq" OWNED BY "AO_D9132D_SCENARIO"."ID";


--
-- Name: AO_D9132D_SCENARIO_ISSUES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_ISSUES" (
    "C_KEY" character varying(255) NOT NULL,
    "DESCRIPTION" text,
    "DESCRIPTION_CHANGED" boolean,
    "DISTRIBUTION_CHANGED" boolean,
    "EARLIEST_START" bigint,
    "EARLIEST_START_CHANGED" boolean,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "LATER_RELEASE" boolean,
    "LATER_RELEASE_CHANGED" boolean,
    "PARENT_ID" character varying(255),
    "PARENT_ID_CHANGED" boolean,
    "PROJECT_ID" bigint,
    "PROJECT_ID_CHANGED" boolean,
    "RESOURCES_CHANGED" boolean,
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "SPRINT_ID" character varying(255),
    "SPRINT_ID_CHANGED" boolean,
    "STATUS_ID" character varying(255),
    "STATUS_ID_CHANGED" boolean,
    "STORY_POINTS_ESTIMATE" double precision,
    "STORY_POINTS_ESTIMATE_CHANGED" boolean,
    "TEAM_ID_CHANGED" boolean,
    "TEAM_KEY" character varying(255),
    "THEME_ID" character varying(255),
    "THEME_ID_CHANGED" boolean,
    "TIME_ESTIMATE" bigint,
    "TIME_ESTIMATE_CHANGED" boolean,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "TYPE_ID" bigint,
    "TYPE_ID_CHANGED" boolean,
    "VERSION_ID" character varying(255),
    "VERSION_ID_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_ISSUES" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_ISSUES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_ISSUES_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_ISSUES_ID_seq" OWNED BY "AO_D9132D_SCENARIO_ISSUES"."ID";


--
-- Name: AO_D9132D_SCENARIO_ISSUE_LINKS; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_ISSUE_LINKS" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "LINK_TYPE" bigint,
    "LINK_TYPE_CHANGED" boolean,
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "SOURCE" character varying(255),
    "TARGET" character varying(255)
);


ALTER TABLE "AO_D9132D_SCENARIO_ISSUE_LINKS" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq" OWNED BY "AO_D9132D_SCENARIO_ISSUE_LINKS"."ID";


--
-- Name: AO_D9132D_SCENARIO_ISSUE_RES; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_ISSUE_RES" (
    "ID" bigint NOT NULL,
    "RESOURCE_ITEM_KEY" character varying(255),
    "SCENARIO_ISSUE_ID" bigint
);


ALTER TABLE "AO_D9132D_SCENARIO_ISSUE_RES" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUE_RES_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_ISSUE_RES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_ISSUE_RES_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_ISSUE_RES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_ISSUE_RES_ID_seq" OWNED BY "AO_D9132D_SCENARIO_ISSUE_RES"."ID";


--
-- Name: AO_D9132D_SCENARIO_PERSON; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_PERSON" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_PERSON" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_PERSON_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_PERSON_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_PERSON_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_PERSON_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_PERSON_ID_seq" OWNED BY "AO_D9132D_SCENARIO_PERSON"."ID";


--
-- Name: AO_D9132D_SCENARIO_RESOURCE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_RESOURCE" (
    "AVAILABILITY_CHANGED" boolean,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "JOIN_DATE" bigint,
    "JOIN_DATE_CHANGED" boolean,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "LEAVE_DATE" bigint,
    "LEAVE_DATE_CHANGED" boolean,
    "PERSON_ITEM_KEY" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "TEAM_ITEM_KEY" character varying(255),
    "WEEKLY_HOURS" double precision,
    "WEEKLY_HOURS_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_RESOURCE" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_RESOURCE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_RESOURCE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_RESOURCE_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_RESOURCE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_RESOURCE_ID_seq" OWNED BY "AO_D9132D_SCENARIO_RESOURCE"."ID";


--
-- Name: AO_D9132D_SCENARIO_SKILL; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_SKILL" (
    "ADD_TO_PLAN" boolean,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "STAGE_ID" character varying(255),
    "STAGE_ID_CHANGED" boolean,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "WEIGHT" double precision,
    "WEIGHT_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_SKILL" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_SKILL_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_SKILL_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_SKILL_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_SKILL_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_SKILL_ID_seq" OWNED BY "AO_D9132D_SCENARIO_SKILL"."ID";


--
-- Name: AO_D9132D_SCENARIO_STAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_STAGE" (
    "COLOR" character varying(255),
    "COLOR_CHANGED" boolean,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "WEIGHT" double precision,
    "WEIGHT_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_STAGE" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_STAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_STAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_STAGE_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_STAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_STAGE_ID_seq" OWNED BY "AO_D9132D_SCENARIO_STAGE"."ID";


--
-- Name: AO_D9132D_SCENARIO_TEAM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_TEAM" (
    "ADD_TO_PLAN" boolean,
    "AVATAR" character varying(255),
    "AVATAR_CHANGED" boolean,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ISSUE_SOURCE_CHANGED" boolean,
    "ISSUE_SOURCE_ID" bigint,
    "ITEM_KEY" character varying(255) NOT NULL,
    "ITERATION_LENGTH" bigint,
    "ITERATION_LENGTH_CHANGED" boolean,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "SCHEDULING_MODE" character varying(255),
    "SCHEDULING_MODE_CHANGED" boolean,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "VELOCITY" double precision,
    "VELOCITY_CHANGED" boolean,
    "WEEKLY_HOURS" double precision,
    "WEEKLY_HOURS_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_TEAM" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_TEAM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_TEAM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_TEAM_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_TEAM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_TEAM_ID_seq" OWNED BY "AO_D9132D_SCENARIO_TEAM"."ID";


--
-- Name: AO_D9132D_SCENARIO_THEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_THEME" (
    "ADD_TO_PLAN" boolean,
    "COLOR" character varying(255),
    "COLOR_CHANGED" boolean,
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "WEIGHT" double precision,
    "WEIGHT_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_THEME" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_THEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_THEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_THEME_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_THEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_THEME_ID_seq" OWNED BY "AO_D9132D_SCENARIO_THEME"."ID";


--
-- Name: AO_D9132D_SCENARIO_VERSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_VERSION" (
    "C_KEY" character varying(255) NOT NULL,
    "DELTA" bigint,
    "DELTA_CHANGED" boolean,
    "DESCRIPTION" text,
    "DESCRIPTION_CHANGED" boolean,
    "END_DATE" bigint,
    "END_DATE_CHANGED" boolean,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "PROJECT_ID" bigint,
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL,
    "START_DATE" bigint,
    "START_DATE_CHANGED" boolean,
    "TITLE" character varying(255),
    "TITLE_CHANGED" boolean,
    "XPROJECT_VERSION" character varying(255),
    "XPROJECT_VERSION_CHANGED" boolean
);


ALTER TABLE "AO_D9132D_SCENARIO_VERSION" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_VERSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_VERSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_VERSION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_VERSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_VERSION_ID_seq" OWNED BY "AO_D9132D_SCENARIO_VERSION"."ID";


--
-- Name: AO_D9132D_SCENARIO_XPVERSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SCENARIO_XPVERSION" (
    "C_KEY" character varying(255) NOT NULL,
    "ID" bigint NOT NULL,
    "ITEM_KEY" character varying(255) NOT NULL,
    "LAST_CHANGE_TIMESTAMP" bigint,
    "LAST_CHANGE_USER" character varying(255),
    "NAME" character varying(255),
    "NAME_CHANGED" boolean,
    "SCENARIO_ID" bigint,
    "SCENARIO_TYPE" character varying(255) NOT NULL
);


ALTER TABLE "AO_D9132D_SCENARIO_XPVERSION" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_XPVERSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SCENARIO_XPVERSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SCENARIO_XPVERSION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SCENARIO_XPVERSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SCENARIO_XPVERSION_ID_seq" OWNED BY "AO_D9132D_SCENARIO_XPVERSION"."ID";


--
-- Name: AO_D9132D_SOLUTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_SOLUTION" (
    "HEARTBEAT_TIMESTAMP" timestamp without time zone NOT NULL,
    "ID" bigint NOT NULL,
    "PLAN" bigint NOT NULL,
    "SCHEDULABLE_ISSUE_COUNT" bigint,
    "SCHEDULED_ISSUE_COUNT" bigint,
    "SOLUTION" text,
    "STATE" character varying(255) NOT NULL,
    "TRIGGER_TIMESTAMP" timestamp without time zone NOT NULL,
    "UNIQUE_GUARD" character varying(255) NOT NULL,
    "VERSION" character varying(255)
);


ALTER TABLE "AO_D9132D_SOLUTION" OWNER TO jira;

--
-- Name: AO_D9132D_SOLUTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_SOLUTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_SOLUTION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_SOLUTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_SOLUTION_ID_seq" OWNED BY "AO_D9132D_SOLUTION"."ID";


--
-- Name: AO_D9132D_STAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_STAGE" (
    "COLOR" character varying(255),
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "SKILL_ID" bigint,
    "WEIGHT" double precision
);


ALTER TABLE "AO_D9132D_STAGE" OWNER TO jira;

--
-- Name: AO_D9132D_STAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_STAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_STAGE_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_STAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_STAGE_ID_seq" OWNED BY "AO_D9132D_STAGE"."ID";


--
-- Name: AO_D9132D_THEME; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_THEME" (
    "COLOR" character varying(255),
    "ID" bigint NOT NULL,
    "SHARED" boolean,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_D9132D_THEME" OWNER TO jira;

--
-- Name: AO_D9132D_THEME_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_THEME_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_THEME_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_THEME_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_THEME_ID_seq" OWNED BY "AO_D9132D_THEME"."ID";


--
-- Name: AO_D9132D_VERSION_ENRICHMENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_VERSION_ENRICHMENT" (
    "DELTA" bigint,
    "ENV_ID" bigint NOT NULL,
    "ID" bigint NOT NULL
);


ALTER TABLE "AO_D9132D_VERSION_ENRICHMENT" OWNER TO jira;

--
-- Name: AO_D9132D_VERSION_ENRICHMENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_VERSION_ENRICHMENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_VERSION_ENRICHMENT_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_VERSION_ENRICHMENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_VERSION_ENRICHMENT_ID_seq" OWNED BY "AO_D9132D_VERSION_ENRICHMENT"."ID";


--
-- Name: AO_D9132D_X_PROJECT_VERSION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_D9132D_X_PROJECT_VERSION" (
    "ID" bigint NOT NULL,
    "PLAN_ID" bigint,
    "TITLE" character varying(255)
);


ALTER TABLE "AO_D9132D_X_PROJECT_VERSION" OWNER TO jira;

--
-- Name: AO_D9132D_X_PROJECT_VERSION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_D9132D_X_PROJECT_VERSION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_D9132D_X_PROJECT_VERSION_ID_seq" OWNER TO jira;

--
-- Name: AO_D9132D_X_PROJECT_VERSION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_D9132D_X_PROJECT_VERSION_ID_seq" OWNED BY "AO_D9132D_X_PROJECT_VERSION"."ID";


--
-- Name: AO_DEB285_BLOG_AO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_DEB285_BLOG_AO" (
    "AUTHOR" character varying(255),
    "ID" bigint NOT NULL,
    "TEXT" character varying(255)
);


ALTER TABLE "AO_DEB285_BLOG_AO" OWNER TO jira;

--
-- Name: AO_DEB285_BLOG_AO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_DEB285_BLOG_AO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_DEB285_BLOG_AO_ID_seq" OWNER TO jira;

--
-- Name: AO_DEB285_BLOG_AO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_DEB285_BLOG_AO_ID_seq" OWNED BY "AO_DEB285_BLOG_AO"."ID";


--
-- Name: AO_DEB285_COMMENT_AO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_DEB285_COMMENT_AO" (
    "AUTHOR" character varying(255),
    "BLOG_ID" bigint,
    "COMMENT" character varying(255),
    "DATE" timestamp with time zone,
    "ID" integer NOT NULL
);


ALTER TABLE "AO_DEB285_COMMENT_AO" OWNER TO jira;

--
-- Name: AO_DEB285_COMMENT_AO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_DEB285_COMMENT_AO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_DEB285_COMMENT_AO_ID_seq" OWNER TO jira;

--
-- Name: AO_DEB285_COMMENT_AO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_DEB285_COMMENT_AO_ID_seq" OWNED BY "AO_DEB285_COMMENT_AO"."ID";


--
-- Name: AO_E8B6CC_BRANCH; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_BRANCH" (
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "REPOSITORY_ID" integer
);


ALTER TABLE "AO_E8B6CC_BRANCH" OWNER TO jira;

--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_BRANCH_HEAD_MAPPING" (
    "BRANCH_NAME" character varying(255),
    "HEAD" character varying(255),
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer
);


ALTER TABLE "AO_E8B6CC_BRANCH_HEAD_MAPPING" OWNER TO jira;

--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_BRANCH_HEAD_MAPPING"."ID";


--
-- Name: AO_E8B6CC_BRANCH_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_BRANCH_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_BRANCH_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_BRANCH_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_BRANCH_ID_seq" OWNED BY "AO_E8B6CC_BRANCH"."ID";


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_CHANGESET_MAPPING" (
    "AUTHOR" character varying(255),
    "AUTHOR_EMAIL" character varying(255),
    "BRANCH" character varying(255),
    "DATE" timestamp with time zone,
    "FILES_DATA" text,
    "FILE_COUNT" integer DEFAULT 0,
    "FILE_DETAILS_JSON" text,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "MESSAGE" text,
    "NODE" character varying(255),
    "PARENTS_DATA" character varying(255),
    "PROJECT_KEY" character varying(255),
    "RAW_AUTHOR" character varying(255),
    "RAW_NODE" character varying(255),
    "REPOSITORY_ID" integer DEFAULT 0,
    "SMARTCOMMIT_AVAILABLE" boolean,
    "VERSION" integer
);


ALTER TABLE "AO_E8B6CC_CHANGESET_MAPPING" OWNER TO jira;

--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_CHANGESET_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_CHANGESET_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_CHANGESET_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_CHANGESET_MAPPING"."ID";


--
-- Name: AO_E8B6CC_COMMIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_COMMIT" (
    "AUTHOR" character varying(255),
    "AUTHOR_AVATAR_URL" character varying(255),
    "DATE" timestamp with time zone NOT NULL,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "MERGE" boolean,
    "MESSAGE" text,
    "NODE" character varying(255),
    "RAW_AUTHOR" character varying(255)
);


ALTER TABLE "AO_E8B6CC_COMMIT" OWNER TO jira;

--
-- Name: AO_E8B6CC_COMMIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_COMMIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_COMMIT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_COMMIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_COMMIT_ID_seq" OWNED BY "AO_E8B6CC_COMMIT"."ID";


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_GIT_HUB_EVENT" (
    "CREATED_AT" timestamp with time zone NOT NULL,
    "GIT_HUB_ID" character varying(255) DEFAULT '0'::character varying NOT NULL,
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer NOT NULL,
    "SAVE_POINT" boolean
);


ALTER TABLE "AO_E8B6CC_GIT_HUB_EVENT" OWNER TO jira;

--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_GIT_HUB_EVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_GIT_HUB_EVENT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_GIT_HUB_EVENT_ID_seq" OWNED BY "AO_E8B6CC_GIT_HUB_EVENT"."ID";


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_ISSUE_TO_BRANCH" (
    "BRANCH_ID" integer,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255)
);


ALTER TABLE "AO_E8B6CC_ISSUE_TO_BRANCH" OWNER TO jira;

--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_TO_BRANCH"."ID";


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_ISSUE_TO_CHANGESET" (
    "CHANGESET_ID" integer,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "PROJECT_KEY" character varying(255)
);


ALTER TABLE "AO_E8B6CC_ISSUE_TO_CHANGESET" OWNER TO jira;

--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_TO_CHANGESET"."ID";


--
-- Name: AO_E8B6CC_MESSAGE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_MESSAGE" (
    "ADDRESS" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "PAYLOAD" text NOT NULL,
    "PAYLOAD_TYPE" character varying(255) NOT NULL,
    "PRIORITY" integer DEFAULT 0 NOT NULL
);


ALTER TABLE "AO_E8B6CC_MESSAGE" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_MESSAGE_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE"."ID";


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_MESSAGE_QUEUE_ITEM" (
    "ID" integer NOT NULL,
    "LAST_FAILED" timestamp with time zone,
    "LAST_RUN" bigint,
    "MESSAGE_ID" integer NOT NULL,
    "QUEUE" character varying(255) NOT NULL,
    "RETRIES_COUNT" integer DEFAULT 0 NOT NULL,
    "STATE" character varying(255) NOT NULL,
    "STATE_INFO" character varying(255)
);


ALTER TABLE "AO_E8B6CC_MESSAGE_QUEUE_ITEM" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"."ID";


--
-- Name: AO_E8B6CC_MESSAGE_TAG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_MESSAGE_TAG" (
    "ID" integer NOT NULL,
    "MESSAGE_ID" integer,
    "TAG" character varying(255)
);


ALTER TABLE "AO_E8B6CC_MESSAGE_TAG" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_TAG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_TAG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_MESSAGE_TAG_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_MESSAGE_TAG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_TAG_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE_TAG"."ID";


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_ORGANIZATION_MAPPING" (
    "ACCESS_TOKEN" character varying(255),
    "ADMIN_PASSWORD" character varying(255),
    "ADMIN_USERNAME" character varying(255),
    "APPROVAL_STATE" character varying(255),
    "AUTOLINK_NEW_REPOS" boolean,
    "DEFAULT_GROUPS_SLUGS" character varying(255),
    "DVCS_TYPE" character varying(255),
    "HOST_URL" character varying(255),
    "ID" integer NOT NULL,
    "LAST_POLLED" bigint DEFAULT 0,
    "NAME" character varying(255),
    "OAUTH_KEY" character varying(255),
    "OAUTH_SECRET" character varying(255),
    "PRINCIPAL_ID" character varying(255),
    "SMARTCOMMITS_FOR_NEW_REPOS" boolean
);


ALTER TABLE "AO_E8B6CC_ORGANIZATION_MAPPING" OWNER TO jira;

--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_ORGANIZATION_MAPPING"."ID";


--
-- Name: AO_E8B6CC_ORG_TO_PROJECT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_ORG_TO_PROJECT" (
    "ID" integer NOT NULL,
    "ORGANIZATION_ID" integer,
    "PROJECT_KEY" character varying(255)
);


ALTER TABLE "AO_E8B6CC_ORG_TO_PROJECT" OWNER TO jira;

--
-- Name: AO_E8B6CC_ORG_TO_PROJECT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_ORG_TO_PROJECT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_ORG_TO_PROJECT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_ORG_TO_PROJECT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_ORG_TO_PROJECT_ID_seq" OWNED BY "AO_E8B6CC_ORG_TO_PROJECT"."ID";


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_PR_ISSUE_KEY" (
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "PULL_REQUEST_ID" integer DEFAULT 0
);


ALTER TABLE "AO_E8B6CC_PR_ISSUE_KEY" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_PR_ISSUE_KEY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_PR_ISSUE_KEY_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_PR_ISSUE_KEY_ID_seq" OWNED BY "AO_E8B6CC_PR_ISSUE_KEY"."ID";


--
-- Name: AO_E8B6CC_PR_PARTICIPANT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_PR_PARTICIPANT" (
    "APPROVED" boolean,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "PULL_REQUEST_ID" integer,
    "ROLE" character varying(255),
    "USERNAME" character varying(255)
);


ALTER TABLE "AO_E8B6CC_PR_PARTICIPANT" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_PARTICIPANT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_PR_PARTICIPANT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_PR_PARTICIPANT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_PARTICIPANT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_PR_PARTICIPANT_ID_seq" OWNED BY "AO_E8B6CC_PR_PARTICIPANT"."ID";


--
-- Name: AO_E8B6CC_PR_TO_COMMIT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_PR_TO_COMMIT" (
    "COMMIT_ID" integer NOT NULL,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "REQUEST_ID" integer NOT NULL
);


ALTER TABLE "AO_E8B6CC_PR_TO_COMMIT" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_TO_COMMIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_PR_TO_COMMIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_PR_TO_COMMIT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_PR_TO_COMMIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_PR_TO_COMMIT_ID_seq" OWNED BY "AO_E8B6CC_PR_TO_COMMIT"."ID";


--
-- Name: AO_E8B6CC_PULL_REQUEST; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_PULL_REQUEST" (
    "AUTHOR" character varying(255),
    "COMMENT_COUNT" integer DEFAULT 0,
    "CREATED_ON" timestamp with time zone,
    "DESTINATION_BRANCH" character varying(255),
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "EXECUTED_BY" character varying(255),
    "ID" integer NOT NULL,
    "LAST_STATUS" character varying(255),
    "NAME" character varying(255),
    "REMOTE_ID" bigint,
    "SOURCE_BRANCH" character varying(255),
    "SOURCE_REPO" character varying(255),
    "TO_REPOSITORY_ID" integer DEFAULT 0,
    "UPDATED_ON" timestamp with time zone,
    "URL" character varying(255)
);


ALTER TABLE "AO_E8B6CC_PULL_REQUEST" OWNER TO jira;

--
-- Name: AO_E8B6CC_PULL_REQUEST_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_PULL_REQUEST_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_PULL_REQUEST_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_PULL_REQUEST_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_PULL_REQUEST_ID_seq" OWNED BY "AO_E8B6CC_PULL_REQUEST"."ID";


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_REPOSITORY_MAPPING" (
    "ACTIVITY_LAST_SYNC" timestamp with time zone,
    "DELETED" boolean,
    "FORK" boolean,
    "FORK_OF_NAME" character varying(255),
    "FORK_OF_OWNER" character varying(255),
    "FORK_OF_SLUG" character varying(255),
    "ID" integer NOT NULL,
    "LAST_CHANGESET_NODE" character varying(255),
    "LAST_COMMIT_DATE" timestamp with time zone,
    "LINKED" boolean,
    "LOGO" text,
    "NAME" character varying(255),
    "ORGANIZATION_ID" integer DEFAULT 0,
    "SLUG" character varying(255),
    "SMARTCOMMITS_ENABLED" boolean,
    "UPDATE_LINK_AUTHORISED" boolean,
    "WEBHOOK_INSTALL_ERROR" boolean,
    "IS_FIRST_SYNC_RUNNING" boolean DEFAULT false,
    "UUID" character varying(255)
);


ALTER TABLE "AO_E8B6CC_REPOSITORY_MAPPING" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_REPOSITORY_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_REPOSITORY_MAPPING_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_REPOSITORY_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_REPOSITORY_MAPPING"."ID";


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_REPO_TO_CHANGESET" (
    "CHANGESET_ID" integer,
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer
);


ALTER TABLE "AO_E8B6CC_REPO_TO_CHANGESET" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_REPO_TO_CHANGESET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_REPO_TO_CHANGESET_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_REPO_TO_CHANGESET_ID_seq" OWNED BY "AO_E8B6CC_REPO_TO_CHANGESET"."ID";


--
-- Name: AO_E8B6CC_REPO_TO_PROJECT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_REPO_TO_PROJECT" (
    "ID" integer NOT NULL,
    "PROJECT_KEY" character varying(255),
    "REPOSITORY_ID" integer
);


ALTER TABLE "AO_E8B6CC_REPO_TO_PROJECT" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPO_TO_PROJECT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_REPO_TO_PROJECT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_REPO_TO_PROJECT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_REPO_TO_PROJECT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_REPO_TO_PROJECT_ID_seq" OWNED BY "AO_E8B6CC_REPO_TO_PROJECT"."ID";


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_SYNC_AUDIT_LOG" (
    "COMMIT_COUNT" integer DEFAULT 0,
    "END_DATE" timestamp with time zone,
    "EXC_TRACE" text,
    "FIRST_REQUEST_DATE" timestamp with time zone,
    "FLIGHT_TIME_MS" bigint DEFAULT 0,
    "ID" integer NOT NULL,
    "JIRA_ISSUE_KEY_COUNT" integer DEFAULT 0,
    "LAST_ERROR" text,
    "LAST_UPDATED" bigint DEFAULT 0,
    "NUM_REQUESTS" integer DEFAULT 0,
    "PULL_REQUEST_COUNT" integer DEFAULT 0,
    "REPO_ID" integer DEFAULT 0,
    "RE_RUN_FLAGS" character varying(255),
    "START_DATE" timestamp with time zone,
    "SYNC_STATUS" character varying(255),
    "SYNC_TYPE" character varying(255),
    "TOTAL_ERRORS" integer DEFAULT 0
);


ALTER TABLE "AO_E8B6CC_SYNC_AUDIT_LOG" OWNER TO jira;

--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq" OWNED BY "AO_E8B6CC_SYNC_AUDIT_LOG"."ID";


--
-- Name: AO_E8B6CC_SYNC_EVENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_E8B6CC_SYNC_EVENT" (
    "EVENT_CLASS" text NOT NULL,
    "EVENT_DATE" timestamp with time zone NOT NULL,
    "EVENT_JSON" text NOT NULL,
    "ID" integer NOT NULL,
    "REPO_ID" integer DEFAULT 0 NOT NULL,
    "SCHEDULED_SYNC" boolean
);


ALTER TABLE "AO_E8B6CC_SYNC_EVENT" OWNER TO jira;

--
-- Name: AO_E8B6CC_SYNC_EVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_E8B6CC_SYNC_EVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_E8B6CC_SYNC_EVENT_ID_seq" OWNER TO jira;

--
-- Name: AO_E8B6CC_SYNC_EVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_E8B6CC_SYNC_EVENT_ID_seq" OWNED BY "AO_E8B6CC_SYNC_EVENT"."ID";


--
-- Name: AO_ED979B_ACTIONREGISTRY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_ED979B_ACTIONREGISTRY" (
    "ACTION_TYPE" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "OBJECT_ID" character varying(255) NOT NULL,
    "OBJECT_TYPE" character varying(255) NOT NULL,
    "TIME" timestamp with time zone NOT NULL,
    "USERNAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_ED979B_ACTIONREGISTRY" OWNER TO jira;

--
-- Name: AO_ED979B_ACTIONREGISTRY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_ED979B_ACTIONREGISTRY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_ED979B_ACTIONREGISTRY_ID_seq" OWNER TO jira;

--
-- Name: AO_ED979B_ACTIONREGISTRY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_ED979B_ACTIONREGISTRY_ID_seq" OWNED BY "AO_ED979B_ACTIONREGISTRY"."ID";


--
-- Name: AO_ED979B_EVENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_ED979B_EVENT" (
    "CREATION_TIME" timestamp with time zone NOT NULL,
    "EVENT_TYPE" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "MESSAGE" character varying(255),
    "OBJECT_ID" character varying(255) NOT NULL,
    "OBJECT_TYPE" character varying(255) NOT NULL,
    "SOURCE_USERNAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_ED979B_EVENT" OWNER TO jira;

--
-- Name: AO_ED979B_EVENTPROPERTY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_ED979B_EVENTPROPERTY" (
    "EVENT_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "KEY" character varying(255) NOT NULL,
    "VALUE" character varying(255)
);


ALTER TABLE "AO_ED979B_EVENTPROPERTY" OWNER TO jira;

--
-- Name: AO_ED979B_EVENTPROPERTY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_ED979B_EVENTPROPERTY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_ED979B_EVENTPROPERTY_ID_seq" OWNER TO jira;

--
-- Name: AO_ED979B_EVENTPROPERTY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_ED979B_EVENTPROPERTY_ID_seq" OWNED BY "AO_ED979B_EVENTPROPERTY"."ID";


--
-- Name: AO_ED979B_EVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_ED979B_EVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_ED979B_EVENT_ID_seq" OWNER TO jira;

--
-- Name: AO_ED979B_EVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_ED979B_EVENT_ID_seq" OWNED BY "AO_ED979B_EVENT"."ID";


--
-- Name: AO_ED979B_SUBSCRIPTION; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_ED979B_SUBSCRIPTION" (
    "EVENT_TYPE" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "RULES" character varying(255),
    "SUBSCRIBED" boolean NOT NULL,
    "USERNAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_ED979B_SUBSCRIPTION" OWNER TO jira;

--
-- Name: AO_ED979B_SUBSCRIPTION_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_ED979B_SUBSCRIPTION_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_ED979B_SUBSCRIPTION_ID_seq" OWNER TO jira;

--
-- Name: AO_ED979B_SUBSCRIPTION_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_ED979B_SUBSCRIPTION_ID_seq" OWNED BY "AO_ED979B_SUBSCRIPTION"."ID";


--
-- Name: AO_ED979B_USEREVENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_ED979B_USEREVENT" (
    "EVENT_ID" integer NOT NULL,
    "ID" integer NOT NULL,
    "USERNAME" character varying(255) NOT NULL
);


ALTER TABLE "AO_ED979B_USEREVENT" OWNER TO jira;

--
-- Name: AO_ED979B_USEREVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_ED979B_USEREVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_ED979B_USEREVENT_ID_seq" OWNER TO jira;

--
-- Name: AO_ED979B_USEREVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_ED979B_USEREVENT_ID_seq" OWNED BY "AO_ED979B_USEREVENT"."ID";


--
-- Name: AO_F1B27B_HISTORY_RECORD; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F1B27B_HISTORY_RECORD" (
    "EVENT_TIME_MILLIS" bigint,
    "EVENT_TYPE" character varying(63) NOT NULL,
    "ID" integer NOT NULL,
    "MESSAGE" character varying(450) NOT NULL,
    "TARGET_TIME_MILLIS" bigint,
    "TIMED_PROMISE_HISTORY_KEY_HASH" character varying(255) NOT NULL
);


ALTER TABLE "AO_F1B27B_HISTORY_RECORD" OWNER TO jira;

--
-- Name: AO_F1B27B_HISTORY_RECORD_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F1B27B_HISTORY_RECORD_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F1B27B_HISTORY_RECORD_ID_seq" OWNER TO jira;

--
-- Name: AO_F1B27B_HISTORY_RECORD_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F1B27B_HISTORY_RECORD_ID_seq" OWNED BY "AO_F1B27B_HISTORY_RECORD"."ID";


--
-- Name: AO_F1B27B_KEY_COMPONENT; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F1B27B_KEY_COMPONENT" (
    "ID" integer NOT NULL,
    "KEY" character varying(450) NOT NULL,
    "TIMED_PROMISE_ID" integer NOT NULL,
    "VALUE" character varying(450) NOT NULL
);


ALTER TABLE "AO_F1B27B_KEY_COMPONENT" OWNER TO jira;

--
-- Name: AO_F1B27B_KEY_COMPONENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F1B27B_KEY_COMPONENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F1B27B_KEY_COMPONENT_ID_seq" OWNER TO jira;

--
-- Name: AO_F1B27B_KEY_COMPONENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F1B27B_KEY_COMPONENT_ID_seq" OWNED BY "AO_F1B27B_KEY_COMPONENT"."ID";


--
-- Name: AO_F1B27B_KEY_COMP_HISTORY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F1B27B_KEY_COMP_HISTORY" (
    "ID" integer NOT NULL,
    "KEY" character varying(450) NOT NULL,
    "TIMED_PROMISE_ID" integer NOT NULL,
    "VALUE" character varying(450) NOT NULL
);


ALTER TABLE "AO_F1B27B_KEY_COMP_HISTORY" OWNER TO jira;

--
-- Name: AO_F1B27B_KEY_COMP_HISTORY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F1B27B_KEY_COMP_HISTORY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F1B27B_KEY_COMP_HISTORY_ID_seq" OWNER TO jira;

--
-- Name: AO_F1B27B_KEY_COMP_HISTORY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F1B27B_KEY_COMP_HISTORY_ID_seq" OWNED BY "AO_F1B27B_KEY_COMP_HISTORY"."ID";


--
-- Name: AO_F1B27B_PROMISE; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F1B27B_PROMISE" (
    "CLASSIFICATION" character varying(127) NOT NULL,
    "CONTENT" text,
    "CREATED_TIME_MILLIS" bigint,
    "ID" integer NOT NULL,
    "KEY_HASH" character varying(255) NOT NULL,
    "MIME_TYPE" character varying(127),
    "STATUS" character varying(63) NOT NULL,
    "TARGET_TIME_MILLIS" bigint,
    "TASK_KEY" character varying(255) NOT NULL,
    "UPDATED_TIME_MILLIS" bigint
);


ALTER TABLE "AO_F1B27B_PROMISE" OWNER TO jira;

--
-- Name: AO_F1B27B_PROMISE_HISTORY; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F1B27B_PROMISE_HISTORY" (
    "CLASSIFICATION" character varying(127) NOT NULL,
    "ID" integer NOT NULL,
    "KEY_HASH" character varying(255) NOT NULL,
    "TASK_KEY" character varying(255) NOT NULL
);


ALTER TABLE "AO_F1B27B_PROMISE_HISTORY" OWNER TO jira;

--
-- Name: AO_F1B27B_PROMISE_HISTORY_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F1B27B_PROMISE_HISTORY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F1B27B_PROMISE_HISTORY_ID_seq" OWNER TO jira;

--
-- Name: AO_F1B27B_PROMISE_HISTORY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F1B27B_PROMISE_HISTORY_ID_seq" OWNED BY "AO_F1B27B_PROMISE_HISTORY"."ID";


--
-- Name: AO_F1B27B_PROMISE_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F1B27B_PROMISE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F1B27B_PROMISE_ID_seq" OWNER TO jira;

--
-- Name: AO_F1B27B_PROMISE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F1B27B_PROMISE_ID_seq" OWNED BY "AO_F1B27B_PROMISE"."ID";


--
-- Name: AO_F4ED3A_ADD_ON_PROPERTY_AO; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE "AO_F4ED3A_ADD_ON_PROPERTY_AO" (
    "ID" integer NOT NULL,
    "PLUGIN_KEY" character varying(80) NOT NULL,
    "PRIMARY_KEY" character varying(208) NOT NULL,
    "PROPERTY_KEY" character varying(127) NOT NULL,
    "VALUE" text NOT NULL
);


ALTER TABLE "AO_F4ED3A_ADD_ON_PROPERTY_AO" OWNER TO jira;

--
-- Name: AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE "AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq" OWNER TO jira;

--
-- Name: AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE "AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq" OWNED BY "AO_F4ED3A_ADD_ON_PROPERTY_AO"."ID";


--
-- Name: addoncloudexport; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE addoncloudexport (
    id bigint NOT NULL,
    export_id character varying(255),
    created timestamp with time zone
);


ALTER TABLE addoncloudexport OWNER TO jira;

--
-- Name: addoncloudexport_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE addoncloudexport_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE addoncloudexport_id_seq OWNER TO jira;

--
-- Name: addoncloudexport_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE addoncloudexport_id_seq OWNED BY addoncloudexport.id;


--
-- Name: adhocupgradetaskhistory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE adhocupgradetaskhistory (
    id bigint NOT NULL,
    task_id bigint,
    key character varying(255) NOT NULL,
    timeperformed timestamp with time zone DEFAULT now() NOT NULL,
    eventtype character varying(60) NOT NULL
);


ALTER TABLE adhocupgradetaskhistory OWNER TO jira;

--
-- Name: adhocupgradetaskhistory_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE adhocupgradetaskhistory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE adhocupgradetaskhistory_id_seq OWNER TO jira;

--
-- Name: adhocupgradetaskhistory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE adhocupgradetaskhistory_id_seq OWNED BY adhocupgradetaskhistory.id;


--
-- Name: amq_temporary_store_a0b856; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE amq_temporary_store_a0b856 (
    id integer NOT NULL,
    web_hook_data text NOT NULL
);


ALTER TABLE amq_temporary_store_a0b856 OWNER TO jira;

--
-- Name: amq_temporary_store_a0b856_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE amq_temporary_store_a0b856_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE amq_temporary_store_a0b856_id_seq OWNER TO jira;

--
-- Name: amq_temporary_store_a0b856_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE amq_temporary_store_a0b856_id_seq OWNED BY amq_temporary_store_a0b856.id;


--
-- Name: app_user; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE app_user (
    id bigint NOT NULL,
    user_key character varying(255),
    lower_user_name character varying(255)
);


ALTER TABLE app_user OWNER TO jira;

--
-- Name: app_user_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE app_user_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE app_user_id_seq OWNER TO jira;

--
-- Name: app_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE app_user_id_seq OWNED BY app_user.id;


--
-- Name: async_task; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE async_task (
    id bigint NOT NULL,
    user_id bigint,
    description character varying(255),
    message character varying(255),
    status character varying(60),
    progress bigint,
    submitted_time timestamp with time zone,
    start_time timestamp with time zone,
    finished_time timestamp with time zone,
    last_update_time timestamp with time zone,
    result text,
    cancellable character(1),
    lock_key character varying(255)
);


ALTER TABLE async_task OWNER TO jira;

--
-- Name: async_task_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE async_task_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE async_task_id_seq OWNER TO jira;

--
-- Name: async_task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE async_task_id_seq OWNED BY async_task.id;


--
-- Name: async_task_payload; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE async_task_payload (
    id bigint NOT NULL,
    async_task_id bigint,
    payload text,
    submitted_time timestamp with time zone
);


ALTER TABLE async_task_payload OWNER TO jira;

--
-- Name: async_task_payload_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE async_task_payload_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE async_task_payload_id_seq OWNER TO jira;

--
-- Name: async_task_payload_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE async_task_payload_id_seq OWNED BY async_task_payload.id;


--
-- Name: audit_changed_value; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE audit_changed_value (
    id bigint NOT NULL,
    log_id bigint,
    name character varying(255),
    delta_from text,
    delta_to text
);


ALTER TABLE audit_changed_value OWNER TO jira;

--
-- Name: audit_changed_value_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE audit_changed_value_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE audit_changed_value_id_seq OWNER TO jira;

--
-- Name: audit_changed_value_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE audit_changed_value_id_seq OWNED BY audit_changed_value.id;


--
-- Name: audit_item; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE audit_item (
    id bigint NOT NULL,
    log_id bigint,
    object_type character varying(60),
    object_id character varying(255),
    object_name character varying(255),
    object_parent_id character varying(255),
    object_parent_name character varying(255)
);


ALTER TABLE audit_item OWNER TO jira;

--
-- Name: audit_item_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE audit_item_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE audit_item_id_seq OWNER TO jira;

--
-- Name: audit_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE audit_item_id_seq OWNED BY audit_item.id;


--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE audit_log (
    id bigint NOT NULL,
    remote_address character varying(60),
    created timestamp with time zone,
    author_key character varying(255),
    summary character varying(255),
    category character varying(255),
    object_type character varying(60),
    object_id character varying(255),
    object_name character varying(255),
    object_parent_id character varying(255),
    object_parent_name character varying(255),
    author_type integer,
    event_source_name character varying(255),
    description character varying(255),
    long_description text,
    search_field text
);


ALTER TABLE audit_log OWNER TO jira;

--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE audit_log_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE audit_log_id_seq OWNER TO jira;

--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE audit_log_id_seq OWNED BY audit_log.id;


--
-- Name: avatar; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE avatar (
    id bigint NOT NULL,
    filename character varying(255),
    contenttype character varying(255),
    avatartype character varying(60),
    owner character varying(255),
    systemavatar integer
);


ALTER TABLE avatar OWNER TO jira;

--
-- Name: avatar_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE avatar_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE avatar_id_seq OWNER TO jira;

--
-- Name: avatar_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE avatar_id_seq OWNED BY avatar.id;


--
-- Name: board; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE board (
    id bigint NOT NULL,
    jql text
);


ALTER TABLE board OWNER TO jira;

--
-- Name: board_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE board_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE board_id_seq OWNER TO jira;

--
-- Name: board_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE board_id_seq OWNED BY board.id;


--
-- Name: boardproject; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE boardproject (
    board_id bigint NOT NULL,
    project_id bigint NOT NULL
);


ALTER TABLE boardproject OWNER TO jira;

--
-- Name: changegroup; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE changegroup (
    id bigint NOT NULL,
    issueid bigint,
    author character varying(255),
    created timestamp with time zone,
    initialgroup boolean
);


ALTER TABLE changegroup OWNER TO jira;

--
-- Name: changegroup_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE changegroup_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE changegroup_id_seq OWNER TO jira;

--
-- Name: changegroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE changegroup_id_seq OWNED BY changegroup.id;


--
-- Name: changeitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE changeitem (
    id bigint NOT NULL,
    groupid bigint,
    fieldtype character varying(255),
    field character varying(255),
    oldvalue text,
    oldstring text,
    newvalue text,
    newstring text,
    fieldid character varying(60),
    nextchanged timestamp with time zone
);


ALTER TABLE changeitem OWNER TO jira;

--
-- Name: changeitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE changeitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE changeitem_id_seq OWNER TO jira;

--
-- Name: changeitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE changeitem_id_seq OWNED BY changeitem.id;


--
-- Name: clusteredjob; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE clusteredjob (
    id bigint NOT NULL,
    job_id character varying(255),
    job_runner_key character varying(255),
    sched_type character(1),
    interval_millis bigint,
    first_run bigint,
    cron_expression character varying(255),
    time_zone character varying(60),
    next_run bigint,
    version bigint,
    parameters bytea
);


ALTER TABLE clusteredjob OWNER TO jira;

--
-- Name: clusteredjob_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE clusteredjob_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE clusteredjob_id_seq OWNER TO jira;

--
-- Name: clusteredjob_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE clusteredjob_id_seq OWNED BY clusteredjob.id;


--
-- Name: clusterlockstatus; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE clusterlockstatus (
    id bigint NOT NULL,
    lock_name character varying(255),
    locked_by_node character varying(60),
    update_time bigint
);


ALTER TABLE clusterlockstatus OWNER TO jira;

--
-- Name: clusterlockstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE clusterlockstatus_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE clusterlockstatus_id_seq OWNER TO jira;

--
-- Name: clusterlockstatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE clusterlockstatus_id_seq OWNED BY clusterlockstatus.id;


--
-- Name: clustermessage; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE clustermessage (
    id bigint NOT NULL,
    source_node character varying(60),
    destination_node character varying(60),
    claimed_by_node character varying(60),
    message character varying(255),
    message_time timestamp with time zone
);


ALTER TABLE clustermessage OWNER TO jira;

--
-- Name: clustermessage_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE clustermessage_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE clustermessage_id_seq OWNER TO jira;

--
-- Name: clustermessage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE clustermessage_id_seq OWNED BY clustermessage.id;


--
-- Name: clusternode; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE clusternode (
    node_id character varying(60) NOT NULL,
    node_state character varying(60),
    "timestamp" bigint,
    ip character varying(60),
    cache_listener_port bigint
);


ALTER TABLE clusternode OWNER TO jira;

--
-- Name: clusternodeheartbeat; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE clusternodeheartbeat (
    node_id character varying(60) NOT NULL,
    heartbeat_time bigint,
    database_time bigint
);


ALTER TABLE clusternodeheartbeat OWNER TO jira;

--
-- Name: columnlayout; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE columnlayout (
    id bigint NOT NULL,
    username character varying(255),
    searchrequest bigint
);


ALTER TABLE columnlayout OWNER TO jira;

--
-- Name: columnlayout_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE columnlayout_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE columnlayout_id_seq OWNER TO jira;

--
-- Name: columnlayout_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE columnlayout_id_seq OWNED BY columnlayout.id;


--
-- Name: columnlayoutitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE columnlayoutitem (
    id bigint NOT NULL,
    columnlayout bigint,
    fieldidentifier character varying(255),
    horizontalposition bigint
);


ALTER TABLE columnlayoutitem OWNER TO jira;

--
-- Name: columnlayoutitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE columnlayoutitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE columnlayoutitem_id_seq OWNER TO jira;

--
-- Name: columnlayoutitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE columnlayoutitem_id_seq OWNED BY columnlayoutitem.id;


--
-- Name: component; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE component (
    id bigint NOT NULL,
    project bigint,
    cname character varying(255),
    description text,
    url character varying(255),
    lead character varying(255),
    assigneetype bigint
);


ALTER TABLE component OWNER TO jira;

--
-- Name: component_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE component_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE component_id_seq OWNER TO jira;

--
-- Name: component_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE component_id_seq OWNED BY component.id;


--
-- Name: configurationcontext; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE configurationcontext (
    id bigint NOT NULL,
    projectcategory bigint,
    project bigint,
    customfield character varying(255),
    fieldconfigscheme bigint
);


ALTER TABLE configurationcontext OWNER TO jira;

--
-- Name: configurationcontext_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE configurationcontext_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE configurationcontext_id_seq OWNER TO jira;

--
-- Name: configurationcontext_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE configurationcontext_id_seq OWNED BY configurationcontext.id;


--
-- Name: connect_addon_dependencies_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addon_dependencies_f4ed3a (
    addon_key character varying(80) NOT NULL,
    dependency_addon_key character varying(80) NOT NULL
);


ALTER TABLE connect_addon_dependencies_f4ed3a OWNER TO jira;

--
-- Name: connect_addon_descriptors_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addon_descriptors_f4ed3a (
    addon_key character varying(80) NOT NULL,
    descriptor text NOT NULL
);


ALTER TABLE connect_addon_descriptors_f4ed3a OWNER TO jira;

--
-- Name: connect_addon_listings_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addon_listings_f4ed3a (
    addon_key character varying(80) NOT NULL,
    addon_name character varying(80),
    description text,
    vendor_name character varying(100),
    vendor_url character varying(200),
    license_enabled boolean DEFAULT false NOT NULL
);


ALTER TABLE connect_addon_listings_f4ed3a OWNER TO jira;

--
-- Name: connect_addon_remnant_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addon_remnant_f4ed3a (
    addon_key character varying(80) NOT NULL,
    shared_secret character varying(400),
    oauth_client_id character varying(512)
);


ALTER TABLE connect_addon_remnant_f4ed3a OWNER TO jira;

--
-- Name: connect_addon_scopes_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addon_scopes_f4ed3a (
    addon_key character varying(80) NOT NULL,
    scope character varying(30) NOT NULL
);


ALTER TABLE connect_addon_scopes_f4ed3a OWNER TO jira;

--
-- Name: connect_addons_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_addons_f4ed3a (
    addon_key character varying(80) NOT NULL,
    base_url character varying(256) NOT NULL,
    is_enabled boolean NOT NULL,
    auth_type character varying(30) NOT NULL,
    version character varying(30),
    installed_date timestamp with time zone NOT NULL,
    last_updated timestamp with time zone NOT NULL
);


ALTER TABLE connect_addons_f4ed3a OWNER TO jira;

--
-- Name: connect_plugin_modules_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE connect_plugin_modules_f4ed3a (
    data bytea NOT NULL
);


ALTER TABLE connect_plugin_modules_f4ed3a OWNER TO jira;

--
-- Name: customfield; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE customfield (
    id bigint NOT NULL,
    cfkey character varying(255),
    customfieldtypekey character varying(255),
    customfieldsearcherkey character varying(255),
    cfname character varying(255),
    description text,
    defaultvalue character varying(255),
    fieldtype bigint,
    project bigint,
    issuetype character varying(255)
);


ALTER TABLE customfield OWNER TO jira;

--
-- Name: customfield_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE customfield_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE customfield_id_seq OWNER TO jira;

--
-- Name: customfield_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE customfield_id_seq OWNED BY customfield.id;


--
-- Name: customfieldoption; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE customfieldoption (
    id bigint NOT NULL,
    customfield bigint,
    customfieldconfig bigint,
    parentoptionid bigint,
    sequence bigint,
    customvalue character varying(255),
    optiontype character varying(60),
    disabled character varying(60)
);


ALTER TABLE customfieldoption OWNER TO jira;

--
-- Name: customfieldoption_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE customfieldoption_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE customfieldoption_id_seq OWNER TO jira;

--
-- Name: customfieldoption_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE customfieldoption_id_seq OWNED BY customfieldoption.id;


--
-- Name: customfieldvalue; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE customfieldvalue (
    id bigint NOT NULL,
    issue bigint,
    customfield bigint,
    parentkey character varying(255),
    stringvalue character varying(255),
    numbervalue double precision,
    textvalue text,
    datevalue timestamp with time zone,
    valuetype character varying(255)
);


ALTER TABLE customfieldvalue OWNER TO jira;

--
-- Name: customfieldvalue_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE customfieldvalue_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE customfieldvalue_id_seq OWNER TO jira;

--
-- Name: customfieldvalue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE customfieldvalue_id_seq OWNED BY customfieldvalue.id;


--
-- Name: cwd_application; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_application (
    id bigint NOT NULL,
    application_name character varying(255),
    lower_application_name character varying(255),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    active integer,
    description character varying(255),
    application_type character varying(255),
    credential character varying(255)
);


ALTER TABLE cwd_application OWNER TO jira;

--
-- Name: cwd_application_address; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_application_address (
    application_id bigint NOT NULL,
    remote_address character varying(255) NOT NULL,
    encoded_address_binary character varying(255),
    remote_address_mask integer
);


ALTER TABLE cwd_application_address OWNER TO jira;

--
-- Name: cwd_application_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_application_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_application_id_seq OWNER TO jira;

--
-- Name: cwd_application_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_application_id_seq OWNED BY cwd_application.id;


--
-- Name: cwd_directory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_directory (
    id bigint NOT NULL,
    directory_name character varying(255),
    lower_directory_name character varying(255),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    active integer,
    description character varying(255),
    impl_class character varying(255),
    lower_impl_class character varying(255),
    directory_type character varying(60),
    directory_position bigint
);


ALTER TABLE cwd_directory OWNER TO jira;

--
-- Name: cwd_directory_attribute; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_directory_attribute (
    directory_id bigint NOT NULL,
    attribute_name character varying(255) NOT NULL,
    attribute_value character varying(255)
);


ALTER TABLE cwd_directory_attribute OWNER TO jira;

--
-- Name: cwd_directory_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_directory_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_directory_id_seq OWNER TO jira;

--
-- Name: cwd_directory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_directory_id_seq OWNED BY cwd_directory.id;


--
-- Name: cwd_directory_operation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_directory_operation (
    directory_id bigint NOT NULL,
    operation_type character varying(60) NOT NULL
);


ALTER TABLE cwd_directory_operation OWNER TO jira;

--
-- Name: cwd_group; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_group (
    id bigint NOT NULL,
    group_name character varying(255),
    lower_group_name character varying(255),
    active integer,
    local integer,
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    description character varying(255),
    lower_description character varying(255),
    group_type character varying(60),
    directory_id bigint
);


ALTER TABLE cwd_group OWNER TO jira;

--
-- Name: cwd_group_attributes; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_group_attributes (
    id bigint NOT NULL,
    group_id bigint,
    directory_id bigint,
    attribute_name character varying(255),
    attribute_value character varying(255),
    lower_attribute_value character varying(255)
);


ALTER TABLE cwd_group_attributes OWNER TO jira;

--
-- Name: cwd_group_attributes_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_group_attributes_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_group_attributes_id_seq OWNER TO jira;

--
-- Name: cwd_group_attributes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_group_attributes_id_seq OWNED BY cwd_group_attributes.id;


--
-- Name: cwd_group_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_group_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_group_id_seq OWNER TO jira;

--
-- Name: cwd_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_group_id_seq OWNED BY cwd_group.id;


--
-- Name: cwd_membership; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_membership (
    id bigint NOT NULL,
    parent_id bigint,
    child_id bigint,
    membership_type character varying(60),
    group_type character varying(60),
    parent_name character varying(255),
    lower_parent_name character varying(255),
    child_name character varying(255),
    lower_child_name character varying(255),
    directory_id bigint
);


ALTER TABLE cwd_membership OWNER TO jira;

--
-- Name: cwd_membership_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_membership_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_membership_id_seq OWNER TO jira;

--
-- Name: cwd_membership_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_membership_id_seq OWNED BY cwd_membership.id;


--
-- Name: cwd_user; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_user (
    id bigint NOT NULL,
    directory_id bigint,
    user_name character varying(255),
    lower_user_name character varying(255),
    active integer,
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    first_name character varying(255),
    lower_first_name character varying(255),
    last_name character varying(255),
    lower_last_name character varying(255),
    display_name character varying(255),
    lower_display_name character varying(255),
    email_address character varying(255),
    lower_email_address character varying(255),
    credential character varying(255),
    deleted_externally integer,
    external_id character varying(255),
    localservicedeskuser integer
);


ALTER TABLE cwd_user OWNER TO jira;

--
-- Name: cwd_user_attributes; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE cwd_user_attributes (
    id bigint NOT NULL,
    user_id bigint,
    directory_id bigint,
    attribute_name character varying(255),
    attribute_value character varying(255),
    lower_attribute_value character varying(255)
);


ALTER TABLE cwd_user_attributes OWNER TO jira;

--
-- Name: cwd_user_attributes_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_user_attributes_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_user_attributes_id_seq OWNER TO jira;

--
-- Name: cwd_user_attributes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_user_attributes_id_seq OWNED BY cwd_user_attributes.id;


--
-- Name: cwd_user_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE cwd_user_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cwd_user_id_seq OWNER TO jira;

--
-- Name: cwd_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE cwd_user_id_seq OWNED BY cwd_user.id;


--
-- Name: deadletter; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE deadletter (
    id bigint NOT NULL,
    message_id character varying(255),
    last_seen bigint,
    mail_server_id bigint,
    folder_name character varying(255)
);


ALTER TABLE deadletter OWNER TO jira;

--
-- Name: deadletter_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE deadletter_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE deadletter_id_seq OWNER TO jira;

--
-- Name: deadletter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE deadletter_id_seq OWNED BY deadletter.id;


--
-- Name: draftworkflowscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE draftworkflowscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    workflow_scheme_id bigint,
    last_modified_date timestamp with time zone,
    last_modified_user character varying(255)
);


ALTER TABLE draftworkflowscheme OWNER TO jira;

--
-- Name: draftworkflowscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE draftworkflowscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE draftworkflowscheme_id_seq OWNER TO jira;

--
-- Name: draftworkflowscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE draftworkflowscheme_id_seq OWNED BY draftworkflowscheme.id;


--
-- Name: draftworkflowschemeentity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE draftworkflowschemeentity (
    id bigint NOT NULL,
    scheme bigint,
    workflow character varying(255),
    issuetype character varying(255)
);


ALTER TABLE draftworkflowschemeentity OWNER TO jira;

--
-- Name: draftworkflowschemeentity_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE draftworkflowschemeentity_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE draftworkflowschemeentity_id_seq OWNER TO jira;

--
-- Name: draftworkflowschemeentity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE draftworkflowschemeentity_id_seq OWNED BY draftworkflowschemeentity.id;


--
-- Name: entity_property; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE entity_property (
    id bigint NOT NULL,
    entity_name character varying(255),
    entity_id bigint,
    property_key character varying(255),
    created timestamp with time zone,
    updated timestamp with time zone,
    json_value text
);


ALTER TABLE entity_property OWNER TO jira;

--
-- Name: entity_property_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE entity_property_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_property_id_seq OWNER TO jira;

--
-- Name: entity_property_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE entity_property_id_seq OWNED BY entity_property.id;


--
-- Name: entity_property_index; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE entity_property_index (
    id bigint NOT NULL,
    entity_type bigint,
    entity_id bigint,
    property_key character varying(255),
    property_path character varying(255)
);


ALTER TABLE entity_property_index OWNER TO jira;

--
-- Name: entity_property_index_document; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE entity_property_index_document (
    id bigint NOT NULL,
    plugin_key character varying(255),
    module_key character varying(255),
    entity_key character varying(255),
    updated timestamp with time zone,
    document text
);


ALTER TABLE entity_property_index_document OWNER TO jira;

--
-- Name: entity_property_index_document_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE entity_property_index_document_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_property_index_document_id_seq OWNER TO jira;

--
-- Name: entity_property_index_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE entity_property_index_document_id_seq OWNED BY entity_property_index_document.id;


--
-- Name: entity_property_index_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE entity_property_index_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_property_index_id_seq OWNER TO jira;

--
-- Name: entity_property_index_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE entity_property_index_id_seq OWNED BY entity_property_index.id;


--
-- Name: entity_property_value; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE entity_property_value (
    id bigint NOT NULL,
    property_index_id bigint,
    value_date timestamp with time zone,
    value_string character varying(255),
    value_number numeric(24,6),
    value_text text,
    tokens tsvector
);


ALTER TABLE entity_property_value OWNER TO jira;

--
-- Name: entity_property_value_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE entity_property_value_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_property_value_id_seq OWNER TO jira;

--
-- Name: entity_property_value_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE entity_property_value_id_seq OWNED BY entity_property_value.id;


--
-- Name: entity_translation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE entity_translation (
    id bigint NOT NULL,
    entity_name character varying(255),
    entity_id bigint,
    locale character varying(60),
    trans_name character varying(255),
    trans_desc character varying(255)
);


ALTER TABLE entity_translation OWNER TO jira;

--
-- Name: entity_translation_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE entity_translation_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_translation_id_seq OWNER TO jira;

--
-- Name: entity_translation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE entity_translation_id_seq OWNED BY entity_translation.id;


--
-- Name: external_entities; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE external_entities (
    id bigint NOT NULL,
    name character varying(255),
    entitytype character varying(255)
);


ALTER TABLE external_entities OWNER TO jira;

--
-- Name: external_entities_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE external_entities_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE external_entities_id_seq OWNER TO jira;

--
-- Name: external_entities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE external_entities_id_seq OWNED BY external_entities.id;


--
-- Name: externalgadget; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE externalgadget (
    id bigint NOT NULL,
    gadget_xml text
);


ALTER TABLE externalgadget OWNER TO jira;

--
-- Name: externalgadget_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE externalgadget_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE externalgadget_id_seq OWNER TO jira;

--
-- Name: externalgadget_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE externalgadget_id_seq OWNED BY externalgadget.id;


--
-- Name: favouriteassociations; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE favouriteassociations (
    id bigint NOT NULL,
    username character varying(255),
    entitytype character varying(60),
    entityid bigint,
    sequence bigint
);


ALTER TABLE favouriteassociations OWNER TO jira;

--
-- Name: favouriteassociations_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE favouriteassociations_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE favouriteassociations_id_seq OWNER TO jira;

--
-- Name: favouriteassociations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE favouriteassociations_id_seq OWNED BY favouriteassociations.id;


--
-- Name: feature; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE feature (
    id bigint NOT NULL,
    feature_name character varying(255),
    feature_type character varying(10),
    user_key character varying(255)
);


ALTER TABLE feature OWNER TO jira;

--
-- Name: feature_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE feature_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE feature_id_seq OWNER TO jira;

--
-- Name: feature_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE feature_id_seq OWNED BY feature.id;


--
-- Name: fieldconfigscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldconfigscheme (
    id bigint NOT NULL,
    configname character varying(255),
    description text,
    fieldid character varying(60),
    customfield bigint,
    scope character(1)
);


ALTER TABLE fieldconfigscheme OWNER TO jira;

--
-- Name: fieldconfigscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldconfigscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldconfigscheme_id_seq OWNER TO jira;

--
-- Name: fieldconfigscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldconfigscheme_id_seq OWNED BY fieldconfigscheme.id;


--
-- Name: fieldconfigschemeissuetype; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldconfigschemeissuetype (
    id bigint NOT NULL,
    issuetype character varying(255),
    fieldconfigscheme bigint,
    fieldconfiguration bigint
);


ALTER TABLE fieldconfigschemeissuetype OWNER TO jira;

--
-- Name: fieldconfigschemeissuetype_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldconfigschemeissuetype_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldconfigschemeissuetype_id_seq OWNER TO jira;

--
-- Name: fieldconfigschemeissuetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldconfigschemeissuetype_id_seq OWNED BY fieldconfigschemeissuetype.id;


--
-- Name: fieldconfiguration; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldconfiguration (
    id bigint NOT NULL,
    configname character varying(255),
    description text,
    fieldid character varying(60),
    customfield bigint,
    scope character(1)
);


ALTER TABLE fieldconfiguration OWNER TO jira;

--
-- Name: fieldconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldconfiguration_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldconfiguration_id_seq OWNER TO jira;

--
-- Name: fieldconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldconfiguration_id_seq OWNED BY fieldconfiguration.id;


--
-- Name: fieldlayout; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldlayout (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    layout_type character varying(255),
    layoutscheme bigint
);


ALTER TABLE fieldlayout OWNER TO jira;

--
-- Name: fieldlayout_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldlayout_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldlayout_id_seq OWNER TO jira;

--
-- Name: fieldlayout_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldlayout_id_seq OWNED BY fieldlayout.id;


--
-- Name: fieldlayoutitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldlayoutitem (
    id bigint NOT NULL,
    fieldlayout bigint,
    fieldidentifier character varying(255),
    description text,
    verticalposition bigint,
    ishidden character varying(60),
    isrequired character varying(60),
    renderertype character varying(255)
);


ALTER TABLE fieldlayoutitem OWNER TO jira;

--
-- Name: fieldlayoutitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldlayoutitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldlayoutitem_id_seq OWNER TO jira;

--
-- Name: fieldlayoutitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldlayoutitem_id_seq OWNED BY fieldlayoutitem.id;


--
-- Name: fieldlayoutscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldlayoutscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE fieldlayoutscheme OWNER TO jira;

--
-- Name: fieldlayoutscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldlayoutscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldlayoutscheme_id_seq OWNER TO jira;

--
-- Name: fieldlayoutscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldlayoutscheme_id_seq OWNED BY fieldlayoutscheme.id;


--
-- Name: fieldlayoutschemeassociation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldlayoutschemeassociation (
    id bigint NOT NULL,
    issuetype character varying(255),
    project bigint,
    fieldlayoutscheme bigint
);


ALTER TABLE fieldlayoutschemeassociation OWNER TO jira;

--
-- Name: fieldlayoutschemeassociation_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldlayoutschemeassociation_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldlayoutschemeassociation_id_seq OWNER TO jira;

--
-- Name: fieldlayoutschemeassociation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldlayoutschemeassociation_id_seq OWNED BY fieldlayoutschemeassociation.id;


--
-- Name: fieldlayoutschemeentity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldlayoutschemeentity (
    id bigint NOT NULL,
    scheme bigint,
    issuetype character varying(255),
    fieldlayout bigint
);


ALTER TABLE fieldlayoutschemeentity OWNER TO jira;

--
-- Name: fieldlayoutschemeentity_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldlayoutschemeentity_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldlayoutschemeentity_id_seq OWNER TO jira;

--
-- Name: fieldlayoutschemeentity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldlayoutschemeentity_id_seq OWNED BY fieldlayoutschemeentity.id;


--
-- Name: fieldscope; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscope (
    project_id bigint NOT NULL,
    issue_type_id character varying(60) NOT NULL,
    field_id character varying(255) NOT NULL,
    in_scope boolean NOT NULL
);


ALTER TABLE fieldscope OWNER TO jira;

--
-- Name: fieldscreen; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscreen (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    scope character(1)
);


ALTER TABLE fieldscreen OWNER TO jira;

--
-- Name: fieldscreen_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldscreen_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldscreen_id_seq OWNER TO jira;

--
-- Name: fieldscreen_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldscreen_id_seq OWNED BY fieldscreen.id;


--
-- Name: fieldscreenlayoutitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscreenlayoutitem (
    id bigint NOT NULL,
    fieldidentifier character varying(255),
    sequence bigint,
    fieldscreentab bigint
);


ALTER TABLE fieldscreenlayoutitem OWNER TO jira;

--
-- Name: fieldscreenlayoutitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldscreenlayoutitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldscreenlayoutitem_id_seq OWNER TO jira;

--
-- Name: fieldscreenlayoutitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldscreenlayoutitem_id_seq OWNED BY fieldscreenlayoutitem.id;


--
-- Name: fieldscreenscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscreenscheme (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    scope character(1)
);


ALTER TABLE fieldscreenscheme OWNER TO jira;

--
-- Name: fieldscreenscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldscreenscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldscreenscheme_id_seq OWNER TO jira;

--
-- Name: fieldscreenscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldscreenscheme_id_seq OWNED BY fieldscreenscheme.id;


--
-- Name: fieldscreenschemeitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscreenschemeitem (
    id bigint NOT NULL,
    operation bigint,
    fieldscreen bigint,
    fieldscreenscheme bigint
);


ALTER TABLE fieldscreenschemeitem OWNER TO jira;

--
-- Name: fieldscreenschemeitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldscreenschemeitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldscreenschemeitem_id_seq OWNER TO jira;

--
-- Name: fieldscreenschemeitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldscreenschemeitem_id_seq OWNED BY fieldscreenschemeitem.id;


--
-- Name: fieldscreentab; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fieldscreentab (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    sequence bigint,
    fieldscreen bigint
);


ALTER TABLE fieldscreentab OWNER TO jira;

--
-- Name: fieldscreentab_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fieldscreentab_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fieldscreentab_id_seq OWNER TO jira;

--
-- Name: fieldscreentab_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fieldscreentab_id_seq OWNED BY fieldscreentab.id;


--
-- Name: fileattachment; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE fileattachment (
    id bigint NOT NULL,
    issueid bigint,
    mimetype character varying(255),
    filename character varying(255),
    created timestamp with time zone,
    filesize bigint,
    author character varying(255),
    zip integer,
    thumbnailable integer
);


ALTER TABLE fileattachment OWNER TO jira;

--
-- Name: fileattachment_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE fileattachment_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE fileattachment_id_seq OWNER TO jira;

--
-- Name: fileattachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE fileattachment_id_seq OWNED BY fileattachment.id;


--
-- Name: filtersubscription; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE filtersubscription (
    id bigint NOT NULL,
    filter_i_d bigint,
    username character varying(60),
    groupname character varying(60),
    last_run timestamp with time zone,
    email_on_empty character varying(10)
);


ALTER TABLE filtersubscription OWNER TO jira;

--
-- Name: filtersubscription_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE filtersubscription_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE filtersubscription_id_seq OWNER TO jira;

--
-- Name: filtersubscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE filtersubscription_id_seq OWNED BY filtersubscription.id;


--
-- Name: flyway_schema_version_16a450; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_16a450 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_16a450 OWNER TO jira;

--
-- Name: flyway_schema_version_182c39; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_182c39 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_182c39 OWNER TO jira;

--
-- Name: flyway_schema_version_21d670; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_21d670 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_21d670 OWNER TO jira;

--
-- Name: flyway_schema_version_3b1893; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_3b1893 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_3b1893 OWNER TO jira;

--
-- Name: flyway_schema_version_550953; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_550953 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_550953 OWNER TO jira;

--
-- Name: flyway_schema_version_563aee; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_563aee (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_563aee OWNER TO jira;

--
-- Name: flyway_schema_version_575bf5; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_575bf5 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_575bf5 OWNER TO jira;

--
-- Name: flyway_schema_version_587b34; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_587b34 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_587b34 OWNER TO jira;

--
-- Name: flyway_schema_version_60db71; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_60db71 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_60db71 OWNER TO jira;

--
-- Name: flyway_schema_version_a0b856; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_a0b856 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_a0b856 OWNER TO jira;

--
-- Name: flyway_schema_version_b59607; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_b59607 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_b59607 OWNER TO jira;

--
-- Name: flyway_schema_version_c18b68; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_c18b68 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_c18b68 OWNER TO jira;

--
-- Name: flyway_schema_version_deb285; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_deb285 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_deb285 OWNER TO jira;

--
-- Name: flyway_schema_version_ec1a8f; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_ec1a8f (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_ec1a8f OWNER TO jira;

--
-- Name: flyway_schema_version_ecd6b3; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_ecd6b3 (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_ecd6b3 OWNER TO jira;

--
-- Name: flyway_schema_version_f4ed3a; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_f4ed3a (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_f4ed3a OWNER TO jira;

--
-- Name: flyway_schema_version_platform; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_platform (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_platform OWNER TO jira;

--
-- Name: flyway_schema_version_plugins; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE flyway_schema_version_plugins (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE flyway_schema_version_plugins OWNER TO jira;

--
-- Name: gadgetuserpreference; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE gadgetuserpreference (
    id bigint NOT NULL,
    portletconfiguration bigint,
    userprefkey character varying(255),
    userprefvalue text
);


ALTER TABLE gadgetuserpreference OWNER TO jira;

--
-- Name: gadgetuserpreference_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE gadgetuserpreference_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE gadgetuserpreference_id_seq OWNER TO jira;

--
-- Name: gadgetuserpreference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE gadgetuserpreference_id_seq OWNED BY gadgetuserpreference.id;


--
-- Name: genericconfiguration; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE genericconfiguration (
    id bigint NOT NULL,
    datatype character varying(60),
    datakey character varying(60),
    xmlvalue text
);


ALTER TABLE genericconfiguration OWNER TO jira;

--
-- Name: genericconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE genericconfiguration_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE genericconfiguration_id_seq OWNER TO jira;

--
-- Name: genericconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE genericconfiguration_id_seq OWNED BY genericconfiguration.id;


--
-- Name: globalpermissionentry; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE globalpermissionentry (
    id bigint NOT NULL,
    permission character varying(255),
    group_id character varying(255)
);


ALTER TABLE globalpermissionentry OWNER TO jira;

--
-- Name: globalpermissionentry_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE globalpermissionentry_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE globalpermissionentry_id_seq OWNER TO jira;

--
-- Name: globalpermissionentry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE globalpermissionentry_id_seq OWNED BY globalpermissionentry.id;


--
-- Name: groupbase; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE groupbase (
    id bigint NOT NULL,
    groupname character varying(255)
);


ALTER TABLE groupbase OWNER TO jira;

--
-- Name: groupbase_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE groupbase_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE groupbase_id_seq OWNER TO jira;

--
-- Name: groupbase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE groupbase_id_seq OWNED BY groupbase.id;


--
-- Name: issue_field_option; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issue_field_option (
    id bigint NOT NULL,
    option_id bigint,
    field_key character varying(255),
    value character varying(255),
    properties text,
    not_selectable character(1)
);


ALTER TABLE issue_field_option OWNER TO jira;

--
-- Name: issue_field_option_attr; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issue_field_option_attr (
    id bigint NOT NULL,
    scope_id bigint NOT NULL,
    attribute character varying(60)
);


ALTER TABLE issue_field_option_attr OWNER TO jira;

--
-- Name: issue_field_option_attr_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issue_field_option_attr_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issue_field_option_attr_id_seq OWNER TO jira;

--
-- Name: issue_field_option_attr_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issue_field_option_attr_id_seq OWNED BY issue_field_option_attr.id;


--
-- Name: issue_field_option_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issue_field_option_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issue_field_option_id_seq OWNER TO jira;

--
-- Name: issue_field_option_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issue_field_option_id_seq OWNED BY issue_field_option.id;


--
-- Name: issue_field_option_scope; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issue_field_option_scope (
    id bigint NOT NULL,
    option_id bigint,
    entity_id character varying(255),
    scope_type character varying(255)
);


ALTER TABLE issue_field_option_scope OWNER TO jira;

--
-- Name: issue_field_option_scope_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issue_field_option_scope_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issue_field_option_scope_id_seq OWNER TO jira;

--
-- Name: issue_field_option_scope_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issue_field_option_scope_id_seq OWNED BY issue_field_option_scope.id;


--
-- Name: issuelink; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuelink (
    id bigint NOT NULL,
    linktype bigint,
    source bigint,
    destination bigint,
    sequence bigint
);


ALTER TABLE issuelink OWNER TO jira;

--
-- Name: issuelink_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuelink_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuelink_id_seq OWNER TO jira;

--
-- Name: issuelink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuelink_id_seq OWNED BY issuelink.id;


--
-- Name: issuelinktype; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuelinktype (
    id bigint NOT NULL,
    linkname character varying(255),
    inward character varying(255),
    outward character varying(255),
    pstyle character varying(60)
);


ALTER TABLE issuelinktype OWNER TO jira;

--
-- Name: issuelinktype_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuelinktype_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuelinktype_id_seq OWNER TO jira;

--
-- Name: issuelinktype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuelinktype_id_seq OWNED BY issuelinktype.id;


--
-- Name: issuesecurityscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuesecurityscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    defaultlevel bigint,
    scope character(1)
);


ALTER TABLE issuesecurityscheme OWNER TO jira;

--
-- Name: issuesecurityscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuesecurityscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuesecurityscheme_id_seq OWNER TO jira;

--
-- Name: issuesecurityscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuesecurityscheme_id_seq OWNED BY issuesecurityscheme.id;


--
-- Name: issuestatus; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuestatus (
    id character varying(60) NOT NULL,
    sequence bigint,
    pname character varying(60),
    description text,
    iconurl character varying(255),
    statuscategory bigint
);


ALTER TABLE issuestatus OWNER TO jira;

--
-- Name: issuestatus_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuestatus_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuestatus_id_seq OWNER TO jira;

--
-- Name: issuestatus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuestatus_id_seq OWNED BY issuestatus.id;


--
-- Name: issuetype; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuetype (
    id character varying(60) NOT NULL,
    sequence bigint,
    pname character varying(60),
    pstyle character varying(60),
    description text,
    iconurl character varying(255),
    avatar bigint,
    scope character(1)
);


ALTER TABLE issuetype OWNER TO jira;

--
-- Name: issuetype_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuetype_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuetype_id_seq OWNER TO jira;

--
-- Name: issuetype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuetype_id_seq OWNED BY issuetype.id;


--
-- Name: issuetypescreenscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuetypescreenscheme (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    scope character(1)
);


ALTER TABLE issuetypescreenscheme OWNER TO jira;

--
-- Name: issuetypescreenscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuetypescreenscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuetypescreenscheme_id_seq OWNER TO jira;

--
-- Name: issuetypescreenscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuetypescreenscheme_id_seq OWNED BY issuetypescreenscheme.id;


--
-- Name: issuetypescreenschemeentity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE issuetypescreenschemeentity (
    id bigint NOT NULL,
    issuetype character varying(255),
    scheme bigint,
    fieldscreenscheme bigint
);


ALTER TABLE issuetypescreenschemeentity OWNER TO jira;

--
-- Name: issuetypescreenschemeentity_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE issuetypescreenschemeentity_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE issuetypescreenschemeentity_id_seq OWNER TO jira;

--
-- Name: issuetypescreenschemeentity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE issuetypescreenschemeentity_id_seq OWNED BY issuetypescreenschemeentity.id;


--
-- Name: jiraaction; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraaction (
    id bigint NOT NULL,
    issueid bigint,
    author character varying(255),
    actiontype character varying(255),
    actionlevel character varying(255),
    rolelevel bigint,
    actionbody text,
    created timestamp with time zone,
    updateauthor character varying(255),
    updated timestamp with time zone,
    actionnum bigint,
    tokens tsvector
);


ALTER TABLE jiraaction OWNER TO jira;

--
-- Name: jiraaction_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraaction_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraaction_id_seq OWNER TO jira;

--
-- Name: jiraaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraaction_id_seq OWNED BY jiraaction.id;


--
-- Name: jiradraftworkflows; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiradraftworkflows (
    id bigint NOT NULL,
    parentname character varying(255),
    descriptor text
);


ALTER TABLE jiradraftworkflows OWNER TO jira;

--
-- Name: jiradraftworkflows_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiradraftworkflows_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiradraftworkflows_id_seq OWNER TO jira;

--
-- Name: jiradraftworkflows_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiradraftworkflows_id_seq OWNED BY jiradraftworkflows.id;


--
-- Name: jiraeventtype; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraeventtype (
    id bigint NOT NULL,
    template_id bigint,
    name character varying(255),
    description text,
    event_type character varying(60)
);


ALTER TABLE jiraeventtype OWNER TO jira;

--
-- Name: jiraeventtype_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraeventtype_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraeventtype_id_seq OWNER TO jira;

--
-- Name: jiraeventtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraeventtype_id_seq OWNED BY jiraeventtype.id;


--
-- Name: jiraissue; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraissue (
    id bigint NOT NULL,
    pkey character varying(255),
    issuenum bigint,
    project bigint,
    reporter character varying(255),
    assignee character varying(255),
    creator character varying(255),
    issuetype character varying(255),
    summary character varying(255),
    description text,
    environment text,
    priority character varying(255),
    resolution character varying(255),
    issuestatus character varying(255),
    created timestamp with time zone,
    updated timestamp with time zone,
    duedate timestamp with time zone,
    resolutiondate timestamp with time zone,
    votes bigint,
    watches bigint,
    timeoriginalestimate bigint,
    timeestimate bigint,
    timespent bigint,
    workflow_id bigint,
    security bigint,
    fixfor bigint,
    component bigint
);


ALTER TABLE jiraissue OWNER TO jira;

--
-- Name: jiraissue_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraissue_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraissue_id_seq OWNER TO jira;

--
-- Name: jiraissue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraissue_id_seq OWNED BY jiraissue.id;


--
-- Name: jiraissuetokens; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraissuetokens (
    issueid bigint NOT NULL,
    field character varying(60) NOT NULL,
    tokens tsvector
);


ALTER TABLE jiraissuetokens OWNER TO jira;

--
-- Name: jiraperms; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraperms (
    id bigint NOT NULL,
    permtype bigint,
    projectid bigint,
    groupname character varying(255)
);


ALTER TABLE jiraperms OWNER TO jira;

--
-- Name: jiraperms_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraperms_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraperms_id_seq OWNER TO jira;

--
-- Name: jiraperms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraperms_id_seq OWNED BY jiraperms.id;


--
-- Name: jiraworkflows; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraworkflows (
    id bigint NOT NULL,
    workflowname character varying(255),
    creatorname character varying(255),
    descriptor text,
    islocked character varying(60),
    scope character(1)
);


ALTER TABLE jiraworkflows OWNER TO jira;

--
-- Name: jiraworkflows_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraworkflows_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraworkflows_id_seq OWNER TO jira;

--
-- Name: jiraworkflows_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraworkflows_id_seq OWNED BY jiraworkflows.id;


--
-- Name: jiraworkflowstatuses; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jiraworkflowstatuses (
    id bigint NOT NULL,
    status character varying(255),
    parentname character varying(255)
);


ALTER TABLE jiraworkflowstatuses OWNER TO jira;

--
-- Name: jiraworkflowstatuses_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE jiraworkflowstatuses_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE jiraworkflowstatuses_id_seq OWNER TO jira;

--
-- Name: jiraworkflowstatuses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE jiraworkflowstatuses_id_seq OWNED BY jiraworkflowstatuses.id;


--
-- Name: jquartz_blob_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_blob_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


ALTER TABLE jquartz_blob_triggers OWNER TO jira;

--
-- Name: jquartz_calendars; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_calendars (
    sched_name character varying(120),
    calendar_name character varying(200) NOT NULL,
    calendar bytea
);


ALTER TABLE jquartz_calendars OWNER TO jira;

--
-- Name: jquartz_cron_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_cron_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120),
    time_zone_id character varying(80)
);


ALTER TABLE jquartz_cron_triggers OWNER TO jira;

--
-- Name: jquartz_fired_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_fired_triggers (
    sched_name character varying(120),
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200),
    trigger_group character varying(200),
    is_volatile boolean,
    instance_name character varying(200),
    fired_time bigint,
    sched_time bigint,
    priority integer,
    state character varying(16),
    job_name character varying(200),
    job_group character varying(200),
    is_stateful boolean,
    is_nonconcurrent boolean,
    is_update_data boolean,
    requests_recovery boolean
);


ALTER TABLE jquartz_fired_triggers OWNER TO jira;

--
-- Name: jquartz_job_details; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_job_details (
    sched_name character varying(120),
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250),
    is_durable boolean,
    is_volatile boolean,
    is_stateful boolean,
    is_nonconcurrent boolean,
    is_update_data boolean,
    requests_recovery boolean,
    job_data bytea
);


ALTER TABLE jquartz_job_details OWNER TO jira;

--
-- Name: jquartz_job_listeners; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_job_listeners (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    job_listener character varying(200) NOT NULL
);


ALTER TABLE jquartz_job_listeners OWNER TO jira;

--
-- Name: jquartz_locks; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_locks (
    sched_name character varying(120),
    lock_name character varying(40) NOT NULL
);


ALTER TABLE jquartz_locks OWNER TO jira;

--
-- Name: jquartz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_paused_trigger_grps (
    sched_name character varying(120),
    trigger_group character varying(200) NOT NULL
);


ALTER TABLE jquartz_paused_trigger_grps OWNER TO jira;

--
-- Name: jquartz_scheduler_state; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_scheduler_state (
    sched_name character varying(120),
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint,
    checkin_interval bigint
);


ALTER TABLE jquartz_scheduler_state OWNER TO jira;

--
-- Name: jquartz_simple_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_simple_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint,
    repeat_interval bigint,
    times_triggered bigint
);


ALTER TABLE jquartz_simple_triggers OWNER TO jira;

--
-- Name: jquartz_simprop_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_simprop_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);


ALTER TABLE jquartz_simprop_triggers OWNER TO jira;

--
-- Name: jquartz_trigger_listeners; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_trigger_listeners (
    trigger_name character varying(200),
    trigger_group character varying(200) NOT NULL,
    trigger_listener character varying(200) NOT NULL
);


ALTER TABLE jquartz_trigger_listeners OWNER TO jira;

--
-- Name: jquartz_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE jquartz_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_volatile boolean,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16),
    trigger_type character varying(8),
    start_time bigint,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr numeric(4,0),
    job_data bytea
);


ALTER TABLE jquartz_triggers OWNER TO jira;

--
-- Name: label; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE label (
    id bigint NOT NULL,
    fieldid bigint,
    issue bigint,
    label character varying(255)
);


ALTER TABLE label OWNER TO jira;

--
-- Name: label_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE label_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE label_id_seq OWNER TO jira;

--
-- Name: label_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE label_id_seq OWNED BY label.id;


--
-- Name: legacy_store_attachment_status; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE legacy_store_attachment_status (
    attachment bigint NOT NULL,
    not_present_in_store integer
);


ALTER TABLE legacy_store_attachment_status OWNER TO jira;

--
-- Name: legacy_store_avatar_status; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE legacy_store_avatar_status (
    avatar bigint NOT NULL,
    not_present_in_store integer
);


ALTER TABLE legacy_store_avatar_status OWNER TO jira;

--
-- Name: licenserolesdefault; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE licenserolesdefault (
    id bigint NOT NULL,
    license_role_name character varying(255)
);


ALTER TABLE licenserolesdefault OWNER TO jira;

--
-- Name: licenserolesdefault_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE licenserolesdefault_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE licenserolesdefault_id_seq OWNER TO jira;

--
-- Name: licenserolesdefault_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE licenserolesdefault_id_seq OWNED BY licenserolesdefault.id;


--
-- Name: licenserolesgroup; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE licenserolesgroup (
    id bigint NOT NULL,
    license_role_name character varying(255),
    group_id character varying(255),
    primary_group character(1)
);


ALTER TABLE licenserolesgroup OWNER TO jira;

--
-- Name: licenserolesgroup_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE licenserolesgroup_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE licenserolesgroup_id_seq OWNER TO jira;

--
-- Name: licenserolesgroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE licenserolesgroup_id_seq OWNED BY licenserolesgroup.id;


--
-- Name: listenerconfig; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE listenerconfig (
    id bigint NOT NULL,
    clazz character varying(255),
    listenername character varying(255)
);


ALTER TABLE listenerconfig OWNER TO jira;

--
-- Name: listenerconfig_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE listenerconfig_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE listenerconfig_id_seq OWNER TO jira;

--
-- Name: listenerconfig_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE listenerconfig_id_seq OWNED BY listenerconfig.id;


--
-- Name: mailserver; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE mailserver (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    mailfrom character varying(255),
    prefix character varying(60),
    smtp_port character varying(60),
    protocol character varying(60),
    server_type character varying(60),
    servername character varying(255),
    jndilocation character varying(255),
    mailusername character varying(255),
    mailpassword character varying(255),
    istlsrequired character varying(60),
    timeout bigint,
    socks_port character varying(60),
    socks_host character varying(60)
);


ALTER TABLE mailserver OWNER TO jira;

--
-- Name: mailserver_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE mailserver_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE mailserver_id_seq OWNER TO jira;

--
-- Name: mailserver_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE mailserver_id_seq OWNED BY mailserver.id;


--
-- Name: managedconfigurationitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE managedconfigurationitem (
    id bigint NOT NULL,
    item_id character varying(255),
    item_type character varying(255),
    managed character varying(10),
    access_level character varying(255),
    source character varying(255),
    description_key character varying(255)
);


ALTER TABLE managedconfigurationitem OWNER TO jira;

--
-- Name: managedconfigurationitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE managedconfigurationitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE managedconfigurationitem_id_seq OWNER TO jira;

--
-- Name: managedconfigurationitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE managedconfigurationitem_id_seq OWNED BY managedconfigurationitem.id;


--
-- Name: media_api_credentials; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE media_api_credentials (
    id bigint NOT NULL,
    client_id character varying(255),
    client_key character varying(255),
    client_title character varying(255),
    client_description character varying(255)
);


ALTER TABLE media_api_credentials OWNER TO jira;

--
-- Name: media_api_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE media_api_credentials_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE media_api_credentials_id_seq OWNER TO jira;

--
-- Name: media_api_credentials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE media_api_credentials_id_seq OWNED BY media_api_credentials.id;


--
-- Name: media_store_attachments; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE media_store_attachments (
    attachment bigint NOT NULL,
    media_store_id character varying(60),
    thumbnail_generated integer
);


ALTER TABLE media_store_attachments OWNER TO jira;

--
-- Name: media_store_avatars; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE media_store_avatars (
    avatar bigint NOT NULL,
    size character varying(60) NOT NULL,
    media_store_id character varying(60)
);


ALTER TABLE media_store_avatars OWNER TO jira;

--
-- Name: membershipbase; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE membershipbase (
    id bigint NOT NULL,
    user_name character varying(255),
    group_name character varying(255)
);


ALTER TABLE membershipbase OWNER TO jira;

--
-- Name: membershipbase_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE membershipbase_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE membershipbase_id_seq OWNER TO jira;

--
-- Name: membershipbase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE membershipbase_id_seq OWNED BY membershipbase.id;


--
-- Name: module_status; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE module_status (
    plugin_key character varying(255) NOT NULL,
    module_key character varying(255) NOT NULL,
    status character varying(60)
);


ALTER TABLE module_status OWNER TO jira;

--
-- Name: moved_issue_key; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE moved_issue_key (
    id bigint NOT NULL,
    old_issue_key character varying(255),
    issue_id bigint
);


ALTER TABLE moved_issue_key OWNER TO jira;

--
-- Name: moved_issue_key_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE moved_issue_key_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE moved_issue_key_id_seq OWNER TO jira;

--
-- Name: moved_issue_key_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE moved_issue_key_id_seq OWNED BY moved_issue_key.id;


--
-- Name: nodeassociation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE nodeassociation (
    source_node_id bigint NOT NULL,
    source_node_entity character varying(60) NOT NULL,
    sink_node_id bigint NOT NULL,
    sink_node_entity character varying(60) NOT NULL,
    association_type character varying(60) NOT NULL,
    sequence integer
);


ALTER TABLE nodeassociation OWNER TO jira;

--
-- Name: nodeindexcounter; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE nodeindexcounter (
    id bigint NOT NULL,
    node_id character varying(60),
    sending_node_id character varying(60),
    index_operation_id bigint
);


ALTER TABLE nodeindexcounter OWNER TO jira;

--
-- Name: nodeindexcounter_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE nodeindexcounter_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE nodeindexcounter_id_seq OWNER TO jira;

--
-- Name: nodeindexcounter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE nodeindexcounter_id_seq OWNED BY nodeindexcounter.id;


--
-- Name: notification; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE notification (
    id bigint NOT NULL,
    scheme bigint,
    event character varying(60),
    event_type_id bigint,
    template_id bigint,
    notif_type character varying(60),
    notif_parameter character varying(60)
);


ALTER TABLE notification OWNER TO jira;

--
-- Name: notification_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE notification_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notification_id_seq OWNER TO jira;

--
-- Name: notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE notification_id_seq OWNED BY notification.id;


--
-- Name: notification_subscription_ec1a8f; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE notification_subscription_ec1a8f (
    id bigint NOT NULL,
    user_key character varying(255) NOT NULL,
    object_type character varying(255) NOT NULL,
    object_context character varying(255),
    rule character varying(255),
    creation_date timestamp without time zone DEFAULT timezone('utc'::text, now()) NOT NULL
);


ALTER TABLE notification_subscription_ec1a8f OWNER TO jira;

--
-- Name: notification_subscription_ec1a8f_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE notification_subscription_ec1a8f_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notification_subscription_ec1a8f_id_seq OWNER TO jira;

--
-- Name: notification_subscription_ec1a8f_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE notification_subscription_ec1a8f_id_seq OWNED BY notification_subscription_ec1a8f.id;


--
-- Name: notificationinstance; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE notificationinstance (
    id bigint NOT NULL,
    notificationtype character varying(60),
    source bigint,
    emailaddress character varying(255),
    messageid character varying(255)
);


ALTER TABLE notificationinstance OWNER TO jira;

--
-- Name: notificationinstance_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE notificationinstance_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notificationinstance_id_seq OWNER TO jira;

--
-- Name: notificationinstance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE notificationinstance_id_seq OWNED BY notificationinstance.id;


--
-- Name: notificationscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE notificationscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    scope character(1)
);


ALTER TABLE notificationscheme OWNER TO jira;

--
-- Name: notificationscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE notificationscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notificationscheme_id_seq OWNER TO jira;

--
-- Name: notificationscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE notificationscheme_id_seq OWNED BY notificationscheme.id;


--
-- Name: oauth_consumer_182c39; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauth_consumer_182c39 (
    key text NOT NULL,
    name text NOT NULL,
    public_key text,
    private_key text,
    description text,
    callback text,
    signature_method text,
    shared_secret text,
    service_name text NOT NULL
);


ALTER TABLE oauth_consumer_182c39 OWNER TO jira;

--
-- Name: oauth_consumer_token_182c39; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauth_consumer_token_182c39 (
    key text NOT NULL,
    token text NOT NULL,
    token_secret text NOT NULL,
    consumer_key text NOT NULL,
    type text NOT NULL,
    properties text
);


ALTER TABLE oauth_consumer_token_182c39 OWNER TO jira;

--
-- Name: oauth_service_provider_consumer_ecd6b3; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauth_service_provider_consumer_ecd6b3 (
    key text NOT NULL,
    name text NOT NULL,
    public_key text NOT NULL,
    description text,
    callback text,
    three_lo_allowed boolean NOT NULL,
    two_lo_allowed boolean NOT NULL,
    executing_two_lo_user text,
    two_lo_impersonation boolean NOT NULL
);


ALTER TABLE oauth_service_provider_consumer_ecd6b3 OWNER TO jira;

--
-- Name: oauth_service_provider_token_ecd6b3; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauth_service_provider_token_ecd6b3 (
    token text NOT NULL,
    token_secret text NOT NULL,
    type text NOT NULL,
    consumer_key text NOT NULL,
    properties text NOT NULL,
    auth text,
    username text,
    verifier text,
    creation_time bigint,
    time_to_live bigint,
    callback text,
    version text,
    session_handle text,
    session_creation_time bigint,
    session_last_renewal bigint,
    session_time_to_live bigint
);


ALTER TABLE oauth_service_provider_token_ecd6b3 OWNER TO jira;

--
-- Name: oauthconsumer; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauthconsumer (
    id bigint NOT NULL,
    created timestamp with time zone,
    consumername character varying(255),
    consumer_key character varying(255),
    consumerservice character varying(255),
    public_key text,
    private_key text,
    description text,
    callback text,
    signature_method character varying(60),
    shared_secret text
);


ALTER TABLE oauthconsumer OWNER TO jira;

--
-- Name: oauthconsumer_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE oauthconsumer_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE oauthconsumer_id_seq OWNER TO jira;

--
-- Name: oauthconsumer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE oauthconsumer_id_seq OWNED BY oauthconsumer.id;


--
-- Name: oauthconsumertoken; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauthconsumertoken (
    id bigint NOT NULL,
    created timestamp with time zone,
    token_key character varying(255),
    token character varying(255),
    token_secret character varying(255),
    token_type character varying(60),
    consumer_key character varying(255)
);


ALTER TABLE oauthconsumertoken OWNER TO jira;

--
-- Name: oauthconsumertoken_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE oauthconsumertoken_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE oauthconsumertoken_id_seq OWNER TO jira;

--
-- Name: oauthconsumertoken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE oauthconsumertoken_id_seq OWNED BY oauthconsumertoken.id;


--
-- Name: oauthspconsumer; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauthspconsumer (
    id bigint NOT NULL,
    created timestamp with time zone,
    consumer_key character varying(255),
    consumername character varying(255),
    public_key text,
    description text,
    callback text,
    two_l_o_allowed character varying(60),
    executing_two_l_o_user character varying(255),
    two_l_o_impersonation_allowed character varying(60),
    three_l_o_allowed character varying(60)
);


ALTER TABLE oauthspconsumer OWNER TO jira;

--
-- Name: oauthspconsumer_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE oauthspconsumer_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE oauthspconsumer_id_seq OWNER TO jira;

--
-- Name: oauthspconsumer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE oauthspconsumer_id_seq OWNED BY oauthspconsumer.id;


--
-- Name: oauthsptoken; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE oauthsptoken (
    id bigint NOT NULL,
    created timestamp with time zone,
    token character varying(255),
    token_secret character varying(255),
    token_type character varying(60),
    consumer_key character varying(255),
    username character varying(255),
    ttl bigint,
    spauth character varying(60),
    callback text,
    spverifier character varying(255),
    spversion character varying(60),
    session_handle character varying(255),
    session_creation_time timestamp with time zone,
    session_last_renewal_time timestamp with time zone,
    session_time_to_live timestamp with time zone
);


ALTER TABLE oauthsptoken OWNER TO jira;

--
-- Name: oauthsptoken_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE oauthsptoken_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE oauthsptoken_id_seq OWNER TO jira;

--
-- Name: oauthsptoken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE oauthsptoken_id_seq OWNED BY oauthsptoken.id;


--
-- Name: optionconfiguration; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE optionconfiguration (
    id bigint NOT NULL,
    fieldid character varying(60),
    optionid character varying(60),
    fieldconfig bigint,
    sequence bigint
);


ALTER TABLE optionconfiguration OWNER TO jira;

--
-- Name: optionconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE optionconfiguration_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE optionconfiguration_id_seq OWNER TO jira;

--
-- Name: optionconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE optionconfiguration_id_seq OWNED BY optionconfiguration.id;


--
-- Name: os_currentstep; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE os_currentstep (
    id bigint NOT NULL,
    entry_id bigint,
    step_id integer,
    action_id integer,
    owner character varying(255),
    start_date timestamp with time zone,
    due_date timestamp with time zone,
    finish_date timestamp with time zone,
    status character varying(60),
    caller character varying(255)
);


ALTER TABLE os_currentstep OWNER TO jira;

--
-- Name: os_currentstep_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE os_currentstep_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE os_currentstep_id_seq OWNER TO jira;

--
-- Name: os_currentstep_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE os_currentstep_id_seq OWNED BY os_currentstep.id;


--
-- Name: os_currentstep_prev; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE os_currentstep_prev (
    id bigint NOT NULL,
    previous_id bigint NOT NULL
);


ALTER TABLE os_currentstep_prev OWNER TO jira;

--
-- Name: os_historystep; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE os_historystep (
    id bigint NOT NULL,
    entry_id bigint,
    step_id integer,
    action_id integer,
    owner character varying(255),
    start_date timestamp with time zone,
    due_date timestamp with time zone,
    finish_date timestamp with time zone,
    status character varying(60),
    caller character varying(255)
);


ALTER TABLE os_historystep OWNER TO jira;

--
-- Name: os_historystep_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE os_historystep_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE os_historystep_id_seq OWNER TO jira;

--
-- Name: os_historystep_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE os_historystep_id_seq OWNED BY os_historystep.id;


--
-- Name: os_historystep_prev; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE os_historystep_prev (
    id bigint NOT NULL,
    previous_id bigint NOT NULL
);


ALTER TABLE os_historystep_prev OWNER TO jira;

--
-- Name: os_wfentry; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE os_wfentry (
    id bigint NOT NULL,
    name character varying(255),
    initialized integer,
    state integer
);


ALTER TABLE os_wfentry OWNER TO jira;

--
-- Name: os_wfentry_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE os_wfentry_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE os_wfentry_id_seq OWNER TO jira;

--
-- Name: os_wfentry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE os_wfentry_id_seq OWNED BY os_wfentry.id;


--
-- Name: permissionscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE permissionscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    scope character(1)
);


ALTER TABLE permissionscheme OWNER TO jira;

--
-- Name: permissionscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE permissionscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE permissionscheme_id_seq OWNER TO jira;

--
-- Name: permissionscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE permissionscheme_id_seq OWNED BY permissionscheme.id;


--
-- Name: pluginstate; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE pluginstate (
    pluginkey character varying(255) NOT NULL,
    pluginenabled character varying(60)
);


ALTER TABLE pluginstate OWNER TO jira;

--
-- Name: pluginversion; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE pluginversion (
    id bigint NOT NULL,
    pluginname character varying(255),
    pluginkey character varying(255),
    pluginversion character varying(255),
    created timestamp with time zone
);


ALTER TABLE pluginversion OWNER TO jira;

--
-- Name: pluginversion_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE pluginversion_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE pluginversion_id_seq OWNER TO jira;

--
-- Name: pluginversion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE pluginversion_id_seq OWNED BY pluginversion.id;


--
-- Name: portalpage; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE portalpage (
    id bigint NOT NULL,
    username character varying(255),
    pagename character varying(255),
    description character varying(255),
    sequence bigint,
    fav_count bigint,
    layout character varying(255),
    ppversion bigint
);


ALTER TABLE portalpage OWNER TO jira;

--
-- Name: portalpage_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE portalpage_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE portalpage_id_seq OWNER TO jira;

--
-- Name: portalpage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE portalpage_id_seq OWNED BY portalpage.id;


--
-- Name: portletconfiguration; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE portletconfiguration (
    id bigint NOT NULL,
    portalpage bigint,
    portlet_id character varying(255),
    column_number integer,
    positionseq integer,
    gadget_xml text,
    color character varying(255),
    dashboard_module_complete_key text,
    title character varying(255)
);


ALTER TABLE portletconfiguration OWNER TO jira;

--
-- Name: portletconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE portletconfiguration_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE portletconfiguration_id_seq OWNER TO jira;

--
-- Name: portletconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE portletconfiguration_id_seq OWNED BY portletconfiguration.id;


--
-- Name: priority; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE priority (
    id character varying(60) NOT NULL,
    sequence bigint,
    pname character varying(60),
    description text,
    iconurl character varying(255),
    status_color character varying(60)
);


ALTER TABLE priority OWNER TO jira;

--
-- Name: priority_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE priority_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE priority_id_seq OWNER TO jira;

--
-- Name: priority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE priority_id_seq OWNED BY priority.id;


--
-- Name: productlicense; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE productlicense (
    id bigint NOT NULL,
    license text
);


ALTER TABLE productlicense OWNER TO jira;

--
-- Name: productlicense_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE productlicense_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE productlicense_id_seq OWNER TO jira;

--
-- Name: productlicense_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE productlicense_id_seq OWNED BY productlicense.id;


--
-- Name: project; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE project (
    id bigint NOT NULL,
    pname character varying(255),
    url character varying(255),
    lead character varying(255),
    description text,
    pkey character varying(255),
    pcounter bigint,
    assigneetype bigint,
    avatar bigint,
    originalkey character varying(255),
    projecttype character varying(255)
);


ALTER TABLE project OWNER TO jira;

--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE project_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE project_id_seq OWNER TO jira;

--
-- Name: project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE project_id_seq OWNED BY project.id;


--
-- Name: project_key; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE project_key (
    id bigint NOT NULL,
    project_id bigint,
    project_key character varying(255)
);


ALTER TABLE project_key OWNER TO jira;

--
-- Name: project_key_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE project_key_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE project_key_id_seq OWNER TO jira;

--
-- Name: project_key_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE project_key_id_seq OWNED BY project_key.id;


--
-- Name: projectcategory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE projectcategory (
    id bigint NOT NULL,
    cname character varying(255),
    description text
);


ALTER TABLE projectcategory OWNER TO jira;

--
-- Name: projectcategory_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE projectcategory_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE projectcategory_id_seq OWNER TO jira;

--
-- Name: projectcategory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE projectcategory_id_seq OWNED BY projectcategory.id;


--
-- Name: projectchangedtime; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE projectchangedtime (
    project_id bigint NOT NULL,
    issue_changed_time timestamp with time zone
);


ALTER TABLE projectchangedtime OWNER TO jira;

--
-- Name: projectrole; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE projectrole (
    id bigint NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE projectrole OWNER TO jira;

--
-- Name: projectrole_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE projectrole_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE projectrole_id_seq OWNER TO jira;

--
-- Name: projectrole_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE projectrole_id_seq OWNED BY projectrole.id;


--
-- Name: projectroleactor; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE projectroleactor (
    id bigint NOT NULL,
    pid bigint,
    projectroleid bigint,
    roletype character varying(255),
    roletypeparameter character varying(255)
);


ALTER TABLE projectroleactor OWNER TO jira;

--
-- Name: projectroleactor_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE projectroleactor_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE projectroleactor_id_seq OWNER TO jira;

--
-- Name: projectroleactor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE projectroleactor_id_seq OWNED BY projectroleactor.id;


--
-- Name: projectversion; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE projectversion (
    id bigint NOT NULL,
    project bigint,
    vname character varying(255),
    description text,
    sequence bigint,
    released character varying(10),
    archived character varying(10),
    url character varying(255),
    startdate timestamp with time zone,
    releasedate timestamp with time zone
);


ALTER TABLE projectversion OWNER TO jira;

--
-- Name: projectversion_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE projectversion_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE projectversion_id_seq OWNER TO jira;

--
-- Name: projectversion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE projectversion_id_seq OWNED BY projectversion.id;


--
-- Name: propertydata; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertydata (
    id bigint NOT NULL,
    propertyvalue oid
);


ALTER TABLE propertydata OWNER TO jira;

--
-- Name: propertydate; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertydate (
    id bigint NOT NULL,
    propertyvalue timestamp with time zone
);


ALTER TABLE propertydate OWNER TO jira;

--
-- Name: propertydecimal; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertydecimal (
    id bigint NOT NULL,
    propertyvalue double precision
);


ALTER TABLE propertydecimal OWNER TO jira;

--
-- Name: propertyentry; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertyentry (
    id bigint NOT NULL,
    entity_name character varying(255),
    entity_id bigint,
    property_key character varying(255),
    propertytype integer
);


ALTER TABLE propertyentry OWNER TO jira;

--
-- Name: propertyentry_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE propertyentry_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE propertyentry_id_seq OWNER TO jira;

--
-- Name: propertyentry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE propertyentry_id_seq OWNED BY propertyentry.id;


--
-- Name: propertynumber; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertynumber (
    id bigint NOT NULL,
    propertyvalue bigint
);


ALTER TABLE propertynumber OWNER TO jira;

--
-- Name: propertystring; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertystring (
    id bigint NOT NULL,
    propertyvalue text
);


ALTER TABLE propertystring OWNER TO jira;

--
-- Name: propertytext; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE propertytext (
    id bigint NOT NULL,
    propertyvalue text
);


ALTER TABLE propertytext OWNER TO jira;

--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_calendars (
    id bigint,
    calendar_name character varying(255) NOT NULL,
    calendar text
);


ALTER TABLE qrtz_calendars OWNER TO jira;

--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_cron_triggers (
    id bigint NOT NULL,
    trigger_id bigint,
    cronexperssion character varying(255)
);


ALTER TABLE qrtz_cron_triggers OWNER TO jira;

--
-- Name: qrtz_cron_triggers_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_cron_triggers_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_cron_triggers_id_seq OWNER TO jira;

--
-- Name: qrtz_cron_triggers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_cron_triggers_id_seq OWNED BY qrtz_cron_triggers.id;


--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_fired_triggers (
    id bigint,
    entry_id character varying(255) NOT NULL,
    trigger_id bigint,
    trigger_listener character varying(255),
    fired_time timestamp with time zone,
    trigger_state character varying(255)
);


ALTER TABLE qrtz_fired_triggers OWNER TO jira;

--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_job_details (
    id bigint NOT NULL,
    job_name character varying(255),
    job_group character varying(255),
    class_name character varying(255),
    is_durable character varying(60),
    is_stateful character varying(60),
    requests_recovery character varying(60),
    job_data character varying(255)
);


ALTER TABLE qrtz_job_details OWNER TO jira;

--
-- Name: qrtz_job_details_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_job_details_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_job_details_id_seq OWNER TO jira;

--
-- Name: qrtz_job_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_job_details_id_seq OWNED BY qrtz_job_details.id;


--
-- Name: qrtz_job_listeners; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_job_listeners (
    id bigint NOT NULL,
    job bigint,
    job_listener character varying(255)
);


ALTER TABLE qrtz_job_listeners OWNER TO jira;

--
-- Name: qrtz_job_listeners_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_job_listeners_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_job_listeners_id_seq OWNER TO jira;

--
-- Name: qrtz_job_listeners_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_job_listeners_id_seq OWNED BY qrtz_job_listeners.id;


--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_simple_triggers (
    id bigint NOT NULL,
    trigger_id bigint,
    repeat_count integer,
    repeat_interval bigint,
    times_triggered integer
);


ALTER TABLE qrtz_simple_triggers OWNER TO jira;

--
-- Name: qrtz_simple_triggers_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_simple_triggers_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_simple_triggers_id_seq OWNER TO jira;

--
-- Name: qrtz_simple_triggers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_simple_triggers_id_seq OWNED BY qrtz_simple_triggers.id;


--
-- Name: qrtz_trigger_listeners; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_trigger_listeners (
    id bigint NOT NULL,
    trigger_id bigint,
    trigger_listener character varying(255)
);


ALTER TABLE qrtz_trigger_listeners OWNER TO jira;

--
-- Name: qrtz_trigger_listeners_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_trigger_listeners_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_trigger_listeners_id_seq OWNER TO jira;

--
-- Name: qrtz_trigger_listeners_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_trigger_listeners_id_seq OWNED BY qrtz_trigger_listeners.id;


--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE qrtz_triggers (
    id bigint NOT NULL,
    trigger_name character varying(255),
    trigger_group character varying(255),
    job bigint,
    next_fire timestamp with time zone,
    trigger_state character varying(255),
    trigger_type character varying(60),
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    calendar_name character varying(255),
    misfire_instr integer
);


ALTER TABLE qrtz_triggers OWNER TO jira;

--
-- Name: qrtz_triggers_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE qrtz_triggers_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qrtz_triggers_id_seq OWNER TO jira;

--
-- Name: qrtz_triggers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE qrtz_triggers_id_seq OWNED BY qrtz_triggers.id;


--
-- Name: reindex_component; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE reindex_component (
    id bigint NOT NULL,
    request_id bigint,
    affected_index character varying(60),
    entity_type character varying(60)
);


ALTER TABLE reindex_component OWNER TO jira;

--
-- Name: reindex_component_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE reindex_component_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE reindex_component_id_seq OWNER TO jira;

--
-- Name: reindex_component_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE reindex_component_id_seq OWNED BY reindex_component.id;


--
-- Name: reindex_request; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE reindex_request (
    id bigint NOT NULL,
    type character varying(60),
    request_time timestamp with time zone,
    start_time timestamp with time zone,
    completion_time timestamp with time zone,
    status character varying(60),
    execution_node_id character varying(60),
    query text
);


ALTER TABLE reindex_request OWNER TO jira;

--
-- Name: reindex_request_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE reindex_request_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE reindex_request_id_seq OWNER TO jira;

--
-- Name: reindex_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE reindex_request_id_seq OWNED BY reindex_request.id;


--
-- Name: remembermetoken; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE remembermetoken (
    id bigint NOT NULL,
    created timestamp with time zone,
    token character varying(255),
    username character varying(255)
);


ALTER TABLE remembermetoken OWNER TO jira;

--
-- Name: remembermetoken_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE remembermetoken_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE remembermetoken_id_seq OWNER TO jira;

--
-- Name: remembermetoken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE remembermetoken_id_seq OWNED BY remembermetoken.id;


--
-- Name: remotelink; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE remotelink (
    id bigint NOT NULL,
    issueid bigint,
    globalid character varying(255),
    title character varying(255),
    summary text,
    url text,
    iconurl text,
    icontitle text,
    relationship character varying(255),
    resolved character(1),
    statusname character varying(255),
    statusdescription text,
    statusiconurl text,
    statusicontitle text,
    statusiconlink text,
    statuscategorykey character varying(255),
    statuscategorycolorname character varying(255),
    applicationtype character varying(255),
    applicationname character varying(255)
);


ALTER TABLE remotelink OWNER TO jira;

--
-- Name: remotelink_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE remotelink_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE remotelink_id_seq OWNER TO jira;

--
-- Name: remotelink_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE remotelink_id_seq OWNED BY remotelink.id;


--
-- Name: replicatedindexoperation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE replicatedindexoperation (
    id bigint NOT NULL,
    index_time timestamp with time zone,
    node_id character varying(60),
    affected_index character varying(60),
    entity_type character varying(60),
    affected_ids text,
    operation character varying(60),
    filename character varying(255)
);


ALTER TABLE replicatedindexoperation OWNER TO jira;

--
-- Name: replicatedindexoperation_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE replicatedindexoperation_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE replicatedindexoperation_id_seq OWNER TO jira;

--
-- Name: replicatedindexoperation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE replicatedindexoperation_id_seq OWNED BY replicatedindexoperation.id;


--
-- Name: resolution; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE resolution (
    id character varying(60) NOT NULL,
    sequence bigint,
    pname character varying(60),
    description text,
    iconurl character varying(255)
);


ALTER TABLE resolution OWNER TO jira;

--
-- Name: resolution_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE resolution_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE resolution_id_seq OWNER TO jira;

--
-- Name: resolution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE resolution_id_seq OWNED BY resolution.id;


--
-- Name: rundetails; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE rundetails (
    id bigint NOT NULL,
    job_id character varying(255),
    start_time timestamp with time zone,
    run_duration bigint,
    run_outcome character(1),
    info_message character varying(255)
);


ALTER TABLE rundetails OWNER TO jira;

--
-- Name: rundetails_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE rundetails_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE rundetails_id_seq OWNER TO jira;

--
-- Name: rundetails_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE rundetails_id_seq OWNED BY rundetails.id;


--
-- Name: savedfiltermigrationbackup; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE savedfiltermigrationbackup (
    id bigint NOT NULL,
    saved_filter_id bigint,
    original_filter text,
    migrated_filter text,
    invalidated timestamp with time zone,
    mode character varying(60)
);


ALTER TABLE savedfiltermigrationbackup OWNER TO jira;

--
-- Name: savedfiltermigrationbackup_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE savedfiltermigrationbackup_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE savedfiltermigrationbackup_id_seq OWNER TO jira;

--
-- Name: savedfiltermigrationbackup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE savedfiltermigrationbackup_id_seq OWNED BY savedfiltermigrationbackup.id;


--
-- Name: schemeissuesecurities; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE schemeissuesecurities (
    id bigint NOT NULL,
    scheme bigint,
    security bigint,
    sec_type character varying(255),
    sec_parameter character varying(255)
);


ALTER TABLE schemeissuesecurities OWNER TO jira;

--
-- Name: schemeissuesecurities_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE schemeissuesecurities_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE schemeissuesecurities_id_seq OWNER TO jira;

--
-- Name: schemeissuesecurities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE schemeissuesecurities_id_seq OWNED BY schemeissuesecurities.id;


--
-- Name: schemeissuesecuritylevels; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE schemeissuesecuritylevels (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    scheme bigint
);


ALTER TABLE schemeissuesecuritylevels OWNER TO jira;

--
-- Name: schemeissuesecuritylevels_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE schemeissuesecuritylevels_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE schemeissuesecuritylevels_id_seq OWNER TO jira;

--
-- Name: schemeissuesecuritylevels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE schemeissuesecuritylevels_id_seq OWNED BY schemeissuesecuritylevels.id;


--
-- Name: schemepermissions; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE schemepermissions (
    id bigint NOT NULL,
    scheme bigint,
    permission bigint,
    perm_type character varying(255),
    perm_parameter character varying(255),
    permission_key character varying(255)
);


ALTER TABLE schemepermissions OWNER TO jira;

--
-- Name: schemepermissions_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE schemepermissions_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE schemepermissions_id_seq OWNER TO jira;

--
-- Name: schemepermissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE schemepermissions_id_seq OWNED BY schemepermissions.id;


--
-- Name: searchrequest; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE searchrequest (
    id bigint NOT NULL,
    filtername character varying(255),
    authorname character varying(255),
    description text,
    username character varying(255),
    groupname character varying(255),
    projectid bigint,
    reqcontent text,
    fav_count bigint,
    filtername_lower character varying(255)
);


ALTER TABLE searchrequest OWNER TO jira;

--
-- Name: searchrequest_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE searchrequest_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE searchrequest_id_seq OWNER TO jira;

--
-- Name: searchrequest_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE searchrequest_id_seq OWNED BY searchrequest.id;


--
-- Name: sequence_value_item; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE sequence_value_item (
    seq_name character varying(60) NOT NULL,
    seq_id bigint
);


ALTER TABLE sequence_value_item OWNER TO jira;

--
-- Name: serviceconfig; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE serviceconfig (
    id bigint NOT NULL,
    delaytime bigint,
    clazz character varying(255),
    servicename character varying(255),
    cron_expression character varying(255)
);


ALTER TABLE serviceconfig OWNER TO jira;

--
-- Name: serviceconfig_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE serviceconfig_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE serviceconfig_id_seq OWNER TO jira;

--
-- Name: serviceconfig_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE serviceconfig_id_seq OWNED BY serviceconfig.id;


--
-- Name: sharepermissions; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE sharepermissions (
    id bigint NOT NULL,
    entityid bigint,
    entitytype character varying(60),
    sharetype character varying(10),
    param1 character varying(255),
    param2 character varying(60)
);


ALTER TABLE sharepermissions OWNER TO jira;

--
-- Name: sharepermissions_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE sharepermissions_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sharepermissions_id_seq OWNER TO jira;

--
-- Name: sharepermissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE sharepermissions_id_seq OWNED BY sharepermissions.id;


--
-- Name: t_cd909f_media_store_logos; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE t_cd909f_media_store_logos (
    file_type character varying(255) NOT NULL,
    media_store_id character varying(255)
);


ALTER TABLE t_cd909f_media_store_logos OWNER TO jira;

--
-- Name: tempattachmentsmonitor; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE tempattachmentsmonitor (
    temporary_attachment_id character varying(255) NOT NULL,
    form_token character varying(255),
    file_name character varying(255),
    content_type character varying(255),
    file_size bigint,
    created_time bigint
);


ALTER TABLE tempattachmentsmonitor OWNER TO jira;

--
-- Name: tenant_properties; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE tenant_properties (
    id bigint NOT NULL,
    key character varying(60),
    value character varying(255)
);


ALTER TABLE tenant_properties OWNER TO jira;

--
-- Name: tenant_properties_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE tenant_properties_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tenant_properties_id_seq OWNER TO jira;

--
-- Name: tenant_properties_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE tenant_properties_id_seq OWNED BY tenant_properties.id;


--
-- Name: trackback_ping; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE trackback_ping (
    id bigint NOT NULL,
    issue bigint,
    url character varying(255),
    title character varying(255),
    blogname character varying(255),
    excerpt character varying(255),
    created timestamp with time zone
);


ALTER TABLE trackback_ping OWNER TO jira;

--
-- Name: trackback_ping_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE trackback_ping_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE trackback_ping_id_seq OWNER TO jira;

--
-- Name: trackback_ping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE trackback_ping_id_seq OWNED BY trackback_ping.id;


--
-- Name: trustedapp; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE trustedapp (
    id bigint NOT NULL,
    application_id character varying(255),
    name character varying(255),
    public_key text,
    ip_match text,
    url_match text,
    timeout bigint,
    created timestamp with time zone,
    created_by character varying(255),
    updated timestamp with time zone,
    updated_by character varying(255)
);


ALTER TABLE trustedapp OWNER TO jira;

--
-- Name: trustedapp_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE trustedapp_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE trustedapp_id_seq OWNER TO jira;

--
-- Name: trustedapp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE trustedapp_id_seq OWNED BY trustedapp.id;


--
-- Name: upgradehistory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE upgradehistory (
    id bigint,
    upgradeclass character varying(255) NOT NULL,
    targetbuild character varying(255),
    status character varying(255),
    downgradetaskrequired character(1)
);


ALTER TABLE upgradehistory OWNER TO jira;

--
-- Name: upgradetaskhistory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE upgradetaskhistory (
    id bigint NOT NULL,
    upgrade_task_factory_key character varying(255),
    build_number integer,
    status character varying(60),
    upgrade_type character varying(10)
);


ALTER TABLE upgradetaskhistory OWNER TO jira;

--
-- Name: upgradetaskhistory_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE upgradetaskhistory_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE upgradetaskhistory_id_seq OWNER TO jira;

--
-- Name: upgradetaskhistory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE upgradetaskhistory_id_seq OWNED BY upgradetaskhistory.id;


--
-- Name: upgradetaskhistoryauditlog; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE upgradetaskhistoryauditlog (
    id bigint NOT NULL,
    upgrade_task_factory_key character varying(255),
    build_number integer,
    status character varying(60),
    upgrade_type character varying(10),
    timeperformed timestamp with time zone,
    action character varying(10)
);


ALTER TABLE upgradetaskhistoryauditlog OWNER TO jira;

--
-- Name: upgradetaskhistoryauditlog_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE upgradetaskhistoryauditlog_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE upgradetaskhistoryauditlog_id_seq OWNER TO jira;

--
-- Name: upgradetaskhistoryauditlog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE upgradetaskhistoryauditlog_id_seq OWNED BY upgradetaskhistoryauditlog.id;


--
-- Name: upgradeversionhistory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE upgradeversionhistory (
    id bigint,
    timeperformed timestamp with time zone,
    targetbuild character varying(255) NOT NULL,
    targetversion character varying(255)
);


ALTER TABLE upgradeversionhistory OWNER TO jira;

--
-- Name: userassociation; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userassociation (
    source_name character varying(60) NOT NULL,
    sink_node_id bigint NOT NULL,
    sink_node_entity character varying(60) NOT NULL,
    association_type character varying(60) NOT NULL,
    sequence integer,
    created timestamp with time zone
);


ALTER TABLE userassociation OWNER TO jira;

--
-- Name: userbase; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userbase (
    id bigint NOT NULL,
    username character varying(255),
    password_hash character varying(255)
);


ALTER TABLE userbase OWNER TO jira;

--
-- Name: userbase_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE userbase_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE userbase_id_seq OWNER TO jira;

--
-- Name: userbase_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE userbase_id_seq OWNED BY userbase.id;


--
-- Name: userhistoryitem; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userhistoryitem (
    id bigint NOT NULL,
    entitytype character varying(10),
    entityid character varying(60),
    username character varying(255),
    lastviewed bigint,
    data text
);


ALTER TABLE userhistoryitem OWNER TO jira;

--
-- Name: userhistoryitem_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE userhistoryitem_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE userhistoryitem_id_seq OWNER TO jira;

--
-- Name: userhistoryitem_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE userhistoryitem_id_seq OWNED BY userhistoryitem.id;


--
-- Name: userpickerfilter; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userpickerfilter (
    id bigint NOT NULL,
    customfield bigint,
    customfieldconfig bigint,
    enabled character varying(60)
);


ALTER TABLE userpickerfilter OWNER TO jira;

--
-- Name: userpickerfilter_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE userpickerfilter_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE userpickerfilter_id_seq OWNER TO jira;

--
-- Name: userpickerfilter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE userpickerfilter_id_seq OWNED BY userpickerfilter.id;


--
-- Name: userpickerfiltergroup; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userpickerfiltergroup (
    id bigint NOT NULL,
    userpickerfilter bigint,
    groupname character varying(255)
);


ALTER TABLE userpickerfiltergroup OWNER TO jira;

--
-- Name: userpickerfiltergroup_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE userpickerfiltergroup_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE userpickerfiltergroup_id_seq OWNER TO jira;

--
-- Name: userpickerfiltergroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE userpickerfiltergroup_id_seq OWNED BY userpickerfiltergroup.id;


--
-- Name: userpickerfilterrole; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE userpickerfilterrole (
    id bigint NOT NULL,
    userpickerfilter bigint,
    projectroleid bigint
);


ALTER TABLE userpickerfilterrole OWNER TO jira;

--
-- Name: userpickerfilterrole_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE userpickerfilterrole_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE userpickerfilterrole_id_seq OWNER TO jira;

--
-- Name: userpickerfilterrole_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE userpickerfilterrole_id_seq OWNED BY userpickerfilterrole.id;


--
-- Name: versioncontrol; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE versioncontrol (
    id bigint NOT NULL,
    vcsname character varying(255),
    vcsdescription character varying(255),
    vcstype character varying(255)
);


ALTER TABLE versioncontrol OWNER TO jira;

--
-- Name: versioncontrol_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE versioncontrol_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE versioncontrol_id_seq OWNER TO jira;

--
-- Name: versioncontrol_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE versioncontrol_id_seq OWNED BY versioncontrol.id;


--
-- Name: votehistory; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE votehistory (
    id bigint NOT NULL,
    issueid bigint,
    votes bigint,
    "timestamp" timestamp with time zone
);


ALTER TABLE votehistory OWNER TO jira;

--
-- Name: votehistory_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE votehistory_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE votehistory_id_seq OWNER TO jira;

--
-- Name: votehistory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE votehistory_id_seq OWNED BY votehistory.id;


--
-- Name: workflowscheme; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE workflowscheme (
    id bigint NOT NULL,
    name character varying(255),
    description text,
    scope character(1)
);


ALTER TABLE workflowscheme OWNER TO jira;

--
-- Name: workflowscheme_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE workflowscheme_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE workflowscheme_id_seq OWNER TO jira;

--
-- Name: workflowscheme_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE workflowscheme_id_seq OWNED BY workflowscheme.id;


--
-- Name: workflowschemeentity; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE workflowschemeentity (
    id bigint NOT NULL,
    scheme bigint,
    workflow character varying(255),
    issuetype character varying(255)
);


ALTER TABLE workflowschemeentity OWNER TO jira;

--
-- Name: workflowschemeentity_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE workflowschemeentity_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE workflowschemeentity_id_seq OWNER TO jira;

--
-- Name: workflowschemeentity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE workflowschemeentity_id_seq OWNED BY workflowschemeentity.id;


--
-- Name: worklog; Type: TABLE; Schema: public; Owner: jira
--

CREATE TABLE worklog (
    id bigint NOT NULL,
    issueid bigint,
    author character varying(255),
    grouplevel character varying(255),
    rolelevel bigint,
    worklogbody text,
    created timestamp with time zone,
    updateauthor character varying(255),
    updated timestamp with time zone,
    startdate timestamp with time zone,
    timeworked bigint,
    tokens tsvector
);


ALTER TABLE worklog OWNER TO jira;

--
-- Name: worklog_id_seq; Type: SEQUENCE; Schema: public; Owner: jira
--

CREATE SEQUENCE worklog_id_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE worklog_id_seq OWNER TO jira;

--
-- Name: worklog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jira
--

ALTER SEQUENCE worklog_id_seq OWNED BY worklog.id;


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXPENSE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_EXPENSE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXP_CATEGORY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_EXP_CATEGORY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_HD_SCHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_DAY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_HD_SCHEME_DAY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_MEMBER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_HD_SCHEME_MEMBER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_PERMISSION_GROUP" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_PERMISSION_GROUP_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_PROJECT_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_PROJECT_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WA_VALUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_WA_VALUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_WL_SCHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_DAY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_WL_SCHEME_DAY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_MEMBER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_WL_SCHEME_MEMBER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WORK_ATTRIBUTE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_013613_WORK_ATTRIBUTE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_KB_HELPFUL_AGGR" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_0201F0_KB_HELPFUL_AGGR_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_KB_VIEW_AGGR" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_0201F0_KB_VIEW_AGGR_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_STATS_EVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_0201F0_STATS_EVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_STATS_EVENT_PARAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_0201F0_STATS_EVENT_PARAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_098293_ANNOUNCEMENT_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_098293_ANNOUNCEMENT_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_21D670_WHITELIST_RULES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_21D670_WHITELIST_RULES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILCHANNEL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILCHANNEL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILCONNECTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILCONNECTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILGLOBALHANDLER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILGLOBALHANDLER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILHANDLER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILHANDLER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILITEM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMAUDIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILITEMAUDIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMCHUNK" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILITEMCHUNK_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILRUNAUDIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2C4E5C_MAILRUNAUDIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ALLOCATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_ALLOCATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ATTACHMENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_ATTACHMENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_BASELINE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_BASELINE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_COMMENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_COMMENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_CUSTOMFIELD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDPVALUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_CUSTOMFIELDPVALUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDVALUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_CUSTOMFIELDVALUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ENTITY_CHANGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_ENTITY_CHANGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXCHANGERATE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_EXCHANGERATE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXPENSE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_EXPENSE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXTERNALTEAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_EXTERNALTEAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FILTER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FILTER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCF" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIOCF_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCFVALUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIOCFVALUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOTOPORTFOLIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIOTOPORTFOLIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_ADMIN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIO_ADMIN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_FORMAT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIO_FORMAT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_USER_AO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_FOLIO_USER_AO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_NWDS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_NWDS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OTRULETOFOLIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_OTRULETOFOLIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OVERTIME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_OVERTIME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PERMISSION_GROUP" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_PERMISSION_GROUP_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PLAN_ALLOCATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_PLAN_ALLOCATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_PORTFOLIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIOTOPORTFOLIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_PORTFOLIOTOPORTFOLIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIO_ADMIN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_PORTFOLIO_ADMIN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_POSITION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_POSITION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_RATE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_RATE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_STATUS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_STATUS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_TIMELINE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_TIMELINE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_WAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WEEKDAY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_WEEKDAY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKED_HOURS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_WORKED_HOURS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKFLOW" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_2D3BEA_WORKFLOW_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_319474_MESSAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE_PROPERTY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_319474_MESSAGE_PROPERTY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_319474_QUEUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE_PROPERTY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_319474_QUEUE_PROPERTY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRACOMMENT_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_JIRACOMMENT_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_BEAN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_JIRAMAPPING_BEAN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_SCHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_JIRAMAPPING_SCHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_SET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_JIRAMAPPING_SET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAPROJECT_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_JIRAPROJECT_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_REMOTE_IDCF" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_REMOTE_IDCF_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_REMOTE_LINK_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3A3ECC_REMOTE_LINK_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3B1893_LOOP_DETECTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_3B1893_LOOP_DETECTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4AEACD_WEBHOOK_DAO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_4AEACD_WEBHOOK_DAO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4E8AE6_NOTIF_BATCH_QUEUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_4E8AE6_NOTIF_BATCH_QUEUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4E8AE6_OUT_EMAIL_SETTINGS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_4E8AE6_OUT_EMAIL_SETTINGS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ASYNCUPGRADERECORD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_ASYNCUPGRADERECORD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CAPABILITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CAPABILITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKB" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CONFLUENCEKB_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBENABLED" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CONFLUENCEKBENABLED_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBLABELS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CONFLUENCEKBLABELS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CUSTOMGLOBALTHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CUSTOMGLOBALTHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CUSTOMTHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_CUSTOMTHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILCHANNELSETTING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_EMAILCHANNELSETTING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILSETTINGS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_EMAILSETTINGS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GOAL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_GOAL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUP" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_GROUP_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUPTOREQUESTTYPE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_GROUPTOREQUESTTYPE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_IMAGES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_IMAGES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_METRICCONDITION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_METRICCONDITION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_ORGANIZATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_MEMBER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_ORGANIZATION_MEMBER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_PROJECT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_ORGANIZATION_PROJECT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_OUT_EMAIL_SETTINGS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_OUT_EMAIL_SETTINGS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_PARTICIPANTSETTINGS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_PARTICIPANTSETTINGS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_QUEUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_QUEUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_QUEUECOLUMN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_QUEUECOLUMN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_REPORT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_REPORT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SERIES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_SERIES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SERVICEDESK" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_SERVICEDESK_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_STATUSMAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_STATUSMAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SUBSCRIPTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_SUBSCRIPTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SYNCUPGRADERECORD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_SYNCUPGRADERECORD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_THRESHOLD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_THRESHOLD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_TIMEMETRIC" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_TIMEMETRIC_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_VIEWPORT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_VIEWPORTFIELD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELDVALUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_VIEWPORTFIELDVALUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFORM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_54307E_VIEWPORTFORM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_550953_SHORTCUT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_550953_SHORTCUT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVAL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_56464C_APPROVAL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_56464C_APPROVER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVERDECISION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_56464C_APPROVERDECISION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_NOTIFICATIONRECORD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_56464C_NOTIFICATIONRECORD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_ISSUE_SUMMARY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_575BF5_ISSUE_SUMMARY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_PROCESSED_COMMITS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_575BF5_PROCESSED_COMMITS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_PROVIDER_ISSUE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_575BF5_PROVIDER_ISSUE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_587B34_PROJECT_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_587B34_PROJECT_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_5FB9D7_AOHIP_CHAT_LINK" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_5FB9D7_AOHIP_CHAT_LINK_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_5FB9D7_AOHIP_CHAT_USER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_5FB9D7_AOHIP_CHAT_USER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_AUDITENTRY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_AUDITENTRY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_BOARDADMINS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_BOARDADMINS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDCOLOR" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_CARDCOLOR_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDLAYOUT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_CARDLAYOUT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_COLUMN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMNSTATUS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_COLUMNSTATUS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CREATIONCONVERSATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_CREATIONCONVERSATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_DETAILVIEWFIELD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_DETAILVIEWFIELD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ESTIMATESTATISTIC" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_ESTIMATESTATISTIC_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ISSUERANKING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_ISSUERANKING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ISSUERANKINGLOG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_ISSUERANKINGLOG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_LEXORANK" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_LEXORANK_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_LEXORANKBALANCER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_LEXORANKBALANCER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_NONWORKINGDAY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_NONWORKINGDAY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_QUICKFILTER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_QUICKFILTER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_RANKABLEOBJECT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_RANKABLEOBJECT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_RAPIDVIEW" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_RAPIDVIEW_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SPRINT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_SPRINT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_STATSFIELD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_STATSFIELD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SUBQUERY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_SUBQUERY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SWIMLANE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_SWIMLANE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_TRACKINGSTATISTIC" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_TRACKINGSTATISTIC_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_VERSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_VERSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_WORKINGDAYS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_60DB71_WORKINGDAYS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_CONNECT_APPLICATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_68DACE_CONNECT_APPLICATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_INSTALLATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_68DACE_INSTALLATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_CALENDAR" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_7A2604_CALENDAR_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_HOLIDAY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_7A2604_HOLIDAY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_WORKINGTIME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_7A2604_WORKINGTIME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABILITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_ABILITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABSENCE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_ABSENCE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_AVAILABILITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_AVAILABILITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_INIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_INIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_PERSON" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_PERSON_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_RESOURCE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_RESOURCE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_SKILL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_SKILL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_TEAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_82B313_TEAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_GRACE_PERIOD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_GRACE_PERIOD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_PROJECT_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_PROJECT_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_STREAMS_ENTRY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_STREAMS_ENTRY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMEPLAN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_TIMEPLAN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMESHEET_APPROVAL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_TIMESHEET_APPROVAL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMESHEET_STATUS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_86ED1B_TIMESHEET_STATUS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_AGREEMENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_AGREEMENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_CONNECTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_CONNECTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_LICENSE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_LICENSE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_MAPPING_BEAN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_MAPPING_BEAN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_MAPPING_ENTRY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_MAPPING_ENTRY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_TRANSACTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_CONTENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_TRANSACTION_CONTENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_LOG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_88DE6A_TRANSACTION_LOG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_EXEC_RULE_MSG_ITEM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_EXEC_RULE_MSG_ITEM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_CONDITION_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_CONDITION_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_CONF_DATA" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_COND_CONF_DATA_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_COND_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_THEN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_IF_THEN_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_PROJECT_USER_CONTEXT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_PROJECT_USER_CONTEXT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_PROJ_CONTEXT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RSETREV_PROJ_CONTEXT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_USER_CONTEXT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RSETREV_USER_CONTEXT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RULE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULESET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RULESET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULESET_REVISION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RULESET_REVISION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULE_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_RULE_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACTION_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_THEN_ACTION_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_CONF_DATA" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_THEN_ACT_CONF_DATA_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_THEN_ACT_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_EXECUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_THEN_EXECUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HANDLER_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_WHEN_HANDLER_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HAND_CONF_DATA" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_9B2E3B_WHEN_HAND_CONF_DATA_ID_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABILITY" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOABILITY_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABSENCE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOABSENCE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOAVAILABILITY" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOAVAILABILITY_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOCONFIGURATION" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOCONFIGURATION_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOCUSTOM_WORDING" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOCUSTOM_WORDING_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AODEPENDENCY" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AODEPENDENCY_ID_OTHER_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AODOOR_STOP" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_A415DF_AODOOR_STOP_ID_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOESTIMATE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOESTIMATE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOEXTENSION_LINK" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOEXTENSION_LINK_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AONON_WORKING_DAYS" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AONON_WORKING_DAYS_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPERMISSION" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOPERMISSION_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPERSON" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOPERSON_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPLAN" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOPLAN_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPLAN_CONFIGURATION" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOPLAN_CONFIGURATION_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPRESENCE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOPRESENCE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORELEASE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AORELEASE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOREPLANNING" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOREPLANNING_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORESOURCE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AORESOURCE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSKILL" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSKILL_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSOLUTION_STORE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSOLUTION_STORE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSPRINT" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSPRINT_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTAGE" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSTAGE_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSTREAM_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM_TO_TEAM" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOSTREAM_TO_TEAM_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTEAM" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOTEAM_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTHEME" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOTHEME_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOWORK_ITEM_ID_OTHER_seq"'::regclass);


--
-- Name: ID_OTHER; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM_TO_RES" ALTER COLUMN "ID_OTHER" SET DEFAULT nextval('"AO_A415DF_AOWORK_ITEM_TO_RES_ID_OTHER_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A44657_HEALTH_CHECK_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_A44657_HEALTH_CHECK_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_MEMBERSHIP" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_MEMBERSHIP_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_PROGRAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_PROGRAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_LINK" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_LINK_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_MEMBER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_MEMBER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_MEMBER_V2" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_MEMBER_V2_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_PERMISSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_PERMISSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_ROLE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_ROLE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_TO_MEMBER" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_TO_MEMBER_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_V2" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_AEFED0_TEAM_V2_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_B9A0F0_APPLIED_TEMPLATE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_B9A0F0_APPLIED_TEMPLATE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_ACCOUNT_V1" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_ACCOUNT_V1_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_BUDGET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_BUDGET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_TYPE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_CATEGORY_TYPE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_V1" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_CATEGORY_V1_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_PERMISSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_CUSTOMER_PERMISSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_V1" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_CUSTOMER_V1_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_LINK_V1" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_LINK_V1_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_RATE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE_TABLE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C3C6E8_RATE_TABLE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C7F17E_LINGO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_REVISION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C7F17E_LINGO_REVISION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_TRANSLATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_C7F17E_LINGO_TRANSLATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_CFF990_AOTRANSITION_FAILURE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_CFF990_AOTRANSITION_FAILURE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ASSIGNMENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_ASSIGNMENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_CONFIGURATION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_CONFIGURATION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DEP_ISSUE_LINK_TYPES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_DEP_ISSUE_LINK_TYPES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DISTRIBUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_DISTRIBUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_EXCLUDED_VERSIONS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_EXCLUDED_VERSIONS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_HIERARCHY_CONFIG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_HIERARCHY_CONFIG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_INIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_INIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ISSUE_SOURCE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_ISSUE_SOURCE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_NONWORKINGDAYS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_NONWORKINGDAYS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PERMISSIONS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PERMISSIONS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLAN" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PLAN_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANSKILL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PLANSKILL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTEAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PLANTEAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTHEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PLANTHEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANVERSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_PLANVERSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_RANK_ITEM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_RANK_ITEM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ABILITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_ABILITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_AVLBLTY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_AVLBLTY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_CHANGES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_CHANGES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_ISSUES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_LINKS" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_ISSUE_LINKS_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_RES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_ISSUE_RES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_PERSON" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_PERSON_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_RESOURCE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_RESOURCE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_SKILL" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_SKILL_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_STAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_STAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_TEAM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_TEAM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_THEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_THEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_VERSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_VERSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_XPVERSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SCENARIO_XPVERSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SOLUTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_SOLUTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_STAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_STAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_THEME" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_THEME_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_VERSION_ENRICHMENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_VERSION_ENRICHMENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_X_PROJECT_VERSION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_D9132D_X_PROJECT_VERSION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_DEB285_BLOG_AO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_DEB285_BLOG_AO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_DEB285_COMMENT_AO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_DEB285_COMMENT_AO_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_BRANCH_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_CHANGESET_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_CHANGESET_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_COMMIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_GIT_HUB_EVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_TAG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ORGANIZATION_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ORG_TO_PROJECT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ORG_TO_PROJECT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_ISSUE_KEY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_ISSUE_KEY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_PARTICIPANT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_TO_COMMIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PULL_REQUEST" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PULL_REQUEST_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPOSITORY_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_REPOSITORY_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_REPO_TO_CHANGESET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_PROJECT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_REPO_TO_PROJECT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_AUDIT_LOG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_EVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_SYNC_EVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_ACTIONREGISTRY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_ED979B_ACTIONREGISTRY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_EVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_ED979B_EVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_EVENTPROPERTY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_ED979B_EVENTPROPERTY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_SUBSCRIPTION" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_ED979B_SUBSCRIPTION_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_USEREVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_ED979B_USEREVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_HISTORY_RECORD" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F1B27B_HISTORY_RECORD_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMPONENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F1B27B_KEY_COMPONENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMP_HISTORY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F1B27B_KEY_COMP_HISTORY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F1B27B_PROMISE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE_HISTORY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F1B27B_PROMISE_HISTORY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F4ED3A_ADD_ON_PROPERTY_AO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_F4ED3A_ADD_ON_PROPERTY_AO_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY addoncloudexport ALTER COLUMN id SET DEFAULT nextval('addoncloudexport_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY adhocupgradetaskhistory ALTER COLUMN id SET DEFAULT nextval('adhocupgradetaskhistory_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY amq_temporary_store_a0b856 ALTER COLUMN id SET DEFAULT nextval('amq_temporary_store_a0b856_id_seq'::regclass);


--
-- Name: activity_id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity ALTER COLUMN activity_id SET DEFAULT nextval('"AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_actor_entity ALTER COLUMN id SET DEFAULT nextval('"AO_563AEE_ACTOR_ENTITY_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_media_link_entity ALTER COLUMN id SET DEFAULT nextval('"AO_563AEE_MEDIA_LINK_ENTITY_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_object_entity ALTER COLUMN id SET DEFAULT nextval('"AO_563AEE_OBJECT_ENTITY_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_target_entity ALTER COLUMN id SET DEFAULT nextval('"AO_563AEE_TARGET_ENTITY_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_a0b856_web_hook_listener_ao ALTER COLUMN id SET DEFAULT nextval('"AO_A0B856_WEB_HOOK_LISTENER_AO_ID_seq"'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY app_user ALTER COLUMN id SET DEFAULT nextval('app_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY async_task ALTER COLUMN id SET DEFAULT nextval('async_task_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY async_task_payload ALTER COLUMN id SET DEFAULT nextval('async_task_payload_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_changed_value ALTER COLUMN id SET DEFAULT nextval('audit_changed_value_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_item ALTER COLUMN id SET DEFAULT nextval('audit_item_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_log ALTER COLUMN id SET DEFAULT nextval('audit_log_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY avatar ALTER COLUMN id SET DEFAULT nextval('avatar_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY board ALTER COLUMN id SET DEFAULT nextval('board_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY changegroup ALTER COLUMN id SET DEFAULT nextval('changegroup_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY changeitem ALTER COLUMN id SET DEFAULT nextval('changeitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusteredjob ALTER COLUMN id SET DEFAULT nextval('clusteredjob_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusterlockstatus ALTER COLUMN id SET DEFAULT nextval('clusterlockstatus_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clustermessage ALTER COLUMN id SET DEFAULT nextval('clustermessage_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY columnlayout ALTER COLUMN id SET DEFAULT nextval('columnlayout_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY columnlayoutitem ALTER COLUMN id SET DEFAULT nextval('columnlayoutitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY component ALTER COLUMN id SET DEFAULT nextval('component_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY configurationcontext ALTER COLUMN id SET DEFAULT nextval('configurationcontext_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfield ALTER COLUMN id SET DEFAULT nextval('customfield_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfieldoption ALTER COLUMN id SET DEFAULT nextval('customfieldoption_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfieldvalue ALTER COLUMN id SET DEFAULT nextval('customfieldvalue_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_application ALTER COLUMN id SET DEFAULT nextval('cwd_application_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_directory ALTER COLUMN id SET DEFAULT nextval('cwd_directory_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_group ALTER COLUMN id SET DEFAULT nextval('cwd_group_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_group_attributes ALTER COLUMN id SET DEFAULT nextval('cwd_group_attributes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_membership ALTER COLUMN id SET DEFAULT nextval('cwd_membership_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_user ALTER COLUMN id SET DEFAULT nextval('cwd_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_user_attributes ALTER COLUMN id SET DEFAULT nextval('cwd_user_attributes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY deadletter ALTER COLUMN id SET DEFAULT nextval('deadletter_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY draftworkflowscheme ALTER COLUMN id SET DEFAULT nextval('draftworkflowscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY draftworkflowschemeentity ALTER COLUMN id SET DEFAULT nextval('draftworkflowschemeentity_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property ALTER COLUMN id SET DEFAULT nextval('entity_property_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_index ALTER COLUMN id SET DEFAULT nextval('entity_property_index_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_index_document ALTER COLUMN id SET DEFAULT nextval('entity_property_index_document_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_value ALTER COLUMN id SET DEFAULT nextval('entity_property_value_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_translation ALTER COLUMN id SET DEFAULT nextval('entity_translation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY external_entities ALTER COLUMN id SET DEFAULT nextval('external_entities_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY externalgadget ALTER COLUMN id SET DEFAULT nextval('externalgadget_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY favouriteassociations ALTER COLUMN id SET DEFAULT nextval('favouriteassociations_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY feature ALTER COLUMN id SET DEFAULT nextval('feature_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfigscheme ALTER COLUMN id SET DEFAULT nextval('fieldconfigscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfigschemeissuetype ALTER COLUMN id SET DEFAULT nextval('fieldconfigschemeissuetype_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfiguration ALTER COLUMN id SET DEFAULT nextval('fieldconfiguration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayout ALTER COLUMN id SET DEFAULT nextval('fieldlayout_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutitem ALTER COLUMN id SET DEFAULT nextval('fieldlayoutitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutscheme ALTER COLUMN id SET DEFAULT nextval('fieldlayoutscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutschemeassociation ALTER COLUMN id SET DEFAULT nextval('fieldlayoutschemeassociation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutschemeentity ALTER COLUMN id SET DEFAULT nextval('fieldlayoutschemeentity_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreen ALTER COLUMN id SET DEFAULT nextval('fieldscreen_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenlayoutitem ALTER COLUMN id SET DEFAULT nextval('fieldscreenlayoutitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenscheme ALTER COLUMN id SET DEFAULT nextval('fieldscreenscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenschemeitem ALTER COLUMN id SET DEFAULT nextval('fieldscreenschemeitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreentab ALTER COLUMN id SET DEFAULT nextval('fieldscreentab_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fileattachment ALTER COLUMN id SET DEFAULT nextval('fileattachment_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY filtersubscription ALTER COLUMN id SET DEFAULT nextval('filtersubscription_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY gadgetuserpreference ALTER COLUMN id SET DEFAULT nextval('gadgetuserpreference_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY genericconfiguration ALTER COLUMN id SET DEFAULT nextval('genericconfiguration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY globalpermissionentry ALTER COLUMN id SET DEFAULT nextval('globalpermissionentry_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY groupbase ALTER COLUMN id SET DEFAULT nextval('groupbase_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option ALTER COLUMN id SET DEFAULT nextval('issue_field_option_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option_attr ALTER COLUMN id SET DEFAULT nextval('issue_field_option_attr_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option_scope ALTER COLUMN id SET DEFAULT nextval('issue_field_option_scope_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuelink ALTER COLUMN id SET DEFAULT nextval('issuelink_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuelinktype ALTER COLUMN id SET DEFAULT nextval('issuelinktype_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuesecurityscheme ALTER COLUMN id SET DEFAULT nextval('issuesecurityscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuestatus ALTER COLUMN id SET DEFAULT (nextval('issuestatus_id_seq'::regclass))::character varying(60);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetype ALTER COLUMN id SET DEFAULT (nextval('issuetype_id_seq'::regclass))::character varying(60);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetypescreenscheme ALTER COLUMN id SET DEFAULT nextval('issuetypescreenscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetypescreenschemeentity ALTER COLUMN id SET DEFAULT nextval('issuetypescreenschemeentity_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraaction ALTER COLUMN id SET DEFAULT nextval('jiraaction_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiradraftworkflows ALTER COLUMN id SET DEFAULT nextval('jiradraftworkflows_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraeventtype ALTER COLUMN id SET DEFAULT nextval('jiraeventtype_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraissue ALTER COLUMN id SET DEFAULT nextval('jiraissue_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraperms ALTER COLUMN id SET DEFAULT nextval('jiraperms_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraworkflows ALTER COLUMN id SET DEFAULT nextval('jiraworkflows_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraworkflowstatuses ALTER COLUMN id SET DEFAULT nextval('jiraworkflowstatuses_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY label ALTER COLUMN id SET DEFAULT nextval('label_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY licenserolesdefault ALTER COLUMN id SET DEFAULT nextval('licenserolesdefault_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY licenserolesgroup ALTER COLUMN id SET DEFAULT nextval('licenserolesgroup_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY listenerconfig ALTER COLUMN id SET DEFAULT nextval('listenerconfig_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY mailserver ALTER COLUMN id SET DEFAULT nextval('mailserver_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY managedconfigurationitem ALTER COLUMN id SET DEFAULT nextval('managedconfigurationitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY media_api_credentials ALTER COLUMN id SET DEFAULT nextval('media_api_credentials_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY membershipbase ALTER COLUMN id SET DEFAULT nextval('membershipbase_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY moved_issue_key ALTER COLUMN id SET DEFAULT nextval('moved_issue_key_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY nodeindexcounter ALTER COLUMN id SET DEFAULT nextval('nodeindexcounter_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notification ALTER COLUMN id SET DEFAULT nextval('notification_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notification_subscription_ec1a8f ALTER COLUMN id SET DEFAULT nextval('notification_subscription_ec1a8f_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notificationinstance ALTER COLUMN id SET DEFAULT nextval('notificationinstance_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notificationscheme ALTER COLUMN id SET DEFAULT nextval('notificationscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthconsumer ALTER COLUMN id SET DEFAULT nextval('oauthconsumer_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthconsumertoken ALTER COLUMN id SET DEFAULT nextval('oauthconsumertoken_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthspconsumer ALTER COLUMN id SET DEFAULT nextval('oauthspconsumer_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthsptoken ALTER COLUMN id SET DEFAULT nextval('oauthsptoken_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY optionconfiguration ALTER COLUMN id SET DEFAULT nextval('optionconfiguration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_currentstep ALTER COLUMN id SET DEFAULT nextval('os_currentstep_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_historystep ALTER COLUMN id SET DEFAULT nextval('os_historystep_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_wfentry ALTER COLUMN id SET DEFAULT nextval('os_wfentry_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY permissionscheme ALTER COLUMN id SET DEFAULT nextval('permissionscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY pluginversion ALTER COLUMN id SET DEFAULT nextval('pluginversion_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY portalpage ALTER COLUMN id SET DEFAULT nextval('portalpage_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY portletconfiguration ALTER COLUMN id SET DEFAULT nextval('portletconfiguration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY priority ALTER COLUMN id SET DEFAULT (nextval('priority_id_seq'::regclass))::character varying(60);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY productlicense ALTER COLUMN id SET DEFAULT nextval('productlicense_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY project ALTER COLUMN id SET DEFAULT nextval('project_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY project_key ALTER COLUMN id SET DEFAULT nextval('project_key_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectcategory ALTER COLUMN id SET DEFAULT nextval('projectcategory_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectrole ALTER COLUMN id SET DEFAULT nextval('projectrole_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectroleactor ALTER COLUMN id SET DEFAULT nextval('projectroleactor_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectversion ALTER COLUMN id SET DEFAULT nextval('projectversion_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertyentry ALTER COLUMN id SET DEFAULT nextval('propertyentry_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_cron_triggers ALTER COLUMN id SET DEFAULT nextval('qrtz_cron_triggers_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_job_details ALTER COLUMN id SET DEFAULT nextval('qrtz_job_details_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_job_listeners ALTER COLUMN id SET DEFAULT nextval('qrtz_job_listeners_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_simple_triggers ALTER COLUMN id SET DEFAULT nextval('qrtz_simple_triggers_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_trigger_listeners ALTER COLUMN id SET DEFAULT nextval('qrtz_trigger_listeners_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_triggers ALTER COLUMN id SET DEFAULT nextval('qrtz_triggers_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY reindex_component ALTER COLUMN id SET DEFAULT nextval('reindex_component_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY reindex_request ALTER COLUMN id SET DEFAULT nextval('reindex_request_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY remembermetoken ALTER COLUMN id SET DEFAULT nextval('remembermetoken_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY remotelink ALTER COLUMN id SET DEFAULT nextval('remotelink_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY replicatedindexoperation ALTER COLUMN id SET DEFAULT nextval('replicatedindexoperation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY resolution ALTER COLUMN id SET DEFAULT (nextval('resolution_id_seq'::regclass))::character varying(60);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY rundetails ALTER COLUMN id SET DEFAULT nextval('rundetails_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY savedfiltermigrationbackup ALTER COLUMN id SET DEFAULT nextval('savedfiltermigrationbackup_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemeissuesecurities ALTER COLUMN id SET DEFAULT nextval('schemeissuesecurities_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemeissuesecuritylevels ALTER COLUMN id SET DEFAULT nextval('schemeissuesecuritylevels_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemepermissions ALTER COLUMN id SET DEFAULT nextval('schemepermissions_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY searchrequest ALTER COLUMN id SET DEFAULT nextval('searchrequest_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY serviceconfig ALTER COLUMN id SET DEFAULT nextval('serviceconfig_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY sharepermissions ALTER COLUMN id SET DEFAULT nextval('sharepermissions_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY tenant_properties ALTER COLUMN id SET DEFAULT nextval('tenant_properties_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY trackback_ping ALTER COLUMN id SET DEFAULT nextval('trackback_ping_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY trustedapp ALTER COLUMN id SET DEFAULT nextval('trustedapp_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradetaskhistory ALTER COLUMN id SET DEFAULT nextval('upgradetaskhistory_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradetaskhistoryauditlog ALTER COLUMN id SET DEFAULT nextval('upgradetaskhistoryauditlog_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userbase ALTER COLUMN id SET DEFAULT nextval('userbase_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userhistoryitem ALTER COLUMN id SET DEFAULT nextval('userhistoryitem_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfilter ALTER COLUMN id SET DEFAULT nextval('userpickerfilter_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfiltergroup ALTER COLUMN id SET DEFAULT nextval('userpickerfiltergroup_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfilterrole ALTER COLUMN id SET DEFAULT nextval('userpickerfilterrole_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY versioncontrol ALTER COLUMN id SET DEFAULT nextval('versioncontrol_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY votehistory ALTER COLUMN id SET DEFAULT nextval('votehistory_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY workflowscheme ALTER COLUMN id SET DEFAULT nextval('workflowscheme_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY workflowschemeentity ALTER COLUMN id SET DEFAULT nextval('workflowschemeentity_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jira
--

ALTER TABLE ONLY worklog ALTER COLUMN id SET DEFAULT nextval('worklog_id_seq'::regclass);


--
-- Name: AO_013613_EXPENSE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXPENSE"
    ADD CONSTRAINT "AO_013613_EXPENSE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_EXP_CATEGORY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXP_CATEGORY"
    ADD CONSTRAINT "AO_013613_EXP_CATEGORY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_HD_SCHEME_DAY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_DAY"
    ADD CONSTRAINT "AO_013613_HD_SCHEME_DAY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_HD_SCHEME_MEMBER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_MEMBER"
    ADD CONSTRAINT "AO_013613_HD_SCHEME_MEMBER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_HD_SCHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME"
    ADD CONSTRAINT "AO_013613_HD_SCHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_PERMISSION_GROUP_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_PERMISSION_GROUP"
    ADD CONSTRAINT "AO_013613_PERMISSION_GROUP_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_PROJECT_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_PROJECT_CONFIG"
    ADD CONSTRAINT "AO_013613_PROJECT_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_WA_VALUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WA_VALUE"
    ADD CONSTRAINT "AO_013613_WA_VALUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_WL_SCHEME_DAY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_DAY"
    ADD CONSTRAINT "AO_013613_WL_SCHEME_DAY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_WL_SCHEME_MEMBER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_MEMBER"
    ADD CONSTRAINT "AO_013613_WL_SCHEME_MEMBER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_WL_SCHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME"
    ADD CONSTRAINT "AO_013613_WL_SCHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_013613_WORK_ATTRIBUTE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WORK_ATTRIBUTE"
    ADD CONSTRAINT "AO_013613_WORK_ATTRIBUTE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_0201F0_KB_HELPFUL_AGGR_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_KB_HELPFUL_AGGR"
    ADD CONSTRAINT "AO_0201F0_KB_HELPFUL_AGGR_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_0201F0_KB_VIEW_AGGR_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_KB_VIEW_AGGR"
    ADD CONSTRAINT "AO_0201F0_KB_VIEW_AGGR_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_0201F0_STATS_EVENT_PARAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_STATS_EVENT_PARAM"
    ADD CONSTRAINT "AO_0201F0_STATS_EVENT_PARAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_0201F0_STATS_EVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_STATS_EVENT"
    ADD CONSTRAINT "AO_0201F0_STATS_EVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_098293_ANNOUNCEMENT_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_098293_ANNOUNCEMENT_ENTITY"
    ADD CONSTRAINT "AO_098293_ANNOUNCEMENT_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_21D670_WHITELIST_RULES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_21D670_WHITELIST_RULES"
    ADD CONSTRAINT "AO_21D670_WHITELIST_RULES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILCHANNEL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILCHANNEL"
    ADD CONSTRAINT "AO_2C4E5C_MAILCHANNEL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILCONNECTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILCONNECTION"
    ADD CONSTRAINT "AO_2C4E5C_MAILCONNECTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILGLOBALHANDLER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILGLOBALHANDLER"
    ADD CONSTRAINT "AO_2C4E5C_MAILGLOBALHANDLER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILHANDLER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILHANDLER"
    ADD CONSTRAINT "AO_2C4E5C_MAILHANDLER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILITEMAUDIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMAUDIT"
    ADD CONSTRAINT "AO_2C4E5C_MAILITEMAUDIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILITEMCHUNK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMCHUNK"
    ADD CONSTRAINT "AO_2C4E5C_MAILITEMCHUNK_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEM"
    ADD CONSTRAINT "AO_2C4E5C_MAILITEM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2C4E5C_MAILRUNAUDIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILRUNAUDIT"
    ADD CONSTRAINT "AO_2C4E5C_MAILRUNAUDIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_ALLOCATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ALLOCATION"
    ADD CONSTRAINT "AO_2D3BEA_ALLOCATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_ATTACHMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ATTACHMENT"
    ADD CONSTRAINT "AO_2D3BEA_ATTACHMENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_BASELINE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_BASELINE"
    ADD CONSTRAINT "AO_2D3BEA_BASELINE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_COMMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_COMMENT"
    ADD CONSTRAINT "AO_2D3BEA_COMMENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_CUSTOMFIELDPVALUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDPVALUE"
    ADD CONSTRAINT "AO_2D3BEA_CUSTOMFIELDPVALUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_CUSTOMFIELDVALUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDVALUE"
    ADD CONSTRAINT "AO_2D3BEA_CUSTOMFIELDVALUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_CUSTOMFIELD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELD"
    ADD CONSTRAINT "AO_2D3BEA_CUSTOMFIELD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_ENTITY_CHANGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ENTITY_CHANGE"
    ADD CONSTRAINT "AO_2D3BEA_ENTITY_CHANGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_EXCHANGERATE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXCHANGERATE"
    ADD CONSTRAINT "AO_2D3BEA_EXCHANGERATE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_EXPENSE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXPENSE"
    ADD CONSTRAINT "AO_2D3BEA_EXPENSE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_EXTERNALTEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXTERNALTEAM"
    ADD CONSTRAINT "AO_2D3BEA_EXTERNALTEAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FILTER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FILTER"
    ADD CONSTRAINT "AO_2D3BEA_FILTER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIOCFVALUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCFVALUE"
    ADD CONSTRAINT "AO_2D3BEA_FOLIOCFVALUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIOCF_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCF"
    ADD CONSTRAINT "AO_2D3BEA_FOLIOCF_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIOTOPORTFOLIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOTOPORTFOLIO"
    ADD CONSTRAINT "AO_2D3BEA_FOLIOTOPORTFOLIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIO_ADMIN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_ADMIN"
    ADD CONSTRAINT "AO_2D3BEA_FOLIO_ADMIN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIO_FORMAT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_FORMAT"
    ADD CONSTRAINT "AO_2D3BEA_FOLIO_FORMAT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIO_USER_AO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_USER_AO"
    ADD CONSTRAINT "AO_2D3BEA_FOLIO_USER_AO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_FOLIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO"
    ADD CONSTRAINT "AO_2D3BEA_FOLIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_NWDS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_NWDS"
    ADD CONSTRAINT "AO_2D3BEA_NWDS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_OTRULETOFOLIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OTRULETOFOLIO"
    ADD CONSTRAINT "AO_2D3BEA_OTRULETOFOLIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_OVERTIME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OVERTIME"
    ADD CONSTRAINT "AO_2D3BEA_OVERTIME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_PERMISSION_GROUP_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PERMISSION_GROUP"
    ADD CONSTRAINT "AO_2D3BEA_PERMISSION_GROUP_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_PLAN_ALLOCATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PLAN_ALLOCATION"
    ADD CONSTRAINT "AO_2D3BEA_PLAN_ALLOCATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_PORTFOLIOTOPORTFOLIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIOTOPORTFOLIO"
    ADD CONSTRAINT "AO_2D3BEA_PORTFOLIOTOPORTFOLIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_PORTFOLIO_ADMIN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIO_ADMIN"
    ADD CONSTRAINT "AO_2D3BEA_PORTFOLIO_ADMIN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_PORTFOLIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIO"
    ADD CONSTRAINT "AO_2D3BEA_PORTFOLIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_POSITION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_POSITION"
    ADD CONSTRAINT "AO_2D3BEA_POSITION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_RATE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_RATE"
    ADD CONSTRAINT "AO_2D3BEA_RATE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_STATUS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_STATUS"
    ADD CONSTRAINT "AO_2D3BEA_STATUS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_TIMELINE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_TIMELINE"
    ADD CONSTRAINT "AO_2D3BEA_TIMELINE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_WAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WAGE"
    ADD CONSTRAINT "AO_2D3BEA_WAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_WEEKDAY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WEEKDAY"
    ADD CONSTRAINT "AO_2D3BEA_WEEKDAY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_WORKED_HOURS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKED_HOURS"
    ADD CONSTRAINT "AO_2D3BEA_WORKED_HOURS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_2D3BEA_WORKFLOW_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKFLOW"
    ADD CONSTRAINT "AO_2D3BEA_WORKFLOW_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_319474_MESSAGE_PROPERTY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE_PROPERTY"
    ADD CONSTRAINT "AO_319474_MESSAGE_PROPERTY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_319474_MESSAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE"
    ADD CONSTRAINT "AO_319474_MESSAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_319474_QUEUE_PROPERTY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE_PROPERTY"
    ADD CONSTRAINT "AO_319474_QUEUE_PROPERTY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_319474_QUEUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE"
    ADD CONSTRAINT "AO_319474_QUEUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_JIRACOMMENT_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRACOMMENT_MAPPING"
    ADD CONSTRAINT "AO_3A3ECC_JIRACOMMENT_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_JIRAMAPPING_BEAN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_BEAN"
    ADD CONSTRAINT "AO_3A3ECC_JIRAMAPPING_BEAN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_JIRAMAPPING_SCHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_SCHEME"
    ADD CONSTRAINT "AO_3A3ECC_JIRAMAPPING_SCHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_JIRAMAPPING_SET_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_SET"
    ADD CONSTRAINT "AO_3A3ECC_JIRAMAPPING_SET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_JIRAPROJECT_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAPROJECT_MAPPING"
    ADD CONSTRAINT "AO_3A3ECC_JIRAPROJECT_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_REMOTE_IDCF_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_REMOTE_IDCF"
    ADD CONSTRAINT "AO_3A3ECC_REMOTE_IDCF_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3A3ECC_REMOTE_LINK_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_REMOTE_LINK_CONFIG"
    ADD CONSTRAINT "AO_3A3ECC_REMOTE_LINK_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_3B1893_LOOP_DETECTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3B1893_LOOP_DETECTION"
    ADD CONSTRAINT "AO_3B1893_LOOP_DETECTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_4AEACD_WEBHOOK_DAO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4AEACD_WEBHOOK_DAO"
    ADD CONSTRAINT "AO_4AEACD_WEBHOOK_DAO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_4E8AE6_NOTIF_BATCH_QUEUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4E8AE6_NOTIF_BATCH_QUEUE"
    ADD CONSTRAINT "AO_4E8AE6_NOTIF_BATCH_QUEUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_4E8AE6_OUT_EMAIL_SETTINGS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_4E8AE6_OUT_EMAIL_SETTINGS"
    ADD CONSTRAINT "AO_4E8AE6_OUT_EMAIL_SETTINGS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_ASYNCUPGRADERECORD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ASYNCUPGRADERECORD"
    ADD CONSTRAINT "AO_54307E_ASYNCUPGRADERECORD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CAPABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CAPABILITY"
    ADD CONSTRAINT "AO_54307E_CAPABILITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CONFLUENCEKBENABLED_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBENABLED"
    ADD CONSTRAINT "AO_54307E_CONFLUENCEKBENABLED_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CONFLUENCEKBLABELS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBLABELS"
    ADD CONSTRAINT "AO_54307E_CONFLUENCEKBLABELS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CONFLUENCEKB_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKB"
    ADD CONSTRAINT "AO_54307E_CONFLUENCEKB_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CUSTOMGLOBALTHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CUSTOMGLOBALTHEME"
    ADD CONSTRAINT "AO_54307E_CUSTOMGLOBALTHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_CUSTOMTHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CUSTOMTHEME"
    ADD CONSTRAINT "AO_54307E_CUSTOMTHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_EMAILCHANNELSETTING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILCHANNELSETTING"
    ADD CONSTRAINT "AO_54307E_EMAILCHANNELSETTING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_EMAILSETTINGS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILSETTINGS"
    ADD CONSTRAINT "AO_54307E_EMAILSETTINGS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_GOAL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GOAL"
    ADD CONSTRAINT "AO_54307E_GOAL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_GROUPTOREQUESTTYPE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUPTOREQUESTTYPE"
    ADD CONSTRAINT "AO_54307E_GROUPTOREQUESTTYPE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_GROUP_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUP"
    ADD CONSTRAINT "AO_54307E_GROUP_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_IMAGES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_IMAGES"
    ADD CONSTRAINT "AO_54307E_IMAGES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_METRICCONDITION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_METRICCONDITION"
    ADD CONSTRAINT "AO_54307E_METRICCONDITION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_ORGANIZATION_MEMBER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_MEMBER"
    ADD CONSTRAINT "AO_54307E_ORGANIZATION_MEMBER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_ORGANIZATION_PROJECT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_PROJECT"
    ADD CONSTRAINT "AO_54307E_ORGANIZATION_PROJECT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_ORGANIZATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION"
    ADD CONSTRAINT "AO_54307E_ORGANIZATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_OUT_EMAIL_SETTINGS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_OUT_EMAIL_SETTINGS"
    ADD CONSTRAINT "AO_54307E_OUT_EMAIL_SETTINGS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_PARTICIPANTSETTINGS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_PARTICIPANTSETTINGS"
    ADD CONSTRAINT "AO_54307E_PARTICIPANTSETTINGS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_QUEUECOLUMN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_QUEUECOLUMN"
    ADD CONSTRAINT "AO_54307E_QUEUECOLUMN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_QUEUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_QUEUE"
    ADD CONSTRAINT "AO_54307E_QUEUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_REPORT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_REPORT"
    ADD CONSTRAINT "AO_54307E_REPORT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_SERIES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SERIES"
    ADD CONSTRAINT "AO_54307E_SERIES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_SERVICEDESK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SERVICEDESK"
    ADD CONSTRAINT "AO_54307E_SERVICEDESK_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_STATUSMAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_STATUSMAPPING"
    ADD CONSTRAINT "AO_54307E_STATUSMAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_SUBSCRIPTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SUBSCRIPTION"
    ADD CONSTRAINT "AO_54307E_SUBSCRIPTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_SYNCUPGRADERECORD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SYNCUPGRADERECORD"
    ADD CONSTRAINT "AO_54307E_SYNCUPGRADERECORD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_THRESHOLD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_THRESHOLD"
    ADD CONSTRAINT "AO_54307E_THRESHOLD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_TIMEMETRIC_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_TIMEMETRIC"
    ADD CONSTRAINT "AO_54307E_TIMEMETRIC_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_VIEWPORTFIELDVALUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELDVALUE"
    ADD CONSTRAINT "AO_54307E_VIEWPORTFIELDVALUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_VIEWPORTFIELD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELD"
    ADD CONSTRAINT "AO_54307E_VIEWPORTFIELD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_VIEWPORTFORM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFORM"
    ADD CONSTRAINT "AO_54307E_VIEWPORTFORM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_54307E_VIEWPORT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORT"
    ADD CONSTRAINT "AO_54307E_VIEWPORT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_550953_SHORTCUT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_550953_SHORTCUT"
    ADD CONSTRAINT "AO_550953_SHORTCUT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_563AEE_ACTIVITY_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity
    ADD CONSTRAINT "AO_563AEE_ACTIVITY_ENTITY_pkey" PRIMARY KEY (activity_id);


--
-- Name: AO_563AEE_ACTOR_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_actor_entity
    ADD CONSTRAINT "AO_563AEE_ACTOR_ENTITY_pkey" PRIMARY KEY (id);


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_media_link_entity
    ADD CONSTRAINT "AO_563AEE_MEDIA_LINK_ENTITY_pkey" PRIMARY KEY (id);


--
-- Name: AO_563AEE_OBJECT_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_object_entity
    ADD CONSTRAINT "AO_563AEE_OBJECT_ENTITY_pkey" PRIMARY KEY (id);


--
-- Name: AO_563AEE_TARGET_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_target_entity
    ADD CONSTRAINT "AO_563AEE_TARGET_ENTITY_pkey" PRIMARY KEY (id);


--
-- Name: AO_56464C_APPROVAL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVAL"
    ADD CONSTRAINT "AO_56464C_APPROVAL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_56464C_APPROVERDECISION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVERDECISION"
    ADD CONSTRAINT "AO_56464C_APPROVERDECISION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_56464C_APPROVER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVER"
    ADD CONSTRAINT "AO_56464C_APPROVER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_56464C_NOTIFICATIONRECORD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_NOTIFICATIONRECORD"
    ADD CONSTRAINT "AO_56464C_NOTIFICATIONRECORD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_575BF5_ISSUE_SUMMARY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_ISSUE_SUMMARY"
    ADD CONSTRAINT "AO_575BF5_ISSUE_SUMMARY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_575BF5_PROCESSED_COMMITS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_PROCESSED_COMMITS"
    ADD CONSTRAINT "AO_575BF5_PROCESSED_COMMITS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_575BF5_PROVIDER_ISSUE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_575BF5_PROVIDER_ISSUE"
    ADD CONSTRAINT "AO_575BF5_PROVIDER_ISSUE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_587B34_GLANCE_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_587B34_GLANCE_CONFIG"
    ADD CONSTRAINT "AO_587B34_GLANCE_CONFIG_pkey" PRIMARY KEY ("ROOM_ID");


--
-- Name: AO_587B34_PROJECT_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_587B34_PROJECT_CONFIG"
    ADD CONSTRAINT "AO_587B34_PROJECT_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_5FB9D7_AOHIP_CHAT_LINK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_5FB9D7_AOHIP_CHAT_LINK"
    ADD CONSTRAINT "AO_5FB9D7_AOHIP_CHAT_LINK_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_5FB9D7_AOHIP_CHAT_USER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_5FB9D7_AOHIP_CHAT_USER"
    ADD CONSTRAINT "AO_5FB9D7_AOHIP_CHAT_USER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_AUDITENTRY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_AUDITENTRY"
    ADD CONSTRAINT "AO_60DB71_AUDITENTRY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_BOARDADMINS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_BOARDADMINS"
    ADD CONSTRAINT "AO_60DB71_BOARDADMINS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_CARDCOLOR_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDCOLOR"
    ADD CONSTRAINT "AO_60DB71_CARDCOLOR_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_CARDLAYOUT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDLAYOUT"
    ADD CONSTRAINT "AO_60DB71_CARDLAYOUT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_COLUMNSTATUS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMNSTATUS"
    ADD CONSTRAINT "AO_60DB71_COLUMNSTATUS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_COLUMN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMN"
    ADD CONSTRAINT "AO_60DB71_COLUMN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_CREATIONCONVERSATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CREATIONCONVERSATION"
    ADD CONSTRAINT "AO_60DB71_CREATIONCONVERSATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_DETAILVIEWFIELD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_DETAILVIEWFIELD"
    ADD CONSTRAINT "AO_60DB71_DETAILVIEWFIELD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_ESTIMATESTATISTIC_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ESTIMATESTATISTIC"
    ADD CONSTRAINT "AO_60DB71_ESTIMATESTATISTIC_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_ISSUERANKINGLOG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ISSUERANKINGLOG"
    ADD CONSTRAINT "AO_60DB71_ISSUERANKINGLOG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_ISSUERANKING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ISSUERANKING"
    ADD CONSTRAINT "AO_60DB71_ISSUERANKING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_LEXORANKBALANCER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_LEXORANKBALANCER"
    ADD CONSTRAINT "AO_60DB71_LEXORANKBALANCER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_LEXORANK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_LEXORANK"
    ADD CONSTRAINT "AO_60DB71_LEXORANK_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_NONWORKINGDAY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_NONWORKINGDAY"
    ADD CONSTRAINT "AO_60DB71_NONWORKINGDAY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_QUICKFILTER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_QUICKFILTER"
    ADD CONSTRAINT "AO_60DB71_QUICKFILTER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_RANKABLEOBJECT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_RANKABLEOBJECT"
    ADD CONSTRAINT "AO_60DB71_RANKABLEOBJECT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_RAPIDVIEW_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_RAPIDVIEW"
    ADD CONSTRAINT "AO_60DB71_RAPIDVIEW_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_SPRINT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SPRINT"
    ADD CONSTRAINT "AO_60DB71_SPRINT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_STATSFIELD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_STATSFIELD"
    ADD CONSTRAINT "AO_60DB71_STATSFIELD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_SUBQUERY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SUBQUERY"
    ADD CONSTRAINT "AO_60DB71_SUBQUERY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_SWIMLANE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SWIMLANE"
    ADD CONSTRAINT "AO_60DB71_SWIMLANE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_TRACKINGSTATISTIC_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_TRACKINGSTATISTIC"
    ADD CONSTRAINT "AO_60DB71_TRACKINGSTATISTIC_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_VERSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_VERSION"
    ADD CONSTRAINT "AO_60DB71_VERSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_60DB71_WORKINGDAYS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_WORKINGDAYS"
    ADD CONSTRAINT "AO_60DB71_WORKINGDAYS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_68DACE_CONNECT_APPLICATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_CONNECT_APPLICATION"
    ADD CONSTRAINT "AO_68DACE_CONNECT_APPLICATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_68DACE_INSTALLATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_INSTALLATION"
    ADD CONSTRAINT "AO_68DACE_INSTALLATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_7A2604_CALENDAR_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_CALENDAR"
    ADD CONSTRAINT "AO_7A2604_CALENDAR_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_7A2604_HOLIDAY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_HOLIDAY"
    ADD CONSTRAINT "AO_7A2604_HOLIDAY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_7A2604_WORKINGTIME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_WORKINGTIME"
    ADD CONSTRAINT "AO_7A2604_WORKINGTIME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_ABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABILITY"
    ADD CONSTRAINT "AO_82B313_ABILITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_ABSENCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABSENCE"
    ADD CONSTRAINT "AO_82B313_ABSENCE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_AVAILABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_AVAILABILITY"
    ADD CONSTRAINT "AO_82B313_AVAILABILITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_INIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_INIT"
    ADD CONSTRAINT "AO_82B313_INIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_PERSON_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_PERSON"
    ADD CONSTRAINT "AO_82B313_PERSON_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_RESOURCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_RESOURCE"
    ADD CONSTRAINT "AO_82B313_RESOURCE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_SKILL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_SKILL"
    ADD CONSTRAINT "AO_82B313_SKILL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_82B313_TEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_TEAM"
    ADD CONSTRAINT "AO_82B313_TEAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_GRACE_PERIOD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_GRACE_PERIOD"
    ADD CONSTRAINT "AO_86ED1B_GRACE_PERIOD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_PROJECT_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_PROJECT_CONFIG"
    ADD CONSTRAINT "AO_86ED1B_PROJECT_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_STREAMS_ENTRY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_STREAMS_ENTRY"
    ADD CONSTRAINT "AO_86ED1B_STREAMS_ENTRY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_TIMEPLAN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMEPLAN"
    ADD CONSTRAINT "AO_86ED1B_TIMEPLAN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_TIMESHEET_APPROVAL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMESHEET_APPROVAL"
    ADD CONSTRAINT "AO_86ED1B_TIMESHEET_APPROVAL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_86ED1B_TIMESHEET_STATUS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_TIMESHEET_STATUS"
    ADD CONSTRAINT "AO_86ED1B_TIMESHEET_STATUS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_AGREEMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_AGREEMENT"
    ADD CONSTRAINT "AO_88DE6A_AGREEMENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_CONNECTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_CONNECTION"
    ADD CONSTRAINT "AO_88DE6A_CONNECTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_LICENSE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_LICENSE"
    ADD CONSTRAINT "AO_88DE6A_LICENSE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_MAPPING_BEAN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_MAPPING_BEAN"
    ADD CONSTRAINT "AO_88DE6A_MAPPING_BEAN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_MAPPING_ENTRY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_MAPPING_ENTRY"
    ADD CONSTRAINT "AO_88DE6A_MAPPING_ENTRY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_TRANSACTION_CONTENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_CONTENT"
    ADD CONSTRAINT "AO_88DE6A_TRANSACTION_CONTENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_TRANSACTION_LOG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_LOG"
    ADD CONSTRAINT "AO_88DE6A_TRANSACTION_LOG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_88DE6A_TRANSACTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION"
    ADD CONSTRAINT "AO_88DE6A_TRANSACTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_EXEC_RULE_MSG_ITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_EXEC_RULE_MSG_ITEM"
    ADD CONSTRAINT "AO_9B2E3B_EXEC_RULE_MSG_ITEM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_CONDITION_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_CONDITION_CONFIG"
    ADD CONSTRAINT "AO_9B2E3B_IF_CONDITION_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_COND_CONF_DATA_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_CONF_DATA"
    ADD CONSTRAINT "AO_9B2E3B_IF_COND_CONF_DATA_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_COND_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_IF_COND_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_IF_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_THEN_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_IF_THEN_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_IF_THEN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN"
    ADD CONSTRAINT "AO_9B2E3B_IF_THEN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_PROJECT_USER_CONTEXT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_PROJECT_USER_CONTEXT"
    ADD CONSTRAINT "AO_9B2E3B_PROJECT_USER_CONTEXT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RSETREV_PROJ_CONTEXT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_PROJ_CONTEXT"
    ADD CONSTRAINT "AO_9B2E3B_RSETREV_PROJ_CONTEXT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RSETREV_USER_CONTEXT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_USER_CONTEXT"
    ADD CONSTRAINT "AO_9B2E3B_RSETREV_USER_CONTEXT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RULESET_REVISION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULESET_REVISION"
    ADD CONSTRAINT "AO_9B2E3B_RULESET_REVISION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RULESET_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULESET"
    ADD CONSTRAINT "AO_9B2E3B_RULESET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RULE_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULE_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_RULE_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_RULE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULE"
    ADD CONSTRAINT "AO_9B2E3B_RULE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_THEN_ACTION_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACTION_CONFIG"
    ADD CONSTRAINT "AO_9B2E3B_THEN_ACTION_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_THEN_ACT_CONF_DATA_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_CONF_DATA"
    ADD CONSTRAINT "AO_9B2E3B_THEN_ACT_CONF_DATA_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_THEN_ACT_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_THEN_ACT_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_THEN_EXECUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_EXECUTION"
    ADD CONSTRAINT "AO_9B2E3B_THEN_EXECUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_WHEN_HANDLER_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HANDLER_CONFIG"
    ADD CONSTRAINT "AO_9B2E3B_WHEN_HANDLER_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_9B2E3B_WHEN_HAND_CONF_DATA_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HAND_CONF_DATA"
    ADD CONSTRAINT "AO_9B2E3B_WHEN_HAND_CONF_DATA_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_A0B856_WEB_HOOK_LISTENER_AO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_a0b856_web_hook_listener_ao
    ADD CONSTRAINT "AO_A0B856_WEB_HOOK_LISTENER_AO_pkey" PRIMARY KEY (id);


--
-- Name: AO_A415DF_AOABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABILITY"
    ADD CONSTRAINT "AO_A415DF_AOABILITY_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOABSENCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABSENCE"
    ADD CONSTRAINT "AO_A415DF_AOABSENCE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOAVAILABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOAVAILABILITY"
    ADD CONSTRAINT "AO_A415DF_AOAVAILABILITY_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOCONFIGURATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOCONFIGURATION"
    ADD CONSTRAINT "AO_A415DF_AOCONFIGURATION_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOCUSTOM_WORDING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOCUSTOM_WORDING"
    ADD CONSTRAINT "AO_A415DF_AOCUSTOM_WORDING_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AODEPENDENCY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AODEPENDENCY"
    ADD CONSTRAINT "AO_A415DF_AODEPENDENCY_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AODOOR_STOP_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AODOOR_STOP"
    ADD CONSTRAINT "AO_A415DF_AODOOR_STOP_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_A415DF_AOESTIMATE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOESTIMATE"
    ADD CONSTRAINT "AO_A415DF_AOESTIMATE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOEXTENSION_LINK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOEXTENSION_LINK"
    ADD CONSTRAINT "AO_A415DF_AOEXTENSION_LINK_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AONON_WORKING_DAYS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AONON_WORKING_DAYS"
    ADD CONSTRAINT "AO_A415DF_AONON_WORKING_DAYS_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOPERMISSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPERMISSION"
    ADD CONSTRAINT "AO_A415DF_AOPERMISSION_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOPERSON_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPERSON"
    ADD CONSTRAINT "AO_A415DF_AOPERSON_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOPLAN_CONFIGURATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPLAN_CONFIGURATION"
    ADD CONSTRAINT "AO_A415DF_AOPLAN_CONFIGURATION_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOPLAN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPLAN"
    ADD CONSTRAINT "AO_A415DF_AOPLAN_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOPRESENCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPRESENCE"
    ADD CONSTRAINT "AO_A415DF_AOPRESENCE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AORELEASE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORELEASE"
    ADD CONSTRAINT "AO_A415DF_AORELEASE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOREPLANNING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOREPLANNING"
    ADD CONSTRAINT "AO_A415DF_AOREPLANNING_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AORESOURCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORESOURCE"
    ADD CONSTRAINT "AO_A415DF_AORESOURCE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSKILL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSKILL"
    ADD CONSTRAINT "AO_A415DF_AOSKILL_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSOLUTION_STORE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSOLUTION_STORE"
    ADD CONSTRAINT "AO_A415DF_AOSOLUTION_STORE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSPRINT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSPRINT"
    ADD CONSTRAINT "AO_A415DF_AOSPRINT_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSTAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTAGE"
    ADD CONSTRAINT "AO_A415DF_AOSTAGE_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSTREAM_TO_TEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM_TO_TEAM"
    ADD CONSTRAINT "AO_A415DF_AOSTREAM_TO_TEAM_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOSTREAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM"
    ADD CONSTRAINT "AO_A415DF_AOSTREAM_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOTEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTEAM"
    ADD CONSTRAINT "AO_A415DF_AOTEAM_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOTHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTHEME"
    ADD CONSTRAINT "AO_A415DF_AOTHEME_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOWORK_ITEM_TO_RES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM_TO_RES"
    ADD CONSTRAINT "AO_A415DF_AOWORK_ITEM_TO_RES_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A415DF_AOWORK_ITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT "AO_A415DF_AOWORK_ITEM_pkey" PRIMARY KEY ("ID_OTHER");


--
-- Name: AO_A44657_HEALTH_CHECK_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A44657_HEALTH_CHECK_ENTITY"
    ADD CONSTRAINT "AO_A44657_HEALTH_CHECK_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_MEMBERSHIP_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_MEMBERSHIP"
    ADD CONSTRAINT "AO_AEFED0_MEMBERSHIP_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_PROGRAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_PROGRAM"
    ADD CONSTRAINT "AO_AEFED0_PROGRAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_LINK_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_LINK"
    ADD CONSTRAINT "AO_AEFED0_TEAM_LINK_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_MEMBER_V2_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_MEMBER_V2"
    ADD CONSTRAINT "AO_AEFED0_TEAM_MEMBER_V2_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_MEMBER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_MEMBER"
    ADD CONSTRAINT "AO_AEFED0_TEAM_MEMBER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_PERMISSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_PERMISSION"
    ADD CONSTRAINT "AO_AEFED0_TEAM_PERMISSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_ROLE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_ROLE"
    ADD CONSTRAINT "AO_AEFED0_TEAM_ROLE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_TO_MEMBER_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_TO_MEMBER"
    ADD CONSTRAINT "AO_AEFED0_TEAM_TO_MEMBER_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_V2_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_V2"
    ADD CONSTRAINT "AO_AEFED0_TEAM_V2_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_AEFED0_TEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM"
    ADD CONSTRAINT "AO_AEFED0_TEAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_B9A0F0_APPLIED_TEMPLATE"
    ADD CONSTRAINT "AO_B9A0F0_APPLIED_TEMPLATE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_ACCOUNT_V1_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_ACCOUNT_V1"
    ADD CONSTRAINT "AO_C3C6E8_ACCOUNT_V1_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_BUDGET_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_BUDGET"
    ADD CONSTRAINT "AO_C3C6E8_BUDGET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_CATEGORY_TYPE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_TYPE"
    ADD CONSTRAINT "AO_C3C6E8_CATEGORY_TYPE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_CATEGORY_V1_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_V1"
    ADD CONSTRAINT "AO_C3C6E8_CATEGORY_V1_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_CUSTOMER_PERMISSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_PERMISSION"
    ADD CONSTRAINT "AO_C3C6E8_CUSTOMER_PERMISSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_CUSTOMER_V1_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_V1"
    ADD CONSTRAINT "AO_C3C6E8_CUSTOMER_V1_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_LINK_V1_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_LINK_V1"
    ADD CONSTRAINT "AO_C3C6E8_LINK_V1_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_RATE_TABLE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE_TABLE"
    ADD CONSTRAINT "AO_C3C6E8_RATE_TABLE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C3C6E8_RATE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE"
    ADD CONSTRAINT "AO_C3C6E8_RATE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C7F17E_LINGO_REVISION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_REVISION"
    ADD CONSTRAINT "AO_C7F17E_LINGO_REVISION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C7F17E_LINGO_TRANSLATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_TRANSLATION"
    ADD CONSTRAINT "AO_C7F17E_LINGO_TRANSLATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_C7F17E_LINGO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO"
    ADD CONSTRAINT "AO_C7F17E_LINGO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_CFF990_AOTRANSITION_FAILURE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_CFF990_AOTRANSITION_FAILURE"
    ADD CONSTRAINT "AO_CFF990_AOTRANSITION_FAILURE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_ASSIGNMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ASSIGNMENT"
    ADD CONSTRAINT "AO_D9132D_ASSIGNMENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_CONFIGURATION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_CONFIGURATION"
    ADD CONSTRAINT "AO_D9132D_CONFIGURATION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_DEP_ISSUE_LINK_TYPES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DEP_ISSUE_LINK_TYPES"
    ADD CONSTRAINT "AO_D9132D_DEP_ISSUE_LINK_TYPES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_DISTRIBUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DISTRIBUTION"
    ADD CONSTRAINT "AO_D9132D_DISTRIBUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_EXCLUDED_VERSIONS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_EXCLUDED_VERSIONS"
    ADD CONSTRAINT "AO_D9132D_EXCLUDED_VERSIONS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_HIERARCHY_CONFIG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_HIERARCHY_CONFIG"
    ADD CONSTRAINT "AO_D9132D_HIERARCHY_CONFIG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_INIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_INIT"
    ADD CONSTRAINT "AO_D9132D_INIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_ISSUE_SOURCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ISSUE_SOURCE"
    ADD CONSTRAINT "AO_D9132D_ISSUE_SOURCE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_NONWORKINGDAYS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_NONWORKINGDAYS"
    ADD CONSTRAINT "AO_D9132D_NONWORKINGDAYS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PERMISSIONS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PERMISSIONS"
    ADD CONSTRAINT "AO_D9132D_PERMISSIONS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PLANSKILL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANSKILL"
    ADD CONSTRAINT "AO_D9132D_PLANSKILL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PLANTEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTEAM"
    ADD CONSTRAINT "AO_D9132D_PLANTEAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PLANTHEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTHEME"
    ADD CONSTRAINT "AO_D9132D_PLANTHEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PLANVERSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANVERSION"
    ADD CONSTRAINT "AO_D9132D_PLANVERSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_PLAN_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLAN"
    ADD CONSTRAINT "AO_D9132D_PLAN_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_RANK_ITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_RANK_ITEM"
    ADD CONSTRAINT "AO_D9132D_RANK_ITEM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_ABILITY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ABILITY"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_ABILITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_AVLBLTY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_AVLBLTY"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_AVLBLTY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_CHANGES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_CHANGES"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_CHANGES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_ISSUES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUES"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_ISSUES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_ISSUE_LINKS_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_LINKS"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_ISSUE_LINKS_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_ISSUE_RES_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_RES"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_ISSUE_RES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_PERSON_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_PERSON"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_PERSON_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_RESOURCE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_RESOURCE"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_RESOURCE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_SKILL_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_SKILL"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_SKILL_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_STAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_STAGE"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_STAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_TEAM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_TEAM"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_TEAM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_THEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_THEME"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_THEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_VERSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_VERSION"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_VERSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_XPVERSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_XPVERSION"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_XPVERSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SCENARIO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO"
    ADD CONSTRAINT "AO_D9132D_SCENARIO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_SOLUTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SOLUTION"
    ADD CONSTRAINT "AO_D9132D_SOLUTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_STAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_STAGE"
    ADD CONSTRAINT "AO_D9132D_STAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_THEME_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_THEME"
    ADD CONSTRAINT "AO_D9132D_THEME_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_VERSION_ENRICHMENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_VERSION_ENRICHMENT"
    ADD CONSTRAINT "AO_D9132D_VERSION_ENRICHMENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_D9132D_X_PROJECT_VERSION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_X_PROJECT_VERSION"
    ADD CONSTRAINT "AO_D9132D_X_PROJECT_VERSION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_DEB285_BLOG_AO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_DEB285_BLOG_AO"
    ADD CONSTRAINT "AO_DEB285_BLOG_AO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_DEB285_COMMENT_AO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_DEB285_COMMENT_AO"
    ADD CONSTRAINT "AO_DEB285_COMMENT_AO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_BRANCH_HEAD_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_BRANCH_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH"
    ADD CONSTRAINT "AO_E8B6CC_BRANCH_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_CHANGESET_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_CHANGESET_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_COMMIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT"
    ADD CONSTRAINT "AO_E8B6CC_COMMIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT"
    ADD CONSTRAINT "AO_E8B6CC_GIT_HUB_EVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_TO_BRANCH_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_TO_CHANGESET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_QUEUE_ITEM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_TAG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_TAG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ORGANIZATION_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_ORGANIZATION_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ORG_TO_PROJECT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ORG_TO_PROJECT"
    ADD CONSTRAINT "AO_E8B6CC_ORG_TO_PROJECT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_ISSUE_KEY"
    ADD CONSTRAINT "AO_E8B6CC_PR_ISSUE_KEY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_PARTICIPANT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT"
    ADD CONSTRAINT "AO_E8B6CC_PR_PARTICIPANT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_TO_COMMIT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT "AO_E8B6CC_PR_TO_COMMIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PULL_REQUEST_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PULL_REQUEST"
    ADD CONSTRAINT "AO_E8B6CC_PULL_REQUEST_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPOSITORY_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_REPOSITORY_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT "AO_E8B6CC_REPO_TO_CHANGESET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_REPO_TO_PROJECT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_PROJECT"
    ADD CONSTRAINT "AO_E8B6CC_REPO_TO_PROJECT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_AUDIT_LOG"
    ADD CONSTRAINT "AO_E8B6CC_SYNC_AUDIT_LOG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_SYNC_EVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_EVENT"
    ADD CONSTRAINT "AO_E8B6CC_SYNC_EVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_ED979B_ACTIONREGISTRY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_ACTIONREGISTRY"
    ADD CONSTRAINT "AO_ED979B_ACTIONREGISTRY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_ED979B_EVENTPROPERTY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_EVENTPROPERTY"
    ADD CONSTRAINT "AO_ED979B_EVENTPROPERTY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_ED979B_EVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_EVENT"
    ADD CONSTRAINT "AO_ED979B_EVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_ED979B_SUBSCRIPTION_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_SUBSCRIPTION"
    ADD CONSTRAINT "AO_ED979B_SUBSCRIPTION_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_ED979B_USEREVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_ED979B_USEREVENT"
    ADD CONSTRAINT "AO_ED979B_USEREVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F1B27B_HISTORY_RECORD_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_HISTORY_RECORD"
    ADD CONSTRAINT "AO_F1B27B_HISTORY_RECORD_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F1B27B_KEY_COMPONENT_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMPONENT"
    ADD CONSTRAINT "AO_F1B27B_KEY_COMPONENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F1B27B_KEY_COMP_HISTORY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMP_HISTORY"
    ADD CONSTRAINT "AO_F1B27B_KEY_COMP_HISTORY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F1B27B_PROMISE_HISTORY_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE_HISTORY"
    ADD CONSTRAINT "AO_F1B27B_PROMISE_HISTORY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F1B27B_PROMISE_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE"
    ADD CONSTRAINT "AO_F1B27B_PROMISE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_F4ED3A_ADD_ON_PROPERTY_AO_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F4ED3A_ADD_ON_PROPERTY_AO"
    ADD CONSTRAINT "AO_F4ED3A_ADD_ON_PROPERTY_AO_pkey" PRIMARY KEY ("ID");


--
-- Name: amq_temporary_store_a0b856_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY amq_temporary_store_a0b856
    ADD CONSTRAINT amq_temporary_store_a0b856_pkey PRIMARY KEY (id);


--
-- Name: connect_addon_dependencies_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_dependencies_f4ed3a
    ADD CONSTRAINT connect_addon_dependencies_f4ed3a_pkey PRIMARY KEY (addon_key, dependency_addon_key);


--
-- Name: connect_addon_descriptors_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_descriptors_f4ed3a
    ADD CONSTRAINT connect_addon_descriptors_f4ed3a_pkey PRIMARY KEY (addon_key);


--
-- Name: connect_addon_listings_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_listings_f4ed3a
    ADD CONSTRAINT connect_addon_listings_f4ed3a_pkey PRIMARY KEY (addon_key);


--
-- Name: connect_addon_remnant_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_remnant_f4ed3a
    ADD CONSTRAINT connect_addon_remnant_f4ed3a_pkey PRIMARY KEY (addon_key);


--
-- Name: connect_addon_scopes_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_scopes_f4ed3a
    ADD CONSTRAINT connect_addon_scopes_f4ed3a_pkey PRIMARY KEY (addon_key, scope);


--
-- Name: connect_addons_f4ed3a_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addons_f4ed3a
    ADD CONSTRAINT connect_addons_f4ed3a_pkey PRIMARY KEY (addon_key);


--
-- Name: flyway_schema_version_16a450_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_16a450
    ADD CONSTRAINT flyway_schema_version_16a450_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_182c39_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_182c39
    ADD CONSTRAINT flyway_schema_version_182c39_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_21d670_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_21d670
    ADD CONSTRAINT flyway_schema_version_21d670_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_3b1893_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_3b1893
    ADD CONSTRAINT flyway_schema_version_3b1893_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_550953_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_550953
    ADD CONSTRAINT flyway_schema_version_550953_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_563aee_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_563aee
    ADD CONSTRAINT flyway_schema_version_563aee_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_575bf5_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_575bf5
    ADD CONSTRAINT flyway_schema_version_575bf5_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_587b34_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_587b34
    ADD CONSTRAINT flyway_schema_version_587b34_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_60db71_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_60db71
    ADD CONSTRAINT flyway_schema_version_60db71_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_a0b856_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_a0b856
    ADD CONSTRAINT flyway_schema_version_a0b856_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_b59607_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_b59607
    ADD CONSTRAINT flyway_schema_version_b59607_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_c18b68_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_c18b68
    ADD CONSTRAINT flyway_schema_version_c18b68_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_deb285_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_deb285
    ADD CONSTRAINT flyway_schema_version_deb285_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_ec1a8f_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_ec1a8f
    ADD CONSTRAINT flyway_schema_version_ec1a8f_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_ecd6b3_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_ecd6b3
    ADD CONSTRAINT flyway_schema_version_ecd6b3_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_f4ed3a_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_f4ed3a
    ADD CONSTRAINT flyway_schema_version_f4ed3a_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_platform_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_platform
    ADD CONSTRAINT flyway_schema_version_platform_pk PRIMARY KEY (installed_rank);


--
-- Name: flyway_schema_version_plugins_pk; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY flyway_schema_version_plugins
    ADD CONSTRAINT flyway_schema_version_plugins_pk PRIMARY KEY (installed_rank);


--
-- Name: issue_field_option_unique_option_id_field_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option
    ADD CONSTRAINT issue_field_option_unique_option_id_field_key UNIQUE (option_id, field_key);


--
-- Name: jiraissuetokens_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraissuetokens
    ADD CONSTRAINT jiraissuetokens_pkey PRIMARY KEY (issueid, field);


--
-- Name: notification_subscription_ec1a8f_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notification_subscription_ec1a8f
    ADD CONSTRAINT notification_subscription_ec1a8f_pkey PRIMARY KEY (id);


--
-- Name: oauth_consumer_182c39_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_consumer_182c39
    ADD CONSTRAINT oauth_consumer_182c39_pkey PRIMARY KEY (key);


--
-- Name: oauth_consumer_182c39_service_name_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_consumer_182c39
    ADD CONSTRAINT oauth_consumer_182c39_service_name_key UNIQUE (service_name);


--
-- Name: oauth_consumer_token_182c39_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_consumer_token_182c39
    ADD CONSTRAINT oauth_consumer_token_182c39_pkey PRIMARY KEY (key);


--
-- Name: oauth_service_provider_consumer_ecd6b3_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_service_provider_consumer_ecd6b3
    ADD CONSTRAINT oauth_service_provider_consumer_ecd6b3_pkey PRIMARY KEY (key);


--
-- Name: oauth_service_provider_token_ecd6b3_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_service_provider_token_ecd6b3
    ADD CONSTRAINT oauth_service_provider_token_ecd6b3_pkey PRIMARY KEY (token);


--
-- Name: pk_addoncloudexport; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY addoncloudexport
    ADD CONSTRAINT pk_addoncloudexport PRIMARY KEY (id);


--
-- Name: pk_adhocupgradetaskhistory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY adhocupgradetaskhistory
    ADD CONSTRAINT pk_adhocupgradetaskhistory PRIMARY KEY (id);


--
-- Name: pk_app_user; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT pk_app_user PRIMARY KEY (id);


--
-- Name: pk_async_task; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY async_task
    ADD CONSTRAINT pk_async_task PRIMARY KEY (id);


--
-- Name: pk_async_task_payload; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY async_task_payload
    ADD CONSTRAINT pk_async_task_payload PRIMARY KEY (id);


--
-- Name: pk_audit_changed_value; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_changed_value
    ADD CONSTRAINT pk_audit_changed_value PRIMARY KEY (id);


--
-- Name: pk_audit_item; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_item
    ADD CONSTRAINT pk_audit_item PRIMARY KEY (id);


--
-- Name: pk_audit_log; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY audit_log
    ADD CONSTRAINT pk_audit_log PRIMARY KEY (id);


--
-- Name: pk_avatar; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY avatar
    ADD CONSTRAINT pk_avatar PRIMARY KEY (id);


--
-- Name: pk_board; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY board
    ADD CONSTRAINT pk_board PRIMARY KEY (id);


--
-- Name: pk_boardproject; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY boardproject
    ADD CONSTRAINT pk_boardproject PRIMARY KEY (board_id, project_id);


--
-- Name: pk_changegroup; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY changegroup
    ADD CONSTRAINT pk_changegroup PRIMARY KEY (id);


--
-- Name: pk_changeitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY changeitem
    ADD CONSTRAINT pk_changeitem PRIMARY KEY (id);


--
-- Name: pk_clusteredjob; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusteredjob
    ADD CONSTRAINT pk_clusteredjob PRIMARY KEY (id);


--
-- Name: pk_clusterlockstatus; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusterlockstatus
    ADD CONSTRAINT pk_clusterlockstatus PRIMARY KEY (id);


--
-- Name: pk_clustermessage; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clustermessage
    ADD CONSTRAINT pk_clustermessage PRIMARY KEY (id);


--
-- Name: pk_clusternode; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusternode
    ADD CONSTRAINT pk_clusternode PRIMARY KEY (node_id);


--
-- Name: pk_clusternodeheartbeat; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY clusternodeheartbeat
    ADD CONSTRAINT pk_clusternodeheartbeat PRIMARY KEY (node_id);


--
-- Name: pk_columnlayout; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY columnlayout
    ADD CONSTRAINT pk_columnlayout PRIMARY KEY (id);


--
-- Name: pk_columnlayoutitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY columnlayoutitem
    ADD CONSTRAINT pk_columnlayoutitem PRIMARY KEY (id);


--
-- Name: pk_component; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY component
    ADD CONSTRAINT pk_component PRIMARY KEY (id);


--
-- Name: pk_configurationcontext; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY configurationcontext
    ADD CONSTRAINT pk_configurationcontext PRIMARY KEY (id);


--
-- Name: pk_customfield; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfield
    ADD CONSTRAINT pk_customfield PRIMARY KEY (id);


--
-- Name: pk_customfieldoption; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfieldoption
    ADD CONSTRAINT pk_customfieldoption PRIMARY KEY (id);


--
-- Name: pk_customfieldvalue; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY customfieldvalue
    ADD CONSTRAINT pk_customfieldvalue PRIMARY KEY (id);


--
-- Name: pk_cwd_application; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_application
    ADD CONSTRAINT pk_cwd_application PRIMARY KEY (id);


--
-- Name: pk_cwd_application_address; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_application_address
    ADD CONSTRAINT pk_cwd_application_address PRIMARY KEY (application_id, remote_address);


--
-- Name: pk_cwd_directory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_directory
    ADD CONSTRAINT pk_cwd_directory PRIMARY KEY (id);


--
-- Name: pk_cwd_directory_attribute; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_directory_attribute
    ADD CONSTRAINT pk_cwd_directory_attribute PRIMARY KEY (directory_id, attribute_name);


--
-- Name: pk_cwd_directory_operation; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_directory_operation
    ADD CONSTRAINT pk_cwd_directory_operation PRIMARY KEY (directory_id, operation_type);


--
-- Name: pk_cwd_group; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_group
    ADD CONSTRAINT pk_cwd_group PRIMARY KEY (id);


--
-- Name: pk_cwd_group_attributes; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_group_attributes
    ADD CONSTRAINT pk_cwd_group_attributes PRIMARY KEY (id);


--
-- Name: pk_cwd_membership; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_membership
    ADD CONSTRAINT pk_cwd_membership PRIMARY KEY (id);


--
-- Name: pk_cwd_user; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_user
    ADD CONSTRAINT pk_cwd_user PRIMARY KEY (id);


--
-- Name: pk_cwd_user_attributes; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY cwd_user_attributes
    ADD CONSTRAINT pk_cwd_user_attributes PRIMARY KEY (id);


--
-- Name: pk_deadletter; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY deadletter
    ADD CONSTRAINT pk_deadletter PRIMARY KEY (id);


--
-- Name: pk_draftworkflowscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY draftworkflowscheme
    ADD CONSTRAINT pk_draftworkflowscheme PRIMARY KEY (id);


--
-- Name: pk_draftworkflowschemeentity; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY draftworkflowschemeentity
    ADD CONSTRAINT pk_draftworkflowschemeentity PRIMARY KEY (id);


--
-- Name: pk_entity_property; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property
    ADD CONSTRAINT pk_entity_property PRIMARY KEY (id);


--
-- Name: pk_entity_property_index; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_index
    ADD CONSTRAINT pk_entity_property_index PRIMARY KEY (id);


--
-- Name: pk_entity_property_index_docum; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_index_document
    ADD CONSTRAINT pk_entity_property_index_docum PRIMARY KEY (id);


--
-- Name: pk_entity_property_value; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_property_value
    ADD CONSTRAINT pk_entity_property_value PRIMARY KEY (id);


--
-- Name: pk_entity_translation; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY entity_translation
    ADD CONSTRAINT pk_entity_translation PRIMARY KEY (id);


--
-- Name: pk_external_entities; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY external_entities
    ADD CONSTRAINT pk_external_entities PRIMARY KEY (id);


--
-- Name: pk_externalgadget; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY externalgadget
    ADD CONSTRAINT pk_externalgadget PRIMARY KEY (id);


--
-- Name: pk_favouriteassociations; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY favouriteassociations
    ADD CONSTRAINT pk_favouriteassociations PRIMARY KEY (id);


--
-- Name: pk_feature; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY feature
    ADD CONSTRAINT pk_feature PRIMARY KEY (id);


--
-- Name: pk_fieldconfigscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfigscheme
    ADD CONSTRAINT pk_fieldconfigscheme PRIMARY KEY (id);


--
-- Name: pk_fieldconfigschemeissuetype; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfigschemeissuetype
    ADD CONSTRAINT pk_fieldconfigschemeissuetype PRIMARY KEY (id);


--
-- Name: pk_fieldconfiguration; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldconfiguration
    ADD CONSTRAINT pk_fieldconfiguration PRIMARY KEY (id);


--
-- Name: pk_fieldlayout; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayout
    ADD CONSTRAINT pk_fieldlayout PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutitem
    ADD CONSTRAINT pk_fieldlayoutitem PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutscheme
    ADD CONSTRAINT pk_fieldlayoutscheme PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutschemeassociatio; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutschemeassociation
    ADD CONSTRAINT pk_fieldlayoutschemeassociatio PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutschemeentity; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldlayoutschemeentity
    ADD CONSTRAINT pk_fieldlayoutschemeentity PRIMARY KEY (id);


--
-- Name: pk_fieldscope; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscope
    ADD CONSTRAINT pk_fieldscope PRIMARY KEY (field_id, issue_type_id, project_id);


--
-- Name: pk_fieldscreen; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreen
    ADD CONSTRAINT pk_fieldscreen PRIMARY KEY (id);


--
-- Name: pk_fieldscreenlayoutitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenlayoutitem
    ADD CONSTRAINT pk_fieldscreenlayoutitem PRIMARY KEY (id);


--
-- Name: pk_fieldscreenscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenscheme
    ADD CONSTRAINT pk_fieldscreenscheme PRIMARY KEY (id);


--
-- Name: pk_fieldscreenschemeitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreenschemeitem
    ADD CONSTRAINT pk_fieldscreenschemeitem PRIMARY KEY (id);


--
-- Name: pk_fieldscreentab; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fieldscreentab
    ADD CONSTRAINT pk_fieldscreentab PRIMARY KEY (id);


--
-- Name: pk_fileattachment; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY fileattachment
    ADD CONSTRAINT pk_fileattachment PRIMARY KEY (id);


--
-- Name: pk_filtersubscription; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY filtersubscription
    ADD CONSTRAINT pk_filtersubscription PRIMARY KEY (id);


--
-- Name: pk_gadgetuserpreference; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY gadgetuserpreference
    ADD CONSTRAINT pk_gadgetuserpreference PRIMARY KEY (id);


--
-- Name: pk_genericconfiguration; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY genericconfiguration
    ADD CONSTRAINT pk_genericconfiguration PRIMARY KEY (id);


--
-- Name: pk_globalpermissionentry; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY globalpermissionentry
    ADD CONSTRAINT pk_globalpermissionentry PRIMARY KEY (id);


--
-- Name: pk_groupbase; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY groupbase
    ADD CONSTRAINT pk_groupbase PRIMARY KEY (id);


--
-- Name: pk_issue_field_option; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option
    ADD CONSTRAINT pk_issue_field_option PRIMARY KEY (id);


--
-- Name: pk_issue_field_option_attr; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option_attr
    ADD CONSTRAINT pk_issue_field_option_attr PRIMARY KEY (id);


--
-- Name: pk_issue_field_option_scope; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issue_field_option_scope
    ADD CONSTRAINT pk_issue_field_option_scope PRIMARY KEY (id);


--
-- Name: pk_issuelink; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuelink
    ADD CONSTRAINT pk_issuelink PRIMARY KEY (id);


--
-- Name: pk_issuelinktype; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuelinktype
    ADD CONSTRAINT pk_issuelinktype PRIMARY KEY (id);


--
-- Name: pk_issuesecurityscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuesecurityscheme
    ADD CONSTRAINT pk_issuesecurityscheme PRIMARY KEY (id);


--
-- Name: pk_issuestatus; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuestatus
    ADD CONSTRAINT pk_issuestatus PRIMARY KEY (id);


--
-- Name: pk_issuetype; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetype
    ADD CONSTRAINT pk_issuetype PRIMARY KEY (id);


--
-- Name: pk_issuetypescreenscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetypescreenscheme
    ADD CONSTRAINT pk_issuetypescreenscheme PRIMARY KEY (id);


--
-- Name: pk_issuetypescreenschemeentity; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY issuetypescreenschemeentity
    ADD CONSTRAINT pk_issuetypescreenschemeentity PRIMARY KEY (id);


--
-- Name: pk_jiraaction; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraaction
    ADD CONSTRAINT pk_jiraaction PRIMARY KEY (id);


--
-- Name: pk_jiradraftworkflows; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiradraftworkflows
    ADD CONSTRAINT pk_jiradraftworkflows PRIMARY KEY (id);


--
-- Name: pk_jiraeventtype; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraeventtype
    ADD CONSTRAINT pk_jiraeventtype PRIMARY KEY (id);


--
-- Name: pk_jiraissue; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraissue
    ADD CONSTRAINT pk_jiraissue PRIMARY KEY (id);


--
-- Name: pk_jiraperms; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraperms
    ADD CONSTRAINT pk_jiraperms PRIMARY KEY (id);


--
-- Name: pk_jiraworkflows; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraworkflows
    ADD CONSTRAINT pk_jiraworkflows PRIMARY KEY (id);


--
-- Name: pk_jiraworkflowstatuses; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraworkflowstatuses
    ADD CONSTRAINT pk_jiraworkflowstatuses PRIMARY KEY (id);


--
-- Name: pk_jquartz_blob_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_blob_triggers
    ADD CONSTRAINT pk_jquartz_blob_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_calendars; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_calendars
    ADD CONSTRAINT pk_jquartz_calendars PRIMARY KEY (calendar_name);


--
-- Name: pk_jquartz_cron_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_cron_triggers
    ADD CONSTRAINT pk_jquartz_cron_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_fired_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_fired_triggers
    ADD CONSTRAINT pk_jquartz_fired_triggers PRIMARY KEY (entry_id);


--
-- Name: pk_jquartz_job_details; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_job_details
    ADD CONSTRAINT pk_jquartz_job_details PRIMARY KEY (job_name, job_group);


--
-- Name: pk_jquartz_job_listeners; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_job_listeners
    ADD CONSTRAINT pk_jquartz_job_listeners PRIMARY KEY (job_name, job_group, job_listener);


--
-- Name: pk_jquartz_locks; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_locks
    ADD CONSTRAINT pk_jquartz_locks PRIMARY KEY (lock_name);


--
-- Name: pk_jquartz_paused_trigger_grps; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_paused_trigger_grps
    ADD CONSTRAINT pk_jquartz_paused_trigger_grps PRIMARY KEY (trigger_group);


--
-- Name: pk_jquartz_scheduler_state; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_scheduler_state
    ADD CONSTRAINT pk_jquartz_scheduler_state PRIMARY KEY (instance_name);


--
-- Name: pk_jquartz_simple_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_simple_triggers
    ADD CONSTRAINT pk_jquartz_simple_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_simprop_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_simprop_triggers
    ADD CONSTRAINT pk_jquartz_simprop_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_trigger_listeners; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_trigger_listeners
    ADD CONSTRAINT pk_jquartz_trigger_listeners PRIMARY KEY (trigger_group, trigger_listener);


--
-- Name: pk_jquartz_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jquartz_triggers
    ADD CONSTRAINT pk_jquartz_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_label; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY label
    ADD CONSTRAINT pk_label PRIMARY KEY (id);


--
-- Name: pk_legacy_store_attachment_sta; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY legacy_store_attachment_status
    ADD CONSTRAINT pk_legacy_store_attachment_sta PRIMARY KEY (attachment);


--
-- Name: pk_legacy_store_avatar_status; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY legacy_store_avatar_status
    ADD CONSTRAINT pk_legacy_store_avatar_status PRIMARY KEY (avatar);


--
-- Name: pk_licenserolesdefault; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY licenserolesdefault
    ADD CONSTRAINT pk_licenserolesdefault PRIMARY KEY (id);


--
-- Name: pk_licenserolesgroup; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY licenserolesgroup
    ADD CONSTRAINT pk_licenserolesgroup PRIMARY KEY (id);


--
-- Name: pk_listenerconfig; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY listenerconfig
    ADD CONSTRAINT pk_listenerconfig PRIMARY KEY (id);


--
-- Name: pk_mailserver; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT pk_mailserver PRIMARY KEY (id);


--
-- Name: pk_managedconfigurationitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY managedconfigurationitem
    ADD CONSTRAINT pk_managedconfigurationitem PRIMARY KEY (id);


--
-- Name: pk_media_api_credentials; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY media_api_credentials
    ADD CONSTRAINT pk_media_api_credentials PRIMARY KEY (id);


--
-- Name: pk_media_store_attachments; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY media_store_attachments
    ADD CONSTRAINT pk_media_store_attachments PRIMARY KEY (attachment);


--
-- Name: pk_media_store_avatars; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY media_store_avatars
    ADD CONSTRAINT pk_media_store_avatars PRIMARY KEY (avatar, size);


--
-- Name: pk_membershipbase; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY membershipbase
    ADD CONSTRAINT pk_membershipbase PRIMARY KEY (id);


--
-- Name: pk_module_status; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY module_status
    ADD CONSTRAINT pk_module_status PRIMARY KEY (plugin_key, module_key);


--
-- Name: pk_moved_issue_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY moved_issue_key
    ADD CONSTRAINT pk_moved_issue_key PRIMARY KEY (id);


--
-- Name: pk_nodeassociation; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY nodeassociation
    ADD CONSTRAINT pk_nodeassociation PRIMARY KEY (source_node_id, source_node_entity, sink_node_id, sink_node_entity, association_type);


--
-- Name: pk_nodeindexcounter; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY nodeindexcounter
    ADD CONSTRAINT pk_nodeindexcounter PRIMARY KEY (id);


--
-- Name: pk_notification; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT pk_notification PRIMARY KEY (id);


--
-- Name: pk_notificationinstance; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notificationinstance
    ADD CONSTRAINT pk_notificationinstance PRIMARY KEY (id);


--
-- Name: pk_notificationscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY notificationscheme
    ADD CONSTRAINT pk_notificationscheme PRIMARY KEY (id);


--
-- Name: pk_oauthconsumer; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthconsumer
    ADD CONSTRAINT pk_oauthconsumer PRIMARY KEY (id);


--
-- Name: pk_oauthconsumertoken; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthconsumertoken
    ADD CONSTRAINT pk_oauthconsumertoken PRIMARY KEY (id);


--
-- Name: pk_oauthspconsumer; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthspconsumer
    ADD CONSTRAINT pk_oauthspconsumer PRIMARY KEY (id);


--
-- Name: pk_oauthsptoken; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauthsptoken
    ADD CONSTRAINT pk_oauthsptoken PRIMARY KEY (id);


--
-- Name: pk_optionconfiguration; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY optionconfiguration
    ADD CONSTRAINT pk_optionconfiguration PRIMARY KEY (id);


--
-- Name: pk_os_currentstep; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_currentstep
    ADD CONSTRAINT pk_os_currentstep PRIMARY KEY (id);


--
-- Name: pk_os_currentstep_prev; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_currentstep_prev
    ADD CONSTRAINT pk_os_currentstep_prev PRIMARY KEY (id, previous_id);


--
-- Name: pk_os_historystep; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_historystep
    ADD CONSTRAINT pk_os_historystep PRIMARY KEY (id);


--
-- Name: pk_os_historystep_prev; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_historystep_prev
    ADD CONSTRAINT pk_os_historystep_prev PRIMARY KEY (id, previous_id);


--
-- Name: pk_os_wfentry; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY os_wfentry
    ADD CONSTRAINT pk_os_wfentry PRIMARY KEY (id);


--
-- Name: pk_permissionscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY permissionscheme
    ADD CONSTRAINT pk_permissionscheme PRIMARY KEY (id);


--
-- Name: pk_pluginstate; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY pluginstate
    ADD CONSTRAINT pk_pluginstate PRIMARY KEY (pluginkey);


--
-- Name: pk_pluginversion; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY pluginversion
    ADD CONSTRAINT pk_pluginversion PRIMARY KEY (id);


--
-- Name: pk_portalpage; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY portalpage
    ADD CONSTRAINT pk_portalpage PRIMARY KEY (id);


--
-- Name: pk_portletconfiguration; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY portletconfiguration
    ADD CONSTRAINT pk_portletconfiguration PRIMARY KEY (id);


--
-- Name: pk_priority; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT pk_priority PRIMARY KEY (id);


--
-- Name: pk_productlicense; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY productlicense
    ADD CONSTRAINT pk_productlicense PRIMARY KEY (id);


--
-- Name: pk_project; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY project
    ADD CONSTRAINT pk_project PRIMARY KEY (id);


--
-- Name: pk_project_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY project_key
    ADD CONSTRAINT pk_project_key PRIMARY KEY (id);


--
-- Name: pk_projectcategory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectcategory
    ADD CONSTRAINT pk_projectcategory PRIMARY KEY (id);


--
-- Name: pk_projectchangedtime; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectchangedtime
    ADD CONSTRAINT pk_projectchangedtime PRIMARY KEY (project_id);


--
-- Name: pk_projectrole; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectrole
    ADD CONSTRAINT pk_projectrole PRIMARY KEY (id);


--
-- Name: pk_projectroleactor; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectroleactor
    ADD CONSTRAINT pk_projectroleactor PRIMARY KEY (id);


--
-- Name: pk_projectversion; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY projectversion
    ADD CONSTRAINT pk_projectversion PRIMARY KEY (id);


--
-- Name: pk_propertydata; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertydata
    ADD CONSTRAINT pk_propertydata PRIMARY KEY (id);


--
-- Name: pk_propertydate; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertydate
    ADD CONSTRAINT pk_propertydate PRIMARY KEY (id);


--
-- Name: pk_propertydecimal; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertydecimal
    ADD CONSTRAINT pk_propertydecimal PRIMARY KEY (id);


--
-- Name: pk_propertyentry; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertyentry
    ADD CONSTRAINT pk_propertyentry PRIMARY KEY (id);


--
-- Name: pk_propertynumber; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertynumber
    ADD CONSTRAINT pk_propertynumber PRIMARY KEY (id);


--
-- Name: pk_propertystring; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertystring
    ADD CONSTRAINT pk_propertystring PRIMARY KEY (id);


--
-- Name: pk_propertytext; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY propertytext
    ADD CONSTRAINT pk_propertytext PRIMARY KEY (id);


--
-- Name: pk_qrtz_calendars; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT pk_qrtz_calendars PRIMARY KEY (calendar_name);


--
-- Name: pk_qrtz_cron_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT pk_qrtz_cron_triggers PRIMARY KEY (id);


--
-- Name: pk_qrtz_fired_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT pk_qrtz_fired_triggers PRIMARY KEY (entry_id);


--
-- Name: pk_qrtz_job_details; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT pk_qrtz_job_details PRIMARY KEY (id);


--
-- Name: pk_qrtz_job_listeners; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT pk_qrtz_job_listeners PRIMARY KEY (id);


--
-- Name: pk_qrtz_simple_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT pk_qrtz_simple_triggers PRIMARY KEY (id);


--
-- Name: pk_qrtz_trigger_listeners; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT pk_qrtz_trigger_listeners PRIMARY KEY (id);


--
-- Name: pk_qrtz_triggers; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT pk_qrtz_triggers PRIMARY KEY (id);


--
-- Name: pk_reindex_component; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY reindex_component
    ADD CONSTRAINT pk_reindex_component PRIMARY KEY (id);


--
-- Name: pk_reindex_request; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY reindex_request
    ADD CONSTRAINT pk_reindex_request PRIMARY KEY (id);


--
-- Name: pk_remembermetoken; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY remembermetoken
    ADD CONSTRAINT pk_remembermetoken PRIMARY KEY (id);


--
-- Name: pk_remotelink; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY remotelink
    ADD CONSTRAINT pk_remotelink PRIMARY KEY (id);


--
-- Name: pk_replicatedindexoperation; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY replicatedindexoperation
    ADD CONSTRAINT pk_replicatedindexoperation PRIMARY KEY (id);


--
-- Name: pk_resolution; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY resolution
    ADD CONSTRAINT pk_resolution PRIMARY KEY (id);


--
-- Name: pk_rundetails; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY rundetails
    ADD CONSTRAINT pk_rundetails PRIMARY KEY (id);


--
-- Name: pk_savedfiltermigrationbackup; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY savedfiltermigrationbackup
    ADD CONSTRAINT pk_savedfiltermigrationbackup PRIMARY KEY (id);


--
-- Name: pk_schemeissuesecurities; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemeissuesecurities
    ADD CONSTRAINT pk_schemeissuesecurities PRIMARY KEY (id);


--
-- Name: pk_schemeissuesecuritylevels; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemeissuesecuritylevels
    ADD CONSTRAINT pk_schemeissuesecuritylevels PRIMARY KEY (id);


--
-- Name: pk_schemepermissions; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY schemepermissions
    ADD CONSTRAINT pk_schemepermissions PRIMARY KEY (id);


--
-- Name: pk_searchrequest; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY searchrequest
    ADD CONSTRAINT pk_searchrequest PRIMARY KEY (id);


--
-- Name: pk_sequence_value_item; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY sequence_value_item
    ADD CONSTRAINT pk_sequence_value_item PRIMARY KEY (seq_name);


--
-- Name: pk_serviceconfig; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY serviceconfig
    ADD CONSTRAINT pk_serviceconfig PRIMARY KEY (id);


--
-- Name: pk_sharepermissions; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY sharepermissions
    ADD CONSTRAINT pk_sharepermissions PRIMARY KEY (id);


--
-- Name: pk_tempattachmentsmonitor; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY tempattachmentsmonitor
    ADD CONSTRAINT pk_tempattachmentsmonitor PRIMARY KEY (temporary_attachment_id);


--
-- Name: pk_tenant_properties; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY tenant_properties
    ADD CONSTRAINT pk_tenant_properties PRIMARY KEY (id);


--
-- Name: pk_trackback_ping; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY trackback_ping
    ADD CONSTRAINT pk_trackback_ping PRIMARY KEY (id);


--
-- Name: pk_trustedapp; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY trustedapp
    ADD CONSTRAINT pk_trustedapp PRIMARY KEY (id);


--
-- Name: pk_upgradehistory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradehistory
    ADD CONSTRAINT pk_upgradehistory PRIMARY KEY (upgradeclass);


--
-- Name: pk_upgradetaskhistory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradetaskhistory
    ADD CONSTRAINT pk_upgradetaskhistory PRIMARY KEY (id);


--
-- Name: pk_upgradetaskhistoryauditlog; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradetaskhistoryauditlog
    ADD CONSTRAINT pk_upgradetaskhistoryauditlog PRIMARY KEY (id);


--
-- Name: pk_upgradeversionhistory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY upgradeversionhistory
    ADD CONSTRAINT pk_upgradeversionhistory PRIMARY KEY (targetbuild);


--
-- Name: pk_userassociation; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userassociation
    ADD CONSTRAINT pk_userassociation PRIMARY KEY (source_name, sink_node_id, sink_node_entity, association_type);


--
-- Name: pk_userbase; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userbase
    ADD CONSTRAINT pk_userbase PRIMARY KEY (id);


--
-- Name: pk_userhistoryitem; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userhistoryitem
    ADD CONSTRAINT pk_userhistoryitem PRIMARY KEY (id);


--
-- Name: pk_userpickerfilter; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfilter
    ADD CONSTRAINT pk_userpickerfilter PRIMARY KEY (id);


--
-- Name: pk_userpickerfiltergroup; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfiltergroup
    ADD CONSTRAINT pk_userpickerfiltergroup PRIMARY KEY (id);


--
-- Name: pk_userpickerfilterrole; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY userpickerfilterrole
    ADD CONSTRAINT pk_userpickerfilterrole PRIMARY KEY (id);


--
-- Name: pk_versioncontrol; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY versioncontrol
    ADD CONSTRAINT pk_versioncontrol PRIMARY KEY (id);


--
-- Name: pk_votehistory; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY votehistory
    ADD CONSTRAINT pk_votehistory PRIMARY KEY (id);


--
-- Name: pk_workflowscheme; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY workflowscheme
    ADD CONSTRAINT pk_workflowscheme PRIMARY KEY (id);


--
-- Name: pk_workflowschemeentity; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY workflowschemeentity
    ADD CONSTRAINT pk_workflowschemeentity PRIMARY KEY (id);


--
-- Name: pk_worklog; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY worklog
    ADD CONSTRAINT pk_worklog PRIMARY KEY (id);


--
-- Name: t_cd909f_media_store_logos_pkey; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY t_cd909f_media_store_logos
    ADD CONSTRAINT t_cd909f_media_store_logos_pkey PRIMARY KEY (file_type);


--
-- Name: u_ao_013613_exp_category_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXP_CATEGORY"
    ADD CONSTRAINT u_ao_013613_exp_category_name UNIQUE ("NAME");


--
-- Name: u_ao_013613_hd_sche903392985; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_MEMBER"
    ADD CONSTRAINT u_ao_013613_hd_sche903392985 UNIQUE ("USER_KEY");


--
-- Name: u_ao_013613_project417523398; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_PROJECT_CONFIG"
    ADD CONSTRAINT u_ao_013613_project417523398 UNIQUE ("PROJECT_ID");


--
-- Name: u_ao_013613_wl_sche1322162621; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_MEMBER"
    ADD CONSTRAINT u_ao_013613_wl_sche1322162621 UNIQUE ("MEMBER_KEY");


--
-- Name: u_ao_013613_wl_scheme_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME"
    ADD CONSTRAINT u_ao_013613_wl_scheme_name UNIQUE ("NAME");


--
-- Name: u_ao_013613_work_attribute_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WORK_ATTRIBUTE"
    ADD CONSTRAINT u_ao_013613_work_attribute_key UNIQUE ("KEY");


--
-- Name: u_ao_319474_queue_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE"
    ADD CONSTRAINT u_ao_319474_queue_name UNIQUE ("NAME");


--
-- Name: u_ao_54307e_organiz2022034244; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION"
    ADD CONSTRAINT u_ao_54307e_organiz2022034244 UNIQUE ("SEARCH_NAME");


--
-- Name: u_ao_54307e_organization_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION"
    ADD CONSTRAINT u_ao_54307e_organization_name UNIQUE ("NAME");


--
-- Name: u_ao_54307e_viewport_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORT"
    ADD CONSTRAINT u_ao_54307e_viewport_key UNIQUE ("KEY");


--
-- Name: u_ao_587b34_project2070954277; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_587B34_PROJECT_CONFIG"
    ADD CONSTRAINT u_ao_587b34_project2070954277 UNIQUE ("NAME_UNIQUE_CONSTRAINT");


--
-- Name: u_ao_60db71_creatio630901986; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CREATIONCONVERSATION"
    ADD CONSTRAINT u_ao_60db71_creatio630901986 UNIQUE ("TOKEN");


--
-- Name: u_ao_68dace_connect479239301; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_CONNECT_APPLICATION"
    ADD CONSTRAINT u_ao_68dace_connect479239301 UNIQUE ("APPLICATION_ID");


--
-- Name: u_ao_68dace_install1682326745; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_INSTALLATION"
    ADD CONSTRAINT u_ao_68dace_install1682326745 UNIQUE ("CLIENT_KEY");


--
-- Name: u_ao_82b313_ability1384204123; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABILITY"
    ADD CONSTRAINT u_ao_82b313_ability1384204123 UNIQUE ("COMBINED_KEY");


--
-- Name: u_ao_82b313_init_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_INIT"
    ADD CONSTRAINT u_ao_82b313_init_key UNIQUE ("KEY");


--
-- Name: u_ao_86ed1b_grace_p168218652; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_GRACE_PERIOD"
    ADD CONSTRAINT u_ao_86ed1b_grace_p168218652 UNIQUE ("RECEIVER");


--
-- Name: u_ao_86ed1b_project1832082062; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_86ED1B_PROJECT_CONFIG"
    ADD CONSTRAINT u_ao_86ed1b_project1832082062 UNIQUE ("PROJECT_ID");


--
-- Name: u_ao_a415df_aosolut752937716; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSOLUTION_STORE"
    ADD CONSTRAINT u_ao_a415df_aosolut752937716 UNIQUE ("AOPLAN_ID");


--
-- Name: u_ao_aefed0_program_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_PROGRAM"
    ADD CONSTRAINT u_ao_aefed0_program_name UNIQUE ("NAME");


--
-- Name: u_ao_aefed0_team_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM"
    ADD CONSTRAINT u_ao_aefed0_team_name UNIQUE ("NAME");


--
-- Name: u_ao_aefed0_team_v2_name; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_V2"
    ADD CONSTRAINT u_ao_aefed0_team_v2_name UNIQUE ("NAME");


--
-- Name: u_ao_c3c6e8_account_v1_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_ACCOUNT_V1"
    ADD CONSTRAINT u_ao_c3c6e8_account_v1_key UNIQUE ("KEY");


--
-- Name: u_ao_c3c6e8_category_v1_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_V1"
    ADD CONSTRAINT u_ao_c3c6e8_category_v1_key UNIQUE ("KEY");


--
-- Name: u_ao_c3c6e8_customer_v1_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_V1"
    ADD CONSTRAINT u_ao_c3c6e8_customer_v1_key UNIQUE ("KEY");


--
-- Name: u_ao_d9132d_dep_iss229485530; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DEP_ISSUE_LINK_TYPES"
    ADD CONSTRAINT u_ao_d9132d_dep_iss229485530 UNIQUE ("LINK_ID");


--
-- Name: u_ao_d9132d_init_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_INIT"
    ADD CONSTRAINT u_ao_d9132d_init_key UNIQUE ("KEY");


--
-- Name: u_ao_d9132d_planskill_c_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANSKILL"
    ADD CONSTRAINT u_ao_d9132d_planskill_c_key UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_plantheme_c_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTHEME"
    ADD CONSTRAINT u_ao_d9132d_plantheme_c_key UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_planversion_c_key; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANVERSION"
    ADD CONSTRAINT u_ao_d9132d_planversion_c_key UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_rank_item_unique; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_RANK_ITEM"
    ADD CONSTRAINT u_ao_d9132d_rank_item_unique UNIQUE ("UNIQUE");


--
-- Name: u_ao_d9132d_scenari1025062113; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_TEAM"
    ADD CONSTRAINT u_ao_d9132d_scenari1025062113 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari106138549; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_SKILL"
    ADD CONSTRAINT u_ao_d9132d_scenari106138549 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari1142118530; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUES"
    ADD CONSTRAINT u_ao_d9132d_scenari1142118530 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari1228886055; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_PERSON"
    ADD CONSTRAINT u_ao_d9132d_scenari1228886055 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari1370120444; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_VERSION"
    ADD CONSTRAINT u_ao_d9132d_scenari1370120444 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari1516406944; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ABILITY"
    ADD CONSTRAINT u_ao_d9132d_scenari1516406944 UNIQUE ("U_AB");


--
-- Name: u_ao_d9132d_scenari1658830125; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_THEME"
    ADD CONSTRAINT u_ao_d9132d_scenari1658830125 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari1732287022; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_RESOURCE"
    ADD CONSTRAINT u_ao_d9132d_scenari1732287022 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari2129424734; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_STAGE"
    ADD CONSTRAINT u_ao_d9132d_scenari2129424734 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari219411406; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ABILITY"
    ADD CONSTRAINT u_ao_d9132d_scenari219411406 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari577680729; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_CHANGES"
    ADD CONSTRAINT u_ao_d9132d_scenari577680729 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari887167849; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_LINKS"
    ADD CONSTRAINT u_ao_d9132d_scenari887167849 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_scenari913320196; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_XPVERSION"
    ADD CONSTRAINT u_ao_d9132d_scenari913320196 UNIQUE ("C_KEY");


--
-- Name: u_ao_d9132d_solutio277170766; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SOLUTION"
    ADD CONSTRAINT u_ao_d9132d_solutio277170766 UNIQUE ("UNIQUE_GUARD");


--
-- Name: u_ao_d9132d_version472426003; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_VERSION_ENRICHMENT"
    ADD CONSTRAINT u_ao_d9132d_version472426003 UNIQUE ("ENV_ID");


--
-- Name: u_ao_f1b27b_promise1620912431; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE_HISTORY"
    ADD CONSTRAINT u_ao_f1b27b_promise1620912431 UNIQUE ("KEY_HASH");


--
-- Name: u_ao_f1b27b_promise_key_hash; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_PROMISE"
    ADD CONSTRAINT u_ao_f1b27b_promise_key_hash UNIQUE ("KEY_HASH");


--
-- Name: u_ao_f4ed3a_add_on_1238639798; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F4ED3A_ADD_ON_PROPERTY_AO"
    ADD CONSTRAINT u_ao_f4ed3a_add_on_1238639798 UNIQUE ("PRIMARY_KEY");


--
-- Name: unique_workflowname; Type: CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY jiraworkflows
    ADD CONSTRAINT unique_workflowname UNIQUE (workflowname);


--
-- Name: action_actiontype; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX action_actiontype ON jiraaction USING btree (actiontype);


--
-- Name: action_author_created; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX action_author_created ON jiraaction USING btree (author, created);


--
-- Name: action_comment; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX action_comment ON jiraaction USING gin (actionbody tools.gin_trgm_ops);


--
-- Name: action_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX action_issue ON jiraaction USING btree (issueid);


--
-- Name: async_task_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX async_task_id ON async_task_payload USING btree (async_task_id);


--
-- Name: attach_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX attach_issue ON fileattachment USING btree (issueid);


--
-- Name: avatar_filename_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX avatar_filename_index ON avatar USING btree (filename, avatartype, systemavatar);


--
-- Name: avatar_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX avatar_index ON avatar USING btree (avatartype, owner);


--
-- Name: cf_cfoption; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cf_cfoption ON customfieldoption USING btree (customfield);


--
-- Name: cf_userpickerfiltergroup; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cf_userpickerfiltergroup ON userpickerfiltergroup USING btree (userpickerfilter);


--
-- Name: cf_userpickerfilterrole; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cf_userpickerfilterrole ON userpickerfilterrole USING btree (userpickerfilter);


--
-- Name: cfvalue_customfield; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_customfield ON customfieldvalue USING btree (customfield);


--
-- Name: cfvalue_datevalue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_datevalue ON customfieldvalue USING btree (datevalue);


--
-- Name: cfvalue_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_issue ON customfieldvalue USING btree (issue, customfield);


--
-- Name: cfvalue_numbervalue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_numbervalue ON customfieldvalue USING btree (numbervalue);


--
-- Name: cfvalue_stringvalue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_stringvalue ON customfieldvalue USING gin (stringvalue tools.gin_trgm_ops);


--
-- Name: cfvalue_stringvalue2; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_stringvalue2 ON customfieldvalue USING btree (stringvalue);


--
-- Name: cfvalue_textvalue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cfvalue_textvalue ON customfieldvalue USING gin (textvalue tools.gin_trgm_ops);


--
-- Name: chggroup_author_created; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chggroup_author_created ON changegroup USING btree (author, created);


--
-- Name: chggroup_created; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chggroup_created ON changegroup USING btree (created);


--
-- Name: chggroup_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chggroup_issue ON changegroup USING btree (issueid);


--
-- Name: chgitem_chggrp_field; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chgitem_chggrp_field ON changeitem USING btree (groupid, fieldid);


--
-- Name: chgitem_field; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chgitem_field ON changeitem USING btree (field);


--
-- Name: chgitem_fieldid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX chgitem_fieldid ON changeitem USING btree (fieldid);


--
-- Name: cl_searchrequest; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cl_searchrequest ON columnlayout USING btree (searchrequest);


--
-- Name: cl_username; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX cl_username ON columnlayout USING btree (username);


--
-- Name: cluster_lock_name_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX cluster_lock_name_idx ON clusterlockstatus USING btree (lock_name);


--
-- Name: clusteredjob_jobid_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX clusteredjob_jobid_idx ON clusteredjob USING btree (job_id);


--
-- Name: clusteredjob_jrk_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX clusteredjob_jrk_idx ON clusteredjob USING btree (job_runner_key);


--
-- Name: clusteredjob_nextrun_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX clusteredjob_nextrun_idx ON clusteredjob USING btree (next_run);


--
-- Name: confcontext; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX confcontext ON configurationcontext USING btree (projectcategory, project, customfield);


--
-- Name: confcontextfieldconfigscheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX confcontextfieldconfigscheme ON configurationcontext USING btree (fieldconfigscheme);


--
-- Name: confcontextprojectkey; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX confcontextprojectkey ON configurationcontext USING btree (project, customfield);


--
-- Name: connect_addon_scopes_index_f4ed3a; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX connect_addon_scopes_index_f4ed3a ON connect_addon_scopes_f4ed3a USING btree (addon_key);


--
-- Name: connect_addons_enabled_index_f4ed3a; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX connect_addons_enabled_index_f4ed3a ON connect_addons_f4ed3a USING btree (is_enabled);


--
-- Name: connect_dependencies_index_f4ed3a; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX connect_dependencies_index_f4ed3a ON connect_addon_dependencies_f4ed3a USING btree (addon_key);


--
-- Name: customfield_customfieldtypekey; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX customfield_customfieldtypekey ON customfield USING btree (customfieldtypekey, id);


--
-- Name: customfieldoption_lower_customvalue_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX customfieldoption_lower_customvalue_idx ON customfieldoption USING btree (lower((customvalue)::text));


--
-- Name: customfieldvalue_customfield_stringvalue_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX customfieldvalue_customfield_stringvalue_idx ON customfieldvalue USING btree ((ROW(customfield, stringvalue)::customfield_stringvalue));


--
-- Name: deadletter_lastseen; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX deadletter_lastseen ON deadletter USING btree (last_seen);


--
-- Name: deadletter_msg_server_folder; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX deadletter_msg_server_folder ON deadletter USING btree (message_id, mail_server_id, folder_name);


--
-- Name: draft_workflow_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX draft_workflow_scheme ON draftworkflowschemeentity USING btree (scheme);


--
-- Name: draft_workflow_scheme_parent; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX draft_workflow_scheme_parent ON draftworkflowscheme USING btree (workflow_scheme_id);


--
-- Name: entity_property_name_id_key_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entity_property_name_id_key_idx ON entity_property USING btree (entity_name, entity_id, property_key);


--
-- Name: entitypropertvalue_text; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertvalue_text ON entity_property_value USING gin (value_text tools.gin_trgm_ops);


--
-- Name: entityproperty_entity; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entityproperty_entity ON entity_property USING btree (entity_name, entity_id);


--
-- Name: entityproperty_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entityproperty_key ON entity_property USING btree (property_key);


--
-- Name: entitypropertyindex_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertyindex_idx ON entity_property_index USING btree (entity_id, property_key, property_path);


--
-- Name: entitypropertyvalue_date; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertyvalue_date ON entity_property_value USING btree (property_index_id, value_date);


--
-- Name: entitypropertyvalue_num; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertyvalue_num ON entity_property_value USING btree (property_index_id, value_number);


--
-- Name: entitypropertyvalue_string; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertyvalue_string ON entity_property_value USING btree (property_index_id, value_string);


--
-- Name: entitypropertyvalue_tokens; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitypropertyvalue_tokens ON entity_property_value USING gin (tokens);


--
-- Name: entitytranslation_locale; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX entitytranslation_locale ON entity_translation USING btree (locale);


--
-- Name: entpropindexdoc_module; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX entpropindexdoc_module ON entity_property_index_document USING btree (plugin_key, module_key);


--
-- Name: ext_entity_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX ext_entity_name ON external_entities USING btree (name);


--
-- Name: favourite_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX favourite_index ON favouriteassociations USING btree (username, entitytype, entityid);


--
-- Name: fc_fieldid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fc_fieldid ON fieldconfiguration USING btree (fieldid);


--
-- Name: fcs_fieldid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fcs_fieldid ON fieldconfigscheme USING btree (fieldid);


--
-- Name: fcs_issuetype; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fcs_issuetype ON fieldconfigschemeissuetype USING btree (issuetype);


--
-- Name: fcs_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fcs_scheme ON fieldconfigschemeissuetype USING btree (fieldconfigscheme);


--
-- Name: feature_id_userkey; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX feature_id_userkey ON feature USING btree (id, user_key);


--
-- Name: fieldid_fieldconf; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldid_fieldconf ON optionconfiguration USING btree (fieldid, fieldconfig);


--
-- Name: fieldid_optionid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldid_optionid ON optionconfiguration USING btree (fieldid, optionid);


--
-- Name: fieldlayout_layout; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldlayout_layout ON fieldlayoutschemeentity USING btree (fieldlayout);


--
-- Name: fieldlayout_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldlayout_scheme ON fieldlayoutschemeentity USING btree (scheme);


--
-- Name: fieldscitem_tab; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldscitem_tab ON fieldscreenlayoutitem USING btree (fieldscreentab);


--
-- Name: fieldscreen_field; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldscreen_field ON fieldscreenlayoutitem USING btree (fieldidentifier);


--
-- Name: fieldscreen_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldscreen_scheme ON issuetypescreenschemeentity USING btree (fieldscreenscheme);


--
-- Name: fieldscreen_tab; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fieldscreen_tab ON fieldscreentab USING btree (fieldscreen);


--
-- Name: fl_scheme_assoc; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX fl_scheme_assoc ON fieldlayoutschemeassociation USING btree (project, issuetype);


--
-- Name: flyway_schema_version_16a450_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_16a450_s_idx ON flyway_schema_version_16a450 USING btree (success);


--
-- Name: flyway_schema_version_182c39_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_182c39_s_idx ON flyway_schema_version_182c39 USING btree (success);


--
-- Name: flyway_schema_version_21d670_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_21d670_s_idx ON flyway_schema_version_21d670 USING btree (success);


--
-- Name: flyway_schema_version_3b1893_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_3b1893_s_idx ON flyway_schema_version_3b1893 USING btree (success);


--
-- Name: flyway_schema_version_550953_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_550953_s_idx ON flyway_schema_version_550953 USING btree (success);


--
-- Name: flyway_schema_version_563aee_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_563aee_s_idx ON flyway_schema_version_563aee USING btree (success);


--
-- Name: flyway_schema_version_575bf5_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_575bf5_s_idx ON flyway_schema_version_575bf5 USING btree (success);


--
-- Name: flyway_schema_version_587b34_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_587b34_s_idx ON flyway_schema_version_587b34 USING btree (success);


--
-- Name: flyway_schema_version_60db71_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_60db71_s_idx ON flyway_schema_version_60db71 USING btree (success);


--
-- Name: flyway_schema_version_a0b856_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_a0b856_s_idx ON flyway_schema_version_a0b856 USING btree (success);


--
-- Name: flyway_schema_version_b59607_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_b59607_s_idx ON flyway_schema_version_b59607 USING btree (success);


--
-- Name: flyway_schema_version_c18b68_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_c18b68_s_idx ON flyway_schema_version_c18b68 USING btree (success);


--
-- Name: flyway_schema_version_deb285_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_deb285_s_idx ON flyway_schema_version_deb285 USING btree (success);


--
-- Name: flyway_schema_version_ec1a8f_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_ec1a8f_s_idx ON flyway_schema_version_ec1a8f USING btree (success);


--
-- Name: flyway_schema_version_ecd6b3_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_ecd6b3_s_idx ON flyway_schema_version_ecd6b3 USING btree (success);


--
-- Name: flyway_schema_version_f4ed3a_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_f4ed3a_s_idx ON flyway_schema_version_f4ed3a USING btree (success);


--
-- Name: flyway_schema_version_platform_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_platform_s_idx ON flyway_schema_version_platform USING btree (success);


--
-- Name: flyway_schema_version_plugins_s_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX flyway_schema_version_plugins_s_idx ON flyway_schema_version_plugins USING btree (success);


--
-- Name: historystep_entryid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX historystep_entryid ON os_historystep USING btree (entry_id);


--
-- Name: idx_all_project_ids; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_all_project_ids ON project_key USING btree (project_id);


--
-- Name: idx_all_project_keys; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX idx_all_project_keys ON project_key USING btree (project_key);


--
-- Name: idx_audit_item_log_id2; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_audit_item_log_id2 ON audit_item USING btree (log_id);


--
-- Name: idx_audit_log_created; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_audit_log_created ON audit_log USING btree (created);


--
-- Name: idx_board_board_ids; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_board_board_ids ON boardproject USING btree (board_id);


--
-- Name: idx_board_project_ids; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_board_project_ids ON boardproject USING btree (project_id);


--
-- Name: idx_changed_value_log_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_changed_value_log_id ON audit_changed_value USING btree (log_id);


--
-- Name: idx_cli_fieldidentifier; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_cli_fieldidentifier ON columnlayoutitem USING btree (fieldidentifier);


--
-- Name: idx_component_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_component_name ON component USING btree (cname);


--
-- Name: idx_component_project; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_component_project ON component USING btree (project);


--
-- Name: idx_directory_active; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_directory_active ON cwd_directory USING btree (active);


--
-- Name: idx_directory_impl; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_directory_impl ON cwd_directory USING btree (lower_impl_class);


--
-- Name: idx_directory_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_directory_type ON cwd_directory USING btree (directory_type);


--
-- Name: idx_display_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_display_name ON cwd_user USING btree (lower_display_name);


--
-- Name: idx_email_address; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_email_address ON cwd_user USING btree (lower_email_address);


--
-- Name: idx_first_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_first_name ON cwd_user USING btree (lower_first_name);


--
-- Name: idx_fli_fieldidentifier; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_fli_fieldidentifier ON fieldlayoutitem USING btree (fieldidentifier);


--
-- Name: idx_fli_fieldlayout; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_fli_fieldlayout ON fieldlayoutitem USING btree (fieldlayout);


--
-- Name: idx_group_active; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_group_active ON cwd_group USING btree (lower_group_name, active);


--
-- Name: idx_group_attr_dir_name_lval; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_group_attr_dir_name_lval ON cwd_group_attributes USING btree (directory_id, attribute_name, lower_attribute_value);


--
-- Name: idx_group_dir_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_group_dir_id ON cwd_group USING btree (directory_id);


--
-- Name: idx_last_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_last_name ON cwd_user USING btree (lower_last_name);


--
-- Name: idx_mem_dir_child; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_mem_dir_child ON cwd_membership USING btree (lower_child_name, membership_type, directory_id);


--
-- Name: idx_mem_dir_child2; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_mem_dir_child2 ON cwd_membership USING btree (child_name, parent_name);


--
-- Name: idx_mem_dir_parent; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_mem_dir_parent ON cwd_membership USING btree (lower_parent_name, membership_type, directory_id);


--
-- Name: idx_mem_dir_parent_child; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_mem_dir_parent_child ON cwd_membership USING btree (lower_parent_name, lower_child_name, membership_type, directory_id);


--
-- Name: idx_old_issue_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX idx_old_issue_key ON moved_issue_key USING btree (old_issue_key);


--
-- Name: idx_parent_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_parent_name ON jiraworkflowstatuses USING btree (parentname);


--
-- Name: idx_project_category_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_project_category_name ON projectcategory USING btree (cname);


--
-- Name: idx_project_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX idx_project_key ON project USING btree (pkey);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON jquartz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_j_g ON jquartz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_jg ON jquartz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_t_g ON jquartz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_tg ON jquartz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON jquartz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_g; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_j_g ON jquartz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_j_grp ON jquartz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_j_req_recovery ON jquartz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_j_state; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_j_state ON jquartz_triggers USING btree (sched_name, trigger_state);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_c ON jquartz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_j ON jquartz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_jg ON jquartz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_n_g_state ON jquartz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_n_state ON jquartz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_next_fire_time ON jquartz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_nft_misfire ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_nft_st ON jquartz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_reindex_component_req_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_reindex_component_req_id ON reindex_component USING btree (request_id);


--
-- Name: idx_tam_by_created_time; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_tam_by_created_time ON tempattachmentsmonitor USING btree (created_time);


--
-- Name: idx_tam_by_form_token; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_tam_by_form_token ON tempattachmentsmonitor USING btree (form_token);


--
-- Name: idx_user_attr_dir_name_lval; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_user_attr_dir_name_lval ON cwd_user_attributes USING btree (directory_id, attribute_name, lower_attribute_value);


--
-- Name: idx_version_project; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_version_project ON projectversion USING btree (project);


--
-- Name: idx_version_sequence; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX idx_version_sequence ON projectversion USING btree (sequence);


--
-- Name: index__t_cd909f_dlk20161028112011; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index__t_cd909f_dlk20161028112011 ON t_cd909f_media_store_logos USING btree (file_type);


--
-- Name: index_ao_013613_exp1681165127; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_exp1681165127 ON "AO_013613_EXPENSE" USING btree ("EXPENSE_CATEGORY_ID");


--
-- Name: index_ao_013613_hd_353373503; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_hd_353373503 ON "AO_013613_HD_SCHEME_DAY" USING btree ("HOLIDAY_SCHEME_ID");


--
-- Name: index_ao_013613_hd_764606571; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_hd_764606571 ON "AO_013613_HD_SCHEME_MEMBER" USING btree ("HOLIDAY_SCHEME_ID");


--
-- Name: index_ao_013613_per594040534; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_per594040534 ON "AO_013613_PERMISSION_GROUP" USING btree ("PERMISSION_KEY");


--
-- Name: index_ao_013613_wa_102849077; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_wa_102849077 ON "AO_013613_WA_VALUE" USING btree ("WORKLOG_ID");


--
-- Name: index_ao_013613_wa_194126942; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_wa_194126942 ON "AO_013613_WA_VALUE" USING btree ("WORK_ATTRIBUTE_ID");


--
-- Name: index_ao_013613_wl_1287258379; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_wl_1287258379 ON "AO_013613_WL_SCHEME_DAY" USING btree ("WORKLOAD_SCHEME_ID");


--
-- Name: index_ao_013613_wl_283136883; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_013613_wl_283136883 ON "AO_013613_WL_SCHEME_MEMBER" USING btree ("WORKLOAD_SCHEME_ID");


--
-- Name: index_ao_0201f0_sta556470213; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_0201f0_sta556470213 ON "AO_0201F0_STATS_EVENT_PARAM" USING btree ("STATS_EVENT_ID");


--
-- Name: index_ao_2c4e5c_mai1011024424; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2c4e5c_mai1011024424 ON "AO_2C4E5C_MAILITEMAUDIT" USING btree ("MAIL_ITEM_ID");


--
-- Name: index_ao_2c4e5c_mai737633864; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2c4e5c_mai737633864 ON "AO_2C4E5C_MAILHANDLER" USING btree ("MAIL_CHANNEL_ID");


--
-- Name: index_ao_2c4e5c_mai809659826; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2c4e5c_mai809659826 ON "AO_2C4E5C_MAILCHANNEL" USING btree ("MAIL_CONNECTION_ID");


--
-- Name: index_ao_2c4e5c_mai9620346; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2c4e5c_mai9620346 ON "AO_2C4E5C_MAILITEMCHUNK" USING btree ("MAIL_ITEM_ID");


--
-- Name: index_ao_2d3bea_all545553968; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_all545553968 ON "AO_2D3BEA_ALLOCATION" USING btree ("POSITION_ID");


--
-- Name: index_ao_2d3bea_att629655053; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_att629655053 ON "AO_2D3BEA_ATTACHMENT" USING btree ("POSITION_ID");


--
-- Name: index_ao_2d3bea_att70353952; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_att70353952 ON "AO_2D3BEA_ATTACHMENT" USING btree ("EXPENSE_ID");


--
-- Name: index_ao_2d3bea_baseline_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_baseline_name ON "AO_2D3BEA_BASELINE" USING btree ("NAME");


--
-- Name: index_ao_2d3bea_cus1293981141; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_cus1293981141 ON "AO_2D3BEA_CUSTOMFIELDVALUE" USING btree ("CUSTOM_FIELD_ID");


--
-- Name: index_ao_2d3bea_cus1659130843; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_cus1659130843 ON "AO_2D3BEA_CUSTOMFIELDPVALUE" USING btree ("CUSTOM_FIELD_ID");


--
-- Name: index_ao_2d3bea_cus1700721342; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_cus1700721342 ON "AO_2D3BEA_CUSTOMFIELDPVALUE" USING btree ("POSITION_ID");


--
-- Name: index_ao_2d3bea_cus1832390725; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_cus1832390725 ON "AO_2D3BEA_CUSTOMFIELDVALUE" USING btree ("EXPENSE_ID");


--
-- Name: index_ao_2d3bea_cus664654363; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_cus664654363 ON "AO_2D3BEA_CUSTOMFIELD" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_ent1268213396; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_ent1268213396 ON "AO_2D3BEA_ENTITY_CHANGE" USING btree ("ENTITY");


--
-- Name: index_ao_2d3bea_ent1481928026; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_ent1481928026 ON "AO_2D3BEA_ENTITY_CHANGE" USING btree ("ENTITY_ID");


--
-- Name: index_ao_2d3bea_exc105892572; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_exc105892572 ON "AO_2D3BEA_EXCHANGERATE" USING btree ("EFFECTIVE_DATE");


--
-- Name: index_ao_2d3bea_exp1879294570; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_exp1879294570 ON "AO_2D3BEA_EXPENSE" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_exp2127791453; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_exp2127791453 ON "AO_2D3BEA_EXPENSE" USING btree ("CATEGORY");


--
-- Name: index_ao_2d3bea_exp336835184; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_exp336835184 ON "AO_2D3BEA_EXPENSE" USING btree ("BASELINE_ID");


--
-- Name: index_ao_2d3bea_expense_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_expense_type ON "AO_2D3BEA_EXPENSE" USING btree ("TYPE");


--
-- Name: index_ao_2d3bea_ext1423640266; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_ext1423640266 ON "AO_2D3BEA_EXTERNALTEAM" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_ext2053839966; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_ext2053839966 ON "AO_2D3BEA_EXTERNALTEAM" USING btree ("EXTERNAL_TEAM_ID");


--
-- Name: index_ao_2d3bea_fol1244646876; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol1244646876 ON "AO_2D3BEA_FOLIO_USER_AO" USING btree ("USER_KEY");


--
-- Name: index_ao_2d3bea_fol1411200621; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol1411200621 ON "AO_2D3BEA_FOLIOCFVALUE" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_fol1864668786; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol1864668786 ON "AO_2D3BEA_FOLIOTOPORTFOLIO" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_fol1921262961; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol1921262961 ON "AO_2D3BEA_FOLIOTOPORTFOLIO" USING btree ("PORTFOLIO_ID");


--
-- Name: index_ao_2d3bea_fol203863211; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol203863211 ON "AO_2D3BEA_FOLIO_ADMIN" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_fol646663790; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol646663790 ON "AO_2D3BEA_FOLIOCFVALUE" USING btree ("CUSTOM_FIELD_ID");


--
-- Name: index_ao_2d3bea_fol689716514; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol689716514 ON "AO_2D3BEA_FOLIOCF" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_fol765065541; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_fol765065541 ON "AO_2D3BEA_FOLIO_FORMAT" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_nwds_folio_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_nwds_folio_id ON "AO_2D3BEA_NWDS" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_otr1829544105; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_otr1829544105 ON "AO_2D3BEA_OTRULETOFOLIO" USING btree ("OTRULE_ID");


--
-- Name: index_ao_2d3bea_otr434298623; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_otr434298623 ON "AO_2D3BEA_OTRULETOFOLIO" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_per731548227; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_per731548227 ON "AO_2D3BEA_PERMISSION_GROUP" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_pla100502968; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pla100502968 ON "AO_2D3BEA_PLAN_ALLOCATION" USING btree ("END_TIME");


--
-- Name: index_ao_2d3bea_pla123275473; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pla123275473 ON "AO_2D3BEA_PLAN_ALLOCATION" USING btree ("START_TIME");


--
-- Name: index_ao_2d3bea_pla2112398099; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pla2112398099 ON "AO_2D3BEA_PLAN_ALLOCATION" USING btree ("SCOPE_ID");


--
-- Name: index_ao_2d3bea_pla289083704; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pla289083704 ON "AO_2D3BEA_PLAN_ALLOCATION" USING btree ("PLAN_ITEM_ID");


--
-- Name: index_ao_2d3bea_pla634436214; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pla634436214 ON "AO_2D3BEA_PLAN_ALLOCATION" USING btree ("ASSIGNEE_KEY");


--
-- Name: index_ao_2d3bea_por1120179622; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_por1120179622 ON "AO_2D3BEA_PORTFOLIOTOPORTFOLIO" USING btree ("CHILD_ID");


--
-- Name: index_ao_2d3bea_por1692002135; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_por1692002135 ON "AO_2D3BEA_PORTFOLIO_ADMIN" USING btree ("PORTFOLIO_ID");


--
-- Name: index_ao_2d3bea_por1799341332; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_por1799341332 ON "AO_2D3BEA_PORTFOLIOTOPORTFOLIO" USING btree ("PARENT_ID");


--
-- Name: index_ao_2d3bea_portfolio_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_portfolio_name ON "AO_2D3BEA_PORTFOLIO" USING btree ("NAME");


--
-- Name: index_ao_2d3bea_pos1563453887; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pos1563453887 ON "AO_2D3BEA_POSITION" USING btree ("OTRULE_ID");


--
-- Name: index_ao_2d3bea_pos1710678563; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pos1710678563 ON "AO_2D3BEA_POSITION" USING btree ("BASELINE_ID");


--
-- Name: index_ao_2d3bea_pos405202839; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pos405202839 ON "AO_2D3BEA_POSITION" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_pos586882798; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pos586882798 ON "AO_2D3BEA_POSITION" USING btree ("MEMBER");


--
-- Name: index_ao_2d3bea_pos653699722; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_pos653699722 ON "AO_2D3BEA_POSITION" USING btree ("CATEGORY");


--
-- Name: index_ao_2d3bea_position_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_position_type ON "AO_2D3BEA_POSITION" USING btree ("TYPE");


--
-- Name: index_ao_2d3bea_rat1642264103; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_rat1642264103 ON "AO_2D3BEA_RATE" USING btree ("EFFECTIVE_DATE");


--
-- Name: index_ao_2d3bea_rate_link_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_rate_link_key ON "AO_2D3BEA_RATE" USING btree ("LINK_KEY");


--
-- Name: index_ao_2d3bea_sta860711872; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_sta860711872 ON "AO_2D3BEA_STATUS" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_tim1628972975; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_tim1628972975 ON "AO_2D3BEA_TIMELINE" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_wag1842102024; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_wag1842102024 ON "AO_2D3BEA_WAGE" USING btree ("POSITION_ID");


--
-- Name: index_ao_2d3bea_wee197705530; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_wee197705530 ON "AO_2D3BEA_WEEKDAY" USING btree ("FOLIO_ID");


--
-- Name: index_ao_2d3bea_wor1157240656; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_wor1157240656 ON "AO_2D3BEA_WORKED_HOURS" USING btree ("POSITION_ID");


--
-- Name: index_ao_2d3bea_wor1498028168; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_2d3bea_wor1498028168 ON "AO_2D3BEA_WORKFLOW" USING btree ("ALLOCATION_ID");


--
-- Name: index_ao_319474_mes1143973536; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes1143973536 ON "AO_319474_MESSAGE_PROPERTY" USING btree ("MESSAGE_ID");


--
-- Name: index_ao_319474_mes1697012995; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes1697012995 ON "AO_319474_MESSAGE" USING btree ("CONTENT_TYPE");


--
-- Name: index_ao_319474_mes1814461114; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes1814461114 ON "AO_319474_MESSAGE" USING btree ("QUEUE_ID");


--
-- Name: index_ao_319474_mes1815442463; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes1815442463 ON "AO_319474_MESSAGE" USING btree ("PRIORITY");


--
-- Name: index_ao_319474_mes33041000; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes33041000 ON "AO_319474_MESSAGE" USING btree ("CLAIMANT");


--
-- Name: index_ao_319474_mes525710975; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_mes525710975 ON "AO_319474_MESSAGE" USING btree ("CREATED_TIME");


--
-- Name: index_ao_319474_message_msg_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_message_msg_id ON "AO_319474_MESSAGE" USING btree ("MSG_ID");


--
-- Name: index_ao_319474_que568114656; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_que568114656 ON "AO_319474_QUEUE_PROPERTY" USING btree ("QUEUE_ID");


--
-- Name: index_ao_319474_queue_claimant; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_queue_claimant ON "AO_319474_QUEUE" USING btree ("CLAIMANT");


--
-- Name: index_ao_319474_queue_topic; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_319474_queue_topic ON "AO_319474_QUEUE" USING btree ("TOPIC");


--
-- Name: index_ao_3a3ecc_jir1233730427; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir1233730427 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("REMOTE_OBJECT_TYPE");


--
-- Name: index_ao_3a3ecc_jir150416577; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir150416577 ON "AO_3A3ECC_JIRAPROJECT_MAPPING" USING btree ("PROJECT_ID");


--
-- Name: index_ao_3a3ecc_jir1629901354; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir1629901354 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("PARENT_ID");


--
-- Name: index_ao_3a3ecc_jir1748623569; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir1748623569 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("NATIVE");


--
-- Name: index_ao_3a3ecc_jir1875265722; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir1875265722 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("REMOTE_ID");


--
-- Name: index_ao_3a3ecc_jir1956064761; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir1956064761 ON "AO_3A3ECC_JIRAMAPPING_BEAN" USING btree ("REMOTE_SYSTEM_ID");


--
-- Name: index_ao_3a3ecc_jir2138794315; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir2138794315 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("COMMENT_ID");


--
-- Name: index_ao_3a3ecc_jir251938693; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir251938693 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("ISSUE_ID");


--
-- Name: index_ao_3a3ecc_jir318333100; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir318333100 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("REMOTE_SYSTEM_ID");


--
-- Name: index_ao_3a3ecc_jir422269206; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir422269206 ON "AO_3A3ECC_JIRAPROJECT_MAPPING" USING btree ("JIRAMAPPING_SCHEME_ID");


--
-- Name: index_ao_3a3ecc_jir6158802; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir6158802 ON "AO_3A3ECC_JIRACOMMENT_MAPPING" USING btree ("IS_NATIVE");


--
-- Name: index_ao_3a3ecc_jir709120556; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_jir709120556 ON "AO_3A3ECC_JIRAMAPPING_BEAN" USING btree ("JIRAMAPPING_SET_ID");


--
-- Name: index_ao_3a3ecc_rem1407629045; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_rem1407629045 ON "AO_3A3ECC_REMOTE_IDCF" USING btree ("REMOTE_SYSTEM_ID");


--
-- Name: index_ao_3a3ecc_rem306661547; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_rem306661547 ON "AO_3A3ECC_REMOTE_IDCF" USING btree ("CUSTOM_FIELD_ID");


--
-- Name: index_ao_3a3ecc_rem75113348; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_3a3ecc_rem75113348 ON "AO_3A3ECC_REMOTE_IDCF" USING btree ("REMOTE_OBJECT_TYPE");


--
-- Name: index_ao_4e8ae6_not1081986701; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_4e8ae6_not1081986701 ON "AO_4E8AE6_NOTIF_BATCH_QUEUE" USING btree ("RECIPIENT_ID");


--
-- Name: index_ao_4e8ae6_not1193702477; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_4e8ae6_not1193702477 ON "AO_4E8AE6_NOTIF_BATCH_QUEUE" USING btree ("ISSUE_ID");


--
-- Name: index_ao_4e8ae6_not1949617122; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_4e8ae6_not1949617122 ON "AO_4E8AE6_NOTIF_BATCH_QUEUE" USING btree ("SENT_TIME");


--
-- Name: index_ao_4e8ae6_not850480572; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_4e8ae6_not850480572 ON "AO_4E8AE6_NOTIF_BATCH_QUEUE" USING btree ("EVENT_TIME");


--
-- Name: index_ao_54307e_cap794737505; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_cap794737505 ON "AO_54307E_CAPABILITY" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_con1117703894; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con1117703894 ON "AO_54307E_CONFLUENCEKBLABELS" USING btree ("FORM_ID");


--
-- Name: index_ao_54307e_con1483953915; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con1483953915 ON "AO_54307E_CONFLUENCEKBENABLED" USING btree ("CONFLUENCE_KBID");


--
-- Name: index_ao_54307e_con15134888; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con15134888 ON "AO_54307E_CONFLUENCEKB" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_con1589156183; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con1589156183 ON "AO_54307E_CONFLUENCEKBLABELS" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_con1935875239; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con1935875239 ON "AO_54307E_CONFLUENCEKBENABLED" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_con534365480; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con534365480 ON "AO_54307E_CONFLUENCEKBENABLED" USING btree ("FORM_ID");


--
-- Name: index_ao_54307e_con714018041; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_con714018041 ON "AO_54307E_CONFLUENCEKBLABELS" USING btree ("CONFLUENCE_KBID");


--
-- Name: index_ao_54307e_ema1343392873; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ema1343392873 ON "AO_54307E_EMAILCHANNELSETTING" USING btree ("REQUEST_TYPE_ID");


--
-- Name: index_ao_54307e_ema1700110113; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ema1700110113 ON "AO_54307E_EMAILSETTINGS" USING btree ("REQUEST_TYPE_ID");


--
-- Name: index_ao_54307e_ema1899742512; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ema1899742512 ON "AO_54307E_EMAILCHANNELSETTING" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_ema648278202; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ema648278202 ON "AO_54307E_EMAILSETTINGS" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_goa1428193453; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_goa1428193453 ON "AO_54307E_GOAL" USING btree ("TIME_METRIC_ID");


--
-- Name: index_ao_54307e_gro236699933; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_gro236699933 ON "AO_54307E_GROUP" USING btree ("VIEWPORT_ID");


--
-- Name: index_ao_54307e_gro66949553; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_gro66949553 ON "AO_54307E_GROUPTOREQUESTTYPE" USING btree ("REQUEST_TYPE_ID");


--
-- Name: index_ao_54307e_gro832680666; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_gro832680666 ON "AO_54307E_GROUPTOREQUESTTYPE" USING btree ("GROUP_ID");


--
-- Name: index_ao_54307e_met1835144061; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_met1835144061 ON "AO_54307E_METRICCONDITION" USING btree ("TIME_METRIC_ID");


--
-- Name: index_ao_54307e_org1226133886; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_org1226133886 ON "AO_54307E_ORGANIZATION_PROJECT" USING btree ("PROJECT_ID");


--
-- Name: index_ao_54307e_org1427239366; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_org1427239366 ON "AO_54307E_ORGANIZATION_PROJECT" USING btree ("ORGANIZATION_ID");


--
-- Name: index_ao_54307e_org1628402717; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_org1628402717 ON "AO_54307E_ORGANIZATION_MEMBER" USING btree ("ORGANIZATION_ID");


--
-- Name: index_ao_54307e_org240106076; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_org240106076 ON "AO_54307E_ORGANIZATION" USING btree ("LOWER_NAME");


--
-- Name: index_ao_54307e_org724569035; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_org724569035 ON "AO_54307E_ORGANIZATION_MEMBER" USING btree ("USER_KEY");


--
-- Name: index_ao_54307e_out808665536; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_out808665536 ON "AO_54307E_OUT_EMAIL_SETTINGS" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_par1577879197; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_par1577879197 ON "AO_54307E_PARTICIPANTSETTINGS" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_que1043532634; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_que1043532634 ON "AO_54307E_QUEUE" USING btree ("PROJECT_KEY");


--
-- Name: index_ao_54307e_que104885056; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_que104885056 ON "AO_54307E_QUEUE" USING btree ("PROJECT_ID");


--
-- Name: index_ao_54307e_que1582220046; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_que1582220046 ON "AO_54307E_QUEUECOLUMN" USING btree ("QUEUE_ID");


--
-- Name: index_ao_54307e_rep1824414723; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_rep1824414723 ON "AO_54307E_REPORT" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_ser1130618333; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ser1130618333 ON "AO_54307E_SERVICEDESK" USING btree ("PROJECT_ID");


--
-- Name: index_ao_54307e_ser315001103; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_ser315001103 ON "AO_54307E_SERIES" USING btree ("REPORT_ID");


--
-- Name: index_ao_54307e_sta805003358; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_sta805003358 ON "AO_54307E_STATUSMAPPING" USING btree ("FORM_ID");


--
-- Name: index_ao_54307e_sub749517042; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_sub749517042 ON "AO_54307E_SUBSCRIPTION" USING btree ("ISSUE_ID");


--
-- Name: index_ao_54307e_thr78312131; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_thr78312131 ON "AO_54307E_THRESHOLD" USING btree ("TIME_METRIC_ID");


--
-- Name: index_ao_54307e_tim1369106182; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_tim1369106182 ON "AO_54307E_TIMEMETRIC" USING btree ("SERVICE_DESK_ID");


--
-- Name: index_ao_54307e_vie1002606617; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie1002606617 ON "AO_54307E_VIEWPORT" USING btree ("THEME_ID");


--
-- Name: index_ao_54307e_vie1086593335; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie1086593335 ON "AO_54307E_VIEWPORT" USING btree ("PROJECT_ID");


--
-- Name: index_ao_54307e_vie1674919217; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie1674919217 ON "AO_54307E_VIEWPORTFIELDVALUE" USING btree ("FIELD_ID");


--
-- Name: index_ao_54307e_vie1903424282; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie1903424282 ON "AO_54307E_VIEWPORTFIELD" USING btree ("FORM_ID");


--
-- Name: index_ao_54307e_vie877344976; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie877344976 ON "AO_54307E_VIEWPORTFORM" USING btree ("VIEWPORT_ID");


--
-- Name: index_ao_54307e_vie883303387; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_54307e_vie883303387 ON "AO_54307E_VIEWPORTFORM" USING btree ("KEY");


--
-- Name: index_ao_550953_sho1778115994; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_550953_sho1778115994 ON "AO_550953_SHORTCUT" USING btree ("PROJECT_ID");


--
-- Name: index_ao_563aee_act1642652291; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_act1642652291 ON ao_563aee_activity_entity USING btree (object_id);


--
-- Name: index_ao_563aee_act1978295567; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_act1978295567 ON ao_563aee_activity_entity USING btree (target_id);


--
-- Name: index_ao_563aee_act972488439; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_act972488439 ON ao_563aee_activity_entity USING btree (icon_id);


--
-- Name: index_ao_563aee_act995325379; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_act995325379 ON ao_563aee_activity_entity USING btree (actor_id);


--
-- Name: index_ao_563aee_obj696886343; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_obj696886343 ON ao_563aee_object_entity USING btree (image_id);


--
-- Name: index_ao_563aee_tar521440921; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_563aee_tar521440921 ON ao_563aee_target_entity USING btree (image_id);


--
-- Name: index_ao_56464c_app107561580; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_56464c_app107561580 ON "AO_56464C_APPROVAL" USING btree ("ISSUE_ID");


--
-- Name: index_ao_56464c_app1713999422; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_56464c_app1713999422 ON "AO_56464C_APPROVERDECISION" USING btree ("APPROVAL_ID");


--
-- Name: index_ao_56464c_app893380122; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_56464c_app893380122 ON "AO_56464C_APPROVER" USING btree ("APPROVAL_ID");


--
-- Name: index_ao_56464c_not5371299; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_56464c_not5371299 ON "AO_56464C_NOTIFICATIONRECORD" USING btree ("APPROVAL_ID");


--
-- Name: index_ao_575bf5_iss1167652510; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss1167652510 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("OPEN_REVIEWS");


--
-- Name: index_ao_575bf5_iss1247851425; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss1247851425 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("REVIEWS");


--
-- Name: index_ao_575bf5_iss1397402016; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss1397402016 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("COMMITS");


--
-- Name: index_ao_575bf5_iss271809672; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss271809672 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("OPEN_PRS");


--
-- Name: index_ao_575bf5_iss686804213; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss686804213 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("PRS");


--
-- Name: index_ao_575bf5_iss85378972; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss85378972 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("FAILING_BUILDS");


--
-- Name: index_ao_575bf5_iss931208829; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_iss931208829 ON "AO_575BF5_ISSUE_SUMMARY" USING btree ("ISSUE_ID");


--
-- Name: index_ao_575bf5_pro1117502689; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_pro1117502689 ON "AO_575BF5_PROVIDER_ISSUE" USING btree ("STALE_AFTER");


--
-- Name: index_ao_575bf5_pro1348521624; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_pro1348521624 ON "AO_575BF5_PROVIDER_ISSUE" USING btree ("PROVIDER_SOURCE_ID");


--
-- Name: index_ao_575bf5_pro1681808571; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_pro1681808571 ON "AO_575BF5_PROCESSED_COMMITS" USING btree ("COMMIT_HASH");


--
-- Name: index_ao_575bf5_pro741170824; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_pro741170824 ON "AO_575BF5_PROVIDER_ISSUE" USING btree ("ISSUE_ID");


--
-- Name: index_ao_575bf5_pro78019725; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_575bf5_pro78019725 ON "AO_575BF5_PROCESSED_COMMITS" USING btree ("METADATA_HASH");


--
-- Name: index_ao_587b34_pro1732672724; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_587b34_pro1732672724 ON "AO_587B34_PROJECT_CONFIG" USING btree ("ROOM_ID");


--
-- Name: index_ao_587b34_pro193829489; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_587b34_pro193829489 ON "AO_587B34_PROJECT_CONFIG" USING btree ("CONFIGURATION_GROUP_ID");


--
-- Name: index_ao_587b34_pro2093917684; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_587b34_pro2093917684 ON "AO_587B34_PROJECT_CONFIG" USING btree ("PROJECT_ID");


--
-- Name: index_ao_587b34_pro2115480362; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_587b34_pro2115480362 ON "AO_587B34_PROJECT_CONFIG" USING btree ("NAME");


--
-- Name: index_ao_5fb9d7_aoh1981563178; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_5fb9d7_aoh1981563178 ON "AO_5FB9D7_AOHIP_CHAT_USER" USING btree ("USER_KEY");


--
-- Name: index_ao_5fb9d7_aoh49772492; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_5fb9d7_aoh49772492 ON "AO_5FB9D7_AOHIP_CHAT_USER" USING btree ("HIP_CHAT_LINK_ID");


--
-- Name: index_ao_60db71_aud137736645; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_aud137736645 ON "AO_60DB71_AUDITENTRY" USING btree ("ENTITY_CLASS");


--
-- Name: index_ao_60db71_aud1756477597; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_aud1756477597 ON "AO_60DB71_AUDITENTRY" USING btree ("CATEGORY");


--
-- Name: index_ao_60db71_aud604788536; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_aud604788536 ON "AO_60DB71_AUDITENTRY" USING btree ("ENTITY_ID");


--
-- Name: index_ao_60db71_boa2110227660; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_boa2110227660 ON "AO_60DB71_BOARDADMINS" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_car149237770; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_car149237770 ON "AO_60DB71_CARDLAYOUT" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_car2031978979; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_car2031978979 ON "AO_60DB71_CARDCOLOR" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_col1856623434; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_col1856623434 ON "AO_60DB71_COLUMNSTATUS" USING btree ("COLUMN_ID");


--
-- Name: index_ao_60db71_col2098611346; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_col2098611346 ON "AO_60DB71_COLUMN" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_cre31866776; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_cre31866776 ON "AO_60DB71_CREATIONCONVERSATION" USING btree ("CREATED_TIME");


--
-- Name: index_ao_60db71_det878495474; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_det878495474 ON "AO_60DB71_DETAILVIEWFIELD" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_est1680565966; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_est1680565966 ON "AO_60DB71_ESTIMATESTATISTIC" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_iss1616896230; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_iss1616896230 ON "AO_60DB71_ISSUERANKING" USING btree ("ISSUE_ID");


--
-- Name: index_ao_60db71_iss1786461035; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_iss1786461035 ON "AO_60DB71_ISSUERANKING" USING btree ("CUSTOM_FIELD_ID");


--
-- Name: index_ao_60db71_lex1632828616; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_lex1632828616 ON "AO_60DB71_LEXORANK" USING btree ("LOCK_HASH");


--
-- Name: index_ao_60db71_lex604083109; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_lex604083109 ON "AO_60DB71_LEXORANK" USING btree ("ISSUE_ID");


--
-- Name: index_ao_60db71_lexorank_fieldid_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_lexorank_fieldid_type ON "AO_60DB71_LEXORANK" USING btree ("FIELD_ID", "TYPE");


--
-- Name: index_ao_60db71_lexorank_rank; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_lexorank_rank ON "AO_60DB71_LEXORANK" USING btree ("RANK");


--
-- Name: index_ao_60db71_non1145824037; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_non1145824037 ON "AO_60DB71_NONWORKINGDAY" USING btree ("WORKING_DAYS_ID");


--
-- Name: index_ao_60db71_qui432573905; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_qui432573905 ON "AO_60DB71_QUICKFILTER" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_spr1457658269; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_spr1457658269 ON "AO_60DB71_SPRINT" USING btree ("SEQUENCE");


--
-- Name: index_ao_60db71_spr1794552746; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_spr1794552746 ON "AO_60DB71_SPRINT" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_sprint_closed; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_sprint_closed ON "AO_60DB71_SPRINT" USING btree ("CLOSED");


--
-- Name: index_ao_60db71_sta1907922871; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_sta1907922871 ON "AO_60DB71_STATSFIELD" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_sub730851836; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_sub730851836 ON "AO_60DB71_SUBQUERY" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_swi1429284592; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_swi1429284592 ON "AO_60DB71_SWIMLANE" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_tra1711190333; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_tra1711190333 ON "AO_60DB71_TRACKINGSTATISTIC" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_60db71_wor1205491794; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_60db71_wor1205491794 ON "AO_60DB71_WORKINGDAYS" USING btree ("RAPID_VIEW_ID");


--
-- Name: index_ao_68dace_con151587646; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_68dace_con151587646 ON "AO_68DACE_CONNECT_APPLICATION" USING btree ("ADDON_KEY");


--
-- Name: index_ao_68dace_con378666296; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_68dace_con378666296 ON "AO_68DACE_CONNECT_APPLICATION" USING btree ("APPLICATION_ID");


--
-- Name: index_ao_68dace_ins1439367734; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_68dace_ins1439367734 ON "AO_68DACE_INSTALLATION" USING btree ("CLIENT_KEY");


--
-- Name: index_ao_68dace_ins453735466; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_68dace_ins453735466 ON "AO_68DACE_INSTALLATION" USING btree ("CONNECT_APPLICATION_ID");


--
-- Name: index_ao_7a2604_hol2069343764; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_7a2604_hol2069343764 ON "AO_7A2604_HOLIDAY" USING btree ("CALENDAR_ID");


--
-- Name: index_ao_7a2604_wor1607107950; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_7a2604_wor1607107950 ON "AO_7A2604_WORKINGTIME" USING btree ("CALENDAR_ID");


--
-- Name: index_ao_82b313_abi1023429172; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_abi1023429172 ON "AO_82B313_ABILITY" USING btree ("SKILL_ID");


--
-- Name: index_ao_82b313_abi1495113378; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_abi1495113378 ON "AO_82B313_ABILITY" USING btree ("PERSON_ID");


--
-- Name: index_ao_82b313_abs847540213; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_abs847540213 ON "AO_82B313_ABSENCE" USING btree ("PERSON_ID");


--
-- Name: index_ao_82b313_ava1174130538; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_ava1174130538 ON "AO_82B313_AVAILABILITY" USING btree ("RESOURCE_ID");


--
-- Name: index_ao_82b313_per1958357978; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_per1958357978 ON "AO_82B313_PERSON" USING btree ("JIRA_USER_ID");


--
-- Name: index_ao_82b313_res425524126; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_res425524126 ON "AO_82B313_RESOURCE" USING btree ("PERSON_ID");


--
-- Name: index_ao_82b313_res832797798; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_82b313_res832797798 ON "AO_82B313_RESOURCE" USING btree ("TEAM_ID");


--
-- Name: index_ao_86ed1b_str962764104; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_86ed1b_str962764104 ON "AO_86ED1B_STREAMS_ENTRY" USING btree ("POSTED_DATE");


--
-- Name: index_ao_86ed1b_tim1535052854; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_86ed1b_tim1535052854 ON "AO_86ED1B_TIMESHEET_APPROVAL" USING btree ("DATE_TO");


--
-- Name: index_ao_86ed1b_tim1893730445; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_86ed1b_tim1893730445 ON "AO_86ED1B_TIMESHEET_APPROVAL" USING btree ("USER_KEY");


--
-- Name: index_ao_86ed1b_tim2012420807; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_86ed1b_tim2012420807 ON "AO_86ED1B_TIMESHEET_APPROVAL" USING btree ("DATE_FROM");


--
-- Name: index_ao_86ed1b_tim2080992994; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_86ed1b_tim2080992994 ON "AO_86ED1B_TIMEPLAN" USING btree ("COLLABORATOR");


--
-- Name: index_ao_88de6a_agr743609004; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_agr743609004 ON "AO_88DE6A_AGREEMENT" USING btree ("VERSION");


--
-- Name: index_ao_88de6a_con1446031970; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_con1446031970 ON "AO_88DE6A_CONNECTION" USING btree ("SYSTEM_TYPE_ID");


--
-- Name: index_ao_88de6a_lic50036445; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_lic50036445 ON "AO_88DE6A_LICENSE" USING btree ("SYSTEM_TYPE_ID");


--
-- Name: index_ao_88de6a_map1288997044; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_map1288997044 ON "AO_88DE6A_MAPPING_ENTRY" USING btree ("MAPPING_BEAN_ID");


--
-- Name: index_ao_88de6a_map1549399589; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_map1549399589 ON "AO_88DE6A_MAPPING_BEAN" USING btree ("LABEL");


--
-- Name: index_ao_88de6a_tra1104463207; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_tra1104463207 ON "AO_88DE6A_TRANSACTION_LOG" USING btree ("TRANSACTION_ID");


--
-- Name: index_ao_88de6a_tra1107031150; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_tra1107031150 ON "AO_88DE6A_TRANSACTION_CONTENT" USING btree ("TRANSACTION_ID");


--
-- Name: index_ao_88de6a_tra139603929; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_88de6a_tra139603929 ON "AO_88DE6A_TRANSACTION_LOG" USING btree ("LEVEL");


--
-- Name: index_ao_9b2e3b_exe1939877636; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_exe1939877636 ON "AO_9B2E3B_EXEC_RULE_MSG_ITEM" USING btree ("RULE_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_if_1335518770; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_1335518770 ON "AO_9B2E3B_IF_THEN" USING btree ("RULE_ID");


--
-- Name: index_ao_9b2e3b_if_180675374; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_180675374 ON "AO_9B2E3B_IF_THEN_EXECUTION" USING btree ("RULE_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_if_1918018153; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_1918018153 ON "AO_9B2E3B_IF_COND_CONF_DATA" USING btree ("IF_CONDITION_CONFIG_ID");


--
-- Name: index_ao_9b2e3b_if_469154523; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_469154523 ON "AO_9B2E3B_IF_EXECUTION" USING btree ("IF_THEN_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_if_939960910; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_939960910 ON "AO_9B2E3B_IF_COND_EXECUTION" USING btree ("IF_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_if_94326430; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_if_94326430 ON "AO_9B2E3B_IF_CONDITION_CONFIG" USING btree ("IF_THEN_ID");


--
-- Name: index_ao_9b2e3b_rse1331505122; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_rse1331505122 ON "AO_9B2E3B_RSETREV_USER_CONTEXT" USING btree ("RULESET_REVISION_ID");


--
-- Name: index_ao_9b2e3b_rse1358405456; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_rse1358405456 ON "AO_9B2E3B_RSETREV_PROJ_CONTEXT" USING btree ("RULESET_REVISION_ID");


--
-- Name: index_ao_9b2e3b_rul106976704; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_rul106976704 ON "AO_9B2E3B_RULESET_REVISION" USING btree ("RULE_SET_ID");


--
-- Name: index_ao_9b2e3b_rul1074875245; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_rul1074875245 ON "AO_9B2E3B_RULE" USING btree ("RULESET_REVISION_ID");


--
-- Name: index_ao_9b2e3b_the1398825512; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_the1398825512 ON "AO_9B2E3B_THEN_ACT_CONF_DATA" USING btree ("THEN_ACTION_CONFIG_ID");


--
-- Name: index_ao_9b2e3b_the293379586; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_the293379586 ON "AO_9B2E3B_THEN_ACT_EXECUTION" USING btree ("THEN_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_the554290529; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_the554290529 ON "AO_9B2E3B_THEN_ACTION_CONFIG" USING btree ("IF_THEN_ID");


--
-- Name: index_ao_9b2e3b_the769956389; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_the769956389 ON "AO_9B2E3B_THEN_EXECUTION" USING btree ("IF_THEN_EXECUTION_ID");


--
-- Name: index_ao_9b2e3b_whe1231833619; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_whe1231833619 ON "AO_9B2E3B_WHEN_HAND_CONF_DATA" USING btree ("WHEN_HANDLER_CONFIG_ID");


--
-- Name: index_ao_9b2e3b_whe716528203; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_9b2e3b_whe716528203 ON "AO_9B2E3B_WHEN_HANDLER_CONFIG" USING btree ("RULE_ID");


--
-- Name: index_ao_a415df_aoa123789499; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aoa123789499 ON "AO_A415DF_AOABSENCE" USING btree ("AOPERSON_ID");


--
-- Name: index_ao_a415df_aoa274205782; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aoa274205782 ON "AO_A415DF_AOAVAILABILITY" USING btree ("AORESOURCE_ID");


--
-- Name: index_ao_a415df_aoa576235854; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aoa576235854 ON "AO_A415DF_AOABILITY" USING btree ("AOPERSON_ID");


--
-- Name: index_ao_a415df_aod1576645787; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aod1576645787 ON "AO_A415DF_AODEPENDENCY" USING btree ("DEPENDENT");


--
-- Name: index_ao_a415df_aod780424464; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aod780424464 ON "AO_A415DF_AODEPENDENCY" USING btree ("DEPENDEE");


--
-- Name: index_ao_a415df_aoe1359587886; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aoe1359587886 ON "AO_A415DF_AOESTIMATE" USING btree ("AOWORK_ITEM_ID");


--
-- Name: index_ao_a415df_aoe449475677; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aoe449475677 ON "AO_A415DF_AOEXTENSION_LINK" USING btree ("AOEXTENDABLE_ID");


--
-- Name: index_ao_a415df_aon1597382101; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aon1597382101 ON "AO_A415DF_AONON_WORKING_DAYS" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aop1188979497; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aop1188979497 ON "AO_A415DF_AOPERSON" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aop1246856669; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aop1246856669 ON "AO_A415DF_AOPRESENCE" USING btree ("AOPERSON_ID");


--
-- Name: index_ao_a415df_aop485454964; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aop485454964 ON "AO_A415DF_AOPLAN_CONFIGURATION" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aop577302103; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aop577302103 ON "AO_A415DF_AOPERMISSION" USING btree ("TARGET_ID");


--
-- Name: index_ao_a415df_aor1533730578; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aor1533730578 ON "AO_A415DF_AORESOURCE" USING btree ("AOTEAM_ID");


--
-- Name: index_ao_a415df_aor287939766; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aor287939766 ON "AO_A415DF_AORESOURCE" USING btree ("AOPERSON_ID");


--
-- Name: index_ao_a415df_aor34455044; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aor34455044 ON "AO_A415DF_AORELEASE" USING btree ("AOSTREAM_ID");


--
-- Name: index_ao_a415df_aor548036520; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aor548036520 ON "AO_A415DF_AOREPLANNING" USING btree ("WORK_ITEM_ID");


--
-- Name: index_ao_a415df_aor752754629; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aor752754629 ON "AO_A415DF_AORELEASE" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aos1271634950; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos1271634950 ON "AO_A415DF_AOSPRINT" USING btree ("AOTEAM_ID");


--
-- Name: index_ao_a415df_aos495640191; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos495640191 ON "AO_A415DF_AOSTREAM_TO_TEAM" USING btree ("AOSTREAM_ID");


--
-- Name: index_ao_a415df_aos562466878; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos562466878 ON "AO_A415DF_AOSTREAM_TO_TEAM" USING btree ("AOTEAM_ID");


--
-- Name: index_ao_a415df_aos598285716; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos598285716 ON "AO_A415DF_AOSTREAM" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aos617973864; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos617973864 ON "AO_A415DF_AOSKILL" USING btree ("AOSTAGE_ID");


--
-- Name: index_ao_a415df_aos901500530; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aos901500530 ON "AO_A415DF_AOSTAGE" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aot1876296753; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aot1876296753 ON "AO_A415DF_AOTEAM" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aot513294467; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aot513294467 ON "AO_A415DF_AOTHEME" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aow1045989527; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1045989527 ON "AO_A415DF_AOWORK_ITEM_TO_RES" USING btree ("AORESOURCE_ID");


--
-- Name: index_ao_a415df_aow1085872694; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1085872694 ON "AO_A415DF_AOWORK_ITEM" USING btree ("ORDER_RANGE_IDENTIFIER");


--
-- Name: index_ao_a415df_aow1276121741; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1276121741 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOTHEME_ID");


--
-- Name: index_ao_a415df_aow1337278188; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1337278188 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOPARENT_ID");


--
-- Name: index_ao_a415df_aow1728248629; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1728248629 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOPLAN_ID");


--
-- Name: index_ao_a415df_aow1977806428; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow1977806428 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOSPRINT_ID");


--
-- Name: index_ao_a415df_aow2028612028; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow2028612028 ON "AO_A415DF_AOWORK_ITEM_TO_RES" USING btree ("AOWORK_ITEM_ID");


--
-- Name: index_ao_a415df_aow221278351; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow221278351 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AORELEASE_ID");


--
-- Name: index_ao_a415df_aow571736702; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow571736702 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOSTREAM_ID");


--
-- Name: index_ao_a415df_aow782862209; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_a415df_aow782862209 ON "AO_A415DF_AOWORK_ITEM" USING btree ("AOTEAM_ID");


--
-- Name: index_ao_aefed0_mem1925201761; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_mem1925201761 ON "AO_AEFED0_MEMBERSHIP" USING btree ("TEAM_ROLE_ID");


--
-- Name: index_ao_aefed0_mem2018539835; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_mem2018539835 ON "AO_AEFED0_MEMBERSHIP" USING btree ("TEAM_MEMBER_ID");


--
-- Name: index_ao_aefed0_tea101585109; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea101585109 ON "AO_AEFED0_TEAM_TO_MEMBER" USING btree ("TEAM_MEMBER_ID");


--
-- Name: index_ao_aefed0_tea1458208454; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea1458208454 ON "AO_AEFED0_TEAM_TO_MEMBER" USING btree ("TEAM_ID");


--
-- Name: index_ao_aefed0_tea1751936206; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea1751936206 ON "AO_AEFED0_TEAM_MEMBER_V2" USING btree ("MEMBER_KEY");


--
-- Name: index_ao_aefed0_tea1838295587; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea1838295587 ON "AO_AEFED0_TEAM_PERMISSION" USING btree ("TEAM_ID");


--
-- Name: index_ao_aefed0_tea1942841893; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea1942841893 ON "AO_AEFED0_TEAM_LINK" USING btree ("SCOPE");


--
-- Name: index_ao_aefed0_tea28350697; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea28350697 ON "AO_AEFED0_TEAM_MEMBER" USING btree ("MEMBER_KEY");


--
-- Name: index_ao_aefed0_tea307973682; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea307973682 ON "AO_AEFED0_TEAM_LINK" USING btree ("TEAM_ID");


--
-- Name: index_ao_aefed0_tea395955497; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea395955497 ON "AO_AEFED0_TEAM_MEMBER_V2" USING btree ("TEAM_ID");


--
-- Name: index_ao_aefed0_tea53980605; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea53980605 ON "AO_AEFED0_TEAM_V2" USING btree ("PROGRAM_ID");


--
-- Name: index_ao_aefed0_tea642239084; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_aefed0_tea642239084 ON "AO_AEFED0_TEAM_PERMISSION" USING btree ("MEMBER_KEY");


--
-- Name: index_ao_c3c6e8_acc1873384369; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_acc1873384369 ON "AO_C3C6E8_ACCOUNT_V1" USING btree ("CATEGORY_ID");


--
-- Name: index_ao_c3c6e8_acc888259217; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_acc888259217 ON "AO_C3C6E8_ACCOUNT_V1" USING btree ("CUSTOMER_ID");


--
-- Name: index_ao_c3c6e8_cat125822599; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_cat125822599 ON "AO_C3C6E8_CATEGORY_V1" USING btree ("CATEGORY_TYPE_ID");


--
-- Name: index_ao_c3c6e8_cus2101421576; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_cus2101421576 ON "AO_C3C6E8_CUSTOMER_PERMISSION" USING btree ("CUSTOMER_ID");


--
-- Name: index_ao_c3c6e8_lin715478897; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_lin715478897 ON "AO_C3C6E8_LINK_V1" USING btree ("ACCOUNT_ID");


--
-- Name: index_ao_c3c6e8_rat1108366127; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_rat1108366127 ON "AO_C3C6E8_RATE" USING btree ("RATE_TABLE_ID");


--
-- Name: index_ao_c3c6e8_rat516463941; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c3c6e8_rat516463941 ON "AO_C3C6E8_RATE_TABLE" USING btree ("PARENT_ID");


--
-- Name: index_ao_c7f17e_lin1649844463; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c7f17e_lin1649844463 ON "AO_C7F17E_LINGO_REVISION" USING btree ("LINGO_ID");


--
-- Name: index_ao_c7f17e_lin2056815817; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c7f17e_lin2056815817 ON "AO_C7F17E_LINGO_TRANSLATION" USING btree ("LINGO_REVISION_ID");


--
-- Name: index_ao_c7f17e_lin523964875; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c7f17e_lin523964875 ON "AO_C7F17E_LINGO" USING btree ("PROJECT_ID");


--
-- Name: index_ao_c7f17e_lin614942907; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_c7f17e_lin614942907 ON "AO_C7F17E_LINGO" USING btree ("LOGICAL_ID");


--
-- Name: index_ao_d9132d_ass1284224961; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass1284224961 ON "AO_D9132D_ASSIGNMENT" USING btree ("SOLUTION_ID");


--
-- Name: index_ao_d9132d_ass1821457992; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass1821457992 ON "AO_D9132D_ASSIGNMENT" USING btree ("INTERVAL_START");


--
-- Name: index_ao_d9132d_ass1897231097; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass1897231097 ON "AO_D9132D_ASSIGNMENT" USING btree ("ISSUE");


--
-- Name: index_ao_d9132d_ass1897285102; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass1897285102 ON "AO_D9132D_ASSIGNMENT" USING btree ("RESOURCE");


--
-- Name: index_ao_d9132d_ass1995881407; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass1995881407 ON "AO_D9132D_ASSIGNMENT" USING btree ("INTERVAL_END");


--
-- Name: index_ao_d9132d_ass199949577; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass199949577 ON "AO_D9132D_ASSIGNMENT" USING btree ("PLAN");


--
-- Name: index_ao_d9132d_ass200062013; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass200062013 ON "AO_D9132D_ASSIGNMENT" USING btree ("TEAM");


--
-- Name: index_ao_d9132d_ass423812856; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ass423812856 ON "AO_D9132D_ASSIGNMENT" USING btree ("VERSION");


--
-- Name: index_ao_d9132d_dis1745341529; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_dis1745341529 ON "AO_D9132D_DISTRIBUTION" USING btree ("SCENARIO_ISSUE_ID");


--
-- Name: index_ao_d9132d_dis912936150; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_dis912936150 ON "AO_D9132D_DISTRIBUTION" USING btree ("SKILL_ITEM_KEY");


--
-- Name: index_ao_d9132d_exc1861854128; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_exc1861854128 ON "AO_D9132D_EXCLUDED_VERSIONS" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_iss1919689979; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_iss1919689979 ON "AO_D9132D_ISSUE_SOURCE" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_non561724511; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_non561724511 ON "AO_D9132D_NONWORKINGDAYS" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_per1164836380; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_per1164836380 ON "AO_D9132D_PERMISSIONS" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_pla1554207320; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla1554207320 ON "AO_D9132D_PLANSKILL" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_pla2036165122; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla2036165122 ON "AO_D9132D_PLANTHEME" USING btree ("THEME_ID");


--
-- Name: index_ao_d9132d_pla2082025806; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla2082025806 ON "AO_D9132D_PLANTEAM" USING btree ("ISSUE_SOURCE_ID");


--
-- Name: index_ao_d9132d_pla2100750794; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla2100750794 ON "AO_D9132D_PLANTEAM" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_pla228745504; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla228745504 ON "AO_D9132D_PLANTHEME" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_pla62020034; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla62020034 ON "AO_D9132D_PLANVERSION" USING btree ("XPROJECT_VERSION_ID");


--
-- Name: index_ao_d9132d_pla958319633; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_pla958319633 ON "AO_D9132D_PLANVERSION" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_ran1612448239; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ran1612448239 ON "AO_D9132D_RANK_ITEM" USING btree ("DOMAIN");


--
-- Name: index_ao_d9132d_ran1797004856; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_ran1797004856 ON "AO_D9132D_RANK_ITEM" USING btree ("RANGE_ID");


--
-- Name: index_ao_d9132d_rank_item_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_rank_item_key ON "AO_D9132D_RANK_ITEM" USING btree ("KEY");


--
-- Name: index_ao_d9132d_sce100282944; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce100282944 ON "AO_D9132D_SCENARIO_ISSUE_LINKS" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1006164830; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1006164830 ON "AO_D9132D_SCENARIO_CHANGES" USING btree ("T_TYPE");


--
-- Name: index_ao_d9132d_sce1017308037; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1017308037 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1039993579; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1039993579 ON "AO_D9132D_SCENARIO_STAGE" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1086205799; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1086205799 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("THEME_ID");


--
-- Name: index_ao_d9132d_sce1119979099; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1119979099 ON "AO_D9132D_SCENARIO_ABILITY" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1120228753; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1120228753 ON "AO_D9132D_SCENARIO_STAGE" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce113715567; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce113715567 ON "AO_D9132D_SCENARIO_XPVERSION" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce114962861; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce114962861 ON "AO_D9132D_SCENARIO_ABILITY" USING btree ("SKILL_ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1165940267; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1165940267 ON "AO_D9132D_SCENARIO_ABILITY" USING btree ("PERSON_ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1223631444; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1223631444 ON "AO_D9132D_SCENARIO" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_sce1224456962; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1224456962 ON "AO_D9132D_SCENARIO_SKILL" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1271866466; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1271866466 ON "AO_D9132D_SCENARIO_SKILL" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1310871043; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1310871043 ON "AO_D9132D_SCENARIO_PERSON" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce1321303140; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1321303140 ON "AO_D9132D_SCENARIO_PERSON" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1333210566; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1333210566 ON "AO_D9132D_SCENARIO_ISSUE_LINKS" USING btree ("TARGET");


--
-- Name: index_ao_d9132d_sce1348810556; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1348810556 ON "AO_D9132D_SCENARIO_ISSUE_LINKS" USING btree ("SOURCE");


--
-- Name: index_ao_d9132d_sce13552520; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce13552520 ON "AO_D9132D_SCENARIO_ABILITY" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce1386125014; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1386125014 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("VERSION_ID");


--
-- Name: index_ao_d9132d_sce138718377; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce138718377 ON "AO_D9132D_SCENARIO_ABILITY" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1400547264; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1400547264 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("STATUS_ID");


--
-- Name: index_ao_d9132d_sce1478246132; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1478246132 ON "AO_D9132D_SCENARIO_STAGE" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce1518982583; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1518982583 ON "AO_D9132D_SCENARIO_VERSION" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1543172557; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1543172557 ON "AO_D9132D_SCENARIO_XPVERSION" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1554987896; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1554987896 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("PARENT_ID");


--
-- Name: index_ao_d9132d_sce1569966108; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1569966108 ON "AO_D9132D_SCENARIO_TEAM" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce1609369417; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1609369417 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("PROJECT_ID");


--
-- Name: index_ao_d9132d_sce1625521782; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1625521782 ON "AO_D9132D_SCENARIO_THEME" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce168803159; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce168803159 ON "AO_D9132D_SCENARIO_RESOURCE" USING btree ("PERSON_ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1697093730; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1697093730 ON "AO_D9132D_SCENARIO_ISSUE_RES" USING btree ("RESOURCE_ITEM_KEY");


--
-- Name: index_ao_d9132d_sce175198003; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce175198003 ON "AO_D9132D_SCENARIO_AVLBLTY" USING btree ("SCENARIO_RESOURCE_ID");


--
-- Name: index_ao_d9132d_sce1834903770; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1834903770 ON "AO_D9132D_SCENARIO_THEME" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce1883185710; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce1883185710 ON "AO_D9132D_SCENARIO_ISSUE_RES" USING btree ("SCENARIO_ISSUE_ID");


--
-- Name: index_ao_d9132d_sce2005427167; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce2005427167 ON "AO_D9132D_SCENARIO_SKILL" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce2082437338; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce2082437338 ON "AO_D9132D_SCENARIO_VERSION" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce2099451160; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce2099451160 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("SPRINT_ID");


--
-- Name: index_ao_d9132d_sce210160351; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce210160351 ON "AO_D9132D_SCENARIO_ISSUE_LINKS" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce224459199; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce224459199 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce232829573; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce232829573 ON "AO_D9132D_SCENARIO_VERSION" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce29849183; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce29849183 ON "AO_D9132D_SCENARIO_RESOURCE" USING btree ("TEAM_ITEM_KEY");


--
-- Name: index_ao_d9132d_sce304701852; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce304701852 ON "AO_D9132D_SCENARIO_ISSUE_LINKS" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce318880623; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce318880623 ON "AO_D9132D_SCENARIO_CHANGES" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce338333120; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce338333120 ON "AO_D9132D_SCENARIO_PERSON" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce355910425; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce355910425 ON "AO_D9132D_SCENARIO_RESOURCE" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce408394583; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce408394583 ON "AO_D9132D_SCENARIO_THEME" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce410924660; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce410924660 ON "AO_D9132D_SCENARIO_TEAM" USING btree ("ISSUE_SOURCE_ID");


--
-- Name: index_ao_d9132d_sce434778360; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce434778360 ON "AO_D9132D_SCENARIO_TEAM" USING btree ("ITEM_KEY");


--
-- Name: index_ao_d9132d_sce502118059; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce502118059 ON "AO_D9132D_SCENARIO_RESOURCE" USING btree ("SCENARIO_TYPE");


--
-- Name: index_ao_d9132d_sce643340770; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce643340770 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce646807048; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce646807048 ON "AO_D9132D_SCENARIO_ISSUES" USING btree ("TYPE_ID");


--
-- Name: index_ao_d9132d_sce838588485; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce838588485 ON "AO_D9132D_SCENARIO_TEAM" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce949090414; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce949090414 ON "AO_D9132D_SCENARIO_XPVERSION" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_sce970354186; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_sce970354186 ON "AO_D9132D_SCENARIO_RESOURCE" USING btree ("SCENARIO_ID");


--
-- Name: index_ao_d9132d_solution_plan; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_solution_plan ON "AO_D9132D_SOLUTION" USING btree ("PLAN");


--
-- Name: index_ao_d9132d_solution_state; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_solution_state ON "AO_D9132D_SOLUTION" USING btree ("STATE");


--
-- Name: index_ao_d9132d_stage_plan_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_stage_plan_id ON "AO_D9132D_STAGE" USING btree ("PLAN_ID");


--
-- Name: index_ao_d9132d_theme_shared; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_theme_shared ON "AO_D9132D_THEME" USING btree ("SHARED");


--
-- Name: index_ao_d9132d_x_p689485877; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_d9132d_x_p689485877 ON "AO_D9132D_X_PROJECT_VERSION" USING btree ("PLAN_ID");


--
-- Name: index_ao_deb285_com782863884; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_deb285_com782863884 ON "AO_DEB285_COMMENT_AO" USING btree ("BLOG_ID");


--
-- Name: index_ao_e8b6cc_bra1368852151; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_bra1368852151 ON "AO_E8B6CC_BRANCH_HEAD_MAPPING" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_bra405461593; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_bra405461593 ON "AO_E8B6CC_BRANCH" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_cha1086340152; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_cha1086340152 ON "AO_E8B6CC_CHANGESET_MAPPING" USING btree ("SMARTCOMMIT_AVAILABLE");


--
-- Name: index_ao_e8b6cc_cha1483243924; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_cha1483243924 ON "AO_E8B6CC_CHANGESET_MAPPING" USING btree ("NODE");


--
-- Name: index_ao_e8b6cc_cha509722037; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_cha509722037 ON "AO_E8B6CC_CHANGESET_MAPPING" USING btree ("RAW_NODE");


--
-- Name: index_ao_e8b6cc_cha897886051; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_cha897886051 ON "AO_E8B6CC_CHANGESET_MAPPING" USING btree ("AUTHOR");


--
-- Name: index_ao_e8b6cc_com1308336834; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_com1308336834 ON "AO_E8B6CC_COMMIT" USING btree ("DOMAIN_ID");


--
-- Name: index_ao_e8b6cc_commit_node; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_commit_node ON "AO_E8B6CC_COMMIT" USING btree ("NODE");


--
-- Name: index_ao_e8b6cc_git1120895454; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_git1120895454 ON "AO_E8B6CC_GIT_HUB_EVENT" USING btree ("GIT_HUB_ID");


--
-- Name: index_ao_e8b6cc_git1804640320; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_git1804640320 ON "AO_E8B6CC_GIT_HUB_EVENT" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_iss1229805759; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_iss1229805759 ON "AO_E8B6CC_ISSUE_TO_CHANGESET" USING btree ("CHANGESET_ID");


--
-- Name: index_ao_e8b6cc_iss1325927291; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_iss1325927291 ON "AO_E8B6CC_ISSUE_TO_BRANCH" USING btree ("BRANCH_ID");


--
-- Name: index_ao_e8b6cc_iss353204826; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_iss353204826 ON "AO_E8B6CC_ISSUE_TO_BRANCH" USING btree ("ISSUE_KEY");


--
-- Name: index_ao_e8b6cc_iss417950110; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_iss417950110 ON "AO_E8B6CC_ISSUE_TO_CHANGESET" USING btree ("ISSUE_KEY");


--
-- Name: index_ao_e8b6cc_iss648895902; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_iss648895902 ON "AO_E8B6CC_ISSUE_TO_CHANGESET" USING btree ("PROJECT_KEY");


--
-- Name: index_ao_e8b6cc_mes1247039512; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes1247039512 ON "AO_E8B6CC_MESSAGE" USING btree ("ADDRESS");


--
-- Name: index_ao_e8b6cc_mes1391090780; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes1391090780 ON "AO_E8B6CC_MESSAGE_TAG" USING btree ("MESSAGE_ID");


--
-- Name: index_ao_e8b6cc_mes1648007831; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes1648007831 ON "AO_E8B6CC_MESSAGE_TAG" USING btree ("TAG");


--
-- Name: index_ao_e8b6cc_mes344532677; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes344532677 ON "AO_E8B6CC_MESSAGE_QUEUE_ITEM" USING btree ("MESSAGE_ID");


--
-- Name: index_ao_e8b6cc_mes59146529; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes59146529 ON "AO_E8B6CC_MESSAGE_QUEUE_ITEM" USING btree ("STATE");


--
-- Name: index_ao_e8b6cc_mes60959905; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_mes60959905 ON "AO_E8B6CC_MESSAGE_QUEUE_ITEM" USING btree ("QUEUE");


--
-- Name: index_ao_e8b6cc_org1513110290; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_org1513110290 ON "AO_E8B6CC_ORGANIZATION_MAPPING" USING btree ("DVCS_TYPE");


--
-- Name: index_ao_e8b6cc_org1899590324; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_org1899590324 ON "AO_E8B6CC_ORG_TO_PROJECT" USING btree ("ORGANIZATION_ID");


--
-- Name: index_ao_e8b6cc_pr_1045528152; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_1045528152 ON "AO_E8B6CC_PR_TO_COMMIT" USING btree ("REQUEST_ID");


--
-- Name: index_ao_e8b6cc_pr_1105917040; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_1105917040 ON "AO_E8B6CC_PR_PARTICIPANT" USING btree ("PULL_REQUEST_ID");


--
-- Name: index_ao_e8b6cc_pr_1458633226; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_1458633226 ON "AO_E8B6CC_PR_TO_COMMIT" USING btree ("COMMIT_ID");


--
-- Name: index_ao_e8b6cc_pr_1639282617; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_1639282617 ON "AO_E8B6CC_PR_ISSUE_KEY" USING btree ("DOMAIN_ID");


--
-- Name: index_ao_e8b6cc_pr_2106805302; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_2106805302 ON "AO_E8B6CC_PR_ISSUE_KEY" USING btree ("ISSUE_KEY");


--
-- Name: index_ao_e8b6cc_pr_281193494; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_281193494 ON "AO_E8B6CC_PR_ISSUE_KEY" USING btree ("PULL_REQUEST_ID");


--
-- Name: index_ao_e8b6cc_pr_685151049; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_685151049 ON "AO_E8B6CC_PR_TO_COMMIT" USING btree ("DOMAIN_ID");


--
-- Name: index_ao_e8b6cc_pr_758084799; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pr_758084799 ON "AO_E8B6CC_PR_PARTICIPANT" USING btree ("DOMAIN_ID");


--
-- Name: index_ao_e8b6cc_pul1230717024; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pul1230717024 ON "AO_E8B6CC_PULL_REQUEST" USING btree ("DOMAIN_ID");


--
-- Name: index_ao_e8b6cc_pul1448445182; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pul1448445182 ON "AO_E8B6CC_PULL_REQUEST" USING btree ("TO_REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_pul602811170; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_pul602811170 ON "AO_E8B6CC_PULL_REQUEST" USING btree ("REMOTE_ID");


--
-- Name: index_ao_e8b6cc_rep1082901832; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_rep1082901832 ON "AO_E8B6CC_REPO_TO_CHANGESET" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_rep1928770529; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_rep1928770529 ON "AO_E8B6CC_REPO_TO_PROJECT" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_rep702725269; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_rep702725269 ON "AO_E8B6CC_REPOSITORY_MAPPING" USING btree ("ORGANIZATION_ID");


--
-- Name: index_ao_e8b6cc_rep922992576; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_rep922992576 ON "AO_E8B6CC_REPO_TO_CHANGESET" USING btree ("CHANGESET_ID");


--
-- Name: index_ao_e8b6cc_rep93578901; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_rep93578901 ON "AO_E8B6CC_REPOSITORY_MAPPING" USING btree ("LINKED");


--
-- Name: index_ao_e8b6cc_syn203792807; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_syn203792807 ON "AO_E8B6CC_SYNC_AUDIT_LOG" USING btree ("REPO_ID");


--
-- Name: index_ao_e8b6cc_syn493078035; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_e8b6cc_syn493078035 ON "AO_E8B6CC_SYNC_EVENT" USING btree ("REPO_ID");


--
-- Name: index_ao_ed979b_act1342837092; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_act1342837092 ON "AO_ED979B_ACTIONREGISTRY" USING btree ("TIME");


--
-- Name: index_ao_ed979b_act1673649171; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_act1673649171 ON "AO_ED979B_ACTIONREGISTRY" USING btree ("USERNAME");


--
-- Name: index_ao_ed979b_act1873660628; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_act1873660628 ON "AO_ED979B_ACTIONREGISTRY" USING btree ("ACTION_TYPE");


--
-- Name: index_ao_ed979b_act2109037028; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_act2109037028 ON "AO_ED979B_ACTIONREGISTRY" USING btree ("OBJECT_ID");


--
-- Name: index_ao_ed979b_act439628349; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_act439628349 ON "AO_ED979B_ACTIONREGISTRY" USING btree ("OBJECT_TYPE");


--
-- Name: index_ao_ed979b_eve1165294688; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve1165294688 ON "AO_ED979B_EVENT" USING btree ("SOURCE_USERNAME");


--
-- Name: index_ao_ed979b_eve1310044801; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve1310044801 ON "AO_ED979B_EVENT" USING btree ("OBJECT_ID");


--
-- Name: index_ao_ed979b_eve1511666309; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve1511666309 ON "AO_ED979B_EVENTPROPERTY" USING btree ("EVENT_ID");


--
-- Name: index_ao_ed979b_eve1528401299; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve1528401299 ON "AO_ED979B_EVENT" USING btree ("CREATION_TIME");


--
-- Name: index_ao_ed979b_eve1873891865; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve1873891865 ON "AO_ED979B_EVENT" USING btree ("EVENT_TYPE");


--
-- Name: index_ao_ed979b_eve527987488; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_eve527987488 ON "AO_ED979B_EVENT" USING btree ("OBJECT_TYPE");


--
-- Name: index_ao_ed979b_sub1687307965; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_sub1687307965 ON "AO_ED979B_SUBSCRIPTION" USING btree ("USERNAME");


--
-- Name: index_ao_ed979b_sub631647980; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_sub631647980 ON "AO_ED979B_SUBSCRIPTION" USING btree ("EVENT_TYPE");


--
-- Name: index_ao_ed979b_use1033144891; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_use1033144891 ON "AO_ED979B_USEREVENT" USING btree ("EVENT_ID");


--
-- Name: index_ao_ed979b_use1576976965; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_ed979b_use1576976965 ON "AO_ED979B_USEREVENT" USING btree ("USERNAME");


--
-- Name: index_ao_f1b27b_key473010270; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_f1b27b_key473010270 ON "AO_F1B27B_KEY_COMPONENT" USING btree ("TIMED_PROMISE_ID");


--
-- Name: index_ao_f1b27b_key828117107; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_f1b27b_key828117107 ON "AO_F1B27B_KEY_COMP_HISTORY" USING btree ("TIMED_PROMISE_ID");


--
-- Name: index_ao_f4ed3a_add50909668; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX index_ao_f4ed3a_add50909668 ON "AO_F4ED3A_ADD_ON_PROPERTY_AO" USING btree ("PLUGIN_KEY");


--
-- Name: issue_assignee; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_assignee ON jiraissue USING btree (assignee);


--
-- Name: issue_created; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_created ON jiraissue USING btree (created);


--
-- Name: issue_description; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_description ON jiraissue USING gin (description tools.gin_trgm_ops);


--
-- Name: issue_duedate; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_duedate ON jiraissue USING btree (duedate);


--
-- Name: issue_environment; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_environment ON jiraissue USING gin (environment tools.gin_trgm_ops);


--
-- Name: issue_field_option_scopes; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_field_option_scopes ON issue_field_option_attr USING btree (scope_id);


--
-- Name: issue_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_key ON jiraissue USING btree (pkey, issuenum);


--
-- Name: issue_proj_num; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_proj_num ON jiraissue USING btree (issuenum, project);


--
-- Name: issue_proj_status; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_proj_status ON jiraissue USING btree (project, issuestatus);


--
-- Name: issue_reporter; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_reporter ON jiraissue USING btree (reporter);


--
-- Name: issue_resolutiondate; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_resolutiondate ON jiraissue USING btree (resolutiondate);


--
-- Name: issue_summary; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_summary ON jiraissue USING gin (summary tools.gin_trgm_ops);


--
-- Name: issue_updated; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_updated ON jiraissue USING btree (updated);


--
-- Name: issue_votes; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_votes ON jiraissue USING btree (votes);


--
-- Name: issue_watches; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_watches ON jiraissue USING btree (watches);


--
-- Name: issue_workflow; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issue_workflow ON jiraissue USING btree (workflow_id);


--
-- Name: issuelink_dest; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issuelink_dest ON issuelink USING btree (destination);


--
-- Name: issuelink_src; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issuelink_src ON issuelink USING btree (source);


--
-- Name: issuelink_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issuelink_type ON issuelink USING btree (linktype);


--
-- Name: issuelink_type_dest; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issuelink_type_dest ON issuelink USING btree (linktype, destination);


--
-- Name: issuelink_type_src; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX issuelink_type_src ON issuelink USING btree (linktype, source);


--
-- Name: jiraaction_token; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX jiraaction_token ON jiraaction USING gin (tokens);


--
-- Name: jiraissuetokens_field; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX jiraissuetokens_field ON jiraissuetokens USING btree (field);


--
-- Name: jiraissuetokens_token; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX jiraissuetokens_token ON jiraissuetokens USING gin (tokens);


--
-- Name: jiraworkflow_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX jiraworkflow_name ON jiraworkflows USING btree (workflowname);


--
-- Name: label_fieldissue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX label_fieldissue ON label USING btree (issue, fieldid);


--
-- Name: label_fieldissuelabel; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX label_fieldissuelabel ON label USING btree (issue, fieldid, label);


--
-- Name: label_fieldlabellower; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX label_fieldlabellower ON label USING btree (lower((label)::text), fieldid);


--
-- Name: label_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX label_issue ON label USING btree (issue);


--
-- Name: label_label; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX label_label ON label USING btree (label);


--
-- Name: licenseroledefault_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX licenseroledefault_index ON licenserolesdefault USING btree (license_role_name);


--
-- Name: licenserolegroup_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX licenserolegroup_index ON licenserolesgroup USING btree (license_role_name, group_id);


--
-- Name: linktypename; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX linktypename ON issuelinktype USING btree (linkname);


--
-- Name: linktypestyle; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX linktypestyle ON issuelinktype USING btree (pstyle);


--
-- Name: lock_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX lock_key ON async_task USING btree (lock_key);


--
-- Name: managedconfigitem_id_type_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX managedconfigitem_id_type_idx ON managedconfigurationitem USING btree (item_id, item_type);


--
-- Name: mshipbase_group; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX mshipbase_group ON membershipbase USING btree (group_name);


--
-- Name: mshipbase_user; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX mshipbase_user ON membershipbase USING btree (user_name);


--
-- Name: node_association_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX node_association_type ON nodeassociation USING btree (association_type, sink_node_entity);


--
-- Name: node_id_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX node_id_idx ON nodeindexcounter USING btree (node_id, sending_node_id);


--
-- Name: node_operation_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX node_operation_idx ON replicatedindexoperation USING btree (node_id, affected_index, operation, index_time);


--
-- Name: node_sink; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX node_sink ON nodeassociation USING btree (sink_node_id, sink_node_entity);


--
-- Name: node_source; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX node_source ON nodeassociation USING btree (source_node_id, source_node_entity);


--
-- Name: nodeassociation_source_sink; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX nodeassociation_source_sink ON nodeassociation USING btree (source_node_id, sink_node_id);


--
-- Name: notif_messageid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX notif_messageid ON notificationinstance USING btree (messageid);


--
-- Name: notif_source; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX notif_source ON notificationinstance USING btree (source);


--
-- Name: ntfctn_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX ntfctn_scheme ON notification USING btree (scheme);


--
-- Name: oauth_consumer_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX oauth_consumer_index ON oauthconsumer USING btree (consumer_key);


--
-- Name: oauth_consumer_service_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX oauth_consumer_service_index ON oauthconsumer USING btree (consumerservice);


--
-- Name: oauth_consumer_token_consumer_key_index_182c39; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX oauth_consumer_token_consumer_key_index_182c39 ON oauth_consumer_token_182c39 USING btree (consumer_key);


--
-- Name: oauth_consumer_token_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX oauth_consumer_token_index ON oauthconsumertoken USING btree (token);


--
-- Name: oauth_consumer_token_key_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX oauth_consumer_token_key_index ON oauthconsumertoken USING btree (token_key);


--
-- Name: oauth_service_token_consumer_key_index_ecd6b3; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX oauth_service_token_consumer_key_index_ecd6b3 ON oauth_service_provider_token_ecd6b3 USING btree (consumer_key);


--
-- Name: oauth_sp_consumer_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX oauth_sp_consumer_index ON oauthspconsumer USING btree (consumer_key);


--
-- Name: oauth_sp_consumer_key_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX oauth_sp_consumer_key_index ON oauthsptoken USING btree (consumer_key);


--
-- Name: oauth_sp_token_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX oauth_sp_token_index ON oauthsptoken USING btree (token);


--
-- Name: osgroup_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX osgroup_name ON groupbase USING btree (groupname);


--
-- Name: osproperty_all; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX osproperty_all ON propertyentry USING btree (entity_id);


--
-- Name: osproperty_entityname; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX osproperty_entityname ON propertyentry USING btree (entity_name);


--
-- Name: osproperty_propertykey; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX osproperty_propertykey ON propertyentry USING btree (property_key);


--
-- Name: osuser_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX osuser_name ON userbase USING btree (username);


--
-- Name: permission_key_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX permission_key_idx ON schemepermissions USING btree (permission_key);


--
-- Name: ppage_username; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX ppage_username ON portalpage USING btree (username);


--
-- Name: rapidview_parentproject_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX rapidview_parentproject_index ON "AO_60DB71_RAPIDVIEW" USING btree ("PARENT_PROJECT_ID");


--
-- Name: rapidview_user_location_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX rapidview_user_location_index ON "AO_60DB71_RAPIDVIEW" USING btree ("USER_LOCATION_ID");


--
-- Name: remembermetoken_username_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX remembermetoken_username_index ON remembermetoken USING btree (username);


--
-- Name: remotelink_globalid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX remotelink_globalid ON remotelink USING btree (globalid);


--
-- Name: remotelink_issueid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX remotelink_issueid ON remotelink USING btree (issueid, globalid);


--
-- Name: role_pid_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX role_pid_idx ON projectroleactor USING btree (pid);


--
-- Name: role_player_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX role_player_idx ON projectroleactor USING btree (projectroleid, pid);


--
-- Name: rundetails_jobid_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX rundetails_jobid_idx ON rundetails USING btree (job_id);


--
-- Name: rundetails_starttime_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX rundetails_starttime_idx ON rundetails USING btree (start_time);


--
-- Name: schemeissuesecurities_type_param; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX schemeissuesecurities_type_param ON schemeissuesecurities USING btree (sec_type, sec_parameter);


--
-- Name: schemepermissions_permission_key_perm_type_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX schemepermissions_permission_key_perm_type_idx ON schemepermissions USING btree (permission_key, perm_type);


--
-- Name: schemepermissions_scheme_param; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX schemepermissions_scheme_param ON schemepermissions USING btree (scheme, perm_parameter);


--
-- Name: screenitem_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX screenitem_scheme ON fieldscreenschemeitem USING btree (fieldscreenscheme);


--
-- Name: searchrequest_filternamelower; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX searchrequest_filternamelower ON searchrequest USING btree (filtername_lower);


--
-- Name: sec_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX sec_scheme ON schemeissuesecurities USING btree (scheme);


--
-- Name: sec_security; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX sec_security ON schemeissuesecurities USING btree (security);


--
-- Name: share_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX share_index ON sharepermissions USING btree (entityid, entitytype);


--
-- Name: source_destination_node_idx; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX source_destination_node_idx ON clustermessage USING btree (source_node, destination_node);


--
-- Name: sr_author; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX sr_author ON searchrequest USING btree (authorname);


--
-- Name: subscrpt_user; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX subscrpt_user ON filtersubscription USING btree (filter_i_d, username);


--
-- Name: subscrptn_group; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX subscrptn_group ON filtersubscription USING btree (filter_i_d, groupname);


--
-- Name: trustedapp_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX trustedapp_id ON trustedapp USING btree (application_id);


--
-- Name: type_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX type_key ON genericconfiguration USING btree (datatype, datakey);


--
-- Name: uh_type_user_entity; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uh_type_user_entity ON userhistoryitem USING btree (entitytype, username, entityid);


--
-- Name: uk_application_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_application_name ON cwd_application USING btree (lower_application_name);


--
-- Name: uk_directory_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX uk_directory_name ON cwd_directory USING btree (lower_directory_name);


--
-- Name: uk_entitytranslation; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_entitytranslation ON entity_translation USING btree (entity_name, entity_id, locale);


--
-- Name: uk_group_attr_name_lval; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_group_attr_name_lval ON cwd_group_attributes USING btree (group_id, attribute_name, lower_attribute_value);


--
-- Name: uk_group_name_dir_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_group_name_dir_id ON cwd_group USING btree (lower_group_name, directory_id);


--
-- Name: uk_lower_user_name; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_lower_user_name ON app_user USING btree (lower_user_name);


--
-- Name: uk_mem_parent_child_type; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_mem_parent_child_type ON cwd_membership USING btree (parent_id, child_id, membership_type);


--
-- Name: uk_user_attr_name_lval; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX uk_user_attr_name_lval ON cwd_user_attributes USING btree (user_id, attribute_name);


--
-- Name: uk_user_externalid_dir_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX uk_user_externalid_dir_id ON cwd_user USING btree (external_id, directory_id);


--
-- Name: uk_user_key; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_user_key ON app_user USING btree (user_key);


--
-- Name: uk_user_name_dir_id; Type: INDEX; Schema: public; Owner: jira
--

CREATE UNIQUE INDEX uk_user_name_dir_id ON cwd_user USING btree (lower_user_name, directory_id);


--
-- Name: upf_customfield; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX upf_customfield ON userpickerfilter USING btree (customfield);


--
-- Name: upf_fieldconfigid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX upf_fieldconfigid ON userpickerfilter USING btree (customfieldconfig);


--
-- Name: user_sink; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX user_sink ON userassociation USING btree (sink_node_id, sink_node_entity);


--
-- Name: user_source; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX user_source ON userassociation USING btree (source_name);


--
-- Name: userpref_portletconfiguration; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX userpref_portletconfiguration ON gadgetuserpreference USING btree (portletconfiguration);


--
-- Name: votehistory_issue_index; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX votehistory_issue_index ON votehistory USING btree (issueid);


--
-- Name: wf_entryid; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX wf_entryid ON os_currentstep USING btree (entry_id);


--
-- Name: workflow_scheme; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX workflow_scheme ON workflowschemeentity USING btree (scheme);


--
-- Name: worklog_author; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX worklog_author ON worklog USING btree (author);


--
-- Name: worklog_comment; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX worklog_comment ON worklog USING gin (worklogbody tools.gin_trgm_ops);


--
-- Name: worklog_issue; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX worklog_issue ON worklog USING btree (issueid);


--
-- Name: worklog_token; Type: INDEX; Schema: public; Owner: jira
--

CREATE INDEX worklog_token ON worklog USING gin (tokens);


--
-- Name: AO_DEB285_COMMENT_AO_BLOG_ID_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_DEB285_COMMENT_AO"
    ADD CONSTRAINT "AO_DEB285_COMMENT_AO_BLOG_ID_fkey" FOREIGN KEY ("BLOG_ID") REFERENCES "AO_DEB285_BLOG_AO"("ID");


--
-- Name: connect_addon_dependencies_f4ed3a_addon_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_dependencies_f4ed3a
    ADD CONSTRAINT connect_addon_dependencies_f4ed3a_addon_key_fkey FOREIGN KEY (addon_key) REFERENCES connect_addons_f4ed3a(addon_key);


--
-- Name: connect_addon_descriptors_f4ed3a_addon_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_descriptors_f4ed3a
    ADD CONSTRAINT connect_addon_descriptors_f4ed3a_addon_key_fkey FOREIGN KEY (addon_key) REFERENCES connect_addons_f4ed3a(addon_key);


--
-- Name: connect_addon_listings_f4ed3a_addon_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_listings_f4ed3a
    ADD CONSTRAINT connect_addon_listings_f4ed3a_addon_key_fkey FOREIGN KEY (addon_key) REFERENCES connect_addons_f4ed3a(addon_key);


--
-- Name: connect_addon_scopes_f4ed3a_addon_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY connect_addon_scopes_f4ed3a
    ADD CONSTRAINT connect_addon_scopes_f4ed3a_addon_key_fkey FOREIGN KEY (addon_key) REFERENCES connect_addons_f4ed3a(addon_key);


--
-- Name: fk_ao_013613_expense_expense_category_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_EXPENSE"
    ADD CONSTRAINT fk_ao_013613_expense_expense_category_id FOREIGN KEY ("EXPENSE_CATEGORY_ID") REFERENCES "AO_013613_EXP_CATEGORY"("ID");


--
-- Name: fk_ao_013613_hd_scheme_day_holiday_scheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_DAY"
    ADD CONSTRAINT fk_ao_013613_hd_scheme_day_holiday_scheme_id FOREIGN KEY ("HOLIDAY_SCHEME_ID") REFERENCES "AO_013613_HD_SCHEME"("ID");


--
-- Name: fk_ao_013613_hd_scheme_member_holiday_scheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_HD_SCHEME_MEMBER"
    ADD CONSTRAINT fk_ao_013613_hd_scheme_member_holiday_scheme_id FOREIGN KEY ("HOLIDAY_SCHEME_ID") REFERENCES "AO_013613_HD_SCHEME"("ID");


--
-- Name: fk_ao_013613_wa_value_work_attribute_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WA_VALUE"
    ADD CONSTRAINT fk_ao_013613_wa_value_work_attribute_id FOREIGN KEY ("WORK_ATTRIBUTE_ID") REFERENCES "AO_013613_WORK_ATTRIBUTE"("ID");


--
-- Name: fk_ao_013613_wl_scheme_day_workload_scheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_DAY"
    ADD CONSTRAINT fk_ao_013613_wl_scheme_day_workload_scheme_id FOREIGN KEY ("WORKLOAD_SCHEME_ID") REFERENCES "AO_013613_WL_SCHEME"("ID");


--
-- Name: fk_ao_013613_wl_scheme_member_workload_scheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_013613_WL_SCHEME_MEMBER"
    ADD CONSTRAINT fk_ao_013613_wl_scheme_member_workload_scheme_id FOREIGN KEY ("WORKLOAD_SCHEME_ID") REFERENCES "AO_013613_WL_SCHEME"("ID");


--
-- Name: fk_ao_0201f0_stats_event_param_stats_event_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_0201F0_STATS_EVENT_PARAM"
    ADD CONSTRAINT fk_ao_0201f0_stats_event_param_stats_event_id FOREIGN KEY ("STATS_EVENT_ID") REFERENCES "AO_0201F0_STATS_EVENT"("ID");


--
-- Name: fk_ao_2c4e5c_mailchannel_mail_connection_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILCHANNEL"
    ADD CONSTRAINT fk_ao_2c4e5c_mailchannel_mail_connection_id FOREIGN KEY ("MAIL_CONNECTION_ID") REFERENCES "AO_2C4E5C_MAILCONNECTION"("ID");


--
-- Name: fk_ao_2c4e5c_mailhandler_mail_channel_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILHANDLER"
    ADD CONSTRAINT fk_ao_2c4e5c_mailhandler_mail_channel_id FOREIGN KEY ("MAIL_CHANNEL_ID") REFERENCES "AO_2C4E5C_MAILCHANNEL"("ID");


--
-- Name: fk_ao_2c4e5c_mailitemaudit_mail_item_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMAUDIT"
    ADD CONSTRAINT fk_ao_2c4e5c_mailitemaudit_mail_item_id FOREIGN KEY ("MAIL_ITEM_ID") REFERENCES "AO_2C4E5C_MAILITEM"("ID");


--
-- Name: fk_ao_2c4e5c_mailitemchunk_mail_item_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2C4E5C_MAILITEMCHUNK"
    ADD CONSTRAINT fk_ao_2c4e5c_mailitemchunk_mail_item_id FOREIGN KEY ("MAIL_ITEM_ID") REFERENCES "AO_2C4E5C_MAILITEM"("ID");


--
-- Name: fk_ao_2d3bea_allocation_position_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ALLOCATION"
    ADD CONSTRAINT fk_ao_2d3bea_allocation_position_id FOREIGN KEY ("POSITION_ID") REFERENCES "AO_2D3BEA_POSITION"("ID");


--
-- Name: fk_ao_2d3bea_attachment_expense_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ATTACHMENT"
    ADD CONSTRAINT fk_ao_2d3bea_attachment_expense_id FOREIGN KEY ("EXPENSE_ID") REFERENCES "AO_2D3BEA_EXPENSE"("ID");


--
-- Name: fk_ao_2d3bea_attachment_position_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_ATTACHMENT"
    ADD CONSTRAINT fk_ao_2d3bea_attachment_position_id FOREIGN KEY ("POSITION_ID") REFERENCES "AO_2D3BEA_POSITION"("ID");


--
-- Name: fk_ao_2d3bea_customfield_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELD"
    ADD CONSTRAINT fk_ao_2d3bea_customfield_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_customfieldpvalue_custom_field_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDPVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_customfieldpvalue_custom_field_id FOREIGN KEY ("CUSTOM_FIELD_ID") REFERENCES "AO_2D3BEA_CUSTOMFIELD"("ID");


--
-- Name: fk_ao_2d3bea_customfieldpvalue_position_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDPVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_customfieldpvalue_position_id FOREIGN KEY ("POSITION_ID") REFERENCES "AO_2D3BEA_POSITION"("ID");


--
-- Name: fk_ao_2d3bea_customfieldvalue_custom_field_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_customfieldvalue_custom_field_id FOREIGN KEY ("CUSTOM_FIELD_ID") REFERENCES "AO_2D3BEA_CUSTOMFIELD"("ID");


--
-- Name: fk_ao_2d3bea_customfieldvalue_expense_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_CUSTOMFIELDVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_customfieldvalue_expense_id FOREIGN KEY ("EXPENSE_ID") REFERENCES "AO_2D3BEA_EXPENSE"("ID");


--
-- Name: fk_ao_2d3bea_expense_baseline_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXPENSE"
    ADD CONSTRAINT fk_ao_2d3bea_expense_baseline_id FOREIGN KEY ("BASELINE_ID") REFERENCES "AO_2D3BEA_BASELINE"("ID");


--
-- Name: fk_ao_2d3bea_expense_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXPENSE"
    ADD CONSTRAINT fk_ao_2d3bea_expense_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_externalteam_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_EXTERNALTEAM"
    ADD CONSTRAINT fk_ao_2d3bea_externalteam_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_folio_admin_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_ADMIN"
    ADD CONSTRAINT fk_ao_2d3bea_folio_admin_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_folio_format_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIO_FORMAT"
    ADD CONSTRAINT fk_ao_2d3bea_folio_format_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_foliocf_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCF"
    ADD CONSTRAINT fk_ao_2d3bea_foliocf_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_foliocfvalue_custom_field_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCFVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_foliocfvalue_custom_field_id FOREIGN KEY ("CUSTOM_FIELD_ID") REFERENCES "AO_2D3BEA_FOLIOCF"("ID");


--
-- Name: fk_ao_2d3bea_foliocfvalue_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOCFVALUE"
    ADD CONSTRAINT fk_ao_2d3bea_foliocfvalue_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_foliotoportfolio_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOTOPORTFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_foliotoportfolio_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_foliotoportfolio_portfolio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_FOLIOTOPORTFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_foliotoportfolio_portfolio_id FOREIGN KEY ("PORTFOLIO_ID") REFERENCES "AO_2D3BEA_PORTFOLIO"("ID");


--
-- Name: fk_ao_2d3bea_nwds_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_NWDS"
    ADD CONSTRAINT fk_ao_2d3bea_nwds_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_otruletofolio_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OTRULETOFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_otruletofolio_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_otruletofolio_otrule_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_OTRULETOFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_otruletofolio_otrule_id FOREIGN KEY ("OTRULE_ID") REFERENCES "AO_2D3BEA_OVERTIME"("ID");


--
-- Name: fk_ao_2d3bea_permission_group_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PERMISSION_GROUP"
    ADD CONSTRAINT fk_ao_2d3bea_permission_group_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_portfolio_admin_portfolio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIO_ADMIN"
    ADD CONSTRAINT fk_ao_2d3bea_portfolio_admin_portfolio_id FOREIGN KEY ("PORTFOLIO_ID") REFERENCES "AO_2D3BEA_PORTFOLIO"("ID");


--
-- Name: fk_ao_2d3bea_portfoliotoportfolio_child_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIOTOPORTFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_portfoliotoportfolio_child_id FOREIGN KEY ("CHILD_ID") REFERENCES "AO_2D3BEA_PORTFOLIO"("ID");


--
-- Name: fk_ao_2d3bea_portfoliotoportfolio_parent_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_PORTFOLIOTOPORTFOLIO"
    ADD CONSTRAINT fk_ao_2d3bea_portfoliotoportfolio_parent_id FOREIGN KEY ("PARENT_ID") REFERENCES "AO_2D3BEA_PORTFOLIO"("ID");


--
-- Name: fk_ao_2d3bea_position_baseline_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_POSITION"
    ADD CONSTRAINT fk_ao_2d3bea_position_baseline_id FOREIGN KEY ("BASELINE_ID") REFERENCES "AO_2D3BEA_BASELINE"("ID");


--
-- Name: fk_ao_2d3bea_position_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_POSITION"
    ADD CONSTRAINT fk_ao_2d3bea_position_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_position_otrule_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_POSITION"
    ADD CONSTRAINT fk_ao_2d3bea_position_otrule_id FOREIGN KEY ("OTRULE_ID") REFERENCES "AO_2D3BEA_OVERTIME"("ID");


--
-- Name: fk_ao_2d3bea_status_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_STATUS"
    ADD CONSTRAINT fk_ao_2d3bea_status_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_timeline_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_TIMELINE"
    ADD CONSTRAINT fk_ao_2d3bea_timeline_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_wage_position_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WAGE"
    ADD CONSTRAINT fk_ao_2d3bea_wage_position_id FOREIGN KEY ("POSITION_ID") REFERENCES "AO_2D3BEA_POSITION"("ID");


--
-- Name: fk_ao_2d3bea_weekday_folio_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WEEKDAY"
    ADD CONSTRAINT fk_ao_2d3bea_weekday_folio_id FOREIGN KEY ("FOLIO_ID") REFERENCES "AO_2D3BEA_FOLIO"("ID");


--
-- Name: fk_ao_2d3bea_worked_hours_position_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKED_HOURS"
    ADD CONSTRAINT fk_ao_2d3bea_worked_hours_position_id FOREIGN KEY ("POSITION_ID") REFERENCES "AO_2D3BEA_POSITION"("ID");


--
-- Name: fk_ao_2d3bea_workflow_allocation_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_2D3BEA_WORKFLOW"
    ADD CONSTRAINT fk_ao_2d3bea_workflow_allocation_id FOREIGN KEY ("ALLOCATION_ID") REFERENCES "AO_2D3BEA_PLAN_ALLOCATION"("ID");


--
-- Name: fk_ao_319474_message_property_message_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE_PROPERTY"
    ADD CONSTRAINT fk_ao_319474_message_property_message_id FOREIGN KEY ("MESSAGE_ID") REFERENCES "AO_319474_MESSAGE"("ID");


--
-- Name: fk_ao_319474_message_queue_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_MESSAGE"
    ADD CONSTRAINT fk_ao_319474_message_queue_id FOREIGN KEY ("QUEUE_ID") REFERENCES "AO_319474_QUEUE"("ID");


--
-- Name: fk_ao_319474_queue_property_queue_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_319474_QUEUE_PROPERTY"
    ADD CONSTRAINT fk_ao_319474_queue_property_queue_id FOREIGN KEY ("QUEUE_ID") REFERENCES "AO_319474_QUEUE"("ID");


--
-- Name: fk_ao_3a3ecc_jiramapping_bean_jiramapping_set_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAMAPPING_BEAN"
    ADD CONSTRAINT fk_ao_3a3ecc_jiramapping_bean_jiramapping_set_id FOREIGN KEY ("JIRAMAPPING_SET_ID") REFERENCES "AO_3A3ECC_JIRAMAPPING_SET"("ID");


--
-- Name: fk_ao_3a3ecc_jiraproject_mapping_jiramapping_scheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_3A3ECC_JIRAPROJECT_MAPPING"
    ADD CONSTRAINT fk_ao_3a3ecc_jiraproject_mapping_jiramapping_scheme_id FOREIGN KEY ("JIRAMAPPING_SCHEME_ID") REFERENCES "AO_3A3ECC_JIRAMAPPING_SCHEME"("ID");


--
-- Name: fk_ao_54307e_capability_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CAPABILITY"
    ADD CONSTRAINT fk_ao_54307e_capability_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_confluencekb_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKB"
    ADD CONSTRAINT fk_ao_54307e_confluencekb_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_confluencekbenabled_confluence_kbid; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBENABLED"
    ADD CONSTRAINT fk_ao_54307e_confluencekbenabled_confluence_kbid FOREIGN KEY ("CONFLUENCE_KBID") REFERENCES "AO_54307E_CONFLUENCEKB"("ID");


--
-- Name: fk_ao_54307e_confluencekbenabled_form_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBENABLED"
    ADD CONSTRAINT fk_ao_54307e_confluencekbenabled_form_id FOREIGN KEY ("FORM_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_confluencekbenabled_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBENABLED"
    ADD CONSTRAINT fk_ao_54307e_confluencekbenabled_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_confluencekblabels_confluence_kbid; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBLABELS"
    ADD CONSTRAINT fk_ao_54307e_confluencekblabels_confluence_kbid FOREIGN KEY ("CONFLUENCE_KBID") REFERENCES "AO_54307E_CONFLUENCEKB"("ID");


--
-- Name: fk_ao_54307e_confluencekblabels_form_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBLABELS"
    ADD CONSTRAINT fk_ao_54307e_confluencekblabels_form_id FOREIGN KEY ("FORM_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_confluencekblabels_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_CONFLUENCEKBLABELS"
    ADD CONSTRAINT fk_ao_54307e_confluencekblabels_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_emailchannelsetting_request_type_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILCHANNELSETTING"
    ADD CONSTRAINT fk_ao_54307e_emailchannelsetting_request_type_id FOREIGN KEY ("REQUEST_TYPE_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_emailchannelsetting_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILCHANNELSETTING"
    ADD CONSTRAINT fk_ao_54307e_emailchannelsetting_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_emailsettings_request_type_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILSETTINGS"
    ADD CONSTRAINT fk_ao_54307e_emailsettings_request_type_id FOREIGN KEY ("REQUEST_TYPE_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_emailsettings_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_EMAILSETTINGS"
    ADD CONSTRAINT fk_ao_54307e_emailsettings_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_goal_time_metric_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GOAL"
    ADD CONSTRAINT fk_ao_54307e_goal_time_metric_id FOREIGN KEY ("TIME_METRIC_ID") REFERENCES "AO_54307E_TIMEMETRIC"("ID");


--
-- Name: fk_ao_54307e_group_viewport_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUP"
    ADD CONSTRAINT fk_ao_54307e_group_viewport_id FOREIGN KEY ("VIEWPORT_ID") REFERENCES "AO_54307E_VIEWPORT"("ID");


--
-- Name: fk_ao_54307e_grouptorequesttype_group_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUPTOREQUESTTYPE"
    ADD CONSTRAINT fk_ao_54307e_grouptorequesttype_group_id FOREIGN KEY ("GROUP_ID") REFERENCES "AO_54307E_GROUP"("ID");


--
-- Name: fk_ao_54307e_grouptorequesttype_request_type_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_GROUPTOREQUESTTYPE"
    ADD CONSTRAINT fk_ao_54307e_grouptorequesttype_request_type_id FOREIGN KEY ("REQUEST_TYPE_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_metriccondition_time_metric_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_METRICCONDITION"
    ADD CONSTRAINT fk_ao_54307e_metriccondition_time_metric_id FOREIGN KEY ("TIME_METRIC_ID") REFERENCES "AO_54307E_TIMEMETRIC"("ID");


--
-- Name: fk_ao_54307e_organization_member_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_MEMBER"
    ADD CONSTRAINT fk_ao_54307e_organization_member_organization_id FOREIGN KEY ("ORGANIZATION_ID") REFERENCES "AO_54307E_ORGANIZATION"("ID");


--
-- Name: fk_ao_54307e_organization_project_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_ORGANIZATION_PROJECT"
    ADD CONSTRAINT fk_ao_54307e_organization_project_organization_id FOREIGN KEY ("ORGANIZATION_ID") REFERENCES "AO_54307E_ORGANIZATION"("ID");


--
-- Name: fk_ao_54307e_out_email_settings_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_OUT_EMAIL_SETTINGS"
    ADD CONSTRAINT fk_ao_54307e_out_email_settings_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_participantsettings_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_PARTICIPANTSETTINGS"
    ADD CONSTRAINT fk_ao_54307e_participantsettings_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_queuecolumn_queue_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_QUEUECOLUMN"
    ADD CONSTRAINT fk_ao_54307e_queuecolumn_queue_id FOREIGN KEY ("QUEUE_ID") REFERENCES "AO_54307E_QUEUE"("ID");


--
-- Name: fk_ao_54307e_report_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_REPORT"
    ADD CONSTRAINT fk_ao_54307e_report_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_series_report_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_SERIES"
    ADD CONSTRAINT fk_ao_54307e_series_report_id FOREIGN KEY ("REPORT_ID") REFERENCES "AO_54307E_REPORT"("ID");


--
-- Name: fk_ao_54307e_statusmapping_form_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_STATUSMAPPING"
    ADD CONSTRAINT fk_ao_54307e_statusmapping_form_id FOREIGN KEY ("FORM_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_threshold_time_metric_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_THRESHOLD"
    ADD CONSTRAINT fk_ao_54307e_threshold_time_metric_id FOREIGN KEY ("TIME_METRIC_ID") REFERENCES "AO_54307E_TIMEMETRIC"("ID");


--
-- Name: fk_ao_54307e_timemetric_service_desk_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_TIMEMETRIC"
    ADD CONSTRAINT fk_ao_54307e_timemetric_service_desk_id FOREIGN KEY ("SERVICE_DESK_ID") REFERENCES "AO_54307E_SERVICEDESK"("ID");


--
-- Name: fk_ao_54307e_viewport_theme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORT"
    ADD CONSTRAINT fk_ao_54307e_viewport_theme_id FOREIGN KEY ("THEME_ID") REFERENCES "AO_54307E_CUSTOMTHEME"("ID");


--
-- Name: fk_ao_54307e_viewportfield_form_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELD"
    ADD CONSTRAINT fk_ao_54307e_viewportfield_form_id FOREIGN KEY ("FORM_ID") REFERENCES "AO_54307E_VIEWPORTFORM"("ID");


--
-- Name: fk_ao_54307e_viewportfieldvalue_field_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFIELDVALUE"
    ADD CONSTRAINT fk_ao_54307e_viewportfieldvalue_field_id FOREIGN KEY ("FIELD_ID") REFERENCES "AO_54307E_VIEWPORTFIELD"("ID");


--
-- Name: fk_ao_54307e_viewportform_viewport_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_54307E_VIEWPORTFORM"
    ADD CONSTRAINT fk_ao_54307e_viewportform_viewport_id FOREIGN KEY ("VIEWPORT_ID") REFERENCES "AO_54307E_VIEWPORT"("ID");


--
-- Name: fk_ao_563aee_activity_entity_actor_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity
    ADD CONSTRAINT fk_ao_563aee_activity_entity_actor_id FOREIGN KEY (actor_id) REFERENCES ao_563aee_actor_entity(id);


--
-- Name: fk_ao_563aee_activity_entity_icon_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity
    ADD CONSTRAINT fk_ao_563aee_activity_entity_icon_id FOREIGN KEY (icon_id) REFERENCES ao_563aee_media_link_entity(id);


--
-- Name: fk_ao_563aee_activity_entity_object_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity
    ADD CONSTRAINT fk_ao_563aee_activity_entity_object_id FOREIGN KEY (object_id) REFERENCES ao_563aee_object_entity(id);


--
-- Name: fk_ao_563aee_activity_entity_target_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_activity_entity
    ADD CONSTRAINT fk_ao_563aee_activity_entity_target_id FOREIGN KEY (target_id) REFERENCES ao_563aee_target_entity(id);


--
-- Name: fk_ao_563aee_object_entity_image_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_object_entity
    ADD CONSTRAINT fk_ao_563aee_object_entity_image_id FOREIGN KEY (image_id) REFERENCES ao_563aee_media_link_entity(id);


--
-- Name: fk_ao_563aee_target_entity_image_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY ao_563aee_target_entity
    ADD CONSTRAINT fk_ao_563aee_target_entity_image_id FOREIGN KEY (image_id) REFERENCES ao_563aee_media_link_entity(id);


--
-- Name: fk_ao_56464c_approver_approval_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVER"
    ADD CONSTRAINT fk_ao_56464c_approver_approval_id FOREIGN KEY ("APPROVAL_ID") REFERENCES "AO_56464C_APPROVAL"("ID");


--
-- Name: fk_ao_56464c_approverdecision_approval_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_APPROVERDECISION"
    ADD CONSTRAINT fk_ao_56464c_approverdecision_approval_id FOREIGN KEY ("APPROVAL_ID") REFERENCES "AO_56464C_APPROVAL"("ID");


--
-- Name: fk_ao_56464c_notificationrecord_approval_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_56464C_NOTIFICATIONRECORD"
    ADD CONSTRAINT fk_ao_56464c_notificationrecord_approval_id FOREIGN KEY ("APPROVAL_ID") REFERENCES "AO_56464C_APPROVAL"("ID");


--
-- Name: fk_ao_5fb9d7_aohip_chat_user_hip_chat_link_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_5FB9D7_AOHIP_CHAT_USER"
    ADD CONSTRAINT fk_ao_5fb9d7_aohip_chat_user_hip_chat_link_id FOREIGN KEY ("HIP_CHAT_LINK_ID") REFERENCES "AO_5FB9D7_AOHIP_CHAT_LINK"("ID");


--
-- Name: fk_ao_60db71_boardadmins_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_BOARDADMINS"
    ADD CONSTRAINT fk_ao_60db71_boardadmins_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_cardcolor_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDCOLOR"
    ADD CONSTRAINT fk_ao_60db71_cardcolor_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_cardlayout_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_CARDLAYOUT"
    ADD CONSTRAINT fk_ao_60db71_cardlayout_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_column_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMN"
    ADD CONSTRAINT fk_ao_60db71_column_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_columnstatus_column_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_COLUMNSTATUS"
    ADD CONSTRAINT fk_ao_60db71_columnstatus_column_id FOREIGN KEY ("COLUMN_ID") REFERENCES "AO_60DB71_COLUMN"("ID");


--
-- Name: fk_ao_60db71_detailviewfield_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_DETAILVIEWFIELD"
    ADD CONSTRAINT fk_ao_60db71_detailviewfield_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_estimatestatistic_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_ESTIMATESTATISTIC"
    ADD CONSTRAINT fk_ao_60db71_estimatestatistic_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_nonworkingday_working_days_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_NONWORKINGDAY"
    ADD CONSTRAINT fk_ao_60db71_nonworkingday_working_days_id FOREIGN KEY ("WORKING_DAYS_ID") REFERENCES "AO_60DB71_WORKINGDAYS"("ID");


--
-- Name: fk_ao_60db71_quickfilter_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_QUICKFILTER"
    ADD CONSTRAINT fk_ao_60db71_quickfilter_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_statsfield_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_STATSFIELD"
    ADD CONSTRAINT fk_ao_60db71_statsfield_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_subquery_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SUBQUERY"
    ADD CONSTRAINT fk_ao_60db71_subquery_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_swimlane_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_SWIMLANE"
    ADD CONSTRAINT fk_ao_60db71_swimlane_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_trackingstatistic_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_TRACKINGSTATISTIC"
    ADD CONSTRAINT fk_ao_60db71_trackingstatistic_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_60db71_workingdays_rapid_view_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_60DB71_WORKINGDAYS"
    ADD CONSTRAINT fk_ao_60db71_workingdays_rapid_view_id FOREIGN KEY ("RAPID_VIEW_ID") REFERENCES "AO_60DB71_RAPIDVIEW"("ID");


--
-- Name: fk_ao_68dace_installation_connect_application_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_68DACE_INSTALLATION"
    ADD CONSTRAINT fk_ao_68dace_installation_connect_application_id FOREIGN KEY ("CONNECT_APPLICATION_ID") REFERENCES "AO_68DACE_CONNECT_APPLICATION"("ID");


--
-- Name: fk_ao_7a2604_holiday_calendar_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_HOLIDAY"
    ADD CONSTRAINT fk_ao_7a2604_holiday_calendar_id FOREIGN KEY ("CALENDAR_ID") REFERENCES "AO_7A2604_CALENDAR"("ID");


--
-- Name: fk_ao_7a2604_workingtime_calendar_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_7A2604_WORKINGTIME"
    ADD CONSTRAINT fk_ao_7a2604_workingtime_calendar_id FOREIGN KEY ("CALENDAR_ID") REFERENCES "AO_7A2604_CALENDAR"("ID");


--
-- Name: fk_ao_82b313_ability_person_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABILITY"
    ADD CONSTRAINT fk_ao_82b313_ability_person_id FOREIGN KEY ("PERSON_ID") REFERENCES "AO_82B313_PERSON"("ID");


--
-- Name: fk_ao_82b313_ability_skill_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABILITY"
    ADD CONSTRAINT fk_ao_82b313_ability_skill_id FOREIGN KEY ("SKILL_ID") REFERENCES "AO_82B313_SKILL"("ID");


--
-- Name: fk_ao_82b313_absence_person_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_ABSENCE"
    ADD CONSTRAINT fk_ao_82b313_absence_person_id FOREIGN KEY ("PERSON_ID") REFERENCES "AO_82B313_PERSON"("ID");


--
-- Name: fk_ao_82b313_availability_resource_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_AVAILABILITY"
    ADD CONSTRAINT fk_ao_82b313_availability_resource_id FOREIGN KEY ("RESOURCE_ID") REFERENCES "AO_82B313_RESOURCE"("ID");


--
-- Name: fk_ao_82b313_resource_person_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_RESOURCE"
    ADD CONSTRAINT fk_ao_82b313_resource_person_id FOREIGN KEY ("PERSON_ID") REFERENCES "AO_82B313_PERSON"("ID");


--
-- Name: fk_ao_82b313_resource_team_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_82B313_RESOURCE"
    ADD CONSTRAINT fk_ao_82b313_resource_team_id FOREIGN KEY ("TEAM_ID") REFERENCES "AO_82B313_TEAM"("ID");


--
-- Name: fk_ao_88de6a_mapping_entry_mapping_bean_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_MAPPING_ENTRY"
    ADD CONSTRAINT fk_ao_88de6a_mapping_entry_mapping_bean_id FOREIGN KEY ("MAPPING_BEAN_ID") REFERENCES "AO_88DE6A_MAPPING_BEAN"("ID");


--
-- Name: fk_ao_88de6a_transaction_content_transaction_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_CONTENT"
    ADD CONSTRAINT fk_ao_88de6a_transaction_content_transaction_id FOREIGN KEY ("TRANSACTION_ID") REFERENCES "AO_88DE6A_TRANSACTION"("ID");


--
-- Name: fk_ao_88de6a_transaction_log_transaction_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_88DE6A_TRANSACTION_LOG"
    ADD CONSTRAINT fk_ao_88de6a_transaction_log_transaction_id FOREIGN KEY ("TRANSACTION_ID") REFERENCES "AO_88DE6A_TRANSACTION"("ID");


--
-- Name: fk_ao_9b2e3b_exec_rule_msg_item_rule_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_EXEC_RULE_MSG_ITEM"
    ADD CONSTRAINT fk_ao_9b2e3b_exec_rule_msg_item_rule_execution_id FOREIGN KEY ("RULE_EXECUTION_ID") REFERENCES "AO_9B2E3B_RULE_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_if_cond_conf_data_if_condition_config_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_CONF_DATA"
    ADD CONSTRAINT fk_ao_9b2e3b_if_cond_conf_data_if_condition_config_id FOREIGN KEY ("IF_CONDITION_CONFIG_ID") REFERENCES "AO_9B2E3B_IF_CONDITION_CONFIG"("ID");


--
-- Name: fk_ao_9b2e3b_if_cond_execution_if_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_COND_EXECUTION"
    ADD CONSTRAINT fk_ao_9b2e3b_if_cond_execution_if_execution_id FOREIGN KEY ("IF_EXECUTION_ID") REFERENCES "AO_9B2E3B_IF_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_if_condition_config_if_then_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_CONDITION_CONFIG"
    ADD CONSTRAINT fk_ao_9b2e3b_if_condition_config_if_then_id FOREIGN KEY ("IF_THEN_ID") REFERENCES "AO_9B2E3B_IF_THEN"("ID");


--
-- Name: fk_ao_9b2e3b_if_execution_if_then_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_EXECUTION"
    ADD CONSTRAINT fk_ao_9b2e3b_if_execution_if_then_execution_id FOREIGN KEY ("IF_THEN_EXECUTION_ID") REFERENCES "AO_9B2E3B_IF_THEN_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_if_then_execution_rule_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN_EXECUTION"
    ADD CONSTRAINT fk_ao_9b2e3b_if_then_execution_rule_execution_id FOREIGN KEY ("RULE_EXECUTION_ID") REFERENCES "AO_9B2E3B_RULE_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_if_then_rule_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_IF_THEN"
    ADD CONSTRAINT fk_ao_9b2e3b_if_then_rule_id FOREIGN KEY ("RULE_ID") REFERENCES "AO_9B2E3B_RULE"("ID");


--
-- Name: fk_ao_9b2e3b_rsetrev_proj_context_ruleset_revision_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_PROJ_CONTEXT"
    ADD CONSTRAINT fk_ao_9b2e3b_rsetrev_proj_context_ruleset_revision_id FOREIGN KEY ("RULESET_REVISION_ID") REFERENCES "AO_9B2E3B_RULESET_REVISION"("ID");


--
-- Name: fk_ao_9b2e3b_rsetrev_user_context_ruleset_revision_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RSETREV_USER_CONTEXT"
    ADD CONSTRAINT fk_ao_9b2e3b_rsetrev_user_context_ruleset_revision_id FOREIGN KEY ("RULESET_REVISION_ID") REFERENCES "AO_9B2E3B_RULESET_REVISION"("ID");


--
-- Name: fk_ao_9b2e3b_rule_ruleset_revision_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULE"
    ADD CONSTRAINT fk_ao_9b2e3b_rule_ruleset_revision_id FOREIGN KEY ("RULESET_REVISION_ID") REFERENCES "AO_9B2E3B_RULESET_REVISION"("ID");


--
-- Name: fk_ao_9b2e3b_ruleset_revision_rule_set_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_RULESET_REVISION"
    ADD CONSTRAINT fk_ao_9b2e3b_ruleset_revision_rule_set_id FOREIGN KEY ("RULE_SET_ID") REFERENCES "AO_9B2E3B_RULESET"("ID");


--
-- Name: fk_ao_9b2e3b_then_act_conf_data_then_action_config_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_CONF_DATA"
    ADD CONSTRAINT fk_ao_9b2e3b_then_act_conf_data_then_action_config_id FOREIGN KEY ("THEN_ACTION_CONFIG_ID") REFERENCES "AO_9B2E3B_THEN_ACTION_CONFIG"("ID");


--
-- Name: fk_ao_9b2e3b_then_act_execution_then_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACT_EXECUTION"
    ADD CONSTRAINT fk_ao_9b2e3b_then_act_execution_then_execution_id FOREIGN KEY ("THEN_EXECUTION_ID") REFERENCES "AO_9B2E3B_THEN_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_then_action_config_if_then_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_ACTION_CONFIG"
    ADD CONSTRAINT fk_ao_9b2e3b_then_action_config_if_then_id FOREIGN KEY ("IF_THEN_ID") REFERENCES "AO_9B2E3B_IF_THEN"("ID");


--
-- Name: fk_ao_9b2e3b_then_execution_if_then_execution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_THEN_EXECUTION"
    ADD CONSTRAINT fk_ao_9b2e3b_then_execution_if_then_execution_id FOREIGN KEY ("IF_THEN_EXECUTION_ID") REFERENCES "AO_9B2E3B_IF_THEN_EXECUTION"("ID");


--
-- Name: fk_ao_9b2e3b_when_hand_conf_data_when_handler_config_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HAND_CONF_DATA"
    ADD CONSTRAINT fk_ao_9b2e3b_when_hand_conf_data_when_handler_config_id FOREIGN KEY ("WHEN_HANDLER_CONFIG_ID") REFERENCES "AO_9B2E3B_WHEN_HANDLER_CONFIG"("ID");


--
-- Name: fk_ao_9b2e3b_when_handler_config_rule_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_9B2E3B_WHEN_HANDLER_CONFIG"
    ADD CONSTRAINT fk_ao_9b2e3b_when_handler_config_rule_id FOREIGN KEY ("RULE_ID") REFERENCES "AO_9B2E3B_RULE"("ID");


--
-- Name: fk_ao_a415df_aoability_aoperson_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABILITY"
    ADD CONSTRAINT fk_ao_a415df_aoability_aoperson_id FOREIGN KEY ("AOPERSON_ID") REFERENCES "AO_A415DF_AOPERSON"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoabsence_aoperson_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOABSENCE"
    ADD CONSTRAINT fk_ao_a415df_aoabsence_aoperson_id FOREIGN KEY ("AOPERSON_ID") REFERENCES "AO_A415DF_AOPERSON"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoavailability_aoresource_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOAVAILABILITY"
    ADD CONSTRAINT fk_ao_a415df_aoavailability_aoresource_id FOREIGN KEY ("AORESOURCE_ID") REFERENCES "AO_A415DF_AORESOURCE"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoestimate_aowork_item_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOESTIMATE"
    ADD CONSTRAINT fk_ao_a415df_aoestimate_aowork_item_id FOREIGN KEY ("AOWORK_ITEM_ID") REFERENCES "AO_A415DF_AOWORK_ITEM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aonon_working_days_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AONON_WORKING_DAYS"
    ADD CONSTRAINT fk_ao_a415df_aonon_working_days_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoperson_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPERSON"
    ADD CONSTRAINT fk_ao_a415df_aoperson_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoplan_configuration_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPLAN_CONFIGURATION"
    ADD CONSTRAINT fk_ao_a415df_aoplan_configuration_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aopresence_aoperson_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOPRESENCE"
    ADD CONSTRAINT fk_ao_a415df_aopresence_aoperson_id FOREIGN KEY ("AOPERSON_ID") REFERENCES "AO_A415DF_AOPERSON"("ID_OTHER");


--
-- Name: fk_ao_a415df_aorelease_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORELEASE"
    ADD CONSTRAINT fk_ao_a415df_aorelease_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aorelease_aostream_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORELEASE"
    ADD CONSTRAINT fk_ao_a415df_aorelease_aostream_id FOREIGN KEY ("AOSTREAM_ID") REFERENCES "AO_A415DF_AOSTREAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoreplanning_work_item_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOREPLANNING"
    ADD CONSTRAINT fk_ao_a415df_aoreplanning_work_item_id FOREIGN KEY ("WORK_ITEM_ID") REFERENCES "AO_A415DF_AOWORK_ITEM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoresource_aoperson_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORESOURCE"
    ADD CONSTRAINT fk_ao_a415df_aoresource_aoperson_id FOREIGN KEY ("AOPERSON_ID") REFERENCES "AO_A415DF_AOPERSON"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoresource_aoteam_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AORESOURCE"
    ADD CONSTRAINT fk_ao_a415df_aoresource_aoteam_id FOREIGN KEY ("AOTEAM_ID") REFERENCES "AO_A415DF_AOTEAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoskill_aostage_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSKILL"
    ADD CONSTRAINT fk_ao_a415df_aoskill_aostage_id FOREIGN KEY ("AOSTAGE_ID") REFERENCES "AO_A415DF_AOSTAGE"("ID_OTHER");


--
-- Name: fk_ao_a415df_aosprint_aoteam_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSPRINT"
    ADD CONSTRAINT fk_ao_a415df_aosprint_aoteam_id FOREIGN KEY ("AOTEAM_ID") REFERENCES "AO_A415DF_AOTEAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aostage_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTAGE"
    ADD CONSTRAINT fk_ao_a415df_aostage_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aostream_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM"
    ADD CONSTRAINT fk_ao_a415df_aostream_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aostream_to_team_aostream_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM_TO_TEAM"
    ADD CONSTRAINT fk_ao_a415df_aostream_to_team_aostream_id FOREIGN KEY ("AOSTREAM_ID") REFERENCES "AO_A415DF_AOSTREAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aostream_to_team_aoteam_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOSTREAM_TO_TEAM"
    ADD CONSTRAINT fk_ao_a415df_aostream_to_team_aoteam_id FOREIGN KEY ("AOTEAM_ID") REFERENCES "AO_A415DF_AOTEAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aoteam_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTEAM"
    ADD CONSTRAINT fk_ao_a415df_aoteam_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aotheme_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOTHEME"
    ADD CONSTRAINT fk_ao_a415df_aotheme_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aoparent_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aoparent_id FOREIGN KEY ("AOPARENT_ID") REFERENCES "AO_A415DF_AOWORK_ITEM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aoplan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aoplan_id FOREIGN KEY ("AOPLAN_ID") REFERENCES "AO_A415DF_AOPLAN"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aorelease_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aorelease_id FOREIGN KEY ("AORELEASE_ID") REFERENCES "AO_A415DF_AORELEASE"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aosprint_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aosprint_id FOREIGN KEY ("AOSPRINT_ID") REFERENCES "AO_A415DF_AOSPRINT"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aostream_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aostream_id FOREIGN KEY ("AOSTREAM_ID") REFERENCES "AO_A415DF_AOSTREAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aoteam_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aoteam_id FOREIGN KEY ("AOTEAM_ID") REFERENCES "AO_A415DF_AOTEAM"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_aotheme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_aotheme_id FOREIGN KEY ("AOTHEME_ID") REFERENCES "AO_A415DF_AOTHEME"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_to_res_aoresource_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM_TO_RES"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_to_res_aoresource_id FOREIGN KEY ("AORESOURCE_ID") REFERENCES "AO_A415DF_AORESOURCE"("ID_OTHER");


--
-- Name: fk_ao_a415df_aowork_item_to_res_aowork_item_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_A415DF_AOWORK_ITEM_TO_RES"
    ADD CONSTRAINT fk_ao_a415df_aowork_item_to_res_aowork_item_id FOREIGN KEY ("AOWORK_ITEM_ID") REFERENCES "AO_A415DF_AOWORK_ITEM"("ID_OTHER");


--
-- Name: fk_ao_aefed0_membership_team_member_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_MEMBERSHIP"
    ADD CONSTRAINT fk_ao_aefed0_membership_team_member_id FOREIGN KEY ("TEAM_MEMBER_ID") REFERENCES "AO_AEFED0_TEAM_MEMBER_V2"("ID");


--
-- Name: fk_ao_aefed0_membership_team_role_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_MEMBERSHIP"
    ADD CONSTRAINT fk_ao_aefed0_membership_team_role_id FOREIGN KEY ("TEAM_ROLE_ID") REFERENCES "AO_AEFED0_TEAM_ROLE"("ID");


--
-- Name: fk_ao_aefed0_team_link_team_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_LINK"
    ADD CONSTRAINT fk_ao_aefed0_team_link_team_id FOREIGN KEY ("TEAM_ID") REFERENCES "AO_AEFED0_TEAM_V2"("ID");


--
-- Name: fk_ao_aefed0_team_member_v2_team_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_MEMBER_V2"
    ADD CONSTRAINT fk_ao_aefed0_team_member_v2_team_id FOREIGN KEY ("TEAM_ID") REFERENCES "AO_AEFED0_TEAM_V2"("ID");


--
-- Name: fk_ao_aefed0_team_permission_team_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_PERMISSION"
    ADD CONSTRAINT fk_ao_aefed0_team_permission_team_id FOREIGN KEY ("TEAM_ID") REFERENCES "AO_AEFED0_TEAM_V2"("ID");


--
-- Name: fk_ao_aefed0_team_to_member_team_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_TO_MEMBER"
    ADD CONSTRAINT fk_ao_aefed0_team_to_member_team_id FOREIGN KEY ("TEAM_ID") REFERENCES "AO_AEFED0_TEAM"("ID");


--
-- Name: fk_ao_aefed0_team_to_member_team_member_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_TO_MEMBER"
    ADD CONSTRAINT fk_ao_aefed0_team_to_member_team_member_id FOREIGN KEY ("TEAM_MEMBER_ID") REFERENCES "AO_AEFED0_TEAM_MEMBER"("ID");


--
-- Name: fk_ao_aefed0_team_v2_program_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_AEFED0_TEAM_V2"
    ADD CONSTRAINT fk_ao_aefed0_team_v2_program_id FOREIGN KEY ("PROGRAM_ID") REFERENCES "AO_AEFED0_PROGRAM"("ID");


--
-- Name: fk_ao_c3c6e8_account_v1_category_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_ACCOUNT_V1"
    ADD CONSTRAINT fk_ao_c3c6e8_account_v1_category_id FOREIGN KEY ("CATEGORY_ID") REFERENCES "AO_C3C6E8_CATEGORY_V1"("ID");


--
-- Name: fk_ao_c3c6e8_account_v1_customer_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_ACCOUNT_V1"
    ADD CONSTRAINT fk_ao_c3c6e8_account_v1_customer_id FOREIGN KEY ("CUSTOMER_ID") REFERENCES "AO_C3C6E8_CUSTOMER_V1"("ID");


--
-- Name: fk_ao_c3c6e8_category_v1_category_type_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CATEGORY_V1"
    ADD CONSTRAINT fk_ao_c3c6e8_category_v1_category_type_id FOREIGN KEY ("CATEGORY_TYPE_ID") REFERENCES "AO_C3C6E8_CATEGORY_TYPE"("ID");


--
-- Name: fk_ao_c3c6e8_customer_permission_customer_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_CUSTOMER_PERMISSION"
    ADD CONSTRAINT fk_ao_c3c6e8_customer_permission_customer_id FOREIGN KEY ("CUSTOMER_ID") REFERENCES "AO_C3C6E8_CUSTOMER_V1"("ID");


--
-- Name: fk_ao_c3c6e8_link_v1_account_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_LINK_V1"
    ADD CONSTRAINT fk_ao_c3c6e8_link_v1_account_id FOREIGN KEY ("ACCOUNT_ID") REFERENCES "AO_C3C6E8_ACCOUNT_V1"("ID");


--
-- Name: fk_ao_c3c6e8_rate_rate_table_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE"
    ADD CONSTRAINT fk_ao_c3c6e8_rate_rate_table_id FOREIGN KEY ("RATE_TABLE_ID") REFERENCES "AO_C3C6E8_RATE_TABLE"("ID");


--
-- Name: fk_ao_c3c6e8_rate_table_parent_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C3C6E8_RATE_TABLE"
    ADD CONSTRAINT fk_ao_c3c6e8_rate_table_parent_id FOREIGN KEY ("PARENT_ID") REFERENCES "AO_C3C6E8_RATE_TABLE"("ID");


--
-- Name: fk_ao_c7f17e_lingo_revision_lingo_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_REVISION"
    ADD CONSTRAINT fk_ao_c7f17e_lingo_revision_lingo_id FOREIGN KEY ("LINGO_ID") REFERENCES "AO_C7F17E_LINGO"("ID");


--
-- Name: fk_ao_c7f17e_lingo_translation_lingo_revision_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_C7F17E_LINGO_TRANSLATION"
    ADD CONSTRAINT fk_ao_c7f17e_lingo_translation_lingo_revision_id FOREIGN KEY ("LINGO_REVISION_ID") REFERENCES "AO_C7F17E_LINGO_REVISION"("ID");


--
-- Name: fk_ao_d9132d_assignment_solution_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ASSIGNMENT"
    ADD CONSTRAINT fk_ao_d9132d_assignment_solution_id FOREIGN KEY ("SOLUTION_ID") REFERENCES "AO_D9132D_SOLUTION"("ID");


--
-- Name: fk_ao_d9132d_distribution_scenario_issue_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_DISTRIBUTION"
    ADD CONSTRAINT fk_ao_d9132d_distribution_scenario_issue_id FOREIGN KEY ("SCENARIO_ISSUE_ID") REFERENCES "AO_D9132D_SCENARIO_ISSUES"("ID");


--
-- Name: fk_ao_d9132d_excluded_versions_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_EXCLUDED_VERSIONS"
    ADD CONSTRAINT fk_ao_d9132d_excluded_versions_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_issue_source_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_ISSUE_SOURCE"
    ADD CONSTRAINT fk_ao_d9132d_issue_source_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_nonworkingdays_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_NONWORKINGDAYS"
    ADD CONSTRAINT fk_ao_d9132d_nonworkingdays_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_permissions_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PERMISSIONS"
    ADD CONSTRAINT fk_ao_d9132d_permissions_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_planskill_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANSKILL"
    ADD CONSTRAINT fk_ao_d9132d_planskill_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_planteam_issue_source_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTEAM"
    ADD CONSTRAINT fk_ao_d9132d_planteam_issue_source_id FOREIGN KEY ("ISSUE_SOURCE_ID") REFERENCES "AO_D9132D_ISSUE_SOURCE"("ID");


--
-- Name: fk_ao_d9132d_planteam_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTEAM"
    ADD CONSTRAINT fk_ao_d9132d_planteam_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_plantheme_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTHEME"
    ADD CONSTRAINT fk_ao_d9132d_plantheme_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_plantheme_theme_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANTHEME"
    ADD CONSTRAINT fk_ao_d9132d_plantheme_theme_id FOREIGN KEY ("THEME_ID") REFERENCES "AO_D9132D_THEME"("ID");


--
-- Name: fk_ao_d9132d_planversion_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANVERSION"
    ADD CONSTRAINT fk_ao_d9132d_planversion_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_planversion_xproject_version_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_PLANVERSION"
    ADD CONSTRAINT fk_ao_d9132d_planversion_xproject_version_id FOREIGN KEY ("XPROJECT_VERSION_ID") REFERENCES "AO_D9132D_X_PROJECT_VERSION"("ID");


--
-- Name: fk_ao_d9132d_scenario_ability_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ABILITY"
    ADD CONSTRAINT fk_ao_d9132d_scenario_ability_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_avlblty_scenario_resource_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_AVLBLTY"
    ADD CONSTRAINT fk_ao_d9132d_scenario_avlblty_scenario_resource_id FOREIGN KEY ("SCENARIO_RESOURCE_ID") REFERENCES "AO_D9132D_SCENARIO_RESOURCE"("ID");


--
-- Name: fk_ao_d9132d_scenario_changes_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_CHANGES"
    ADD CONSTRAINT fk_ao_d9132d_scenario_changes_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_issue_links_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_LINKS"
    ADD CONSTRAINT fk_ao_d9132d_scenario_issue_links_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_issue_res_scenario_issue_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUE_RES"
    ADD CONSTRAINT fk_ao_d9132d_scenario_issue_res_scenario_issue_id FOREIGN KEY ("SCENARIO_ISSUE_ID") REFERENCES "AO_D9132D_SCENARIO_ISSUES"("ID");


--
-- Name: fk_ao_d9132d_scenario_issues_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_ISSUES"
    ADD CONSTRAINT fk_ao_d9132d_scenario_issues_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_person_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_PERSON"
    ADD CONSTRAINT fk_ao_d9132d_scenario_person_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO"
    ADD CONSTRAINT fk_ao_d9132d_scenario_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_scenario_resource_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_RESOURCE"
    ADD CONSTRAINT fk_ao_d9132d_scenario_resource_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_skill_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_SKILL"
    ADD CONSTRAINT fk_ao_d9132d_scenario_skill_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_stage_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_STAGE"
    ADD CONSTRAINT fk_ao_d9132d_scenario_stage_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_team_issue_source_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_TEAM"
    ADD CONSTRAINT fk_ao_d9132d_scenario_team_issue_source_id FOREIGN KEY ("ISSUE_SOURCE_ID") REFERENCES "AO_D9132D_ISSUE_SOURCE"("ID");


--
-- Name: fk_ao_d9132d_scenario_team_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_TEAM"
    ADD CONSTRAINT fk_ao_d9132d_scenario_team_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_theme_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_THEME"
    ADD CONSTRAINT fk_ao_d9132d_scenario_theme_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_version_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_VERSION"
    ADD CONSTRAINT fk_ao_d9132d_scenario_version_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_scenario_xpversion_scenario_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_SCENARIO_XPVERSION"
    ADD CONSTRAINT fk_ao_d9132d_scenario_xpversion_scenario_id FOREIGN KEY ("SCENARIO_ID") REFERENCES "AO_D9132D_SCENARIO"("ID");


--
-- Name: fk_ao_d9132d_stage_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_STAGE"
    ADD CONSTRAINT fk_ao_d9132d_stage_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_d9132d_x_project_version_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_D9132D_X_PROJECT_VERSION"
    ADD CONSTRAINT fk_ao_d9132d_x_project_version_plan_id FOREIGN KEY ("PLAN_ID") REFERENCES "AO_D9132D_PLAN"("ID");


--
-- Name: fk_ao_e8b6cc_branch_head_mapping_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING"
    ADD CONSTRAINT fk_ao_e8b6cc_branch_head_mapping_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_branch_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH"
    ADD CONSTRAINT fk_ao_e8b6cc_branch_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_git_hub_event_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT"
    ADD CONSTRAINT fk_ao_e8b6cc_git_hub_event_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_issue_to_branch_branch_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH"
    ADD CONSTRAINT fk_ao_e8b6cc_issue_to_branch_branch_id FOREIGN KEY ("BRANCH_ID") REFERENCES "AO_E8B6CC_BRANCH"("ID");


--
-- Name: fk_ao_e8b6cc_issue_to_changeset_changeset_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_issue_to_changeset_changeset_id FOREIGN KEY ("CHANGESET_ID") REFERENCES "AO_E8B6CC_CHANGESET_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_message_queue_item_message_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"
    ADD CONSTRAINT fk_ao_e8b6cc_message_queue_item_message_id FOREIGN KEY ("MESSAGE_ID") REFERENCES "AO_E8B6CC_MESSAGE"("ID");


--
-- Name: fk_ao_e8b6cc_message_tag_message_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG"
    ADD CONSTRAINT fk_ao_e8b6cc_message_tag_message_id FOREIGN KEY ("MESSAGE_ID") REFERENCES "AO_E8B6CC_MESSAGE"("ID");


--
-- Name: fk_ao_e8b6cc_org_to_project_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_ORG_TO_PROJECT"
    ADD CONSTRAINT fk_ao_e8b6cc_org_to_project_organization_id FOREIGN KEY ("ORGANIZATION_ID") REFERENCES "AO_E8B6CC_ORGANIZATION_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_pr_participant_pull_request_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_participant_pull_request_id FOREIGN KEY ("PULL_REQUEST_ID") REFERENCES "AO_E8B6CC_PULL_REQUEST"("ID");


--
-- Name: fk_ao_e8b6cc_pr_to_commit_commit_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_to_commit_commit_id FOREIGN KEY ("COMMIT_ID") REFERENCES "AO_E8B6CC_COMMIT"("ID");


--
-- Name: fk_ao_e8b6cc_pr_to_commit_request_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_to_commit_request_id FOREIGN KEY ("REQUEST_ID") REFERENCES "AO_E8B6CC_PULL_REQUEST"("ID");


--
-- Name: fk_ao_e8b6cc_repo_to_changeset_changeset_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_changeset_id FOREIGN KEY ("CHANGESET_ID") REFERENCES "AO_E8B6CC_CHANGESET_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_repo_to_changeset_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_repo_to_project_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_PROJECT"
    ADD CONSTRAINT fk_ao_e8b6cc_repo_to_project_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_f1b27b_key_comp_history_timed_promise_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMP_HISTORY"
    ADD CONSTRAINT fk_ao_f1b27b_key_comp_history_timed_promise_id FOREIGN KEY ("TIMED_PROMISE_ID") REFERENCES "AO_F1B27B_PROMISE_HISTORY"("ID");


--
-- Name: fk_ao_f1b27b_key_component_timed_promise_id; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY "AO_F1B27B_KEY_COMPONENT"
    ADD CONSTRAINT fk_ao_f1b27b_key_component_timed_promise_id FOREIGN KEY ("TIMED_PROMISE_ID") REFERENCES "AO_F1B27B_PROMISE"("ID");


--
-- Name: oauth_consumer_token_182c39_consumer_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_consumer_token_182c39
    ADD CONSTRAINT oauth_consumer_token_182c39_consumer_key_fkey FOREIGN KEY (consumer_key) REFERENCES oauth_consumer_182c39(key);


--
-- Name: oauth_service_provider_token_ecd6b3_consumer_key_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jira
--

ALTER TABLE ONLY oauth_service_provider_token_ecd6b3
    ADD CONSTRAINT oauth_service_provider_token_ecd6b3_consumer_key_fkey FOREIGN KEY (consumer_key) REFERENCES oauth_service_provider_consumer_ecd6b3(key);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: tools; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA tools FROM PUBLIC;
REVOKE ALL ON SCHEMA tools FROM postgres;
GRANT ALL ON SCHEMA tools TO postgres;
GRANT USAGE ON SCHEMA tools TO jira;


--
-- PostgreSQL database dump complete
--

