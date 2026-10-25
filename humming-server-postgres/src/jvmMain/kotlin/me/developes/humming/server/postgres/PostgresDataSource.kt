package me.developes.humming.server.postgres

import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

/** Credentials belong to the host. Each operation closes its own connection; no implicit pool. */
fun postgresDataSource(url: String, user: String, password: String): DataSource {
    require(url.startsWith("jdbc:postgresql://") && user.isNotBlank() && password.isNotEmpty())
    return PGSimpleDataSource().apply {
        setURL(url)
        this.user = user
        this.password = password
        connectTimeout = 5
        socketTimeout = 15
        applicationName = "humming-drafts"
    }
}
