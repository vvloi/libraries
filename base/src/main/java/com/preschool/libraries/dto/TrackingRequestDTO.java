package com.preschool.libraries.dto;

import com.preschool.libraries.eumeration.RequestType;
import java.util.List;

public record TrackingRequestDTO(String requestId, MetadataTracking metadataTracking) {
    public record MetadataTracking(RequestType requestType, Request request, Response response) {}

    public record Request(String method, String endpoint, List<Header> headers, String payload) {
        public record Header(String name, String value) {}
    }

    public record Response(String status, String body) {}
}
