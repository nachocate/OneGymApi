# OneGymApi

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

* [Ktor Documentation](https://ktor.io/docs/home.html)
* [Ktor GitHub page](https://github.com/ktorio/ktor)
* [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). [Request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up).

## Features

Here's a list of features included in this project:

| Name                                                                                  | Description                                                                        |
|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Content Negotiation](https://start.ktor.io/p/io.ktor/server-content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/io.ktor/server-kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |

## Building & Running

To build or run the project, use one of the following tasks:

| Task              | Description       |
|-------------------|-------------------|
| `./gradlew test`  | Run the tests     |
| `./gradlew build` | Build the project |
| `./gradlew run`   | Run the server    |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## PostgreSQL

The API uses the existing PostgreSQL database `onegym_db`. Configure the
connection in `src/main/resources/application.conf`:

```hocon
database {
    url = "jdbc:postgresql://localhost:5432/onegym_db"
    user = "your_user"
    password = "your_password"
    pool-size = 10
}
```

Then start the server:

```powershell
./gradlew.bat run
```

The `users` table is created automatically on the first user request.
Environment variables named `DB_URL`, `DB_USER`, `DB_PASSWORD` and `DB_POOL_SIZE`
can be used later and take priority over `application.conf`.

## Authentication

All resource routes require `Authorization: Bearer <accessToken>`. Local JWT
settings are in `src/main/resources/application.conf`. For production, override
the values with these environment variables (use independent random values of
at least 32 characters):

```powershell
$env:JWT_SECRET = "replace-with-a-long-random-secret"
$env:JWT_REFRESH_TOKEN_HASH_SECRET = "replace-with-a-second-long-random-secret"
```

`POST /login` accepts the former login body plus optional `deviceInfo` and returns
the user, a 15-minute access token, and a 30-day refresh token. Use
`POST /auth/refresh` with `{ "refreshToken": "..." }` to rotate both tokens,
and `POST /auth/logout` with the refresh token to revoke that session. The TTLs
can be changed through `JWT_ACCESS_TOKEN_TTL_MINUTES` and
`JWT_REFRESH_TOKEN_TTL_DAYS`.

For an existing database, run
`src/main/resources/migrations/V2__create_user_refresh_tokens.sql` once before
deploying. The API does not currently include a migration runner.
