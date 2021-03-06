/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.pipes.emitter.fs;

import org.apache.tika.config.Field;
import org.apache.tika.pipes.emitter.AbstractEmitter;
import org.apache.tika.pipes.emitter.Emitter;
import org.apache.tika.pipes.emitter.TikaEmitterException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.serialization.JsonMetadataList;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Emitter to write to a file system.
 *
 * This calculates the path to write to based on the {@link #basePath}
 * and the value of the {@link TikaCoreProperties#SOURCE_PATH} value.
 *
 * <pre class="prettyprint">
 *  &lt;properties&gt;
 *      &lt;emitters&gt;
 *          &lt;emitter class="org.apache.tika.pipes.emitter.fs.FileSystemEmitter&gt;
 *              &lt;params&gt;
 *                  &lt;!-- required --&gt;
 *                  &lt;param name="name" type="string"&gt;fs&lt;/param&gt;
 *                  &lt;!-- required --&gt;
 *                  &lt;param name="basePath" type="string"&gt;/path/to/output&lt;/param&gt;
 *                  &lt;!-- optional; default is 'json' --&gt;
 *                  &lt;param name="fileExtension" type="string"&gt;json&lt;/param&gt;
 *              &lt;/params&gt;
 *          &lt;/emitter&gt;
 *      &lt;/emitters&gt;
 *  &lt;/properties&gt;</pre>
 */
public class FileSystemEmitter extends AbstractEmitter {

    private Path basePath = null;
    private String fileExtension = "json";


    @Override
    public void emit(List<Metadata> metadataList) throws IOException, TikaException {
        Path output;
        if (metadataList == null || metadataList.size() == 0) {
            throw new TikaEmitterException("metadata list must not be null or of size 0");
        }

        String relPath = metadataList.get(0)
                .get(TikaCoreProperties.SOURCE_PATH);
        if (relPath == null) {
            throw new TikaEmitterException("Must specify a "+TikaCoreProperties.SOURCE_PATH.getName() +
                    " in the metadata in order for this emitter to generate the output file path.");
        }
        if (fileExtension != null && fileExtension.length() > 0) {
            relPath += "." + fileExtension;
        }
        if (basePath != null) {
            output = basePath.resolve(relPath);
        } else {
            output = Paths.get(relPath);
        }

        if (!Files.isDirectory(output.getParent())) {
            Files.createDirectories(output.getParent());
        }
        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            JsonMetadataList.toJson(metadataList, writer);
        }
    }

    @Field
    public void setBasePath(String basePath) {
        this.basePath = Paths.get(basePath);
    }

    /**
     * If you want to customize the output file's file extension.
     * Do not include the "."
     * @param fileExtension
     */
    @Field
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
