# API users

Set SPOTIFY_API_USERS_JSON to a JSON object mapping each distinct username to its encoded bcrypt password hash. Each hash must have the `{bcrypt}` prefix followed by a full bcrypt hash; plaintext and `{noop}` values are rejected. For example, generate a hash with Spring's BCryptPasswordEncoder in a trusted local administrative tool. Do not place real hashes or passwords in repository examples.

No account or password is supplied by default. Requests use HTTP Basic per user; deploy only behind HTTPS. The frontend asks each user for their credentials and keeps them in memory until sign-out/reload. CORS remains restricted to the configured local UI origin; configure a specific HTTPS origin when deploying.

Rotate any password reused from the old hardcoded demo account. The H2 console and API docs are disabled by default. The current shared USER role is for this code-challenge API; define resource ownership before adding user-private records.
