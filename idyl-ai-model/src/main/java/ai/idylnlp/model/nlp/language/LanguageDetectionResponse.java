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
package ai.idylnlp.model.nlp.language;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class LanguageDetectionResponse {
	
	private List<Pair<String, Double>> languages;
	
	public LanguageDetectionResponse(List<Pair<String, Double>> languages) {
		
		this.languages = languages;

	}

	public List<Pair<String, Double>> getLanguages() {
		return languages;
	}

	public void setLanguages(List<Pair<String, Double>> languages) {
		this.languages = languages;
	}
	
}
