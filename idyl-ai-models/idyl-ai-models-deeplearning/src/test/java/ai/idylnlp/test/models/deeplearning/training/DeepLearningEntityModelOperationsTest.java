/*******************************************************************************
 * Copyright 2018 Mountain Fog, Inc.
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
package ai.idylnlp.test.models.deeplearning.training;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.mtnfog.entity.Entity;
import com.neovisionaries.i18n.LanguageCode;

import ai.idylnlp.model.ModelValidator;
import ai.idylnlp.model.manifest.ModelManifestUtils;
import ai.idylnlp.model.manifest.SecondGenModelManifest;
import ai.idylnlp.model.nlp.EntityExtractionRequest;
import ai.idylnlp.model.nlp.EntityExtractionResponse;
import ai.idylnlp.models.deeplearning.training.DeepLearningEntityModelOperations;
import ai.idylnlp.models.deeplearning.training.model.DeepLearningTrainingDefinition;
import ai.idylnlp.models.deeplearning.training.model.EvaluationData;
import ai.idylnlp.models.deeplearning.training.model.HyperParameters;
import ai.idylnlp.models.deeplearning.training.model.Layer;
import ai.idylnlp.models.deeplearning.training.model.Layers;
import ai.idylnlp.models.deeplearning.training.model.Monitoring;
import ai.idylnlp.models.deeplearning.training.model.NetworkConfigurationParameters;
import ai.idylnlp.models.deeplearning.training.model.Output;
import ai.idylnlp.models.deeplearning.training.model.RegularizationParameters;
import ai.idylnlp.models.deeplearning.training.model.TrainingData;
import ai.idylnlp.models.deeplearning.training.model.UpdaterParameters;
import ai.idylnlp.nlp.recognizer.DeepLearningEntityRecognizer;
import ai.idylnlp.nlp.recognizer.configuration.DeepLearningEntityRecognizerConfiguration;
import ai.idylnlp.testing.markers.ExternalData;
import ai.idylnlp.testing.markers.HighMemoryUsage;

public class DeepLearningEntityModelOperationsTest {

	private static final Logger LOGGER = LogManager.getLogger(DeepLearningEntityModelOperationsTest.class);

	private static final String TRAINING_DATA_PATH = System.getProperty("testDataPath");

	@Category({ExternalData.class, HighMemoryUsage.class})
	public void trainAndUseOpenNLPFormat() throws Exception {

		DeepLearningTrainingDefinition definition = getSampleDefinition();

		DeepLearningEntityModelOperations ops = new DeepLearningEntityModelOperations();

		long startTime = System.currentTimeMillis();

		String modelId = ops.train(definition);

		LOGGER.info("Elapsed time: {}", (System.currentTimeMillis() - startTime));

		//  Generate a model manifest.
		SecondGenModelManifest manifest = new SecondGenModelManifest(modelId, definition.getOutput().getOutputFile(), LanguageCode.en,
				"person", "model", "2", definition.getTrainingData().getWordVectorsFile(),
				definition.getHyperParameters().getWindowSize(), "", "");

		File secondGenModelManifest = File.createTempFile("model", ".manifest");
		ModelManifestUtils.generateSecondGenModelManifest(secondGenModelManifest, manifest);
		LOGGER.info("Model manifest written to {}", secondGenModelManifest.getAbsolutePath());

		// Use the model.

		ModelValidator modelValidator = Mockito.mock(ModelValidator.class);
		
		when(modelValidator.validateVersion(any(String.class))).thenReturn(true);

		DeepLearningEntityRecognizerConfiguration configuration = new  DeepLearningEntityRecognizerConfiguration.Builder().build(TRAINING_DATA_PATH);

		configuration.addEntityModel("person", LanguageCode.en, manifest);

		DeepLearningEntityRecognizer recognizer = new DeepLearningEntityRecognizer(configuration);

		String input = "George Washington was president.";
		String[] text = input.split(" ");
		
		EntityExtractionRequest request = new EntityExtractionRequest(text)
			.withType("person")
			.withLanguage(LanguageCode.en);

		EntityExtractionResponse response = recognizer.extractEntities(request);

		//assertTrue(response.getEntities().size() > 0);
		//assertTrue(response.getExtractionTime() > 0);

		for(Entity entity : response.getEntities()) {
			LOGGER.info(entity.toString());
		}

	}
	
	private DeepLearningTrainingDefinition getSampleDefinition() throws IOException {

		UpdaterParameters updaterParameters = new UpdaterParameters();
		updaterParameters.setUpdater("rmsprop");

		RegularizationParameters regularizationParameters = new RegularizationParameters();
		regularizationParameters.setRegularization(true);
		regularizationParameters.setL2(1e-5);

		Map<String, Double> learningRateSchedule = new HashMap<String, Double>();
		learningRateSchedule.put("0", 0.01);
		learningRateSchedule.put("1000", 0.005);
		learningRateSchedule.put("2000", 0.001);
		learningRateSchedule.put("3000", 0.0001);
		learningRateSchedule.put("4000", 0.00001);		
		
		Layer layer1 = new Layer();
		layer1.setLearningRate(0.00001);
		layer1.setLearningRateSchedule(learningRateSchedule);
		
		Layer layer2 = new Layer();
		layer2.setLearningRate(0.00001);
		layer2.setLearningRateSchedule(learningRateSchedule);

		Layers layers = new Layers(layer1, layer2);

		NetworkConfigurationParameters networkConfigurationParameters = new NetworkConfigurationParameters();
		networkConfigurationParameters.setOptimizationAlgorithm("stochastic_gradient_descent");
		networkConfigurationParameters.setGradientNormalization("clipelementwiseabsolutevalue");
		networkConfigurationParameters.setGradientNormalizationThreshold(1.0);
		networkConfigurationParameters.setUpdaterParameters(updaterParameters);
		networkConfigurationParameters.setRegularizationParameters(regularizationParameters);
		networkConfigurationParameters.setLayers(layers);
		networkConfigurationParameters.setPretrain(false);
		networkConfigurationParameters.setBackprop(true);
		networkConfigurationParameters.setWeightInit("xavier");
		
		HyperParameters hyperParameters = new HyperParameters();
		hyperParameters.setEpochs(1);
		hyperParameters.setWindowSize(5);
		hyperParameters.setSeed(1497630814976308L);
		hyperParameters.setBatchSize(32);
		hyperParameters.setNetworkConfigurationParameters(networkConfigurationParameters);

		Monitoring monitoring = new Monitoring();
		monitoring.setScoreIteration(100);
		
		//hyperParameters.setWordVectorsFile(TRAINING_DATA_PATH + "/glove.6B.50d.glv");
		String wordVectorsFile = TRAINING_DATA_PATH + "/reuters-vectors.txt";

		DeepLearningTrainingDefinition definition = new DeepLearningTrainingDefinition();
		definition.setTrainingData(new TrainingData("conll2003", TRAINING_DATA_PATH + "/conll2003-eng.train", wordVectorsFile));
		definition.setEvaluationData(new EvaluationData("conll2003", TRAINING_DATA_PATH + "/conll2003-eng.testa"));
		definition.setOutput(new Output(File.createTempFile("multilayernetwork", ".zip").getAbsolutePath(), "/tmp/stats.dl4j"));
		//definition.setEarlyTermination(new EarlyTermination(20, 180));
		definition.setEntityType("person");
		definition.setHyperParameters(hyperParameters);
		definition.setMonitoring(monitoring);
		
		return definition;

	}

}
