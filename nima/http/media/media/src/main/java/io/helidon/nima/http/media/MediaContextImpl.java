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

package io.helidon.nima.http.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.helidon.common.GenericType;
import io.helidon.common.HelidonServiceLoader;
import io.helidon.common.http.Headers;
import io.helidon.common.http.WritableHeaders;
import io.helidon.nima.http.media.spi.MediaSupportProvider;
import io.helidon.nima.http.media.spi.MediaSupportProvider.ReaderResponse;
import io.helidon.nima.http.media.spi.MediaSupportProvider.WriterResponse;

import static io.helidon.nima.http.media.spi.MediaSupportProvider.SupportLevel.COMPATIBLE;
import static io.helidon.nima.http.media.spi.MediaSupportProvider.SupportLevel.SUPPORTED;

@SuppressWarnings("unchecked")
class MediaContextImpl implements MediaContext {
    private static final System.Logger LOGGER = System.getLogger(MediaContextImpl.class.getName());
    private static final ConcurrentHashMap<GenericType<?>, AtomicBoolean> LOGGED_READERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<GenericType<?>, AtomicBoolean> LOGGED_WRITERS = new ConcurrentHashMap<>();

    private final List<MediaSupportProvider> providers =
            HelidonServiceLoader.builder(ServiceLoader.load(MediaSupportProvider.class))
                    .addService(new StringSupportProvider())
                    .addService(new FormParamsSupportProvider())
                    .addService(new PathSupportProvider())
                    .build()
                    .asList();

    MediaContextImpl() {
        providers.forEach(it -> it.init(this));
    }

    @Override
    public <T> EntityReader<T> reader(GenericType<T> type, Headers headers) {
        ReaderResponse<T> compatible = null;
        for (MediaSupportProvider provider : providers) {
            ReaderResponse<T> response = provider.reader(type, headers);
            if (response.support() == SUPPORTED) {
                return entityReader(response);
            } else if (response.support() == COMPATIBLE) {
                compatible = compatible == null ? response : compatible;
            }
        }
        if (compatible == null) {
            return FailingReader.instance();
        }
        return entityReader(compatible);
    }

    @Override
    public <T> EntityWriter<T> writer(GenericType<T> type,
                                      Headers requestHeaders,
                                      WritableHeaders<?> responseHeaders) {
        WriterResponse<T> compatible = null;
        for (MediaSupportProvider provider : providers) {
            WriterResponse<T> response = provider.writer(type, requestHeaders, responseHeaders);
            if (response.support() == SUPPORTED) {
                return entityWriter(response);
            }
            if (response.support() == COMPATIBLE) {
                compatible = compatible == null ? response : compatible;
            }
        }

        if (compatible == null) {
            return FailingWriter.instance();
        }
        return entityWriter(compatible);
    }

    @Override
    public <T> EntityReader<T> reader(GenericType<T> type,
                                      Headers requestHeaders,
                                      Headers responseHeaders) {

        ReaderResponse<T> compatible = null;
        for (MediaSupportProvider provider : providers) {
            ReaderResponse<T> response = provider.reader(type, requestHeaders, responseHeaders);
            if (response.support() == SUPPORTED) {
                return entityReader(response);
            }
            if (response.support() == COMPATIBLE) {
                compatible = compatible == null ? response : compatible;
            }
        }
        if (compatible == null) {
            return FailingReader.instance();
        }
        return entityReader(compatible);
    }

    @Override
    public <T> EntityWriter<T> writer(GenericType<T> type, WritableHeaders<?> requestHeaders) {
        WriterResponse<T> compatible = null;
        for (MediaSupportProvider provider : providers) {
            WriterResponse<T> response = provider.writer(type, requestHeaders);
            if (response.support() == SUPPORTED) {
                return entityWriter(response);
            }
            if (response.support() == COMPATIBLE) {
                compatible = compatible == null ? response : compatible;
            }
        }

        if (compatible == null) {
            return FailingWriter.instance();
        }
        return entityWriter(compatible);
    }

    private <T> EntityWriter<T> entityWriter(WriterResponse<T> response) {
        return new CloseStreamWriter(response.supplier().get());
    }

    private <T> EntityReader<T> entityReader(ReaderResponse<T> response) {
        return new CloseStreamReader(response.supplier().get());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class FailingWriter implements EntityWriter {
        private static final FailingWriter INSTANCE = new FailingWriter();

        static <T> EntityWriter<T> instance() {
            return INSTANCE;
        }

        @Override
        public void write(GenericType type,
                          Object object,
                          OutputStream outputStream,
                          Headers requestHeaders,
                          WritableHeaders responseHeaders) {
            if (LOGGED_WRITERS.computeIfAbsent(type, it -> new AtomicBoolean()).compareAndSet(false, true)) {
                LOGGER.log(System.Logger.Level.WARNING, "There is no media writer configured for " + type);
            }

            throw new IllegalArgumentException("No server response media writer for " + type + " configured");
        }

        @Override
        public void write(GenericType type,
                          Object object,
                          OutputStream outputStream,
                          WritableHeaders headers) {

            if (LOGGED_WRITERS.computeIfAbsent(type, it -> new AtomicBoolean()).compareAndSet(false, true)) {
                LOGGER.log(System.Logger.Level.WARNING, "There is no media writer configured for " + type);
            }

            throw new IllegalArgumentException("No client request media writer for " + type + " configured");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class FailingReader implements EntityReader {
        private static final FailingReader INSTANCE = new FailingReader();

        static <T> EntityReader<T> instance() {
            return INSTANCE;
        }

        @Override
        public Object read(GenericType type, InputStream stream, Headers headers) {
            if (LOGGED_READERS.computeIfAbsent(type, it -> new AtomicBoolean()).compareAndSet(false, true)) {
                LOGGER.log(System.Logger.Level.WARNING, "There is no media reader configured for " + type);
            }
            throw new IllegalArgumentException("No server request media support for " + type + " configured");
        }

        @Override
        public Object read(GenericType type,
                           InputStream stream,
                           Headers requestHeaders,
                           Headers responseHeaders) {
            if (LOGGED_READERS.computeIfAbsent(type, it -> new AtomicBoolean()).compareAndSet(false, true)) {
                LOGGER.log(System.Logger.Level.WARNING, "There is no media reader configured for " + type);
            }
            throw new IllegalArgumentException("No client response media support for " + type + " configured");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class CloseStreamReader implements EntityReader {
        private final EntityReader delegate;

        CloseStreamReader(EntityReader delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object read(GenericType type, InputStream stream, Headers headers) {
            try (stream) {
                return delegate.read(type, stream, headers);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read server request", e);
            }
        }

        @Override
        public Object read(GenericType type,
                           InputStream stream,
                           Headers requestHeaders,
                           Headers responseHeaders) {
            try (stream) {
                return delegate.read(type, stream, requestHeaders, responseHeaders);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read client response", e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class CloseStreamWriter implements EntityWriter {
        private final EntityWriter delegate;

        CloseStreamWriter(EntityWriter delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(GenericType type,
                          Object object,
                          OutputStream outputStream,
                          Headers requestHeaders,
                          WritableHeaders responseHeaders) {
            delegate.write(type, object, outputStream, requestHeaders, responseHeaders);
        }

        @Override
        public void write(GenericType type,
                          Object object,
                          OutputStream outputStream,
                          WritableHeaders headers) {
            delegate.write(type,
                           object,
                           outputStream,
                           headers);
        }
    }
}
