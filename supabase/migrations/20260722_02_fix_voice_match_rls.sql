-- Disable RLS on voice match tables
ALTER TABLE public.voice_match_queue DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.voice_match_usage DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.random_voice_calls DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.voice_match_abuse DISABLE ROW LEVEL SECURITY;

GRANT ALL ON TABLE public.voice_match_queue TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.voice_match_usage TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.random_voice_calls TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.voice_match_abuse TO anon, authenticated, service_role;
