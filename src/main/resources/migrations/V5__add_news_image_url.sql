-- Adds the optional image URL without changing existing news records.
ALTER TABLE news
    ADD COLUMN IF NOT EXISTS image_url TEXT DEFAULT NULL;
