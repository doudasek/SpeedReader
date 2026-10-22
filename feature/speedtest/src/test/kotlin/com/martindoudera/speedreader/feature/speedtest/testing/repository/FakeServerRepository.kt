package com.martindoudera.speedreader.feature.speedtest.testing.repository

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository

class FakeServerRepository : ServerRepository {
    var servers: NetworkOutcome<List<SpeedTestServer>> = Outcome.Success(emptyList())

    override suspend fun servers(): NetworkOutcome<List<SpeedTestServer>> = servers
}
