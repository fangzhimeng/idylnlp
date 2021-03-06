/*******************************************************************************
 * Copyright 2019 Mountain Fog, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ai.idylnlp.test.models.opennlp.training;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.i18n.LanguageCode;

import ai.idylnlp.model.nlp.subjects.DefaultSubjectOfTrainingOrEvaluation;
import ai.idylnlp.model.nlp.subjects.SubjectOfTrainingOrEvaluation;
import ai.idylnlp.model.training.AccuracyEvaluationResult;
import ai.idylnlp.models.opennlp.training.PartOfSpeechModelOperations;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;

public class PartOfSpeechTokenModelOperationsTest {

  private static final Logger LOGGER = LogManager.getLogger(PartOfSpeechModelOperations.class);

  private static final String TRAINING_DATA_PATH = new File("src/test/resources/").getAbsolutePath();
  private static final String INPUT_FILE = TRAINING_DATA_PATH + File.separator + "pos-train.txt";
  private static final String SEPARATE_DATE_INPUT_FILE = TRAINING_DATA_PATH + File.separator + "pos-separate-data.txt";

  @Test
  public void perceptronModelSeparateDataEvaluateTest() throws IOException {

    PartOfSpeechModelOperations ops = new PartOfSpeechModelOperations();

    File temp = File.createTempFile("model", ".bin");

    final String modelOutputFile = temp.getAbsolutePath();

    SubjectOfTrainingOrEvaluation SubjectOfTrainingOrEvaluation = new DefaultSubjectOfTrainingOrEvaluation(INPUT_FILE);

    // First, create a model.
    final String modelId = ops.trainPerceptron(SubjectOfTrainingOrEvaluation, modelOutputFile, LanguageCode.en, 0, 5);

    assertNotNull(modelId);

    SubjectOfTrainingOrEvaluation SubjectOfTrainingOrEvaluationEvaluation = new DefaultSubjectOfTrainingOrEvaluation(SEPARATE_DATE_INPUT_FILE);

    // Do the cross validation.
    AccuracyEvaluationResult result = ops.separateDataEvaluate(SubjectOfTrainingOrEvaluationEvaluation, modelOutputFile);

    // Output will be like: Precision: 0.7; Recall: 0.30434782608695654; F-Measure: 0.42424242424242425
    LOGGER.info(result.toString());

    assertTrue(result != null);
    assertTrue(result.getWordAccuracy() > 0);
    assertTrue(result.getWordCount() > 0);

  }

  @Test
  @Ignore("This test fails to complete.")
  public void trainMaxEntQN() throws IOException {

    File temp = File.createTempFile("pos-model", ".bin");
    String modelOutputFile = temp.getAbsolutePath();

    LOGGER.info("Generating output model file to: {}", modelOutputFile);

    SubjectOfTrainingOrEvaluation SubjectOfTrainingOrEvaluation = new DefaultSubjectOfTrainingOrEvaluation(INPUT_FILE);

    PartOfSpeechModelOperations ops = new PartOfSpeechModelOperations();
    String modelId = ops.trainMaxEntQN(SubjectOfTrainingOrEvaluation, modelOutputFile, LanguageCode.en, 5, 1, 1, 1, 1, 1, 1);

    LOGGER.info("The generated model's ID is {}.", modelId);

    try {

      UUID uuid = UUID.fromString(modelId);

    } catch (IllegalArgumentException ex) {

      fail("The generated model ID is not a valid UUID.");

    }

    // Verify that the UUID returned matches what's in the model's properties.
    POSModel model = new POSModelLoader().load(new File(modelOutputFile));
    assertEquals(modelId, model.getManifestProperty("model.id"));

  }

  @Test
  public void trainPerceptron() throws IOException {

    File temp = File.createTempFile("pos-model", ".bin");
    String modelOutputFile = temp.getAbsolutePath();

    LOGGER.info("Generating output model file to: {}", modelOutputFile);

    SubjectOfTrainingOrEvaluation SubjectOfTrainingOrEvaluation = new DefaultSubjectOfTrainingOrEvaluation(INPUT_FILE);

    PartOfSpeechModelOperations ops = new PartOfSpeechModelOperations();
    String modelId = ops.trainPerceptron(SubjectOfTrainingOrEvaluation, modelOutputFile, LanguageCode.en, 0, 1);

    LOGGER.info("The generated model's ID is {}.", modelId);

    try {

      UUID uuid = UUID.fromString(modelId);

    } catch (IllegalArgumentException ex) {

      fail("The generated model ID is not a valid UUID.");

    }

    // Verify that the UUID returned matches what's in the model's properties.
    POSModel model = new POSModelLoader().load(new File(modelOutputFile));
    assertEquals(modelId, model.getManifestProperty("model.id"));

  }

}
