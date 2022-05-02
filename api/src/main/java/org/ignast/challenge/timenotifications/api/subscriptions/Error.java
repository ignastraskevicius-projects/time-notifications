package org.ignast.challenge.timenotifications.api.subscriptions;

record Error(String error) {
    static Error uriAlreadyExists() {
        return new Error("URI is already registered");
    }
}
