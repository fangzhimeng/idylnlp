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
package ai.idylnlp.model.extractors;

/**
 * A request to extract sentences from a HTML document.
 * @author Mountain Fog, Inc.
 *
 */
public class HtmlExtractionRequest extends TextExtractionRequest {
	
	private static final long serialVersionUID = -7494678513795383473L;
	
	private String html;

	/**
	 * Creates a HTML document sentence extraction request.
	 * @param html The HTML.
	 */
	public HtmlExtractionRequest(String html) {
			
		this.html = html;
		
	}

	/**
	 * Gets the HTML.
	 * @return The HTML.
	 */
	public String getHtml() {
		return html;
	}

}
