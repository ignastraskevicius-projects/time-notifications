package org.ignast.challenge.timenotifications.api.root;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.val;
import org.ignast.challenge.timenotifications.api.subscriptions.SubscriptionController;
import org.ignast.challenge.timenotifications.api.subscriptions.SubscriptionDto;
import org.ignast.challenge.timenotifications.api.subscriptions.TimePeriodDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    private final SubscriptionDto anySubscriptionDto = anySubscriptionDto();

    @GetMapping(value = "/")
    public HttpEntity<Root> getRoot() {
        final val root = new Root();
        root.add(
            linkTo(methodOn(SubscriptionController.class).subscribe(anySubscriptionDto))
                .withRel("subscriptions:create")
        );
        return new ResponseEntity<>(root, HttpStatus.OK);
    }

    private SubscriptionDto anySubscriptionDto() {
        try {
            return new SubscriptionDto(new URI(""), new TimePeriodDto(4, "seconds"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
