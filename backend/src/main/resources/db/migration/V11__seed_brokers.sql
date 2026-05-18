INSERT INTO broker (id, name, api_base, is_active) VALUES
    (gen_random_uuid(), 'Tinkoff',    'https://invest-public-api.tinkoff.ru/rest', TRUE),
    (gen_random_uuid(), 'Finam',      'https://trade-api.finam.ru',               TRUE),
    (gen_random_uuid(), 'BCS',        'https://api.bcs.ru/trading',               TRUE),
    (gen_random_uuid(), 'Sberbank',   'https://api.sberbank.ru/invest',           TRUE),
    (gen_random_uuid(), 'Alfa-Bank',  'https://api.alfabank.ru/invest',           TRUE);
