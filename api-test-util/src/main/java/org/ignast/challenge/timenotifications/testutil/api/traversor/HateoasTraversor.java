package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.link;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import lombok.NonNull;
import lombok.val;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;

public final class HateoasTraversor {

    private final MediaType appMediaType;

    private final Hop.Factory hopFactory;

    private final List<Hop.TraversableHop> hops;

    private final String rootUri;

    private HateoasTraversor(
        final MediaType appMediaType,
        final Hop.Factory hopFactory,
        final String rootUri,
        final List<Hop.TraversableHop> hops
    ) {
        this.appMediaType = appMediaType;
        this.hopFactory = hopFactory;
        this.rootUri = rootUri;
        this.hops = hops;
    }

    public HateoasTraversor hop(final Function<Hop.Factory, Hop.TraversableHop> constructHop) {
        final val newHop = constructHop.apply(hopFactory);
        final val hopsPlusNewOne = concat(hops.stream(), of(newHop)).collect(toUnmodifiableList());
        return new HateoasTraversor(appMediaType, hopFactory, rootUri, hopsPlusNewOne);
    }

    public ResponseEntity<String> perform() {
        final val rootRel = "root";
        final val linkToRoot = link(rootRel, rootUri);
        final val fakeLinkToRoot = ResponseEntity
            .status(HttpStatus.OK)
            .contentType(appMediaType)
            .body(linkToRoot);
        final val rootHop = hopFactory.get(rootRel);
        return concat(of(rootHop), hops.stream())
            .reduce(fakeLinkToRoot, (r, h) -> h.traverse(r), combinerUnsupported());
    }

    private BinaryOperator<ResponseEntity<String>> combinerUnsupported() {
        return (a, b) -> {
            throw new IllegalArgumentException("combinations are not supported");
        };
    }

    @Service
    public static final class Factory {

        private final Hop.Factory hopFactory;

        private final MediaType appMediaType;

        public Factory(final RestTemplateBuilder builder, final MediaType appMediaType) {
            this.appMediaType = appMediaType;
            final val restTemplate = builder.errorHandler(new NoSpecialHandling()).build();
            hopFactory = new Hop.Factory(appMediaType, restTemplate, new HrefExtractor(appMediaType));
        }

        public HateoasTraversor startAt(@NonNull final String rootUri) {
            return new HateoasTraversor(appMediaType, hopFactory, rootUri, emptyList());
        }

        private static class NoSpecialHandling implements ResponseErrorHandler {

            @Override
            public boolean hasError(final ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(final ClientHttpResponse response) {}
        }
    }
}
