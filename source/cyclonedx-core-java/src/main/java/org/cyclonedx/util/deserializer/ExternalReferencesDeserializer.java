/*
 * This file is part of CycloneDX Core (Java).
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.cyclonedx.util.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.cyclonedx.model.ExternalReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExternalReferencesDeserializer extends JsonDeserializer<List<ExternalReference>> {

    private final HashesDeserializer hashesDeserializer = new HashesDeserializer();

    @Override
    public List<ExternalReference> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node.has("reference")) {
            return parseExternalReferences(node.get("reference"), parser, context);
        } else {
            return parseExternalReferences(node, parser, context);
        }
    }

    private List<ExternalReference> parseExternalReferences(JsonNode node, JsonParser p, DeserializationContext ctxt) throws IOException {
        List<ExternalReference> references = new ArrayList<>();
        ArrayNode nodes = DeserializerUtils.getArrayNode(node, null);
        for (JsonNode resolvesNode : nodes) {
            ExternalReference type = parseExternalReference(resolvesNode, p, ctxt);
            references.add(type);
        }
        return references;
    }

    private ExternalReference parseExternalReference(JsonNode node, JsonParser p, DeserializationContext ctxt) throws IOException {
        ExternalReference reference = new ExternalReference();
        if (node.has("url")) {
            reference.setUrl(node.get("url").asText());
        }
        if (node.has("type")) {
            reference.setType(ExternalReference.Type.fromString(node.get("type").asText()));
        }
        if (node.has("comment")) {
            reference.setComment(node.get("comment").asText());
        }
        if (node.has("hashes")) {
            JsonParser hashesParser = node.get("hashes").traverse(p.getCodec());
            hashesParser.nextToken();
            reference.setHashes(hashesDeserializer.deserialize(hashesParser, ctxt));
        }
        return reference;
    }

}
