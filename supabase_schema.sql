-- ====================================================================
-- SANKALP KAVACHAM - SUPABASE DATABASE SCHEMA
-- Community-Driven Smart Call & Cyber Safety Platform
-- ====================================================================

-- 1. Create spam_numbers table
CREATE TABLE IF NOT EXISTS public.spam_numbers (
    id BIGSERIAL PRIMARY KEY,
    number TEXT NOT NULL UNIQUE,
    reason TEXT NOT NULL DEFAULT 'Spam / Fraud Call',
    reported_count INT NOT NULL DEFAULT 1,
    is_verified_spam BOOLEAN GENERATED ALWAYS AS (reported_count >= 10) STORED,
    risk_level TEXT GENERATED ALWAYS AS (
        CASE 
            WHEN reported_count >= 10 THEN 'RED'
            WHEN reported_count >= 3 THEN 'ORANGE'
            ELSE 'YELLOW'
        END
    ) STORED,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Index for instant number lookup
CREATE INDEX IF NOT EXISTS idx_spam_numbers_number ON public.spam_numbers(number);
CREATE INDEX IF NOT EXISTS idx_spam_numbers_count ON public.spam_numbers(reported_count DESC);

-- 2. Create spam_messages table
CREATE TABLE IF NOT EXISTS public.spam_messages (
    id BIGSERIAL PRIMARY KEY,
    sender TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('Fake Suffix', 'Honey Trap', 'Deepfake', 'Bomber', 'Phishing', 'General')),
    is_spam BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE INDEX IF NOT EXISTS idx_spam_messages_sender ON public.spam_messages(sender);
CREATE INDEX IF NOT EXISTS idx_spam_messages_type ON public.spam_messages(type);

-- 3. Create reports table
CREATE TABLE IF NOT EXISTS public.reports (
    id BIGSERIAL PRIMARY KEY,
    user_number TEXT DEFAULT 'Anonymous',
    fraud_number TEXT NOT NULL,
    message TEXT DEFAULT '',
    type TEXT NOT NULL DEFAULT 'Spam Call',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

CREATE INDEX IF NOT EXISTS idx_reports_fraud_number ON public.reports(fraud_number);

-- ====================================================================
-- 4. AUTOMATIC AGGREGATION TRIGGER:
-- When a user reports a number in `reports`, automatically upsert into
-- `spam_numbers`, increment `reported_count`, and if count >= 10,
-- mark as verified community SPAM for all users!
-- ====================================================================

CREATE OR REPLACE FUNCTION public.handle_new_report()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.spam_numbers (number, reason, reported_count, updated_at)
    VALUES (
        NEW.fraud_number,
        COALESCE(NULLIF(NEW.message, ''), NEW.type, 'Community Reported Spam'),
        1,
        timezone('utc'::text, now())
    )
    ON CONFLICT (number) DO UPDATE
    SET 
        reported_count = public.spam_numbers.reported_count + 1,
        reason = EXCLUDED.reason,
        updated_at = timezone('utc'::text, now());
        
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_report_inserted ON public.reports;
CREATE TRIGGER on_report_inserted
    AFTER INSERT ON public.reports
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_report();

-- ====================================================================
-- 5. ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.spam_numbers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.spam_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;

-- Allow anyone (public/anon) to read community spam lists
CREATE POLICY "Public read access for spam_numbers" 
    ON public.spam_numbers FOR SELECT 
    USING (true);

-- Allow anyone to insert/report a number
CREATE POLICY "Public insert access for spam_numbers" 
    ON public.spam_numbers FOR INSERT 
    WITH CHECK (true);

CREATE POLICY "Public update access for spam_numbers" 
    ON public.spam_numbers FOR UPDATE 
    USING (true);

-- Spam messages policies
CREATE POLICY "Public read access for spam_messages" 
    ON public.spam_messages FOR SELECT 
    USING (true);

CREATE POLICY "Public insert access for spam_messages" 
    ON public.spam_messages FOR INSERT 
    WITH CHECK (true);

-- Reports policies
CREATE POLICY "Public read access for reports" 
    ON public.reports FOR SELECT 
    USING (true);

CREATE POLICY "Public insert access for reports" 
    ON public.reports FOR INSERT 
    WITH CHECK (true);

-- ====================================================================
-- 6. SUPABASE REALTIME ENABLEMENT
-- Allows Android app and Web preview to receive instant live alerts
-- when any user reports a spam number or message!
-- ====================================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'spam_numbers'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.spam_numbers;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'spam_messages'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.spam_messages;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'reports'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.reports;
    END IF;
END $$;
