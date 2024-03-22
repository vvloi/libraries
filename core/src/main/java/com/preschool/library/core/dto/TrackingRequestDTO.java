package com.preschool.library.core.dto;

import com.preschool.library.webutils.RequestType;
import java.util.List;

public record TrackingRequestDTO(String requestId, MetadataTracking metadataTracking) {
    public record MetadataTracking(RequestType requestType, Request request, Response response) {}

    public record Request(String method, String url, List<Header> headers, String payload) {
        public record Header(String name, String value) {}
    }

    public record Response(String body) {}
}
