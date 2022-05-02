package org.ignast.challenge.timenotifications.api.subscriptions;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.validation.Valid;
import lombok.val;
import org.ignast.challenge.timenotifications.domain.Subscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final ExecutorService singleThreadExecution = Executors.newSingleThreadExecutor();

    @Autowired
    private final Subscriptions subscriptions;

    SubscriptionController(final Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    @ResponseBody
    @PostMapping(produces = HAL_JSON_VALUE)
    public DeferredResult<ResponseEntity<EntityModel<SubscriptionDto>>> subscribe(
        @Valid @RequestBody final SubscriptionDto subscriptionDto
    ) {
        val result = new DeferredResult<ResponseEntity<EntityModel<SubscriptionDto>>>();
        val selfLink = generateSelfLink(subscriptionDto.subscriptionUri());
        singleThreadExecution.submit(() -> {
            if (subscriptions.subscribe(subscriptionDto.toPeriodicNotification())) {
                result.setResult(
                    ResponseEntity.created(selfLink.toUri()).body(EntityModel.of(subscriptionDto, selfLink))
                );
            } else {
                result.setErrorResult(ResponseEntity.badRequest().body(Error.uriAlreadyExists()));
            }
        });
        return result;
    }

    @DeleteMapping(value = "/{base64SubscriptionUri}")
    public DeferredResult<ResponseEntity<Void>> unsubscribe(
        @PathVariable final String base64SubscriptionUri
    ) {
        val result = new DeferredResult<ResponseEntity<Void>>();
        val decodedUri = base64decode(base64SubscriptionUri);
        singleThreadExecution.submit(() -> {
            decodedUri
                .filter(uri -> subscriptions.unsubscribe(uri))
                .ifPresentOrElse(
                    uri -> result.setResult(ResponseEntity.ok().build()),
                    () -> result.setErrorResult(ResponseEntity.notFound().build())
                );
        });
        return result;
    }

    @ResponseBody
    @PutMapping(value = "/{base64SubscriptionUri}")
    public DeferredResult<ResponseEntity<EntityModel<SubscriptionDto>>> reschedule(
        @PathVariable final String base64SubscriptionUri,
        @Valid @RequestBody final SubscriptionDto frequencyDto
    ) {
        val result = new DeferredResult<ResponseEntity<EntityModel<SubscriptionDto>>>();
        val subscriptionDto = base64decode(base64SubscriptionUri)
            .map(uri -> new SubscriptionDto(uri, frequencyDto.frequency()));
        val selfLink = subscriptionDto.map(dto -> generateSelfLink(dto.subscriptionUri()));
        singleThreadExecution.submit(() -> {
            subscriptionDto
                .filter(dto -> subscriptions.reschedule(dto.toPeriodicNotification()))
                .ifPresentOrElse(
                    dto -> result.setResult(ResponseEntity.ok().body(EntityModel.of(dto, selfLink.get()))),
                    () -> result.setErrorResult(ResponseEntity.notFound().build())
                );
        });
        return result;
    }

    private Link generateSelfLink(URI uri) {
        return linkTo(methodOn(SubscriptionController.class).reschedule(base64encode(uri), null))
            .withSelfRel();
    }

    private String base64encode(final URI uri) {
        return Base64.getEncoder().encodeToString(uri.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Optional<URI> base64decode(final String encodedUri) {
        try {
            return Optional.of(
                URI.create(new String(Base64.getDecoder().decode(encodedUri), StandardCharsets.UTF_8))
            );
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
