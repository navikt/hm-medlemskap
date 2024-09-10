package no.nav.hjelpemidler.medlemskap.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.medlemskap.LovMeApiClient

sealed class AsyncListener(
    protected val lovMeApiClient: LovMeApiClient,
) : River.PacketListener {
    override fun onPacket(packet: JsonMessage, context: MessageContext) =
        runBlocking(Dispatchers.IO) {
            onPacketAsync(packet, context)
        }

    abstract suspend fun onPacketAsync(packet: JsonMessage, context: MessageContext)
}
