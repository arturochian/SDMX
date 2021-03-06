/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/

package it.bancaditalia.oss.sdmx.parser.v20;

import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.util.Configuration;
import it.bancaditalia.oss.sdmx.util.LocalizedText;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Attilio Mattiocco
 *
 */
public class DataflowParser {
	protected static Logger logger = Configuration.getSdmxLogger();

	private static final String DATAFLOW = "Dataflow";
	private static final String ID = "id";
	private static final String AGENCY = "agencyID";
	private static final String VERSION = "version";
	private static final String NAME = "Name";
	private static final String KF_REF = "KeyFamilyRef";
	private static final String KF_ID = "KeyFamilyID";
	private static final String KF_AGID = "KeyFamilyAgencyID";
	private static final String KF_VER = "Version";

	public static List<Dataflow> parse(InputStreamReader xmlBuffer) throws XMLStreamException, UnsupportedEncodingException {
		List<Dataflow> dfList = new ArrayList<Dataflow>();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlBuffer);

		Dataflow df = null;
		LocalizedText currentName = new LocalizedText();

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();

				if (startElement.getName().getLocalPart() == (DATAFLOW)) {
					df = new Dataflow();
					@SuppressWarnings("unchecked")
					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attr = attributes.next();
						String id = null;
						String agency = null;
						String version = null;
						if (attr.getName().toString().equals(ID)) {
							id = attr.getValue();
							df.setId(id);
						}
						else if (attr.getName().toString().equals(AGENCY)) {
							agency = attr.getValue();
							df.setAgency(agency);
						}
						else if (attr.getName().toString().equals(VERSION)) {
							version = attr.getValue();
							df.setVersion(version);
						}
					}
				}
				else if (startElement.getName().getLocalPart() == (NAME)) {
					currentName.setText(startElement, eventReader);
				}
				else if (startElement.getName().getLocalPart() == (KF_REF)) {
					setKeyFamily(df, eventReader);
				}
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (DATAFLOW)) {
					df.setName(currentName.getText());
					dfList.add(df);
				}
			}

		}
		
		return dfList;
	}
	private static void setKeyFamily(Dataflow df, XMLEventReader eventReader) throws XMLStreamException{
		String id = null;
		String agency = null;
		String version = null;

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			logger.finest(event.toString());
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equalsIgnoreCase(KF_ID)) {
					id = eventReader.getElementText();
				}
				else if (startElement.getName().getLocalPart().equalsIgnoreCase(KF_AGID)) {
					agency = eventReader.getElementText();
				}
				else if (startElement.getName().getLocalPart().equalsIgnoreCase(KF_VER)) {
					version = eventReader.getElementText();
				}
				
			}
			if (event.isEndElement()) {
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart() == (KF_REF)) {
					DSDIdentifier dsd = new DSDIdentifier(id, agency, version);
					df.setDsdIdentifier(dsd);
					break;
				}
			}
		}
		
	}

} 

