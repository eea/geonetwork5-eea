/*
 * SPDX-FileCopyrightText: 2001 FAO-UN and others <geonetwork@osgeo.org>
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.geonetwork.ogcapi.service.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.geonetwork.index.model.record.IndexRecord;
import org.geonetwork.ogcapi.ctrlreturntypes.OgcApiRecordsMultiRecordResponse;
import org.geonetwork.ogcapi.ctrlreturntypes.OgcApiRecordsSingleRecordResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;

class RssCollectionMessageWriterTest {

    @Test
    void writesRssForItemsResponse() throws Exception {
        var writer = new RssCollectionMessageWriter();
        var contentType = MediaType.valueOf("application/rss+xml");
        assertTrue(writer.canWrite(OgcApiRecordsMultiRecordResponse.class, contentType));

        var indexRecord = new IndexRecord();
        indexRecord.setUuid("record-1");
        indexRecord.setResourceTitle(Map.of("default", "Record <1>"));
        indexRecord.setResourceAbstract(Map.of("default", "A & B"));
        indexRecord.setChangeDate("2025-06-23T22:21:13.857456Z");

        var item = new OgcApiRecordsSingleRecordResponse(
                "main",
                "record-1",
                indexRecord,
                null,
                URI.create("https://example.org/ogcapi-records/collections/main/items/record-1?f=geojson"));

        var response = new OgcApiRecordsMultiRecordResponse();
        response.setCatalogId("main");
        response.setRecords(List.of(item));
        response.setJsonLink(URI.create("https://example.org/ogcapi-records/collections/main/items?f=json"));

        var output = new MockHttpOutputMessage();
        writer.write(response, contentType, output);

        assertEquals(contentType, output.getHeaders().getContentType());
        var body = output.getBodyAsString(StandardCharsets.UTF_8);

        assertTrue(body.contains("<rss version=\"2.0\">"));
        assertTrue(body.contains("<title>GeoNetwork OGC API Records - main</title>"));
        assertTrue(body.contains("<title>Record &lt;1&gt;</title>"));
        assertTrue(body.contains("<description>A &amp; B</description>"));
        assertTrue(body.contains(
                "<link>https://example.org/ogcapi-records/collections/main/items/record-1?f=geojson</link>"));
        assertTrue(body.contains("<guid isPermaLink=\"false\">record-1</guid>"));
        assertTrue(body.contains("<pubDate>"));
    }
}
