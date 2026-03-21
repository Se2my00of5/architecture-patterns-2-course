package ru.hits.core_service.ws;

import java.util.UUID;

public final class OperationWsTopics {

    private OperationWsTopics() {
    }

    public static String accountOperations(UUID accountId) {
        return "/topic/accounts/" + accountId + "/operations";
    }
}