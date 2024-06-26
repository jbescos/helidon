/*
 * Copyright (c) 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.common.socket;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Optional;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

/**
 * TLS socket.
 */
public final class TlsSocket extends PlainSocket {
    private final SSLSocket sslSocket;

    private TlsSocket(SSLSocket socket, String channelId, String serverChannelId) {
        super(socket, channelId, serverChannelId);

        this.sslSocket = socket;
    }

    /**
     * Create a server TLS socket.
     *
     * @param delegate underlying socket
     * @param channelId listener channel id
     * @param serverChannelId connection channel id
     * @return a new TLS socket
     */
    public static TlsSocket server(SSLSocket delegate,
                                   String channelId,
                                   String serverChannelId) {
        return new TlsSocket(delegate, channelId, serverChannelId);
    }

    /**
     * Create a client TLS socket.
     *
     * @param delegate underlying socket
     * @param channelId channel id
     * @return a new TLS socket
     */
    public static TlsSocket client(SSLSocket delegate,
                                   String channelId) {
        return new TlsSocket(delegate, channelId, "client");
    }

    @Override
    public PeerInfo remotePeer() {
        return PeerInfoImpl.createRemote(this);
    }

    @Override
    public PeerInfo localPeer() {
        return PeerInfoImpl.createLocal(this);
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public boolean protocolNegotiated() {
        String protocol = sslSocket.getApplicationProtocol();
        return protocol != null && !protocol.isBlank();
    }

    @Override
    public String protocol() {
        return sslSocket.getApplicationProtocol();
    }

    Optional<Principal> tlsPeerPrincipal() {
        try {
            return Optional.of(sslSocket.getSession().getPeerPrincipal());
        } catch (SSLPeerUnverifiedException e) {
            return Optional.empty();
        }
    }

    Optional<Certificate[]> tlsPeerCertificates() {
        try {
            return Optional.of(sslSocket.getSession().getPeerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return Optional.empty();
        }
    }

    Optional<Principal> tlsPrincipal() {
        return Optional.of(sslSocket.getSession().getLocalPrincipal());
    }

    Optional<Certificate[]> tlsCertificates() {
        return Optional.of(sslSocket.getSession().getLocalCertificates());
    }
}
