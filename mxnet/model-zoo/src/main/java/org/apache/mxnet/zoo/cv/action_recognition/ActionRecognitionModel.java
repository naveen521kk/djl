/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.apache.mxnet.zoo.cv.action_recognition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.mxnet.zoo.ModelNotFoundException;
import org.apache.mxnet.zoo.ModelZoo;
import org.apache.mxnet.zoo.ZooModel;
import software.amazon.ai.Model;
import software.amazon.ai.ndarray.NDList;
import software.amazon.ai.repository.Artifact;
import software.amazon.ai.repository.MRL;
import software.amazon.ai.repository.Metadata;
import software.amazon.ai.repository.Repository;
import software.amazon.ai.repository.VersionRange;

public class ActionRecognitionModel {
    private static final String ARTIFACT_ID = "inception";
    private static final MRL INCEPTION =
            new MRL(MRL.Model.CV.ACTION_RECOGNITION, ModelZoo.GROUP_ID, ARTIFACT_ID);

    private Repository repository;
    private Metadata metadata;

    public ActionRecognitionModel(Repository repository) {
        this.repository = repository;
    }

    private void locateMetadata() throws IOException, ModelNotFoundException {
        if (metadata == null) {
            metadata = repository.locate(INCEPTION);
            if (metadata == null) {
                throw new ModelNotFoundException("Simple Pose Models not found.");
            }
        }
    }

    public ZooModel<NDList, NDList> loadModel(Map<String, String> criteria)
            throws IOException, ModelNotFoundException {
        locateMetadata();
        Artifact artifact = match(criteria);
        if (artifact == null) {
            throw new ModelNotFoundException("Model not found.");
        }
        repository.prepare(artifact);
        Path dir = repository.getCacheDirectory();
        String relativePath = artifact.getResourceUri().getPath();
        Path modelPath = dir.resolve(relativePath);
        Model model = Model.loadModel(modelPath, artifact.getName());
        return new ZooModel<>(model, new ActionRecognitionTranslator());
    }

    public Artifact match(Map<String, String> criteria) {
        List<Artifact> list = search(criteria);
        if (list.isEmpty()) {
            return null;
        }

        list.sort(Artifact.COMPARATOR);
        return list.get(0);
    }

    public List<Artifact> search(Map<String, String> criteria) {
        return metadata.search(VersionRange.parse("0.0.1"), criteria);
    }
}
